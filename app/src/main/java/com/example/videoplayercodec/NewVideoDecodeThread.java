package com.example.videoplayercodec;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NewVideoDecodeThread extends Thread {
    public static final String TAG = "VideoDecodeThread";
    private boolean isVideoFinish = false;
    int frameIndex = 0;
    AssetFileDescriptor mFileDescriptor;
    Surface mSurface;
    MediaFormat mMediaFormat;
    MediaCodec videoCodec;
    long startMs;
    MediaExtractor videoExtractor;
    MediaCodecCallBack mediaCodecCallBack;

    public NewVideoDecodeThread(Surface surface, AssetFileDescriptor fileDescriptor) {
        Log.d(TAG,"VideoThread Thread: "+currentThread());
        mSurface = surface;
        mFileDescriptor = fileDescriptor;
        startMs = System.currentTimeMillis();
    }

    public class MediaCodecCallBack extends MediaCodec.Callback{
        @Override
        public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(i);
            inputBuffer.clear();
            Log.d(TAG,"VideoThread currentThreadVideoInput: "+currentThread());
            if (!isVideoFinish) {
                //只要视频没有结束，就连续提取一段视频帧放到mediacodec的视频缓冲区中
                int sampleSize = videoExtractor.readSampleData(inputBuffer, 0);
                if (sampleSize < 0) {
                    mediaCodec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.d("Utils", "media finished");
                } else {
                    long sampleTime = videoExtractor.getSampleTime();
                    Log.d(TAG,"sample time: "+sampleTime);
                    mediaCodec.queueInputBuffer(i, 0, sampleSize, sampleTime, 0);
                    //准备下一个单位的数据
                    boolean advance = videoExtractor.advance();
                    if (!advance) {
                        isVideoFinish = false;
                    }
                }
                //isVideoFinish = Utils.putBufferToMediaCodec(videoExtractor, mediaCodec, inputBuffer, i);
            }
        }
        @Override
        public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {
            if(videoCodec == mediaCodec){
                Log.d(TAG,"same codec. currentThread"+currentThread());
            }
            synchronized (mediaCodec){
                while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                    continue;
//                        try {
//                            Log.d(TAG,"sleep~~ "+bufferInfo.presentationTimeUs/1000);
//                            mediaCodec.wait(4000);
//                            //sleep(1000);
//                            //currentThread().wait(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                            break;
//                        }
                }
            }

            mediaCodec.releaseOutputBuffer(i,true);
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.v(TAG, "buffer stream out");
            }
        }
        @Override
        public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
        }
        @Override
        public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,5);
            mMediaFormat = mediaFormat;
            int frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            //mMediaFormat = mediaFormat;
            Log.d(TAG,"format changed: "+frameRate);
        }
    }

    @Override
    public void run() {
        Log.d(TAG,"VideoThread currentThreadVideo: "+currentThread());
        videoExtractor = new MediaExtractor();
        setTrackAndCodecInfo(videoExtractor);
        long duration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG,"duration is :"+duration);

        if (videoCodec == null) {
            Log.d(TAG, "videoCodec is null");
        }
        mediaCodecCallBack = new MediaCodecCallBack();
        videoCodec.setCallback(mediaCodecCallBack);
//        videoCodec.setCallback(new MediaCodec.Callback() {
//            @Override
//            public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
//                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(i);
//                inputBuffer.clear();
//
//                if (!isVideoFinish) {
//                    //只要视频没有结束，就连续提取一段视频帧放到mediacodec的视频缓冲区中
//                    int sampleSize = videoExtractor.readSampleData(inputBuffer, 0);
//                    if (sampleSize < 0) {
//                        mediaCodec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                        Log.d("Utils", "media finished");
//                    } else {
//                        long sampleTime = videoExtractor.getSampleTime();
//                        Log.d(TAG,"sample time: "+sampleTime);
//                        mediaCodec.queueInputBuffer(i, 0, sampleSize, sampleTime, 0);
//                        //准备下一个单位的数据
//                        boolean advance = videoExtractor.advance();
//                        if (!advance) {
//                            isVideoFinish = false;
//                        }
//                    }
//                    //isVideoFinish = Utils.putBufferToMediaCodec(videoExtractor, mediaCodec, inputBuffer, i);
//                }
//            }
//
//            @Override
//            public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {
////                synchronized (currentThread()){
////                    try{
////                        currentThread().wait(40);
////                    }catch (InterruptedException e){
////                        Log.d(TAG,e.getMessage());
////                    }
////                }
//
//                if(videoCodec == mediaCodec){
//                    Log.d(TAG,"same codec. currentThread"+currentThread());
//                }
//                synchronized (mediaCodec){
//                    while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
//                        continue;
////                        try {
////                            Log.d(TAG,"sleep~~ "+bufferInfo.presentationTimeUs/1000);
////                            mediaCodec.wait(4000);
////                            //sleep(1000);
////                            //currentThread().wait(10);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                            break;
////                        }
//                    }
//                }
//
//                mediaCodec.releaseOutputBuffer(i,true);
//
////                switch (i) {
////                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
////                        Log.v(TAG, "format changed");
////                        break;
////                    case MediaCodec.INFO_TRY_AGAIN_LATER:
////                        Log.d(TAG, "video time out");
////                        break;
////                    default:
//////                        long startMs = System.currentTimeMillis();
//////                        Utils.sleepRender(bufferInfo, startMs);
////
////                        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(i);
////                        byte[] buffer = new byte[outputBuffer.remaining()];
////                        Log.d(TAG,"video buffer: "+buffer[80]);
////                        outputBuffer.get(buffer);
////                        mediaCodec.releaseOutputBuffer(i,false);
////
////                        frameIndex++;
////                        Log.d(TAG, "frameIndex: " + frameIndex);
////                        break;
////                }
//
//
//                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.v(TAG, "buffer stream out");
//                }
//            }
//
//            @Override
//            public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
//                Log.e(TAG, e.getMessage());
//            }
//
//            @Override
//            public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
//                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,5);
//                mMediaFormat = mediaFormat;
//                int frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
//                //mMediaFormat = mediaFormat;
//                Log.d(TAG,"format changed: "+frameRate);
//            }
//        });
        videoCodec.configure(mMediaFormat,mSurface,null,0);
        mMediaFormat = videoCodec.getOutputFormat();
        videoCodec.start();
    }

    public void setTrackAndCodecInfo(MediaExtractor videoExtractor) {
        try {
            videoExtractor.setDataSource(mFileDescriptor.getFileDescriptor(), mFileDescriptor.getStartOffset(), mFileDescriptor.getLength());
            Log.d(TAG, "setDataSource over");

            //获取视频所在的轨道
            int trackIndex = Utils.getMediaTrackIndex(videoExtractor, "video/");
            if (trackIndex >= 0) {
                mMediaFormat = videoExtractor.getTrackFormat(trackIndex);
                //指定解码后的帧格式
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                //mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 2);
                //mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
                String mimeType = mMediaFormat.getString(MediaFormat.KEY_MIME);
                int width = mMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                int heigth = mMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                long duration = mMediaFormat.getLong(MediaFormat.KEY_DURATION);
                videoExtractor.selectTrack(trackIndex);
                //创建解码视频的mediacodec
                videoCodec = MediaCodec.createDecoderByType(mimeType);
                if (videoCodec == null) {
                    Log.d(TAG, "mediaCodec is null,return");
                    return;
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
