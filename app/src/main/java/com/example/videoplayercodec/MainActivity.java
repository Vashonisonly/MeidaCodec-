package com.example.videoplayercodec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    VideoViewPlayer videoViewPlayer;
    AudioPlayer audioPlayer;
    AssetFileDescriptor fileDescriptor;
    SurfaceView surfaceView;

    final public class MediaCodecUtils {
        private static final String TAG = "VideoPlayerCodec";
//        private boolean isSemiPalnarYUV(int colorFormat) {
//            switch (colorFormat) {
//                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
//                    return false;
//                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
//                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
//                case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
//                    return true;
//                default:
//                    throw new RuntimeException("unkonw format " + colorFormat);
//            }
//        }
    }

    public void getFileDescriptor(){
        try {
            AssetManager assetManager = getAssets();
            fileDescriptor = assetManager.openFd("video.mp4");
        }catch (IOException e){
            Log.d(TAG, e.getMessage());
        }
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFileDescriptor();

        //videoViewPlayer = new VideoViewPlayer(R.id.surfaceView);
        //videoViewPlayer = new VideoViewPlayer(this);
        surfaceView = findViewById(R.id.surfaceView);
        videoViewPlayer = new VideoViewPlayer(fileDescriptor,surfaceView);
        videoViewPlayer.start();
        audioPlayer = new AudioPlayer(fileDescriptor);
        audioPlayer.start();

        Log.d("MediaCodec","begin1!");
    }
}
