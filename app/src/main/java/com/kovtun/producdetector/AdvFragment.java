package com.kovtun.producdetector;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class AdvFragment extends Fragment {
    private GifImageView advImage;
    private int indexTimer=0;
    private int timeReklam=10;
    private Timer timer;
    private TimerTask task;
    private Handler handler;
    private GifDrawable gifFromPath;
    private Bitmap bitmap;
    private int len;
    private int index=0;
    private File[] files;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_adv, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        advImage=(GifImageView)getActivity().findViewById(R.id.GifImageView);

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==1){
                    advImage.setImageDrawable(gifFromPath);
                }else if (msg.what==2){
                    advImage.setImageBitmap(bitmap);
                }
                super.handleMessage(msg);
            }
        };
        files=ImageLoader.getPictures();
        if (files.length>0){
            len=files.length;
           showAdv();
        }  else {
            if (getFragmentManager().getBackStackEntryCount()>1)
                getFragmentManager().popBackStack();
        }
    }

    private void start_timer(){
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                indexTimer++;
                Log.d("myLOG",indexTimer + "  "+ timeReklam);

                if(indexTimer >= timeReklam)
                {
                    showAdv();
                }

            }
        };

        timer.scheduleAtFixedRate(task, 10, 1000);
    }

    private void stop_timer(){
        timer.cancel();
        task.cancel();
        indexTimer=0;
    }

    private void showAdv() {
        if (timer!=null){
            stop_timer();
        }
        if (index<len){
            downloadFile(files[index]);
            index++;
        }else {
            index=0;
            downloadFile(files[index]);
            index++;
        }
        start_timer();
    }

    private void downloadFile(File file){
         if (file.exists()){
             System.out.println(file.getPath().indexOf("gif"));
             if (file.getPath().indexOf("gif")>0){
                 try {

                     gifFromPath=new GifDrawable( file );
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 handler.sendEmptyMessage(1);
             }else {
                 bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                 handler.sendEmptyMessage(2);
             }

         }
    }

    @Override
    public void onDestroy() {
        stop_timer();
        super.onDestroy();
    }
}
