/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


import groovy.cli.Option
@GrabResolver(name = 'pentaho', root = 'https://repo.orl.eng.hitachivantara.com/artifactory/pnt-mvn', m2Compatible = 'true')
@Grapes([
    @Grab(group = 'org.apache.velocity', module = 'velocity-engine-core', version = '2.1')
])

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity

import java.nio.file.Files
import java.nio.file.Paths
import java.text.DecimalFormat
import java.time.LocalDateTime

class HostedArtifactsManager implements Serializable {

  @Delegate
  CLI cli

  Artifactory artifactoryHandler

  HostedArtifactsManager(CLI cli) {
    this.cli = cli
    this.artifactoryHandler = new Artifactory(cli)
  }

  /**
   * Reads the artifacts manifest list either from a
   * manifest file, a Map or a List
   *
   * @return list of the files names
   */
  List<String> getArtifactsNames(String buildNbr = null) {
    ManifestReader manif = new ManifestReader(cli, buildNbr)
    List<String> fileNames = manif.parseManifest()

    return fileNames
  }

/**
 * Looks for the artifacts and does the actual archiving
 */
  void hostArtifacts() {
    validate()

    String hostedRoot = getHostedRoot()
    if (!hostedRoot) {
      println("Not able to determine where to create hosted page!")

    } else {
      Files.createDirectories(Paths.get(hostedRoot))

      final String header = resolveTemplate("templates/header", [:], false)
      StringBuilder content = new StringBuilder(header)

      if (isSnapshotBuild()) {
        // releaseVersion being set in CLI
        String pathMatcher = "$releaseVersion-SNAPSHOT"
        List<String> fileNames = getArtifactsNames()
        if (fileNames) {
          List<Map> artifactsMetadata = this.artifactoryHandler.searchArtifacts(fileNames, pathMatcher)

          content.append(buildHtmlPortion(artifactsMetadata, pathMatcher, null, {
            artifactsMetadata = artifactsMetadata.collect {
              String fileName = it.name as String
              fileName = fileName.replaceAll("($releaseVersion-[\\d]+.[\\d]+-[\\d]+)", pathMatcher)
              it.put("name", fileName)
              return it
            }
          }))
        } else {
          println("No file names to search for")
        }

      } else {
        List<Map> versionsData = this.artifactoryHandler
            .searchArtifacts(
                ["pentaho-parent-pom-${releaseVersion}-*.pom"],
                "$releaseVersion-*",
                "$releaseVersion-SNAPSHOT",
                "\$desc",
                4,
                true
            )

        if (!versionsData) {
          return
        }

        List<Map> relevantBuilds = versionsData.collect {
          [
              number :
                  (it.name as String)
                      .replace("pentaho-parent-pom-${releaseVersion}-", "")
                      .replace(".pom", ""),
              created: it.created
          ]
        }

        try {
          List<String> menuIds = relevantBuilds.collect {
            "$releaseVersion-$it.number"
          } as List<String>

          content.append(createMenus(menuIds))

          boolean isLatest = true
          for (Map build : relevantBuilds) {
            String pathMatcher = "$releaseVersion-$build.number"
            String createDate = "$build.created"
            List<Map> artifactsMetadata = this.artifactoryHandler.searchArtifacts(getArtifactsNames("$build.number"), null)

            if (artifactsMetadata?.size() > 0) {
              println("${artifactsMetadata.size()} artifacts found for version ${pathMatcher}")

              String htmlPortion = buildHtmlPortion(artifactsMetadata, pathMatcher, createDate)
              content.append(htmlPortion)

              /*
                When iterating the builds, when we find the current build, we need to:
                  - create the sum files in the proper path
                  - create the index in the 'latest' folder
              */
              if (isLatest) {
                // creates the checksum files
                //TODO uncomment sum files creation when going into production
//                for (Map file : artifactsMetadata) {
//                  writeFile("$hostedRoot/${file.name}.sum", "SHA1=${file.actual_sha1}")
//                }

                String versionFreeHtmlPortion = buildHtmlPortion(artifactsMetadata, pathMatcher, createDate, {
                  artifactsMetadata = artifactsMetadata.collect {
                    it.put("name", (it.name as String).replace("-$pathMatcher", ""))
                    return it
                  }
                })
                writeFile("$hostedRoot/../latest/new-layout/index.html", "$header $versionFreeHtmlPortion")
                String buildDateString = createDate.replace('T', ' ').substring(0, 16)
                writeFile("$hostedRoot/../latest/new-layout/build.info", "$build.number\n$buildDateString\n")
              }

            } else {
              println("No artifacts were found in Artifactory for build ${build.number}!")
            }
            isLatest = false
          }
        } catch (Exception e) {
          System.err.println("$e")
        }
      }

      content.append(createFooter())

      // creates index at the main build folder
      writeFile("${hostedRoot}/../new-layout/index.html", content.toString())
      println("Index HTML page (re)generated at ${hostedRoot}/../")
    }
  }

  String createFooter() {
    LocalDateTime buildDate = LocalDateTime.now()
    String pageCreationDate = String.format('%tF %<tH:%<tM', buildDate)
    return resolveTemplate("templates/footer.vm", ["creationDate": pageCreationDate])
  }

  static def writeFile(String location, String text) {
    File file = new File(location)
    file.getParentFile().mkdirs()
    file.createNewFile()
    file.text = text

  }

  String buildHtmlPortion(
      List<Map> artifactsMetadata,
      String version,
      String createDate,
      Closure replaceDisplayLinkText = null
  ) {

    if (isSnapshotBuild()) {
      artifactsMetadata = getLatestArtifacts(artifactsMetadata)
    }

    if (replaceDisplayLinkText != null) {
      replaceDisplayLinkText.call()
    }

    return createHtmlPortion(artifactsMetadata, version, createDate)
  }

  static List getLatestArtifacts(List<Map> artifactsMetadata) {
    Map<String, Map> latestArtifacts = [:]
    artifactsMetadata.forEach {
      String name = it.name as String
      name = name.replaceAll("[\\d.]", "");
      latestArtifacts.put(name, it)
    }

    return latestArtifacts.collect { key, value ->
      value
    }.sort { a, b -> a.name <=> b.name }

  }

  boolean isSnapshotBuild() {
    return Boolean.valueOf(isSnapshot).booleanValue() // set in CLI
  }

  String createMenus(List<String> relevantBuildNbrs) {
    Map bindings = [
        builds: relevantBuildNbrs
    ]

    return resolveTemplate("templates/buildsMenu.vm", bindings)
  }

  String createHtmlPortion(List<Map> artifactsMetadata, String version, String createDate) {
    String buildDateString = ""
    if (!createDate) {
      LocalDateTime buildDate = LocalDateTime.now()
      buildDateString = String.format('%tF %<tH:%<tM', buildDate)
    } else {
      buildDateString = createDate
          .replace('T', ' ')
          .substring(0, 16)
    }

    Map bindings = [
        files          : artifactsMetadata,
        buildHeaderInfo: "Build ${version} | ${buildDateString}",
        artifactoryURL : rtURL.endsWith('/') ? rtURL : rtURL + '/',
        numberFormat   : new DecimalFormat("###,##0.000"),
        version        : version
    ]

    return resolveTemplate("templates/artifacts.vm", bindings)
  }

  String resolveTemplate(String template, Map parameters, boolean runVelocity = true) {

    String scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent

    if (runVelocity) {
      StringWriter writer = new StringWriter()

      VelocityContext context = new VelocityContext()
      parameters.each { key, value ->
        context.put(key, value)
      }


      Velocity.init()
      Velocity.evaluate(context, writer, 'resolveTemplate',
          new File(
              Paths.get(
                  scriptDir,
                  '/',
                  template) as String
          ).newReader()
      )

      return writer.toString()
    } else {
      return new File(Paths.get(
          scriptDir,
          '/',
          template) as String).text
    }
  }

  /**
   * Retrieves the target path on hosted
   * @return
   */
  String getHostedRoot() {

    if (!buildHostingRoot) {
      return ''
    }

    return Paths.get(
        buildHostingRoot.trim(),
        deploymentFolder.trim(),
        releaseBuildNumber.trim()
    ) as String
  }

  void validate() {
    if (!rtURL) printError('Artifactory url missing')
    if (!(rtUsername && rtPassword) && !rtApiToken)
      printError('Artifactory authentication missing. Provide a username and password, or an Api token.')
    if (!manifestFile) printError('Manifest file missing')
    if (!versionsFile) printError('Versions file missing')
    if (!buildHostingRoot) printError('Build\'s root hosting path missing')
    if (!deploymentFolder) printError('Build\'s deployment folder missing')
    if (!releaseBuildNumber) printError('Build number missing')
    if (!releaseVersion) printError('Build version missing')
  }
}
