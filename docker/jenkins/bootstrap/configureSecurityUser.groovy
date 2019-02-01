/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

import hudson.security.AuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.SecurityRealm
import jenkins.model.Jenkins
import hudson.security.GlobalMatrixAuthorizationStrategy
import org.apache.commons.io.FileUtils

import java.util.logging.Logger

Logger logger = Logger.getLogger('configureSecurityUser')

Jenkins jenkins = Jenkins.get()

if (!jenkins.isQuietingDown()) {
  File f = new File(jenkins.getRootDir(), 'jenkins.bootstrap.security.state')

  if (!f.exists()) {
    SecurityRealm securityRealm = new HudsonPrivateSecurityRealm(true, false, null)

    // configure guest to be administrator
    AuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy()
    authorizationStrategy.add(Jenkins.ADMINISTER, Jenkins.ANONYMOUS.name)

    jenkins.setSecurityRealm(securityRealm)
    jenkins.setAuthorizationStrategy(authorizationStrategy)
    jenkins.save()

    FileUtils.writeStringToFile(f, jenkins.VERSION)
  } else {
    logger.info 'Skipping default admin setup.'
  }
} else {
  logger.info 'Shutdown mode enabled.  Bootstrap configuration skipped.'
}