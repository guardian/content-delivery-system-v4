package datastore

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by localhome on 27/10/2016.
  */
trait Datastore {
  def createNewDatastore(params:Map[String,String]):Future[Boolean]
  def close():Unit
  def setMulti(section:String, params:Map[String,String], whoami:String):Future[List[Boolean]]
  def getMulti(section:String, keys:List[String]):Future[Map[String,String]]

  def set(section:String, key:String, value:String, whoami:String):Future[Boolean] =
    setMulti(section,Map(key->value),whoami).flatMap((x:List[Boolean])=>Future(x.head))

  def setSync(section:String, key: String, value: String, whoami:String, timeout: Duration):Boolean =
    Await.result(set(section,key,value,whoami), timeout).asInstanceOf[Boolean]

  def get(section:String, key: String):Future[String] =
    getMulti(section,List(key)).flatMap((x:Map[String,String])=>Future(x.head._2))

  def getSync(section:String, key: String, timeout: Duration):String =
    Await.result(get(section,key), timeout)

  /*http://stackoverflow.com/questions/4636610/how-to-pattern-match-using-regular-expression-in-scala*/
  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  def substituteString(input:String):Future[String] = {
    def nextSubstitution(string:String,futures:List[Future[(String,String)]]):List[Future[(String,String)]] = {
      println(s"string is '$string'")
      val start = string indexOf "{"
      if(start == -1){
        println("no more subs")
        return futures
      }
      val end = string indexOf "}"
      val identifier = string.substring(start,end+1)

      println(s"identifier is $identifier")
//      val parts = identifier.split(":").map(_.trim)

      val to_sub = identifier match {
        case r"\{(\w+)$section:(\w+)${key}\}$$"=>get(section,key)
        case r"^\{(.+)${key}\}$$"=>get("core",key)
        case _=>Future("(invalid substitution)")
      }
//      val to_sub = if(parts.length==1){
//        get("core",parts.head)
//      } else {
//        get(parts.head,parts(1))
//      }

      //return an updated version of the input Futures list, including a new one for this substitution
      val newfutures = futures :+ to_sub.map(x=>(identifier,x))
      nextSubstitution(string.substring(end+2),newfutures)
    }

    //build the whole lot into a string, returned as a Future
    //Future.sequence(nextSubstitution("blah",List())).map(_.mkString)
    Future.sequence(nextSubstitution(input,List()))
      .map(_.foldLeft(input)((str:String,subtuple:(String,String))=>{
        println(s"replacing ${subtuple._1} with ${subtuple._2}")
        str.replace(subtuple._1,subtuple._2)
      }))

  }
}


/* "A have got a {media:something} with a {meta:something_else} and a {config:whatsit}" */

/* Await.result(store.substituteString("my {meta:info} string"), delay) */