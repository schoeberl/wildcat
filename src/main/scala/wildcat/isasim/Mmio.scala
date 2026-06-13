/*
 * Copyright (c) 2015-2017, DTU
 * Simplified BSD License
 */

/*
 * This file adds support for memory mapped IO.
 * It has a simple MMIO interface and implements two devices:
 *   - A UART 16550 compatible serial port at 0x10000000
 *   - A CLINT timer at 0x02000000 that can trigger timer interrupts
 * Finally it has a global MMIO class that provides easy access to all MMIO devices.
 */

package wildcat.isasim

import java.util.concurrent.ConcurrentLinkedQueue

import wildcat.MMIO._
import wildcat.LoadStoreFunct3._

// An interface for memory mapped devices
trait MmioDevice {
  def base: Int
  def size: Int
  final def contains(addr: Int): Boolean = addr >= base && addr < base + size

  // offset is address relative to base; mtime is the current machine time
  def load(offset: Int, mtime: Long): Int
  def store(funct3: Int, offset: Int, value: Int): Unit
}

// UART 16550 compatible
class Uart extends MmioDevice {
  val base = UART_BASE
  val size = UART_SIZE

  // A thread that pushes stdin bytes into a queue
  private val rxQueue = new ConcurrentLinkedQueue[Integer]()
  private val rxThread = new Thread(() => {
    val in = java.lang.System.in
    var b = in.read()
    while (b >= 0) {
      rxQueue.offer(b & 0xff)
      b = in.read()
    }
  }, "uart-rx")
  rxThread.setDaemon(true)
  rxThread.start()

  def load(offset: Int, mtime: Long): Int = offset match {
    case 0 => val b = rxQueue.poll(); if (b == null) 0 else b.intValue() // RBR
    case 5 => 0x60 | (if (rxQueue.isEmpty) 0 else 1)                     // LSR
    case _ => 0x00
  }

  def store(funct3: Int, offset: Int, value: Int): Unit = funct3 match {
    // Only a byte store to THR (offset 0) prints; other regs are no-ops
    case SB if offset == 0 =>
      val b = value & 0xff
      if (b != 0x0d) {  // drop \r
        Console.out.write(b)
        Console.out.flush()
      }
    case _ =>
  }
}

// CLINT - Timer interrupt only
class Clint extends MmioDevice {
  val base = CLINT_BASE
  val size = CLINT_SIZE

  // Timer compare value (no pending timer by default)
  private var mtimecmp: Long = Long.MaxValue

  // Timer interrupt pending once mtime has reached mtimecmp
  def timerPending(mtime: Long): Boolean = mtime >= mtimecmp

  def load(offset: Int, mtime: Long): Int = offset match {
    case 0xbff8 => (mtime & 0xffffffffL).toInt             // mtime lo
    case 0xbffc => ((mtime >>> 32) & 0xffffffffL).toInt    // mtime hi
    case 0x4000 => (mtimecmp & 0xffffffffL).toInt          // mtimecmp lo
    case 0x4004 => ((mtimecmp >>> 32) & 0xffffffffL).toInt // mtimecmp hi
    case _ => 0
  }

  def store(funct3: Int, offset: Int, value: Int): Unit = offset match {
    case 0x4000 => mtimecmp = (mtimecmp & 0xffffffff00000000L) | (value.toLong & 0xffffffffL)
    case 0x4004 => mtimecmp = (mtimecmp & 0x00000000ffffffffL) | ((value.toLong & 0xffffffffL) << 32)
    case _ =>
  }
}

// Global MMIO class with access to all devices
class Mmio(enabled: Boolean) {

  private val uart = new Uart
  private val clint = new Clint
  private val devices: Array[MmioDevice] =
    if (enabled) Array(uart, clint) else Array.empty[MmioDevice]

  // The device mapped at addr or null if the address is not MMIO
  private def deviceAt(addr: Int): MmioDevice = {
    for (d <- devices) if (d.contains(addr)) return d
    null
  }

  def isMmio(addr: Int): Boolean = deviceAt(addr) != null

  def load(addr: Int, mtime: Long): Int = {
    val d = deviceAt(addr)
    if (d == null) 0 else d.load(addr - d.base, mtime)
  }

  def store(funct3: Int, addr: Int, value: Int): Unit = {
    val d = deviceAt(addr)
    if (d != null) d.store(funct3, addr - d.base, value)
  }

  def timerPending(mtime: Long): Boolean = enabled && clint.timerPending(mtime)
}
