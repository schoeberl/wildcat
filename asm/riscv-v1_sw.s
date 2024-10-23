#*****************************************************************************
# riscv-v1_sw.S
#-----------------------------------------------------------------------------
#
# Test sw instruction.

        # Test 1: Store then load some data

        la x1, tdat

        addi x2, x0, 0x0ff
        sw    x2, 0(x1)
        lw    x3, 0(x1)
        bne   x3, x2, fail

        addi x2, x0, 0x7f0
        sw    x2, 4(x1)
        lw    x3, 4(x1)
        bne   x3, x2, fail

        addi x2, x0, 0x70f
        sw    x2, 12(x1)
        lw    x3, 12(x1)
        bne   x3, x2, fail

        # Test 2: Store then load some data (negative offsets)

        la x1, tdat8

        addi x2, x0, 0x0ff
        sw    x2, -12(x1)
        lw    x3, -12(x1)
        bne   x3, x2, fail

        addi x2, x0, 0x7f0
        sw    x2, -8(x1)
        lw    x3, -8(x1)
        bne   x3, x2, fail

        addi x2, x0, 0x70f
        sw    x2, 0(x1)
        lw    x3, 0(x1)
        bne   x3, x2, fail

        # If we get here then we passed

        addi a0, x0, 0
        beq   x0, x0, pass

fail:
        addi a0, x0, 2
	ecall
1:      beq   x0, x0, 1b

pass:
	ecall
1:      beq   x0, x0, 1b

        #-------------------------------------------------------------
        # Test data
        #-------------------------------------------------------------

        .data
tdat:
tdat1:  .word 0xdeadbeef
tdat2:  .word 0xdeadbeef
tdat3:  .word 0xdeadbeef
tdat4:  .word 0xdeadbeef
tdat5:  .word 0xdeadbeef
tdat6:  .word 0xdeadbeef
tdat7:  .word 0xdeadbeef
tdat8:  .word 0xdeadbeef
