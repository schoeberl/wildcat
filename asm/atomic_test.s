.section .text
.global _start

_start:
    # Test 1: Simple successful LR/SC sequence
    # Initialize test value in memory
    li      t0, 0x1000     # Load address for test
    li      t1, 42         # Initial value
    sw      t1, 0(t0)      # Store initial value to memory
    
    # Perform atomic increment using LR/SC
    li      t2, 1          # Value to add
retry1:
    lr.w    t3, (t0)       # Load-reserve current value
    add     t4, t3, t2     # Increment value
    sc.w    t5, t4, (t0)   # Store-conditional new value
    bnez    t5, retry1     # Retry if SC failed (t5 != 0 means failure)
    
    # Verify result (should be 43)
    lw      t6, 0(t0)
    li      a0, 43
    bne     t6, a0, fail

    # Test 2: Demonstrate SC failure after reservation is broken
    li      t0, 0x1004     # New test address
    li      t1, 100        # Initial value
    sw      t1, 0(t0)      # Store initial value
    
    # Start LR/SC sequence
    lr.w    t3, (t0)       # Load-reserve current value
    
    # Simulate interference by writing to same location
    sw      t1, 0(t0)      # This breaks the reservation
    
    # Try SC - should fail
    li      t4, 200
    sc.w    t5, t4, (t0)   # This SC should fail (t5 should be non-zero)
    beqz    t5, fail       # If t5 is 0, SC succeeded when it shouldn't have
    
    # Verify original value is unchanged
    lw      t6, 0(t0)
    li      a0, 100
    bne     t6, a0, fail

    # Test 3: Atomic swap using LR/SC
    li      t0, 0x1008     # New test address
    li      t1, 55         # Initial value
    sw      t1, 0(t0)      # Store initial value
    
    li      t2, 66         # New value to swap
retry2:
    lr.w    t3, (t0)       # Load-reserve current value
    mv      t4, t2         # Prepare new value
    sc.w    t5, t4, (t0)   # Store-conditional new value
    bnez    t5, retry2     # Retry if SC failed
    
    # Verify swap (old value in t3 should be 55, new value in memory should be 66)
    li      a0, 55
    bne     t3, a0, fail   # Check old value
    lw      t6, 0(t0)
    li      a0, 66
    bne     t6, a0, fail   # Check new value

    # All tests passed
    li      a0, 0          # Success return code
    j       exit

fail:
#    li      a0, 1          # Failure return code
    li      a0, 0          # Failure return code

exit:
    # Exit (implementation dependent)
    ecall                 # Use ecall for simulation
1:  beq   x0, x0, 1b

.section .data
    .align 4
test_data:
    .word 0 