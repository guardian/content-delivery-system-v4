import org.scalatest.{FlatSpec, Matchers}
import CDS.FileCollection
import java.net.URI

/**
  * Created by localhome on 11/11/2016.
  */
class FileCollectionSpec extends FlatSpec with Matchers {
  "A CDS.FileCollection" should "read values in from a tempfile" in {
    val u = new URI("file:///path/to/datastore.db")
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some(u))

    fc.length should be (1)

    fc.head should be (FileCollection("mediafile","inmetafile","metafile","xmlfile",u,fc.head.tempFile))
  }

  it should "read values from commandline argument map" in {
    val u = new URI("file:///path/to/datastore.db")
    val info = Map('media->"/path/to/mediafile",'inmeta->"/path/to/inmetafile",
                   'meta->"/path/to/metafile",'xml->"/path/to/xmlfile")
    val fc = FileCollection.fromOptionMap(info,u)
    fc.mediaFile should be ("/path/to/mediafile")
    fc.inmetaFile should be ("/path/to/inmetafile")
    fc.metaFile should be ("/path/to/metafile")
    fc.xmlFile should be ("/path/to/xmlfile")
  }

  it should "update by returning a new collection with moodified values" in {
    val u = new URI("file:///path/to/datastore.db")
    val fc_orig = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some(u))

    fc_orig.length should be (1)

    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicupdate",Some(fc_orig.head),None)
    fc.length should be (1)

    fc.head should be (FileCollection("mediafile","inmetafile","metafile","/path/to/newxmlfile",u,fc.head.tempFile))
  }

  it should "read batch mode values in from a tempfile" in {
    val u = new URI("file:///path/to/datastore.db")
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/batchoutput",None,Some(u))
    fc.length should be (4)
    fc.head should be (FileCollection("mediafile1","inmetafile1","","",u,fc.head.tempFile))
    fc(1) should be (FileCollection("mediafile2","inmetafile2","","",u,fc(1).tempFile))
    fc(2) should be (FileCollection("mediafile3","inmetafile3","","xmlfile3",u,fc(2).tempFile))
    fc(3) should be (FileCollection("","","metafile4","xmlfile4",u,fc(3).tempFile))
  }

  it should "return a map of environment variables to set" in {
    val u = new URI("file:///path/to/datastore.db")
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some(u))
    val reqd = Seq("media","inmeta","meta","xml","randominvalidthing")
    fc.length should be (1)
    fc.head.getEnvironmentMap(reqd) should be (Map(
      "cf_media_file"->"mediafile",
      "cf_inmeta_file"->"inmetafile",
      "cf_meta_file"->"metafile",
      "cf_xml_file"->"xmlfile",
      "cf_datastore_location"->"/path/to/datastore.db",
      "cf_temp_file"->fc.head.tempFile
    ))
  }

  it should "throw a runtime exception if the datastore location is not a file:// uri" in {
    val u =new URI("http://server/some_http/path?something")
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some(u))
    val reqd = Seq("media","inmeta","meta","xml","randominvalidthing")
    fc.length should be (1)

    assertThrows[RuntimeException] {
      fc.head.getEnvironmentMap(reqd)
    }
  }

  it should "validate whether it contains certain file references" in {
    val u = new URI("file:///path/to/datastore.db")
    val info = Map('media->"/path/to/mediafile",'inmeta->"/path/to/inmetafile",
      'meta->"/path/to/metafile",'xml->"/path/to/xmlfile")
    val fc = FileCollection.fromOptionMap(info,u)

    fc.hasFiles(Seq("media","inmeta","meta","xml")) should be (true)
    fc.hasFiles(Seq("media")) should be (true)
    fc.hasFiles(Seq("inmeta")) should be (true)
    fc.hasFiles(Seq("meta")) should be (true)
    fc.hasFiles(Seq("xml")) should be (true)

    val fc2 = FileCollection.fromOptionMap(Map('media->"/path/to/mediafile"),u)
    fc2.hasFiles(Seq("media","inmeta","meta","xml")) should be (false)
    fc2.hasFiles(Seq("media")) should be (true)
    fc2.hasFiles(Seq("inmeta")) should be (false)
    fc2.hasFiles(Seq("meta")) should be (false)
    fc2.hasFiles(Seq("xml")) should be (false)
  }
}