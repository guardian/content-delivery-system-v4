import org.scalatest._

import datastore.SqliteBackedDatastore

class DatastoreSpec extends FlatSpec with Matchers{
  "An SQLiteBackedDatastore" should "create a new datastore file" in {
    val store = new SqliteBackedDatastore(Map("databasepath"->"/tmp","routename"->"test"))
    store.createNewDatastore(Map())
  }
}
