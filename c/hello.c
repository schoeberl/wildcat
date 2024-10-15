

int main() {
  // simulator has simple output on 0xf0000000
  volatile int *ptr = (int *) 0xf0000000;
  char *str = "Hello World!";
  for (int i = 0; i < 12; i++) {
    *ptr = str[i];
  }
}
