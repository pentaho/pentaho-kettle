/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */


import hudson.plugins.gradle.Gradle
import hudson.plugins.gradle.GradleInstallation
import hudson.plugins.gradle.GradleInstaller
import hudson.tasks.Ant
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Maven
import hudson.tools.CommandInstaller
import hudson.tools.InstallSourceProperty
import hudson.tools.ToolInstallation
import hudson.tools.ToolInstaller
import jenkins.model.Jenkins
import java.util.logging.Logger

Logger logger = Logger.getLogger('configureGlobalTools')
def jenkins = Jenkins.get()

def mavenName = 'maven3-auto'
def mavenVersion = '3.5.3'
def takariVersion = '0.12.0'

def antName = 'ant-auto'
def antVersion = '1.10.3'

def gradleName = 'gradle-auto'
def gradleVersion = '4.8'

def installTool = { params ->
  Class<BuildStepDescriptor> tool = params['tool']
  Class<ToolInstallation> toolInstallation = params['toolInstallation']
  Class<ToolInstaller> toolInstaller = params['toolInstaller']
  String name = params['name']
  String version = params['version']
  String command = params['command']

  def toolPlugin = jenkins.getExtensionList(tool)[0]

  if (!toolPlugin.installations.any { it.name == name }) {
    toolPlugin.installations += toolInstallation.newInstance(name, '', [new InstallSourceProperty(
        [command ? toolInstaller.newInstance('', command, '.') : toolInstaller.newInstance(version)])
    ])
    toolPlugin.save()
  }
}

if (!jenkins.isQuietingDown()) {
  def cmd = """\
MAVEN_VERSION=$mavenVersion
TAKARI_VERSION=$takariVersion
if [ ! -f ".installedFrom" ]; then
    echo "https://archive.apache.org/dist/maven/maven-3/\${MAVEN_VERSION}/binaries/apache-maven-\${MAVEN_VERSION}-bin.tar.gz" > .installedFrom
    wget -q https://archive.apache.org/dist/maven/maven-3/\${MAVEN_VERSION}/binaries/apache-maven-\${MAVEN_VERSION}-bin.tar.gz
    tar -zxf apache-maven-\${MAVEN_VERSION}-bin.tar.gz --strip-components=1
    wget -q -P lib/ext http://nexus.pentaho.org/content/groups/omni/org/hitachi/aether/takari-local-repository/\${TAKARI_VERSION}/takari-local-repository-\${TAKARI_VERSION}.jar
    rm apache-maven-\${MAVEN_VERSION}-bin.tar.gz
fi
"""

  // Install maven tool
  installTool([
      'tool'            : Maven.DescriptorImpl,
      'toolInstallation': Maven.MavenInstallation,
      'toolInstaller'   : CommandInstaller,
      'name'            : mavenName,
      'version'         : mavenVersion,
      'command'         : cmd
  ])
  logger.info 'Configured Maven install'

  // Install ant tool
  installTool(['tool': Ant.DescriptorImpl, 'toolInstallation': Ant.AntInstallation, 'toolInstaller': Ant.AntInstaller, 'name': antName, 'version': antVersion])
  logger.info 'Configured Ant install'

  // Install gradle tool
  installTool(['tool': Gradle.DescriptorImpl, 'toolInstallation': GradleInstallation, 'toolInstaller': GradleInstaller, 'name': gradleName, 'version': gradleVersion])
  logger.info 'Configured Gradle install'

  jenkins.save()

} else {
  logger.info 'Shutdown mode enabled.  Tools configuration skipped.'
}