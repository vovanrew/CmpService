package compressor

import java.io._

import akka.actor.{Actor, Props}

object Compressor {
  def props(input_file: String) = Props(classOf[Compressor], input_file)

  case object CntCompress
  case object TODO
  case object CompresedFile
}

class Compressor (input_file: String) extends Actor {
  import Compressor._

  override def receive = {

    case CntCompress => {

      def compressedFile = input_file + ".cmps"

      def charCntCompressor(input_file: String): String = {
        //read string
        val input_string = scala.io.Source.fromFile(input_file).getLines.mkString.stripLineEnd

        //check format
        val m = input_string matches "[a-z0-9A-z]+"
        if (m == false) {
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
        output
      }

      val writeFile = {
        val file = new File(compressedFile)
        val bw = new BufferedWriter(new FileWriter(file))
        bw.write(charCntCompressor(input_file))
        bw.close()
        sender() ! "compressed"
      }
    }

    case CompresedFile =>
      sender() ! input_file + ".cmps"
  }

}