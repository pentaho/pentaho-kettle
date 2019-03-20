/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.kitchen;

import org.pentaho.di.base.Params;
import org.pentaho.di.base.KettleConstants;
import org.pentaho.di.core.parameters.NamedParams;

import java.util.HashMap;
import java.util.Map;

public class JobParams extends Params {


  public JobParams(String blockRepoConns, String repoName, String repoUsername, String trustRepoUser, String repoPassword, String inputDir,
                    String inputFile, String listRepoFiles, String listRepoDirs, String exportRepo, String localFile,
                    String localJarFile, String localInitialDir, String listRepos, String listFileParams, String logLevel,
                    String maxLogLines, String maxLogTimeout, String logFile, String oldLogFile, String version,
                    String resultSetStepName, String resultSetCopyNumber, NamedParams namedParams, NamedParams customParams ) {

      super( blockRepoConns, repoName, repoUsername, trustRepoUser, repoPassword, inputDir, inputFile, listRepoFiles, listRepoDirs, exportRepo,
                localFile, localJarFile, localInitialDir, listRepos, null, null, listFileParams, logLevel, maxLogLines,
                maxLogTimeout, logFile, oldLogFile, version, resultSetStepName, resultSetCopyNumber, namedParams, customParams );
  }

  @Override
  public Map<String, String> asMap() {

        Map<String, String> arguments = new HashMap<>();

        arguments.putIfAbsent( "uuid", getUuid() );
        arguments.putIfAbsent( KettleConstants.REPO, getRepoName() );
        arguments.putIfAbsent( KettleConstants.NO_REPO, getBlockRepoConns() );
        arguments.putIfAbsent( KettleConstants.USER, getRepoUsername() );
        arguments.putIfAbsent( KettleConstants.TRUST_USER, getTrustRepoUser() );
        arguments.putIfAbsent( KettleConstants.PASS, getRepoPassword() );
        arguments.putIfAbsent( KettleConstants.DIR, getInputDir() );
        arguments.putIfAbsent( KettleConstants.FILE, getLocalFile() );
        arguments.putIfAbsent( KettleConstants.JARFILE, getLocalJarFile() );
        arguments.putIfAbsent( KettleConstants.JOB, getInputFile() );
        arguments.putIfAbsent( KettleConstants.LIST_JOBS, getListRepoFiles() );
        arguments.putIfAbsent( KettleConstants.LIST_DIRS, getListRepoDirs() );
        arguments.putIfAbsent( KettleConstants.EXPORT_REPO_JOB, getExportRepo() );
        arguments.putIfAbsent( KettleConstants.INITIAL_DIR, getLocalInitialDir() );
        arguments.putIfAbsent( KettleConstants.LIST_REPOS, getListRepos() );
        arguments.putIfAbsent( KettleConstants.SAFEMODE, getSafeMode() );
        arguments.putIfAbsent( KettleConstants.METRICS, getMetrics() );
        arguments.putIfAbsent( KettleConstants.LIST_PARAMS, getListFileParams() );
        arguments.putIfAbsent( KettleConstants.LEVEL, getLogLevel() );
        arguments.putIfAbsent( KettleConstants.MAX_LOG_LINES, getMaxLogLines() );
        arguments.putIfAbsent( KettleConstants.MAX_LOG_TIMEOUT, getMaxLogTimeout() );
        arguments.putIfAbsent( KettleConstants.LOGFILE, getLogFile() );
        arguments.putIfAbsent( KettleConstants.LOG, getOldLogFile() );
        arguments.putIfAbsent( KettleConstants.VERSION, getVersion() );
        arguments.putIfAbsent( KettleConstants.RESULT_SET_STEP_NAME, getResultSetStepName() );
        arguments.putIfAbsent( KettleConstants.RESULT_SET_COPY_NUMBER, getResultSetCopyNumber() );

        if ( getParams() != null && !getParams().isEmpty() ) {

            getParams().keySet().stream().forEach( paramKey -> {
                arguments.putIfAbsent( ( KettleConstants.PARAM + ":" + paramKey ), getParams().get( paramKey ) );
            } );
        }

        if ( getCustomParams() != null && !getCustomParams().isEmpty() ) {

            getCustomParams().keySet().stream().forEach( paramKey -> {
                arguments.putIfAbsent( ( KettleConstants.CUSTOM + ":" + paramKey ), getParams().get( paramKey ) );
            } );
        }

        return arguments;
    }
}
