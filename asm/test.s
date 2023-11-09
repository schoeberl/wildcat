#
# Very simple assembler statements to get started
#


	addi	x1, x0, 0x111
	addi	x2, x0, 0x222
# missing forwarding
	nop
	nop
	nop
	add	x3, x1, x2
	addi	x1, x0, 0x0f
	addi	x2, x0, 0xfc
	or	x3, x1, x2
	and	x3, x1, x2
	addi x28, x3, -11

# notify success to the simulator
    ecall
1:  beq   x0, x0, 1b
