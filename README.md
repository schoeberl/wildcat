
![build status](https://github.com/schoeberl/wildcat/actions/workflows/scala.yml/badge.svg)

# Wildcat

Implementation variations of RISC-V for education.

This repository contains an ISA simulator and hardware
implementations of the RISC-V instruction set architecture.
Concrete the 32-bit integer version (RV32I).

## Why Wildcat?

After listening to Andrew's PhD defense I was running
in the Wildcat Canyon in Tilden park. A very nice area.
The day after, I started with the RV32I simulator.

## Setup

Prerequisit for this project is a working installation of
the [RISC-V tools](https://github.com/riscv/riscv-tools). Chances are high that you can install
them with your packet manager (see below).

Furthermore, you need a Java in version >= 8 and < 21
(sbt is broken for Java 21).

To build Wildcat in an FPGA you need either Quartus or Vivado.
The repo contains configuration for the Cyclon-IV based DE2-115
and for the Artix 7 based Nexys A7.

To start with Wildcat either fork the project or clone it from here with:

    git clone https://github.com/schoeberl/wildcat
    cd wildcat

Here you can start the ISA simulator executing a simple program with

```
make app run
```
That command will assemble a tiny program and run
the simulator executing `asm/test.s`, a small program,
which exits with a test pass code (from an old test convention).
To run other programs use the environment variable APP. E.g.:
```
make app run APP=asm/mem.s
```
### More make targets

To generate Verilog code for synthesis, e.g., running a blinking
LED:

```
make APP=asm/apps/blink.s hw
```

For resource and timing analysis there is a top-level with
connections to the insrtuction and data memory:

```
make hw-fmax
```

Tests are run with
```
make test
```

## Results

Here are resource and fmax results for the 3-stage pipeline.
We exclude instruction and data memory/cache in the numbers.

Cyclon IV has 4-bit LUTs and the timing info
is for the Slow 1200mV 85C Model.

Artix 7 has 6-bit LUTs and the timing info

Skywater130 timing info is for max_tt_025C_1v80.

| Design  (Cyclon IV)        | Fmax     | LEs   | Regs  | RAM bits |
|:---------------------------|:---------|:------|:------|:---------|
| Three stages (regfile FF)  | 80.2 MHz | 3,130 | 1,295 | 0        |
| Three stages (regfile mem) | 86.2 MHz | 1,756 | 379   | 2,048    |

| Design  (Artix 7)          | Fmax      | LCs   | Regs  | RAM bits |
|:---------------------------|:----------|:------|:------|:---------|
| Three stages (regfile FF)  | 99.6 MHz  | 1,744 | 1,329 | 0        |
| Three stages (regfile mem) | 112.3 MHz | 1,270 | 303   | 0        |

| Design  (Skywater130)     | Fmax     | Size           |
|:--------------------------|:---------|:---------------|
| Three stages (regfile FF) | 81.2 MHz | 429 x 432 umm2 |

For the FPGA designs we use on-chip memory for the register file.
For the ASIC design we use flip-flops. It is estimated that the
RF dominates the area in the ASIC design.

The memory based register file is implemented with distributed
LUT RAMs in the Artix. Therefore, the number of bits in block
RAMs is zero.

### Tools on MacOS

Use brew to install gcc:

```
brew tap riscv-software-src/riscv 
brew install riscv-gnu-toolchain --with-NOmultilib
```

### Tools on Ubuntu:

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

## Note Collection

Scribbles related to RISC-V projects and tools:

 * [Collection of Notes](doc)

