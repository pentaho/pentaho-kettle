#!/usr/bin/env groovy
//
// Do the initial staging of the installer "patched" folder
//
// Typically you do this just after you release a major version, like 8.0.0.0 or 8.1.0.0.
//
// Requires Unzip in classpath
//
import groovy.util.CliBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import static java.nio.file.StandardCopyOption.*

class CliHandler {
  def cliBuilder = new CliBuilder(usage: "sp-initial-staging.groovy [options]")

  String spPatchedFolder = null
  String buildHostingRootFolder = null
  String releaseVersion = null
  String suiteReleaseVersion = null
  String releaseBuildNumber = null

  CliHandler() {
    cliBuilder.header = "Options:"
    cliBuilder.help("print this message")
    cliBuilder.spPatchedFolder(args: 1, argName: "folder", "Folder to stage the patches eg /build2/sps/8.0-patched")
    cliBuilder.buildHostingRootFolder(args: 1, argName: "folder", "Folder under which where the installer artifacts are located")
    cliBuilder.releaseVersion(args: 1, argName: "version-build", "full product suite version eg. 9.0.0.0-423")
  }

  Boolean process(String[] args) {
    def options = cliBuilder.parse(args)

    // Validate options
    try {
      if(options.help) {
        cliBuilder.usage()
        return false
      }

      if(options.spPatchedFolder) {
        this.spPatchedFolder = options.spPatchedFolder
      }

      if(options.buildHostingRootFolder) {
        this.buildHostingRootFolder = options.buildHostingRootFolder
      }

      if(options.releaseVersion) {
        def version_build = options.releaseVersion
        def version_length = version_build.lastIndexOf('-')

        if(version_length <= 0) {
          println "Failed to parse 'releaseVersion' version/build string: ${version_build}"
          return false
        }

        this.releaseVersion = options.releaseVersion
        this.suiteReleaseVersion = version_build.substring(0, version_length)
        this.releaseBuildNumber = version_build.substring(version_length + 1, version_build.length())
      }

      if(options.spPatchedFolder && options.buildHostingRootFolder && options.releaseVersion) {
        return true
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

public static void makeFolder(String folder) {
  println "Making folder ${folder}"
  File newFolder = new File(folder)
  if(!newFolder.exists()) {
    newFolder.mkdirs()
  }
  newFolder.setReadable(true, false)
  newFolder.setWritable(true, true)
  newFolder.setExecutable(true, false)
}

public static void copyFile(String source, String destination) {
  Path src = Paths.get(source)
  Path dest = Paths.get(destination)
  Files.copy(src, dest, REPLACE_EXISTING)
}

public static void expandArchive(String archive, String folder) {
  println "Expanding ${archive} to ${folder}..."
  Unzip unzipper = new Unzip()
  unzipper.unzip(archive, folder)
}

def execProcessBlocking(command) {
  return execProcessBlocking(command, null)
}

def execProcessBlocking(command, String workingDirectory) {
  println "Executing: ${command.join(' ')}"

  def proc = null
  if(null == workingDirectory) {
    proc = command.execute()
  } else {
    proc = command.execute([], new File(workingDirectory))
  }

  def out = new StringBuffer()
  def err = new StringBuffer()
  proc.consumeProcessOutput( out, err )
  def returnCode = proc.waitFor()

  if( out.size() > 0 ) println out
  if( err.size() > 0 ) println err

  return returnCode
}

public void tarFolder(String folder, String archive) {
  // You need to tar from the parent folder and only include the folder name
  File f = new File(folder)
  String parentFolder = f.absoluteFile.parent
  String folderName = f.name

  execProcessBlocking(['tar', '-cvf', archive, folderName], parentFolder)
}


// Process command line arguments
CliHandler cli = new CliHandler()
assert cli.process(args) : "Unable to parse command-line arguments"

// Source archives - from hosted
def buildHostingVersionFolder = "${cli.buildHostingRootFolder}/${cli.suiteReleaseVersion}/${cli.releaseBuildNumber}"
def aggDesignerArchive = "${buildHostingVersionFolder}/pad-ee-${cli.releaseVersion}.zip"
def pdiClientArchive = "${buildHostingVersionFolder}/pdi-ee-client-${cli.releaseVersion}.zip"
def metadataEditorArchive = "${buildHostingVersionFolder}/pme-ee-${cli.releaseVersion}.zip"
def serverArchive = "${buildHostingVersionFolder}/pentaho-server-ee-${cli.releaseVersion}.zip"
def reportDesignerArchive = "${buildHostingVersionFolder}/prd-ee-${cli.releaseVersion}.zip"
def macReportDesignerArchive = "${buildHostingVersionFolder}/prd-ee-mac-${cli.releaseVersion}.zip"
def schemaWorkbenchArchive = "${buildHostingVersionFolder}/psw-ee-${cli.releaseVersion}.zip"
def bigDataPluginArchive = "${buildHostingVersionFolder}/pentaho-big-data-ee-package-${cli.releaseVersion}.zip"
def pazPluginArchive = "${buildHostingVersionFolder}/paz-plugin-ee-${cli.releaseVersion}.zip"
def pddPluginArchive= "${buildHostingVersionFolder}/pdd-plugin-ee-${cli.releaseVersion}.zip"
def pirPluginArchive = "${buildHostingVersionFolder}/pir-plugin-ee-${cli.releaseVersion}.zip"

// Patch destination folders
def aggDesignerFolder ="${cli.spPatchedFolder}/pad-ee/${cli.releaseVersion}"
def pdiClientFolder ="${cli.spPatchedFolder}/pdi-ee-client/${cli.releaseVersion}"
def metadataEditorFolder ="${cli.spPatchedFolder}/pme-ee/${cli.releaseVersion}"
def serverFolder = "${cli.spPatchedFolder}/pentaho-server-ee/${cli.releaseVersion}"
def reportDesignerFolder = "${cli.spPatchedFolder}/prd-ee/${cli.releaseVersion}"
def schemaWorkbenchFolder = "${cli.spPatchedFolder}/psw-ee/${cli.releaseVersion}"
def manualServerFolder = "${cli.spPatchedFolder}/pentaho-server-ee/manual_patches"
def manualBigDataPluginFolder = "${cli.spPatchedFolder}/manual/big-data-plugin"

// Patch Extraction folders
def aggDesignerExtractFolder ="${cli.spPatchedFolder}/pad-ee/${cli.releaseVersion}/extract/pad-ee"
def pdiClientExtractFolder ="${cli.spPatchedFolder}/pdi-ee-client/${cli.releaseVersion}/extract/pdi-ee-client"
def metadataEditorExtractFolder ="${cli.spPatchedFolder}/pme-ee/${cli.releaseVersion}/extract/pme-ee"
def serverExtractFolder = "${cli.spPatchedFolder}/pentaho-server-ee/${cli.releaseVersion}/extract/pentaho-server-ee"
def reportDesignerExtractFolder = "${cli.spPatchedFolder}/prd-ee/${cli.releaseVersion}/extract/prd-ee"
def schemaWorkbenchExtractFolder = "${cli.spPatchedFolder}/psw-ee/${cli.releaseVersion}/extract/psw-ee"

// Destination archives
def aggDesignerTarArchive ="${aggDesignerFolder}.tar"
def pdiClientTarArchive ="${pdiClientFolder}.tar"
def metadataEditorTarArchive ="${metadataEditorFolder}.tar"
def serverTarArchive = "${serverFolder}.tar"
def reportDesignerTarArchive = "${reportDesignerFolder}.tar"
def schemaWorkbenchTarArchive = "${schemaWorkbenchFolder}.tar"
def manualServerTarArchive = "${manualServerFolder}/${cli.releaseVersion}.tar"
def manualBigDataPluginZipArchive = "${manualBigDataPluginFolder}/pentaho-big-data-ee-package-${cli.releaseVersion}.zip"

// Server plugin folder
def pentahoServerPluginFolder = "${serverExtractFolder}/pentaho-server/pentaho-solutions/system"


// Make the initial "patched" root folder
String spPatchedFolder = cli.spPatchedFolder
makeFolder(spPatchedFolder)

// Make all of the patch folders
makeFolder(aggDesignerExtractFolder)
makeFolder(pdiClientExtractFolder)
makeFolder(metadataEditorExtractFolder)
makeFolder(serverExtractFolder)
makeFolder(reportDesignerExtractFolder)
makeFolder(schemaWorkbenchExtractFolder)
// Manual folders
makeFolder(manualServerFolder)
makeFolder(manualBigDataPluginFolder)

// Expand the product archives
expandArchive(aggDesignerArchive, aggDesignerExtractFolder)
expandArchive(pdiClientArchive, pdiClientExtractFolder)
expandArchive(metadataEditorArchive, metadataEditorExtractFolder)
expandArchive(serverArchive, serverExtractFolder)
expandArchive(reportDesignerArchive, reportDesignerExtractFolder)
expandArchive(schemaWorkbenchArchive, schemaWorkbenchExtractFolder)

// Expand the plugin archives to the server folder
expandArchive(pazPluginArchive, pentahoServerPluginFolder)
expandArchive(pddPluginArchive, pentahoServerPluginFolder)
expandArchive(pirPluginArchive, pentahoServerPluginFolder)

// Now tar the extract folders back up
tarFolder(aggDesignerFolder, aggDesignerTarArchive)
tarFolder(pdiClientFolder, pdiClientTarArchive)
tarFolder(metadataEditorFolder, metadataEditorTarArchive)
tarFolder(serverFolder, serverTarArchive)
tarFolder(reportDesignerFolder, reportDesignerTarArchive)
tarFolder(schemaWorkbenchFolder, schemaWorkbenchTarArchive)

// Copy the server tar to the manual_patches folder
copyFile(serverTarArchive, manualServerTarArchive)
// Copy the big data plugin zip file since it is just a duplicate
copyFile(bigDataPluginArchive, manualBigDataPluginZipArchive)

return 0