# 1 "rv32ui/sw.S"
# 1 "<built-in>" 1
# 1 "rv32ui/sw.S" 2
 # See LICENSE for license details.

 #*****************************************************************************
 # sw.S
 #-----------------------------------------------------------------------------

 # Test sw instruction.


# 1 "rv32ui/riscv_test.h" 1
# 11 "rv32ui/sw.S" 2
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
# 12 "rv32ui/sw.S" 2

.macro init; .endm
.text; .align 6; 1:

  #-------------------------------------------------------------
  # Basic tests
  #-------------------------------------------------------------

  test_2: la x1, tdat; li x2, 0x00aa00aa; sw x2, 0(x1); lw x3, 0(x1);; li x29, 0x00aa00aa; li x28, 2; bne x3, x29, fail;;
  test_3: la x1, tdat; li x2, 0xaa00aa00; sw x2, 4(x1); lw x3, 4(x1);; li x29, 0xaa00aa00; li x28, 3; bne x3, x29, fail;;
  test_4: la x1, tdat; li x2, 0x0aa00aa0; sw x2, 8(x1); lw x3, 8(x1);; li x29, 0x0aa00aa0; li x28, 4; bne x3, x29, fail;;
  test_5: la x1, tdat; li x2, 0xa00aa00a; sw x2, 12(x1); lw x3, 12(x1);; li x29, 0xa00aa00a; li x28, 5; bne x3, x29, fail;;

  # Test with negative offset

  test_6: la x1, tdat8; li x2, 0x00aa00aa; sw x2, -12(x1); lw x3, -12(x1);; li x29, 0x00aa00aa; li x28, 6; bne x3, x29, fail;;
  test_7: la x1, tdat8; li x2, 0xaa00aa00; sw x2, -8(x1); lw x3, -8(x1);; li x29, 0xaa00aa00; li x28, 7; bne x3, x29, fail;;
  test_8: la x1, tdat8; li x2, 0x0aa00aa0; sw x2, -4(x1); lw x3, -4(x1);; li x29, 0x0aa00aa0; li x28, 8; bne x3, x29, fail;;
  test_9: la x1, tdat8; li x2, 0xa00aa00a; sw x2, 0(x1); lw x3, 0(x1);; li x29, 0xa00aa00a; li x28, 9; bne x3, x29, fail;;

  # Test with a negative base

  test_10: la x1, tdat9; li x2, 0x12345678; addi x4, x1, -32; sw x2, 32(x4); lw x3, 0(x1);; li x29, 0x12345678; li x28, 10; bne x3, x29, fail;







  # Test with unaligned base

  test_11: la x1, tdat9; li x2, 0x58213098; addi x1, x1, -3; sw x2, 7(x1); la x4, tdat10; lw x3, 0(x4);; li x29, 0x58213098; li x28, 11; bne x3, x29, fail;
# 53 "rv32ui/sw.S"
  #-------------------------------------------------------------
  # Bypassing tests
  #-------------------------------------------------------------

  test_12: li x28, 12; li x4, 0; 1: la x1, 0xaabbccdd; la x2, tdat; sw x1, 0(x2); lw x3, 0(x2); li x29, 0xaabbccdd; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_13: li x28, 13; li x4, 0; 1: la x1, 0xdaabbccd; la x2, tdat; nop; sw x1, 4(x2); lw x3, 4(x2); li x29, 0xdaabbccd; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_14: li x28, 14; li x4, 0; 1: la x1, 0xddaabbcc; la x2, tdat; nop; nop; sw x1, 8(x2); lw x3, 8(x2); li x29, 0xddaabbcc; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_15: li x28, 15; li x4, 0; 1: la x1, 0xcddaabbc; nop; la x2, tdat; sw x1, 12(x2); lw x3, 12(x2); li x29, 0xcddaabbc; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_16: li x28, 16; li x4, 0; 1: la x1, 0xccddaabb; nop; la x2, tdat; nop; sw x1, 16(x2); lw x3, 16(x2); li x29, 0xccddaabb; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_17: li x28, 17; li x4, 0; 1: la x1, 0xbccddaab; nop; nop; la x2, tdat; sw x1, 20(x2); lw x3, 20(x2); li x29, 0xbccddaab; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;

  test_18: li x28, 18; li x4, 0; 1: la x2, tdat; la x1, 0x00112233; sw x1, 0(x2); lw x3, 0(x2); li x29, 0x00112233; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_19: li x28, 19; li x4, 0; 1: la x2, tdat; la x1, 0x30011223; nop; sw x1, 4(x2); lw x3, 4(x2); li x29, 0x30011223; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_20: li x28, 20; li x4, 0; 1: la x2, tdat; la x1, 0x33001122; nop; nop; sw x1, 8(x2); lw x3, 8(x2); li x29, 0x33001122; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_21: li x28, 21; li x4, 0; 1: la x2, tdat; nop; la x1, 0x23300112; sw x1, 12(x2); lw x3, 12(x2); li x29, 0x23300112; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_22: li x28, 22; li x4, 0; 1: la x2, tdat; nop; la x1, 0x22330011; nop; sw x1, 16(x2); lw x3, 16(x2); li x29, 0x22330011; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;
  test_23: li x28, 23; li x4, 0; 1: la x2, tdat; nop; nop; la x1, 0x12233001; sw x1, 20(x2); lw x3, 20(x2); li x29, 0x12233001; bne x3, x29, fail; addi x4, x4, 1; li x5, 2; bne x4, x5, 1b;

  bne x0, x28, pass; fail: 1: beqz x28, 1b; sll x28, x28, 1; or x28, x28, 1; j ecall; pass: li x28, 1; j ecall

ecall: ecall; j ecall

  .data
 .align 4; .global begin_signature; begin_signature:



tdat:
tdat1: .word 0xdeadbeef
tdat2: .word 0xdeadbeef
tdat3: .word 0xdeadbeef
tdat4: .word 0xdeadbeef
tdat5: .word 0xdeadbeef
tdat6: .word 0xdeadbeef
tdat7: .word 0xdeadbeef
tdat8: .word 0xdeadbeef
tdat9: .word 0xdeadbeef
tdat10: .word 0xdeadbeef

.align 4; .global end_signature; end_signature:
