package com.preventionyun.a2048.gameModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class GameMain {
	final static boolean debugMode = false;
	
	// 키 입력 관련. 테트리스의 코드를 재활용
	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static String line = null;
	private static int nKeys = 0;
	private static char getKey() throws IOException {
        char ch;
        if (nKeys != 0) {
            ch = line.charAt(line.length() - nKeys);
            nKeys--;
            return ch;
        }
        do {
            line = br.readLine();
            nKeys = line.length();
        } while (nKeys == 0);
        ch = line.charAt(0);
        nKeys--;
        return ch;
    }
		
	public static void main(String[] args) throws Exception{
		char key;	
		char key2;	// 랜덤 좌표를 위하여 key 인자를 하나 더 늘림.
		Random random = new Random();
		GameModel gameModel = new GameModel();
		
		// 프로그램이 처음 시작하면 랜덤으로 숫자를 생성
		key = (char) ('0' + random.nextInt(4));	
        key2 = (char) ('0' + random.nextInt(4));
        gameModel.accept((char)((key2 - '0') * gameModel.arrayLength + (key - '0')));	// accept로 넣는다.
		
		while((key = getKey()) != 'q'){
			gameModel.accept(key);
			switch(gameModel.gameState){
				case NewNumber :
					if(debugMode) System.out.println("새로운 숫자 필요!");
			        while(true){	// 무한루프를 돌면서 빈 공간을 찾는다. 이 게임에서는 10번의 제한 카운트가 있기 때문에 빈 곳이 없어서 무한루프에 빠지는 경우는 없음
			        	key = (char) ('0' + random.nextInt(4));		// 랜덤으로 행과 열을 만들어 본다.
				        key2 = (char) ('0' + random.nextInt(4));	
				        if(gameModel.screen.get_array()[key-'0'][key2-'0'] == 0){	// 매트릭스에서 랜덤으로 빈 곳을 발견.
				        	//((key2 - '0') * gameModel.arrayLength + (key - '0')); // 2차원 행과 열 값을 1차원으로 변경...
				        	// 예) 4x4 행렬은 1x16 행렬로 바꿀 수 있음.
				        	// accept의 인자로 행과 열 2개를 보내려면 너무 복잡해지니까.. 변경함.
				        	// if(debugMode) System.out.println("y : " + key + " , x : " + key2 + "는 비어있음.");
				        	// if(debugMode) System.out.println((key2 - '0') * gameModel.arrayLength + (key - '0'));
				        	gameModel.accept((char)((key2 - '0') * gameModel.arrayLength + (key - '0')));	// accept로 넣는다.
				        	break;	// 무한루프 종료
				        }
			        }
			        break;
				
				case Finished :
					System.out.println("Game Finished!");
					System.out.println("Your total score : " + gameModel.totalScore);
					return ;
			}
		}
	}
}
