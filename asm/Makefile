
all:
	riscv64-unknown-elf-as riscv-v1_bne.S
	riscv64-unknown-elf-objdump -d a.out
	riscv64-unknown-elf-objcopy -O binary a.out a.bin
	hexdump -e '"%08x\n"' a.bin

clean:
	-rm *.out *.bin
