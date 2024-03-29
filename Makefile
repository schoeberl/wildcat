#
# Makefile (work in progress) for Wildcat, a RISC-V simulator and implementation.
#
# Include user makefile for local configurations, e.g., path to RISC-V tools
-include config.mk

SBT = sbt

#APP=asm/test.s
APP=asm/riscv-v1_addi.s
# Use a different program, e.g., one from the CAE lab
# APP=../../cae-lab/lab3/minimal.s
# APP can be set from the command line, such as:
# make app run APP=../cae-lab/lab3/minimal.s

all:
	echo "Select your make target"

risc-v-lab:
	git clone https://github.com/schoeberl/risc-v-lab.git

work:
	sbt "testOnly wildcat.SingleTest"

app:
	riscv64-unknown-elf-as -march rv32i $(APP)
	riscv64-unknown-elf-objdump -d a.out
	riscv64-unknown-elf-objcopy -O binary -j .text a.out text.bin
	riscv64-unknown-elf-objcopy -O binary -j .data a.out data.bin
	cat text.bin data.bin > a.bin
	hexdump -e '"%08x\n"' a.bin

run:
	sbt "runMain wildcat.isasim.SimRV a.bin"

test: risc-v-lab
	sbt test

sim-test:
	sbt "testOnly wildcat.SimulatorTest"

elf:
	sbt "runMain wildcat.isasim.ElfUtil a.out"

comp:
	make -C c

clean:
	git clean -fd
	rm -rf ./idea
	rm -rf ./risv-v-lab



#############################################
# Below for future work - please ignore
# Probably old stuff that needs to be removed
#############################################

HW_ARGS = --targetDir generated --backend v
TEST_ARGS = --genHarness --test --backend c --compile --targetDir generated
TEST_ARGS_VCD = --genHarness --test --backend c --compile --vcd --targetDir generated

# This generates the Verilog and C++ files by invoking main from
# class HelloMain in package hello.
# The source directory is configured in build.sbt.
# The Scala/Java build directory is default ./target.

# The first two arguments are consumed by sbt, the rest is
# forwarded to the Scala/Chisel main().

hw:
	$(SBT) "runMain wildcat.pipeline.WildcatMain $(HW_ARGS)"

test-hw:
	$(SBT) "runMain wildcat.pipeline.WildcatTester $(TEST_ARGS_VCD)"
	gtkwave generated/Wildcat.vcd --save=wildcat.gtkw

# Assume RISC-V tools are built and installed.
# Set the path here or in config.mk.
TEST_DIR=$(RISCV)/riscv-tests/isa
test-old:
	cp $(TEST_DIR)/rv32ui-p-xori.hex asm/a.hex
	sbt "run-main wildcat.isasim.SimRV"

# passed tests (simulation):
# add addi and andi auipc beq bge bgeu blt bltu bne fence_i j jal jalr
# lb lbu ld lh lhu lui lw or ori sb sh simple sll slli slt slti sra
# srai srl srli sub sw xor xori
# 
# failed:
# amo* as this is A extension
# div divu divuw divw mul mulh mulhsu mulhu mulw as this is M extension

# rv32ui tests available:
# add addi amoadd_w amoand_w amomax_w amomaxu_w amomin_w amominu_w amoor_w amoswap_w and andi auipc beq bge bgeu blt bltu bne div divu divuw divw fence_i j jal jalr lb lbu ld lh lhu lui lw mul mulh mulhsu mulhu mulw or ori rem remu sb sh simple sll slli slt slti sra srai srl srli sub sw xor xori	





