import org.scalatest._
import datastore.SqliteBackedDatastore
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}

import scala.concurrent.Await
import scala.concurrent.duration._

class DatastoreSpec extends FlatSpec with Matchers{
  val dsProps = Map("databasepath"->"/tmp","routename"->"test")
  val store = new SqliteBackedDatastore(dsProps) //creating a new Store for each test will create a new database as well

  "An SQLiteBackedDatastore" should "create a new datastore file" in {

    val response = Await.ready(store.createNewDatastore(Map()),1.second).value.get

    println(s"Got response $response, ${response.isSuccess}")
    assert(response.isSuccess)
  }

  it should "create a new source ID for an unrecongnised source" in {
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "return the same source ID for the next try" in {
    val sid = store.getSourceIdSync("testclient","core")
    assert(sid==1)
  }

  it should "add a value to the store" in {
    val f = store.setMulti("meta",Map("food"->"spam","accompaniament"->"eggs","pudding"->"more eggs"),"test")
    ScalaFutures.whenReady(f,PatienceConfiguration.Timeout(10 seconds)){
      _.foreach(x=>assert(x)) //assert that True is returned in each element of the list
    }
  }

  it should "retrieve the values that were previously set" in {
    val f = store.getMulti("meta",List("food","accompaniament","pudding"))
    ScalaFutures.whenReady(f){ resultMap=>
      assert(resultMap("food")=="spam")
      assert(resultMap("accompaniament")=="eggs")
      assert(resultMap("pudding")=="more eggs")
    }
  }

  it should "substitute meta portions into a string" in {
    val f = store.substituteString("I want {meta:food} with {meta:accompaniament}! Not {meta:pudding}.")
    ScalaFutures.whenReady(f){ subbedString=>
      assertResult(subbedString)("I want spam with eggs! Not more eggs.")
    }
  }
}
