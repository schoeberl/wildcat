package wildcat

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.common.Functions.registerFile


class RegisterFileTest extends AnyFlatSpec with ChiselScalatestTester {

  class RegisterFile(version: Int) extends Module {
    val io = IO(new Bundle {
      val wrAddr = Input(UInt(5.W))
      val wrData = Input(UInt(32.W))
      val wrEna = Input(Bool())
      val rdAddr1 = Input(UInt(5.W))
      val rdAddr2 = Input(UInt(5.W))
      val rdData1 = Output(UInt(32.W))
      val rdData2 = Output(UInt(32.W))
    })

    if (version == 0) {
      val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

      when(io.wrEna && io.wrAddr =/= 0.U) {
        regs(io.wrAddr) := io.wrData
      }

      io.rdData1 := regs(io.rdAddr1)
      io.rdData2 := regs(io.rdAddr2)
    } else if (version == 1) {
      val (rs1Val, rs2Val) = registerFile(io.rdAddr1, io.rdAddr2, io.wrAddr, io.wrData, io.wrEna, false)
      io.rdData1 := rs1Val
      io.rdData2 := rs2Val
    } else {
      val (rs1Val, rs2Val) = registerFile(io.rdAddr1, io.rdAddr2, io.wrAddr, io.wrData, io.wrEna, true)
      io.rdData1 := rs1Val
      io.rdData2 := rs2Val
    }
  }


  def doTest(d: RegisterFile) = {
    // random (generated) test
    d.io.wrAddr.poke(1.U)
    d.io.wrData.poke(42.U)
    d.io.wrEna.poke(true.B)
    d.io.rdAddr1.poke(1.U)
    d.io.rdAddr2.poke(2.U)
    d.clock.step(1)
    d.io.rdData1.expect(42.U)
    d.io.rdData2.expect(0.U)

    // Check normal work, R0 is always 0
    for (i <- 0 until 10) {
      d.io.wrAddr.poke(i.U)
      d.io.wrData.poke((i + 1).U)
      d.io.wrEna.poke(true.B)
      d.clock.step(1)
    }
    for (i <- 0 until 10) {
      d.io.rdAddr1.poke(i.U)
      d.io.rdAddr2.poke(i.U)
      d.clock.step(1)
      if (i == 0) {
        d.io.rdData1.expect(0.U)
        d.io.rdData2.expect(0.U)
      } else {
        d.io.rdData1.expect((i+1).U)
        d.io.rdData2.expect((i+1).U)
      }
    }

    // read during write test
    d.io.wrAddr.poke(1.U)
    d.io.wrData.poke(42.U)
    d.io.wrEna.poke(true.B)
    d.io.rdAddr1.poke(1.U)
    d.io.rdAddr2.poke(2.U)
    d.clock.step(1)
    d.io.rdData1.expect(42.U)
    d.io.rdData2.expect(3.U) // still 3

    d.io.wrAddr.poke(1.U)
    d.io.wrData.poke(123.U)
    d.io.wrEna.poke(true.B)
    d.io.rdAddr1.poke(1.U)
    d.clock.step(1)
    d.io.rdData1.expect(123.U)
  }
  // val rfs = List(Module(new RegisterFile()), Module(new RegisterFile2()))

  //for (rf <- rfs) {

  "RegisterFile plain" should "pass" in {
    test(new RegisterFile(0))(doTest(_))
  }
  "RegisterFile FF" should "pass" in {
    test(new RegisterFile(1))(doTest(_))
  }
  "RegisterFile SRAM" should "pass" in {
    test(new RegisterFile(2))(doTest(_))
  }
  // }
}
