#!/usr/bin/env bash
//usr/bin/env groovy -cp "$(cd $(dirname ${BASH_SOURCE[0]}); pwd)/groovy" -Dgroovy.grape.report.downloads=true "$0" "$@"; exit $?

/*
This script will generate and submit the Artifactory Build Info json for the
given manifest.yaml / version.properties files.
Note that any BUILDTAG string present in version.properties will be replace by either
BUILDTAG environment variable or the build number passed by --buildNumber argument.

Usage:
  ./rtBuildInfo.groovy \
    --rtURL http://localhost:8081/artifactory/libs-release-local \
    --rtApiToken 000000000000000000000000000000000000000000000000000000 \
    --manifest-file ../../resources/config/artifacts/manifest.yaml \
    --versions-file ../../resources/config/suite-release.versions \
    --buildName suite-release \
    --buildNumber RC1
 */

def cli = new CLI(name: this.class.canonicalName)
cli.process(args)

def manif = new ManifestReader(cli)
def rt = new Artifactory(cli)
rt.testConnection()

def filenames = manif.parseManifest()
def artifacts = rt.searchArtifacts(filenames)
int total = artifacts.size()
println "Total artifacts found: ${total}"

if (total != filenames.size()) {
  println '=== These files are in the manifest but where not found! ==='
  def missing = filenames - artifacts*.name
  missing.each {
    println "$it: not found!"
  }
  println '=== end of manifest misses ==='
}

rt.sendBuildInfo(artifacts)