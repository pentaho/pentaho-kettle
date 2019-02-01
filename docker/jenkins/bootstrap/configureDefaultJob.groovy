/**
 * Copyright 2018 Hitachi Vantara.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */


import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.GitSCM
import hudson.plugins.git.UserRemoteConfig
import hudson.scm.SCM
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.flow.FlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

import java.util.logging.Logger


Logger logger = Logger.getLogger('configureDefaultJob')

Jenkins jenkins = Jenkins.get()
Map env = System.getenv()

if (!jenkins.isQuietingDown()) {
  if (!jenkins.items.any { it.name == env['DEFAULT_JOB_NAME'] }) {
    SCM scm = new GitSCM('')
    scm.userRemoteConfigs = [new UserRemoteConfig(
        env['DEFAULT_JOB_REPO'],
        '',
        '',
        env['SCM_CREDENTIALS_ID']
    )]
    scm.branches = [new BranchSpec("*/${env['DEFAULT_JOB_BRANCH']}")]

    FlowDefinition flowDefinition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
    flowDefinition.lightweight = true

    WorkflowJob job = new WorkflowJob(jenkins, env['DEFAULT_JOB_NAME'])
    job.definition = flowDefinition

    StringParameterDefinition buildFile = new StringParameterDefinition('BUILD_DATA_FILE', 'sample-pipeline.yaml')
    job.addProperty(new ParametersDefinitionProperty(buildFile))

    jenkins.reload()
  }
} else {
  logger.info 'Shutdown mode enabled.  Default job creation skipped.'
}
