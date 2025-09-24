# Implementation Notes

Keep a log on resource usage and fmax.

Sky130 use max_tt_025C_1v80

We should report core sizes, as we are not intrested padframe.

## Experiments

### AluSpeed

Palane ALU with input and output registers
Clock period 5 ns (200 MHz)

core size: 151.8 155.04
max_tt_025C_1v80: 0.315

with two registers on input and output
core size: 165.6 168.64
min_tt_025C_1v80: 0.28

clock period 3 ns (333 MHz)

core size: 151.8 155.04
max_tt_025C_1v80: -1.81

with two registers on input and output
core size: 165.6 168.64
max_tt_025C_1v80: -1.72

Summary: the plain ALU can run at 200 MHz.

### ForwardingSpeed

Clock 5 ns (200 MHz)

#### Forwarding in EX stage

max_tt_025C_1v80: -1.25
t_ex = 6.25 ns (160 MHz)

FPGA (5 ns constr) slack: -1.965 ns (144 MHz)

#### Forwarding in ID stage

max_tt_025C_1v80: 0.094
t_id = 4.9 ns (204 MHz)
28% higher fmax!

FPGA (5 ns constr) slack: -0.178 ns (193 MHz)

#### Chisel Versions

 * with ID stage forwarding
 * with ALU in a Chisel switch statement or Vec

Clock 5 ns (200 MHz)

Chisel 3.6, with emitVerilog
Generates `? :` pririty muxes (probably the old Verilog emitter)
max_tt_025C_1v80: 0.094
Chisel 3.6 with emitSystemVerilogFile
Generates `if` `else if` priority muxes (probably via CIRCT)
max_tt_025C_1v80: 0.0098 (less slack than emitVerilog)

Chisel 5 
emitVerilog fails as it generates `automatic` variables, which yosys does not like, maybe Vivado and Quartus would accept it. Therefore, use emitSystemVerilogFile to have CIRCT options
max_tt_025C_1v80: 0.0098 (same as Chisel 3.6 with emitSystemVerilogFile)

Chisel 6
max_tt_025C_1v80: -0.126 This is a regression compared to Chisel 5

**With Vec instead of switch**

Chisel 3.6 emitVerilog: leads to same ? : chain: max_tt_025C_1v80: -1.62 (32 % regression!)
**TODO: needs investigation!** maybe this in line 17: `  wire [62:0] _res_res_2_T_1 = _GEN_0 << decExReg_rs2Val[4:0];`
Check switch again, with `res := (a << b(4, 0))(31, 0)` gives less 62 bit signals, slack now max_tt_025C_1v80: 0.135!
With Vec and `res(SLL.id.U) := (a << b(4, 0))(31, 0)`, ony one 62 bit signal, still: max_tt_025C_1v80: -1.62
Chisel 3.6 emitSystemVerilogFile: translated now to a casez: max_tt_025C_1v80: -1.29 (still 26 % regression)

Chisel 5: max_tt_025C_1v80: -1.29
Chisel 6: max_tt_025C_1v80: 0.17 (best result!)

**Manual Verilog Changes**

droppping the not needed casez: max_tt_025C_1v80: -0.035!!!

Use only 32 bits in shift left: max_tt_025C_1v80: 0.17
Shift right signal only 5 bits: max_tt_025C_1v80: 0.17


## Wildcat synth results:

Xilinx/AMD FPGA 100 MHz, ASIC 50 MHz constraint
FPGA with the register file as memory

ASIC size in report 13, openroad-floorplan.log at the end
ASIC for max tt 025C 1v80 in report 54, ws.max.rpt

make hw-fmax

make synth-fmax
make librelane


### 14 Sept 2024 (DATE submission) hash 01cc107dde

1744 LUTs, 1329 FFs - with overconstrained to 200 MHz, and FF reg file

with 100 MHz, reg file in memory

1214 LUTs, 303 FFs
Slack 0.120ns

probably as a register file as FFs
[INFO] Floorplanned on a die area of 0.0 0.0 434.645 445.365 (um).
max_tt_025C_1v80: 3.07


### 16 Jan 2025

1273 LUTs, 305 FFs
Slack -0.162ns

with register file defined as memory:
[INFO] Floorplanned on a die area of 0.0 0.0 411.61 422.33 (um).
max_tt_025C_1v80: 4.86

with register file defined as FF:
[INFO] Floorplanned on a die area of 0.0 0.0 438.505 449.225 (um).
max_tt_025C_1v80: 2.55

Looks like the Verilog memory definition is more efficient than the register array.
Probably because of the priority mux definition of the generated Verilog code.

### 8 July 2025

1340 LUTs, 310 FFs
Slack (VIOLATED) :        -0.400ns  (required time - arrival time)

### 18 Sept 2025

Fix reset and initial instruction.
Change PipeCon.
Update of LibreLane.

[INFO] Floorplanned on a die area of 0.0 0.0 419.83 430.55 (µm).
max_tt_025C_1v80: 4.32

1323 LUTs, 310 FFs, 0 BRAM
Slack (VIOLATED) :        -1.178ns  (required time - arrival time)

