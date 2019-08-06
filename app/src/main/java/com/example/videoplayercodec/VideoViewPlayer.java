package com.example.videoplayercodec;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class VideoViewPlayer {
    public final static String TAG = "VideoViewPlayer";
    private NewVideoDecodeThread videoDecodeThread;
    //private Context mContext;
    private SurfaceView msurfaceView;
    private SurfaceHolder surfaceHolder;
    private AssetFileDescriptor mfileDescriptor;
    private boolean iscreated = false;

    //constructor
    public VideoViewPlayer(AssetFileDescriptor fileDescriptor, SurfaceView surfaceView){
//        super(context);
//        getHolder().addCallback(this);
//        mContext = context;
        //getFileDescriptor();
        mfileDescriptor = fileDescriptor;
        msurfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.d(TAG,"surfaceCreated");
                iscreated = true;
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

    }
//    public VideoViewPlayer(Context context, AttributeSet attributeSet){
//        super(context,attributeSet);
//        getHolder().addCallback(this);
//        mContext = context;
//        getFileDescriptor();
//    }
//    public VideoViewPlayer(Context context, AttributeSet attrs, int defStyle){
//        super(context, attrs, defStyle);
//        getHolder().addCallback(this);
//        mContext = context;
//        getFileDescriptor();
//    }
//

//    //callback
//    @Override
//    public void surfaceCreated(SurfaceHolder holder){
//        Log.d(TAG,"surfaceCreated");
//        iscreated = true;
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int heigt){
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder){
//
//    }

//    public void getFileDescriptor(){
//        try {
//            AssetManager assetManager = mContext.getAssets();
//            mfileDescriptor = assetManager.openFd("video.mp4");
//        }catch (IOException e){
//            Log.d(TAG, e.getMessage());
//        }
//    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!iscreated){
                        Thread.sleep(1000);
                    }
                }catch (InterruptedException e){

                }
                if (iscreated) {
                    if (videoDecodeThread == null) {
                        SurfaceHolder surfaceHolder1 = surfaceHolder;
                        videoDecodeThread = new NewVideoDecodeThread(surfaceHolder1.getSurface(), mfileDescriptor);
                        synchronized (videoDecodeThread){
                            videoDecodeThread.start();
                        }
                    } else {
                        Log.d(TAG, "start when videoDecodeThread start");
                    }
                } else {
                    Log.d(TAG, "not created");
                }
            }

        }).start();

    }
}
