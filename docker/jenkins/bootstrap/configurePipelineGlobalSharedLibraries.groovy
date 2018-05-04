/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

/*
   Configure pipeline shared libraries in the global Jenkins configuration.

   jenkins-shared-libraries @ master
   https://github.com/pentaho/jenkins-shared-libraries.git
 */


import jenkins.model.Jenkins
import jenkins.plugins.git.GitSCMSource
import jenkins.plugins.git.traits.BranchDiscoveryTrait
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

import java.util.logging.Logger

Logger logger = Logger.getLogger('configurePipelineGlobalSharedLibraries')
def jenkins = Jenkins.get()

def env = System.getenv()
def name = env['SHARED_LIBRARIES_NAME']
def global_settings = jenkins.getExtensionList(GlobalLibraries.class)[0]

if (!jenkins.isQuietingDown()) {
  if (!global_settings.libraries.any { it.name == name }) {
    def scm = new GitSCMSource(env['SHARED_LIBRARIES_REPO'])
    scm.credentialsId = env['CREDENTIALS_ID']
    scm.traits = [new BranchDiscoveryTrait()]
    def library = new LibraryConfiguration(name, new SCMSourceRetriever(scm))
    library.defaultVersion = env['SHARED_LIBRARIES_BRANCH']
    library.implicit = false
    library.allowVersionOverride = true
    library.includeInChangesets = true
    global_settings.libraries += library
    global_settings.save()
  }
} else {
  logger.info 'Shutdown mode enabled.  Pipeline Global Shared Libraries configuration skipped.'
}