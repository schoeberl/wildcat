#
# Blink an LED
#
    li      x2, 0
begin:
    li      x1, 0x7ff
loop:
    li      x3, 0x7ff
inner:
	addi	x3, x3, -1
	bnez	x3, inner
	addi	x1, x1, -1
	bnez	x1, loop
	sw      x2, 0(x0)
    addi    x2, x2, 1
#    j       begin
    beqz      x0, begin
