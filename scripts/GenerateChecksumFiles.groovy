#!/usr/bin/env groovy
import java.security.MessageDigest; // Checksum methods
import static groovy.io.FileType.FILES
import groovy.util.CliBuilder

/**
 * Commandline interface handler for the ChecksumGenerator
 * <p/>
 * usage: generate-checksum-files.groovy [options]
 * -debug             debug logging
.* -folder <folder>   folder path to generate sums
 * -force             force generation even if file exists
 * -help              print this message
 * -md5               generate MD5 sum files
 * -recurse           recurse into folder
 * -sha1              generate SHA-1 sum files
 */
class CliHandler {
  def cliBuilder = new CliBuilder(usage: "GenerateChecksumFiles.groovy [options]")

  def folder = null
  def recurse = false
  def force = false
  def sha1 = false
  def md5 = false
  def debug = false

  CliHandler() {
    cliBuilder.help("print this message")
    cliBuilder.folder(args: 1, argName: "folder", "folder path to generate sums")
    cliBuilder.recurse("recurse into folder")
    cliBuilder.force("force generation even if file exists")
    cliBuilder.sha1("generate SHA-1 sum files")
    cliBuilder.md5("generate MD5 sum files")
    cliBuilder.debug("debug logging")
  }

  Boolean process(String[] args) {
    def options = cliBuilder.parse(args)

    // Validate options
    try {
      if(options.help) {
        cliBuilder.usage()
        return false
      }

      if(options.folder) {
        this.folder = options.folder
      }

      if(options.recurse) {
        this.recurse = options.recurse
      }

      if(options.sha1) {
        this.sha1 = options.sha1
      }

      if(options.md5) {
        this.md5 = options.md5
      }

      if(options.debug) {
        this.debug = options.debug
      }

      // At minimum, user must specify a folder and checksum type
      if(this.folder && (this.sha1 || this.md5)) {
        return true
      }

      if (!this.folder) {
        println "ERROR: Unspecified folder"
      }
      else if (!this.sha1 && !this.md5) {
        println "ERROR: Unspecified checksum type (-sha1 or -md5)"
      }

      cliBuilder.usage()
    } catch(java.lang.NullPointerException e) {
      // Bad arguments passed in. CliBuilder automatically handles this
    } catch(Exception e) {
      // Unexpected error
      e.printStackTrace()
    }

    return false
  }
}

class ChecksumGenerator {
  def folder = null
  def recurse = false
  def force = false
  def sha1 = false
  def md5 = false
  def debug = false

  /**
   * Calls the sha1 write sum to file methods.
   * @param file
   * @param force
   * @param debug
   * @return File containing the checksum
   */
  public void createChecksumFile(File file, boolean force, boolean debug) {
    if (sha1) {
      File sumsFile = new File(file.getAbsolutePath() + '.sum')
      if (!file.name.endsWith('.sum')) {
        if (force || !sumsFile.exists() || firstFileIsNewer(file, sumsFile)) {
          sumsFile.createNewFile()
          sumsFile.write("")      //empty the file if already exists
          writeSha1SumToFile(file, sumsFile, debug)
        }
      }
    }
    if (md5) {
      File md5File = new File(file.getAbsolutePath() + '.md5')
      if (!file.name.endsWith('.md5')) {
        if (force || !md5File.exists() || firstFileIsNewer(file, md5File)) {
          md5File.createNewFile()
          md5File.write("")      //empty the file if already exists
          writeMd5ToFile(file, md5File, debug)
        }
      }
    }

  }

  /**
   * Create SHA-1 checksum file
   * @param archiveFile
   * @param sumsFile
   * @param debug
   */
  private void writeSha1SumToFile(File archiveFile, File sumsFile, boolean debug) {
    def messageDigest = MessageDigest.getInstance("SHA1")
    archiveFile.eachByte(8388608) { byte[] buf, int bytesRead ->
      messageDigest.update(buf, 0, bytesRead);
    }
    def value = new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0').toString()
    if (debug) println "Writing sha1 ${value} to file ${sumsFile.getPath()}"
    sumsFile << "SHA1=" + value
    sumsFile << System.getProperty("line.separator")
  }

  /**
   * Create MD-5 checksum file
   * @param archiveFile
   * @param sumsFile
   * @param debug
   */
  private void writeMd5ToFile(File archiveFile, File sumsFile, boolean debug) {
    def messageDigest = MessageDigest.getInstance("MD5")
    archiveFile.eachByte(8388608) { byte[] buf, int bytesRead ->
      messageDigest.update(buf, 0, bytesRead);
    }
    def value = new BigInteger(1, messageDigest.digest()).toString(16).padLeft(32, '0').toString()
    if (debug) println "Writing md5 ${value} to file ${sumsFile.getPath()}"
    sumsFile << "MD5=" + value
    sumsFile << System.getProperty("line.separator")
  }

  private Boolean isChecksumableFile(File file) {
    def extensionsRegex = /(.*\.zip$|.*\.tar.gz$|.*\.exe$|.*\.jar$|.*\.bin$|.*\.pdf)/
    def name = file.getName()
    def matcher = (name =~ extensionsRegex)
    return matcher.matches()
  }

  /**
   *
   * @param first
   * @param second
   * @return true if first is newer than second
   */
  private Boolean firstFileIsNewer(File first, File second) {

    def firstDate = new Date(first.lastModified())
    def secondDate = new Date(second.lastModified())

    return (firstDate > secondDate)
  }

  public void generate(String folder) {
    def createCheckSumClosure = {
      if (isChecksumableFile(it)) {
        println "${it}"
        createChecksumFile(it, this.force, this.debug)
      }
    }

    File dir = new File(folder)
    if(this.recurse) {
      println "Recursively creating checksum for ${folder}..."
      dir.eachFileRecurse(FILES, createCheckSumClosure)
    }
    else {
      println "Creating checksum for ${folder}..."
      dir.eachFile(FILES, createCheckSumClosure)
    }
  }

}

// Process command line arguments
CliHandler cli = new CliHandler()
if(!cli.process(args)) {
  System.exit(1)
}

File dir = new File(cli.folder)
if (!dir.exists()) {
  println "ERROR: Folder does not exist: " + cli.folder
  System.exit(1)
}

ChecksumGenerator generator = new ChecksumGenerator()
generator.recurse = cli.recurse
generator.force = cli.force
generator.sha1 = cli.sha1
generator.md5 = cli.md5
generator.debug = cli.debug
generator.generate(cli.folder)
