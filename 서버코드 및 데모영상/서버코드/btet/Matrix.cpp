#include "Matrix.h"

int Matrix::nAlloc = 0;
int Matrix::nFree = 0;

void Matrix::alloc(int cy, int cx) {
  if((cy < 0) || (cx < 0)) {
    cout << "Matrix: wrong matrix size" << endl;
    return;
  }
  dy = cy;
  dx = cx;
  dx_shift = get_shift_value(dx);
  dx_powered = 1 << dx_shift;
  array1D = new int[dy*dx_powered];
  array = new int*[dy];
  for (int i = 0; i < dy; i++)
    array[i] = &array1D[i << dx_shift];
  
  nAlloc++; // count the number of allocated objects
}
  
Matrix::Matrix() { alloc(0, 0); }

Matrix::Matrix(int cy, int cx) {
  alloc(cy, cx); 
  for(int y = 0; y < dy; y++)
    for(int x = 0; x < dx; x++)
      array[y][x] = 0;
}

Matrix::Matrix(const Matrix& obj) {
  alloc(obj.dy, obj.dx);
  for(int y = 0; y < dy; y++)
    for(int x = 0; x < dx; x++)
      array[y][x] = obj.array[y][x];
}	

Matrix::Matrix(int a[], int cy, int cx) {
  alloc(cy, cx); 
  for(int y = 0; y < dy; y++)
    for(int x = 0; x < dx; x++)
      array[y][x] = a[y*dx + x];
}

Matrix::~Matrix() {
  if (array != NULL) {
    delete []array1D;
    delete []array;
  }
  nFree++;
}

Matrix Matrix::clip(int top, int left, int bottom, int right) {
  int cy = bottom - top; 
  int cx = right - left; 
  Matrix temp(cy, cx);

  if(dy == 0 || dx == 0) {
    cout << "Matrix: not initialized object" << endl;
    return *this;
  }  
  for(int y = 0; y < cy; y++){
    for(int x = 0; x < cx; x++){
      if((top+y >= 0) && (left+x >= 0) && (top+y < dy) && (left+x < dx)) 
	temp.array[y][x] = array[top+y][left+x];
      else
	cout << "Matrix: invalid matrix range" << endl;
    }
  }
  return temp; 
}

void Matrix::paste(Matrix& obj, int top, int left) {
  if(dy == 0 || dx == 0) {
    cout << "Matrix: not initialized object" << endl;
    return;
  }  
  for(int y = 0; y < obj.dy; y++)
    for(int x = 0; x < obj.dx; x++) {
      if((top+y >= 0) && (left+x >= 0) && (top+y < dy) && (left+x < dx)) 
	array[y + top][x + left] = obj.array[y][x];
      else 
	cout << "Matrix: invalid matrix range" << endl; 
    }
}

int Matrix::sum(){
  if(dy == 0 || dx == 0) {
    cout << "Matrix: not initialized object" << endl;
    return -1;
  }    
  int total = 0;
  for(int y = 0; y < dy; y++)
    for(int x = 0; x < dx; x++)
      total += array[y][x];
  return total;
}	

void Matrix::mulc(int coef){
  if(dy == 0 || dx == 0) {
    cout << "Matrix: not initialized object" << endl;
    return;
  }  
  for(int y = 0; y < dy; y++)
    for(int x = 0; x < dx; x++)
      array[y][x] = coef * array[y][x];
}	

bool Matrix::anyGreaterThan(int val){
  if(dy == 0 || dx == 0) {
    cout << "Matrix: not initialized object" << endl;
    return false;
  }  
  for(int y = 0; y < dy; y++){
    for(int x = 0; x < dx; x++){
      if (array[y][x] > val) return true;  
    }
  }
  return false; 
}	

/////////////////////////////////////////////
////////// operator overloading /////////////
/////////////////////////////////////////////

int Matrix::get_shift_value(int val)
{
  int i;
  if(val < 1) return -1;
  for(i = 0; i < 31; i++){
    if((1 << i) < val)
      continue;
    break;
  }
  return i;
}

Matrix& Matrix::operator=(const Matrix& obj)
{
  if(this == &obj)
    return *this;
  else{
    if((dx != obj.dx) || (dy != obj.dy)){
      delete []array1D;
      delete []array;
      alloc(obj.dy, obj.dx);
    }
    for(int y = 0; y < dy; y++)
      for(int x = 0; x < dx; x++)
	array[y][x] = obj.array[y][x];
  }
  return *this;
}

ostream& operator<<(ostream& out, const Matrix& obj) {
  cout << "Matrix(" << obj.dy << "," << obj.dx << ")" << endl;
  for(int y = 0; y < obj.dy; y++) {
    for(int x = 0; x < obj.dx; x++)
      cout << obj.array[y][x] << " ";
    cout << endl;
  }
  cout << endl;
  return out;
}

const Matrix operator+(const Matrix& m1, const Matrix& m2) {
  if((m1.dx != m2.dx) || (m1.dy != m2.dy)) {
    cout << "Matrix: matrix sizes mismatch" << endl;
    return Matrix(0, 0);
  }
  Matrix temp(m1.dy, m1.dx);
  for(int y = 0; y < m1.dy; y++)
    for(int x = 0; x < m1.dx; x++)
      temp.array[y][x] = m1.array[y][x] + m2.array[y][x]; 
  return temp;
}

