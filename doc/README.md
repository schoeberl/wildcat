# Collection of Notes

Some notes collected along exploring RISC-V.

## ECALL

`ecall` is an instruction to call out to a supervisor or operating system:
environment call.
The meaning of parameters is on purpose not defined in the RISC-V ISA.

However, following uses have been observed:

 * Assembler tests (old, from Sodor?), as included in this repo, use x28 with
   1 as a notion of pass and 2 as a notion to fail. This has been implemented
   in Wildcat.
 * Venus, a JavaScript based simulator, implements
   [environment calls](https://github.com/kvakil/venus/wiki/Environmental-Calls)
   some of the [system calls](https://www.doc.ic.ac.uk/lab/secondyear/spim/node8.html)
   from the MIPS simulators SPIM and MARS.
 * Current ISA tests use a0 and ecall for pass/fail, including a failed test case number.
   A value of 1 is pass, fail is the test number multiplied by 2 added 1.
   Probably spike implements this semantic.
 * What else in ecall is implemented in spike?
 * In a (newlib) compiled C program clib functions (such as file IO) call
   `__internal_syscall`, which itself (after rotation of registers `a0`-`a7`)
   executes `ecall`. Does this go to the proxy kernel (pk)?
 * RISC-V tests at https://github.com/riscv-software-src/riscv-tests

## Memory Layout

The memory layout is probably also environment (OS) dependent.
Here some observations:

 * Venus follows the MIPS (MARS/SPIM) convention:
   * `.text` at 0x00000000 
   * `.data` at 0x10000000
   * sp at 0x7ffffff0
   * heap probably at 0x10008000
 * Hex files of some (old Sodor?) test cases start at 0x200.
 * ISA test cases start at 0x80000000 with a `_start` symbol
 * An assembler program linked with `.start` at 0x8000000 is executed by spike.
   However, spike starts at 0x1000 and executes 5 boostrap instructions.
   Assume those are hardcoded into spike.
 * Compiling a C program (with newlib) has a start address (`_start`) at 0x00010074.
   Assume that the proxy kernel is loaded at 0x8000000 and simply gets the start
  address from the .elf file.

## Some Links

 * Online assembler: <https://riscvasm.lucasteske.dev/>
 * Carlos' assmebler: https://github.com/carlosedp/riscvassembler


   