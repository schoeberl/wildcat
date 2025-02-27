package Bootloader

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Bootloader by Alexander and Georg for the Wildcat
 */
class BootloaderTopTest extends AnyFlatSpec with
  ChiselScalatestTester {
  "BootloaderTop" should "pass" in {
    test(new BootloaderTop(10000000))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val BIT_CNT = ((10000000 + 115200 / 2) / 115200 - 1)
      dut.io.rx.poke(1.U)

      //First byte:
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(0.U) //Start bit
      dut.clock.step(BIT_CNT)

      dut.io.rx.poke(0.U) //First data bit
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(1.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(0.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(1.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(0.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(1.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(0.U)
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(1.U) //Last data bit
      dut.clock.step(100)

      dut.io.instrData.expect("haa000000".U)
      dut.io.wrEnabled.expect(0.U)


    }
  }

}

class BootloaderTopTestFullInstr extends AnyFlatSpec with
  ChiselScalatestTester {
  "BootloaderTop" should "Receive entire instruction and enable writing" in {
    test(new BootloaderTop(10000000))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        val BIT_CNT = ((10000000 + 115200 / 2) / 115200 - 1)

        //Start protocol
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U) //Start bit
        dut.clock.step(BIT_CNT)

        //First byte:
        dut.io.rx.poke(0.U) //First data bit
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U) //Last data bit
        dut.clock.step(BIT_CNT)

        //Second byte:
        //Second Start bits:
        dut.io.rx.poke(1.U) //Start bit
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U) //Start bit 2
        dut.clock.step(BIT_CNT)

        dut.io.rx.poke(0.U) //First data bit of the byte
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U) //Last data bit of the byte
        dut.clock.step(BIT_CNT)

        //Third byte:
        //Third Start bits:
        dut.io.rx.poke(1.U) //Start bit
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U) //Start bit 2
        dut.clock.step(BIT_CNT)

        dut.io.rx.poke(0.U) //First data bit of the byte
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U) //Last data bit of the byte
        dut.clock.step(BIT_CNT)

        //Fourth byte:
        //Fourth Start bits:
        dut.io.rx.poke(1.U) //Start bit
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U) //Start bit 2
        dut.clock.step(BIT_CNT)

        dut.io.rx.poke(0.U) //First data bit of the byte
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(0.U)
        dut.clock.step(BIT_CNT)
        dut.io.rx.poke(1.U) //Last data bit of the byte
        dut.clock.step(100)

        dut.io.instrData.expect("haa54f08e".U)
        dut.io.wrEnabled.expect(0.U)


      }
  }

}