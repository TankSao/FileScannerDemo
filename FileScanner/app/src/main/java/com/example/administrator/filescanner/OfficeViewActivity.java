package com.example.administrator.filescanner;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/11/20.
 */

public class OfficeViewActivity  extends AppCompatActivity implements TbsReaderView.ReaderCallback{
    //文件路径
    private String path = "";
    private TbsReaderView mTbsReaderView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_office_view);
        ButterKnife.bind(this);
        path = getIntent().getStringExtra("path");
        Log.e("path",path+"s");
        mTbsReaderView = new TbsReaderView(this, this);
        RelativeLayout rootRl = (RelativeLayout) findViewById(R.id.rl_root);
        rootRl.addView(mTbsReaderView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        File file = new File(path);
        openFile(file);
    }

    private void openFile(File file) {
        Bundle bundle = new Bundle();
        bundle.putString("filePath", file.getPath());
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        boolean result = mTbsReaderView.preOpen(parseFormat(file.getName()), false);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }
    }
    private String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }
}
