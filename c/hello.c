
int main() {
  // simulator has simple output on 0xf0000000
  int *ptr = (int *) 0xf0000000;
  *ptr = 'H';
}
