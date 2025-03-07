# Rust integration

RUST_TARGET = riscv32i-unknown-none-elf
RUST_APP = target/$(RUST_TARGET)/release/rust-riscv32i

all:
	cargo build --release

	rust-objcopy $(RUST_APP) -O binary -j .text target/text.bin
	rust-objcopy $(RUST_APP) -O binary -j .data target/data.bin

	cat target/text.bin target/data.bin > target/rust.bin

	rust-objdump --disassemble --arch=riscv32 $(RUST_APP)