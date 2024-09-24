// See LICENSE for license details.

#ifndef _ENV_PHYSICAL_SINGLE_CORE_H
#define _ENV_PHYSICAL_SINGLE_CORE_H


//-----------------------------------------------------------------------
// Begin Macro
//-----------------------------------------------------------------------

#define RVTEST_RV64U                                                    \
  .macro init;                                                          \
  .endm

#define RVTEST_RV64UF                                                   \
  .macro init;                                                          \
  RVTEST_FP_ENABLE;                                                     \
  .endm

#define RVTEST_RV64UV                                                   \
  .macro init;                                                          \
  RVTEST_FP_ENABLE;                                                     \
  RVTEST_VEC_ENABLE;                                                    \
  .endm

#define RVTEST_RV32U                                                    \
  .macro init;                                                          \
  .endm

#define RVTEST_RV32UF                                                   \
  .macro init;                                                          \
  RVTEST_FP_ENABLE;                                                     \
  .endm

#define RVTEST_RV32UV                                                   \
  .macro init;                                                          \
  RVTEST_FP_ENABLE;                                                     \
  RVTEST_VEC_ENABLE;                                                    \
  .endm

#define RVTEST_RV64M                                                    \
  .macro init;                                                          \
  RVTEST_ENABLE_MACHINE;                                                \
  .endm

#define RVTEST_RV64S                                                    \
  .macro init;                                                          \
  RVTEST_ENABLE_SUPERVISOR;                                             \
  .endm

#define RVTEST_RV64SV                                                   \
  .macro init;                                                          \
  RVTEST_ENABLE_SUPERVISOR;                                             \
  RVTEST_VEC_ENABLE;                                                    \
  .endm

#define RVTEST_RV32M                                                    \
  .macro init;                                                          \
  RVTEST_ENABLE_MACHINE;                                                \
  .endm

#define RVTEST_RV32S                                                    \
  .macro init;                                                          \
  RVTEST_ENABLE_SUPERVISOR;                                             \
  .endm

#ifdef __riscv64
# define CHECK_XLEN csrr a0, mcpuid; bltz a0, 1f; RVTEST_PASS; 1:
#else
# define CHECK_XLEN csrr a0, mcpuid; bgez a0, 1f; RVTEST_PASS; 1:
#endif

#define RVTEST_ENABLE_SUPERVISOR                                        \
  li a0, MSTATUS_PRV1 & (MSTATUS_PRV1 >> 1);                            \
  csrs mstatus, a0;                                                     \

#define RVTEST_ENABLE_MACHINE                                           \
  li a0, MSTATUS_PRV1;                                                  \
  csrs mstatus, a0;                                                     \

#define RVTEST_FP_ENABLE                                                \
  li a0, MSTATUS_FS & (MSTATUS_FS >> 1);                                \
  csrs mstatus, a0;                                                     \
  csrr a0, mcpuid;                                                      \
  andi a0, a0, 1 << ('D' - 'A'); /* test for D extension */             \
  bnez a0, 1f;                                                          \
  RVTEST_PASS; /* "pass" the test if FPU not present */                 \
1:csrwi fcsr, 0

#define RVTEST_VEC_ENABLE                                               \
  li a0, SSTATUS_XS & (SSTATUS_XS >> 1);                                \
  csrs sstatus, a0;                                                     \
  csrr a1, sstatus;                                                     \
  and a0, a0, a1;                                                       \
  bnez a0, 2f;                                                          \
  RVTEST_PASS;                                                          \
2:                                                                      \

#define RISCV_MULTICORE_DISABLE                                         \
  csrr a0, mhartid;                                                     \
  1: bnez a0, 1b

#define EXTRA_TVEC_USER
#define EXTRA_TVEC_SUPERVISOR
#define EXTRA_TVEC_HYPERVISOR
#define EXTRA_TVEC_MACHINE
#define EXTRA_INIT
#define EXTRA_INIT_TIMER

#define RVTEST_CODE_BEGIN                                               \
        .text;                                                          \
        .align  6;                                                      \
1:

//-----------------------------------------------------------------------
// End Macro
//-----------------------------------------------------------------------

#define RVTEST_CODE_END                                                 \
ecall:  ecall;                                                          \
        j ecall

//-----------------------------------------------------------------------
// Pass/Fail Macro
//-----------------------------------------------------------------------

#define RVTEST_PASS                                                     \
        li TESTNUM, 1;                                                  \
        j ecall

#define TESTNUM x28
#define RVTEST_FAIL                                                     \
1:      beqz TESTNUM, 1b;                                               \
        sll TESTNUM, TESTNUM, 1;                                        \
        or TESTNUM, TESTNUM, 1;                                         \
        j ecall

//-----------------------------------------------------------------------
// Data Section Macro
//-----------------------------------------------------------------------

#define EXTRA_DATA

#define RVTEST_DATA_BEGIN EXTRA_DATA .align 4; .global begin_signature; begin_signature:
#define RVTEST_DATA_END .align 4; .global end_signature; end_signature:

#endif
