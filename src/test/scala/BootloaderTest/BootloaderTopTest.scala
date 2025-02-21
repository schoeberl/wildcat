package BootloaderTest

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

    }
  }

}
