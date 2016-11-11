package CDS
import scala.io.Source

case class FileCollection(mediaFile:String,inmetaFile:String,metaFile:String,xmlFile:String,dsLocation:String,tempFile:String) {
  def replace(xmediaFile:Option[String],xinmetaFile:Option[String],xmetaFile:Option[String],xxmlFile:Option[String]):FileCollection = {
    val newMediaFile = xmediaFile match {
      case Some(filename)=>filename
      case None=>mediaFile
    }
    val newInmetaFile = xinmetaFile match {
      case Some(filename)=>filename
      case None=>inmetaFile
    }
    val newMetaFile = xmetaFile match {
      case Some(filename)=>filename
      case None=>metaFile
    }
    val newXmlFile = xxmlFile match {
      case Some(filename)=>filename
      case None=>xmlFile
    }
    FileCollection(newMediaFile,newInmetaFile,newMetaFile,newXmlFile,dsLocation,tempFile)
  }

  def close = {
    try {
      val f = new java.io.File(tempFile)
      f.delete()
    } catch {
      case _:Throwable=>
        println("Unable to delete tempfile")
    }
  }

  def tempFilePath = tempFile
}

object FileCollection {
  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  def fromTempFile(tempFile:String,previous:Option[FileCollection],newDatastoreLocation:Option[String]):List[FileCollection] = {
    val src = Source.fromFile(tempFile)

    val tempFileDir=new java.io.File("/tmp/cds_backend")

    tempFileDir.mkdirs()
    val tmpfiledata = src.getLines().map(_ match {
        case r"\s*([^=]+)${key}\s*=(.*)${value}"=>(key,value)
    }).toMap

    println(s"tmpfiledata got $tmpfiledata")

    if(tmpfiledata.contains("batch")){
      println("batch mode detected")

      src.getLines().map(_ match {
        case r"\s*([^,]+)${media},([^,]+)${inmeta},([^,]+)${meta},([^,]+)${xml}.*"=>
        previous match {
          case Some(fileCollection)=>
            fileCollection.replace(Some(media),Some(inmeta),Some(meta),Some(xml))
          case None=>
            val newtempfile = java.io.File.createTempFile("cds_",".tmp",tempFileDir)
            FileCollection(media,inmeta,meta,xml,newDatastoreLocation.get,newtempfile.getAbsolutePath)
        }
      }).toList

    } else {

      val paramList:List[Option[String]] = List("cf_media_file","cf_inmeta_file","cf_meta_file","cf_xml_file")
        .map(filetype=>{
          if(tmpfiledata.contains(filetype)){
            Some(tmpfiledata(filetype))
          } else {
            None
          }
        })
      previous match {
        case Some(filecollection)=>List(filecollection.replace(paramList(0),paramList(1),paramList(2),paramList(3)))
        case None=>
          val newtempfile = java.io.File.createTempFile("cds_",".tmp",tempFileDir)
          List(FileCollection(paramList(0).getOrElse(""),
            paramList(1).getOrElse(""),
            paramList(2).getOrElse(""),
            paramList(3).getOrElse(""),
            newDatastoreLocation.get,
            newtempfile.getAbsolutePath)
          )
      }
    }

  }
}