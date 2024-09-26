# 1 "rv32ui/sll.S"
# 1 "<built-in>" 1
# 1 "rv32ui/sll.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # sll.S
 #-----------------------------------------------------------------------------

 # Test sll instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/sll.S" 2
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
# 12 "rv32ui/sll.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Arithmetic tests
  #-------------------------------------------------------------

  test_2: li x1, 0x00000001; li x2, 0; sll x3, x1, x2;; li x29, 0x00000001; li x28, 2; bne x3, x29, fail;;
  test_3: li x1, 0x00000001; li x2, 1; sll x3, x1, x2;; li x29, 0x00000002; li x28, 3; bne x3, x29, fail;;
  test_4: li x1, 0x00000001; li x2, 7; sll x3, x1, x2;; li x29, 0x00000080; li x28, 4; bne x3, x29, fail;;
  test_5: li x1, 0x00000001; li x2, 14; sll x3, x1, x2;; li x29, 0x00004000; li x28, 5; bne x3, x29, fail;;
  test_6: li x1, 0x00000001; li x2, 31; sll x3, x1, x2;; li x29, 0x80000000; li x28, 6; bne x3, x29, fail;;

  test_7: li x1, 0xffffffff; li x2, 0; sll x3, x1, x2;; li x29, 0xffffffff; li x28, 7; bne x3, x29, fail;;
  test_8: li x1, 0xffffffff; li x2, 1; sll x3, x1, x2;; li x29, 0xfffffffe; li x28, 8; bne x3, x29, fail;;
  test_9: li x1, 0xffffffff; li x2, 7; sll x3, x1, x2;; li x29, 0xffffff80; li x28, 9; bne x3, x29, fail;;
  test_10: li x1, 0xffffffff; li x2, 14; sll x3, x1, x2;; li x29, 0xffffc000; li x28, 10; bne x3, x29, fail;;
  test_11: li x1, 0xffffffff; li x2, 31; sll x3, x1, x2;; li x29, 0x80000000; li x28, 11; bne x3, x29, fail;;

  test_12: li x1, 0x21212121; li x2, 0; sll x3, x1, x2;; li x29, 0x21212121; li x28, 12; bne x3, x29, fail;;
  test_13: li x1, 0x21212121; li x2, 1; sll x3, x1, x2;; li x29, 0x42424242; li x28, 13; bne x3, x29, fail;;
  test_14: li x1, 0x21212121; li x2, 7; sll x3, x1, x2;; li x29, 0x90909080; li x28, 14; bne x3, x29, fail;;
  test_15: li x1, 0x21212121; li x2, 14; sll x3, x1, x2;; li x29, 0x48484000; li x28, 15; bne x3, x29, fail;;
  test_16: li x1, 0x21212121; li x2, 31; sll x3, x1, x2;; li x29, 0x80000000; li x28, 16; bne x3, x29, fail;;

  # Verify that shifts only use bottom five bits

  test_17: li x1, 0x21212121; li x2, 0xffffffe0; sll x3, x1, x2;; li x29, 0x21212121; li x28, 17; bne x3, x29, fail;;
  test_18: li x1, 0x21212121; li x2, 0xffffffe1; sll x3, x1, x2;; li x29, 0x42424242; li x28, 18; bne x3, x29, fail;;
  test_19: li x1, 0x21212121; li x2, 0xffffffe7; sll x3, x1, x2;; li x29, 0x90909080; li x28, 19; bne x3, x29, fail;;
  test_20: li x1, 0x21212121; li x2, 0xffffffee; sll x3, x1, x2;; li x29, 0x48484000; li x28, 20; bne x3, x29, fail;;
  test_21: li x1, 0x21212120; li x2, 0xffffffff; sll x3, x1, x2;; li x29, 0x00000000; li x28, 21; bne x3, x29, fail;;

  #-------------------------------------------------------------
  # Source/Destination tests
  #-------------------------------------------------------------

  test_22: li x1, 0x00000001; li x2, 7; sll x1, x1, x2;; li x29, 0x00000080; li x28, 22; bne x1, x29, fail;;
  test_23: li x1, 0x00000001; li x2, 14; sll x2, x1, x2;; li x29, 0x00004000; li x28, 23; bne x2, x29, fail;;
  test_24: li x1, 3; sll x1, x1, x1;; li x29, 24; li x28, 24; bne x1, x29, fail;;

  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_25: li x4, 0; 1: li x1, 0x00000001; li x2, 7; sll x3, x1, x2; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00000080; li x28, 25; bne x6, x29, fail;;
  test_26: li x4, 0; 1: li x1, 0x00000001; li x2, 14; sll x3, x1, x2; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00004000; li x28, 26; bne x6, x29, fail;;
  test_27: li x4, 0; 1: li x1, 0x00000001; li x2, 31; sll x3, x1, x2; nop; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x80000000; li x28, 27; bne x6, x29, fail;;

  test_28: li x4, 0; 1: li x1, 0x00000001; li x2, 7; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00000080; li x28, 28; bne x3, x29, fail;;
  test_29: li x4, 0; 1: li x1, 0x00000001; li x2, 14; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00004000; li x28, 29; bne x3, x29, fail;;
  test_30: li x4, 0; 1: li x1, 0x00000001; li x2, 31; nop; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x80000000; li x28, 30; bne x3, x29, fail;;
  test_31: li x4, 0; 1: li x1, 0x00000001; nop; li x2, 7; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00000080; li x28, 31; bne x3, x29, fail;;
  test_32: li x4, 0; 1: li x1, 0x00000001; nop; li x2, 14; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00004000; li x28, 32; bne x3, x29, fail;;
  test_33: li x4, 0; 1: li x1, 0x00000001; nop; nop; li x2, 31; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x80000000; li x28, 33; bne x3, x29, fail;;

  test_34: li x4, 0; 1: li x2, 7; li x1, 0x00000001; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00000080; li x28, 34; bne x3, x29, fail;;
  test_35: li x4, 0; 1: li x2, 14; li x1, 0x00000001; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00004000; li x28, 35; bne x3, x29, fail;;
  test_36: li x4, 0; 1: li x2, 31; li x1, 0x00000001; nop; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x80000000; li x28, 36; bne x3, x29, fail;;
  test_37: li x4, 0; 1: li x2, 7; nop; li x1, 0x00000001; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00000080; li x28, 37; bne x3, x29, fail;;
  test_38: li x4, 0; 1: li x2, 14; nop; li x1, 0x00000001; nop; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x00004000; li x28, 38; bne x3, x29, fail;;
  test_39: li x4, 0; 1: li x2, 31; nop; nop; li x1, 0x00000001; sll x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 0x80000000; li x28, 39; bne x3, x29, fail;;

  test_40: li x1, 15; sll x2, x0, x1;; li x29, 0; li x28, 40; bne x2, x29, fail;;
  test_41: li x1, 32; sll x2, x1, x0;; li x29, 32; li x28, 41; bne x2, x29, fail;;
  test_42: sll x1, x0, x0;; li x29, 0; li x28, 42; bne x1, x29, fail;;
  test_43: li x1, 1024; li x2, 2048; sll x0, x1, x2;; li x29, 0; li x28, 43; bne x0, x29, fail;;

  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



.align 4; .global end_signature; end_signature:
