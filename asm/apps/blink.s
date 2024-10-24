#
# Blink an LED
#
    li      x4, 0xf0010000
begin:
    li      x1, 0x7ff
loop:
    li      x2, 0x7ff
inner:
	addi	x2, x2, -1
	bnez	x2, inner
	addi	x1, x1, -1
	bnez	x1, loop
	sw      x4, 0(x0)
    addi    x4, x4, 1
#    j       begin
    beqz      x0, begin
