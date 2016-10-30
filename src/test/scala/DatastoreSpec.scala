import org.scalatest._
import datastore.SqliteBackedDatastore
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}

import scala.concurrent.Await
import scala.concurrent.duration.{Deadline, Duration}

class DatastoreSpec extends FlatSpec with Matchers{
  val dsProps = Map("databasepath"->"/tmp","routename"->"test")

  "An SQLiteBackedDatastore" should "create a new datastore file" in {
    val store = new SqliteBackedDatastore(dsProps)
    Await.result(store.createNewDatastore(Map()),Duration(1,"second"))
  }

  it should "create a new source ID for an unrecongnised source" in {
    val store = new SqliteBackedDatastore(dsProps)
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "return the same source ID for the next try" in {
    val store = new SqliteBackedDatastore(dsProps)
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "add a value to the store" in {
    val store = new SqliteBackedDatastore(dsProps)
    val f = store.setMulti("meta",Map("food"->"spam","accompaniament"->"eggs","pudding"->"more eggs"),"test")
    ScalaFutures.whenReady(f){
      _.foreach(x=>assert(x)) //assert that True is returned in each element of the list
    }
  }

  it should "retrieve the values that were previously set" in {
    val store = new SqliteBackedDatastore(dsProps)
    val f = store.getMulti("meta",List("food","accompaniament","pudding"))
    ScalaFutures.whenReady(f){ resultMap=>
      assert(resultMap("food")=="spam")
      assert(resultMap("accompaniament")=="eggs")
      assert(resultMap("pudding")=="more eggs")
    }
  }

  it should "substitute meta portions into a string" in {
    val store = new SqliteBackedDatastore(dsProps)
    val f = store.substituteString("I want {meta:food} with {meta:accompaniament}! Not {meta:pudding}.")
    ScalaFutures.whenReady(f){ subbedString=>
      assertResult(subbedString)("I want spam with eggs! Not more eggs.")
    }
  }
}
