# TODO

 * Check ALU with the decoding
 * Get a testing setup for the simulator
 * Get a testing setup for the hardware implementation
 * Rescue RISC-V test fromm my local copy of sodor in data/repositories
   * also test from https://github.com/mortbopet/Ripes (same os the above?)
   * And the student project from CAE: where did he get the tests from?
   * Also the tests on Helena
   * They are also at: https://github.com/YosysHQ/picorv32/tree/master/tests
   * Build see: https://github.com/YosysHQ/picorv32/blob/f00a88c36eaab478b64ee27d8162e421049bcc66/Makefile#L121
 * maybe do also: RegInit(0.U.asTypeOf(new IF_ID))
 * Checkout: https://github.com/SpinalHDL/VexRiscv (has a lot of info)
 * unaligned access is not too hard with byte-wise access