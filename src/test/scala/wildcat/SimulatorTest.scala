package wildcat

import java.io.File
import scala.sys.process._
import org.scalatest.flatspec.AnyFlatSpec

import wildcat.isasim._

class SimulatorTest extends AnyFlatSpec {

  val files = new File("asm").listFiles.filter(_.isFile).toList.filter(_.getName.endsWith(".s"))
  for (f <- files) {
    s"Simulator $f" should "pass" in {
      "make app".!
      SimRV.main(Array("a.bin"))
    }
  }
}
