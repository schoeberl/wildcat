
SECTIONS
{
  . = 0x0000;      /* Start address for the .text section */
  .text : { *(.text) }
  /* . = 0x8000000;    Start address for the .data section */
  .data : { *(.data) }
  .bss : { *(.bss) }
}
