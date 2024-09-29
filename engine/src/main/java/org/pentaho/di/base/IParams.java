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

import org.pentaho.di.core.parameters.NamedParams;

import java.io.Serializable;
import java.util.Map;

public interface IParams extends Serializable {

  /**
   * @return uuid uniquely identifies an execution request
   */
  String getUuid();

  /**
   * @return repoName Enterprise or database repository name, if you are using one
   */
  String getRepoName();

  /**
   * @return blockRepoConns Prevents from logging into a repository. If you have set the KETTLE_REPOSITORY, KETTLE_USER,
   * and KETTLE_PASSWORD environment variables, then this option will enable you to prevent from logging into the
   * specified repository, assuming you would like to execute a local KTR file instead.
   */
  String getBlockRepoConns();


  /**
   * @return repoUsername Repository username
   */
  String getRepoUsername();

  /**
   * @return trustRepoUser Trust the repository username passed along ( i.e. no password required )
   */
  String getTrustRepoUser();

  /**
   * @return repoPassword Repository password
   */
  String getRepoPassword();


  /**
   * @return inputDir The directory that contains the file, including the leading slash
   */
  String getInputDir();


  /**
   * @return localFile If you are calling a local file, this is the filename, including the path if it is not
   * in the local directory
   */
  String getLocalFile();


  /**
   * @return localJarFile If you are calling a file within a local jar file, this is the filename, including the path if
   * it is not in the local directory
   */
  String getLocalJarFile();


  /**
   * @return inputFile The name of the file to launch
   */
  String getInputFile();


  /**
   * @return listRepoFiles Lists the transformations in the specified repository directory
   */
  String getListRepoFiles();


  /**
   * @return listDirs Lists the directories in the specified repository
   */
  String getListRepoDirs();


  /**
   * @return exportRepo Exports all repository objects to one XML file
   */
  String getExportRepo();


  /**
   * @return localInitialDir if local filename starts with scheme like zip:, then you can pass along the initial dir to it
   */
  String getLocalInitialDir();


  /**
   * @return listRepos Lists the available repositories
   */
  String getListRepos();


  /**
   * @return safeMode Runs in safe mode, which enables extra checking
   */
  String getSafeMode();


  /**
   * @return metrics Enables kettle metric gathering
   */
  String getMetrics();


  /**
   * @return listFileParams List information about the defined named parameters in the specified file.
   */
  String getListFileParams();


  /**
   * @return logLevel The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)
   */
  String getLogLevel();


  /**
   * @return maxLogLines The maximum number of log lines that are kept internally by PDI.
   * Set to 0 to keep all rows (default)
   */
  String getMaxLogLines();


  /**
   * @return maxLogTimeout The maximum age (in minutes) of a log line while being kept internally by PDI.
   * Set to 0 to keep all rows indefinitely (default)
   */
  String getMaxLogTimeout();


  /**
   * @return logFile A local filename to write log output to
   */
  String getLogFile();


  /**
   * @return oldLogFile if the old style of logging name is filled in, and the new one is not, overwrite the new by the old
   */
  String getOldLogFile();


  /**
   * @return version Shows the version, revision, and build date
   */
  String getVersion();


  /**
   * @return the step name from which we want the result set
   */
  String getResultSetStepName();

  /**
    * @return the step copy number from which we want the result set
   */
  String getResultSetCopyNumber();


  /**
   * @return the BASE64 representation of the zipped ETL work item
   */
  String getBase64Zip();

  /**
   * @return params parameters to be passed into the executing file
   */
  Map<String, String> getParams();


  /**
   * @return namedParams parameters to be passed into the executing file
   */
  NamedParams getNamedParams();


  /**
   * @return customParams parameters to be passed into the executing file
   */
  Map<String, String> getCustomParams();


  /**
   * @return namedParams custom parameters to be passed into the executing file
   */
  NamedParams getCustomNamedParams();
}
