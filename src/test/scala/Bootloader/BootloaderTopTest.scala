package Bootloader

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.Console.println

/**
 * Bootloader by Alexander and Georg for the Wildcat
 */
class BootloaderTopTest extends AnyFlatSpec with
  ChiselScalatestTester {
  "BootloaderTop" should "receive 1 byte" in {
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
  "BootloaderTop" should "Receive entire instruction" in {
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

class BootloaderTopTestScala extends AnyFlatSpec with
  ChiselScalatestTester {
  "BootloaderTop" should "Receive entire instruction and addr and enable writing" in {
    test(new BootloaderTop(10000000))
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
        val BIT_CNT = ((10000000 + 115200 / 2) / 115200 - 1)

        val instrData = "h12345678".U
        val instrAddr = "haa54f08e".U

        //Start protocol
        def preByteProtocol() = {
          dut.io.rx.poke(1.U)
          dut.clock.step(BIT_CNT)
          dut.io.rx.poke(0.U)
          dut.clock.step(BIT_CNT)
        }

        def sendByte(n: UInt) = {
          preByteProtocol()

          for(j <- 0 until 8){ //0 until 8 means it runs from 0 to and with 7
            dut.io.rx.poke(n(j))
            dut.clock.step(BIT_CNT)
          }
        }

        def send32bit(n: UInt) = {
          sendByte(n(7,0))
          sendByte(n(15,8))
          sendByte(n(23,16))
          sendByte(n(31,24))
        }

        send32bit(instrAddr) //First send address
        dut.io.instrData.expect(instrAddr) //instrAddr should be in instrData space now
        send32bit(instrData) //Then send the instrData

        dut.clock.step(BIT_CNT)
        dut.io.instrAddr.expect(instrAddr)
        dut.io.instrData.expect(instrData)
        dut.io.wrEnabled.expect(1.U)


      }
  }

}