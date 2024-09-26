# 1 "rv32ui/sub.S"
# 1 "<built-in>" 1
# 1 "rv32ui/sub.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # sub.S
 #-----------------------------------------------------------------------------

 # Test sub instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/sub.S" 2
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
# 12 "rv32ui/sub.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Arithmetic tests
  #-------------------------------------------------------------

  test_2: li x1, 0x00000000; li x2, 0x00000000; sub x3, x1, x2;; li x29, 0x00000000; li x28, 2; bne x3, x29, fail;;
  test_3: li x1, 0x00000001; li x2, 0x00000001; sub x3, x1, x2;; li x29, 0x00000000; li x28, 3; bne x3, x29, fail;;
  test_4: li x1, 0x00000003; li x2, 0x00000007; sub x3, x1, x2;; li x29, 0xfffffffc; li x28, 4; bne x3, x29, fail;;

  test_5: li x1, 0x00000000; li x2, 0xffff8000; sub x3, x1, x2;; li x29, 0x00008000; li x28, 5; bne x3, x29, fail;;
  test_6: li x1, 0x80000000; li x2, 0x00000000; sub x3, x1, x2;; li x29, 0x80000000; li x28, 6; bne x3, x29, fail;;
  test_7: li x1, 0x80000000; li x2, 0xffff8000; sub x3, x1, x2;; li x29, 0x80008000; li x28, 7; bne x3, x29, fail;;

  test_8: li x1, 0x00000000; li x2, 0x00007fff; sub x3, x1, x2;; li x29, 0xffff8001; li x28, 8; bne x3, x29, fail;;
  test_9: li x1, 0x7fffffff; li x2, 0x00000000; sub x3, x1, x2;; li x29, 0x7fffffff; li x28, 9; bne x3, x29, fail;;
  test_10: li x1, 0x7fffffff; li x2, 0x00007fff; sub x3, x1, x2;; li x29, 0x7fff8000; li x28, 10; bne x3, x29, fail;;

  test_11: li x1, 0x80000000; li x2, 0x00007fff; sub x3, x1, x2;; li x29, 0x7fff8001; li x28, 11; bne x3, x29, fail;;
  test_12: li x1, 0x7fffffff; li x2, 0xffff8000; sub x3, x1, x2;; li x29, 0x80007fff; li x28, 12; bne x3, x29, fail;;

  test_13: li x1, 0x00000000; li x2, 0xffffffff; sub x3, x1, x2;; li x29, 0x00000001; li x28, 13; bne x3, x29, fail;;
  test_14: li x1, 0xffffffff; li x2, 0x00000001; sub x3, x1, x2;; li x29, 0xfffffffe; li x28, 14; bne x3, x29, fail;;
  test_15: li x1, 0xffffffff; li x2, 0xffffffff; sub x3, x1, x2;; li x29, 0x00000000; li x28, 15; bne x3, x29, fail;;

  #-------------------------------------------------------------
  # Source/Destination tests
  #-------------------------------------------------------------

  test_16: li x1, 13; li x2, 11; sub x1, x1, x2;; li x29, 2; li x28, 16; bne x1, x29, fail;;
  test_17: li x1, 14; li x2, 11; sub x2, x1, x2;; li x29, 3; li x28, 17; bne x2, x29, fail;;
  test_18: li x1, 13; sub x1, x1, x1;; li x29, 0; li x28, 18; bne x1, x29, fail;;

  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_19: li x4, 0; 1: li x1, 13; li x2, 11; sub x3, x1, x2; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 2; li x28, 19; bne x6, x29, fail;;
  test_20: li x4, 0; 1: li x1, 14; li x2, 11; sub x3, x1, x2; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 3; li x28, 20; bne x6, x29, fail;;
  test_21: li x4, 0; 1: li x1, 15; li x2, 11; sub x3, x1, x2; nop; nop; addi x6, x3, 0; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 4; li x28, 21; bne x6, x29, fail;;

  test_22: li x4, 0; 1: li x1, 13; li x2, 11; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 2; li x28, 22; bne x3, x29, fail;;
  test_23: li x4, 0; 1: li x1, 14; li x2, 11; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 3; li x28, 23; bne x3, x29, fail;;
  test_24: li x4, 0; 1: li x1, 15; li x2, 11; nop; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 4; li x28, 24; bne x3, x29, fail;;
  test_25: li x4, 0; 1: li x1, 13; nop; li x2, 11; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 2; li x28, 25; bne x3, x29, fail;;
  test_26: li x4, 0; 1: li x1, 14; nop; li x2, 11; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 3; li x28, 26; bne x3, x29, fail;;
  test_27: li x4, 0; 1: li x1, 15; nop; nop; li x2, 11; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 4; li x28, 27; bne x3, x29, fail;;

  test_28: li x4, 0; 1: li x2, 11; li x1, 13; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 2; li x28, 28; bne x3, x29, fail;;
  test_29: li x4, 0; 1: li x2, 11; li x1, 14; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 3; li x28, 29; bne x3, x29, fail;;
  test_30: li x4, 0; 1: li x2, 11; li x1, 15; nop; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 4; li x28, 30; bne x3, x29, fail;;
  test_31: li x4, 0; 1: li x2, 11; nop; li x1, 13; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 2; li x28, 31; bne x3, x29, fail;;
  test_32: li x4, 0; 1: li x2, 11; nop; li x1, 14; nop; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 3; li x28, 32; bne x3, x29, fail;;
  test_33: li x4, 0; 1: li x2, 11; nop; nop; li x1, 15; sub x3, x1, x2; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b; li x29, 4; li x28, 33; bne x3, x29, fail;;

  test_34: li x1, -15; sub x2, x0, x1;; li x29, 15; li x28, 34; bne x2, x29, fail;;
  test_35: li x1, 32; sub x2, x1, x0;; li x29, 32; li x28, 35; bne x2, x29, fail;;
  test_36: sub x1, x0, x0;; li x29, 0; li x28, 36; bne x1, x29, fail;;
  test_37: li x1, 16; li x2, 30; sub x0, x1, x2;; li x29, 0; li x28, 37; bne x0, x29, fail;;

  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



.align 4; .global end_signature; end_signature:
