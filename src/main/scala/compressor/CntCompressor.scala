package compressor

import java.io._
import scala.io.Source

import akka.actor.{Actor, Props}

object Compressor {
  def props(input_file: String) = Props(classOf[Compressor], input_file)

  case object CntCompress
  case object CompresedFile
}

class Compressor (input_file: String) extends Actor {
  import Compressor._

  override def receive = {

    case CntCompress => {

      def compressedFile = input_file + ".cmps"

      val inputFile = Source.fromFile(input_file)

      val bw = new BufferedWriter(new FileWriter(new File(compressedFile)))

      inputFile.getLines().foreach { input_string =>

        val m = input_string matches "[a-z0-9A-z]+"

        if (m == false) {
          bw.close()
          throw new Exception("Format not match!")
        }

        //output string
        var output = ""

        //initial the count
        var cnt = 1
        var charPrev = input_string.charAt(0)
        var charCurrent = input_string.charAt(0)
        val n = input_string.length - 1

        for (i <- 1 to n) {
          charCurrent = input_string.charAt(i)
          if (charCurrent == charPrev) {
            cnt += 1
          }
          else {
            output += charPrev
            output += cnt
            output += "-"
            charPrev = charCurrent
            cnt = 1
          }
          // last character
          if (i == n) {
            output += charCurrent
            output += cnt
          }
        }

        bw.write(output)
        bw.newLine()
      }

      bw.close()
      sender() ! "compressed"
    }

    case CompresedFile =>
      sender() ! input_file + ".cmps"
  }
}