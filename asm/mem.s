#
# Memory tests
#
    li gp, 1
    li x1, 0x100
    li x2, 0x12345678
    sw x2, 4(x1)
    lw x3, 4(x1)
    bne x2, x3, fail

    li gp, 2
    li x2, 0xdeadbeef
    li x1, 0x110
    sw x2, 8(x1)
    lw t0, 8(x1)
    bne x2, t0, fail

    li gp, 3
    li x2, 0x12345678
    sw x2, (x1)
    li x2, 0x0001234
    beq x0, x0, over
    sw x2, (x1)
over:
    lw t0, (x1)
    beq x2, t0, fail

pass:
    addi a0, x0, 0
	ecall
1:  beq   x0, x0, 1b
    nop

fail:
    addi a0, x0, 2
	ecall
1:  beq   x0, x0, 1b
    nop


