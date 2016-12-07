package CDS
import scala.io.Source
import java.net.URI

case class FileCollection(mediaFile:String,inmetaFile:String,metaFile:String,xmlFile:String,dsLocation:URI,tempFile:String){
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

  def getEnvironmentMap(requiredFiles:Seq[String]):Map[String,String] = {
    if(dsLocation.getScheme != "file") throw new RuntimeException("Non file-based datastores are not yet supported.")
    requiredFiles.map({
      case "media"=>Some("cf_media_file"->mediaFile)
      case "inmeta"=>Some("cf_inmeta_file"->inmetaFile)
      case "meta"=>Some("cf_meta_file"->metaFile)
      case "xml"=>Some("cf_xml_file"->xmlFile)
      case _=>None
    }).filter({
      case Some(tuple)=>true
      case None=>false
    }).map(
      _.get
    ).toMap ++ Map(
      "cf_datastore_location"->dsLocation.getPath,
      "cf_temp_file"->tempFile
    )
  }
  
}

object FileCollection {
  val tempFileDir=new java.io.File("/tmp/cds_backend")

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  def fromOptionMap(optionMap:Map[Symbol,String],datastoreLocation:URI) = {
    tempFileDir.mkdirs()
    val newtempfile = java.io.File.createTempFile("cds_",".tmp",tempFileDir)

    FileCollection(
      optionMap.getOrElse('media,""),
      optionMap.getOrElse('meta,""),
      optionMap.getOrElse('inmeta,""),
      optionMap.getOrElse('xml,""),
      datastoreLocation,
      newtempfile.getAbsolutePath
    )
  }

  def readTempFileKeyValue(tempFile:String):Map[String,String] = {
    val src = Source.fromFile(tempFile)

    tempFileDir.mkdirs()
    val tmpfiledata = src.getLines().map({
      case r"\s*([^=]+)${key}\s*=(.*)${value}"=>Some((key,value))
      case _=>None
    }).filter(_ match {
      case Some(tuple)=>true
      case None=>false
    }).map(_.get)
      .toMap
    src.close()

    tmpfiledata
  }

  def readTempFileBatchModeCSV(tempFile:String,previous:Option[FileCollection],newDatastoreLocation:Option[URI],newTempFile:java.io.File):List[FileCollection] = {
    val newsrc = Source.fromFile(tempFile)

    newsrc.getLines().map({
      case r"\s*([^,]*)${media},([^,]*)${inmeta},([^,]*)${meta},([^,]*)${xml}\s*"=>
        previous match {
          case Some(fileCollection)=>
            Some(fileCollection.replace(Some(media),Some(inmeta),Some(meta),Some(xml)))
          case None=>
            Some(FileCollection(media,inmeta,meta,xml,newDatastoreLocation.get,newTempFile.getAbsolutePath))
        }
      case _=>None
    }).filter(_ match {
      case Some(fc)=>true
      case None=>false
    }).map(_.get)
      .toList
  }

  def getFilesFromKeyValueData(tmpfiledata:Map[String,String]):List[Option[String]] = {
    List("cf_media_file","cf_inmeta_file","cf_meta_file","cf_xml_file")
      .map(filetype=>{
        if(tmpfiledata.contains(filetype)){
          Some(tmpfiledata(filetype))
        } else {
          None
        }
      })
  }

  def fromTempFile(tempFile:String,previous:Option[FileCollection],newDatastoreLocation:Option[URI]):List[FileCollection] = {
    val tempFileDir=new java.io.File("/tmp/cds_backend")

    tempFileDir.mkdirs()
    val tmpfiledata = readTempFileKeyValue(tempFile)

    println(s"tmpfiledata got $tmpfiledata")

    if(tmpfiledata.contains("batch")){
      println("batch mode detected")

      val newtempfile = java.io.File.createTempFile("cds_",".tmp",tempFileDir)
      readTempFileBatchModeCSV(tempFile,previous,newDatastoreLocation,newtempfile)
    } else {
      val paramList:List[Option[String]] = getFilesFromKeyValueData(tmpfiledata)
        previous match {
        case Some(filecollection)=>
          //we have an existing filecollection to update
          List(filecollection.replace(paramList(0),paramList(1),paramList(2),paramList(3)))
        case None=>
          //we need to create a new filecollection
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