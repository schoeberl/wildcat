package wildcat.pipeline

import chisel3._
class Wildcat extends Module {

  val fetch = Module(new Fetch())
  val decode = Module(new Decode())

  fetch.io.fedec <> decode.io.fedec
}
