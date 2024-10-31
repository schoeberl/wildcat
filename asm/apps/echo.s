#
# Echo
#
start:
    li      x4, 0xf0000000

# avoid this due to we receive one character on configuration
#    li      x2, ':'
#    jal     put_char

loop:
    jal     get_char
    jal     put_char
    j       loop

exit:
    addi x28, x0, 1
	ecall
1:  beq   x0, x0, 1b

get_char:
    lw      x3, 0(x4)
    nop
    andi    x3, x3, 2
    nop
    beqz    x3, get_char
    nop
    lw      x2, 4(x4)
    ret 

put_char:
    lw      x3, 0(x4)
    nop
    andi    x3, x3, 1
    nop
    beqz    x3, put_char
    nop
    sw      x2, 4(x4)
    ret
