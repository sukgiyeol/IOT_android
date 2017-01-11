package com.untoc.ks_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BackGround extends AppCompatActivity {
    private Button start, end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_ground);

        start = (Button)findViewById(R.id.start);
        end = (Button)findViewById(R.id.end);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Service 시작",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BackGround.this, MyService.class);
                startService(intent);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Service 끝",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BackGround.this,MyService.class);
                stopService(intent);
            }
        });
    }
}
