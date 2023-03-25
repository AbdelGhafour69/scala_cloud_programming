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
import scala.collection.mutable

class LRUCache {
  val hashSet = mutable.LinkedHashSet.empty[String]
  val map = scala.collection.mutable.HashMap.empty[String, String]
  def store(
      key: String,
      value: String,
      capacity: Int
  ) = {
    if (hashSet contains key) {
      hashSet.remove(key)
      hashSet.addOne(key)
      map += key -> value
      // hashSet
    } else {
      if (hashSet.size == capacity) {
        map -= hashSet.head
        hashSet.remove(hashSet.head)
        map += key -> value
        hashSet.add(key)
        // hashSet
      } else {
        map += key -> value
        hashSet.add(key)
        // hashSet
      }
    }
  }
  def delete(key: String) = {

    if (hashSet contains key) {
      hashSet.remove(key)
      // hashSet
    }
    map -= key
  }
}

class CacheAgent extends Actor {
  val map = scala.collection.mutable.HashMap.empty[String, String]
  var lru = new LRUCache()
  val ma = context.actorOf(Props[MapAgent], "MapAgent")
  val capacity = 50
  implicit val to = Timeout(10 seconds)
  import Messages._

  override def receive: Receive = {
    case Store(key: String, value: String) =>
      lru.store(key, value, capacity)
      val future = (ma ? Store(key, value))
      Await.result(future, 5 seconds)
      sender ! "Store done"
    // println("Cache agent map " + map.toString)
    case Delete(key: String) =>
      lru.delete(key)
      val future = (ma ? Delete(key))
      Await.result(future, 5 seconds)
      sender ! "Delete done"
    // println("Cache agent map " + map.toString)
    case Lookup(key: String, store: ActorRef) =>
      val res = lru.map.get(key) match {
        case None            => None
        case Some(s: String) => s
      }

      if (res == None) {
        val future = (ma ? Lookup(key, self))
        val kvres = Await.result(future, 5 seconds)

        sender ! kvres
        kvres match {
          case LookupResponse(value, ar) => map += key -> value
          case _                         =>
        }
        // println("Cache agent map " + map.toString)
      } else { sender ! LookupResponse(res.toString, self) }
  }
}
