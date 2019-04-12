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

import org.apache.commons.lang.math.NumberUtils;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

public abstract class Params implements IParams {

  private String uuid;
  private String repoName;
  private String blockRepoConns;
  private String repoUsername;
  private String trustRepoUser;
  private String repoPassword;
  private String inputDir;
  private String inputFile;
  private String listRepoFiles;
  private String listRepoDirs;
  private String exportRepo;
  private String localFile;
  private String localJarFile;
  private String localInitialDir;
  private String listRepos;
  private String safeMode;
  private String metrics;
  private String listFileParams;
  private String logLevel;
  private String maxLogLines;
  private String maxLogTimeout;
  private String logFile;
  private String oldLogFile;
  private String version;
  private String resultSetStepName;
  private String resultSetCopyNumber;
  private String base64Zip;
  private NamedParams namedParams;
  private NamedParams customNamedParams;

  public Params( String blockRepoConns, String repoName, String repoUsername, String trustRepoUser, String repoPassword, String inputDir,
                   String inputFile, String listRepoFiles, String listRepoDirs, String exportRepo, String localFile,
                   String localJarFile, String localInitialDir, String listRepos, String safeMode, String metrics,
                   String listFileParams, String logLevel, String maxLogLines, String maxLogTimeout, String logFile,
                   String oldLogFile, String version, String resultSetStepName, String resultSetCopyNumber, String base64Zip, NamedParams params,
                   NamedParams customNamedParams ) {

    setUuid( java.util.UUID.randomUUID().toString() );

    setBlockRepoConns( blockRepoConns );
    setRepoName( repoName );
    setRepoUsername( repoUsername );
    setTrustRepoUser( trustRepoUser );
    setRepoPassword( repoPassword );
    setInputDir( inputDir );
    setInputFile( inputFile );
    setListRepoFiles( listRepoFiles );
    setListRepoDirs( listRepoDirs );
    setExportRepo( exportRepo );
    setLocalFile( localFile );
    setLocalJarFile( localJarFile );
    setLocalInitialDir( localInitialDir );
    setListRepos( listRepos );
    setSafeMode( safeMode );
    setMetrics( metrics );
    setListFileParams( listFileParams );
    setLogLevel( logLevel );
    setMaxLogLines( maxLogLines );
    setMaxLogTimeout( maxLogTimeout );
    setLogFile( logFile );
    setOldLogFile( oldLogFile );
    setVersion( version );
    setResultSetStepName( resultSetStepName );
    setResultSetCopyNumber( NumberUtils.isNumber( resultSetCopyNumber ) ? resultSetCopyNumber : "0" /* default */ );
    setBase64Zip( base64Zip );
    setNamedParams( params );
    setNamedCustomParams( customNamedParams );
  }

  @Override
  public abstract Map<String, String> asMap();

  @Override
  public String getUuid() {
    return uuid;
  }

  public void setUuid( String uuid ) {
    this.uuid = uuid;
  }

  @Override
  public String getRepoName() {
    return repoName;
  }

  public void setRepoName( String repoName ) {
    this.repoName = repoName;
  }

  @Override
  public String getBlockRepoConns() {
    return blockRepoConns;
  }

  public void setBlockRepoConns( String blockRepoConns ) {
    this.blockRepoConns = blockRepoConns;
  }

  @Override
  public String getRepoUsername() {
    return repoUsername;
  }

  public void setRepoUsername( String repoUsername ) {
    this.repoUsername = repoUsername;
  }

  @Override
  public String getTrustRepoUser() {
    return trustRepoUser;
  }

  public void setTrustRepoUser( String trustRepoUser ) {
    this.trustRepoUser = trustRepoUser;
  }

  @Override
  public String getRepoPassword() {
    return repoPassword;
  }

  public void setRepoPassword( String repoPassword ) {
    this.repoPassword = repoPassword;
  }

  @Override
  public String getInputDir() {
    return inputDir;
  }

  public void setInputDir( String inputDir ) {
    this.inputDir = inputDir;
  }

  public String getLocalFile() {
    return localFile;
  }

  public void setLocalFile( String localFile ) {
    this.localFile = localFile;
  }

  @Override
  public String getLocalJarFile() {
    return localJarFile;
  }

  public void setLocalJarFile( String localJarFile ) {
    this.localJarFile = localJarFile;
  }

  @Override
  public String getInputFile() {
    return inputFile;
  }

  public void setInputFile( String inputFile ) {
    this.inputFile = inputFile;
  }

  @Override
  public String getListRepoFiles() {
    return listRepoFiles;
  }

  public void setListRepoFiles( String listRepoFiles ) {
    this.listRepoFiles = listRepoFiles;
  }

  @Override
  public String getListRepoDirs() {
    return listRepoDirs;
  }

  public void setListRepoDirs( String listRepoDirs ) {
    this.listRepoDirs = listRepoDirs;
  }

  @Override
  public String getExportRepo() {
    return exportRepo;
  }

  public void setExportRepo( String exportRepo ) {
    this.exportRepo = exportRepo;
  }

  @Override
  public String getLocalInitialDir() {
    return localInitialDir;
  }

  public void setLocalInitialDir( String localInitialDir ) {
    this.localInitialDir = localInitialDir;
  }

  @Override
  public String getListRepos() {
    return listRepos;
  }

  public void setListRepos( String listRepos ) {
    this.listRepos = listRepos;
  }

  @Override
  public String getSafeMode() {
    return safeMode;
  }

  public void setSafeMode( String safeMode ) {
    this.safeMode = safeMode;
  }

  @Override
  public String getMetrics() {
    return metrics;
  }

  public void setMetrics( String metrics ) {
    this.metrics = metrics;
  }

  public String getListFileParams() {
    return listFileParams;
  }

  public void setListFileParams( String listFileParams ) {
    this.listFileParams = listFileParams;
  }

  @Override
  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel( String logLevel ) {
    this.logLevel = logLevel;
  }

  @Override
  public String getMaxLogLines() {
    return maxLogLines;
  }

  public void setMaxLogLines( String maxLogLines ) {
    this.maxLogLines = maxLogLines;
  }

  @Override
  public String getMaxLogTimeout() {
    return maxLogTimeout;
  }

  public void setMaxLogTimeout( String maxLogTimeout ) {
    this.maxLogTimeout = maxLogTimeout;
  }

  @Override
  public String getLogFile() {
    return logFile;
  }

  public void setLogFile( String logFile ) {
    this.logFile = logFile;
  }

  @Override
  public String getOldLogFile() {
    return oldLogFile;
  }

  public void setOldLogFile( String oldLogFile ) {
    this.oldLogFile = oldLogFile;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  @Override
  public String getResultSetStepName() {
    return resultSetStepName;
  }

  public void setResultSetStepName( String resultSetStepName ) {
    this.resultSetStepName = resultSetStepName;
  }

  @Override
  public String getResultSetCopyNumber() {
    return resultSetCopyNumber;
  }

  public void setResultSetCopyNumber( String resultSetCopyNumber ) {
    this.resultSetCopyNumber = resultSetCopyNumber;
  }

  @Override
  public String getBase64Zip() {
    return base64Zip;
  }

  public void setBase64Zip( String base64Zip ) {
    this.base64Zip = base64Zip;
  }

  @Override
  public NamedParams getNamedParams() {
    return namedParams;
  }

  public void setNamedParams( NamedParams params ) {
    this.namedParams = params;
  }

  @Override
  public Map<String, String> getParams() {
    if ( this.namedParams == null ) {
      return Collections.EMPTY_MAP;
    }

    Map<String, String> params = new HashMap<String, String>();

    for ( String key : this.namedParams.listParameters() ) {
      try {
        params.put( key, this.namedParams.getParameterValue( key ) );
      } catch ( UnknownParamException e ) {
        /* no-op */
      }
    }

    return params;
  }

  @Override
  public NamedParams getCustomNamedParams() {
    return customNamedParams;
  }

  public void setNamedCustomParams( NamedParams customParams ) {
    this.customNamedParams = customParams;
  }

  @Override
  public Map<String, String> getCustomParams() {
    if ( this.customNamedParams == null ) {
      return Collections.EMPTY_MAP;
    }

    Map<String, String> customParams = new HashMap<String, String>();

    for ( String key : this.customNamedParams.listParameters() ) {
      try {
        customParams.put( key, this.customNamedParams.getParameterValue( key ) );
      } catch ( UnknownParamException e ) {
        /* no-op */
      }
    }

    return customParams;
  }
}
