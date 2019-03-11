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
import org.apache.commons.lang.StringUtils;
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


        putIfNotEmpty( arguments, "uuid", getUuid() );
        putIfNotEmpty( arguments, KettleConstants.REPO, getRepoName() );
        putIfNotEmpty( arguments, KettleConstants.NO_REPO, getBlockRepoConns() );
        putIfNotEmpty( arguments, KettleConstants.USER, getRepoUsername() );
        putIfNotEmpty( arguments, KettleConstants.TRUST_USER, getTrustRepoUser() );
        putIfNotEmpty( arguments, KettleConstants.PASS, getRepoPassword() );
        putIfNotEmpty( arguments, KettleConstants.DIR, getInputDir() );
        putIfNotEmpty( arguments, KettleConstants.FILE, getLocalFile() );
        putIfNotEmpty( arguments, KettleConstants.JARFILE, getLocalJarFile() );
        putIfNotEmpty( arguments, KettleConstants.JOB, getInputFile() );
        putIfNotEmpty( arguments, KettleConstants.LIST_JOBS, getListRepoFiles() );
        putIfNotEmpty( arguments, KettleConstants.LIST_DIRS, getListRepoDirs() );
        putIfNotEmpty( arguments, KettleConstants.EXPORT_REPO_JOB, getExportRepo() );
        putIfNotEmpty( arguments, KettleConstants.INITIAL_DIR, getLocalInitialDir() );
        putIfNotEmpty( arguments, KettleConstants.LIST_REPOS, getListRepos() );
        putIfNotEmpty( arguments, KettleConstants.SAFEMODE, getSafeMode() );
        putIfNotEmpty( arguments, KettleConstants.METRICS, getMetrics() );
        putIfNotEmpty( arguments, KettleConstants.LIST_PARAMS, getListFileParams() );
        putIfNotEmpty( arguments, KettleConstants.LEVEL, getLogLevel() );
        putIfNotEmpty( arguments, KettleConstants.MAX_LOG_LINES, getMaxLogLines() );
        putIfNotEmpty( arguments, KettleConstants.MAX_LOG_TIMEOUT, getMaxLogTimeout() );
        putIfNotEmpty( arguments, KettleConstants.LOGFILE, getLogFile() );
        putIfNotEmpty( arguments, KettleConstants.LOG, getOldLogFile() );
        putIfNotEmpty( arguments, KettleConstants.VERSION, getVersion() );
        putIfNotEmpty( arguments, KettleConstants.RESULT_SET_STEP_NAME, getResultSetStepName() );
        putIfNotEmpty( arguments, KettleConstants.RESULT_SET_COPY_NUMBER, getResultSetCopyNumber() );

        if ( getParams() != null && !getParams().isEmpty() ) {

            getParams().keySet().stream().forEach( paramKey -> {
                putIfNotEmpty( arguments, ( KettleConstants.PARAM + ":" + paramKey ), getParams().get( paramKey ) );
            } );
        }

        if ( getCustomParams() != null && !getCustomParams().isEmpty() ) {

            getCustomParams().keySet().stream().forEach( paramKey -> {
                putIfNotEmpty( arguments, ( KettleConstants.CUSTOM + ":" + paramKey ), getParams().get( paramKey ) );
            } );
        }

        return arguments;
    }

    private void putIfNotEmpty( Map<String, String> map, final String key, final String value ) {

        if ( map != null && !StringUtils.isEmpty( key ) && !StringUtils.isEmpty( value ) ) {
            map.put( key, value );
        }
    }

}
