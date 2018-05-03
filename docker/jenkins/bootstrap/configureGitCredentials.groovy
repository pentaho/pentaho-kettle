/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import jenkins.model.Jenkins
import org.apache.commons.io.FileUtils

import java.util.logging.Logger

Logger logger = Logger.getLogger('configureGitCredentials')

def jenkins = Jenkins.get()
def env = System.getenv()

def getCredentials = { ->
  def credentials = new Properties()
  try {
    new File('/usr/share/jenkins/secrets/credentials').withInputStream { credentials.load(it) }
  } catch (FileNotFoundException ignored) {}
  credentials?:env
}

if (!jenkins.isQuietingDown()) {
  File f = new File(jenkins.getRootDir(), 'jenkins.bootstrap.credentials.state')

  if (!f.exists()) {
    def credentials = getCredentials()

    Credentials c = (Credentials) new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        env['CREDENTIALS_ID'],
        'Git credentials',
        credentials['GIT_USER'],
        credentials['GIT_PASS']
    )
    SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), c)

    FileUtils.writeStringToFile(f, jenkins.VERSION)
  } else {
    logger.info 'Skipping credentials configuration.'
  }

} else {
  logger.info 'Shutdown mode enabled.  Credentials configuration skipped.'
}