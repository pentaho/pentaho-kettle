#!/usr/bin/env groovy
@GrabResolver(name='pentaho', root='https://one.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grab('org.yaml:snakeyaml:1.18')
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions

@Grab('pentaho-build:file-gatherer:1.3.0')
import com.pentaho.build.utilities.FileGatherer

import groovy.util.CliBuilder
import groovy.io.FileType

/**
 * Commandline interface handler for the Harvester
 * <p/>
 *  -manifest <manifest>                       path to yaml manifest file
 *  -source <directory>                        source directory
 *  -destination <directory>                   directory in which to harvest the files
 *  -versions <comma seperated versions>       version properties to replace in manifest
 *  -versionproperties <properties file path>  path to version properties file
 *  -include <comma separated list>            items to include (optional)
 *  -exclude <comma separated list>            items to exclude (optional)
 *  -flat                                      don't create folder structure in manifest
 *  -o, -options
 *  -help                                          print this message
 */
class CliHandler {
  def cliBuilder = new CliBuilder(usage: "harvester.groovy [options]")

  def manifest = null
  def regex = null
  def sourceDirectory = null
  def destinationDirectory = null
  def versionProperties = null
  def versionPropertiesPath = null
  def inclusions = []
  def exclusions = []
  def flatHarvest = false
  def nonDistHarvest = false
  def validate = false
  def gathererOptions = null

  CliHandler() {
    cliBuilder.header = "Options: Either -manifest or -regex MUST be specified"
    cliBuilder.help("print this message")
    cliBuilder.manifest(args: 1, argName: "manifest", "path to yaml manifest file")
    cliBuilder.regex(args: 1, argName: "regex", "regex of files")
    cliBuilder.source(args: 1, argName: "directory", "source directory", required: true)
    cliBuilder.destination(args: 1, argName: "directory", "directory in which to harvest the files to", required: true)
    cliBuilder.versionproperties(args: 1, argName: "property file", "path to version properties file")
    cliBuilder.versions(args: 1, argName: "comma separated versions", "version properties to replace in manifest", valueSeparator: ',')
    cliBuilder.include(args: 1, argName: "comma separated list", "items to include (optional)", valueSeparator: ',')
    cliBuilder.exclude(args: 1, argName: "comma separated list", "items to exclude (optional)", valueSeparator: ',')
    cliBuilder.flat("don't create manifest folder structure at destination")
    cliBuilder.nondist("harvest dist archives that have non-dist counterparts ONLY")
    cliBuilder.validate("validate that all of the files in manifest were harvested")
    cliBuilder.o(args: 1, longOpt: "options", "file gatherer options")
  }

  Boolean process(String[] args) {
    def options = cliBuilder.parse(args)

    // Validate options
    try {
      if(options.help) {
        cliBuilder.usage()
        return false
      }

      if(options.manifest) {
        this.manifest = options.manifest
      }

      if(options.regex) {
        this.regex = options.regex
      }

      if(options.source) {
        this.sourceDirectory = options.source
      }

      if(options.destination) {
        this.destinationDirectory = options.destination
      }

      if(options.versions) {
        // Split "RELEASE_VERSION=foo,SP_RELEASE_VERSION=bar" into a map
        def versionPropertiesList = options.versions.tokenize(',')

        Properties versionPropertiesMap = new Properties()
        versionPropertiesList.each {
          def list = it.trim().split('=')
          versionPropertiesMap.put(list[0], list[1])
        }

        this.versionProperties = versionPropertiesMap
      }

      if(options.versionproperties) {
        this.versionPropertiesPath = options.versionproperties
      }
      else {
        // If a version.properties file is not specified, try the one in the grandparent folder
        def scriptFolder = new File(getClass().protectionDomain.codeSource.location.path).parent
        def versionPropertiesFile = new File(scriptFolder, '../resources/config/suite-release.versions')
        if (versionPropertiesFile.exists()) {
          this.versionPropertiesPath = versionPropertiesFile.absolutePath
        }
      }

      // Include only certain folders from the manifest
      // eg. -include client-tools,
      if(options.include) {
        this.inclusions = options.include.tokenize(',')
      }

      // Exclude certain folders from the manifest
      // -exclude shims
      if(options.exclude) {
        this.exclusions = options.exclude.tokenize(',')
      }

      // Options to pass on to the file gatherer
      if(options.o) {
        this.gathererOptions = options.o
      }

      if (options.flat) {
        this.flatHarvest = options.flat
      }

      if (options.nondist) {
        this.nonDistHarvest = options.nondist
      }

      if (options.validate) {
        this.validate = options.validate
      }

      // User must specify either a manifest or a regex
      if((this.manifest || this.regex) && this.sourceDirectory && this.destinationDirectory) {
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

class Harvester {
  def manifest = [:]
  def regex = ""
  def versionProperties = [:]
  def inclusions = []
  def exclusions = []
  def flatHarvest = false // Don't create folder structure in manifest
  def nonDistHarvest = false
  def validate = false
  def options = ""
  def missingFiles = []

  def replaceVersionProperties(def fileList) {
    def resultFileList = []
    fileList.each {
      def fileName = it
      versionProperties.each { k, v ->
        def token = "\${$k}"
        if (fileName.contains(token)) {
          fileName = fileName.replace(token, v)
        }
      }
      resultFileList.add(fileName)
    }
    return resultFileList
  }

  def makeDirectory(def directory) {
    def dir = new File(directory)
    if(!dir.exists()) {
      dir.mkdirs()
    }
  }

  def removeDirectoryIfEmpty(def directory) {
    def dir = new File(directory)
    if(dir.exists()) {
      if (dir.listFiles().size() == 0) {
        println "Pruning ${directory}"
        dir.deleteDir()
      }
    }
  }


  // This method calls out to the external FileGatherer java class
  public static void fileGatherer(def regex, def sourceDirectory, def destinationDirectory, def opts) {
    FileGatherer gatherer = new FileGatherer()
    gatherer.setMatchPatterns(regex)
    gatherer.setSourcePath(sourceDirectory)
    gatherer.setTargetPath(destinationDirectory)
    if (opts) {
      gatherer.setCreateSoftLinks(opts.contains("s"));
      gatherer.setCreateHardLinks(opts.contains("h"));
      gatherer.setTrimVersions(opts.contains("t"));
      gatherer.setFollowSymbolicLinks(opts.contains("l"));
      gatherer.setDryRun(opts.contains("d"));
      gatherer.setVerbose(opts.contains("v"));
      gatherer.setNoOverwriteExisting(opts.contains("n"));
    }
    gatherer.initialize();
    gatherer.execute();
  }

  def harvestFileList(def fileList, def sourceDirectory, def destinationDirectory) {
    // Replace tokens like (RELEASE_VERSION) with their proper version
    def list = replaceVersionProperties(fileList)
    println list

    // Create the pattern matching regex from the file list
    def regex = ""
    list.each {
      // If non-dist harvest remove "-dist" from file name
      if (nonDistHarvest) {
        if (it.contains("-dist")) {
          def nonDist = it.replace("-dist", "")
          regex += nonDist
          regex += ","
          // Worker nodes non-eula wrap archive ends in tar.gz, not .zip
          if (nonDist.endsWith(".zip")) {
            regex += nonDist.replaceAll(".zip\$",".tar.gz");
            regex += ","
          }
        }
      }
      else {
        regex += "${it},"
      }
    }
    regex = regex.replaceFirst(/,$/,"") // Remove last comma
    //println regex

    makeDirectory(destinationDirectory)

    fileGatherer(regex, sourceDirectory, destinationDirectory, this.options)

    if ((validate) && (!nonDistHarvest)) {
      list.each {
        def file = new File(destinationDirectory, it)
        if(!file.exists()) {
          missingFiles.add(file.absolutePath)
        }
      }
    }
  }

  def harvestFolder(def folderMap, def sourceDirectory, def destinationDirectory) {
    folderMap.each { k,v ->
      // This block controls of this will be a flat or heirarchical harvest
      def harvestDestinationDirectory = ""
      if (!flatHarvest) {
        File dir = new File(destinationDirectory, k)
        harvestDestinationDirectory = dir.absolutePath
      }
      else {
        harvestDestinationDirectory = destinationDirectory
      }

      // If the type is a map then that signifies the keys are folders, if it
      // is a list, it is a list of files name(s) to be harvested to the
      // destination directory
      if (v instanceof Map)
        harvestFolder(v, sourceDirectory, harvestDestinationDirectory)
      else if (v instanceof List) {
        harvestFileList(v, sourceDirectory, harvestDestinationDirectory)
        // If nothing was harvested, prune the folder if empty. Pruning
        // a flat harvest would shoot ourselves in the foot because the
        // top-level folder could be deleted.
        if (!flatHarvest) {
          removeDirectoryIfEmpty(harvestDestinationDirectory)
        }
      }
    }
  }

  def harvestManifest(def manifest, def sourceDirectory, def destinationDirectory, def options) {
    println "Harvesting manifest ${sourceDirectory} to ${destinationDirectory} ..."
    this.options = options
    makeDirectory(destinationDirectory)
    harvestFolder(manifest, sourceDirectory, destinationDirectory) // manifest is a folder map
  }

  def harvestRegex(def regex, def sourceDirectory, def destinationDirectory, def options) {
    println "Harvesting regex ${sourceDirectory} to ${destinationDirectory} ..."
    this.options = options
    makeDirectory(destinationDirectory)
    fileGatherer(regex, sourceDirectory, destinationDirectory, options) // regex is a string
  }
}

//
// Manifest filtering methods
//
def includeInManifest(def manifest, def inclusions) {
  def filteredManifest = [:]
  manifest.each { k, v ->
    if (inclusions.contains(k)) {
      filteredManifest.put(k, v)
    }
    if (v instanceof Map) {
      def subManifest = includeInManifest(v, inclusions)
      if (!subManifest.isEmpty())
        filteredManifest.put(k, subManifest)
    }
  }
  return filteredManifest
}

def excludeFromManifest(def manifest, def exclusions) {
  def filteredManifest = [:]
  manifest.each { k, v ->
    if (exclusions.contains(k)) {
      return
    }
    if (v instanceof Map) {
      def subManifest = excludeFromManifest(v, exclusions)
      filteredManifest.put(k, subManifest)
    }
    else {
      filteredManifest.put(k, v)
    }
  }
  return filteredManifest
}


def filterManifest(def manifest, def inclusions, def exclusions) {
  def filteredManifest = manifest
  if (inclusions.size() > 0) {
    filteredManifest = includeInManifest(filteredManifest, inclusions)
  }
  if (exclusions.size() > 0) {
    filteredManifest = excludeFromManifest(filteredManifest, exclusions)
  }
  return filteredManifest
}


// Process command line arguments
CliHandler cli = new CliHandler()
if(!cli.process(args)) {
  System.exit(1)
}

// Load the manifest file
def manifest = null
if (cli.manifest) {
  InputStream input = new FileInputStream(new File(cli.manifest));
  Yaml yaml = new Yaml()
  def loadedManifest = yaml.load(input)

  // Filter the manifest file of inclusions/exclusions
  manifest = filterManifest(loadedManifest, cli.inclusions, cli.exclusions)
  //println manifest
}

Properties versionProperties = new Properties()
// Load any version.properties
if (cli.versionPropertiesPath) {
  Properties properties = new Properties()
  File propertiesFile = new File(cli.versionPropertiesPath)
  propertiesFile.withInputStream {
      properties.load(it)
  }
  versionProperties.putAll(properties)
}
// Load any properties from the command-line, overriding any from the version.properties or build-control.properties
if (cli.versionProperties) {
  versionProperties.putAll(cli.versionProperties)
}

// versionProperties.each { k, v ->
//   println "${k} = ${v}"
// }

Harvester harvester = new Harvester()
harvester.flatHarvest = cli.flatHarvest
harvester.nonDistHarvest = cli.nonDistHarvest
harvester.validate = cli.validate
harvester.versionProperties = versionProperties // Contains build and version properties
if (manifest)
  harvester.harvestManifest(manifest, cli.sourceDirectory, cli.destinationDirectory, cli.gathererOptions)
else if (cli.regex)
  harvester.harvestRegex(cli.regex, cli.sourceDirectory, cli.destinationDirectory, cli.gathererOptions)
else
  println "ERROR: Nothing to harvest. Empty filtered -manifest or -regex."

// We still want all of the copying to take place, so don't error out until the end
if ((cli.validate) && (!harvester.missingFiles.isEmpty())) {
  println "ERROR: Missing manifest files:"
  harvester.missingFiles.each {
    println it
  }
  System.exit(2)
}
