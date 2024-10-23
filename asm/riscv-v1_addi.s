#*****************************************************************************
# riscv-v1_addi.S
#-----------------------------------------------------------------------------
#
#  Test addi instruction
#

        # Test 1: 1 + 1 = 2

        addi x1, x0, 1
        addi x2, x1, 1
        addi x3, x0, 2
        bne   x3, x2, fail

        # Test 2: 0x0ff + 0x001 = 0x100

        addi x1, x0, 0x0ff
        addi x2, x1, 0x001
        addi x3, x0, 0x100
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

