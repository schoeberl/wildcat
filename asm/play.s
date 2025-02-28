#
# Play ground
#
	rdcycle	x1
	rdtime	x1
	rdcycle	x1
	rdtime	x1
	rdcycle	x1
	rdtime	x1

    addi a0, x0, 1
	csrr a0, marchid
	addi a0, a0, -47 # marchid of Wildcat is 47
	# simulator needs adaption
	li a0, 0

# notify success to the simulator
    ecall
1:  beq   x0, x0, 1b
