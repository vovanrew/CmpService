package compressor

import akka.actor.{Actor, Props}

import java.io._

object Decompressor {

  def props(imput_file: String) = Props(classOf[Decompressor], imput_file)

  case object CntDecompress
  case object TODO
  case object DecompressedFile
}

class Decompressor(input_file: String) extends Actor {
  import Decompressor._

  override def receive = {

    case CntDecompress => {

      val decompressedFile = input_file + ".dcmps"

      def charCntDecompressor(input_file: String): String = {
        //read string
        val input_string = scala.io.Source.fromFile(input_file).getLines.mkString.stripLineEnd

        //check format
        val m = input_string matches "([a-z0-9A-z][0-9]-)+([a-z0-9A-z][0-9])"
        if (m == false) {
          throw new Exception("Format not match! Empty file:" + decompressedFile + "generated.\n")
        }

        def restore(s: String): String = {
          val charCurrent = s.charAt(0)
          val cnt = s.takeRight(s.length - 1).toString.toInt
          charCurrent.toString * cnt
        }

        //output string
        var output = ""
        for (s <- input_string.split("-")) {
          output += restore(s)
        }
        output
      }

      val writeFile = {
        val file = new File(decompressedFile)
        val bw = new BufferedWriter(new FileWriter(file))
        bw.write(charCntDecompressor(input_file))
        bw.close()
      }
    }

    case DecompressedFile =>
      sender() ! input_file + ".dcmps"
  }
}