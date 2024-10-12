#
# Memory tests
#
    li x1, 0x40
    li x2, 0x12345678
    sw x2, 4(x1)
    lw x3, 4(x1)
    bne x2, x3, fail

    li x2, 0xdeadbeef
    li x1, 0x50
    sw x2, 8(x1)
    lw x3, 8(x1)
    bne x2, x3, fail

pass:
    addi x28, x0, 1
	ecall
1:  beq   x0, x0, 1b

fail:
    addi x28, x0, 2
	ecall
1:  beq   x0, x0, 1b
    nop


