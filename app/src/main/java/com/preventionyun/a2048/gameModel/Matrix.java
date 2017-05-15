package com.preventionyun.a2048.gameModel;
public class Matrix {
	
	private final static int currentDebugLevel = 0;	// 현재 디버그 레벨.
	private final static int debugLevel1 = 1;	// 프로그램의 흐름에 대한 정보. 
	private final static int debugLevel2 = 2;	// 프로그램에서 어떠한 이벤트에 대한 정보.
	private final static int debugLevel3 = 3;	// 특정 이벤트가 발생한 상황에서 변수의 변화 등에 대한 정보.
	
	private static int nAlloc = 0;
	private static int nFree = 0;
	protected void finalize() throws Throwable {
	   super.finalize();
	   nFree++;
	}
	public int get_nAlloc() { return nAlloc; }
	public int get_nFree() { return nFree; }
	private int dy = 0;
	public int get_dy() { return dy; }
	private int dx = 0;
	public int get_dx() { return dx; }
	private int[][] array = null;
	public int[][] get_array() { return array; }
	private void alloc(int cy, int cx) throws MatrixException {
	   if((cy < 0) || (cx < 0))
		   throw new MatrixException("wrong matrix size");
	      
	   dy = cy;
	   dx = cx;
	   array = new int[dy][dx];
	   nAlloc++;
	}
	public Matrix() throws MatrixException { alloc(0, 0); }
	public Matrix (int cy, int cx) throws MatrixException {
	   alloc(cy, cx);
	   for(int y = 0; y < dy; y++)
	      for(int x = 0; x < dx; x++)
	         array[y][x] = 0;
	}
	public Matrix(Matrix obj) throws MatrixException {
	   alloc(obj.dy, obj.dx);
	   for(int y = 0; y < dy; y++)
	      for(int x = 0; x < dx; x++)
	         array[y][x] = obj.array[y][x];
	}
	public Matrix(int[][] arr) throws MatrixException {
	   alloc(arr.length, arr[0].length);
	   for(int y = 0; y < dy; y++)
	      for(int x = 0; x < dx; x++)
	         array[y][x] = arr[y][x];
	}
	
	// 내용물이 같으면 True, 다르면 False
	public boolean equals(Matrix anotherMatrix){
		for(int y = 0; y < dy; y++) {
		      for(int x = 0; x < dx; x++){
		    	  if(array[y][x] != anotherMatrix.get_array()[y][x]) return false;
		      }
		}
		return true;
	}
	
	public void print() {
	   //System.out.println("Matrix(" + dy + "," + dx + ")");
	   for(int y = 0; y < dy; y++) {
	      for(int x = 0; x < dx; x++)
	         System.out.print(array[y][x] + " ");
	      System.out.println();
	   }
	} 
}
	
	class MatrixException extends Exception {
		public MatrixException() { super("Matrix Exception"); }
		public MatrixException(String msg) { super(msg); }
	}
	class MismatchedMatrixException extends MatrixException {
		public MismatchedMatrixException() { super("Mismatched Matrix Exception"); }
		public MismatchedMatrixException(String msg) { super(msg); }
	}

