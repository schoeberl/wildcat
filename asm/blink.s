#
# Blink an LED
#
    li      x2, 0
begin:
    li      x1, 0x7ff
loop:
# TODO: some forwarding issues

    nop
    li      x3, 0x7ff
    nop
inner:
	addi	x3, x3, -1
	nop
	bnez	x3, inner
    nop
	addi	x1, x1, -1
	nop
	bnez	x1, loop
	sw      x2, 0(x0)
    addi    x2, x2, 1
#    j       begin
    beqz      x0, begin
