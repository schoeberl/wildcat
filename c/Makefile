
# build a minimal executable without any library

all:
	riscv64-unknown-elf-gcc -m32 crt0.c -c -o crt0.o
	riscv64-unknown-elf-gcc -m32 hello.c -c -o hello.o
	riscv64-unknown-elf-ld -melf32lriscv crt0.o hello.o
	riscv64-unknown-elf-objdump -d a.out
	riscv64-unknown-elf-objcopy -O binary -j .text a.out text.bin
	riscv64-unknown-elf-objcopy -O binary -j .data a.out data.bin
	cat text.bin data.bin > ../asm/a.bin
	hexdump -e '"%08x\n"' ../asm/a.bin

clean:
	-rm *.o *.out *.bin
