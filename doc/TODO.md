# TODO

 * Work on simulator: read .elf file and put stuff into right memory places
   - should be able to run the standard tests
 * For load tests: simply load binary into imem and dmem
 * have isBranch in decode (not in core)
 * Should fail when not given a program (no default to silly program)
 * Something is fishy with testing, as SingleCycle works (even without branch)
 * width.s should not fail in the ISA simulator
   - string should work as well, do we need a linker script?
 * Simulator should also be tested with the standard tests
 * Work on failing simple tests
 * Aim to have same interface for pipeline and single cycle
   - reuse tests 
 * Forwarding from ALU/memory to address computation is missing.
 * 4/5 stages stall on load use hazard is missing (not covered by the tests)
 * 3/4/5 load and use as address is wrong (need a forward to the adder)
   - test missing
 * Later
   - lb, lh, lbu, lhu
   - sb, sh
   - ecall, ebreak
   - csrrw, csrrs, csrrc, csrrwi, csrrsi, csrrci
 * Better names for signals (e.g., for those with a feedback, e.g., RF write)
 * Read .elf files in Scala
 * Maybe commit the .elf files for faster tests
 * Maybe add some tracing facility
 * Get C compiled apps running
 * Get Rust bare metal running
 * Move selection between register and imm back into decode
   - maybe more of the muxing could be done there
   - probably branch target computation
   - or the whole branching itself
 * ecall
 * Use Java to read .elf files
   - maybe check them in for faster tests
 * JALR: The target address is obtained by adding the 12-bit signed I-immediate to the register rs1, then setting the least-significant bit of the result to zero.
   - Maybe this is not needed as the compiler does this already
 * JALR should check for func3 being 0 - it uses this 0 for ALU op
 * What are the types of immediate values? mostly signed or mostly unsigned?
 * Check ALU with the decoding (what did I mean?)
 * And much more not listed here, e.g. caches, interconnect, newlib, ...
 * Write documentation

## Better Naming

 * wrEna is later called valid

# Docu Start

Branch is currently executed in the EX stage.On a taken branch the
FE and DEC stages are flushed by changing ```instrReg``` to ```NOP```
and setting ```decExReg.vald``` to ```false.B ```.

JALR uses the ALU with the immediate similar to ADDI.

# Further Reading
 * A nice single cycle implementation: /Users/martin/data/dtu/teaching/cae/exam2022/Final Assignment ...
 * Checkout: https://github.com/SpinalHDL/VexRiscv (has a lot of info)
 * unaligned access is not too hard with byte-wise access
   * or trap on unaligned access
 * Explore compiler options
 * Explore https://inst.eecs.berkeley.edu/~cs250/fa10/ (for chip design course)
 * https://inst.eecs.berkeley.edu/~cs250/fa10/handouts/tut3-riscv.pdf
 * Checkout https://github.com/ucb-bar/riscv-mini
 * Checkout https://github.com/bobbl/rudolv (Joerg Mische), might be close to Wildcat plan
 * List of RV cores with IDs: https://github.com/riscv/riscv-isa-manual/blob/latex/marchid.md
 * Ibex is 2 stages: https://ibex-core.readthedocs.io/en/latest/03_reference/pipeline_details.html
 * Checkout random instruction generator: https://ibex-core.readthedocs.io/en/latest/03_reference/pipeline_details.html
 * Checkout https://github.com/SpinalHDL/VexiiRiscv
 * Interesting core: https://github.com/darklife/darkriscv
