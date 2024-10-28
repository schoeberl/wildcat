

int main() {
  // UART on 0xf0000004: status and output
  volatile int *ptr = (int *) 0xf0000004;
  char *str = "Hello World!";
  for (int i = 0; i < 12; i++) {
    *ptr = str[i];
  }
}
