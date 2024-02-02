@GrabResolver(name='pentaho', root='https://one.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grapes([
  @Grab(group = 'org.yaml', module = 'snakeyaml', version = '1.26')
])

import org.yaml.snakeyaml.Yaml

class ManifestReader {
  @Delegate CLI cli

  ManifestReader(CLI cli) {
    this.cli = cli
  }

  def parseManifest() {
    loadVersions()
    if (manifestFile) {
      validate(manifestFile)
      Set<String> filenames = manifestFile.withInputStream { input ->
        Yaml yaml = new Yaml()
        return getManifestFiles(yaml.loadAll(input))
      }
      filenames -= null // ignore null entries
      return filterFilenames(filenames)
    }
    printError('missing manifest file')
  }

  /**
   * load versions file in memory
   * @return
   */
  void loadVersions() {
    if (versionsFile) {
      validate(versionsFile)
      versionsFile.withInputStream { input ->
        env.load(input)
      }
    }
  }

  private void validate(File file) {
    if (file.isFile()) {
      return
    } else if (file.isDirectory()) {
      printError("${file}: Is a directory")
    } else {
      printError("${file}: No such file or directory")
    }
  }

  /**
   * Given a structured manifest data, extract all the leafs.
   * @param document
   */
  def getManifestFiles(document) {
    def filter // recursive function to get leafs
    filter = { obj ->
      switch (obj) {
        case Map:
          obj.collectMany {k,v -> filter(v)}
          break
        case Iterable:
          return obj.collectMany(filter)
        default:
          return [obj]
      }
    }
    return filter(document)
  }

  def filterFilenames(Set<String> strings) {
    def version = { prop, number -> prop.replace('BUILDTAG', number) }.memoize()
    Set<String> miss = []

    strings*.replaceAll(/\$\{(.*?)}/) { m, k ->
      String key = env.get(k)
      if (key) return version(key, env.get('BUILDTAG') ?: buildNumber)
      if (miss.add(k)) {
        println "$k: missing property!"
      }
      // property not found, don't change!
      return m
    }
  }
}