#
# Blink an LED
#
# When ignoring the address decoding, this blinks an LED with just 3 instructions: addi, sw, and bnez.
#
    li      x4, 0xf0010000
    li      x3, 1
    sw      x3, 0(x4)

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
    li      x1, 1
    bnez    x1, begin
