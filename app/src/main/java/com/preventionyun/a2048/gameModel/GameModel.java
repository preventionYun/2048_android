package com.preventionyun.a2048.gameModel;

import java.io.Serializable;

public class GameModel implements Serializable{
	public enum GameState{
		Start(0), Running(1), NewNumber(2), Finished(3), Error(4);
		private final int value;
		private GameState(int value){ this.value = value; }
		public int value() { return value; }
	}

	final static boolean debugMode = false;
	public final static int arrayLength = 4;	// 정방행렬 사이즈 어떻게 할 것인가?
	public int count = 20;	// 게임 목숨
	public int totalScore = 0;
	
	boolean gameActionsInitialized;
	public Matrix screen;
	
	public GameState gameState;
	
	// 처음 빈 화면을 만들기 위한 2차원 배열 
	final static int[][] blankArray = {
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 0, 0, 0}
	};
	public GameModel() throws Exception{	// 생성자
		gameActionsInitialized = false;		
		gameState = GameState.Start;
		screen = new Matrix(blankArray);
	}
	
	public GameState accept(char key) throws Exception {
		if(debugMode) System.out.println("키가 들어옴 : " + key);
		
		if(gameState == GameState.Start){
			if(debugMode) System.out.println("게임이 처음 시작됨");
			setGameActions();	// 리스너 등록
			gameState = GameState.NewNumber;	// 게임이 처음 시작되면 새로운 숫자를 랜덤하게 생성하기 위함.
			//return gameState;
		}
		
		if(gameState == GameState.NewNumber){	// 새로운 숫자를 생성하는 상태인 경우
			if(debugMode) System.out.println("새로운 숫자를 랜덤한 위치에 만들어냅니다.");
			makeNewNumber(key);
			screen.print();
			gameState = GameState.Running;
		}
		
		boolean newNumberNeeded = false;
		switch (key) {
	        case 'a':
	        	newNumberNeeded = moveLeft(key);
	            break;
	        case 'd':
	        	newNumberNeeded = moveRight(key);
	            break;
	        case 's':
	        	newNumberNeeded = moveDown(key);
	            break;
	        case 'w':
	        	newNumberNeeded = moveUp(key);
	            break;
		}
		
		if(newNumberNeeded){	// 단축키 조작 결과로 새로운 숫자가 필요한 상황인가 체크
			gameState = GameState.NewNumber;	// 상태변경
		}else{
			screen.print();		// 새로운 상태가 필요한 경우라면 지금 화면을 갱신할 필요는 없음.
		}
		
		if(count == 0) gameState = GameState.Finished;	// 카운트가 다 떨어졌다면 종료
		
		System.out.println("totalScore : " + totalScore);
		System.out.println("남은 횟수 : " + count);
		
		if(debugMode) System.out.println("상태 : " + gameState);
		
		return gameState;
	}
	
	private OnLeft onLeft = new OnLeft();
    private OnRight onRight = new OnRight();
    private OnDown onDown = new OnDown();
    private OnUp onUp = new OnUp();
    private OnNewNumber onNewNumber = new OnNewNumber();
    
	private GameAction moveLeft, moveRight, moveDown, moveUp, makeNewNumber;
    protected void setGameActions(){
    	moveLeft = new GameAction(onLeft);
    	moveRight = new GameAction(onRight);
    	moveDown = new GameAction(onDown);
    	moveUp = new GameAction(onUp);
    	makeNewNumber = new GameAction(onNewNumber);
    	gameActionsInitialized = true;
    }
    public void setOnLeftListener(OnLeft listener) { gameActionsInitialized = false; onLeft = listener; }
    public void setOnRightListener(OnRight listener) { gameActionsInitialized = false; onRight = listener; }
    public void setOnDownListener(OnDown listener) { gameActionsInitialized = false; onDown = listener; }
    public void setOnUpListener(OnUp listener) { gameActionsInitialized = false; onUp = listener; }
    public void setOnNewNumberListener(OnNewNumber listener) { gameActionsInitialized = false; onNewNumber = listener; }
    
    protected boolean moveLeft(char key) throws Exception { return moveLeft.run(this, key); }
	protected boolean moveRight(char key) throws Exception { return moveRight.run(this, key); }
	protected boolean moveDown(char key) throws Exception { return moveDown.run(this, key); }
	protected boolean moveUp(char key) throws Exception { return moveUp.run(this, key); }
	protected boolean makeNewNumber(char key) throws Exception { return makeNewNumber.run(this, key); }
	
	interface ActionHandler {
		public int run(GameModel gm, char key) throws Exception;
	}
	class OnLeft implements ActionHandler, Serializable {
		public int run(GameModel gm, char key) {
			int score = 0;
			for(int iterator = 0; iterator < arrayLength; iterator++){
				for(int col = 1; col < arrayLength; col++){
					for(int row = 0; row < arrayLength; row++){
						if(screen.get_array()[row][col - 1] == 0){	// 비어있으면
							screen.get_array()[row][col - 1] = screen.get_array()[row][col];	// 왼쪽 이동
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}else if(screen.get_array()[row][col - 1] == screen.get_array()[row][col]){ // 같은 숫자인 경우
							score = score + 2*screen.get_array()[row][col];	// 점수획득 
							screen.get_array()[row][col - 1] = screen.get_array()[row][col - 1] + screen.get_array()[row][col];	// 더함
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}
					}
				}
			}
			return score;
		}
	}
	class OnRight implements ActionHandler, Serializable {
		public int run(GameModel gm, char key) { 
			int score = 0;
			for(int iterator = 0; iterator < arrayLength; iterator++){
				for(int col = arrayLength - 2; col >= 0; col--){
					for(int row = 0; row < arrayLength; row++){
						if(screen.get_array()[row][col + 1] == 0){	// 비어있으면
							screen.get_array()[row][col + 1] = screen.get_array()[row][col];	// 우로 이동
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}else if(screen.get_array()[row][col + 1] == screen.get_array()[row][col]){ // 같은 숫자인 경우
							score = score + 2*screen.get_array()[row][col];	// 점수획득
							screen.get_array()[row][col + 1] = screen.get_array()[row][col + 1] + screen.get_array()[row][col];	// 더함
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}
					}
				}
			}
			return score;
		}
	}
	class OnDown implements ActionHandler, Serializable {
		public int run(GameModel gm, char key) {
			int score = 0;
			for(int iterator = 0; iterator < arrayLength; iterator++){
				for(int row = arrayLength - 2; row >= 0; row--){	
					for(int col = 0; col < arrayLength; col++){
						if(screen.get_array()[row + 1][col] == 0){	// 비어있으면
							screen.get_array()[row + 1][col] = screen.get_array()[row][col];	// 아래로 이동
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}else if(screen.get_array()[row + 1][col] == screen.get_array()[row][col]){ // 같은 숫자인 경우
							score = score + 2*screen.get_array()[row][col];	// 점수획득
							screen.get_array()[row + 1][col] = screen.get_array()[row + 1][col] + screen.get_array()[row][col];	// 더함
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}
					}
				}
			}
			return score;
		}
	}
	class OnUp implements ActionHandler, Serializable {
		public int run(GameModel gm, char key) {
			int score = 0;
			for(int iterator = 0; iterator < arrayLength; iterator++){	
				for(int row = 1; row < arrayLength; row++){
					for(int col = 0; col < arrayLength; col++){
						if(screen.get_array()[row - 1][col] == 0){	// 비어있으면
							screen.get_array()[row - 1][col] = screen.get_array()[row][col];	// 위로 이동
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}else if(screen.get_array()[row - 1][col] == screen.get_array()[row][col]){ // 같은 숫자인 경우
							score = score + 2*screen.get_array()[row][col];	// 점수획득
							screen.get_array()[row - 1][col] = screen.get_array()[row - 1][col] + screen.get_array()[row][col];	// 더함
							screen.get_array()[row][col] = 0;	// 이동한 자리는 비움
							continue;
						}
					}
				}
			}
			return score;
		}
	}
	class OnNewNumber implements ActionHandler, Serializable {
		public int run(GameModel gm, char key){
			int temp = (int)key;	// int형으로 변환을 시키고...
			// aceept의 인자로 2차원 행렬 -> 1차원 행렬로 바꿔서 인덱스를 보냈음. 반대 과정을 거쳐서 행과 열을 알아낸다.
			int row = temp / 4;		// 반대 과정을 거쳐서 행	
			int col = temp % 4;		// 반대 과정을 거쳐서 열
			if(debugMode) System.out.println("row : " + row + " col : " + col);
			gm.screen.get_array()[col][row] = 2;	// 2숫자를 생성
			return 0;
		}
	}
	
	class GameAction implements Serializable {
    	protected ActionHandler hDo;
    	public GameAction(ActionHandler d){
    		hDo = d;
    	}
    	
    	// 새 숫자가 필요한 상태라면 True, 필요 없다면 False를 리턴한다. 
    	public boolean run(GameModel gm, char key) throws Exception{
    		Matrix tempMatrix = new Matrix(screen); 
    		int score = hDo.run(gm, key);	// 단축키 조작 결과로 얻은 점수를 리턴함

    		if(debugMode){
	    		if(tempMatrix.equals(screen)) System.out.println("조작 후 내용물이 변하지 않음."); 
	    		else System.out.println("조작 후 내용물이 변함.");
    		}

    		totalScore = totalScore + score;	// 점수 갱신
    		if(gm.gameState != GameState.NewNumber && !tempMatrix.equals(screen)) count--;	// 횟수 차감, NewNumber 상태에서는 차감을 하지 않음 + 무언가 내용이 바뀌어야함.
    		return !tempMatrix.equals(screen);	// 내용물이 변하면 True, 변하지 않으면 False 리턴. -> 내용물이 변했으면 새로운 숫자를 랜덤한 위치에 뿌려줘야함.
    	}
    }
}
