package t20220049.sw_vision;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.SurfaceViewRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import t20220049.sw_vision.wtc_meeting.WebRTCManager;


public class CameraService extends Service implements SurfaceHolder.Callback {

    private static final String TAG = "CameraTest";
    private TextureView mTextureView; //预览框对象
    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private SurfaceHolder msurfaceHolder;
    private WebRTCManager manager = WebRTCManager.getInstance();

    private LocalBinder binder = new LocalBinder();

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        msurfaceHolder = surfaceHolder;
        requestCamera();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class LocalBinder extends Binder {
        // 声明一个方法，getService。（提供给客户端调用）
        public CameraService getService() {
            // 返回当前对象LocalService,这样我们就可在客户端端调用Service的公共方法了
            return CameraService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        tempVideo = new File(getBaseContext().getFilesDir() + "", "temp.mp4");
        intiView();
        String CHANNEL_ONE_ID = "t20220049.sw_vision";
        String CHANNEL_ONE_NAME = "Static Channel";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new Notification.Builder(this).setChannelId(CHANNEL_ONE_ID)
                .setContentTitle("摄像服务运行中")
                .setContentText("")
                .build();
        startForeground(10, notification);
        windowManager = (WindowManager) CameraService.this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopPreview();
        windowManager.removeView(surfaceView);
        super.onDestroy();
    }

    private void intiView() {
//        mTextureView = findViewById(R.id.textureView);
    }


    Camera mCamera; //可以用来对打开的摄像头进行关闭，释放
    private int mCameraId = 1;
    MediaRecorder mMediaRecorder = new MediaRecorder();
    boolean isRecord = false;

    public void switchCamera() {
        if (mCameraId == 1) {
            mCameraId = 0;
        } else {
            mCameraId = 1;
        }
        requestCamera();
    }

    public void requestCamera() {
        try {
            //0/1/2
            mCamera = Camera.open(mCameraId);//手机上可以用来切换前后摄像头，不同的设备要看底层支持情况
            Log.i(TAG, "handleRequestCamera mCameraId = " + mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            if (mCameraId == 0) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
            }
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(msurfaceHolder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        } catch (Exception error) {
            Log.e(TAG, "handleRequestCamera error = " + error.getMessage());
        }
    }

    File tempVideo;

    public void initMedia(SurfaceViewRenderer remote_view) {
        requestCamera();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置音频源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);//设置视频源
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//设置音频输出格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//设置视频编码格式
        mMediaRecorder.setVideoSize(1920, 1080);
        mMediaRecorder.setVideoEncodingBitRate(8 * 1920 * 1080);//设置视频的比特率
        mMediaRecorder.setVideoFrameRate(60);//设置视频的帧率
        if (mCameraId == 0) {
            mMediaRecorder.setOrientationHint(90);//设置视频的角度
        }else {
            mMediaRecorder.setOrientationHint(270);//设置视频的角度
        }
        mMediaRecorder.setMaxDuration(60 * 1000);//设置最大录制时间
        mMediaRecorder.setOutputFile(tempVideo);//设置文件保存路径
        mMediaRecorder.setPreviewDisplay(msurfaceHolder.getSurface());
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() { //录制异常监听
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                startPreview();
            }
        });
    }

    public void takePicture() {
        requestCamera();
        manager.stopCapture();
        mCamera.takePicture(null, null, (bytes, camera) -> {
            new Thread(() -> {
                long curTime = new Date().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String fileName = "record_photo " + sdf.format(curTime) + ".png";
                Matrix m = new Matrix();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);//重新在文件里获取图片
                if (mCameraId == 0) {
                    m.setRotate(90, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
                } else {
                    m.setRotate(270, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, fileName);
            }).start();
//            mCamera.startPreview();
        });
        manager.startCapture();
    }

    public void activateRecord(SurfaceViewRenderer remote_view) {
        if (isRecord) {
            Toast.makeText(getBaseContext(), "停止录制", Toast.LENGTH_SHORT).show();
            mMediaRecorder.stop();
            mMediaRecorder.reset();//重置,将MediaRecorder调整为空闲状态
            isRecord = false;
            requestCamera();
            insertVideo(tempVideo.getAbsolutePath(), getApplicationContext());
            manager.startCapture();
        } else {
            Toast.makeText(getBaseContext(), "开始录制", Toast.LENGTH_SHORT).show();
            mCamera.stopPreview();
//            manager.stopCapture();

            initMedia(remote_view);
            try {
                mMediaRecorder.prepare();//准备录制
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaRecorder.start();
            isRecord = true;
        }
    }

    boolean isPause = false;

    public void pauseRecord() {
        if (isRecord) {
            if (isPause) {
                mMediaRecorder.resume();
                isPause = false;
            } else {
                mMediaRecorder.pause();
                isPause = true;
            }
        }
    }

    private static final String VIDEO_BASE_URI = "content://media/external/video/media";

    private void insertVideo(String videoPath, Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        int nVideoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int nVideoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int duration = Integer
                .parseInt(retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        long dateTaken = System.currentTimeMillis();
        File file = new File(videoPath);
        String title = file.getName();
        String filename = file.getName();
        String mime = "video/mp4";
        ContentValues mCurrentVideoValues = new ContentValues(9);
        mCurrentVideoValues.put(MediaStore.Video.Media.TITLE, title);
        mCurrentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaStore.MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATA, videoPath);
        mCurrentVideoValues.put(MediaStore.Video.Media.WIDTH, nVideoWidth);
        mCurrentVideoValues.put(MediaStore.Video.Media.HEIGHT, nVideoHeight);
        mCurrentVideoValues.put(MediaStore.Video.Media.RESOLUTION, Integer.toString(nVideoWidth) + "x" + Integer.toString(nVideoHeight));
        mCurrentVideoValues.put(MediaStore.Video.Media.SIZE, new File(videoPath).length());
        mCurrentVideoValues.put(MediaStore.Video.Media.DURATION, duration);
        ContentResolver contentResolver = context.getContentResolver();
        Uri videoTable = Uri.parse(VIDEO_BASE_URI);
        Uri uri = contentResolver.insert(videoTable, mCurrentVideoValues);
        writeFile(videoPath, mCurrentVideoValues, contentResolver, uri);
    }

    private void writeFile(String imagePath, ContentValues values, ContentResolver contentResolver, Uri item) {
        try (OutputStream rw = contentResolver.openOutputStream(item, "rw")) {
            // Write data into the pending image.
            Sink sink = Okio.sink(rw);
            BufferedSource buffer = Okio.buffer(Okio.source(new File(imagePath)));
            buffer.readAll(sink);
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            contentResolver.update(item, values, null, null);
            new File(imagePath).delete();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Cursor query = getContentResolver().query(item, null, null, null);
                if (query != null) {
                    int count = query.getCount();
                    Log.e("writeFile", "writeFile result :" + count);
                    query.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        Log.i(TAG, "startPreview");
        requestCamera();
    }


    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


}