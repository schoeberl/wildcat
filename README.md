
![build status](https://github.com/schoeberl/wildcat/actions/workflows/scala.yml/badge.svg)

# Wildcat

An implementation of RISC-V for education.

This repository currently contains an ISA simulator of the RISC-V instruction
set. Concrete the 32-bit integer version.

Minimal SW prerequisites are a version of git and sbt.

To start with wildcat either fork the project or clone it from here with:

    git clone https://github.com/schoeberl/wildcat
    cd wildcat

Here you can start the ISA simulator executing a simple program with

    sbt "runMain wildcat.isasim.SimRV"

That command will execute the already assembled tiny program included
as constants in the simulator.

To assemble other programs or compile C programs you need to install
the [RISC-V tools](https://github.com/riscv/riscv-tools).

Assemble assembler files and simulate them with
```
make app run
```
which executes `asm/test.s` a small program, which exits with a test pass
code (from probably an old test convention). To run other programs
use the environment variable APP. E.g.:
```
make app run APP=../cae-lab/lab3/minimal.s
```

### Tools on MacOS

Use brew to install gcc:

```
brew tap riscv-software-src/riscv 
brew install riscv-gnu-toolchain --with-NOmultilib
```

### Quickstart to install the tools on Ubuntu:

Should be best installed with apt-get:

    sudo apt-get install -y gcc-riscv64-unknown-elf

#### Build from source

If you want to compiler them from source, here are some notes:

    sudo apt-get install autoconf automake autotools-dev curl libmpc-dev libmpfr-dev libgmp-dev gawk build-essential bison flex texinfo gperf libtool patchutils bc
    git clone https://github.com/riscv/riscv-tools.git
    cd riscv-tools
    git submodule update --init --recursive
    export RISCV=$HOME/riscv-tools/local
    ./build.sh

Then add the tools to your PATH in .bashrc or .profile with:

    export PATH=$PATH:$HOME/riscv-tools/local/bin

See also: [RISC-V Ubuntu Setup](https://github.com/schoeberl/cae-lab#vm-and-tool-installation)

## Resources

There are several RISC-V projects around. As a starting point:

 * [Rocket Chip](https://github.com/ucb-bar/rocket-chip) the Berkeley processor including the tools (e.g., compiler)
 * [YARVI](https://github.com/tommythorn/yarvi) a RISC-V implementation in Verilog. Probably the first publicly available implementation that is synthesizabe for an FPGA.

## Why Wildcat?

The day before starting this project I was running
in the Wildcat Canyon in Tilden park. A very nice area.

## Note Collection

Scribbles related to RISC-V projects and tools:

 * [Collection of Notes](doc)

