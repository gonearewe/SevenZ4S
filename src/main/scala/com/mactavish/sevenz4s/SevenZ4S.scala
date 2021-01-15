import java.io.{FileNotFoundException, IOException, RandomAccessFile}
import java.{lang, util}

import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.{ArchiveFormat, IArchiveOpenCallback, IArchiveOpenVolumeCallback, IInStream, PropID, SevenZip, SevenZipException}


object OpenMultipartArchiveRar {

  def main(args: Array[String]): Unit = {
    val archiveOpenVolumeCallback = new OpenMultipartArchiveRar.ArchiveOpenVolumeCallback
    val inStream = archiveOpenVolumeCallback.getStream("sevenz4s.part1.rar")
    val inArchive = SevenZip.openInArchive(ArchiveFormat.RAR, inStream, archiveOpenVolumeCallback)
    System.out.println("   Size   | Compr.Sz. | Filename")
    System.out.println("----------+-----------+---------")
    val itemCount = inArchive.getNumberOfItems
    for (i <- 0 until itemCount) {
      System.out.println(String.format("%9s | %9s | %s", inArchive.getProperty(i, PropID.SIZE), inArchive.getProperty(i, PropID.PACKED_SIZE), inArchive.getProperty(i, PropID.PATH)))
    }
  }

  private class ArchiveOpenVolumeCallback extends IArchiveOpenVolumeCallback with IArchiveOpenCallback {
    /**
     * Cache for opened file streams
     */
    private val openedRandomAccessFileList = new util.HashMap[String, RandomAccessFile]
    /**
     * Name of the last volume returned by
     */
    private var name: String = _

    /**
     * This method should at least provide the name of the last
     * opened volume (propID=PropID.NAME).
     *
     * @see IArchiveOpenVolumeCallback#getProperty(PropID)
     */
    @throws[SevenZipException]
    override def getProperty(propID: PropID): Any = {
      propID match {
        case PropID.NAME =>
          return name
      }
      null
    }

    /**
     * The name of the required volume will be calculated out of the
     * name of the first volume and a volume index. In case of RAR file,
     * the substring ".partNN." in the name of the volume file will
     * indicate a volume with id NN. For example:
     * <ul>
     * <li>test.rar - single part archive or multi-part archive with
     * a single volume</li>
     * <li>test.part23.rar - 23-th part of a multi-part archive</li>
     * <li>test.part001.rar - first part of a multi-part archive.
     * "00" indicates, that at least 100 volumes must exist.</li>
     * </ul>
     */
    @throws[SevenZipException]
    override def getStream(filename: String): IInStream = try { // We use caching of opened streams, so check cache first
      var randomAccessFile = openedRandomAccessFileList.get(filename)
      if (randomAccessFile != null) { // Cache hit.
        // Move the file pointer back to the beginning
        // in order to emulating new stream
        randomAccessFile.seek(0)
        // Save current volume name in case getProperty() will be called
        name = filename
        return new RandomAccessFileInStream(randomAccessFile)
      }
      // Nothing useful in cache. Open required volume.
      randomAccessFile = new RandomAccessFile(filename, "r")
      // Put new stream in the cache
      openedRandomAccessFileList.put(filename, randomAccessFile)
      name = filename
      new RandomAccessFileInStream(randomAccessFile)
    } catch {
      case fileNotFoundException: FileNotFoundException =>
        // Required volume doesn't exist. This happens if the volume:
        // 1. never exists. 7-Zip doesn't know how many volumes should
        //    exist, so it have to try each volume.
        // 2. should be there, but doesn't. This is an error case.
        // Since normal and error cases are possible,
        // we can't throw an error message
        null // We return always null in this case

      case e: Exception =>
        throw new RuntimeException(e)
    }

    /**
     * Close all opened streams
     */
    @throws[IOException]
    def close(): Unit = {
    }

    override def setTotal(aLong: lang.Long, aLong1: lang.Long): Unit = {}

    override def setCompleted(aLong: lang.Long, aLong1: lang.Long): Unit = {}
  }

}
