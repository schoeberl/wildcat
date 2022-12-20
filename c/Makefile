
# build a minimal executable without any library

all:
	riscv32-unknown-elf-gcc crt0.c -c -o crt0.o
	riscv32-unknown-elf-gcc hello.c -c -o hello.o
	riscv32-unknown-elf-ld -melf32lriscv crt0.o hello.o
	riscv32-unknown-elf-objdump -d a.out
	riscv32-unknown-elf-objcopy -O binary -j .text a.out text.bin
	riscv32-unknown-elf-objcopy -O binary -j .data a.out data.bin
	cat text.bin data.bin > ../a.bin
	hexdump -e '"%08x\n"' ../a.bin

clean:
	-rm *.o *.out *.bin
