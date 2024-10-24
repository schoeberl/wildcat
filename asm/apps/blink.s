#
# Blink an LED
#
    li      x4, 0xf0010000
    li      x3, 0
begin:
    li      x1, 0x7ff
loop:
    li      x2, 0x7ff
inner:
	addi	x2, x2, -1
	bnez	x2, inner
	addi	x1, x1, -1
	bnez	x1, loop
	sw      x3, 0(x4)
    addi    x3, x3, 1
    beqz      x0, begin
