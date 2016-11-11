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

  def close:Boolean = {
    try {
      val f = new java.io.File(tempFile)
      f.delete()
      true
    } catch {
      case _:Throwable=>
        false
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
        case r"\s*([^=]+)${key}\s*=(.*)${value}"=>Some((key,value))
        case _=>None
    }).filter(_ match {
      case Some(tuple)=>true
      case None=>false
    }).map(_.get)
        .toMap

    src.close()

    println(s"tmpfiledata got $tmpfiledata")

    if(tmpfiledata.contains("batch")){
      println("batch mode detected")
      val newsrc = Source.fromFile(tempFile)

      newsrc.getLines().map({
        case r"\s*([^,]*)${media},([^,]*)${inmeta},([^,]*)${meta},([^,]*)${xml}\s*"=>
          previous match {
            case Some(fileCollection)=>
              Some(fileCollection.replace(Some(media),Some(inmeta),Some(meta),Some(xml)))
            case None=>
              val newtempfile = java.io.File.createTempFile("cds_",".tmp",tempFileDir)
              Some(FileCollection(media,inmeta,meta,xml,newDatastoreLocation.get,newtempfile.getAbsolutePath))
          }
        case _=>None
      }).filter(_ match {
        case Some(fc)=>true
        case None=>false
      }).map(_.get)
        .toList
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