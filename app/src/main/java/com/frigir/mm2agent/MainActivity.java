package com.frigir.mm2agent;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
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

        TextView status = new TextView(this);
        status.setText(
            "Версия 0.1.0\n\n" +
            "Проект запущен.\n" +
            "Следующий этап: захват экрана и компьютерное зрение."
        );
        status.setTextSize(17);
        status.setTextColor(Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 28, 0, 28);

        Button button = new Button(this);
        button.setText("Проверить приложение");
        button.setOnClickListener(v ->
            status.setText(
                "MM2 AI Agent работает!\n\n" +
                "Интерфейс готов. Захват экрана и анализ игры " +
                "будут подключены следующим этапом."
            )
        );

        layout.addView(title);
        layout.addView(status);
        layout.addView(button);

        setContentView(layout);
    }
}
