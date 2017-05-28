package com.preventionyun.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {
    final static String TAG = "MenuActivity";

    Button btnStart;
    Button btnSetting;
    Button btnRanking;
    Button btnQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnSetting = (Button)findViewById(R.id.btnSetting);
        btnRanking = (Button)findViewById(R.id.btnRanking);
        btnQuit = (Button)findViewById(R.id.btnQuit);

        btnStart.setOnClickListener(OnClickListener);
        btnSetting.setOnClickListener(OnClickListener);
        btnRanking.setOnClickListener(OnClickListener);
        btnQuit.setOnClickListener(OnClickListener);
    }

    private View.OnClickListener OnClickListener = new View.OnClickListener(){
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btnStart:
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);  // 게임 화면으로 이동
                    finish();               // 이 액티비티는 종료
                    break;

                case R.id.btnSetting:
                    Toast.makeText(getApplicationContext(), "추후 서비스 예정입니다.",Toast.LENGTH_SHORT).show();
                    break;

                case R.id.btnRanking:
                    Toast.makeText(getApplicationContext(), "추후 서비스 예정입니다.",Toast.LENGTH_SHORT).show();
                    break;

                case R.id.btnQuit:
                    finish();   // 액티비티 종료
                    break;

                default: return;
            }
        }
    };
}
