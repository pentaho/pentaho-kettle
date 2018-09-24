/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */


/*
   This script will configure system settings for a Jenkins instance:
     - Set master label to non-master to be able to run pipelines
     - Disable Usage Statistics
     - Global Properties
     - Agents Protocols
     - Enable Agent -> Master Access Control
     - CSRF Protection
     - Disable CLI remoting
     - Markup Formatter
     - Project name restrictions
 */
import hudson.markup.RawHtmlMarkupFormatter
import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.CLI
import jenkins.model.Jenkins
import jenkins.model.ProjectNamingStrategy
import jenkins.security.s2m.AdminWhitelistRule
import org.apache.commons.io.FileUtils

import java.util.logging.Logger

Logger logger = Logger.getLogger('configureGlobalSettings')

def jenkins = Jenkins.get()
def env = System.getenv()
def masterLabels = env['MASTER_LABELS']?:'non-master'
/*
    Disable all JNLP protocols except for JNLP4.  JNLP4 is the most secure agent
    protocol because it is using standard TLS.
 */
Set<String> agentProtocolsList = ['JNLP4-connect', 'Ping']

if (!jenkins.isQuietingDown()) {
  File f = new File(jenkins.getRootDir(), 'jenkins.bootstrap.settings.state')

  if (!f.exists()) {

    if (jenkins.labelString != masterLabels) {
      jenkins.labelString = masterLabels
    }

    /*
         Disable submitting usage statistics
     */
    if (jenkins.isUsageStatisticsCollected()) {
      jenkins.setNoUsageStatistics(true)
      logger.info 'Disabled submitting usage stats to Jenkins project.'
    }

    if (!agentProtocolsList.containsAll(jenkins.getAgentProtocols())) {
      jenkins.setAgentProtocols(agentProtocolsList)
      logger.info 'Configured Agent Protocols'
    }

    /*
        Enable Agent Master Access Control
     */
    jenkins.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
    logger.info 'Configured Master Access Control'

    /*
        Prevent Cross Site Request Forgery exploits
     */
    jenkins.crumbIssuer = new DefaultCrumbIssuer(true)
    logger.info 'Configured CSRF Protection'

    /*
        Configure Markup Formatter to use Safe HTML
     */
    if (jenkins.markupFormatter.class != RawHtmlMarkupFormatter) {
      jenkins.markupFormatter = new RawHtmlMarkupFormatter(false)
      logger.info 'Configured Markup Formatter'
    }

    jenkins.projectNamingStrategy =
        new ProjectNamingStrategy.PatternProjectNamingStrategy('[a-z0-9-\\.]{3,50}',"", true)

    jenkins.save()

    CLI.get().setEnabled(false)
    logger.info 'Disabled CLI remote'

    FileUtils.writeStringToFile(f, jenkins.VERSION)
  } else {
    logger.info 'Skipping global settings configuration.'
  }
} else {
  logger.info 'Shutdown mode enabled.  Global configuration skipped.'
}
