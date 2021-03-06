package datastore
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.sql._

/*https://bitbucket.org/xerial/sqlite-jdbc*/

/**
  * Created by localhome on 27/10/2016.
  */
class SqliteBackedDatastore(params:Map[String,String]) extends Datastore {
  val databasePath = params("databasepath") + "/" + params("routename") + ".db"

  override def createNewDatastore(params: Map[String, String]) = Future {
    //need to explicitly load the driver: https://bitbucket.org/xerial/sqlite-jdbc
    Class.forName("org.sqlite.JDBC")
    val db = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")
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
    } finally {
      db.close()
    }
  }

  override def close(): Unit = {}

  def getSourceIdSync(whoami:String,mytype:String):Integer = {
    val db = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")
    try {
      val st = db.prepareStatement("select id from sources where provider_method=?")
      st.setString(1, whoami)
      val resultSet = st.executeQuery()
      resultSet.next match {
        case false =>
          val insertst = db.prepareStatement("INSERT INTO sources (type,provider_method,ctime) values (?,?,?)")
          insertst.setString(1, mytype)
          insertst.setString(2, whoami)
          insertst.setString(3, new java.util.Date().toString)
          insertst.executeUpdate()

          val newGetSt = db.createStatement()
          val newResultSet = newGetSt.executeQuery("SELECT last_insert_rowid()")
          val r = newResultSet.getInt(1)
          db.close()
          r
        case true =>
          val r = resultSet.getInt(1)
          db.close()
          r
      }
    } finally {
      db.close()
    }
  }

  override def setMulti(section: String, params: Map[String, String], whoami:String): Future[List[Boolean]] = Future {
    val db = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")
    try {
      db.setAutoCommit(false)
      val sourceId = getSourceIdSync(whoami, "cds")

      val maybeSt: Option[PreparedStatement] = section match {
        case "meta" | "media" | "track" =>
          Some(db.prepareStatement(s"INSERT into $section (source_id,key,value) VALUES (?,?,?)"))
        case _ =>
          println(s"$section is not a valid datastore section")
          None
      }

      maybeSt match {
        case Some(st) =>
          val result = params.map(kvtuple =>
            try {
              st.setInt(1, sourceId)
              st.setString(2, kvtuple._1)
              st.setString(3, kvtuple._2)
              val r = st.executeUpdate()
              if (r == 1) {
                true
              } else {
                false
              }
            } catch {
              case e: SQLException =>
                println("-ERROR: " + e)
                false
            }
          ).toList
          db.commit()
          result
        case None=>
          List()
      }
    } finally {
      db.close()
    }
  }


  override def getMulti(section: String, keys: List[String]): Future[Map[String, String]] = Future {
    val db = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")
    try {
      val st = db.prepareStatement("SELECT value from meta where key=?")

      keys.map(k => {
        st.setString(1, k)
        val r = st.executeQuery()
        (k, r.getString(1))
      }
      ).toMap[String, String]
    } finally {
      db.close()
    }
  }
}
