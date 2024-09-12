#
# Blink an LED
#
	addi	x1, x0, 0x12
	sw      x1, 0(x0)
    li      x1, 0x0
    sw      x1, 0(x0)
