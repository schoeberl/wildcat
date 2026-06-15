#
# Play ground
#
	li	x1, 4
	li  x2, 2
loop:
	addi x2, x2, 1
    addi x1, x1, -1
    bnez x1, loop

    addi a0, x0, 0

# notify success to the simulator
    ecall
1:  beq   x0, x0, 1b
