package com.example.videoplayercodec;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;


public class Utils {
    private static final String TAG = "Utils";
    //private Context mContex;

//    public Utils(Context context){
//        mContex = context;
//    }
//
//    public AssetFileDescriptor getFileDescriptor(){
//
//    }

    //获取指定类型媒体文件所在的通道
    public static int getMediaTrackIndex(MediaExtractor mediaExtractor, String MEDIA_TYPE) {
        int trackIndex = -1;
        //获取轨道数量
        int trackNum = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackNum; i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(MEDIA_TYPE)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }

    //传递数据到缓冲区
    public static boolean putBufferToMediaCodec(MediaExtractor mediaExtractor, MediaCodec decoder, ByteBuffer inputBuffer,int inputBufferIndex) {
        boolean isMediaFinish = false;
        int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
        if (sampleSize < 0) {
            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            isMediaFinish = true;
            Log.d("Utils", "media finished");
        } else {
            decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
            //准备下一个单位的数据
            boolean advance = mediaExtractor.advance();
            if (!advance) {
                isMediaFinish = false;
            }
        }

//        //解码器输出缓冲器可用位置的索引
//        int inputBufferIndex = decoder.dequeueInputBuffer(10000);
//        if (inputBufferIndex >= 0) {
//            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
//            if (sampleSize < 0) {
//                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                isMediaFinish = true;
//                Log.d("Utils", "media finished");
//            } else {
//                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
//                //准备下一个单位的数据
//                boolean ad = mediaExtractor.advance();
//                if (!ad) {
//                    isMediaFinish = false;
//                }
//            }
//        } else {
//            Log.d(TAG, "buffer is full");
//        }
       return isMediaFinish;
    }

    //休息一下
    public static void sleepRender(MediaCodec.BufferInfo audioBufferInfo, long startMs) {
        while (audioBufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.d(TAG, "process error:" + e.getMessage());
                break;
            }
        }
    }
}

