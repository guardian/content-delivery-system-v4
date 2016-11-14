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
}
