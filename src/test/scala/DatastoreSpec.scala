import org.scalatest._
import datastore.SqliteBackedDatastore
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}

import scala.concurrent.Await
import scala.concurrent.duration.{Deadline, Duration}

class DatastoreSpec extends FlatSpec with Matchers{
  "An SQLiteBackedDatastore" should "create a new datastore file" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    Await.result(store.createNewDatastore(Map()),Duration(1,"second"))
  }

  it should "create a new source ID for an unrecongnised source" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "return the same source ID for the next try" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "add a value to the store" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    val f = store.setMulti("meta",Map("food"->"spam","accompaniament"->"eggs","pudding"->"more eggs"),"test")
    ScalaFutures.whenReady(f){
      _.foreach(x=>assert(x)) //assert that True is returned in each element of the list
    }
  }

  it should "retrieve the values that were previously set" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    val f = store.getMulti("meta",List("food","accompaniament","pudding"))
    ScalaFutures.whenReady(f){ resultMap=>
      assert(resultMap("food")=="spam")
      assert(resultMap("accompaniament")=="eggs")
      assert(resultMap("pudding")=="more eggs")
    }
  }
}
