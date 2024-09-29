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

public class Params implements IParams {

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

  private Params() {

  }

  public static class Builder {
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

    public Builder() {
      this( java.util.UUID.randomUUID().toString() );
    }

    public Builder( String  uuid ) {
      this.uuid = uuid;
    }

    public Builder blockRepoConns( String blockRepoConns ) {
      this.blockRepoConns = blockRepoConns;
      return this;
    }

    public Builder repoName( String repoName ) {
      this.repoName = repoName;
      return this;
    }

    public Builder repoUsername( String repoUsername ) {
      this.repoUsername = repoUsername;
      return this;
    }

    public Builder trustRepoUser( String trustRepoUser ) {
      this.trustRepoUser = trustRepoUser;
      return this;
    }

    public Builder repoPassword( String repoPassword ) {
      this.repoPassword = repoPassword;
      return this;
    }

    public Builder inputDir( String inputDir ) {
      this.inputDir = inputDir;
      return this;
    }

    public Builder inputFile( String inputFile ) {
      this.inputFile = inputFile;
      return this;
    }

    public Builder listRepoFiles( String listRepoFiles ) {
      this.listRepoFiles = listRepoFiles;
      return this;
    }

    public Builder listRepoDirs( String listRepoDirs ) {
      this.listRepoDirs = listRepoDirs;
      return this;
    }

    public Builder exportRepo( String exportRepo ) {
      this.exportRepo = exportRepo;
      return this;
    }

    public Builder localFile( String localFile ) {
      this.localFile = localFile;
      return this;
    }

    public Builder localJarFile( String localJarFile ) {
      this.localJarFile = localJarFile;
      return this;
    }

    public Builder localInitialDir( String localInitialDir ) {
      this.localInitialDir = localInitialDir;
      return this;
    }

    public Builder listRepos( String listRepos ) {
      this.listRepos = listRepos;
      return this;
    }

    public Builder safeMode( String safeMode ) {
      this.safeMode = safeMode;
      return this;
    }

    public Builder metrics( String metrics ) {
      this.metrics = metrics;
      return this;
    }

    public Builder listFileParams( String listFileParams ) {
      this.listFileParams = listFileParams;
      return this;
    }

    public Builder logLevel( String logLevel ) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder maxLogLines( String maxLogLines ) {
      this.maxLogLines = maxLogLines;
      return this;
    }

    public Builder maxLogTimeout( String maxLogTimeout ) {
      this.maxLogTimeout = maxLogTimeout;
      return this;
    }

    public Builder logFile( String logFile ) {
      this.logFile = logFile;
      return this;
    }

    public Builder oldLogFile( String oldLogFile ) {
      this.oldLogFile = oldLogFile;
      return this;
    }

    public Builder version( String version ) {
      this.version = version;
      return this;
    }

    public Builder resultSetStepName( String resultSetStepName ) {
      this.resultSetStepName = resultSetStepName;
      return this;
    }

    public Builder resultSetCopyNumber( String resultSetCopyNumber ) {
      this.resultSetCopyNumber = resultSetCopyNumber;
      return this;
    }

    public Builder base64Zip( String base64Zip ) {
      this.base64Zip = base64Zip;
      return this;
    }

    public Builder namedParams( NamedParams namedParams ) {
      this.namedParams = namedParams;
      return this;
    }

    public Builder customNamedParams( NamedParams customNamedParams ) {
      this.customNamedParams = customNamedParams;
      return this;
    }

    public Params build() {
      Params params = new Params();
      params.uuid = uuid;
      params.blockRepoConns = blockRepoConns;
      params.repoName = repoName;
      params.repoUsername = repoUsername;
      params.trustRepoUser = trustRepoUser;
      params.repoPassword = repoPassword;
      params.inputDir = inputDir;
      params.inputFile = inputFile;
      params.listRepoFiles = listRepoFiles;
      params.listRepoDirs = listRepoDirs;
      params.exportRepo = exportRepo;
      params.localFile = localFile;
      params.localJarFile = localJarFile;
      params.localInitialDir = localInitialDir;
      params.listRepos = listRepos;
      params.safeMode = safeMode;
      params.metrics = metrics;
      params.listFileParams = listFileParams;
      params.logLevel = logLevel;
      params.maxLogLines = maxLogLines;
      params.maxLogTimeout = maxLogTimeout;
      params.logFile = logFile;
      params.oldLogFile = oldLogFile;
      params.version = version;
      params.resultSetStepName = resultSetStepName;
      params.resultSetCopyNumber = NumberUtils.isNumber( resultSetCopyNumber ) ? resultSetCopyNumber : "0" /* default */;
      params.base64Zip = base64Zip;
      params.namedParams = namedParams;
      params.customNamedParams = customNamedParams;

      return params;
    }
  }

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

  public void setCustomNamedParams( NamedParams customNamedParams ) {
    this.customNamedParams = customNamedParams;
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
