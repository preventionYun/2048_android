#include "Tetris.h"

using namespace std;

int Tetris::nBlockDegrees = 0;
int Tetris::nBlockTypes = 0;
Matrix **Tetris::arrayOfBlockObjects = NULL;
int Tetris::iScreenDw = 0;

int* Tetris::createArrayScreen(int dy, int dx, int dw) {
  int y, x;
  int DY = dy + dw;
  int DX = dx + 2*dw;
  int* array = new int[DY*DX];
  for (y = 0; y < dy; y++) {
    for (x = 0; x < dw; x++)
      array[y*DX + x] = 1;
    for (x = dw + dx; x < DX; x++)
      array[y*DX + x] = 1;
  }
  for (y = dy; y < DY; y++)
    for (x = 0; x < DX; x++)
      array[y*DX + x] = 1;
  return array;
}

void Tetris::printMatrix(Matrix blk) { // for debugging purposes
  int dy = blk.get_dy();
  int dx = blk.get_dx();
  int dxpow = blk.get_dx_powered();
  int* array = blk.get_array();
  for (int y = 0; y < dy; y++) {
    for (int x = 0; x < dx; x++) {
      if (array[y*dxpow + x] == 0) cout << "□ ";
      else if (array[y*dxpow + x] == 1) cout << "■ ";
      else cout << "X ";
    }
    cout << endl;
  }
}

void Tetris::printScreen() {
  Matrix *p_screen = p_oScreen;
  int dy = p_screen->get_dy();
  int dx = p_screen->get_dx();
  int dxpow = p_screen->get_dx_powered();
  int dw = iScreenDw;
  int* array = p_screen->get_array();
  for (int y = 0; y < dy - dw + 1; y++) {
    for (int x = dw - 1; x < dx - dw + 1; x++) {
      if (array[y*dxpow + x] == 0) cout << "□ ";
      else if (array[y*dxpow + x] == 1) cout << "■ ";
      else cout << "X ";
    }
    cout << endl;
  }
}

static int intArrayLength(int *array) {
  int i;
  for (i = 0; array[i] >= 0; i++);
  return i;
}

static int findLargestBlockSize(int *arrayOfarray1D[], int ntypes, int ndegrees)
{
  int k, len, kmax = 0, maxlen = 0;
  for (int t = 0, ilen = 0; t < ntypes; t++) {
    ilen = intArrayLength(arrayOfarray1D[t*ndegrees]);
    for (k = 1; k < MAX_BLOCK_SIZE; k++) {
      if (ilen == k*k) break;
    }
    if (k == MAX_BLOCK_SIZE) {
      cout << "Tetris: every block should be a square with width smaller than"
	   << MAX_BLOCK_SIZE << endl;
      return -1;
    }
    
    // check if all rotating forms of a block have the same array length
    for (int d = 1; d < ndegrees; d++) {
      len = intArrayLength(arrayOfarray1D[t*ndegrees + d]);
      if (len != ilen) {
	cout << "Tetris: every rotation of a block should have the same size" << endl;
	return -1;
      }
    }
    if (ilen > maxlen) {
      kmax = k;
      maxlen = ilen;
    }
  }
  return kmax;
}
  
void Tetris::createArrayOfBlockObjects(int* arrayOfarray1D[], int ntypes, int ndegrees) {
  int len, size;
  iScreenDw = findLargestBlockSize(arrayOfarray1D, ntypes, ndegrees);
  arrayOfBlockObjects = new Matrix *[ntypes*ndegrees];
  for (int t = 0; t < ntypes; t++) {
    for (int d = 0; d < ndegrees; d++) {
      len = intArrayLength(arrayOfarray1D[t*ndegrees+d]);
      size = sqrt(len);
      arrayOfBlockObjects[t*ndegrees+d] =
	new Matrix(arrayOfarray1D[t*ndegrees+d], size, size);
    }
  }
  return;
}

void Tetris::init(int *arrayOfarray1D[], int ntypes, int ndegrees) {
  nBlockTypes = ntypes;
  nBlockDegrees = ndegrees;
  createArrayOfBlockObjects(arrayOfarray1D, nBlockTypes, nBlockDegrees);
}

void Tetris::setTetrisActions() {
  if (p_moveLeft != NULL) delete p_moveLeft;
  if (p_moveRight != NULL) delete p_moveRight;
  if (p_moveDown != NULL) delete p_moveDown;
  if (p_rotateCW != NULL) delete p_rotateCW;
  if (p_insertBlk != NULL) delete p_insertBlk;
  p_moveLeft  = new TetrisAction(p_onLeft, p_onRight);
  p_moveRight = new TetrisAction(p_onRight, p_onLeft);
  p_moveDown  = new TetrisAction(p_onDown, p_onUp);
  p_rotateCW  = new TetrisAction(p_onCW, p_onCCW);
  p_insertBlk = new TetrisAction(p_onNewBlock, p_onFinished);
  tetrisActionsInitialized = true;
  cout << "Tetris:setTetrisActions() called" << endl;
}

bool Tetris::moveLeft(char key, bool update) {
  return p_moveLeft->run(this, key, update);
}

bool Tetris::moveRight(char key, bool update) {
  return p_moveRight->run(this, key, update);
}

bool Tetris::moveDown(char key, bool update) {
  return p_moveDown->run(this, key, update);
}

bool Tetris::rotateCW(char key, bool update) {
  return p_rotateCW->run(this, key, update);
}

bool Tetris::insertBlk(char key, bool update) {
  return p_insertBlk->run(this, key, update);
}

class OnLeft : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnLeft::run(Tetris *t, char key) { t->left = t->left - 1; }

class OnRight : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnRight::run(Tetris *t, char key) { t->left = t->left + 1; }

class OnDown : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnDown::run(Tetris *t, char key) { t->top = t->top + 1; }

class OnUp : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnUp::run(Tetris *t, char key) { t->top = t->top - 1; }

class OnCW : public ActionHandler {
public: void run(Tetris *t, char key);
};	
void OnCW::run(Tetris *t, char key) { 			
  t->idxBlockDegree = (t->idxBlockDegree+1)%t->nBlockDegrees;
  t->p_currBlk = t->arrayOfBlockObjects[t->idxBlockType * t->nBlockDegrees +
					t->idxBlockDegree]; 
}

class OnCCW : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnCCW::run(Tetris *t, char key) { 			
  t->idxBlockDegree = (t->idxBlockDegree+3)%t->nBlockDegrees; 
  t->p_currBlk = t->arrayOfBlockObjects[t->idxBlockType * t->nBlockDegrees +
					t->idxBlockDegree]; 
}

class OnNewBlock : public ActionHandler {
public: void run(Tetris *t, char key);
};
static void deleteFullLines(Matrix *p_screen, Matrix *p_blk, int top, int dw) {
  int dy = p_screen->get_dy() - dw;
  int dx = p_screen->get_dx();
  Matrix line0(1, dx), line, tmp;
  int *array = line0.get_array();
  for (int i = 0; i < dw; i++) { // build up an empty line
    array[i] = 1;
    array[i+dx-dw] = 1;
  }
  int cy, y, nDeleted = 0, nScanned = p_blk->get_dy();
  if (top + p_blk->get_dy() - 1 >= dy)
    nScanned -= (top + p_blk->get_dy() - dy);
  for (y = nScanned - 1; y >= 0 ; y--) {
    cy = top + y + nDeleted;
    line = p_screen->clip(cy, 0, cy + 1, p_screen->get_dx());
    if (line.sum() == p_screen->get_dx()) {
      tmp = p_screen->clip(0, 0, cy, p_screen->get_dx());
      p_screen->paste(tmp, 1, 0);
      p_screen->paste(line0, 0, 0);
      nDeleted++;
    }
  }
}
void OnNewBlock::run(Tetris *t, char key) {
  if (t->isJustStarted == false) 
    deleteFullLines(t->p_oScreen, t->p_currBlk, t->top, t->iScreenDw);
  t->isJustStarted = false;
  t->p_iScreen->paste(*t->p_oScreen, 0, 0);
  t->top = 0;
  t->left = t->iScreenDw + t->iScreenDx/2 - 2;
  t->idxBlockType = key - '0'; // copied from key
  t->idxBlockDegree = 0;
  t->p_currBlk = t->arrayOfBlockObjects[t->idxBlockType * t->nBlockDegrees +
					t->idxBlockDegree]; 
}

class OnFinished : public ActionHandler {
public: void run(Tetris *t, char key);
};
void OnFinished::run(Tetris *t, char key) {
  cout << "OnFinished.run() called" << endl;
}

Tetris::Tetris(int cy, int cx, int cw) {
  //if (arrayOfBlockObjects == NULL) init();
  if (cy < cw || cx < cw)
    cout << "Tetris: too small screen" << endl; 
  iScreenDy = cy;
  iScreenDx = cx;
  iScreenDw = cw;
  state = NewBlock;

  int *arrayScreen = createArrayScreen(cy, cx, iScreenDw);
  p_iScreen = new Matrix(arrayScreen, cy+iScreenDw, cx+2*iScreenDw);
  delete [] arrayScreen;
  p_oScreen = new Matrix(*p_iScreen);
  isJustStarted = true;
  tetrisActionsInitialized = false;

  p_onLeft  = new OnLeft();
  p_onRight = new OnRight();
  p_onDown  = new OnDown();
  p_onUp    = new OnUp();
  p_onCW    = new OnCW();
  p_onCCW   = new OnCCW();
  p_onNewBlock = new OnNewBlock();
  p_onFinished = new OnFinished();
  p_moveLeft  = new TetrisAction(p_onLeft, p_onRight);
  p_moveRight = new TetrisAction(p_onRight, p_onLeft);
  p_moveDown  = new TetrisAction(p_onDown, p_onUp);
  p_rotateCW  = new TetrisAction(p_onCW, p_onCCW);
  p_insertBlk = new TetrisAction(p_onNewBlock, p_onFinished);
  tetrisActionsInitialized = true;
}

Tetris::~Tetris() {
  if (arrayOfBlockObjects != NULL) {
    delete p_onLeft;
    delete p_onRight;
    delete p_onDown;
    delete p_onUp;
    delete p_onCW;
    delete p_onCCW;
    delete p_onNewBlock;
    delete p_onFinished;
    delete p_moveLeft;
    delete p_moveRight;
    delete p_moveDown;
    delete p_rotateCW;
    delete p_insertBlk;
    delete p_currBlk;
    delete p_iScreen;
    delete p_oScreen;
    for (int t = 0; t < nBlockTypes; t++)
      for (int d = 0; d < nBlockDegrees; d++)
	delete arrayOfBlockObjects[t*nBlockDegrees+d];
    delete [] arrayOfBlockObjects;
  }
}

void Tetris::setOnLeftListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onLeft != NULL) delete p_onLeft;
  p_onLeft = p_listener;
}

void Tetris::setOnRightListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onRight != NULL) delete p_onRight;
  p_onRight = p_listener;
}

void Tetris::setOnDownListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onDown != NULL) delete p_onDown;
  p_onDown = p_listener;
}

void Tetris::setOnUpListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onUp != NULL) delete p_onUp;
  p_onUp = p_listener;
}

void Tetris::setOnCWListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onCW != NULL) delete p_onCW;
  p_onCW = p_listener;
}

void Tetris::setOnCCWListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onCCW != NULL) delete p_onCCW;
  p_onCCW = p_listener;
}

void Tetris::setOnNewBlockListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onNewBlock != NULL) delete p_onNewBlock;
  p_onNewBlock = p_listener;
}

void Tetris::setOnFinishedListener(ActionHandler *p_listener) {
  tetrisActionsInitialized = false;
  if (p_onFinished != NULL) delete p_onFinished;
  p_onFinished = p_listener;
}	

TetrisState Tetris::accept(char key) {
  if (tetrisActionsInitialized == false)
    setTetrisActions();
  if (state == NewBlock) {
    if (insertBlk(key,  true) == true)
      state = Finished;
    else
      state = Running;
    return state;
  }
  switch(key) {
  case 'a': moveLeft(key, true); break; // move left
  case 'd': moveRight(key, true); break; // move right
  case 'w': rotateCW(key, true); break; // rotateCW
  case 's': // move down
    if (moveDown(key, true) == true)
      state = NewBlock; 
    break; 
  case ' ': // drop the block
    while (moveDown(key, false) == false);
    moveDown(key,  true);
    state = NewBlock;
    break;
  default:
    cout << "Tetris: unknown key!" << endl;
  }
  return state;
}	
 
