# TODO

 * Check ALU with the decoding
 * Get a testing setup for the simulator
 * Get a testing setup for the hardware implementation
 * Rescue RISC-V test from my local copy of sodor in data/repositories
   * Can be found in the inital commit of FlexPRET
   * also test from https://github.com/mortbopet/Ripes (same as the above?)
   * Look into YARVI for tests, they are committed
   * And the student project from CAE: where did he get the tests from?
   * Also the tests on Helena
   * They are also at: https://github.com/YosysHQ/picorv32/tree/master/tests
   * Build see: https://github.com/YosysHQ/picorv32/blob/f00a88c36eaab478b64ee27d8162e421049bcc66/Makefile#L121
 * maybe do also: RegInit(0.U.asTypeOf(new IF_ID))
 * Throw an exception when simulator test fails
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
