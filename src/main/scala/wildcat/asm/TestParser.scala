package wildcat.asm

import scala.util.parsing.combinator._

case class WordFreq(word: String, count: Int) {

  override def toString = "Word <" + word + "> " + "occurs with frequency " + count

}

class SimpleParser extends RegexParsers {

  def word: Parser[String] = """[a-z]+""".r ^^ { _.toString }
  def number: Parser[Int] = """(0|[1-9]\d*)""".r ^^ { _.toInt }
  def label: Parser[String] = """[a-z]+:""".r ^^ { _.toString }
  
  def freq: Parser[WordFreq] = word ~ number ^^ { case wd ~ fr => WordFreq(wd, fr) }

  def reg: Parser[String] = """x(\d\d?)""".r ^^ { _.toString }
  def comment: Parser[String] = """#.*""".r ^^ { _.toString }
  def test: Parser[String] = label.? ~ word ~ reg.? ~ comment.? ~ reg.?  ^^
    { case l ~ wd ~ r ~ c  => l + "---" + wd + "++++" + r + ":::" + c }
}

object TestParser extends SimpleParser {

  def main(args: Array[String]) {

    val code = List(
        "command",
        "abc # this is a comment",
        "label: xxx")

    for (line <- code) {
      parse(test, line) match {
        case Success(matched, _) => println(matched)
        case Failure(msg, _) => println("FAILURE: " + msg)
        case Error(msg, _) => println("ERROR: " + msg)
      }
    }

  }
}