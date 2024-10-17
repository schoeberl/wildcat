#
# Memory tests
#
    li x1, 0x40
    li x2, 0x12345678
    sw x2, 4(x1)

    li x3, 0x0
    sb x3, 5(x1)
    lw x3, 4(x1)
    li x4, 0x12340078
    bne x4, x3, fail

pass:
    addi x28, x0, 1
	ecall
1:  beq   x0, x0, 1b

fail:
    addi x28, x0, 2
	ecall
1:  beq   x0, x0, 1b
    nop


