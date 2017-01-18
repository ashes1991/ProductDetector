package com.kovtun.producdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kovtun on 23.06.2016.
 */
public class ImageLoader {
    private static String DirPath=null;
    private static FileFilter filterPictures=new FileFilter() {
        private String[] files=new String[]{"bmp",
                "jpg","jpeg","png","gif"};

        @Override
        public boolean accept(File pathname) {
            for (int i=0;i<files.length;i++)
            {
                if(pathname.isDirectory()){
                    return true;
                }else if (pathname.getName().toLowerCase().endsWith(files[i]))
                {
                    return true;
                }
            }
            return false;
        }
    };

    private static void getDirPath(){
        if(DirPath == null) {
            File sdPath = Environment.getExternalStorageDirectory();
            sdPath = new File(sdPath.getAbsolutePath() + "/ProductDetector");
            sdPath.mkdirs();
            sdPath = new File(sdPath.getAbsolutePath() + "/adv_pic");
            sdPath.mkdirs();
            DirPath = sdPath.getAbsolutePath();
        }

    }

    public static Bitmap readDisplayPicture(Context context) throws IOException {
        if (DirPath==null){
            getDirPath();
        }
        File file=new File(new File(DirPath).getParent()+"/disp_pic.jpg");
        Bitmap out=null;
        if (file.exists()){
            out=BitmapFactory.decodeFile(file.getAbsolutePath());
        }else{
            out= BitmapFactory.decodeResource(context.getResources(),R.drawable.disp_pic);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            out.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            fileOutputStream.write(stream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        return out;
    }

    public static File[] getPictures(){
        File[] files=null;
        if (DirPath==null){
            getDirPath();
        }
        File file=new File(DirPath);
        files=file.listFiles(filterPictures);
        return files;
    }
}
