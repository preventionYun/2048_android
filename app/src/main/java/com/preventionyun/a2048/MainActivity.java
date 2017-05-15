package com.preventionyun.a2048;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.preventionyun.a2048.gameModel.GameModel;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainACtivity";
    private GameModel gameModel;

    private Button upArrowBtn, leftArrowBtn, rightArrowBtn, downArrowBtn;
    private Button newBtn, endBtn;
    private TextView enemyTextView1, enemyTextView2, enemyTextView3, enemyTextView4,
            enemyTextView5, enemyTextView6, enemyTextView7, enemyTextView8,
            enemyTextView9, enemyTextView10, enemyTextView11, enemyTextView12,
            enemyTextView13, enemyTextView14, enemyTextView15, enemyTextView16;
    private TextView myTextView1, myTextView2, myTextView3, myTextView4,
            myTextView5, myTextView6, myTextView7, myTextView8,
            myTextView9, myTextView10, myTextView11, myTextView12,
            myTextView13, myTextView14, myTextView15, myTextView16;

    private GameModel.GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Log.d(TAG, "게임 모델 생성");
            gameModel = new GameModel();
        }catch (Exception e){
            e.printStackTrace();
        }

        upArrowBtn = (Button)findViewById(R.id.upArrowBtn);
        leftArrowBtn = (Button)findViewById(R.id.leftArrowBtn);
        rightArrowBtn = (Button)findViewById(R.id.rightArrowBtn);
        downArrowBtn = (Button)findViewById(R.id.downArrowBtn);
        newBtn = (Button) findViewById(R.id.newBtn);
        endBtn = (Button) findViewById(R.id.endBtn);

        enemyTextView1 = (TextView)findViewById(R.id.enemyTextView1);
        enemyTextView2 = (TextView)findViewById(R.id.enemyTextView2);
        enemyTextView3 = (TextView)findViewById(R.id.enemyTextView3);
        enemyTextView4 = (TextView)findViewById(R.id.enemyTextView4);
        enemyTextView5 = (TextView)findViewById(R.id.enemyTextView5);
        enemyTextView6 = (TextView)findViewById(R.id.enemyTextView6);
        enemyTextView7 = (TextView)findViewById(R.id.enemyTextView7);
        enemyTextView8 = (TextView)findViewById(R.id.enemyTextView8);
        enemyTextView9 = (TextView)findViewById(R.id.enemyTextView9);
        enemyTextView10 = (TextView)findViewById(R.id.enemyTextView10);
        enemyTextView11 = (TextView)findViewById(R.id.enemyTextView11);
        enemyTextView12 = (TextView)findViewById(R.id.enemyTextView12);
        enemyTextView13 = (TextView)findViewById(R.id.enemyTextView13);
        enemyTextView14 = (TextView)findViewById(R.id.enemyTextView14);
        enemyTextView15 = (TextView)findViewById(R.id.enemyTextView15);
        enemyTextView16 = (TextView)findViewById(R.id.enemyTextView16);

        myTextView1 = (TextView)findViewById(R.id.myTextView1);
        myTextView2 = (TextView)findViewById(R.id.myTextView2);
        myTextView3 = (TextView)findViewById(R.id.myTextView3);
        myTextView4 = (TextView)findViewById(R.id.myTextView4);
        myTextView5 = (TextView)findViewById(R.id.myTextView5);
        myTextView6 = (TextView)findViewById(R.id.myTextView6);
        myTextView7 = (TextView)findViewById(R.id.myTextView7);
        myTextView8 = (TextView)findViewById(R.id.myTextView8);
        myTextView9 = (TextView)findViewById(R.id.myTextView9);
        myTextView10 = (TextView)findViewById(R.id.myTextView10);
        myTextView11 = (TextView)findViewById(R.id.myTextView11);
        myTextView12 = (TextView)findViewById(R.id.myTextView12);
        myTextView13 = (TextView)findViewById(R.id.myTextView13);
        myTextView14 = (TextView)findViewById(R.id.myTextView14);
        myTextView15 = (TextView)findViewById(R.id.myTextView15);
        myTextView16 = (TextView)findViewById(R.id.myTextView16);

        upArrowBtn.setOnClickListener(OnClickListener);
        leftArrowBtn.setOnClickListener(OnClickListener);
        rightArrowBtn.setOnClickListener(OnClickListener);
        downArrowBtn.setOnClickListener(OnClickListener);
        newBtn.setOnClickListener(OnClickListener);
        endBtn.setOnClickListener(OnClickListener);
    }

    private View.OnClickListener OnClickListener = new View.OnClickListener(){
        public void onClick(View v){
            char key;
            char key2;	// 랜덤 좌표를 위하여 key 인자를 하나 더 늘림.
            Random random = new Random();

            int id = v.getId();

            switch (id) {   // 눌린 버튼의 종류에 따라서 다르게 동작한다.
                case R.id.upArrowBtn : key = 'w'; break;
                case R.id.leftArrowBtn : key = 'a'; break;
                case R.id.rightArrowBtn : key = 'd'; break;
                case R.id.downArrowBtn : key = 's'; break;

                //case R.id.newBtn : key = 'n'; break;
                case R.id.newBtn :
                    // 프로그램이 처음 시작하면 랜덤으로 숫자를 생성
                    key = (char) ('0' + random.nextInt(4));
                    key2 = (char) ('0' + random.nextInt(4));
                    try {
                        gameModel.accept((char) ((key2 - '0') * gameModel.arrayLength + (key - '0')));    // accept로 넣는다.
                    }catch (Exception e){
                        Log.d(TAG, "newBtn 에러 발생");
                        e.printStackTrace();
                    }
                    break;
                case R.id.endBtn : key = 'e'; break;
                default: return;
            }
            Log.d(TAG, "눌린 버튼 : " + key);

            try {
                gameState = gameModel.accept(key);
                switch (gameState) {
                    case NewNumber:
                        while (true) {    // 무한루프를 돌면서 빈 공간을 찾는다. 이 게임에서는 10번의 제한 카운트가 있기 때문에 빈 곳이 없어서 무한루프에 빠지는 경우는 없음
                            key = (char) ('0' + random.nextInt(4));        // 랜덤으로 행과 열을 만들어 본다.
                            key2 = (char) ('0' + random.nextInt(4));
                            if (gameModel.screen.get_array()[key - '0'][key2 - '0'] == 0) {    // 매트릭스에서 랜덤으로 빈 곳을 발견.
                                //((key2 - '0') * gameModel.arrayLength + (key - '0')); // 2차원 행과 열 값을 1차원으로 변경...
                                // 예) 4x4 행렬은 1x16 행렬로 바꿀 수 있음.
                                // accept의 인자로 행과 열 2개를 보내려면 너무 복잡해지니까.. 변경함.
                                // if(debugMode) System.out.println("y : " + key + " , x : " + key2 + "는 비어있음.");
                                // if(debugMode) System.out.println((key2 - '0') * gameModel.arrayLength + (key - '0'));
                                gameModel.accept((char) ((key2 - '0') * gameModel.arrayLength + (key - '0')));    // accept로 넣는다.
                                break;    // 무한루프 종료
                            }
                        }
                        break;

                    case Finished:
                        System.out.println("Game Finished!");
                        System.out.println("Your total score : " + gameModel.totalScore);
                        return;
                }
            }catch (Exception e){
                Log.d(TAG, "Exception 발생");
                e.printStackTrace();
            }
            updateMyView();
        }
    };

    public void updateMyView(){
        myTextView1.setText("" + gameModel.screen.get_array()[0][0]);
        myTextView2.setText("" + gameModel.screen.get_array()[0][1]);
        myTextView3.setText("" + gameModel.screen.get_array()[0][2]);
        myTextView4.setText("" + gameModel.screen.get_array()[0][3]);
        myTextView5.setText("" + gameModel.screen.get_array()[1][0]);
        myTextView6.setText("" + gameModel.screen.get_array()[1][1]);
        myTextView7.setText("" + gameModel.screen.get_array()[1][2]);
        myTextView8.setText("" + gameModel.screen.get_array()[1][3]);
        myTextView9.setText("" + gameModel.screen.get_array()[2][0]);
        myTextView10.setText("" + gameModel.screen.get_array()[2][1]);
        myTextView11.setText("" + gameModel.screen.get_array()[2][2]);
        myTextView12.setText("" + gameModel.screen.get_array()[2][3]);
        myTextView13.setText("" + gameModel.screen.get_array()[3][0]);
        myTextView14.setText("" + gameModel.screen.get_array()[3][1]);
        myTextView15.setText("" + gameModel.screen.get_array()[3][2]);
        myTextView16.setText("" + gameModel.screen.get_array()[3][3]);
    }
}
