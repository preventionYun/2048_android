package com.preventionyun.a2048;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingActivity extends AppCompatActivity {
    private EditText viewHostName, viewPortNum;
    private EditText viewMyNickName, viewPeerNickName;
    private String serverHostName, serverPortNum;
    private String myNickName, peerNickName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        viewHostName = (EditText) findViewById(R.id.editView1);
        viewPortNum = (EditText) findViewById(R.id.editView2);
        viewMyNickName = (EditText) findViewById(R.id.editView3);
        viewPeerNickName = (EditText) findViewById(R.id.editView4);
        Intent intent = getIntent();
        serverHostName = intent.getStringExtra("serverHostName");
        serverPortNum = intent.getStringExtra("serverPortNum");
        myNickName = intent.getStringExtra("myNickName");
        peerNickName = intent.getStringExtra("peerNickName");
        if (serverHostName == null) {
            serverHostName = "10.0.2.2";
            serverPortNum = "8080";
            myNickName = "me";
            peerNickName = "me";
        }
        viewHostName.setText(serverHostName);
        viewPortNum.setText(serverPortNum);
        viewMyNickName.setText(myNickName);
        viewPeerNickName.setText(peerNickName);
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.confirm:
                Intent intent = new Intent();
                intent.putExtra("serverHostName", viewHostName.getText().toString());
                intent.putExtra("serverPortNum", viewPortNum.getText().toString());
                intent.putExtra("myNickName", viewMyNickName.getText().toString());
                intent.putExtra("peerNickName", viewPeerNickName.getText().toString());
                setResult(RESULT_OK, intent);
                break;
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                break;
        }
        finish();
    }
}