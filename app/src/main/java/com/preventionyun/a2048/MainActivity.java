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
    private boolean isGameStarted = false;

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
    private TextView myScore, myCount;
    private GameModel.GameState gameState;

    public enum GameState {
        Error(-1), Initial(0), Running(1), Paused(2);
        private final int value;
        private GameState(int value) { this.value = value; }
        public int value() { return value; }
        public static GameState fromInteger(int value) {
            switch (value) {
                case -1: return Error;
                case 0: return Initial;
                case 1: return Running;
                case 2: return Paused;
                default: return null;
            }
        }
    }

    public enum UserCommand {
        NOP(-1), Quit(0), Start(1), Pause(2), Resume(3), Update(4), Recover(5);
        private final int value;
        private UserCommand(int value) { this.value = value; }
        public int value() { return value; }
    }

    int stateMatrix[][] = { // stateMatrix[currState][userCmd] -> nextState
            // Quit(0), Start(1), Pause(2), Resume(3), Update(4), Recover(5)
            /*Initial(0)*/{ -1, 1, -1, -1, -1, 2},   // [Initial][Start] -> Running, [Initial][Recover] -> Paused
            /*Running(1)*/{ 0, -1, 2, -1, 1, -1},     // [Running][Quit] -> Initial, [Running][Pause] -> Paused, [Running][Update] -> Running
            /*Paused (2)*/{ 0, 1, -1, 1, 2, -1},      // [Paused][Quit] -> Initial, [Paused][Started,Resume] -> Running, [Paused][Update] -> Paused
    };

    boolean buttonState[][] = { // buttonState[currState][btnID] -> to be enabled/disabled
            /*Initial(0)*/{ true, false, false }, // [Initial][StartBtn] -> enabled
            /*Running(1)*/{ true, true, true },   // [Running][anyBtn] -> enabled
            /*Paused (2)*/{ true, true, false },  // [Paused][StartBtn,ResumeBtn] -> enable
    };

    // 버튼의 활성화를 설정하는 함수
    private void setButtonsState(){
        // buttonState[현재상태][버튼의 종류] = 버튼의 활성화 유무
        boolean startFlag = buttonState[gameState.value()][0];
        boolean pausedFlag = buttonState[gameState.value()][1];
        boolean otherFlag = buttonState[gameState.value()][2];  // 키 조작
        newBtn.setEnabled(startFlag);
        endBtn.setEnabled(pausedFlag);
        // 조작키
        upArrowBtn.setEnabled(otherFlag);
        leftArrowBtn.setEnabled(otherFlag);
        rightArrowBtn.setEnabled(otherFlag);
        downArrowBtn.setEnabled(otherFlag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        myCount = (TextView)findViewById(R.id.myCount);
        myScore = (TextView)findViewById(R.id.myScore);

        // 리스너 등록
        upArrowBtn.setOnClickListener(OnClickListener);
        leftArrowBtn.setOnClickListener(OnClickListener);
        rightArrowBtn.setOnClickListener(OnClickListener);
        downArrowBtn.setOnClickListener(OnClickListener);
        newBtn.setOnClickListener(OnClickListener);
        endBtn.setOnClickListener(OnClickListener);

        // 버튼의 활성화 관련 변경(New -> 활성화, End -> 비활성화)
        setButtonsState(isGameStarted);
    }

    private View.OnClickListener OnClickListener = new View.OnClickListener(){
        public void onClick(View v){
            char key;
            int id = v.getId();
            switch (id) {   // 눌린 버튼의 종류에 따라서 다르게 동작한다.
                case R.id.upArrowBtn : key = 'w'; break;
                case R.id.leftArrowBtn : key = 'a'; break;
                case R.id.rightArrowBtn : key = 'd'; break;
                case R.id.downArrowBtn : key = 's'; break;
                case R.id.newBtn :
                    try {
                        Log.d(TAG, "게임 모델 생성");
                        gameModel = new GameModel();    // 새 버튼을 누를 때마다 게임 모델을 재생성함. 이전의 모델은 버려버림.
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    key = 'n';
                    isGameStarted = true;
                    setButtonsState(isGameStarted);
                    try {
                        // 랜덤으로 2를 2개 생성한다.
                        gameModel.accept(getBlankLocation());
                        // ?? 게임 모델의 상태를 이쪽에서 바꿔버리면 MVC 패턴을 사용하는 것에서 벗어나지 않는가..?
                        // 게임 모델의 상태를 여기서 NewNumber로 바꾸지 않으면, Running 상태에선 빈공간의 char 값을 받아도 무시하게 됨.
                        // 불확정적인 것은 모델 밖으로 빼야한다면... 모델 전체를 변경해야함..??
                        gameModel.gameState = GameModel.GameState.NewNumber; // ?? 문제의 부분
                        gameModel.accept(getBlankLocation());
                    }catch (Exception e){
                        Log.d(TAG, "newBtn 에러 발생");
                        e.printStackTrace();
                    }
                    break;
                case R.id.endBtn :
                    key = 'e';
                    isGameStarted = false;
                    setButtonsState(isGameStarted);
                    break;
                default: return;
            }
            Log.d(TAG, "눌린 버튼 : " + key);
            try {
                gameState = gameModel.accept(key);  // 키에 맞게 동작을 시킨다.
                switch (gameState) {    // 동작의 결과인 gameState따라 추후 동작 결정.
                    case NewNumber: // 새 번호가 필요한 상태
                        gameModel.accept(getBlankLocation());   // 빈 자리에 2를 생성함.
                        break;

                    case Finished:  // 종료상태(카운트 다 사용함)
                        isGameStarted = false;
                        setButtonsState(isGameStarted);
                        System.out.println("Game Finished!");
                        System.out.println("Your total score : " + gameModel.totalScore);
                        //return;
                }
            }catch (Exception e){
                Log.d(TAG, "Exception 발생");
                e.printStackTrace();
            }
            updateMyView(); // 화면 갱신
        }
    };

    public void updateMyView(){
        if(gameModel.screen.get_array()[0][0] == 0) myTextView1.setText("");
        else myTextView1.setText("" + gameModel.screen.get_array()[0][0]);
        if(gameModel.screen.get_array()[0][1] == 0) myTextView2.setText("");
        else myTextView2.setText("" + gameModel.screen.get_array()[0][1]);
        if(gameModel.screen.get_array()[0][2] == 0) myTextView3.setText("");
        else myTextView3.setText("" + gameModel.screen.get_array()[0][2]);
        if(gameModel.screen.get_array()[0][3] == 0) myTextView4.setText("");
        else myTextView4.setText("" + gameModel.screen.get_array()[0][3]);
        if(gameModel.screen.get_array()[1][0] == 0) myTextView5.setText("");
        else myTextView5.setText("" + gameModel.screen.get_array()[1][0]);
        if(gameModel.screen.get_array()[1][1] == 0) myTextView6.setText("");
        else myTextView6.setText("" + gameModel.screen.get_array()[1][1]);
        if(gameModel.screen.get_array()[1][2] == 0) myTextView7.setText("");
        else myTextView7.setText("" + gameModel.screen.get_array()[1][2]);
        if(gameModel.screen.get_array()[1][3] == 0) myTextView8.setText("");
        else myTextView8.setText("" + gameModel.screen.get_array()[1][3]);
        if(gameModel.screen.get_array()[2][0] == 0) myTextView9.setText("");
        else myTextView9.setText("" + gameModel.screen.get_array()[2][0]);
        if(gameModel.screen.get_array()[2][1] == 0) myTextView10.setText("");
        else myTextView10.setText("" + gameModel.screen.get_array()[2][1]);
        if(gameModel.screen.get_array()[2][2] == 0) myTextView11.setText("");
        else myTextView11.setText("" + gameModel.screen.get_array()[2][2]);
        if(gameModel.screen.get_array()[2][3] == 0) myTextView12.setText("");
        else myTextView12.setText("" + gameModel.screen.get_array()[2][3]);
        if(gameModel.screen.get_array()[3][0] == 0) myTextView13.setText("");
        else myTextView13.setText("" + gameModel.screen.get_array()[3][0]);
        if(gameModel.screen.get_array()[3][1] == 0) myTextView14.setText("");
        else myTextView14.setText("" + gameModel.screen.get_array()[3][1]);
        if(gameModel.screen.get_array()[3][2] == 0) myTextView15.setText("");
        else myTextView15.setText("" + gameModel.screen.get_array()[3][2]);
        if(gameModel.screen.get_array()[3][3] == 0) myTextView16.setText("");
        else myTextView16.setText("" + gameModel.screen.get_array()[3][3]);

        myScore.setText("" + gameModel.totalScore);
        myCount.setText("" + gameModel.count);
    }

    // 게임이 진행중인지 flag를 인자로 넣음
    // 게임중이면 New 버튼 비활성화, End 버튼 활성화
    private void setButtonsState(boolean flag){
        newBtn.setEnabled(!flag);
        endBtn.setEnabled(flag);
        upArrowBtn.setEnabled(flag);
        downArrowBtn.setEnabled(flag);
        rightArrowBtn.setEnabled(flag);
        leftArrowBtn.setEnabled(flag);
    }

    private char getBlankLocation(){
        Random random = new Random();
        char x;
        char y;
        Log.d(TAG, "빈 자리 찾기 전 스크린 상태");
        gameModel.screen.print();
        while (true) {    // 무한루프를 돌면서 빈 공간을 찾는다. 이 게임에서는 제한 카운트가 있기 때문에 빈 곳이 없어서 무한루프에 빠지는 경우는 없음.
            x = (char) ('0' + random.nextInt(4));        // 랜덤으로 행과 열을 만들어 본다.
            y = (char) ('0' + random.nextInt(4));
            if (gameModel.screen.get_array()[x - '0'][y - '0'] == 0) {    // 매트릭스에서 랜덤으로 빈 곳을 발견.
                Log.d(TAG, "빈 위치 발견 - x축 : " + x + " y축 : " + y);
                return (char) ((y - '0') * gameModel.arrayLength + (x - '0'));
            }
        }
    }
}
