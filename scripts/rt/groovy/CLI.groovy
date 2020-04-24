import groovy.cli.Option
import groovy.cli.commons.CliBuilder

class Options {
  @Option(shortName = 'h', description = 'print this message') boolean help
  @Option(shortName = 'f', longName = 'manifest-file', description = 'manifest file to read') File manifestFile
  @Option(shortName = 'v', longName = 'versions-file', description = 'versions file to use') File versionsFile
  @Option(shortName = 'n', description = 'release build name') String buildName = ''
  @Option(shortName = 'b', description = 'release build number') String buildNumber = ''
  @Option(description = 'artifactory repository URL') String rtURL
  @Option(description = 'artifactory username') String rtUsername
  @Option(description = 'artifactory password') String rtPassword
  @Option(description = 'artifactory API Token') String rtApiToken
  @Option(description = 'don\'t commit changes') boolean dryRun
}

class CLI {
  String name
  def cli
  Properties env = new Properties(System.getenv())
  @Delegate Options options = new Options()

  def process(args) {
    cli = new CliBuilder(
      usage: "$name --manifest-file file --versionsfile file --buildNumber number --buildName name [options]",
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