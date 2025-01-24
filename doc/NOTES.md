# Implementation Notes

Keep a log on resource needs and fmax

## Wildcat synth results:

Xilinx/AMD FPGA 100 MHz, ASIC 50 MHz constraint
FPGA with register file as memory

ASIC size in report 13, openroad-floorplan.log at the end
ASIC for max tt 025C 1v80 in report 54, ws.max.rpt

make hw-fmax

make synth-fmax
make openlane


### 14 Sept 2024 (DATE submission) hash 01cc107dde

1744 LUTs, 1329 FFs - with overconstrained to 200 MHz, and FF reg file

with 100 MHz, reg file in memory

1214 LUTs, 303 FFs
Slack 0.120ns

probably as register file as FFs
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

