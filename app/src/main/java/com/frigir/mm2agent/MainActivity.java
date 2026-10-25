package com.frigir.mm2agent;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST = 1001;
    private TextView status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.rgb(17, 24, 39));

        TextView title = new TextView(this);
        title.setText("MM2 AI AGENT");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 28, 0, 28);
        status.setText("Версия 0.2.0\n\nЗахват экрана ещё не запускался.");

        Button capture = new Button(this);
        capture.setText("Захватить кадр экрана");
        capture.setOnClickListener(v -> {
            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(
                manager.createScreenCaptureIntent(),
                CAPTURE_REQUEST
            );
        });

        layout.addView(title);
        layout.addView(status);
        layout.addView(capture);
        setContentView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) {
            String result = getSharedPreferences("agent", MODE_PRIVATE)
                .getString("last_result", null);
            if (result != null) status.setText(result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CAPTURE_REQUEST) return;

        if (resultCode != RESULT_OK || data == null) {
            status.setText("Захват отменён. Нажми кнопку, чтобы попробовать снова.");
            return;
        }

        Intent service = new Intent(this, ScreenCaptureService.class);
        service.putExtra("resultCode", resultCode);
        service.putExtra("projectionData", data);

        try {
            startForegroundService(service);
            status.setText("Запрашиваем первый кадр…");
        } catch (Exception e) {
            status.setText("Не удалось запустить захват: " + e.getMessage());
        }
    }
}
