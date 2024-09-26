# 1 "rv32ui/bne.S"
# 1 "<built-in>" 1
# 1 "rv32ui/bne.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # bne.S
 #-----------------------------------------------------------------------------

 # Test bne instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/bne.S" 2
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
# 12 "rv32ui/bne.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Branch tests
  #-------------------------------------------------------------

  # Each test checks both forward and backward branches

  test_2: li x28, 2; li x1, 0; li x2, 1; bne x1, x2, 2f; bne x0, x28, fail; 1: bne x0, x28, 3f; 2: bne x1, x2, 1b; bne x0, x28, fail; 3:;
  test_3: li x28, 3; li x1, 1; li x2, 0; bne x1, x2, 2f; bne x0, x28, fail; 1: bne x0, x28, 3f; 2: bne x1, x2, 1b; bne x0, x28, fail; 3:;
  test_4: li x28, 4; li x1, -1; li x2, 1; bne x1, x2, 2f; bne x0, x28, fail; 1: bne x0, x28, 3f; 2: bne x1, x2, 1b; bne x0, x28, fail; 3:;
  test_5: li x28, 5; li x1, 1; li x2, -1; bne x1, x2, 2f; bne x0, x28, fail; 1: bne x0, x28, 3f; 2: bne x1, x2, 1b; bne x0, x28, fail; 3:;

  test_6: li x28, 6; li x1, 0; li x2, 0; bne x1, x2, 1f; bne x0, x28, 2f; 1: bne x0, x28, fail; 2: bne x1, x2, 1b; 3:;
  test_7: li x28, 7; li x1, 1; li x2, 1; bne x1, x2, 1f; bne x0, x28, 2f; 1: bne x0, x28, fail; 2: bne x1, x2, 1b; 3:;
  test_8: li x28, 8; li x1, -1; li x2, -1; bne x1, x2, 1f; bne x0, x28, 2f; 1: bne x0, x28, fail; 2: bne x1, x2, 1b; 3:;

  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_9: li x28, 9; li x4, 0; 1: li x1, 0; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_10: li x28, 10; li x4, 0; 1: li x1, 0; li x2, 0; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_11: li x28, 11; li x4, 0; 1: li x1, 0; li x2, 0; nop; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_12: li x28, 12; li x4, 0; 1: li x1, 0; nop; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_13: li x28, 13; li x4, 0; 1: li x1, 0; nop; li x2, 0; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_14: li x28, 14; li x4, 0; 1: li x1, 0; nop; nop; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;

  test_15: li x28, 15; li x4, 0; 1: li x1, 0; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_16: li x28, 16; li x4, 0; 1: li x1, 0; li x2, 0; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_17: li x28, 17; li x4, 0; 1: li x1, 0; li x2, 0; nop; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_18: li x28, 18; li x4, 0; 1: li x1, 0; nop; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_19: li x28, 19; li x4, 0; 1: li x1, 0; nop; li x2, 0; nop; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_20: li x28, 20; li x4, 0; 1: li x1, 0; nop; nop; li x2, 0; bne x1, x2, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;

  #-------------------------------------------------------------
  # Test delay slot instructions not executed nor bypassed
  #-------------------------------------------------------------

  test_21: li x1, 1; bne x1, x0, 1f; addi x1, x1, 1; addi x1, x1, 1; addi x1, x1, 1; addi x1, x1, 1; 1: addi x1, x1, 1; addi x1, x1, 1;; li x29, 3; li x28, 21; bne x1, x29, fail;
# 64 "rv32ui/bne.S"
  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



.align 4; .global end_signature; end_signature:
