import java.security.MessageDigest
import static groovy.io.FileType.FILES

/**
 * Created with IntelliJ IDEA.
 * User: LGrill
 * Date: 3/22/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */

final boolean forceChecksumGeneration = System.getenv()['FORCE_CHECKSUM_GENERATION'] != null && System.getenv()['FORCE_CHECKSUM_GENERATION'].equals("true")
if (forceChecksumGeneration) {
  println "Forcing checksum generation on all builds."
}
final boolean recurse = System.getenv()['RECURSE'] != null && System.getenv()['RECURSE'].equals("true")
final boolean debug = System.getenv()['DEBUG'] != null && System.getenv()['DEBUG'].equals("true")
final String undeletableBuilds = (System.getenv()['UNDELETABLE_BUILDS'] == null ? "" : System.getenv()['UNDELETABLE_BUILDS'])
final def undeletableBuildsList = undeletableBuilds.trim().split(',')
println "Un-deletable builds = ${undeletableBuildsList}"

String hostBaseUrl = System.getenv()['BASE_URL']  // Set this in the Jenkins Job 'http://release.pentaho.com/50/'
assert (hostBaseUrl != null)
hostBaseUrl = getStringWithoutTrailingSlash(hostBaseUrl)

String pageUrlRootDir = System.getenv()['DOWNLOAD_PAGE_URL_DIR'] // Relative path from base URL to the download page
assert (pageUrlRootDir != null)
pageUrlRootDir = getStringWithoutTrailingSlash(pageUrlRootDir)

String pageRootUrl = new String().format("%s/%s", hostBaseUrl, pageUrlRootDir)

String deploymentFolder = System.getenv()['DEPLOYMENT_FOLDER']
String title = deploymentFolder + ' Release Build';

String tableHeaderBackColor = '#1BE0B2'

String htmlHeaderData = "<!DOCTYPE html>\n" +
    "<!--[if lt IE 7]><html class=\"no-js lt-ie9 lt-ie8 lt-ie7\"> <![endif]-->\n" +
    "<!--[if IE 7]><html class=\"no-js lt-ie9 lt-ie8\"> <![endif]-->\n" +
    "<!--[if IE 8]><html class=\"no-js lt-ie9\"> <![endif]-->\n" +
    "<!--[if gt IE 8]><!--> <html class=\"no-js\"> <!--<![endif]-->\n" +
    "<html class=\"en\">\n" +
    "<head>\n" +
    "<title>${title}</title>\n" +
    "<meta http-equiv=\"content-language\" content=\"en-US\" />\n" +
    "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n" +
    "<meta name=\"baseURL\" content=\"${pageRootUrl}\" />\n" +
    "<link rel=\"icon\" href=\"http://build.pentaho.net/web/logo_icon.png\" type=\"image/png\" />\n" +
    "<link rel=\"shortcut icon\" href=\"http://build.pentaho.net/web/logo_icon.png\" type=\"image/png\" />\n" +
    "<link rel=\"alternate\" type=\"application/rss+xml\" href=\"http://feeds.feedburner.com/pentaho-releases\" />\n" +
    "<link rel=\"alternate\" type=\"application/rss+xml\" href=\"http://feeds.feedburner.com/pentaho-highlights\" />\n" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://build.pentaho.net/web/css/pages_2011.css?2052\" />\n" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.14/themes/base/jquery-ui.css\" />\n" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://build.pentaho.net/web/css/dialog.css?2011\" />\n" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://build.pentaho.net/web/css/forms.css?2011\" />\n" +
    "<link rel=\"stylesheet\" href=\"http://build.pentaho.net/web/css/master.css\">\n" +
    "\t<link rel=\"stylesheet\" href=\"//fonts.googleapis.com/css?family=Open+Sans:300\">\n" +
    "\t\n" +
    "\t<script>document.write(unescape('%3Cstyle type=\"text/css\" media=\"screen\"%3E .js-hide {display: none;} .js-show {display: block;} .fouc-fix {display:none;} %3C/style%3E'));</script>\n" +
    "<style>\n" +
    ".tableheader-nav a, a.active {\n" +
    "  color: #333333;\n" +
    "  text-decoration: none;\n" +
    "}\n" +
    ".tableheader-nav a:hover {\n" +
    "  color: #FFFFFF;\n" +
    "  text-decoration: underline;\n" +
    "}\n" +
    "</style>\n" +
    "</head>\n" +
    "<body id=\"home_home\" class=\"home\">\n" +
    "    <!-- BEGIN HEADER -->\n" +
    "    <div class=\"header\">\n" +
    "\t\t<div class=\"container\">\n" +
    "\t\t\t<a href=\"http://www.pentaho.com/\" class=\"logo\">\n" +
    "\t\t\t\t<img src=\"http://build.pentaho.net/web/logo-pentaho.png\" width=\"195\" height=\"52\" alt=\"\">\n" +
    "\t\t\t</a>\n" +
    "\t\t\t<div class=\"right\">\n" +
    "\t\t\t\t<ul class=\"super-nav\">\n" +
    "\t\t\t\t\t<li><span>|</span></li>\n" +
    "\t\t\t\t</ul>\n" +
    "\t\t\t</div>\n" +
    "\t\t\t<div class=\"left\">\n" +
    "\t\t\t\t<ul class=\"main-nav\">\n" +
    "\t\t\t\t\t<li><span>|</span><a href=\"\" target=\"_self\"><strong>${title} Downloads</strong></a></li>\n" +
    "\t\t\t\t</ul>\n" +
    "\t\t\t</div>\n" +
    "\t\t</div>\n" +
    "\t\t<i class=\"shadow\"></i>\n" +
    "\t</div>\n" +
    "    <!-- END HEADER -->\n" +
    "\t<div class=\"primary-content\">\n" +
    "\t\t<div class=\"container\">"

String htmlFooterData = "\t\t</div>\n" +
    "\t</div>\n" +
    "</body>\n" +
    "</html>"


String latestBuildDirName = 'latest'

String targetDir
if (args.length > 0) {
  targetDir = args[0]
} else {
  targetDir = System.getenv()['TARGET_DIR']
}

println 'Target dir: ' + targetDir

File rootDir = targetDir == null ? null : new File(targetDir)
if (targetDir == null || !rootDir.exists()) {
  println "Target directory: ${targetDir} does not exist"
  System.exit(1)
}
File latestDir = new File(rootDir, latestBuildDirName)

def latestBuildNumber = System.getenv()['RELEASE_BUILD_NUMBER']
assert latestBuildNumber != null
println "Latest build number ${latestBuildNumber}"


def maxBuilds = System.getenv()['MAX_BUILDS_TO_KEEP']
if (maxBuilds == null) {
  maxBuilds = 7
} else {
  maxBuilds = Integer.parseInt(maxBuilds)
  if (maxBuilds < 1) {
    maxBuilds = 1
  }
}

println "Max builds to keep ${maxBuilds}"

//Start the index.html page
File indexPage = new File(targetDir, 'index.html')
indexPage.createNewFile()

File tempPage = File.createTempFile("pen", null, rootDir)
tempPage.deleteOnExit()

//dump the header stuff to the file
tempPage.write(htmlHeaderData)

//Directory file filter
FileFilter directoryFilter = new FileFilter() {
  @Override
  boolean accept(File file) {
    return file.isDirectory()
  }
}

def directories = cleanUpExtraDirectories(rootDir, maxBuilds, undeletableBuildsList, debug)

//Scan for build.info files in the build directories
directories.each { dir ->

  //Look for a build.info file in the subdir
  File infoFile = new File(dir.getAbsolutePath(), 'build.info')
  if (infoFile.exists()) {
    //read the two lines from the file to get build number and build id
    def lines = infoFile.readLines()
    /*
     * The build info file has been getting added lines so just require 2, ignore any more.
     */
    if (lines.size() > 1) {
      def buildNumber = lines[0].trim()
      def buildId = lines[1].trim()
      assert buildNumber.toInteger().intValue() > 0

      boolean latestBuild = (buildNumber.toInteger().intValue() == latestBuildNumber.toInteger().intValue()) ? true : false
      String downLoadString = latestBuild ? 'Download*' : 'Download'

      String buildNumberLink = "<span class=\"tableheader-nav\"><a href=\"${buildNumber}\">Build ${buildNumber}</a></span>"
      StringBuffer buildHeaderInfo = new StringBuffer("${buildNumberLink} | ${buildId}")
      StringBuffer buildLogHeaderInfo = new StringBuffer("====== Build: ${buildNumber} with Build ID: ${buildId}")
      if (latestBuild) {
        String latestLink = " | <span class=\"tableheader-nav\"><a href=\"latest\">Latest</a></span>"
        buildHeaderInfo.append(latestLink)
        buildLogHeaderInfo.append(' (latest)')
      }

      buildLogHeaderInfo.append(' ======')
      println buildLogHeaderInfo.toString()
      //Start the table for this build

      String tableHeader = "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
          "\t<tr>\n" +
          "\t\t<th class=\"Title\" bgcolor=\"${tableHeaderBackColor}\" align=\"left\" width=500>${buildHeaderInfo.toString()}</th>\n" +
          "\t\t<th class=\"Title\" bgcolor=\"${tableHeaderBackColor}\" align=\"center\" width=90>OS</th>\n" +
          "\t\t<th class=\"Title\" bgcolor=\"${tableHeaderBackColor}\" align=\"center\" width=90>Type</th>\n" +
          "\t\t<th class=\"Title\" bgcolor=\"${tableHeaderBackColor}\" align=\"center\" width=90>Size</th>\n" +
          "\t\t<th class=\"Title\" bgcolor=\"${tableHeaderBackColor}\" align=\"center\" width=90>Checksums&nbsp;</th>\n" +
          "\t</tr>\n"

      tempPage.append(tableHeader.bytes)

      // Get the list of files depending on if recursion is on or not
      def fileList = []
      if(recurse) {
        dir.eachFileRecurse(FILES) { fileList.add(it) }
      }
      else {
        dir.eachFile(FILES) { fileList.add(it) }
      }
      fileList.sort { it.name }

      def extensionsRegex = /(.*\.zip$|.*\.tar.gz$|.*\.exe$|.*\.jar$|.*\.bin$|.*\.pdf|.*\.kar)/
      fileList.each { file ->
        def name = file.getName()
        def matcher = (name =~ extensionsRegex)
        if (matcher.matches()) {

          def osName = 'all'
          if (name.contains(".mac.") || name.contains("-mac") || name.contains(".app.")) {
            osName = 'os x'
          } else if (name.contains("-win") || name.contains(".win.") ) {
            osName = 'windows'
          }

          def fileType
          if (name.endsWith('tar.gz')) {
            fileType = 'tar.gz'
          } else {
            fileType = name.substring(name.lastIndexOf('.') + 1)
          }

          if (fileType.equals('bin') && osName != 'os x') {
            osName = 'unix'
          } else if (fileType == 'exe') {
            osName = 'windows'
          }

          def idx = name.lastIndexOf(fileType)
          if (idx > 1) {
            name = name.substring(0, idx - 1)
          }

          String fileSize = getPrettyByteSize(file.size())

          // Generate the file URL
          def fileLinkPath = getRelativePath(file, dir.getParentFile())
          def fileUrl = "${pageRootUrl}/${fileLinkPath}"
          println fileUrl

          // Create the checksum file for the file and generate the checksums URL
          def checksumsFile = getCreateChecksumFile(file, forceChecksumGeneration, debug)
          def checksumsLinkPath = getRelativePath(checksumsFile, dir.getParentFile())
          def checksumsUrl = "${pageRootUrl}/${checksumsLinkPath}"
          println checksumsUrl

          String line = "\t<tr>\n" +
              "\t\t<td class=\"Item\" align=\"left\"><A href=\"${fileUrl}\">${name}</A></td>\n" +
              "\t\t<td class=\"Item\" align=\"center\">${osName}</td>\n" +
              "\t\t<td class=\"Item\" align=\"center\">${fileType}</td>\n" +
              "\t\t<td class=\"Item\" align=\"center\">${fileSize}</td>\n" +
              "\t\t<td class=\"Item\" align=\"center\"><A href=\"${checksumsUrl}\">sums</A></td>\n" +
              "\t</tr>\n"

          tempPage.append(line.bytes)
        }
      }

      tempPage.append("</table>".bytes)

      //If latest build, dump the latest links to the log
      if (latestBuild) {
        println "*** Latest build links ***"
        try {
          files = latestDir.listFiles().sort()
          files.each { file ->
            println "${pageRootUrl}" + File.separator + "${latestBuildDirName}" + File.separator + "${file.getName()}"
          }
        } catch (NullPointerException npe) {
          println "Latest links dir is empty (${latestDir.getPath()})"
        }
      }
    }
  }
}

tempPage.append(htmlFooterData.bytes)
//And we're done, let's copy the tempPage over our exsting index page
copyFile(tempPage, indexPage)

// Dump all of the relative paths to the console
if (debug) {
  println "====== ${pageRootUrl} ======"
  if (recurse) {
    rootDir.eachFileRecurse(FILES) {
      println getRelativePath(it, rootDir)
    }
  }
  else {
    rootDir.eachFile(FILES) {
      println getRelativePath(it, rootDir)
    }
  }
  println "====== ${pageRootUrl} ======"
}
else {
  // It is helpful to see a clickable link to the hosted location at the bottom of the console
  println "${pageRootUrl}"
}

/**
 * To get a pretty file size for the web page
 * @param bytes
 * @return
 */
public static String getPrettyByteSize(long bytes) {
  int unit = 1000
  if (bytes < unit) return bytes + " B"
  int exp = (int) (Math.log(bytes) / Math.log(unit))
  String pre = "kMGTPE".charAt(exp - 1)
  return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre).toLowerCase()
}

/**
 * Calls the sha1 write sum to file methods.
 * @param file
 * @param force
 * @param debug
 * @return File containing the checksum
 */
public File getCreateChecksumFile(File file, boolean force, boolean debug) {

  File sumsFile = new File(file.getAbsolutePath() + '.sum')
  if (!file.name.endsWith('.sum')) {
    if (force || !sumsFile.exists() || firstFileIsNewer(file, sumsFile)) {
      sumsFile.createNewFile()
      sumsFile.write("")      //empty the file if already exists
      writeSha1SumToFile(file, sumsFile, debug)
    }
  }
  return sumsFile
}

/**
 * Get the relative path of a file from a base directory.
 * @param file
 * @param directory
 * @return String relative link path
 */
public String getRelativePath(File file, File directory) {
  return new File( directory.toURI().relativize(file.toURI()).toString() )
}

/**
 *
 * @param archiveFile
 * @param sumsFile
 * @param debug
 */
public void writeSha1SumToFile(File archiveFile, File sumsFile, boolean debug) {

  def messageDigest = MessageDigest.getInstance("SHA1")
  archiveFile.eachByte(1048576) { byte[] buf, int bytesRead ->
    messageDigest.update(buf, 0, bytesRead);
  }
  def value = new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0').toString()
  if (debug) println "Writing sha1 ${value} to file ${sumsFile.getPath()}"
  sumsFile << "SHA1=" + value
  sumsFile << System.getProperty("line.separator")

}

/**
 *
 * @param archiveFile
 * @param sumsFile
 * @param debug
 */
public void writeMd5ToFile(File archiveFile, File sumsFile, boolean debug) {

  def messageDigest = MessageDigest.getInstance("MD5")
  archiveFile.eachByte(1048576) { byte[] buf, int bytesRead ->
    messageDigest.update(buf, 0, bytesRead);
  }
  def value = new BigInteger(1, messageDigest.digest()).toString(16).padLeft(32, '0').toString()
  if (debug) println "Writing md5 ${value} to file ${sumsFile.getPath()}"
  sumsFile << "MD5=" + value
  sumsFile << System.getProperty("line.separator")
}

/**
 * Delete a number of directories that exceed the maxBuilds count.
 * Directories should be deleted based on their build number -- lowest (oldest) directories first.
 * @param rootDir
 * @param maxBuilds
 * @param debug
 * @return array of directories after cleanup
 */
public def cleanUpExtraDirectories(File rootDir, int maxBuilds, def undeletableBuildsList, boolean debug) {

  def directories = []

  //Only clean up the numbered directories (not 'latest' or any files
  def directoryNumbers = []
  rootDir.eachDir() { dir ->
    if (dir.name.isNumber()) {
      directoryNumbers.add(Integer.parseInt(dir.name))
    }
  }

  // Create a new collection of numeric sorted directory names
  // Enforced sorting is safer and we need to return a reverse sorted list
  directoryNumbers.sort().reverse().each { dirNum ->
    directories.add(new File(rootDir, dirNum.toString()))
  }

  if (debug) {
    println "Sorted numeric directories before max dir cleanup."
    directories.each { dir ->
      println dir.getPath()
    }
  }

  if (debug) {
    println "Cleanup info"
    println "Root dir ${rootDir.getAbsolutePath()}"
    println "Root Directory count ${directories.size()}"
    println "Max builds ${maxBuilds}"
  }

  String dirName
  while (directories.size() > maxBuilds) {
    dirName = directories.last().getName()
    if (!dirName.equals("latest")) {
      if (buildIsDeletable(dirName, undeletableBuildsList)) {
        println "Removing excess build directory ${dirName}"
        if (!directories.last().deleteDir()) {
          throw new IOException("Cleanup max builds could not delete ${directories.last().getAbsolutePath()}")
        }
      } else {
        maxBuilds = maxBuilds > 1 ? maxBuilds - 1 : 1
      }
      directories = directories.minus(directories.last())
    }
  }

  //Add the un-deletable directories back into the array for processing
  undeletableBuildsList.each {
    directories.add(new File(rootDir, it.trim()))
  }

  if (debug) {
    println "Sorted numeric directories after max dir cleanup."
    directories.each { dir ->
      println dir.getPath()
    }
  }

  return directories
}

/*
 * If a build number is one of those listed in the UNDELETABLE parameter,
  * flag as non-deletable
 */

boolean buildIsDeletable(String buildNumber, def undeletableBuildsList) {

  boolean result = true
  for (String name : undeletableBuildsList) {
    if (name.trim().equals(buildNumber.trim())) {
      println "Ignoring un-deletable build \"${buildNumber.trim()}\""
      result = false;
      break;
    }
  }
  return result

}

/**
 * This method does not use file locking, but it may be safe enough for our purposes
 * If this turns out not to be true, then implement locking.
 * @param src
 * @param dest
 */
public void copyFile(File src, File dest) {

  def source = src.newDataInputStream()
  def target = dest.newDataOutputStream()

  target << source

  source.close()
  target.close()
}

/**
 *
 * @param first
 * @param second
 * @return true if first is newer than second
 */
public boolean firstFileIsNewer(File first, File second) {

  def firstDate = new Date(first.lastModified())
  def secondDate = new Date(second.lastModified())

  return (firstDate > secondDate)
}


public String getStringWithoutTrailingSlash(String s){

    String trimmedStr = s
    int lastSlashIndex = s.lastIndexOf('/');
    if (lastSlashIndex == s.length() - 1) {
        trimmedStr = s.substring(0, lastSlashIndex);
    }

    return trimmedStr
}
