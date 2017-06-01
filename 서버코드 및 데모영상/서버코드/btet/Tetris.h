#include <cmath>
#include "Matrix.h"

#define MAX_BLOCK_SIZE 100
enum TetrisState { Running = 0, NewBlock = 1, Finished = 2 };

class Tetris;

class ActionHandler { 
public: virtual void run(Tetris *t, char key) = 0; 
};

class Tetris {
private:
  TetrisState state;
  int *createArrayScreen(int dy, int dx, int dw);
  void printMatrix(Matrix blk);
  ActionHandler *p_onLeft;
  ActionHandler *p_onRight;
  ActionHandler *p_onDown;
  ActionHandler *p_onUp;
  ActionHandler *p_onCW;
  ActionHandler *p_onCCW;
  ActionHandler *p_onNewBlock;
  ActionHandler *p_onFinished;

protected:
  class TetrisAction {
  private: ActionHandler *hDo, *hUndo;
  public:
    TetrisAction(ActionHandler *d, ActionHandler *u) { hDo = d; hUndo = u; }
    bool run(Tetris *t, char key, bool update) {
      bool anyConflict = false;
      hDo->run(t, key);
      Matrix tempBlk;
      tempBlk = t->p_iScreen->clip(t->top, t->left,
				t->top + t->p_currBlk->get_dy(),
				t->left + t->p_currBlk->get_dx());
      tempBlk = tempBlk + *t->p_currBlk;	
      if ((anyConflict = tempBlk.anyGreaterThan(1)) == true) {
	hUndo->run(t, key);
	tempBlk = t->p_iScreen->clip(t->top, t->left,
				  t->top + t->p_currBlk->get_dy(),
				  t->left + t->p_currBlk->get_dx());
	tempBlk = tempBlk + *t->p_currBlk;	
      }
      if (update == true) {
	t->p_oScreen->paste(*t->p_iScreen, 0, 0);
	t->p_oScreen->paste(tempBlk, t->top, t->left);				
      }				
      return anyConflict;
    }
  };

  void createArrayOfBlockObjects(int* arrayOfarray1D[], int ntypes, int ndegrees);
  TetrisAction *p_moveLeft;
  TetrisAction *p_moveRight;
  TetrisAction *p_moveDown;
  TetrisAction *p_rotateCW;
  TetrisAction *p_insertBlk;
  void setTetrisActions();
  bool tetrisActionsInitialized;
  bool moveLeft(char key, bool update);
  bool moveRight(char key, bool update);
  bool moveDown(char key, bool update);
  bool rotateCW(char key, bool update);
  bool insertBlk(char key, bool update);

public:
  static int nBlockDegrees;	// number of block degrees (typically 4)
  static int nBlockTypes;	// number of block types (typically 7)
  static Matrix **arrayOfBlockObjects;
  static int iScreenDw;		// large enough to cover the largest block
  int top;		// y of the top left corner of the current block
  int left;		// x of the top left corner of the current block
  int idxBlockType;	// index for the current block type
  int idxBlockDegree;	// index for the current block degree
  Matrix *p_currBlk;	// current block
  bool isJustStarted;	// is the game just started?
  Matrix *p_iScreen;	// input screen (as background)
  Matrix *p_oScreen;	// output screen
  int iScreenDy;	// height of the background screen (excluding walls)
  int iScreenDx;	// width of the background screen (excluding walls)
  Tetris(int cy, int cx, int cw);
  ~Tetris();
  void init(int *arrayOfarray1D[], int ntypes, int ndegrees);
  void printScreen();
  void setOnLeftListener(ActionHandler *p_listener);
  void setOnRightListener(ActionHandler *p_listener);
  void setOnDownListener(ActionHandler *p_listener);
  void setOnUpListener(ActionHandler *p_listener);
  void setOnCWListener(ActionHandler *p_listener);
  void setOnCCWListener(ActionHandler *p_listener);
  void setOnNewBlockListener(ActionHandler *p_listener);
  void setOnFinishedListener(ActionHandler *p_listener);	
  TetrisState accept(char key);
};
