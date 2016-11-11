import org.scalatest.{FlatSpec, Matchers}
import CDS.FileCollection
/**
  * Created by localhome on 11/11/2016.
  */
class FileCollectionSpec extends FlatSpec with Matchers {
  "A CDS.FileCollection" should "read values in from a tempfile" in {
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some("/path/to/datastore.db"))

    fc.length should be (1)

    fc.head should be (FileCollection("mediafile","inmetafile","metafile","xmlfile","/path/to/datastore.db",fc.head.tempFile))
  }

  it should "update by returning a new collection with moodified values" in {
    val fc_orig = FileCollection.fromTempFile("src/test/resources/tempfiles/basicoutput",None,Some("/path/to/datastore.db"))

    fc_orig.length should be (1)

    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/basicupdate",Some(fc_orig.head),None)
    fc.length should be (1)

    fc.head should be (FileCollection("mediafile","inmetafile","metafile","/path/to/newxmlfile","/path/to/datastore.db",fc.head.tempFile))
  }

  it should "read batch mode values in from a tempfile" in {
    val fc = FileCollection.fromTempFile("src/test/resources/tempfiles/batchoutput",None,Some("/path/to/datastore.db"))
    fc.length should be (4)
    fc.head should be (FileCollection("mediafile1","inmetafile1","","","/path/to/datastore.db",fc.head.tempFile))
    fc(1) should be (FileCollection("mediafile2","inmetafile2","","","/path/to/datastore.db",fc(1).tempFile))
    fc(2) should be (FileCollection("mediafile3","inmetafile3","","xmlfile3","/path/to/datastore.db",fc(2).tempFile))
    fc(3) should be (FileCollection("","","metafile4","xmlfile4","/path/to/datastore.db",fc(3).tempFile))
  }
}
