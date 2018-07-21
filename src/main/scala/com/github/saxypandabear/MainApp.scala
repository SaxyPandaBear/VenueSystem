package com.github.saxypandabear

import java.io.FileInputStream
import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

// starting code from
// https://doc.akka.io/docs/akka-http/current/introduction.html#using-akka-http
object MainApp {
  // TODO: parameterize this eventually
  private final val propertiesFileName = "environment-dev.properties"

  def main(args: Array[String]) {

    val (url, port) =
      try {
        val properties = new Properties()
        properties.load(new FileInputStream(propertiesFileName))
        (
          properties.getProperty("url"),
          properties.getProperty("port")
        )
      } catch { case e: Exception =>
          e.printStackTrace()
          sys.exit(1)
      }

    implicit val system: ActorSystem = ActorSystem("venue-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, , )

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}