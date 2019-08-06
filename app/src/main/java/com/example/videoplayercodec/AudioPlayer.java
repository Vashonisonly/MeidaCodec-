package com.example.videoplayercodec;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";
    Context mContext;
    private AssetFileDescriptor mfileDescriptor;
    NewAudioDecodeThread audioDecodeThread;


    public AudioPlayer(AssetFileDescriptor fileDescriptor){
        mfileDescriptor = fileDescriptor;
        Log.d(TAG,"audio start");
    }

//    public void getFileDescriptor(){
//        try {
//            AssetManager assetManager = mContext.getAssets();
//            fileDescriptor = assetManager.openFd("test.mp4");
//        }catch (IOException e){
//            Log.d(TAG, e.getMessage());
//        }
//    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"audio start1");
                if(audioDecodeThread == null){
                    audioDecodeThread = new NewAudioDecodeThread(mfileDescriptor.getFileDescriptor(),mfileDescriptor.getStartOffset(),mfileDescriptor.getLength());
                    audioDecodeThread.start();
                }else {
                    Log.d(TAG,"start when videoDecodeThread start");
                }
            }
        }).start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG,"audio start2");
//                if(audioDecodeThread == null){
//                    audioDecodeThread = new NewAudioDecodeThread(mfileDescriptor);
//                    audioDecodeThread.start();
//                }else {
//                    Log.d(TAG,"start when videoDecodeThread start");
//                }
//            }
//        }).start();
    }
}
