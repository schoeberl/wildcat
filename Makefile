#
# Makefile (work in progress) for Wildcat, a RISC-V simulator and implementation.
#
# Include user makefile for local configurations, e.g., path to RISC-V tools
-include config.mk
RISCV?=$(HOME)/data/repository/rocket-chip/riscv-tools

# Using sbt with a .jar file in the repository. Better install sbt
# SBT = java -Xmx1024M -Xss8M -XX:MaxPermSize=128M -jar sbt/sbt-launch.jar

SBT = sbt

APP=test.s
# Use a different program, e.g., one from the CAE lab
#APP=../../cae-lab/lab3/minimal.s

all:
	echo "Select your make target"

app:
	make -C asm APP=$(APP)
	cp asm/test.bin bin

run:
	sbt "run-main wildcat.isasim.SimRV bin/test.bin"


# Just a start of an assembler in Scala
assemble:
	sbt "run-main wildcat.asm.TestParser"


#############################################
# Below for future work - please ignore
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


# Generate Verilog code
hdl:
	$(SBT) "run-main hello.HelloMain --targetDir generated --backend v"

# Generate C++ code (simulation)
cpp:
	$(SBT) "run-main hello.HelloMain --backend c --compile --targetDir generated"

hw:
	$(SBT) "run-main wildcat.pipeline.WildcatMain $(HW_ARGS)"

test-hw:
	$(SBT) "run-main wildcat.pipeline.WildcatTester $(TEST_ARGS_VCD)"
	gtkwave generated/Wildcat.vcd --save=wildcat.gtkw

# Assume RISC-V tools are built and installed.
# Set the path here or in config.mk.
TEST_DIR=$(RISCV)/riscv-tests/isa
test:
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





