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
 * Current ISA tests use a0 and ecall for pass fail, including a failure number.
   A value of 1 is pass, fail is != 1 with the test number multiplied by 2.
   Probably spike implements this semantics.
 * What else in ecall is implemented in spike?

## Memory Layout


   