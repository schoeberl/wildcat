
all:
	riscv64-unknown-elf-as -m32 test.S
	riscv64-unknown-elf-objdump -d a.out
	riscv64-unknown-elf-objcopy -O binary -j .text a.out text.bin
	riscv64-unknown-elf-objcopy -O binary -j .data a.out data.bin
	cat text.bin data.bin > test.bin
	hexdump -e '"%08x\n"' test.bin

clean:
	-rm *.out *.bin
