package com.example.administrator.filescanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018/11/20.
 */

public class FileScanAdapter  extends BaseAdapter {
    private LayoutInflater inflater;
    //存储文件名称
    private ArrayList<String> names = null;
    //存储文件路径
    private ArrayList<String> paths = null;
    private OnDeleteListener listener;
    public void setOnDeleteListener(OnDeleteListener listener){
        this.listener = listener;
    }
    //参数初始化
    public FileScanAdapter(Context context, ArrayList<String> na, ArrayList<String> pa){
        names = na;
        paths = pa;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return names.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return names.get(position);
    }
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (null == convertView){
            convertView = inflater.inflate(R.layout.file_scan_item, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.delete = (ImageView)convertView.findViewById(R.id.delete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        File f = new File(paths.get(position).toString());
        holder.name.setText(names.get(position).toString());
        String type = FileType.getFileType(names.get(position).toString());
        switch (type){
            case FileType.PICTURE:
                holder.icon.setImageResource(R.mipmap.file_picture);
                break;
            case FileType.AUDIO:
                holder.icon.setImageResource(R.mipmap.file_audio);
                break;
            case FileType.VIDEO:
                holder.icon.setImageResource(R.mipmap.file_video);
                break;
            case FileType.TXT:
                holder.icon.setImageResource(R.mipmap.file_txt);
                break;
            case FileType.WORD:
                holder.icon.setImageResource(R.mipmap.file_word);
                break;
            case FileType.PPT:
                holder.icon.setImageResource(R.mipmap.file_ppt);
                break;
            case FileType.PDF:
                holder.icon.setImageResource(R.mipmap.file_pdf);
                break;
            case FileType.EXCEL:
                holder.icon.setImageResource(R.mipmap.file_excel);
                break;
            case FileType.DEFAULTTYPE:
                holder.icon.setImageResource(R.mipmap.file_default);
                break;
        }
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null){
                    listener.onDelete(position);
                }
            }
        });
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null){
                    listener.onSelect(position);
                }
            }
        });
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null){
                    listener.onSelect(position);
                }
            }
        });
        return convertView;
    }
    private class ViewHolder{
        private TextView name;
        private ImageView icon;
        private ImageView delete;
    }
}