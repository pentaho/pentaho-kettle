/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

import hudson.plugins.git.BranchSpec
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

import java.util.logging.Logger


Logger logger = Logger.getLogger('configureSamplePipelineJob')

def jenkins = Jenkins.get()
def env = System.getenv()
def jobName = env['SAMPLE_PIPELINE_NAME']

if (!jenkins.isQuietingDown()) {
  if (!jenkins.items.any { it.name == jobName }) {
    def scm = new GitSCM('')
    scm.userRemoteConfigs = [new UserRemoteConfig(
        env['SAMPLE_PIPELINE_REPO'],
        '',
        '',
        env['CREDENTIALS_ID']
    )]
    scm.branches = [new BranchSpec("*/${env['SAMPLE_PIPELINE_BRANCH']}")]

    def flowDefinition = new CpsScmFlowDefinition(scm, "Jenkinsfile")

    def job = new WorkflowJob(jenkins, jobName)
    job.definition = flowDefinition

    jenkins.reload()
  }
} else {
  logger.info 'Shutdown mode enabled.  Sample pipeline job creation skipped.'
}
