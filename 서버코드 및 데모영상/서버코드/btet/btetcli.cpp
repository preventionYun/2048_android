#include <iostream>
#include <cstdlib>
#include <ctime>
#include <stdio.h>
#include <termios.h>

#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "Tetris.h"

using namespace std;

class myOnLeft : public ActionHandler { 
public: void run(Tetris *t, char key); 
};
void myOnLeft::run(Tetris *t, char key) { t->left = t->left - 1; }

class myOnRight : public ActionHandler {
public: void run(Tetris *t, char key);
};
void myOnRight::run(Tetris *t, char key) { t->left = t->left + 1; }

class myOnDown : public ActionHandler {
public: void run(Tetris *t, char key);
};
void myOnDown::run(Tetris *t, char key) { t->top = t->top + 1; }

class myOnUp : public ActionHandler {
public: void run(Tetris *t, char key);
};
void myOnUp::run(Tetris *t, char key) { t->top = t->top - 1; }

class myOnCW : public ActionHandler {
public: void run(Tetris *t, char key);
};	
void myOnCW::run(Tetris *t, char key) { 			
  t->idxBlockDegree = (t->idxBlockDegree+1)%t->nBlockDegrees;
  t->p_currBlk = t->arrayOfBlockObjects[t->idxBlockType * t->nBlockDegrees +
					t->idxBlockDegree]; 
}

class myOnCCW : public ActionHandler {
public: void run(Tetris *t, char key);
};
void myOnCCW::run(Tetris *t, char key) { 			
  t->idxBlockDegree = (t->idxBlockDegree+3)%t->nBlockDegrees; 
  t->p_currBlk = t->arrayOfBlockObjects[t->idxBlockType * t->nBlockDegrees +
					t->idxBlockDegree]; 
}

class myOnNewBlock : public ActionHandler {
public: void run(Tetris *t, char key);
};
void deleteFullLines(Matrix *p_screen, Matrix *p_blk, int top, int dw) {
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
void myOnNewBlock::run(Tetris *t, char key) {
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

class myOnFinished : public ActionHandler {
public: void run(Tetris *t, char key);
};
void myOnFinished::run(Tetris *t, char key) {
  cout << "OnFinished.run() called" << endl;
}

static struct termios oldterm, newterm;

/* Initialize new terminal i/o settings */
void initTermios(int echo) {
  tcgetattr(0, &oldterm); /* grab old terminal i/o settings */
  newterm = oldterm; /* make new settings same as old settings */
  newterm.c_lflag &= ~ICANON; /* disable buffered i/o */
  newterm.c_lflag &= echo ? ECHO : ~ECHO; /* set echo mode */
  tcsetattr(0, TCSANOW, &newterm); /* use these new terminal i/o settings now */
}

/* Restore old terminal i/o settings */
void resetTermios(void) {
  tcsetattr(0, TCSANOW, &oldterm);
}

/* Read 1 character - echo defines echo mode */
char getch_(int echo) {
  char ch;
  initTermios(echo);
  ch = getchar();
  resetTermios();
  return ch;
}

/* Read 1 character without echo */
char getch(void) {  return getch_(0); }

/* Read 1 character with echo */
char getche(void) {  return getch_(1); }

int type0deg000[17] = { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, -1 };
int type0deg090[17] = { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1 };
int type0deg180[17] = { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, -1 };
int type0deg270[17] = { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1 };
int type1deg000[10] = { 1, 0, 0, 1, 1, 1, 0, 0, 0, -1 };
int type1deg090[10] = { 0, 1, 1, 0, 1, 0, 0, 1, 0, -1 };
int type1deg180[10] = { 0, 0, 0, 1, 1, 1, 0, 0, 1, -1 };
int type1deg270[10] = { 0, 1, 0, 0, 1, 0, 1, 1, 0, -1 };
int type2deg000[10] = { 0, 0, 1, 1, 1, 1, 0, 0, 0, -1 };
int type2deg090[10] = { 0, 1, 0, 0, 1, 0, 0, 1, 1, -1 };
int type2deg180[10] = { 0, 0, 0, 1, 1, 1, 1, 0, 0, -1 };
int type2deg270[10] = { 1, 1, 0, 0, 1, 0, 0, 1, 0, -1 };
int type3deg000[10] = { 0, 1, 0, 1, 1, 1, 0, 0, 0, -1 };
int type3deg090[10] = { 0, 1, 0, 0, 1, 1, 0, 1, 0, -1 };
int type3deg180[10] = { 0, 0, 0, 1, 1, 1, 0, 1, 0, -1 };
int type3deg270[10] = { 0, 1, 0, 1, 1, 0, 0, 1, 0, -1 };
int type4deg000[10] = { 0, 1, 0, 1, 1, 0, 1, 0, 0, -1 };
int type4deg090[10] = { 1, 1, 0, 0, 1, 1, 0, 0, 0, -1 };
int type4deg180[10] = { 0, 1, 0, 1, 1, 0, 1, 0, 0, -1 };
int type4deg270[10] = { 1, 1, 0, 0, 1, 1, 0, 0, 0, -1 };
int type5deg000[10] = { 0, 1, 0, 0, 1, 1, 0, 0, 1, -1 };
int type5deg090[10] = { 0, 1, 1, 1, 1, 0, 0, 0, 0, -1 };
int type5deg180[10] = { 0, 1, 0, 0, 1, 1, 0, 0, 1, -1 };
int type5deg270[10] = { 0, 1, 1, 1, 1, 0, 0, 0, 0, -1 };
int type6deg000[5] = { 1, 1, 1, 1, -1 };
int type6deg090[5] = { 1, 1, 1, 1, -1 };
int type6deg180[5] = { 1, 1, 1, 1, -1 };
int type6deg270[5] = { 1, 1, 1, 1, -1 };
#define NTYPES   7
#define NDEGREES 4
int *arrayOfarray1D[NTYPES*NDEGREES] = {
  type0deg000, type0deg090, type0deg180, type0deg270,
  type1deg000, type1deg090, type1deg180, type1deg270,
  type2deg000, type2deg090, type2deg180, type2deg270,
  type3deg000, type3deg090, type3deg180, type3deg270,
  type4deg000, type4deg090, type4deg180, type4deg270,
  type5deg000, type5deg090, type5deg180, type5deg270,
  type6deg000, type6deg090, type6deg180, type6deg270,
};

int intArrayLength(int *array) {
  int i;
  for (i = 0; array[i] >= 0; i++);
  return i;
}

int findLargestBlockSize(int *arrayOfarray1D[], int ntypes, int ndegrees)
{
  int k, len, kmax = 0, maxlen = 0;
  for (int t = 0, ilen = 0; t < ntypes; t++) {
    ilen = intArrayLength(arrayOfarray1D[t*ndegrees]);
    for (k = 1; k < MAX_BLOCK_SIZE; k++) {
      if (ilen == k*k) break;
    }
    if (k == MAX_BLOCK_SIZE) {
      cout << "every block should be a square with width smaller than"
	   << MAX_BLOCK_SIZE << endl;
      return -1;
    }
    
    // check if all rotating forms of a block have the same array length
    for (int d = 1; d < ndegrees; d++) {
      len = intArrayLength(arrayOfarray1D[t*ndegrees + d]);
      if (len != ilen) {
	cout << "every rotation of a block should have the same size" << endl;
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

int connect_server(char *ipaddr, char *portno)
{
  int sd = -1;
  struct sockaddr_in sin;

  if ((sd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
    cerr << "socket error: " << strerror(errno) << endl;
    return -1;
  }

  memset(&sin, 0, sizeof(sin));
  sin.sin_family = AF_INET;
  sin.sin_addr.s_addr = inet_addr(ipaddr);
  sin.sin_port = htons((short) atoi(portno));

  if (connect(sd, (struct sockaddr *)&sin, sizeof(sin)) < 0) {
    cerr << "connect error: " << strerror(errno) << endl;
    return -1;
  }
  return sd;
}

int sendto_server(int sd, char *line)
{
  int len = strlen(line);
  if (write(sd, line, len) != len) {
    cerr << "write error: " << strerror(errno) << endl;
    return -1;
  }
  return 0;
}

void countdown(int sec)
{
  int i;
  for (i = 0; i < sec; i++) {
    cout << "Countdown: " << (sec - i) << " seconds..." << endl;
    sleep(1);
  }
}

#define MAXDW 50
char myname[32], peername[32];

void run_in_solo_mode(Tetris *p_board)
{
  char currblk, nextblk, key;
  TetrisState state;
  char line[128];

  std::srand(std::time(0));
  currblk = (char) (std::rand()%NTYPES);
  currblk = currblk + '0';
  cout << currblk << endl;
  nextblk = (char) (std::rand()%NTYPES);
  nextblk = nextblk + '0';
  cout << nextblk << endl;
  p_board->accept(currblk); cout << endl;
  p_board->printScreen(); cout << endl;
  
  while ((key = getch()) != 'q') {
    cout << key << endl;
    state = p_board->accept(key); cout << endl;
    p_board->printScreen(); cout << endl;

    if (state == NewBlock) {
      currblk = nextblk;
      nextblk = (char) (std::rand()%NTYPES);
      nextblk = nextblk + '0';
      cout << nextblk << endl;
      state = p_board->accept(currblk); cout << endl;
      p_board->printScreen(); cout << endl;

      if (state == Finished) break; // Game Over!
    }
  }
}

void run_in_send_mode(int sd, Tetris *p_board)
{
  char currblk, nextblk, key;
  TetrisState state;
  char line[128];

  std::srand(std::time(0));
  currblk = (char) (std::rand()%NTYPES);
  currblk = currblk + '0';
  cout << currblk << endl;
  nextblk = (char) (std::rand()%NTYPES);
  nextblk = nextblk + '0';
  cout << nextblk << endl;
  p_board->accept(currblk); cout << endl;
  p_board->printScreen(); cout << endl;
  sprintf(line, "/msg %s %c\n", peername, currblk);  sendto_server(sd, line);
  sprintf(line, "/msg %s %c\n", peername, nextblk);  sendto_server(sd, line);

  while ((key = getch()) != 'q') {
    cout << key << endl;
    state = p_board->accept(key); cout << endl;
    p_board->printScreen(); cout << endl;
    sprintf(line, "/msg %s %c\n", peername, key); sendto_server(sd, line);

    if (state == NewBlock) {
      currblk = nextblk;
      nextblk = (char) (std::rand()%NTYPES);
      nextblk = nextblk + '0';
      cout << nextblk << endl;
      state = p_board->accept(currblk); cout << endl;
      p_board->printScreen(); cout << endl;
      sprintf(line, "/msg %s %c\n", peername, nextblk); sendto_server(sd, line);

      if (state == Finished) break; // Game Over!
    }
  }

  sprintf(line, "/msg %s %c\n", peername, 'Q');  sendto_server(sd, line);
  sprintf(line, "/quit\n");  sendto_server(sd, line);
}

char net_getch(int sd)
{
  char key, name[128], *pos;
  static char message[1024], *ppos = message;
  static int n = 0;

  if (ppos == message) { // read from the socket
    memset(message, 0, sizeof(message));
    if ((n = read(sd, message, sizeof(message))) < 0) {
      cerr << "read error: " << strerror(errno) << endl;
      return -1;
    }
  }
  //cout << "ppos: " << ppos << endl;
  if ((pos = strstr(ppos, "\n")) != NULL) {
    char *cpos = strstr(ppos, ":");
    *pos = 0;
    memcpy(name, ppos, cpos - ppos);
    name[cpos - ppos] = 0;
    key = cpos[2];
    if (strcmp(name, peername) != NULL) {
      cout << "peername mismatch!!" << endl;      
      cout << "message: " << message << endl;
      exit(1);
    }
    //cout << "name: " << name << endl;
    //cout << "key: " << key << endl;
    n -= (pos + 1 - ppos);
    ppos = pos + 1;
    if (n == 0) // reached the end of the content
      ppos = message;
  }
  
  return key;
}

void run_in_recv_mode(int sd, Tetris *p_board)
{
  char currblk, nextblk, key;
  TetrisState state;
  char line[128];

  currblk = net_getch(sd); cout << currblk << endl;
  nextblk = net_getch(sd); cout << nextblk << endl;
  p_board->accept(currblk); cout << endl;
  p_board->printScreen(); cout << endl;

  while ((key = net_getch(sd)) != 'Q') {
    cout << key << endl;
    state = p_board->accept(key); cout << endl;
    p_board->printScreen(); cout << endl;

    if (state == NewBlock) {
      currblk = nextblk;
      nextblk = net_getch(sd); cout << nextblk << endl;
      state = p_board->accept(currblk); cout << endl;
      p_board->printScreen(); cout << endl;

      if (state == Finished) break; // Game Over!
    }
  }
  cout << key << endl;
  //sprintf(line, "/msg %s %c\n", peername, 'Q');  sendto_server(sd, line);
  sprintf(line, "/quit\n");  sendto_server(sd, line);
}

int main(int argc, char *argv[]) {
  char line[128];
  int sd, flagsolo = 0, flagsend = 0, flagrecv = 0;
  int dy, dx, dw = findLargestBlockSize(arrayOfarray1D, NTYPES, NDEGREES);

  if (argc != 7) {
    cout << "usage: " << argv[0] << " ipaddr portnum myname peername dy dx" << endl;
    exit(1);
  }
  if (strstr(argv[0], "btetclisend") != NULL) {
    cout << "running mode: send" << endl;
    flagsend = 1;
  }
  if (strstr(argv[0], "btetclirecv") != NULL) {
    cout << "running mode: recv" << endl;
    flagrecv = 1;
  }
  if (strstr(argv[0], "btetclisolo") != NULL) {
    cout << "running mode: solo" << endl;
    flagsolo = 1;
  }
  dy = atoi(argv[5]);
  dx = atoi(argv[6]);
  if (dy < dw || dy > MAXDW  || dx < dw || dx > MAXDW) {
    cout << dw << " <= (dy, dx) <= " << MAXDW << endl;
    exit(1);
  }
  Tetris *p_board = new Tetris(dy, dx, dw);
  p_board->init(arrayOfarray1D, NTYPES, NDEGREES);
  p_board->setOnLeftListener(new myOnLeft());
  p_board->setOnRightListener(new myOnRight());
  p_board->setOnDownListener(new myOnDown());
  p_board->setOnUpListener(new myOnUp());
  p_board->setOnCWListener(new myOnCW());
  p_board->setOnCCWListener(new myOnCCW());
  p_board->setOnNewBlockListener(new myOnNewBlock());
  p_board->setOnFinishedListener(new myOnFinished());

  if (flagsolo == 0) {
    if ((sd = connect_server(argv[1], argv[2])) < 0)  exit(1);
    strcpy(myname, argv[3]);
    strcpy(peername, argv[4]);
    sprintf(line, "/nick %s\n", myname);  sendto_server(sd, line);
    countdown(10);
  }
  
  if (flagsolo == 1)
    run_in_solo_mode(p_board);
  if (flagsend == 1)
    run_in_send_mode(sd, p_board);
  if (flagrecv == 1)
    run_in_recv_mode(sd, p_board);

  if (flagsolo == 0) close(sd);
  cout << "Program terminated!" << endl;
}


