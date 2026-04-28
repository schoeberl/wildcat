package wildcat.pipeline

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

import soc._

/**
 * On-chip memory with one clock cycle read timing and write forwarding
 */
class ScratchPadMem(data: Array[Int], nrBytes: Int = 4096) extends PipeConDevice(32) {

  val mems = Array(
    SyncReadMem(nrBytes/4, UInt(8.W), SyncReadMem.WriteFirst),
    SyncReadMem(nrBytes/4, UInt(8.W), SyncReadMem.WriteFirst),
    SyncReadMem(nrBytes/4, UInt(8.W), SyncReadMem.WriteFirst),
    SyncReadMem(nrBytes/4, UInt(8.W), SyncReadMem.WriteFirst))

  /* not used, would be too easy
  val dataHex = data.map(_.toHexString).mkString("\n")
  val file = new java.io.PrintWriter("data.hex")
  file.write(dataHex)
  file.close()
  loadMemoryFromFile(mem, "data.hex")
   */

  // split an integer into a seq of 4 bytes
  // little endian, so first byte in seq goes to mem(0)
  def splitInt(x: Int): Seq[Int] = {
    (0 until 4).map(i => (x >> (i * 8)) & 0xff)
  }
  val split = data.map(splitInt)
  // Don't know how to split this in a functional style
  // TODO: fight repetition
  val init0 = split.map(_(0)).map(_.toHexString).mkString("\n")
  val file0 = new java.io.PrintWriter("data0.hex")
  file0.write(init0)
  file0.close()
  loadMemoryFromFile(mems(0), "data0.hex")
  val init1 = split.map(_(1)).map(_.toHexString).mkString("\n")
  val file1 = new java.io.PrintWriter("data1.hex")
  file1.write(init1)
  file1.close()
  loadMemoryFromFile(mems(1), "data1.hex")
  val init2 = split.map(_(2)).map(_.toHexString).mkString("\n")
  val file2 = new java.io.PrintWriter("data2.hex")
  file2.write(init2)
  file2.close()
  loadMemoryFromFile(mems(2), "data2.hex")
  val init3 = split.map(_(3)).map(_.toHexString).mkString("\n")
  val file3 = new java.io.PrintWriter("data3.hex")
  file3.write(init3)
  file3.close()
  loadMemoryFromFile(mems(3), "data3.hex")

  val idx = log2Up(nrBytes/4)
  cpuPort.rdData := mems(3).read(cpuPort.address(idx+2, 2)) ##
    mems(2).read(cpuPort.address(idx+2, 2)) ##
    mems(1).read(cpuPort.address(idx+2, 2)) ##
    mems(0).read(cpuPort.address(idx+2, 2))
  when(cpuPort.wrMask(0) && cpuPort.wr) {
    mems(0).write(cpuPort.address(idx+2, 2), cpuPort.wrData(7, 0))
  }
  when(cpuPort.wrMask(1) && cpuPort.wr) {
    mems(1).write(cpuPort.address(idx+2, 2), cpuPort.wrData(15, 8))
  }
  when(cpuPort.wrMask(2) && cpuPort.wr) {
    mems(2).write(cpuPort.address(idx+2, 2), cpuPort.wrData(23, 16))
  }
  when(cpuPort.wrMask(3) && cpuPort.wr) {
    mems(3).write(cpuPort.address(idx+2, 2), cpuPort.wrData(31, 24))
  }
  cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B) // TODO: test stalling
}
