import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip
{
    Boolean quiet = false
    public static void main( String[] args )
    {
      if (args.length != 2)
      {
        println "Unzip <zip archive> <destination folder>"
        return;
      }

      Unzip unzipper = new Unzip()
      unzipper.unzip(args[0],args[1])
    }

    def log(def string) {
      if (!quiet) {
        println string
      }
    }

    /**
     * Unzip a file path to a destination folder
     * @param zipPath input zip file
     * @param destinationFolder zip file output folder
     */
    public void unzip(String zipPath, String destinationFolder)
    {
     byte[] buffer = new byte[4096]

     try
     {
       // Create destination folder if it does not exist
       File folder = new File(destinationFolder)
       if (!folder.exists()) {
         folder.mkdir()
       }

       ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zipPath))
       ZipEntry entry = inputStream.getNextEntry()

       while ( entry != null ) {
         String fileName = entry.getName()
         File newFile = new File(destinationFolder, fileName)

         log("Unzipping " + newFile.getAbsoluteFile() + " ...")

         // Create all intermediate folders
         File parentFolder = new File(newFile.getParent())
         parentFolder.mkdirs()

         if (entry.isDirectory()) {
           newFile.mkdirs()
         }
         else {
           int len;
           FileOutputStream outputStream = new FileOutputStream(newFile)
           while ((len = inputStream.read(buffer)) > 0) {
             outputStream.write(buffer, 0, len)
           }
           outputStream.close()
         }

         entry = inputStream.getNextEntry()
       }

       inputStream.closeEntry()
       inputStream.close()
    } catch( IOException ex ){
       ex.printStackTrace()
    }
   }
}
