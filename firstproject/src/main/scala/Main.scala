import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.util.Random._
import scala.collection.mutable.HashMap
import scala.io.StdIn.readLine
import scala.util.control.Breaks._
// object Messages {
//   case class Done(result: Int)
//   case class GetFibNumber(input: Int)
//   case class Start(actorRef: ActorRef)
// }

// class FibResonder extends Actor {
//   import Messages._
//   override def receive: Receive = {
//     case GetFibNumber(n) =>
//       val fn = fibonacci(n)
//       /* Thread.sleep(2000) */
//       sender ! Done(fn)
//     case _ =>
//       println("Erroneous message")
//   }

//   def fibonacci(n: Int): Int = n match {
//     case 0 | 1 => 1
//     case _     => fibonacci(n - 1) + fibonacci(n - 2)
//   }
// }

// class User extends Actor {
//   val map = scala.collection.mutable.HashMap.empty[Int, String]
//   println(map)
//   import Messages._

//   override def receive: Receive = {
//     case Start(ar: ActorRef) =>
//       val x = nextInt(10)
//       println(s"get Fibo for $x")
//       ar ! GetFibNumber(x)
//     case Done(n: Int) =>
//       println(s" The answer was $n")
//   }
// }

object Messages {
  case class Store(key: String, value: String)
  case class Lookup(key: String)
  case class Delete(key: String)
  case class Start(ar: ActorRef)
}

class User extends Actor {
  import Messages._
  override def receive: Receive = {

    case Start(store: ActorRef) =>
      breakable {
        while (true) {
          print("Enter your request")
          val request = readLine()
          println(s"Your request is $request")
          break
        }
      }
    case _ =>
      println("wssuup")
  }
}

class Store_Manager extends Actor {
  val map = scala.collection.mutable.HashMap.empty[String, String]
  println(map)
  import Messages._

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      println("Not implemented Store")
    case Delete(key: String) =>
      println("Not implemented Delete")

    case Lookup(key: String) =>
      println("Not implemented Lookup")

  }
}

object File_System extends App {
  import Messages._

  val as = ActorSystem("File_System")
  val store = as.actorOf(Props[Store_Manager], "Store_manager")
  val qa = as.actorOf(Props[User], "User")
  qa ! Start(store)
  /* as.terminate() */
}
