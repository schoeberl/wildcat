package wildcat

import chisel3._
import chisel3.util._
import chisel3.util.experimental.BoringUtils
import chiseltest._
import memory.InstructionROM
import org.scalatest.flatspec.AnyFlatSpec
import soc._
import wildcat.pipeline._

class DelayedReadMem(delay: Int, readValue: Int) extends PipeConDevice(32) {
  require(delay > 1, "This test memory needs a multi-cycle response")

  val busy = RegInit(false.B)
  val count = RegInit(0.U(log2Ceil(delay + 1).W))
  val ack = busy && count === (delay - 1).U

  when(!busy && cpuPort.rd) {
    busy := true.B
    count := 0.U
  }.elsewhen(busy) {
    when(ack) {
      busy := false.B
    }.otherwise {
      count := count + 1.U
    }
  }

  cpuPort.rdData := Mux(ack, readValue.U(32.W), 0.U)
  cpuPort.ack := ack
}

class DataMemoryStallTop(delay: Int, readValue: Int) extends Module {
  val io = IO(new Bundle {
    val regFile = Output(Vec(32, UInt(32.W)))
    val stop = Output(Bool())
  })

  val cpu = Module(new ThreeCats())
  val imem = Module(new InstructionROM(Array(
    0x00002283, // lw x5, 0(x0)
    0x00028513, // addi x10, x5, 0
    0x00000073  // ecall
  )))
  val dmem = Module(new DelayedReadMem(delay, readValue))

  cpu.io.imem <> imem.cpuPort
  cpu.io.dmem <> dmem.cpuPort

  io.regFile := DontCare
  BoringUtils.bore(cpu.debugRegs, Seq(io.regFile))
  io.stop := DontCare
  BoringUtils.bore(cpu.stop, Seq(io.stop))
}

class DataMemoryStallTest extends AnyFlatSpec with ChiselScalatestTester {

  "ThreeCats" should "stall until delayed data memory read acknowledgement" in {
    val readValue = 0x12345678

    test(new DataMemoryStallTop(delay = 4, readValue = readValue)) { dut =>
      dut.clock.setTimeout(0)

      var stopped = false
      var cycles = 0
      while(!stopped && cycles < 40) {
        dut.clock.step()
        stopped = dut.io.stop.peekBoolean()
        cycles += 1
      }

      assert(stopped, "Program did not reach ecall")
      assert(
        dut.io.regFile(10).peekInt() == BigInt(readValue & 0xffffffffL),
        "A delayed load should write the acknowledged data to x10"
      )
    }
  }
}
