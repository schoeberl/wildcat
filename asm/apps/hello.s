#
# Hello
#
    li      x4, 0xf0000000
    li      x1, 'H'
	sw      x1, 0(x4)
    li      x1, 'e'
	sw      x1, 0(x4)
    li      x1, 'l'
	sw      x1, 0(x4)
    li      x1, 'l'
	sw      x1, 0(x4)
    li      x1, 'o'
	sw      x1, 0(x4)
    li      x1, ' '
	sw      x1, 0(x4)
    li      x1, 'W'
	sw      x1, 0(x4)
    li      x1, 'o'
	sw      x1, 0(x4)
    li      x1, 'r'
	sw      x1, 0(x4)
    li      x1, 'k'
	sw      x1, 0(x4)
    li      x1, 'd'
	sw      x1, 0(x4)
    li      x1, '!'
	sw      x1, 0(x4)
    li      x1, '\n'
	sw      x1, 0(x4)

exit:
    addi x28, x0, 1
	ecall

