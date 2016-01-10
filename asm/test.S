#
# Very simple assembler statements to get started
#

	addi	x1, x0, 0x111
	addi	x2, x1, 0x222
	add	x3, x1, x2
	addi	x1, x0, 0x0f
	addi	x2, x0, 0xfc
# the following two are broken
	or	x3, x1, x2
	and	x3, x1, x2
