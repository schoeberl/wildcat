# Collection of Notes

Some notes collected along exploring RISC-V.

## Pipeline Organizations

 * single cycle (= simulator, or tiny programs)
 * sequential (like processors from the last century)
 * 2 stages (load and store will stall)
 * 3 stages
 * EX and MEM can be in parallel
 * EX can include decode
 * 4 stages: standard without the WB
 * 5 stages: mostly useless WB (just a Mux)

## Notes, Ideas

 * LUI could be recoded as ADDI with R0
 * Branch predictor idea from Tommy: ```pc == bp_addr ? bp_target : pc + 4``` and setting those on any taken branch

## Module Organization

 * An instruction memory/cache can contain a counter for the PC with autoincrement
   - just reset it on a jump or branch (like IBEX)
   - Or keep it classic with a pc-next input

## ECALL

`ecall` is an instruction to call out to a supervisor or operating system:
environment call.
The meaning of parameters is on purpose not defined in the RISC-V ISA.

However, following uses have been observed:

* Current ISA tests use a0/x10 and ecall for pass/fail, including a failed test case number.
  - `li a0, 0; li a7, 93`
  - A value of 0 is pass, fail is the test number multiplied by 2 added 1.
  - failing test is also in gp/x3 (somtimes multiplied by 2 and 1 added)
  - Probably spike implements this semantic.
  - Tests from ripes (and adapted riscv-tests) ecall a7/x17 93, a0/x10 0 pass, 1 fail; test number in gp/x3
 * Venus, a JavaScript based simulator, implements
   [environment calls](https://github.com/kvakil/venus/wiki/Environmental-Calls)
   some of the [system calls](https://www.doc.ic.ac.uk/lab/secondyear/spim/node8.html)
   from the MIPS simulators SPIM and MARS.
 * What does Ripes do?
 * What else in ecall is implemented in spike?
 * In a (newlib) compiled C program clib functions (such as file IO) call
   `__internal_syscall`, which itself (after rotation of registers `a0`-`a7`)
   executes `ecall`. Does this go to the proxy kernel (pk)?
 * RISC-V tests at https://github.com/riscv-software-src/riscv-tests

## Memory Layout

The memory layout is probably also environment (OS) dependent.
Here some observations:

 * ISA test cases start at 0x80000000 with a `_start` symbol
 * An assembler program linked with `.start` at 0x8000000 is executed by spike.
   However, spike starts at 0x1000 and executes 5 boostrap instructions.
   Assume those are hardcoded into spike.
 * Compiling a C program (with newlib) has a start address (`_start`) at 0x00010074.
   Assume that the proxy kernel is loaded at 0x8000000 and simply gets the start
  address from the .elf file.
 * Venus follows the MIPS (MARS/SPIM) convention:
   * `.text` at 0x00000000
   * `.data` at 0x10000000
   * sp at 0x7ffffff0
   * heap probably at 0x10008000
 * Hex files of some (old Sodor?) test cases start at 0x200.

## Software Traps

 * Unaligned memory access can be trapped and handled in SW (Berkeley cores do/did this)
   * Same can be done for floating point emulation


## Some Links

 * Online assembler: <https://riscvasm.lucasteske.dev/>
 * Carlos' assembler: https://github.com/carlosedp/riscvassembler


   