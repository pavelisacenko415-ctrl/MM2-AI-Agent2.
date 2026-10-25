package com.frigir.mm2agent;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {
    private static final String CHANNEL = "mm2_capture";
    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private HandlerThread thread;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(
            CHANNEL, "MM2 screen capture",
            NotificationManager.IMPORTANCE_LOW
        ));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this, CHANNEL)
            .setContentTitle("MM2 AI Agent")
            .setContentText("Получение одного кадра экрана")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(7, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(7, notification);
        }

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data;
        if (Build.VERSION.SDK_INT >= 33) {
            data = intent.getParcelableExtra("projectionData", Intent.class);
        } else {
            data = intent.getParcelableExtra("projectionData");
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            saveStatus("Не получено разрешение на захват экрана.");
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            projection = manager.getMediaProjection(resultCode, data);

            projection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    cleanup();
                    stopSelf();
                }
            }, new Handler(Looper.getMainLooper()));

            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            android.view.WindowManager wm =
                (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getRealMetrics(metrics);

            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int density = metrics.densityDpi;

            reader = ImageReader.newInstance(width, height,
                PixelFormat.RGBA_8888, 2);

            thread = new HandlerThread("MM2Capture");
            thread.start();

            reader.setOnImageAvailableListener(r -> {
                Image image = null;
                try {
                    image = r.acquireLatestImage();
                    if (image == null) return;

                    Image.Plane plane = image.getPlanes()[0];
                    ByteBuffer buffer = plane.getBuffer();
                    int pixelStride = plane.getPixelStride();
                    int rowStride = plane.getRowStride();
                    int rowPadding = rowStride - pix

Павел:
elStride * width;

                    Bitmap padded = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height, Bitmap.Config.ARGB_8888);
                    padded.copyPixelsFromBuffer(buffer);

                    Bitmap bitmap = Bitmap.createBitmap(padded, 0, 0, width, height);
                    padded.recycle();

                    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (dir == null) throw new Exception("Папка для кадра недоступна");
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new Exception("Не удалось создать папку");
                    }

                    File file = new File(dir, "mm2_frame.jpg");
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }

                    bitmap.recycle();
                    saveStatus("Кадр успешно сохранён!\n\n" +
                        width + " × " + height +
                        "\n\nФайл: " + file.getAbsolutePath() +
                        "\n\nСледующий этап: анализ изображения.");
                    cleanup();
                    stopSelf();
                } catch (Exception e) {
                    saveStatus("Ошибка кадра: " + e.getMessage());
                    cleanup();
                    stopSelf();
                } finally {
                    if (image != null) image.close();
                }
            }, new Handler(thread.getLooper()));

            display = projection.createVirtualDisplay(
                "MM2AgentCapture", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.getSurface(), null, null
            );
        } catch (Exception e) {
            saveStatus("Ошибка захвата: " + e.getMessage());
            cleanup();
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void saveStatus(String value) {
        getSharedPreferences("agent", MODE_PRIVATE)
            .edit().putString("last_result", value).apply();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(7, new Notification.Builder(this, CHANNEL)
            .setContentTitle("MM2 AI Agent")
            .setContentText(value.replace('\n', ' '))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build());
    }

    private void cleanup() {
        if (display != null) {
            display.release();
            display = null;
        }
        if (reader != null) {
            reader.close();
            reader = null;
        }
        if (projection != null) {
            projection.stop();
            projection = null;
        }
        if (thread != null) {
            thread.quitSafely();
            thread = null;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Override
    public void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    @Override
    public android.os.IBinder onBind(Intent intent) {
        return null;
    }
}
