library(identifier: "${params.LIB_NAME ?: 'pentaho-library'}@${params.LIB_VERSION ?: '20240722'}", changelog: false)

node(params.SLAVE_NODE_LABEL ?: 'non-master') {
  timestamps {
    stages.configure()

    catchError {
      timeout(config.get().timeout) {
        stages.preClean()
        stages.checkout()
        stages.version()
        stages.build()
        stages.test()
        stages.push()
        stages.tag()
        stages.archive()
        stages.postClean()
      }
    }

    stages.report()
  }
}
