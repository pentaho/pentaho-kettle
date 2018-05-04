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


def jenkins = Jenkins.getInstance()
// workdir names with spaces causes problems in javascript builds
def jobName = 'sample-pipeline'
def env = System.getenv()

if (!jenkins.isQuietingDown()) {
    if (!jenkins.items.any { it.name == jobName }) {
        def scm = new GitSCM('')
        scm.userRemoteConfigs = [new UserRemoteConfig( env['SAMPLE_PIPELINE']?:'https://github.com/pentaho/jenkins-pipelines.git', '', '', env['CREDENTIALS_ID'])]
        scm.branches = [new BranchSpec("*/${env['SAMPLE_PIPELINE_BRANCH']?:'master'}")]

        def flowDefinition = new CpsScmFlowDefinition(scm, "Jenkinsfile")

        def job = new WorkflowJob(jenkins, jobName)
        job.definition = flowDefinition

        jenkins.reload()
    }
} else {
    logger.info 'Shutdown mode enabled.  Sample pipeline job creation skipped.'
}