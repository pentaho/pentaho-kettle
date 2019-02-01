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

Logger logger = Logger.getLogger('configureCredentials')

Jenkins jenkins = Jenkins.get()
Map env = System.getenv()

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
    SystemCredentialsProvider credentialsProvider = SystemCredentialsProvider.getInstance()
    Map credentials = getCredentials()

    Credentials scm = (Credentials) new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        env['SCM_CREDENTIALS_ID'],
        'Git credentials',
        credentials['GIT_USER'],
        credentials['GIT_TOKEN']
    )
    credentialsProvider.getStore().addCredentials(Domain.global(), scm)

    Credentials deploy = (Credentials) new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        env['DEPLOY_CREDENTIALS_ID'],
        'Nexus credentials',
        credentials['DEPLOY_USER'],
        credentials['DEPLOY_PASS']
    )
    credentialsProvider.getStore().addCredentials(Domain.global(), deploy)

    FileUtils.writeStringToFile(f, jenkins.VERSION)
  } else {
    logger.info 'Skipping credentials configuration.'
  }

} else {
  logger.info 'Shutdown mode enabled.  Credentials configuration skipped.'
}