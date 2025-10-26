package com.rnr.hub;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Rogues & Ruins — Hub");
        tv.setTextSize(22f);
        tv.setPadding(48, 48, 48, 48);
        setContentView(tv);
    }
}
