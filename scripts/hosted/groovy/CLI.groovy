import groovy.cli.Option
import groovy.cli.commons.CliBuilder

class Options {
  @Option(shortName = 'h', description = 'print this message') boolean help
  @Option(shortName = 'f', longName = 'manifest-file', description = 'manifest file to read') File manifestFile
  @Option(shortName = 'v', longName = 'versions-file', description = 'versions file to use') File versionsFile
  @Option(description = 'artifactory repository URL') String rtURL
  @Option(description = 'artifactory username') String rtUsername
  @Option(description = 'artifactory password') String rtPassword
  @Option(description = 'artifactory API Token') String rtApiToken
  @Option(description = 'root hosting path') String buildHostingRoot
  @Option(description = 'build specific deployment folder') String deploymentFolder
  @Option(description = 'release build number') String releaseBuildNumber
  @Option(description = 'release version') String releaseVersion
  @Option(description = 'is a snapshot build?') Boolean isSnapshot
}

class CLI {
  String name
  def cli
  Properties env = new Properties(System.getenv())
  @Delegate Options options = new Options()

  def process(args) {
    cli = new CliBuilder(
      usage: "$name --manifest-file file --versionsfile file [options]",
      header: 'Options:',
      width: 80
    )
    cli.parseFromInstance(options, args)

    if (help || !args) {
      cli.usage()
      System.exit(0)
    }
  }

  void printError(message) {
    System.err.println("${name}: $message")
    System.exit(1)
  }
}