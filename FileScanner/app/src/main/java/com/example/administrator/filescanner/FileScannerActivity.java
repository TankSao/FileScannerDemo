package com.example.administrator.filescanner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/11/20.
 */

public class FileScannerActivity extends AppCompatActivity implements OnDeleteListener{
    @BindView(R.id.files)
    ListView listView;
    //文件路径
    private String path = "";
    //存储文件名称
    private ArrayList<String> names = null;
    //存储文件路径
    private ArrayList<String> paths = null;
    private FileScanAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_scan);
        ButterKnife.bind(this);
        path = getIntent().getStringExtra("path");
        Log.e("path",path+"s");
        getFileList(path);
        adapter = new FileScanAdapter(FileScannerActivity.this,names,paths);
        adapter.setOnDeleteListener(this);
        listView.setAdapter(adapter);
    }
    @OnClick({R.id.back,R.id.delete})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.delete:
                String msg = "是否清空目录\n不可恢复";
                confirmDelete(msg,path);
                break;
        }
    }

    private void confirmDelete(String msg,final String pa) {
        new ConfirmDialog(FileScannerActivity.this,null, msg, null,new ConfirmDialog.AlertDialogUser() {

            @Override
            public void onResult(boolean confirmed, Bundle bundle) {
                if(confirmed){
                    try {
                        File files = new File(pa);
                        deleteDirWihtFile(files);
                        getFileList(path);
                        adapter = new FileScanAdapter(FileScannerActivity.this,names,paths);
                        adapter.setOnDeleteListener(FileScannerActivity.this);
                        listView.setAdapter(adapter);
                    }catch (Exception e){
                        Log.e("error",e.getMessage()+"s");
                        Toast.makeText(FileScannerActivity.this,"目录清空失败:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, true).show();
    }

    //删除目录
    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            dir.delete();
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    //获取文件列表
    private void getFileList(String path){
        names = new ArrayList<String>();
        paths = new ArrayList<String>();
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        File[] files = file.listFiles();
        Log.e("fileLen",files.length+"s");
        //添加所有文件
        for (File f : files){
            names.add(f.getName());
            paths.add(f.getPath());
        }
    }

    @Override
    public void onDelete(int position) {
        String msg = "是否删除文件\n不可恢复";
        Log.e("pathpath",paths.get(position)+"");
        if(paths.size()>0) {
            confirmDelete(msg, paths.get(position));
        }else{
            Toast.makeText(FileScannerActivity.this,"文件不存在",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSelect(int position) {
        File file = new File(paths.get(position));
        if(FileType.isDocs(names.get(position))){
            Intent intent = new Intent(this,OfficeViewActivity.class);
            intent.putExtra("path",paths.get(position));
            startActivity(intent);
        }else {
            openFile(this, file);
        }
    }
    private static void grantUriPermission (Context context, Uri fileUri, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
    public void openFile(Context context, File file){

        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            //设置intent的data和Type属性。android 7.0以上crash,改用provider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName()+".provider", file);//android 7.0以上
                intent.setDataAndType(fileUri, type);
                grantUriPermission(context, fileUri, intent);
            }else {
                intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
            }
            //跳转
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private String getMIMEType(File file) {

        String type="*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0)
            return type;
    /* 获取文件的后缀名 */
        String fileType = fName.substring(dotIndex,fName.length()).toLowerCase();
        if(fileType == null || "".equals(fileType))
            return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){
            if(fileType.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }
    private static final String[][] MIME_MapTable={
            //{后缀名，    MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",      "image/bmp"},
            {".c",        "text/plain"},
            {".class",    "application/octet-stream"},
            {".conf",    "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",    "application/x-gtar"},
            {".gz",        "application/x-gzip"},
            {".h",        "text/plain"},
            {".htm",    "text/html"},
            {".html",    "text/html"},
            {".jar",    "application/java-archive"},
            {".java",    "text/plain"},
            {".jpeg",    "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js",        "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",    "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",    "video/mp4"},
            {".mpga",    "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".prop",    "text/plain"},
            {".rar",    "application/x-rar-compressed"},
            {".rc",        "text/plain"},
            {".rmvb",    "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh",        "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            //{".xml",    "text/xml"},
            {".xml",    "text/plain"},
            {".z",        "application/x-compress"},
            {".zip",    "application/zip"},
            {"",        "*/*"}
    };
}
