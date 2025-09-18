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

#### Forwarding in ID stage

max_tt_025C_1v80: 0.094

t_ex = 6.25 ns (160 MHz)
t_id = 4.9 ns (204 MHz)
28% higher fmax!

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

Better define the steps for fmax:

`make APP=asm/apps/blink.s app hw-fmax synth-fmax`

not really needed, as fmax does not use any app.

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

