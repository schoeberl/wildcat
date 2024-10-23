#*****************************************************************************
# riscv-v1_bne.S
#-----------------------------------------------------------------------------
#
#  Test bne instruction
#

        # Test 1: Taken?

        addi x1, x0, 1
        addi x2, x0, 2
        bne   x1, x2, 1f
        bne   x0, x1, fail
1:

        # Test 2: Not taken?

        addi x1, x0, 1
        addi x2, x0, 1
        bne   x1, x2, fail

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
