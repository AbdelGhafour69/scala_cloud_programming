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

class MapAgent extends Actor {
  val map = scala.collection.mutable.HashMap.empty[String, String]
  implicit val to = Timeout(10 seconds)
  import Messages._

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      map += key -> value
      add_to_file("Store " + key + " " + value + "\n")
      sender ! "Store done"
    // println("Map agent map " + map.toString)
    case Delete(key: String) =>
      map -= key
      add_to_file("Delete " + key + "\n")
      sender ! "Delete done"
    // println("Map agent map " + map.toString)
    case Lookup(key: String, store: ActorRef) =>
      val res = map.get(key) match {
        case None            => None
        case Some(s: String) => s
      }

      if (res == None) {
        val filename = "../actions.txt"
        val lines = Source.fromFile(filename).getLines().toSeq.reverse
        val found = false
        breakable {
          lines.foreach(line => {
            val splitline: List[String] = line.split(" ").map(_.trim).toList
            if (splitline(1).equals(key) && splitline(0).equals("Store")) {
              map += key -> splitline(2).toString
              sender ! LookupResponse(splitline(2), self)
              // println("Cache agent map " + map.toString)
              found -> true
              break
            }
            if (splitline(1).equals(key)) {
              sender ! LookupError(s"File with key $key was deleted !", self)
              found -> true
              break
            }
          })
          if (found == false) {
            sender ! LookupError("File not found !", self)
          }
        }
      } else { sender ! LookupResponse(res.toString, self) }
  }

  def add_to_file(action: String) = {
    val fw = new FileWriter("../actions.txt", true)
    try {
      fw.write(action)
    } finally fw.close()
  }
}
