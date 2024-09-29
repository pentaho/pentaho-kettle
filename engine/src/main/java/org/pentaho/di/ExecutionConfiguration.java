/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface ExecutionConfiguration extends Cloneable {

  public Object clone();

  public Repository connectRepository( RepositoriesMeta repositoriesMeta, String repositoryName, String username,
      String password ) throws KettleException;

  public Map<String, String> getArguments();

  public void setArguments( Map<String, String> arguments );

  public String[] getArgumentStrings();

  public void setArgumentStrings( String[] arguments );

  public LogLevel getLogLevel();

  public void setLogLevel( LogLevel logLevel );

  public Map<String, String> getParams();

  public void setParams( Map<String, String> params );

  public Long getPassedBatchId();

  public void setPassedBatchId( Long passedBatchId );

  public Result getPreviousResult();

  public void setPreviousResult( Result previousResult );

  public SlaveServer getRemoteServer();

  public void setRemoteServer( SlaveServer remoteServer );

  public Date getReplayDate();

  public void setReplayDate( Date replayDate );

  public Repository getRepository();

  public void setRepository( Repository repository );

  public Map<String, String> getVariables();

  public void setVariables( Map<String, String> variables );

  public void setVariables( VariableSpace space );

  public String getXML() throws IOException;

  public boolean isClearingLog();

  public void setClearingLog( boolean clearingLog );

  public boolean isExecutingLocally();

  public void setExecutingLocally( boolean localExecution );

  public boolean isExecutingRemotely();

  public void setExecutingRemotely( boolean remoteExecution );

  public boolean isGatheringMetrics();

  public void setGatheringMetrics( boolean gatheringMetrics );

  public boolean isPassingExport();

  public void setPassingExport( boolean passingExport );

  public boolean isSafeModeEnabled();

  public void setSafeModeEnabled( boolean usingSafeMode );

  String getRunConfiguration();

  void setRunConfiguration( String runConfiguration );
}
