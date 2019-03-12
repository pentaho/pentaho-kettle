import Unzip;

import java.nio.file.Files
import java.nio.file.Path;
import static groovy.io.FileType.FILES

public class UnEULAWrap
{
    public Boolean quiet = false
    public static void main( String[] args )
    {
      if (args.length != 2)
      {
        println "UnEULAWrap <zip archive> <destination folder>"
        return;
      }

      UnEULAWrap unEULAWrap = new UnEULAWrap()
      unEULAWrap.quiet = true
      unEULAWrap.removeEULAWrap(args[0],args[1])
    }

    def log(def string) {
      if (!quiet) {
        println string
      }
    }

    def findFileRecursive( def folderName, def fileName ) {
      def foundFile
      def folder = new File(folderName)
      folder.eachFileRecurse(FILES) {
          if(it.name == fileName) {
              foundFile = it
          }
      }

      return foundFile
    }

    def executeCommand( def command ) {
      def sout = new StringBuilder(), serr = new StringBuilder()
      def proc = command.execute()
      proc.consumeProcessOutput(sout, serr)
      proc.waitForOrKill(10000)
      log("$sout")
      if (serr.size() > 0)
        log("$serr")
    }

    /**
     * Uneulawrap a zip file to a destination folder
     * @param zipPath input zip file
     * @param destinationFolder zip file output folder
     */
    public void removeEULAWrap(String zipPath, String destinationFolder)
    {
     try
     {
       // Create a temporary directory to expand the .zip file
       Path tempFolder = Files.createTempDirectory("eula-wrap-")

       // Unzip the IzPack installer to a temp folder
       Unzip unzipper = new Unzip()
       unzipper.quiet = quiet
       unzipper.unzip(zipPath, tempFolder.toString())

       def izPackInstallerJar = findFileRecursive(tempFolder.toString(), "installer.jar")
       if (!izPackInstallerJar) {
         println "ERROR: installer.jar not found in archive"
         tempFolder.deleteDir()
         return;
       }

       def izPackInstallerCommand = "java -DINSTALL_PATH=${destinationFolder} -DEULA_ACCEPT=true -jar ${izPackInstallerJar.absolutePath} -options-system"
       log(izPackInstallerCommand)
       executeCommand(izPackInstallerCommand)

       // Create destination folder if it does not exist
       File folder = new File(destinationFolder)
       if (!folder.exists()) {
         folder.mkdir()
       }

       // Remove temporary folder
       tempFolder.deleteDir()

    } catch( IOException ex ){
       ex.printStackTrace()
    }
   }
}
