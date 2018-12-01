/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

import hudson.model.JDK
import hudson.tools.InstallSourceProperty
import hudson.tools.JDKInstaller
import jenkins.model.Jenkins

import java.util.logging.Logger

Logger logger = Logger.getLogger('configureJDKTools')
def jenkins = Jenkins.get()
def env = System.getenv()

def versions = [
    'Java9_auto': 'jdk-9.0.4-oth-JPR',
    'Java8_auto': 'jdk-8u192-oth-JPR',
    'Java7_auto': 'jdk-7u80-oth-JPR'
]

def getCredentials = { ->
  def credentials = new Properties()
  try {
    new File('/usr/share/jenkins/secrets/credentials').withInputStream { credentials.load(it) }
  } catch (FileNotFoundException ignored) {}
  credentials?:env
}

if (!jenkins.isQuietingDown()) {
  def credentials = getCredentials()

  def (user, pass) = [
      credentials['ORACLE_USER'],
      credentials['ORACLE_PASS']
  ]

  // To access older versions of JDK, you need to have an Oracle Account.
  if (user && pass) {
    logger.info 'Adding Oracle credentials.'
    def jdkDescriptor = jenkins.getDescriptor(JDKInstaller.class)
    jdkDescriptor.doPostCredential(user, pass)
  }

  def installations = []
  def toolPlugin = jenkins.getExtensionList(JDK.DescriptorImpl.class)[0]
  versions.each { name, version ->
    if (!toolPlugin.installations.any { it.name == name }) {
      logger.info "Adding $name installer."
      installations << new JDK(name, '', [new InstallSourceProperty([new JDKInstaller(version, true)])])
    } else {
      logger.info "JDK $name already configured. Skipping $name auto installer. "
    }
  }

  if (installations) {
    toolPlugin.setInstallations(installations.toArray(new JDK[0]))
    toolPlugin.save()
    jenkins.save()
  }

} else {
  logger.info 'Shutdown mode enabled.  JDK tools configuration skipped.'
}