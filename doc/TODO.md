# TODO
 * why does // val pcReg = RegInit(-4.S(32.W).asUInt)  result in failing tests?
 * UART
 * Check if gcc can do RV32E
 * Get the C code compilation better integrated (with a reasonable linker.ld)
 * Get C compiled apps running (more than hello)
 * Update simulation (and C/asm code) for new address mapping and checking for ready
 * Why do I have read and write addresses in the data memory, when only one will happen at a time?
   - Check others
 * Something is fishy with testing, as SingleCycle works (even without branch)
 * Two tests fail with co-simulation when adding stall to fetch (the toggle)
 * width.s should not fail in the ISA simulator
   - string should work as well, do we need a linker script?
   - Work on failing simple tests (Simulator and Wildcat)
 * Memory range checks
 * Aim to have same interface for pipeline and single cycle
   - reuse tests 
 * Forwarding from ALU/memory to address computation is missing.
 * 4/5 stages stall on load use hazard is missing (not covered by the tests)
 * Single cycle is not finished - tests are failing, disabled
 * 3/4/5 load and use as address is wrong (need a forward to the adder)
   - test missing
 * ecall, ebreak
 * csrrw, csrrs, csrrc, csrrwi, csrrsi, csrrci
 * At some point try to run the "real" RISC-V tests (need quite some infra)
 * Traps on not implemented instructions and unaligned access
 * Better names for signals (e.g., for those with a feedback, e.g., RF write)
 * Maybe commit the .elf files for faster tests
 * Maybe add some tracing facility
 * compare code can be optimized (see Tommy's code, or JOP code)
 * Get Rust bare metal running
 * Move selection between register and imm back into decode
   - maybe more of the muxing could be done there
   - probably branch target computation
   - or the whole branching itself
 * ecall
 * Maybe check the .elf files in for faster tests
 * What are the types of immediate values? mostly signed or mostly unsigned?
 * And much more not listed here, e.g. caches, interconnect, newlib, ...
 * Write documentation

## Better Naming

 * wrEna is later called valid

# Docu Start

Branch is currently executed in the EX stage. On a taken branch the
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
