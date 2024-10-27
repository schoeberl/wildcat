#
# Hello
#
start:
    li      x4, 0xf0000000

    li      x2, 'H'
    jal     put_char
    li      x2, 'e'
    jal     put_char
    li      x2, 'l'
    jal     put_char
    li      x2, 'l'
    jal     put_char
    li      x2, 'o'
    jal     put_char
    li      x2, ' '
    jal     put_char
    li      x2, 'W'
    jal     put_char
    li      x2, 'o'
    jal     put_char
    li      x2, 'r'
    jal     put_char
    li      x2, 'l'
    jal     put_char
    li      x2, 'd'
    jal     put_char
    li      x2, '!'
    jal     put_char
    li      x2, '\r'
    jal     put_char
    li      x2, '\n'
    jal     put_char
exit:
    addi x28, x0, 1
	ecall
1:  beq   x0, x0, 1b


put_char:
    lw      x3, 0(x4)
    andi    x3, x3, 1
    beqz    x3, put_char
    sw      x2, 4(x4)
    ret