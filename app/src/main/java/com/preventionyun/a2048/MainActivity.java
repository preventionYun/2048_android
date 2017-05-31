package com.preventionyun.a2048;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.preventionyun.a2048.gameModel.GameModel;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainACtivity";
    // 서버 관련
    private String serverIP = "10.0.2.2";
    private int serverPortNum = 8080;
    private String myNickName = "me";
    private String peerNickName = "me";

    private EchoServer echoServer;
    private GameModel myGameModel, peerGameModel;
    private boolean battleMode = false;
    private String gameResult = "You Win!";


    private Button upArrowBtn, leftArrowBtn, rightArrowBtn, downArrowBtn;
    private Button newBtn, modeBtn, endBtn;
    private TextView enemyTextView1, enemyTextView2, enemyTextView3, enemyTextView4,
            enemyTextView5, enemyTextView6, enemyTextView7, enemyTextView8,
            enemyTextView9, enemyTextView10, enemyTextView11, enemyTextView12,
            enemyTextView13, enemyTextView14, enemyTextView15, enemyTextView16;
    private TextView myTextView1, myTextView2, myTextView3, myTextView4,
            myTextView5, myTextView6, myTextView7, myTextView8,
            myTextView9, myTextView10, myTextView11, myTextView12,
            myTextView13, myTextView14, myTextView15, myTextView16;
    private TextView myScore, myCount;
    //private android.os.Handler mHandler;

    private GameState myGameState = GameState.Initial;
    private GameState peerGameState = GameState.Initial;
    private GameState savedState;
    private char savedKey;

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
        NOP(-1), Quit(0), Start(1), Pause(2), Resume(3), Update(4), Recover(5), Win(6);
        private final int value;
        private UserCommand(int value) { this.value = value; }
        public int value() { return value; }
    }

    int stateMatrix[][] = { // stateMatrix[currState][userCmd] -> nextState
            // Quit(0), Start(1), Pause(2), Resume(3), Update(4), Recover(5), Win(6)
            /*Initial(0)*/{ -1, 1, -1, -1, -1, 2, 0},   // [Initial][Start] -> Running, [Initial][Recover] -> Paused
            /*Running(1)*/{ 0, -1, 2, -1, 1, -1, 0},     // [Running][Quit] -> Initial, [Running][Pause] -> Paused, [Running][Update] -> Running
            /*Paused (2)*/{ 0, 1, -1, 1, 2, -1, -1},      // [Paused][Quit] -> Initial, [Paused][Started,Resume] -> Running, [Paused][Update] -> Paused
    };

    boolean buttonState[][] = { // buttonState[currState][btnID] -> to be enabled/disabled
            /*Initial(0)*/{ true, false, false }, // [Initial][StartBtn] -> enabled
            /*Running(1)*/{ true, true, true },   // [Running][anyBtn] -> enabled
            /*Paused (2)*/{ true, true, false },  // [Paused][StartBtn,ResumeBtn] -> enable
    };

    private void initPeerGame(){
        peerGameState = GameState.Initial;
    }

    private Handler hMyViews = new Handler();
    private Handler hPeerViews = new Handler() {
        public void handleMessage(Message msg){
            if(echoServer.isAvailable() == false) return;
            char key = (char) msg.arg1;
            GameModel.GameState state = GameModel.GameState.Finished;
            if(key == 'Q'){
                gameResult = "You Win!";
                executeUserCommand(UserCommand.Win);
                return;
            }

            if(peerGameState == GameState.Initial) {         // 적의 게임 모델이 생성이 안되있는 상태라면
                try {
                    peerGameModel = new GameModel();        // 게임 모델 생성
                    peerGameModel.accept(key);
                    peerGameModel.gameState = GameModel.GameState.NewNumber;
                    peerGameState = GameState.Running;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Log.d(TAG, "peer가 받은 key : " + key);
                try {
                    state = peerGameModel.accept(key);
                }       // 키 accept
                catch (Exception e) {
                    e.printStackTrace();
                }
                updateEnemyView();                          // 화면 갱신
            }
            if(state == GameModel.GameState.Finished)
                executeUserCommand(UserCommand.Win);
            return ;
        };
    };

    // 버튼의 활성화를 설정하는 함수
    private void setButtonsState(){
        // buttonState[현재상태][버튼의 종류] = 버튼의 활성화 유무
        boolean startFlag = buttonState[myGameState.value()][0];
        boolean pausedFlag = buttonState[myGameState.value()][1];
        boolean otherFlag = buttonState[myGameState.value()][2];  // 키 조작
        newBtn.setEnabled(startFlag);
        endBtn.setEnabled(pausedFlag);
        modeBtn.setEnabled(startFlag && !pausedFlag);
        // 조작키
        upArrowBtn.setEnabled(otherFlag);
        leftArrowBtn.setEnabled(otherFlag);
        rightArrowBtn.setEnabled(otherFlag);
        downArrowBtn.setEnabled(otherFlag);
    }

    private void executeUserCommand(UserCommand cmd) {
        GameState prevState = myGameState;
        myGameState = GameState.fromInteger(stateMatrix[myGameState.value()][cmd.value()]); // stateMatrix를 통해 커맨드 실행 후 다음 상태를 갖고 옴
        // !! gameState Error!!
        if(myGameState == GameState.Error){
            Log.d(TAG, "game state error! (state.cmd)=(" + prevState.value() + "," + cmd.value() + ")");
            myGameState = prevState;
            return;
        }
        switch (cmd.value()){
            // NOP(-1), Quit(0), Start(1), Pause(2), Resume(3), Update(4), Recover(5)
            case 0: hMyViews.post(runnableQuit); break;
            case 1: hMyViews.post(runnableStart); break;
            case 2: hMyViews.post(runnablePause); break;
            case 3: hMyViews.post(runnableResume); break;
            case 4: hMyViews.post(runnableUpdate); break;
            case 5: hMyViews.post(runnableRecover); break;
            case 6: hMyViews.post(runnableWin); break;
            default: Log.d(TAG, "unknown user command!"); break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = getWindowManager();
        if (wm == null) return;
        int rotation = wm.getDefaultDisplay().getRotation();
        // 디바이스의 회전에 따라 다르게 뷰를 불러온다.
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            Log.d(TAG, "onCreate: portrait mode");
            setContentView(R.layout.activity_main); // 일반모드
        }else {
            Log.d(TAG, "onCreate: landscape mode");
            setContentView(R.layout.activity_main_landscape);   // 가로모드
        }

        upArrowBtn = (Button)findViewById(R.id.upArrowBtn);
        leftArrowBtn = (Button)findViewById(R.id.leftArrowBtn);
        rightArrowBtn = (Button)findViewById(R.id.rightArrowBtn);
        downArrowBtn = (Button)findViewById(R.id.downArrowBtn);
        newBtn = (Button) findViewById(R.id.newBtn);
        modeBtn = (Button) findViewById(R.id.modeBtn);
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
        modeBtn.setOnClickListener(OnClickListener);

        setButtonsState();
        echoServer = new EchoServer(hPeerViews, MainActivity.this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(battleMode && echoServer.isAvailable()){
            echoServer.send('Q');
            echoServer.disconnect();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // onPause가 불리는 순간
        savedState = myGameState;                     // 게임의 상태변수를 저장함.
        if (myGameState == GameState.Running)         // Running 상태였다면,
            executeUserCommand(UserCommand.Pause);  // Pause 커맨드
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // onResume
        if (savedState == GameState.Running)        // 저장된 상태가 Running이었다면,
            executeUserCommand(UserCommand.Resume); // Resume 커맨드 (홈 화면에 갔다오거나, 전화 등이 와서 멈추었다가 다시 돌아온 상태)
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSave");
        outState.putSerializable("myGameModel", myGameModel); // 게임 모델
        outState.putInt("savedState", savedState.value());  // 게임의 상태
        if(battleMode){
            outState.putBoolean("battleMode", battleMode);
            outState.putSerializable("peerGameModel", peerGameModel);
            outState.putInt("peerGameState", peerGameState.value());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        Log.d(TAG, "onRestore");
        // 복구
        savedState = GameState.fromInteger(inState.getInt("savedState"));
        if (savedState != GameState.Initial) {  // Initial 단계에는 저장된 모델이 없음.
            myGameModel = (GameModel) inState.getSerializable("myGameModel");
            executeUserCommand(UserCommand.Recover);    // 복구 커맨드
            if (battleMode){
                peerGameModel = (GameModel) inState.getSerializable("peerGameModel");
                peerGameState = GameState.fromInteger(inState.getInt("peerGameState"));
            }
        }
    }

    // 어떻게 동작할 것인가, 정의
    private View.OnClickListener OnClickListener = new View.OnClickListener(){
        public void onClick(View v) {
            int id = v.getId();
            UserCommand cmd = UserCommand.NOP;
            switch (id) {   // 눌린 버튼의 종류에 따라서 다르게 동작한다.
                // 눌린 버튼
                // 게임의 상태
                // 위 두 가지를 고려해서 동작을 결정함.
                case R.id.newBtn:
                    savedKey = 'N';
                    if (myGameState == GameState.Initial) cmd = UserCommand.Start;
                    else if (myGameState == GameState.Running) cmd = UserCommand.Pause;    // Running 면 'N'은 'P'모양을 함.
                    else if (myGameState == GameState.Paused) cmd = UserCommand.Resume;      // Paused 면 'N'은 'R'모양을 함.
                    break;
                case R.id.endBtn:
                    savedKey = 'Q';
                    if (myGameState == GameState.Running) cmd = UserCommand.Quit;
                    else if (myGameState == GameState.Paused) cmd = UserCommand.Quit;
                    break;
                case R.id.upArrowBtn: savedKey = 'w'; cmd = UserCommand.Update; break;
                case R.id.leftArrowBtn: savedKey = 'a'; cmd = UserCommand.Update; break;
                case R.id.rightArrowBtn: savedKey = 'd'; cmd = UserCommand.Update; break;
                case R.id.downArrowBtn: savedKey = 's'; cmd = UserCommand.Update; break;
                case R.id.modeBtn:
                    battleMode = !battleMode;
                    if (battleMode) modeBtn.setText("2");
                    else modeBtn.setText("1");
                default: return;
            }
            // 위에서 눌린 버튼 종류, 게임의 상태를 고려하여 유저가 실행할 커맨드를 결정했음.
            executeUserCommand(cmd);    // 커맨드를 실행
        }
    };
    /*
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
    */

    public void updateMyView(){
        if(myGameModel.screen.get_array()[0][0] == 0) myTextView1.setText("");
        else myTextView1.setText("" + myGameModel.screen.get_array()[0][0]);
        if(myGameModel.screen.get_array()[0][1] == 0) myTextView2.setText("");
        else myTextView2.setText("" + myGameModel.screen.get_array()[0][1]);
        if(myGameModel.screen.get_array()[0][2] == 0) myTextView3.setText("");
        else myTextView3.setText("" + myGameModel.screen.get_array()[0][2]);
        if(myGameModel.screen.get_array()[0][3] == 0) myTextView4.setText("");
        else myTextView4.setText("" + myGameModel.screen.get_array()[0][3]);
        if(myGameModel.screen.get_array()[1][0] == 0) myTextView5.setText("");
        else myTextView5.setText("" + myGameModel.screen.get_array()[1][0]);
        if(myGameModel.screen.get_array()[1][1] == 0) myTextView6.setText("");
        else myTextView6.setText("" + myGameModel.screen.get_array()[1][1]);
        if(myGameModel.screen.get_array()[1][2] == 0) myTextView7.setText("");
        else myTextView7.setText("" + myGameModel.screen.get_array()[1][2]);
        if(myGameModel.screen.get_array()[1][3] == 0) myTextView8.setText("");
        else myTextView8.setText("" + myGameModel.screen.get_array()[1][3]);
        if(myGameModel.screen.get_array()[2][0] == 0) myTextView9.setText("");
        else myTextView9.setText("" + myGameModel.screen.get_array()[2][0]);
        if(myGameModel.screen.get_array()[2][1] == 0) myTextView10.setText("");
        else myTextView10.setText("" + myGameModel.screen.get_array()[2][1]);
        if(myGameModel.screen.get_array()[2][2] == 0) myTextView11.setText("");
        else myTextView11.setText("" + myGameModel.screen.get_array()[2][2]);
        if(myGameModel.screen.get_array()[2][3] == 0) myTextView12.setText("");
        else myTextView12.setText("" + myGameModel.screen.get_array()[2][3]);
        if(myGameModel.screen.get_array()[3][0] == 0) myTextView13.setText("");
        else myTextView13.setText("" + myGameModel.screen.get_array()[3][0]);
        if(myGameModel.screen.get_array()[3][1] == 0) myTextView14.setText("");
        else myTextView14.setText("" + myGameModel.screen.get_array()[3][1]);
        if(myGameModel.screen.get_array()[3][2] == 0) myTextView15.setText("");
        else myTextView15.setText("" + myGameModel.screen.get_array()[3][2]);
        if(myGameModel.screen.get_array()[3][3] == 0) myTextView16.setText("");
        else myTextView16.setText("" + myGameModel.screen.get_array()[3][3]);

        myScore.setText("" + myGameModel.totalScore);
        myCount.setText("" + myGameModel.count);
    }

    public void updateEnemyView(){
        if(peerGameModel.screen.get_array()[0][0] == 0) enemyTextView1.setText("");
        else enemyTextView1.setText("" + peerGameModel.screen.get_array()[0][0]);
        if(peerGameModel.screen.get_array()[0][1] == 0) enemyTextView2.setText("");
        else enemyTextView2.setText("" + peerGameModel.screen.get_array()[0][1]);
        if(peerGameModel.screen.get_array()[0][2] == 0) enemyTextView3.setText("");
        else enemyTextView3.setText("" + peerGameModel.screen.get_array()[0][2]);
        if(peerGameModel.screen.get_array()[0][3] == 0) enemyTextView4.setText("");
        else enemyTextView4.setText("" + peerGameModel.screen.get_array()[0][3]);
        if(peerGameModel.screen.get_array()[1][0] == 0) enemyTextView5.setText("");
        else enemyTextView5.setText("" + peerGameModel.screen.get_array()[1][0]);
        if(peerGameModel.screen.get_array()[1][1] == 0) enemyTextView6.setText("");
        else enemyTextView6.setText("" + peerGameModel.screen.get_array()[1][1]);
        if(peerGameModel.screen.get_array()[1][2] == 0) enemyTextView7.setText("");
        else enemyTextView7.setText("" + peerGameModel.screen.get_array()[1][2]);
        if(peerGameModel.screen.get_array()[1][3] == 0) enemyTextView8.setText("");
        else enemyTextView8.setText("" + peerGameModel.screen.get_array()[1][3]);
        if(peerGameModel.screen.get_array()[2][0] == 0) enemyTextView9.setText("");
        else enemyTextView9.setText("" + peerGameModel.screen.get_array()[2][0]);
        if(peerGameModel.screen.get_array()[2][1] == 0) enemyTextView10.setText("");
        else enemyTextView10.setText("" + peerGameModel.screen.get_array()[2][1]);
        if(peerGameModel.screen.get_array()[2][2] == 0) enemyTextView11.setText("");
        else enemyTextView11.setText("" + peerGameModel.screen.get_array()[2][2]);
        if(peerGameModel.screen.get_array()[2][3] == 0) enemyTextView12.setText("");
        else enemyTextView12.setText("" + peerGameModel.screen.get_array()[2][3]);
        if(peerGameModel.screen.get_array()[3][0] == 0) enemyTextView13.setText("");
        else enemyTextView13.setText("" + peerGameModel.screen.get_array()[3][0]);
        if(peerGameModel.screen.get_array()[3][1] == 0) enemyTextView14.setText("");
        else enemyTextView14.setText("" + peerGameModel.screen.get_array()[3][1]);
        if(peerGameModel.screen.get_array()[3][2] == 0) enemyTextView15.setText("");
        else enemyTextView15.setText("" + peerGameModel.screen.get_array()[3][2]);
        if(peerGameModel.screen.get_array()[3][3] == 0) enemyTextView16.setText("");
        else enemyTextView16.setText("" + peerGameModel.screen.get_array()[3][3]);

        myScore.setText("" + peerGameModel.totalScore);
        myCount.setText("" + peerGameModel.count);
    }

    private char getBlankLocation(){
        Random random = new Random();
        char x;
        char y;
        Log.d(TAG, "빈 자리 찾기 전 스크린 상태");
        myGameModel.screen.print();
        while (true) {    // 무한루프를 돌면서 빈 공간을 찾는다. 이 게임에서는 제한 카운트가 있기 때문에 빈 곳이 없어서 무한루프에 빠지는 경우는 없음.
            x = (char) ('0' + random.nextInt(4));        // 랜덤으로 행과 열을 만들어 본다.
            y = (char) ('0' + random.nextInt(4));
            if (myGameModel.screen.get_array()[x - '0'][y - '0'] == 0) {    // 매트릭스에서 랜덤으로 빈 곳을 발견.
                Log.d(TAG, "빈 위치 발견 - x축 : " + x + " y축 : " + y);
                return (char) ('A' + (y - '0') * myGameModel.arrayLength + (x - '0'));  // !!
            }
        }
    }

    private Runnable runnableWin = new Runnable() {
        public void run() {
            Log.d(TAG, "runnableWin");
            setButtonsState();
            if(battleMode && echoServer.isAvailable()){
                echoServer.disconnect();
                initPeerGame();
            }
        }
    };

    private Runnable runnableQuit = new Runnable() {
        public void run() {
            Log.d(TAG, "runnableQuit");
            setButtonsState();
            newBtn.setText("NEW");
            //Toast.makeText(MainActivity.this, "Game Over!", Toast.LENGTH_SHORT).show();
            if(battleMode && echoServer.isAvailable()){
                echoServer.send('Q');
                echoServer.disconnect();
                initPeerGame();
            }
        }
    };
    private Runnable runnableStart = new Runnable() {
        @Override
        public void run() {
            try{
                char randomLocation1;
                char randomLocation2;

                myGameModel = new GameModel();    // 새 버튼을 누를 때마다 게임 모델을 재생성함. 이전의 모델은 버려버림.
                // 랜덤으로 2를 2개 생성한다.
                myGameModel.accept(randomLocation1 = getBlankLocation());
                // ?? 게임 모델의 상태를 이쪽에서 바꿔버리면 MVC 패턴을 사용하는 것에서 벗어나지 않는가..?
                // 게임 모델의 상태를 여기서 NewNumber로 바꾸지 않으면, Running 상태에선 빈공간의 char 값을 받아도 무시하게 됨.
                // 불확정적인 것은 모델 밖으로 빼야한다면... 모델 전체를 변경해야함..??
                myGameModel.gameState = GameModel.GameState.NewNumber; // ?? 문제의 부분
                myGameModel.accept(randomLocation2 = getBlankLocation());
                updateMyView();

                if(battleMode){ // !!
                    if(echoServer.connect(serverIP, serverPortNum, myNickName, peerNickName) == false ||    // 서버 접속
                            echoServer.send(randomLocation1) == false ||                                    // 랜덤위치 전송
                            echoServer.send(randomLocation2) == false) {                                    // 2번째 랜덤위치
                        Log.d(TAG, "runnableStart Error..");
                        gameResult = "Connection Error";
                        executeUserCommand(UserCommand.Quit);
                        return;
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            setButtonsState();
            newBtn.setText("P");
            //Toast.makeText(MainActivity.this, "Game Stated!", Toast.LENGTH_SHORT).show();
        }
    };
    private Runnable runnablePause = new Runnable() {
        @Override
        public void run() {
            setButtonsState();
            newBtn.setText("R");
            //Toast.makeText(MainActivity.this, "Game Paused!", Toast.LENGTH_SHORT).show();
        }
    };
    private Runnable runnableResume = new Runnable() {
        @Override
        public void run() {
            setButtonsState();
            newBtn.setText("P");
            //Toast.makeText(MainActivity.this, "Game Resumed!", Toast.LENGTH_SHORT).show();
        }
    };
    private Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            try{
                GameModel.GameState actionResult = myGameModel.accept(savedKey);  // 키에 맞게 동작을 시킨다.
                if(battleMode){
                    if(!echoServer.send(savedKey)){
                        gameResult = "Connection Error!";
                        executeUserCommand(UserCommand.Quit);
                        return;
                    }
                }
                switch (actionResult) {    // 동작의 결과인 gameState따라 추후 동작 결정.
                    case NewNumber: // 새 번호가 필요한 상태(내용물이 변함.)
                        char tempLocation = getBlankLocation();
                        myGameModel.accept(tempLocation);   // 빈 자리에 2를 생성함.
                        if(battleMode){
                            if(!echoServer.send(tempLocation)){
                                gameResult = "Connection Error!";
                                executeUserCommand(UserCommand.Quit);
                                return;
                            }
                        }
                        break;
                    case Finished:  // 종료상태(카운트 다 사용하면, Finished 상태를 리턴함.)
                        UserCommand cmd = UserCommand.Quit;
                        executeUserCommand(cmd);                // 종료 커맨드를 실행
                        System.out.println("Game Finished!");
                        System.out.println("Your total score : " + myGameModel.totalScore);
                        //return;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            updateMyView();
        }
    };

    private Runnable runnableRecover = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "recover");
            updateMyView();
            if(battleMode){
                updateEnemyView();
                modeBtn.setText("2");
            }
            setButtonsState();
            newBtn.setText("P");
            //Toast.makeText(MainActivity.this, "Game Recovered!", Toast.LENGTH_SHORT).show();
            if (savedState == GameState.Running){   // 복구 전 상태가 진행중이었다면
                hMyViews.post(runnableResume);      // 게임 resume
                newBtn.setText("P");                // 버튼 갱신
            }
            else if (savedState == GameState.Paused)    // 멈춤 상태였다면
                newBtn.setText("R");                    // 버튼만 갱신
        }
    };
}
