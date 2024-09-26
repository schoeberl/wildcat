# 1 "rv32ui/lhu.S"
# 1 "<built-in>" 1
# 1 "rv32ui/lhu.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # lhu.S
 #-----------------------------------------------------------------------------

 # Test lhu instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/lhu.S" 2
# 1 "rv32ui/test_macros.h" 1






 #-----------------------------------------------------------------------
 # Helper macros
 #-----------------------------------------------------------------------
# 18 "rv32ui/test_macros.h"
 # We use a macro hack to simpify code generation for various numbers
 # of bubble cycles.
# 34 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # RV64UI MACROS
 #-----------------------------------------------------------------------

 #-----------------------------------------------------------------------
 # Tests for instructions with immediate operand
 #-----------------------------------------------------------------------
# 90 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Tests for vector config instructions
 #-----------------------------------------------------------------------
# 118 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Tests for an instruction with register operands
 #-----------------------------------------------------------------------
# 146 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Tests for an instruction with register-register operands
 #-----------------------------------------------------------------------
# 240 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Test memory instructions
 #-----------------------------------------------------------------------
# 317 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Test branch instructions
 #-----------------------------------------------------------------------
# 402 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Test jump instructions
 #-----------------------------------------------------------------------
# 431 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # RV64UF MACROS
 #-----------------------------------------------------------------------

 #-----------------------------------------------------------------------
 # Tests floating-point instructions
 #-----------------------------------------------------------------------
# 568 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # RV64SV MACROS
 #-----------------------------------------------------------------------







 #-----------------------------------------------------------------------
 # Pass and fail code (assumes test num is in x28)
 #-----------------------------------------------------------------------
# 590 "rv32ui/test_macros.h"
 #-----------------------------------------------------------------------
 # Test data section
 #-----------------------------------------------------------------------
# 12 "rv32ui/lhu.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Basic tests
  #-------------------------------------------------------------

  test_2: la x1, tdat; lhu x3, 0(x1);; li x29, 0x000000ff; li x28, 2; bne x3, x29, fail;;
  test_3: la x1, tdat; lhu x3, 2(x1);; li x29, 0x0000ff00; li x28, 3; bne x3, x29, fail;;
  test_4: la x1, tdat; lhu x3, 4(x1);; li x29, 0x00000ff0; li x28, 4; bne x3, x29, fail;;
  test_5: la x1, tdat; lhu x3, 6(x1);; li x29, 0x0000f00f; li x28, 5; bne x3, x29, fail;;

  # Test with negative offset

  test_6: la x1, tdat4; lhu x3, -6(x1);; li x29, 0x000000ff; li x28, 6; bne x3, x29, fail;;
  test_7: la x1, tdat4; lhu x3, -4(x1);; li x29, 0x0000ff00; li x28, 7; bne x3, x29, fail;;
  test_8: la x1, tdat4; lhu x3, -2(x1);; li x29, 0x00000ff0; li x28, 8; bne x3, x29, fail;;
  test_9: la x1, tdat4; lhu x3, 0(x1);; li x29, 0x0000f00f; li x28, 9; bne x3, x29, fail;;

  # Test with a negative base

  test_10: la x1, tdat; addi x1, x1, -32; lhu x3, 32(x1);; li x29, 0x000000ff; li x28, 10; bne x3, x29, fail;





  # Test with unaligned base

  test_11: la x1, tdat; addi x1, x1, -5; lhu x3, 7(x1);; li x29, 0x0000ff00; li x28, 11; bne x3, x29, fail;





  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_12: li x28, 12; li x4, 0; 1: la x1, tdat2; lhu x3, 2(x1); addi x6, x3, 0; li x29, 0x00000ff0; bne x6, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;;
  test_13: li x28, 13; li x4, 0; 1: la x1, tdat3; lhu x3, 2(x1); nop; addi x6, x3, 0; li x29, 0x0000f00f; bne x6, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;;
  test_14: li x28, 14; li x4, 0; 1: la x1, tdat1; lhu x3, 2(x1); nop; nop; addi x6, x3, 0; li x29, 0x0000ff00; bne x6, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;;

  test_15: li x28, 15; li x4, 0; 1: la x1, tdat2; lhu x3, 2(x1); li x29, 0x00000ff0; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_16: li x28, 16; li x4, 0; 1: la x1, tdat3; nop; lhu x3, 2(x1); li x29, 0x0000f00f; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_17: li x28, 17; li x4, 0; 1: la x1, tdat1; nop; nop; lhu x3, 2(x1); li x29, 0x0000ff00; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;

  #-------------------------------------------------------------
  # Test write-after-write hazard
  #-------------------------------------------------------------

  test_18: la x3, tdat; lhu x2, 0(x3); li x2, 2;; li x29, 2; li x28, 18; bne x2, x29, fail;





  test_19: la x3, tdat; lhu x2, 0(x3); nop; li x2, 2;; li x29, 2; li x28, 19; bne x2, x29, fail;






  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



tdat:
tdat1: .half 0x00ff
tdat2: .half 0xff00
tdat3: .half 0x0ff0
tdat4: .half 0xf00f

.align 4; .global end_signature; end_signature:
