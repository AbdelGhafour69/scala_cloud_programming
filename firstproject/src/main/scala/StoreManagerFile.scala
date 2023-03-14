import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.util.Random._
import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import scala.util.control.Breaks._
import java.io._

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

    case _ =>
      println("Do nothing")
  }
}

class StoreManager extends Actor {

  import Messages._

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      add_to_file("Store " + key + " " + value + "\n")
    case Delete(key: String) =>
      add_to_file("Delete " + key + "\n")
    case Lookup(key: String, user: ActorRef) =>
      print()
  }
  def add_to_file(action: String) = {
    val fw = new FileWriter("../actions.txt", true)
    try {
      fw.write(action)
    } finally fw.close()
  }
}

object StoreManagerFile extends App {
  import Messages._

  val as = ActorSystem("FileSystem")
  val store = as.actorOf(Props[StoreManager], "StoreManager")
  val qa = as.actorOf(Props[UserFile], "User")
  qa ! Start(store)

}
