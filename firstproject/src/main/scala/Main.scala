import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.util.Random._
import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import scala.util.control.Breaks._

object Messages {
  case class Store(key: String, value: String)
  case class Lookup(key: String, ar: ActorRef)
  case class Delete(key: String)
  case class Start(ar: ActorRef)
  case class LookupResponse(value: String, store: ActorRef)
  case class LookupError(value: String, store: ActorRef)

}

class User extends Actor {
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

    case _ =>
      println("Do nothing")
  }
}

class Store_Manager extends Actor {
  val map = scala.collection.mutable.HashMap.empty[String, String]

  import Messages._

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      map += key -> value
      println(map)
    case Delete(key: String) =>
      map -= key
      println(map)
    case Lookup(key: String, user: ActorRef) =>
      user ! LookupResponse(map(key), self)
  }
}

object File_System extends App {
  import Messages._

  val as = ActorSystem("File_System")
  val store = as.actorOf(Props[Store_Manager], "Store_manager")
  val qa = as.actorOf(Props[User], "User")
  qa ! Start(store)

}
