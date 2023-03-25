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
import scala.util.Random.nextInt
import scala.util.Random.between

class RandomUser extends Actor {
  import Messages._
  var last = "1"
  var iter = 0
  override def receive: Receive = {

    case Populate() =>
      for (w <- 1 to 500) {
        add_to_file("Store " + w + " " + w + "\n")
      }

    case Start(store: ActorRef) =>
      for (iter <- iter until 5000) {
        nextInt(3) match {
          case 0 =>
            var key_value = between(501, 1000)
            store ! Store(key_value.toString, key_value.toString)
          case 1 =>
            if (nextInt(2) == 1) {
              store ! Lookup(last, self)
            } else {
              var key = 1
              if (between(1, 10) <= 7) {
                key = between(1, 500)
              } else {
                key = between(501, 1000)
              }
              last = key.toString
              store ! Lookup(key.toString, self)
            }

          case 2 =>
            var key = between(501, 1000)
            store ! Delete(key.toString)
        }
      }

    case LookupResponse(value: String, store: ActorRef) =>
      println(value)
    // self ! Start(store)

    case LookupError(value: String, store: ActorRef) =>
      println(value)
    // self ! Start(store)
  }
  def add_to_file(action: String) = {
    val fw = new FileWriter("../actions.txt", true)
    try {
      fw.write(action)
    } finally fw.close()
  }
}
