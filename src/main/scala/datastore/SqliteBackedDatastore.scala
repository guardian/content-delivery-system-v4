package datastore
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/*https://bitbucket.org/xerial/sqlite-jdbc*/

/**
  * Created by localhome on 27/10/2016.
  */
class SqliteBackedDatastore(params:Map[String,String]) extends Datastore {
  val databasePath = params("databasepath") + "/" + params("routename") + ".db"
  val db = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")

  override def createNewDatastore(params: Map[String, String]) = Future {
    val st = db.createStatement()
    st.setQueryTimeout(30)

    try {
      st.executeUpdate("CREATE TABLE sources (id integer primary key autoincrement,type,provider_method,ctime,filename,filepath)")
      st.executeUpdate("CREATE TABLE meta (id integer primary key autoincrement,source_id,key,value)")
      st.executeUpdate("CREATE TABLE system (schema_version,cds_version)")
      st.executeUpdate("CREATE TABLE tracks (id integer primary key autoincrement,source_id,track_index,key,value)")
      st.executeUpdate("CREATE TABLE media (id integer primary key autoincrement,source_id,key,value)")
      st.executeUpdate("INSERT INTO system (schema_version,cds_version) VALUES (1.0,4.0)")
      true
    } catch {
      case e:SQLException=>
        println("Unable to set up datastore: " +e)
        false
    }
  }

  override def close(): Unit = db.close()

  override def setMulti(section: String, params: Map[String, String]): Future[List[Boolean]] = Future {
    List()
  }

  override def getMulti(section: String, keys: List[String]): Future[Map[String, String]] = Future {
    Map()
  }
}
