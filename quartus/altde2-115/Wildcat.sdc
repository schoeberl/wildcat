###########################################################################
# SDC file for DE2-115 board
###########################################################################

# Clock in input pin (50 MHz, overdo ut with 200 MHz)
create_clock -period 5 [get_ports clock]

# Create generated clocks based on PLLs
derive_pll_clocks -use_tan_name

derive_clock_uncertainty