#include <iostream>

using namespace std;

class Matrix {
private:
  static int nAlloc;
  static int nFree;
  int dy;
  int dx;
  int dx_shift;
  int dx_powered;
  int* array1D; // one-dimensional representation
  int** array; // two-dimensional representation
  void alloc(int, int);
  int get_shift_value(int);

public:
  int get_nAlloc() { return nAlloc; }
  int get_nFree() { return nFree; }
  int get_dy() { return dy; }
  int get_dx() { return dx; }
  int get_dx_shift() { return dx_shift; }
  int get_dx_powered() { return dx_powered; }
  int* get_array() { return array1D; }
  
  Matrix();
  Matrix(int cy, int cx);
  Matrix(int a[], int cy, int cx);
  Matrix(const Matrix &obj);
  ~Matrix();

  Matrix clip(int top, int left, int bottom, int right);
  void paste(Matrix &obj, int top, int left);
  int sum();
  void mulc(int coef);
  bool anyGreaterThan(int val);

  // overloaded operators
  Matrix& operator=(const Matrix&);
  friend ostream& operator<<(ostream&, const Matrix&);
  friend const Matrix operator+(const Matrix& m1, const Matrix& m2);
};

