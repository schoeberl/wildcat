package memory

import chisel3._
import chisel3.BlackBox

class SramMacro extends BlackBox {
    val io = IO(new Bundle {
        val clk0 = Input(Clock())
        val csb0 = Input(Bool())
        val web0 = Input(Bool())
        val wmask0 = Input(UInt(4.W))
        val addr0 = Input(UInt(8.W))
        val din0 = Input(UInt(32.W))
        val dout0 = Output(UInt(32.W))

        val clk1 = Input(Clock())
        val csb1 = Input(Bool())
        val addr1 = Input(UInt(8.W))
        val dout1 = Output(UInt(32.W))
    })

    override def desiredName: String = s"sky130_sram_1kbyte_1rw1r_32x256_8" 
}
