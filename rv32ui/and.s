# 1 "rv32ui/and.S"
# 1 "<built-in>" 1
# 1 "rv32ui/and.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # and.S
 #-----------------------------------------------------------------------------

 # Test and instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/and.S" 2
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
# 12 "rv32ui/and.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Logical tests
  #-------------------------------------------------------------

  test_2: li x1, 0xff00ff00; li x2, 0x0f0f0f0f; and x3, x1, x2;; li x29, 0x0f000f00; li x28, 2; bne x3, x29, fail;;
  test_3: li x1, 0x0ff00ff0; li x2, 0xf0f0f0f0; and x3, x1, x2;; li x29, 0x00f000f0; li x28, 3; bne x3, x29, fail;;
  test_4: li x1, 0x00ff00ff; li x2, 0x0f0f0f0f; and x3, x1, x2;; li x29, 0x000f000f; li x28, 4; bne x3, x29, fail;;
  test_5: li x1, 0xf00ff00f; li x2, 0xf0f0f0f0; and x3, x1, x2;; li x29, 0xf000f000; li x28, 5; bne x3, x29, fail;;

  #-------------------------------------------------------------
  # Source/Destination tests
  #-------------------------------------------------------------

  test_6: li x1, 0xff00ff00; li x2, 0x0f0f0f0f; and x1, x1, x2;; li x29, 0x0f000f00; li x28, 6; bne x1, x29, fail;;
  test_7: li x1, 0x0ff00ff0; li x2, 0xf0f0f0f0; and x2, x1, x2;; li x29, 0x00f000f0; li x28, 7; bne x2, x29, fail;;
  test_8: li x1, 0xff00ff00; and x1, x1, x1;; li x29, 0xff00ff00; li x28, 8; bne x1, x29, fail;;

  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_9: li x4, 0; 1: li x1, 0xff00ff00; li x2, 0x0f0f0f0f; and x3, x1, x2; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x0f000f00; li x28, 9; bne x6, x29, fail;;
  test_10: li x4, 0; 1: li x1, 0x0ff00ff0; li x2, 0xf0f0f0f0; and x3, x1, x2; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00f000f0; li x28, 10; bne x6, x29, fail;;
  test_11: li x4, 0; 1: li x1, 0x00ff00ff; li x2, 0x0f0f0f0f; and x3, x1, x2; nop; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x000f000f; li x28, 11; bne x6, x29, fail;;

  test_12: li x4, 0; 1: li x1, 0xff00ff00; li x2, 0x0f0f0f0f; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x0f000f00; li x28, 12; bne x3, x29, fail;;
  test_13: li x4, 0; 1: li x1, 0x0ff00ff0; li x2, 0xf0f0f0f0; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00f000f0; li x28, 13; bne x3, x29, fail;;
  test_14: li x4, 0; 1: li x1, 0x00ff00ff; li x2, 0x0f0f0f0f; nop; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x000f000f; li x28, 14; bne x3, x29, fail;;
  test_15: li x4, 0; 1: li x1, 0xff00ff00; nop; li x2, 0x0f0f0f0f; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x0f000f00; li x28, 15; bne x3, x29, fail;;
  test_16: li x4, 0; 1: li x1, 0x0ff00ff0; nop; li x2, 0xf0f0f0f0; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00f000f0; li x28, 16; bne x3, x29, fail;;
  test_17: li x4, 0; 1: li x1, 0x00ff00ff; nop; nop; li x2, 0x0f0f0f0f; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x000f000f; li x28, 17; bne x3, x29, fail;;

  test_18: li x4, 0; 1: li x2, 0x0f0f0f0f; li x1, 0xff00ff00; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x0f000f00; li x28, 18; bne x3, x29, fail;;
  test_19: li x4, 0; 1: li x2, 0xf0f0f0f0; li x1, 0x0ff00ff0; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00f000f0; li x28, 19; bne x3, x29, fail;;
  test_20: li x4, 0; 1: li x2, 0x0f0f0f0f; li x1, 0x00ff00ff; nop; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x000f000f; li x28, 20; bne x3, x29, fail;;
  test_21: li x4, 0; 1: li x2, 0x0f0f0f0f; nop; li x1, 0xff00ff00; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x0f000f00; li x28, 21; bne x3, x29, fail;;
  test_22: li x4, 0; 1: li x2, 0xf0f0f0f0; nop; li x1, 0x0ff00ff0; nop; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00f000f0; li x28, 22; bne x3, x29, fail;;
  test_23: li x4, 0; 1: li x2, 0x0f0f0f0f; nop; nop; li x1, 0x00ff00ff; and x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x000f000f; li x28, 23; bne x3, x29, fail;;

  test_24: li x1, 0xff00ff00; and x2, x0, x1;; li x29, 0; li x28, 24; bne x2, x29, fail;;
  test_25: li x1, 0x00ff00ff; and x2, x1, x0;; li x29, 0; li x28, 25; bne x2, x29, fail;;
  test_26: and x1, x0, x0;; li x29, 0; li x28, 26; bne x1, x29, fail;;
  test_27: li x1, 0x11111111; li x2, 0x22222222; and x0, x1, x2;; li x29, 0; li x28, 27; bne x0, x29, fail;;

  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



.align 4; .global end_signature; end_signature:
