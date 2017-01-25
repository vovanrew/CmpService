import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.stream.{ActorMaterializer, Materializer}
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Success => ScalaSuccess}
import scala.util.{Failure => ScalaFailure}
import scala.io.StdIn

import compressor.Compressor
import compressor.Decompressor

//define compress and decomrpess request
case class CompressReq(inputFile: String)
case class DecompressReq(inputFile: String)

trait Protocols extends DefaultJsonProtocol {
  implicit val compressFormat = jsonFormat1(CompressReq.apply)
  implicit val decompressFormat = jsonFormat1(DecompressReq.apply)
}

trait CompressService extends Protocols {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  implicit val timeout: Timeout

  import Compressor._
  import Decompressor._

  def config: Config
  val logger: LoggingAdapter

  def convert(compressReq: CompressReq)(implicit ec: ExecutionContext): Future[CompressReq] = Future {
    val compressor = system.actorOf(Compressor.props(compressReq.inputFile))
    val s = Await.result(compressor ? CntCompress, 5 seconds)
    println("File " + s)
    system.stop(compressor)
    compressReq
  }

  def deconvert(decompressReq: DecompressReq)(implicit ec: ExecutionContext): Future[DecompressReq] = Future {
    val decompressor = system.actorOf(Decompressor.props(decompressReq.inputFile))
    val s = Await.result(decompressor ? CntDecompress, 5 seconds)
    println("File " + s)
    system.stop(decompressor)
    decompressReq
  }

  val routes = {
    logRequestResult("akka-http-microservice") {
      pathPrefix("compress") {
        post{
          entity(as[CompressReq]) { compressReq =>
            onComplete(convert(compressReq)) { 
              case ScalaSuccess(value) => {
                val compressor = system.actorOf(Compressor.props(compressReq.inputFile))
                complete(StatusCodes.OK + "\nfilename: " + Await.result(compressor ? CompresedFile, 5 seconds) + "\n")
              }
              case ScalaFailure(ex)    => complete((StatusCodes.InternalServerError + "\n" + ex.getMessage + "\n"))
            }
          }
        }
      } ~
      pathPrefix("decompress") {
        post{
          entity(as[DecompressReq]) { decompressReq =>
            onComplete(deconvert(decompressReq)) {
              case ScalaSuccess(value) => {
                val decompressor = system.actorOf(Decompressor.props(decompressReq.inputFile))
                complete(StatusCodes.OK + "\nfilename: " + Await.result(decompressor ? DecompressedFile, 5 seconds) + "\n")
              }
              case ScalaFailure(ex)    => complete((StatusCodes.InternalServerError + "\n" + ex.getMessage + "\n"))
            }
          }
        }
      }
    }
  }
}

object AkkaHttpMicroservice extends App with CompressService{
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override implicit val timeout = Timeout(5 seconds)

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
  StdIn.readLine()
  system.terminate()
}
