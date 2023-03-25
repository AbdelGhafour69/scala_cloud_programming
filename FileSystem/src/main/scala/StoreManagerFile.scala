import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.util.Random._
import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import scala.util.control.Breaks._
import java.io._
import scala.io.Source
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

class UserFile extends Actor {
  import Messages._
  override def receive: Receive = {

    case Start(store: ActorRef) =>
      breakable {
        while (true) {
          println("Enter your request (Stop to stop)")
          val req = readLine()
          val trimmedreq: List[String] = req.split("\\s+").map(_.trim).toList

          trimmedreq(0) match {
            case "Store" =>
              if (trimmedreq.length == 3) {
                store ! Store(trimmedreq(1), trimmedreq(2))
              } else {
                println("Number of arguments is not compatible")
              }

            case "Lookup" =>
              if (trimmedreq.length == 2) {
                store ! Lookup(trimmedreq(1), self)
                break
              } else {
                println("Number of arguments is not compatible")
              }

            case "Delete" =>
              if (trimmedreq.length == 2) {
                store ! Delete(trimmedreq(1))
              } else {
                println("Number of arguments is not compatible")
              }
            case "Stop" =>
              println("Service has stopped !")
              break

          }
        }
      }
    case LookupResponse(value: String, store: ActorRef) =>
      println(value)
      self ! Start(store)

    case LookupError(value: String, store: ActorRef) =>
      println(value)
      self ! Start(store)

  }
}

class StoreManager extends Actor {
  import Messages._

  val ma = context.actorOf(Props[CacheAgent], "CacheAgent")
  implicit val to = Timeout(10 seconds)

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      val future = (ma ? Store(key, value))
      Await.result(future, 1 seconds)

    case Delete(key: String) =>
      val future = (ma ? Delete(key))
      Await.result(future, 1 seconds)

    case Lookup(key: String, user: ActorRef) =>
      val future = (ma ? Lookup(key, self))
      val res = Await.result(future, 5 seconds)
      res match {
        case LookupError(value, ar)    => sender ! LookupError(value, self)
        case LookupResponse(value, ar) => sender ! LookupResponse(value, self)
      }
  }
}

object StoreManagerFile extends App {
  import Messages._

  val as = ActorSystem("FileSystem")
  val store = as.actorOf(Props[StoreManager], "StoreManager")
  // val qa = as.actorOf(Props[UserFile], "User")
  val qa = as.actorOf(Props[RandomUser], "User")
  qa ! Populate()
  qa ! Start(store)

}
