package wildcat.pipeline

import chisel3._
class Wildcat {

  val fetch = Module(new Fetch())
  val decode = Module(new Decode())
}
