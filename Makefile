#
# Makefile (work in progress) for Wildcat, a RISC-V implementation.
#
# Include user makefile for local configurations, e.g., path to RISC-V tools
-include config.mk
RISCV?=$(HOME)/data/repository/rocket-chip/riscv-tools

#
# sbt looks for default into a folder ./project and . for build.sdt and Build.scala
# sbt creates per default a ./target folder

# Using sbt with a .jar file in the repository. Better install sbt
# SBT = java -Xmx1024M -Xss8M -XX:MaxPermSize=128M -jar sbt/sbt-launch.jar

SBT = sbt

all:
	echo "Select your make target"

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

# Assume RISC-V tools are built and installed.
# Set the path here or outside.
TEST_DIR=$(RISCV)/riscv-tests/isa
test:
	cp $(TEST_DIR)/rv32ui-p-lhu.hex asm/a.hex
	sbt run wildcat.isasim.SimRV

# passed tests (simulation):
# add addi and andi auipc beq bge bgeu blt bltu bne fence_i j jal jalr
# lb lbu ld lh lhu
# failed:
# amo* as this is A extension
# div divu divuw divw as this is M extension

# rv32ui tests available:
# add addi amoadd_w amoand_w amomax_w amomaxu_w amomin_w amominu_w amoor_w amoswap_w and andi auipc beq bge bgeu blt bltu bne div divu divuw divw fence_i j jal jalr lb lbu ld lh lhu lrsc lui lw lwu mul mulh mulhsu mulhu mulw or ori rem remu sb sh simple sll slli slt slti sra srai srl srli sub sw xor xori	

# get the test cases from Sodor as simple hex files
# not really used (yet)
test-sodor:
	-rm -rf riscv-sodor
	git clone https://github.com/ucb-bar/riscv-sodor.git
	cp -r riscv-sodor/install/riscv-tests/ tests
	rm -rf riscv-sodor





