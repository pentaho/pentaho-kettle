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

package org.pentaho.di.base;

import java.util.HashMap;
import java.util.Map;

public class KettleConstants {

  /**
   * See
   * @link https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools#Pan_Options_and_Syntax
   * @link https://help.pentaho.com/Documentation/8.0/Products/Data_Integration/Command_Line_Tools#Kitchen_Options_and_Syntax
   */

  public static final String LOG = "log";
  public static final String DIR = "dir";
  public static final String JOB = "job";
  public static final String REPO = "rep";
  public static final String FILE = "file";
  public static final String USER = "user";
  public static final String PASS = "pass";
  public static final String LEVEL = "level";
  public static final String TRANS = "trans";
  public static final String PARAM = "param";
  public static final String NO_REPO = "norep";
  public static final String CUSTOM = "custom";
  public static final String VERSION = "version";
  public static final String JARFILE = "jarFile";
  public static final String METRICS = "metrics";
  public static final String LOGFILE = "logfile";
  public static final String SAFEMODE = "safemode";
  public static final String LIST_JOBS = "listjob";
  public static final String LIST_DIRS = "listdir";
  public static final String LIST_REPOS = "listrep";
  public static final String TRUST_USER = "trustuser";
  public static final String LIST_TRANS = "listtrans";
  public static final String LIST_PARAMS = "listparam";
  public static final String EXPORT_REPO_JOB = "export";
  public static final String INITIAL_DIR = "initialDir";
  public static final String EXPORT_REPO_TRANS = "exprep";
  public static final String MAX_LOG_LINES = "maxloglines";
  public static final String MAX_LOG_TIMEOUT = "maxlogtimeout";
  public static final String RESULT_SET_STEP_NAME = "stepname";
  public static final String RESULT_SET_COPY_NUMBER = "copynum";
  public static final String BASE64_ZIP = "base64zip";
  public static final String UUID = "uuid";

  public static Map<String, String> toJobMap( Params params ) {

    Map<String, String> arguments = new HashMap<>();

    arguments.putIfAbsent( "uuid", params.getUuid() );
    arguments.putIfAbsent( KettleConstants.REPO, params.getRepoName() );
    arguments.putIfAbsent( KettleConstants.NO_REPO, params.getBlockRepoConns() );
    arguments.putIfAbsent( KettleConstants.USER, params.getRepoUsername() );
    arguments.putIfAbsent( KettleConstants.TRUST_USER, params.getTrustRepoUser() );
    arguments.putIfAbsent( KettleConstants.PASS, params.getRepoPassword() );
    arguments.putIfAbsent( KettleConstants.DIR, params.getInputDir() );
    arguments.putIfAbsent( KettleConstants.FILE, params.getLocalFile() );
    arguments.putIfAbsent( KettleConstants.JARFILE, params.getLocalJarFile() );
    arguments.putIfAbsent( KettleConstants.JOB, params.getInputFile() );
    arguments.putIfAbsent( KettleConstants.LIST_JOBS, params.getListRepoFiles() );
    arguments.putIfAbsent( KettleConstants.LIST_DIRS, params.getListRepoDirs() );
    arguments.putIfAbsent( KettleConstants.EXPORT_REPO_JOB, params.getExportRepo() );
    arguments.putIfAbsent( KettleConstants.INITIAL_DIR, params.getLocalInitialDir() );
    arguments.putIfAbsent( KettleConstants.LIST_REPOS, params.getListRepos() );
    arguments.putIfAbsent( KettleConstants.SAFEMODE, params.getSafeMode() );
    arguments.putIfAbsent( KettleConstants.METRICS, params.getMetrics() );
    arguments.putIfAbsent( KettleConstants.LIST_PARAMS, params.getListFileParams() );
    arguments.putIfAbsent( KettleConstants.LEVEL, params.getLogLevel() );
    arguments.putIfAbsent( KettleConstants.MAX_LOG_LINES, params.getMaxLogLines() );
    arguments.putIfAbsent( KettleConstants.MAX_LOG_TIMEOUT, params.getMaxLogTimeout() );
    arguments.putIfAbsent( KettleConstants.LOGFILE, params.getLogFile() );
    arguments.putIfAbsent( KettleConstants.LOG, params.getOldLogFile() );
    arguments.putIfAbsent( KettleConstants.VERSION, params.getVersion() );
    arguments.putIfAbsent( KettleConstants.RESULT_SET_STEP_NAME, params.getResultSetStepName() );
    arguments.putIfAbsent( KettleConstants.RESULT_SET_COPY_NUMBER, params.getResultSetCopyNumber() );

    if ( params.getParams() != null && !params.getParams().isEmpty() ) {

      params.getParams().keySet().stream().forEach( paramKey -> {
        arguments.putIfAbsent( ( KettleConstants.PARAM + ":" + paramKey ), params.getParams().get( paramKey ) );
      } );
    }

    if ( params.getCustomParams() != null && !params.getCustomParams().isEmpty() ) {

      params.getCustomParams().keySet().stream().forEach( paramKey -> {
        arguments.putIfAbsent( ( KettleConstants.CUSTOM + ":" + paramKey ), params.getParams().get( paramKey ) );
      } );
    }

    return arguments;
  }

  public static Map<String, String> toTransMap( Params params ) {

    Map<String, String> arguments = new HashMap<>();

    arguments.putIfAbsent(  "uuid", params.getUuid() );
    arguments.putIfAbsent(  KettleConstants.REPO, params.getRepoName() );
    arguments.putIfAbsent(  KettleConstants.NO_REPO, params.getBlockRepoConns() );
    arguments.putIfAbsent(  KettleConstants.USER, params.getRepoUsername() );
    arguments.putIfAbsent(  KettleConstants.TRUST_USER, params.getTrustRepoUser() );
    arguments.putIfAbsent(  KettleConstants.PASS, params.getRepoPassword() );
    arguments.putIfAbsent(  KettleConstants.DIR, params.getInputDir() );
    arguments.putIfAbsent(  KettleConstants.FILE, params.getLocalFile() );
    arguments.putIfAbsent(  KettleConstants.JARFILE, params.getLocalJarFile() );
    arguments.putIfAbsent(  KettleConstants.TRANS, params.getInputFile() );
    arguments.putIfAbsent(  KettleConstants.LIST_TRANS, params.getListRepoFiles() );
    arguments.putIfAbsent(  KettleConstants.LIST_DIRS, params.getListRepoDirs() );
    arguments.putIfAbsent(  KettleConstants.EXPORT_REPO_TRANS, params.getExportRepo() );
    arguments.putIfAbsent(  KettleConstants.INITIAL_DIR, params.getLocalInitialDir() );
    arguments.putIfAbsent(  KettleConstants.LIST_REPOS, params.getListRepos() );
    arguments.putIfAbsent(  KettleConstants.SAFEMODE, params.getSafeMode() );
    arguments.putIfAbsent(  KettleConstants.METRICS, params.getMetrics() );
    arguments.putIfAbsent(  KettleConstants.LIST_PARAMS, params.getListFileParams() );
    arguments.putIfAbsent(  KettleConstants.LEVEL, params.getLogLevel() );
    arguments.putIfAbsent(  KettleConstants.MAX_LOG_LINES, params.getMaxLogLines() );
    arguments.putIfAbsent(  KettleConstants.MAX_LOG_TIMEOUT, params.getMaxLogTimeout() );
    arguments.putIfAbsent(  KettleConstants.LOGFILE, params.getLogFile() );
    arguments.putIfAbsent(  KettleConstants.LOG, params.getOldLogFile() );
    arguments.putIfAbsent(  KettleConstants.VERSION, params.getVersion() );
    arguments.putIfAbsent(  KettleConstants.RESULT_SET_STEP_NAME, params.getResultSetStepName() );
    arguments.putIfAbsent(  KettleConstants.RESULT_SET_COPY_NUMBER, params.getResultSetCopyNumber() );
    arguments.putIfAbsent(  KettleConstants.BASE64_ZIP, params.getBase64Zip() );

    if ( params.getParams() != null && !params.getParams().isEmpty() ) {

      params.getParams().keySet().stream().forEach( paramKey -> {
        arguments.putIfAbsent(  ( KettleConstants.PARAM + ":" + paramKey ), params.getParams().get( paramKey ) );
      } );
    }

    // KTRs do not use customParams

    return arguments;
  }

}
