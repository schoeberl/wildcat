
![build status](https://github.com/schoeberl/wildcat/actions/workflows/scala.yml/badge.svg)

# Wildcat

Variations of RISC-V cores for education and real-time systems.

This repository contains an ISA simulator and hardware
implementations of the RISC-V instruction set architecture (ISA).
Up to now the 32-bit integer version (RV32I).

## Why Wildcat?

After listening to Andrew's PhD defense I was running
in the Wildcat Canyon in Tilden park. A very nice area.
The day after, I started with the RV32I simulator.

## A Paper

If you use Wildcat in your research or teaching, please cite:

```bibtex
@InProceedings{wildcat:2024,
  author    = {Martin Schoeberl},
  booktitle = {Proceedings of the Sixth Workshop on Open-Source EDA Technology (WOSET)},
  title     = {The Educational RISC-V Microprocessor Wildcat},
  year      = {2024},
}
```
Available at the [WOSET conference page](https://woset-workshop.github.io/WOSET2024.html).

and
```bibtex
@misc{schoeberl2025wildcateducationalriscvmicroprocessors,
      title={Wildcat: Educational RISC-V Microprocessors}, 
      author={Martin Schoeberl},
      year={2025},
      eprint={2502.20197},
      archivePrefix={arXiv},
      primaryClass={cs.AR},
      url={https://arxiv.org/abs/2502.20197}, 
}
```

## Setup

Prerequisit for this project is a working installation of
the [RISC-V tools](https://github.com/riscv/riscv-tools). Chances are high that you can install
them with your packet manager (see below).

Furthermore, you need a Java in version >= 8 and < 21
(the Chisel/Scala version in use in this project is broken for Java 21).

To build Wildcat in an FPGA you need either Quartus or Vivado.
The repo contains configuration for the Cyclon-IV based DE2-115
and for the Artix 7 based Nexys A7.

To start with Wildcat either fork the project or clone it from here with:

    git clone https://github.com/schoeberl/wildcat
    cd wildcat
    git submodule update --init --recursive

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

Running the simulation on one example (and generate .vcd files):

```
make APP=asm/test.s work
```

To generate Verilog code for synthesis, e.g., running a blinking
LED:

```
make APP=asm/apps/blink.s hw
make synth
```

For resource and timing analysis there is a top-level with
connections to the instruction and data memory:

```
make hw-fmax
```

Tests are run with
```
make test
```

### Memory Mapping

 * Current memory starts at 0
 * Simulator has a simple text output at address 0xf000_0000
 * LED is mapped to, maybe 0xe000_0000

## Results

Here are resource and fmax results for the 3-, 4-, and 5-stages pipeline.
We exclude instruction and data memory/cache in the numbers.

Cyclon IV has 4-bit LUTs and the timing info
is for the Slow 1200mV 85C Model.

Artix 7 has 6-bit LUTs.

Skywater130 timing info is for max_tt_025C_1v80.

| Design  (Cyclon IV)        | Fmax     | LEs   | Regs  | RAM bits |
|:---------------------------|:---------|:------|:------|:---------|
| Three stages (regfile FF)  | 80.2 MHz | 3,130 | 1,295 | 0        |
| Three stages (regfile mem) | 86.2 MHz | 1,756 | 379   | 2,048    |
| Four stages (regfile FF)   | 83.9 MHz | 3,040 | 1,367 | 0        |
| Four stages (regfile mem)  | 84.5 MHz | 1,727 | 451   | 2,048    |
| Five stages (regfile FF)   | 78.4 MHz | 3,107 | 1,438 | 0        |
| Five stages (regfile mem)  | 75.7 MHz | 1,813 | 522   | 2,048    |

| Design  (Artix 7)          | Fmax      | LCs   | Regs  | RAM bits |
|:---------------------------|:----------|:------|:------|:---------|
| Three stages (regfile FF)  | 99.6 MHz  | 1,744 | 1,329 | 0        |
| Three stages (regfile mem) | 112.3 MHz | 1,270 | 303   | 0        |
| Four stages (regfile FF)   | 107.5 MHz | 1,551 | 1,438 | 0        |
| Four stages (regfile mem)  | 111.2 MHz | 993   | 442   | 0        |
| Five stages (regfile FF)   | 106.1 MHz | 1,724 | 1,511 | 0        |
| Five stages (regfile mem)  | 102.0 MHz | 1,158 | 515   | 0        |

| Design  (Skywater130)     | Fmax     | Size           |
|:--------------------------|:---------|:---------------|
| Three stages (regfile FF) | 81.2 MHz | 429 x 432 umm2 |
| Four stages (regfile FF)  | 73.2 MHz | 433 x 438 umm2 |
| Five stages (regfile FF)  | 69.5 MHz | 439 x 443 umm2 |


For the FPGA designs we explore flip-flops (FF) and on-chip memory
(mem) for the register file.
For the ASIC design we use flip-flops only.
It is estimated that the register file dominates the area in the ASIC design.

The memory based register file is implemented with distributed
LUT RAMs in the Artix. Therefore, the number of bits in block
RAMs is zero.

## RISC-V Tools

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

## Student Projects

The Wildcat project shall become a complete microcontroller. Therefore, there are many possible student projects.
Here a list of project that could be a BSc, an MSc, a special course, an AdvCA project:

* Bootloader
    - can be code running on the core or a serial port based FSM accessing memory and reset
* Caches (I\$ and D\$)
* newlib port
* reactor-uc
* Testing infrstructure (e.g., complience tests)
* OS support: Zyphyr or similar
* Memory controller (SRAM on DE2-115 and DDR on Nexys A7)
* L2 cache for multi-core
* Tapeout
    - Tiny Tapeout
    - Edu4Chip
    - efabless
* Rust and WCET analysis
* ISA extensions
* Comparing RISC-V cores (start from https://dl-acm-org.proxy.findit.cvt.dk/doi/pdf/10.1145/3457388.3458657)
* Branch predictor (RTS?)
* Wildcat for real-time systems
  - WCET analysis
  - cache analysis (I\$ and D\$)
* Out-of-order version
* Traps to handle unimplemented features:
  - Unaligned memory access can be trapped and handled in SW (Berkeley cores do/did this)
  - floating point emulation
  - compressed instructions
* Interrupts and interrupt controller
* G extension
* floating-point unit
* Compare Ibex, Wildcat, and a third RISC-V core
* Towards booting uLinux and full Linux
* SoC stuff: interconnect, peripherals, ...

## Resources

There are several RISC-V projects around. As a starting point:

 * [Rocket Chip](https://github.com/ucb-bar/rocket-chip) the Berkeley processor including the tools (e.g., compiler)
 * [YARVI](https://github.com/tommythorn/yarvi) a RISC-V implementation in Verilog. Probably the first publicly available implementation that is synthesizabe for an FPGA.

## Note Collection

Scribbles related to RISC-V projects and tools:

 * [Collection of Notes](doc)

## Rust integration
**Note: This is still work-in-progress.**

### Setup
The Rust toolchain ([rustup](https://www.rust-lang.org/tools/install)) should be installed, as it is a
prerequisite for running embedded Rust using the ISA simulator.

Once ```rustup``` has been installed, the following components should be added by executing the following commands:
#### Target for RV32I
- ```rustup target add riscv32i-unknown-none-elf```
##### LLVM tools
- ```rustup component add llvm-tools-preview```
- ```cargo install cargo-binutils```

### Getting started
Currently, a simple starter project has been setup at [rust/starter-project](rust/starter-project).
To compile and run the starter project with the ISA simulator, 
execute the following command from the root of the Wildcat project:
- ```make rust-compile``` (compiles)
- ```make rust-run``` (executes)

Furthermore, the disassembly can be viewed by executing the following command:
- ```make rust-disassemble``` (prints disassembly)

### Creating new Rust projects
To maintain an organized project structure, a new Rust project can be created by executing the following command
from the root of the Wildcat project:
- ```cargo new rust/[YOUR_PROJECT_NAME] --vcs=none```

To compile and run the new project with the ISA simulator,
execute the following command from the root of the Wildcat project:
- ```make rust-compile RUST_PROJECT=[YOUR_PROJECT_NAME]``` (compiles target project)
- ```make rust-run RUST_PROJECT=[YOUR_PROJECT_NAME]``` (executes target project)

Furthermore, the disassembly for the new project can be viewed by executing the following command:
- ```make rust-disassembly``` (prints disassembly)