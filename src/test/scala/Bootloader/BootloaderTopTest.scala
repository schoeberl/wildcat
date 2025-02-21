package Bootloader

import Bootloader.BootloaderTop
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Bootloader by Alexander and Georg for the Wildcat
 */
class BootloaderTopTest extends AnyFlatSpec with
  ChiselScalatestTester {
  "BootloaderTop" should "pass" in {
    test(new BootloaderTop(100000000)) { dut =>
      val BIT_CNT = ((100000000 + 115200 / 2) / 115200 - 1)
      dut.io.rx.poke(1.U)

      //First byte:
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(0.U) //Start bit
      dut.clock.step(BIT_CNT)
      dut.io.rx.poke(1.U) //First data bit
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
      dut.io.rx.poke(0.U) //Last data bit

      dut.io.instrData.expect("h000000aa".U)
      dut.io.wrEnabled.expect(0.U)


    }
  }

}
