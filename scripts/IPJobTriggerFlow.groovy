import hudson.model.*

import java.util.regex.Pattern

def allowSourceConsolidation = Boolean.parseBoolean(params.ALLOW_SOURCE_CONSOLIDATION)
def allowDistributionsConsolidation = Boolean.parseBoolean(params.ALLOW_DISTRIBUTIONS_CONSOLIDATION)
def allowIPScan = Boolean.parseBoolean(params.ALLOW_IP_SCAN)
def allowHubScan = Boolean.parseBoolean(params.ALLOW_HUB_SCAN)
def suiteReleaseVersion = params.SUITE_RELEASE_VERSION
def sourceConsolidationJob = params.SOURCE_CONSOLIDATION_JOB
def distribConsolidationJob = params.DISTRIBUTIONS_CONSOLIDATION_JOB
def ipScanJob = params.IP_SCAN_JOB
def hubScanJob = params.HUB_SCAN_JOB
def buildNumber = params.RELEASE_BUILD_NUMBER

build.setDisplayName("#${build.number} - ${suiteReleaseVersion}-${buildNumber}");

Pattern releasePattern = Pattern.compile("^\\d\\.\\d\\.\\d\\.\\d");
Pattern qatPattern = Pattern.compile("^\\d\\.\\d-QAT");

println "\n*** Begin Job Info ***\n"
println "Suite release version                 = ${suiteReleaseVersion}"
println "\nIP job atomic controls:"
println "Allow source consolidation job        = ${allowSourceConsolidation}"
println "Allow distributions consolidation job = ${allowDistributionsConsolidation}"
println "Allow IP scan jobs                    = ${allowIPScan}"
println "\nIP job paths:"
println "Source consolidation job              = ${sourceConsolidationJob}"
println "Distributions consolidation job       = ${distribConsolidationJob}"
println "IP scan job                           = ${ipScanJob}"


def isQAT = false
def isRelease = false

if (qatPattern.matcher(suiteReleaseVersion)) {
  isQAT = true
} else if (releasePattern.matcher(suiteReleaseVersion)) {
  isRelease = true
}

def allowRunIPJobs = false
if (isQAT) {
  allowRunIPJobs = ((suiteReleaseVersion.equals("9.1-QAT") && Boolean.parseBoolean(params.DO_91_QAT_SCAN))
    || (suiteReleaseVersion.equals("9.0-QAT") && Boolean.parseBoolean(params.DO_90_QAT_SCAN))
    || (suiteReleaseVersion.equals("8.3-QAT") && Boolean.parseBoolean(params.DO_83_QAT_SCAN)))
} else if (isRelease) {
  allowRunIPJobs = ((suiteReleaseVersion.startsWith("9.1") && Boolean.parseBoolean(params.DO_91_R_SCAN))
    || (suiteReleaseVersion.startsWith("9.0") && Boolean.parseBoolean(params.DO_90_R_SCAN))
    || (suiteReleaseVersion.startsWith("8.3") && Boolean.parseBoolean(params.DO_83_R_SCAN)))
}

if (allowRunIPJobs) {

  def parameters = [
    "SUITE_RELEASE_VERSION"       : params.SUITE_RELEASE_VERSION,
    "BUILD_HOSTING_ROOT"          : params.BUILD_HOSTING_ROOT,
    "SUITE_BUILD_RESOURCES_BRANCH": params.SUITE_BUILD_RESOURCES_BRANCH,
    "RELEASE_BUILD_NUMBER"        : buildNumber
  ]

  // Run the consolidation jobs in parallel
  if (allowSourceConsolidation || allowDistributionsConsolidation) {
    println "\nLaunching consolidation jobs...\n"
    parallel(
      {
        if (allowSourceConsolidation) {
          build(parameters, sourceConsolidationJob)
        }
      },
      {
        if (allowDistributionsConsolidation) {
          build(parameters, distribConsolidationJob)
        }
      }
    )
  } else {
    println "\nNot running consolidation jobs because the option is unset."
  }

  // Now run the Blackduck Protex scans
  if (allowIPScan) {
    println "\nLaunching BlackDuck Protex scan job...\n"
    build(parameters, ipScanJob)
  } else {
    println "\nNot running BlackDuck Protex scan job because the option is unset."
  }

  // Now run the BlackDuck Hub scans
  if (allowHubScan) {
    println "\nLaunching BlackDuck Hub scan job...\n"
    build(parameters, hubScanJob)
  } else {
    println "\nNot running BlackDuck Hub scan job because the option is unset."
  }

} else {
  println "\nNot running IP jobs because version controlled build for ${suiteReleaseVersion} is unset."
}

println "\n*** End Job Info ***\n"
