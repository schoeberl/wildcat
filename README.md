# Wildcat

An implementation of RISC-V.

This repository currently contains an ISA simulator of the RISC-V instruction
set. Concrete the 32-bit integer version.

Minimal SW prerequisites are a version of git and sbt.

To start with wildcat either fork the project or clone it from here with:

    git clone https://github.com/schoeberl/wildcat
    cd wildcat

Here you can start the ISA simulator executing a simple program with

    sbt "run-main wildcat.isasim.SimRV bin/test.bin"

That command will execute the already assembled assembler program asm/test.S
as bin/test.bin and print out a register dump for each instruction.

To assemble other programs or compile C programs you need to install
the [RISC-V tools](https://github.com/riscv/riscv-tools).

Quickstart to install the tools on Ubuntu:

    sudo apt-get install autoconf automake autotools-dev curl libmpc-dev libmpfr-dev libgmp-dev gawk build-essential bison flex texinfo gperf libtool patchutils bc
    git clone https://github.com/riscv/riscv-tools.git
    cd riscv-tools
    git submodule update --init --recursive
    export RISCV=$HOME/riscv-tools/local
    ./build.sh

Than add the tools to your PATH in .bashrc with:

    export PATH=$PATH:$HOME/riscv-tools/local/bin

## Resources

There are several RISC-V projects around. As a starting point:

 * [Rocket Chip](https://github.com/ucb-bar/rocket-chip) the Berkeley processor including the tools (e.g., compiler)
 * [YARVI](https://github.com/tommythorn/yarvi) a RISC-V implementation in Verilog. Probably the first publicly available implementation that is synthesizabe for an FPGA.

## Notes

Why wildcat? The day before starting this project I was running
in the Wildcat Canyon in Tilden park. Very nice area.

## Note Collection

 * [Collection of Notes](doc)
