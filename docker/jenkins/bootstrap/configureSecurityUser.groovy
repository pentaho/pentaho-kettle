/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

import jenkins.model.Jenkins
import hudson.security.GlobalMatrixAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import org.apache.commons.io.FileUtils

import java.util.logging.Logger

Logger logger = Logger.getLogger('configureSecurityUser')

def env = System.getenv()
def jenkins = Jenkins.get()

def getCredentials = { ->
  def credentials = new Properties()
  try {
    new File('/usr/share/jenkins/secrets/credentials').withInputStream { credentials.load(it) }
  } catch (FileNotFoundException ignored) {}
  credentials?:env
}

if (!jenkins.isQuietingDown()) {
  File f = new File(jenkins.getRootDir(), 'jenkins.bootstrap.security.state')

  if (!f.exists()) {
    def credentials = getCredentials()

    // configure default admin user
    jenkins.setSecurityRealm(new HudsonPrivateSecurityRealm(true, false, null))
    jenkins.setAuthorizationStrategy(new GlobalMatrixAuthorizationStrategy())

    def user = jenkins.getSecurityRealm().createAccount(
        credentials['JENKINS_USER'],
        credentials['JENKINS_PASS']
    )
    user.save()

    jenkins.getAuthorizationStrategy().add(Jenkins.ADMINISTER, credentials['JENKINS_USER'])
    jenkins.getAuthorizationStrategy().add(Jenkins.READ, Jenkins.ANONYMOUS.name)

    jenkins.save()
    FileUtils.writeStringToFile(f, jenkins.VERSION)
  } else {
    logger.info 'Skipping default admin user creation.'
  }
} else {
  logger.info 'Shutdown mode enabled.  Bootstrap configuration skipped.'
}