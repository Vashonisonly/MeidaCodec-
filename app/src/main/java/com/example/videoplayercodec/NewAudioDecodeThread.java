package com.example.videoplayercodec;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NewAudioDecodeThread extends Thread {
    public static final String TAG = "AudioDecodeThread";
    private int audioInputBufferSize;
    private AudioTrack audioTrack;
    private AssetFileDescriptor mFileDescriptor;
    boolean isAudioFinish = false;
    MediaFormat mediaFormat;
    MediaCodec audioCodec;
    MediaExtractor audioExtractor;
    File file;
    FileOutputStream fos;


    public NewAudioDecodeThread(FileDescriptor fileDescriptor, long startoffset, long length){
        Log.d(TAG,"AudioThread Thread: "+currentThread());
        audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(fileDescriptor, startoffset,length);
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
//        try {
//            file = new File("pcmdata");
//            file.setReadable(true,false);
//            file.setWritable(true,false);
//            file.setExecutable(true,false);
//            fos = new FileOutputStream(file,true);
//        }catch (FileNotFoundException e){
//            Log.d(TAG,e.getMessage());
//        }
    }

    @Override
    public void run(){
        setTrackAndCodecInfo(audioExtractor);

        audioCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
                Log.d(TAG,"VideoThread currentThreadAudioInput: "+currentThread());
                ByteBuffer inputbuffer = audioCodec.getInputBuffer(i);
                //int size = inputbuffer.capacity();

                inputbuffer.clear();
                if (!isAudioFinish) {
                    isAudioFinish = Utils.putBufferToMediaCodec(audioExtractor, audioCodec, inputbuffer,i);
                }
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec mediaCodec, int i, MediaCodec.BufferInfo bufferInfo) {
                switch (i) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "format changed");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "audio time out");
                        break;
                    default:
                        Log.d(TAG,"i = "+ i + "time is: "+ System.currentTimeMillis());
                        ByteBuffer outPutBuffer = audioCodec.getOutputBuffer(i);
                        //ByteBuffer outPutBuffer = outputBuffers[outputBufferIndex];
//                        long startMs = System.currentTimeMillis();
//                        Utils.sleepRender(bufferInfo, startMs);

                        if (bufferInfo.size > 0) {
//                            if (mAudioOutTempBuf.length < bufferInfo.size) {
//                                mAudioOutTempBuf = new byte[bufferInfo.size];
//                            }
                            //outPutBuffer.position(0);
                            byte[] buffer = new byte[outPutBuffer.remaining()];
                            outPutBuffer.get(buffer);

//                            try {
//                                fos.write(outPutBuffer.array());
//                            }catch (IOException e){
//                                Log.d(TAG,e.getMessage());
//                            }

                            outPutBuffer.clear();
                            //outPutBuffer.get(mAudioOutTempBuf, 0, bufferInfo.size);
                            if (audioTrack != null) {
                                audioTrack.write(buffer, 0, buffer.length) ;
                                Log.d(TAG,"buffer "+ buffer[50]);
                            }else{
                                Log.e(TAG,"audioTrack is null!");
                            }
                        }
                        audioCodec.releaseOutputBuffer(i, false);
                        break;
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "buffer stream end");
                }
            }

            @Override
            public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {

            }
        });
        audioCodec.configure(mediaFormat, null, null, 0);
        audioCodec.start();
    }

    public void setTrackAndCodecInfo(MediaExtractor audioExtractor){

        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
            mediaFormat = audioExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                Log.d(TAG,"mime: "+mime);
                audioExtractor.selectTrack(i);
                //通道数
                int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                //采样率
                int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);

                //最小缓冲区大小
                int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate, (audioChannels == 1 ?
                        AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO), AudioFormat.ENCODING_PCM_16BIT);
//                audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
//                int frameSizeInBytes = audioChannels * 2;
//                audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, (audioChannels == 1 ?
                        AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO), AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
                Log.d(TAG, "audio start play");

                try {
                    audioCodec = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    Log.e("process error", e.getMessage());
                }

                break;
            }
        }

        if (audioCodec == null) {
            Log.d(TAG, "audio decoder null");
            return;
        }
    }
}
