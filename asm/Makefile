
all:
	riscv32-unknown-elf-as test.s
	riscv32-unknown-elf-objdump -d a.out
	riscv32-unknown-elf-objcopy -O binary -j .text a.out text.bin
	riscv32-unknown-elf-objcopy -O binary -j .data a.out data.bin
	cat text.bin data.bin > test.bin
	hexdump -e '"%08x\n"' test.bin

clean:
	-rm *.out *.bin
