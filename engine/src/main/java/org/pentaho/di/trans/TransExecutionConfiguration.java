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

package org.pentaho.di.trans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.w3c.dom.Node;

public class TransExecutionConfiguration implements ExecutionConfiguration {
  public static final String XML_TAG = "transformation_execution_configuration";

  private final LogChannelInterface log = LogChannel.GENERAL;

  private boolean executingLocally;

  private boolean executingRemotely;
  private SlaveServer remoteServer;
  private boolean passingExport;

  private boolean executingClustered;
  private boolean clusterPosting;
  private boolean clusterPreparing;
  private boolean clusterStarting;
  private boolean clusterShowingTransformation;

  private Map<String, String> arguments;
  private Map<String, String> params;
  private Map<String, String> variables;

  private Date replayDate;
  private boolean safeModeEnabled;
  private LogLevel logLevel;
  private boolean clearingLog;

  private TransDebugMeta transDebugMeta;

  private Result previousResult;
  private Repository repository;

  private boolean gatheringMetrics;
  private boolean showingSubComponents;
  private boolean setLogfile;
  private boolean setAppendLogfile;
  private String logFileName;
  private boolean createParentFolder;
  private Long passedBatchId;

  private String runConfiguration;
  private boolean logRemoteExecutionLocally;

  public TransExecutionConfiguration() {
    executingLocally = true;

    clusterPosting = true;
    clusterPreparing = true;
    clusterStarting = true;
    clusterShowingTransformation = false;

    passingExport = false;

    arguments = new HashMap<String, String>();
    params = new HashMap<String, String>();
    variables = new HashMap<String, String>();

    transDebugMeta = null;

    logLevel = LogLevel.BASIC;

    clearingLog = true;

    gatheringMetrics = false;
    showingSubComponents = true;
  }

  public Object clone() {
    try {
      TransExecutionConfiguration configuration = (TransExecutionConfiguration) super.clone();

      configuration.params = new HashMap<String, String>();
      configuration.params.putAll( params );

      configuration.arguments = new HashMap<String, String>();
      configuration.arguments.putAll( arguments );

      configuration.variables = new HashMap<String, String>();
      configuration.variables.putAll( variables );

      return configuration;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * @return the arguments
   */
  public Map<String, String> getArguments() {
    return arguments;
  }

  /**
   * @param arguments
   *          the arguments to set
   */
  public void setArguments( Map<String, String> arguments ) {
    this.arguments = arguments;
  }

  /**
   * @param params
   *          the parameters to set
   */
  public void setParams( Map<String, String> params ) {
    this.params = params;
  }

  /**
   * @return the parameters.
   */
  public Map<String, String> getParams() {
    return params;
  }

  /**
   * @param arguments
   *          the arguments to set
   */
  public void setArgumentStrings( String[] arguments ) {
    this.arguments = new HashMap<String, String>();
    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        this.arguments.put( "arg " + ( i + 1 ), arguments[i] );
      }
    }
  }

  /**
   * @return the clusteredExecution
   */
  public boolean isExecutingClustered() {
    return executingClustered;
  }

  /**
   * @param clusteredExecution
   *          the clusteredExecution to set
   */
  public void setExecutingClustered( boolean clusteredExecution ) {
    this.executingClustered = clusteredExecution;
  }

  /**
   * @return the notExecuting
   */
  public boolean isClusterStarting() {
    return clusterStarting;
  }

  /**
   * @param notExecuting
   *          the notExecuting to set
   */
  public void setClusterStarting( boolean notExecuting ) {
    this.clusterStarting = notExecuting;
  }

  /**
   * @return the showingTransformations
   */
  public boolean isClusterShowingTransformation() {
    return clusterShowingTransformation;
  }

  /**
   * @param showingTransformations
   *          the showingTransformations to set
   */
  public void setClusterShowingTransformation( boolean showingTransformations ) {
    this.clusterShowingTransformation = showingTransformations;
  }

  /**
   * @return the variables
   */
  public Map<String, String> getVariables() {
    return variables;
  }

  /**
   * @param variables
   *          the variables to set
   */
  public void setVariables( Map<String, String> variables ) {
    this.variables = variables;
  }

  public void setVariables( VariableSpace space ) {
    this.variables = new HashMap<String, String>();

    for ( String name : space.listVariables() ) {
      String value = space.getVariable( name );
      this.variables.put( name, value );
    }
  }

  /**
   * @return the remoteExecution
   */
  public boolean isExecutingRemotely() {
    return executingRemotely;
  }

  /**
   * @param remoteExecution
   *          the remoteExecution to set
   */
  public void setExecutingRemotely( boolean remoteExecution ) {
    this.executingRemotely = remoteExecution;
  }

  /**
   * @return the clusterPosting
   */
  public boolean isClusterPosting() {
    return clusterPosting;
  }

  /**
   * @param clusterPosting
   *          the clusterPosting to set
   */
  public void setClusterPosting( boolean clusterPosting ) {
    this.clusterPosting = clusterPosting;
  }

  /**
   * @return the localExecution
   */
  public boolean isExecutingLocally() {
    return executingLocally;
  }

  /**
   * @param localExecution
   *          the localExecution to set
   */
  public void setExecutingLocally( boolean localExecution ) {
    this.executingLocally = localExecution;
  }

  /**
   * @return the clusterPreparing
   */
  public boolean isClusterPreparing() {
    return clusterPreparing;
  }

  /**
   * @param clusterPreparing
   *          the clusterPreparing to set
   */
  public void setClusterPreparing( boolean clusterPreparing ) {
    this.clusterPreparing = clusterPreparing;
  }

  /**
   * @return the remoteServer
   */
  public SlaveServer getRemoteServer() {
    return remoteServer;
  }

  /**
   * @param remoteServer
   *          the remoteServer to set
   */
  public void setRemoteServer( SlaveServer remoteServer ) {
    this.remoteServer = remoteServer;
  }

  public void getAllVariables( TransMeta transMeta ) {
    Properties sp = new Properties();
    VariableSpace space = Variables.getADefaultVariableSpace();

    String[] keys = space.listVariables();
    for ( int i = 0; i < keys.length; i++ ) {
      sp.put( keys[i], space.getVariable( keys[i] ) );
    }

    String[] vars = transMeta.listVariables();
    if ( vars != null && vars.length > 0 ) {
      HashMap<String, String> newVariables = new HashMap<String, String>();

      for ( int i = 0; i < vars.length; i++ ) {
        String varname = vars[i];
        newVariables.put( varname, Const.NVL( variables.get( varname ), sp.getProperty( varname, "" ) ) );
      }
      // variables.clear();
      variables.putAll( newVariables );
    }

    // Also add the internal job variables if these are set...
    //
    for ( String variableName : Const.INTERNAL_JOB_VARIABLES ) {
      String value = transMeta.getVariable( variableName );
      if ( !Utils.isEmpty( value ) ) {
        variables.put( variableName, value );
      }
    }
  }

  public void getUsedVariables( TransMeta transMeta ) {
    Properties sp = new Properties();
    VariableSpace space = Variables.getADefaultVariableSpace();

    String[] keys = space.listVariables();
    for ( int i = 0; i < keys.length; i++ ) {
      sp.put( keys[i], space.getVariable( keys[i] ) );
    }

    List<String> vars = transMeta.getUsedVariables();
    if ( vars != null && vars.size() > 0 ) {
      HashMap<String, String> newVariables = new HashMap<String, String>();

      for ( int i = 0; i < vars.size(); i++ ) {
        String varname = vars.get( i );
        if ( !varname.startsWith( Const.INTERNAL_VARIABLE_PREFIX ) ) {
          newVariables.put( varname, Const.NVL( variables.get( varname ), sp.getProperty( varname, "" ) ) );
        }
      }
      // variables.clear();
      variables.putAll( newVariables );
    }

    // Also add the internal job variables if these are set...
    //
    for ( String variableName : Const.INTERNAL_JOB_VARIABLES ) {
      String value = transMeta.getVariable( variableName );
      if ( !Utils.isEmpty( value ) ) {
        variables.put( variableName, value );
      }
    }
  }

  public void getUsedArguments( TransMeta transMeta, String[] commandLineArguments ) {
    // OK, see if we need to ask for some arguments first...
    //
    Map<String, String> map = transMeta.getUsedArguments( commandLineArguments );
    for ( String key : map.keySet() ) {
      String value = map.get( key );
      if ( !arguments.containsKey( key ) ) {
        arguments.put( key, value );
      }
    }
  }

  /**
   * @return the replayDate
   */
  public Date getReplayDate() {
    return replayDate;
  }

  /**
   * @param replayDate
   *          the replayDate to set
   */
  public void setReplayDate( Date replayDate ) {
    this.replayDate = replayDate;
  }

  /**
   * @return the usingSafeMode
   */
  public boolean isSafeModeEnabled() {
    return safeModeEnabled;
  }

  /**
   * @param usingSafeMode
   *          the usingSafeMode to set
   */
  public void setSafeModeEnabled( boolean usingSafeMode ) {
    this.safeModeEnabled = usingSafeMode;
  }

  /**
   * @return the logLevel
   */
  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * @param logLevel
   *          the logLevel to set
   */
  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
  }

  public String getXML() throws IOException {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "  <" + XML_TAG + ">" ).append( Const.CR );

    xml.append( "    " ).append( XMLHandler.addTagValue( "exec_local", executingLocally ) );

    xml.append( "    " ).append( XMLHandler.addTagValue( "exec_remote", executingRemotely ) );
    if ( remoteServer != null ) {
      xml.append( "    " ).append( remoteServer.getXML() ).append( Const.CR );
    }
    xml.append( "    " ).append( XMLHandler.addTagValue( "pass_export", passingExport ) );

    xml.append( "    " ).append( XMLHandler.addTagValue( "exec_cluster", executingClustered ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "cluster_post", clusterPosting ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "cluster_prepare", clusterPreparing ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "cluster_start", clusterStarting ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "cluster_show_trans", clusterShowingTransformation ) );

    // Serialize the parameters...
    //
    xml.append( "    <parameters>" ).append( Const.CR );
    List<String> paramNames = new ArrayList<String>( params.keySet() );
    Collections.sort( paramNames );
    for ( String name : paramNames ) {
      String value = params.get( name );
      xml.append( "    <parameter>" );
      xml.append( XMLHandler.addTagValue( "name", name, false ) );
      xml.append( XMLHandler.addTagValue( "value", value, false ) );
      xml.append( "</parameter>" ).append( Const.CR );
    }
    xml.append( "    </parameters>" ).append( Const.CR );

    // Serialize the variables...
    //
    xml.append( "    <variables>" ).append( Const.CR );
    List<String> variableNames = new ArrayList<String>( variables.keySet() );
    Collections.sort( variableNames );
    for ( String name : variableNames ) {
      String value = variables.get( name );
      xml.append( "    <variable>" );
      xml.append( XMLHandler.addTagValue( "name", name, false ) );
      xml.append( XMLHandler.addTagValue( "value", value, false ) );
      xml.append( "</variable>" ).append( Const.CR );
    }
    xml.append( "    </variables>" ).append( Const.CR );

    // Serialize the variables...
    //
    xml.append( "    <arguments>" ).append( Const.CR );
    List<String> argumentNames = new ArrayList<String>( arguments.keySet() );
    Collections.sort( argumentNames );
    for ( String name : argumentNames ) {
      String value = arguments.get( name );
      xml.append( "    <argument>" );
      xml.append( XMLHandler.addTagValue( "name", name, false ) );
      xml.append( XMLHandler.addTagValue( "value", value, false ) );
      xml.append( "</argument>" ).append( Const.CR );
    }
    xml.append( "    </arguments>" ).append( Const.CR );

    // IMPORTANT remote debugging is not yet supported
    //
    // xml.append(transDebugMeta.getXML());

    xml.append( "    " ).append( XMLHandler.addTagValue( "replay_date", replayDate ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "safe_mode", safeModeEnabled ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "log_level", logLevel.getCode() ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "log_file", setLogfile ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "log_filename", logFileName ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "log_file_append", setAppendLogfile ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "create_parent_folder", createParentFolder ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "clear_log", clearingLog ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "gather_metrics", gatheringMetrics ) );
    xml.append( "    " ).append( XMLHandler.addTagValue( "show_subcomponents", showingSubComponents ) );
    if ( passedBatchId != null ) {
      xml.append( "    " ).append( XMLHandler.addTagValue( "passedBatchId", passedBatchId ) );
    }
    xml.append( "    " ).append( XMLHandler.addTagValue( "run_configuration", runConfiguration ) );

    // The source rows...
    //
    if ( previousResult != null ) {
      xml.append( previousResult.getXML() );
    }

    // Send the repository name and user to the remote site...
    //
    if ( repository != null ) {
      xml.append( XMLHandler.openTag( "repository" ) );
      xml.append( XMLHandler.addTagValue( "name", repository.getName() ) );
      // File base repositories doesn't have user info
      if ( repository.getUserInfo() != null ) {
        xml.append( XMLHandler.addTagValue( "login", repository.getUserInfo().getLogin() ) );
        xml.append( XMLHandler.addTagValue( "password", Encr.encryptPassword( repository
          .getUserInfo().getPassword() ) ) );
      }
      xml.append( XMLHandler.closeTag( "repository" ) );
    }

    xml.append( "</" + XML_TAG + ">" ).append( Const.CR );
    return xml.toString();
  }

  public TransExecutionConfiguration( Node trecNode ) throws KettleException {
    this();

    executingLocally = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "exec_local" ) );

    executingRemotely = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "exec_remote" ) );
    Node remoteHostNode = XMLHandler.getSubNode( trecNode, SlaveServer.XML_TAG );
    if ( remoteHostNode != null ) {
      remoteServer = new SlaveServer( remoteHostNode );
    }
    passingExport = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "pass_export" ) );

    executingClustered = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "exec_cluster" ) );
    clusterPosting = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "cluster_post" ) );
    clusterPreparing = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "cluster_prepare" ) );
    clusterStarting = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "cluster_start" ) );
    clusterShowingTransformation = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "cluster_show_trans" ) );

    // Read the variables...
    //
    Node varsNode = XMLHandler.getSubNode( trecNode, "variables" );
    int nrVariables = XMLHandler.countNodes( varsNode, "variable" );
    for ( int i = 0; i < nrVariables; i++ ) {
      Node argNode = XMLHandler.getSubNodeByNr( varsNode, "variable", i );
      String name = XMLHandler.getTagValue( argNode, "name" );
      String value = XMLHandler.getTagValue( argNode, "value" );
      if ( !Utils.isEmpty( name ) ) {
        variables.put( name, Const.NVL( value, "" ) );
      }
    }

    // Read the arguments...
    //
    Node argsNode = XMLHandler.getSubNode( trecNode, "arguments" );
    int nrArguments = XMLHandler.countNodes( argsNode, "argument" );
    for ( int i = 0; i < nrArguments; i++ ) {
      Node argNode = XMLHandler.getSubNodeByNr( argsNode, "argument", i );
      String name = XMLHandler.getTagValue( argNode, "name" );
      String value = XMLHandler.getTagValue( argNode, "value" );
      if ( !Utils.isEmpty( name ) && !Utils.isEmpty( value ) ) {
        arguments.put( name, value );
      }
    }

    // Read the parameters...
    //
    Node parmsNode = XMLHandler.getSubNode( trecNode, "parameters" );
    int nrParams = XMLHandler.countNodes( parmsNode, "parameter" );
    for ( int i = 0; i < nrParams; i++ ) {
      Node parmNode = XMLHandler.getSubNodeByNr( parmsNode, "parameter", i );
      String name = XMLHandler.getTagValue( parmNode, "name" );
      String value = XMLHandler.getTagValue( parmNode, "value" );
      if ( !Utils.isEmpty( name ) ) {
        params.put( name, value );
      }
    }

    // IMPORTANT: remote preview and remote debugging is NOT yet supported.
    //

    replayDate = XMLHandler.stringToDate( XMLHandler.getTagValue( trecNode, "replay_date" ) );
    safeModeEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "safe_mode" ) );
    logLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue( trecNode, "log_level" ) );
    setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "log_file" ) );
    logFileName = XMLHandler.getTagValue( trecNode, "log_filename" );
    setAppendLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "log_file_append" ) );
    createParentFolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "create_parent_folder" ) );
    clearingLog = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "clear_log" ) );
    gatheringMetrics = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "gather_metrics" ) );
    showingSubComponents = "Y".equalsIgnoreCase( XMLHandler.getTagValue( trecNode, "show_subcomponents" ) );
    String sPassedBatchId = XMLHandler.getTagValue( trecNode, "passedBatchId" );
    if ( !StringUtils.isEmpty( sPassedBatchId ) ) {
      passedBatchId = Long.parseLong( sPassedBatchId );
    }
    runConfiguration = XMLHandler.getTagValue( trecNode, "run_configuration" );

    Node resultNode = XMLHandler.getSubNode( trecNode, Result.XML_TAG );
    if ( resultNode != null ) {
      try {
        previousResult = new Result( resultNode );
      } catch ( KettleException e ) {
        throw new KettleException( "Unable to hydrate previous result", e );
      }
    }

    // Try to get a handle to the repository from here...
    //
    Node repNode = XMLHandler.getSubNode( trecNode, "repository" );
    if ( repNode != null ) {
      String repositoryName = XMLHandler.getTagValue( repNode, "name" );
      String username = XMLHandler.getTagValue( repNode, "login" );
      String password = Encr.decryptPassword( XMLHandler.getTagValue( repNode, "password" ) );

      RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
      repositoriesMeta.getLog().setLogLevel( log.getLogLevel() );
      try {
        repositoriesMeta.readData();
      } catch ( Exception e ) {
        throw new KettleException( "Unable to get a list of repositories to locate repository '" + repositoryName + "'" );
      }
      connectRepository( repositoriesMeta, repositoryName, username, password );
    }
  }

  public Repository connectRepository( RepositoriesMeta repositoriesMeta, String repositoryName, String username, String password ) throws KettleException {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryName );
    if ( repositoryMeta == null ) {
      log.logBasic( "I couldn't find the repository with name '" + repositoryName + "'" );
      return null;
    }
    Repository rep = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    if ( rep == null ) {
      log.logBasic( "Unable to load repository plugin for '" + repositoryName + "'" );
      return null;
    }
    rep.init( repositoryMeta );

    try {
      rep.connect( username, password );
      setRepository( rep );
      return rep;
    } catch ( Exception e ) {
      log.logBasic( "Unable to connect to the repository with name '" + repositoryName + "'" );
      return null;
    }
  }

  public String[] getArgumentStrings() {
    if ( arguments == null || arguments.size() == 0 ) {
      return null;
    }

    String[] argNames = arguments.keySet().toArray( new String[arguments.size()] );
    Arrays.sort( argNames );

    String[] values = new String[argNames.length];
    for ( int i = 0; i < argNames.length; i++ ) {
      if ( argNames[i].equalsIgnoreCase( Props.STRING_ARGUMENT_NAME_PREFIX + ( i + 1 ) ) ) {
        values[i] = arguments.get( argNames[i] );
      }
    }

    return values;
  }

  /**
   * @return the transDebugMeta
   */
  public TransDebugMeta getTransDebugMeta() {
    return transDebugMeta;
  }

  /**
   * @param transDebugMeta
   *          the transDebugMeta to set
   */
  public void setTransDebugMeta( TransDebugMeta transDebugMeta ) {
    this.transDebugMeta = transDebugMeta;
  }

  /**
   * @return the previousResult
   */
  public Result getPreviousResult() {
    return previousResult;
  }

  /**
   * @param previousResult
   *          the previousResult to set
   */
  public void setPreviousResult( Result previousResult ) {
    this.previousResult = previousResult;
  }

  /**
   * @return the repository
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * @param repository
   *          the repository to set
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  /**
   * @return the clearingLog
   */
  public boolean isClearingLog() {
    return clearingLog;
  }

  /**
   * @param clearingLog
   *          the clearingLog to set
   */
  public void setClearingLog( boolean clearingLog ) {
    this.clearingLog = clearingLog;
  }

  /**
   * @return the passingExport
   */
  public boolean isPassingExport() {
    return passingExport;
  }

  /**
   * @param passingExport
   *          the passingExport to set
   */
  public void setPassingExport( boolean passingExport ) {
    this.passingExport = passingExport;
  }

  /**
   * @return the gatheringMetrics
   */
  public boolean isGatheringMetrics() {
    return gatheringMetrics;
  }

  /**
   * @param gatheringMetrics
   *          the gatheringMetrics to set
   */
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    this.gatheringMetrics = gatheringMetrics;
  }

  /**
   * @return the showingSubComponents
   */
  public boolean isShowingSubComponents() {
    return showingSubComponents;
  }

  /**
   * @param showingSubComponents
   *          the showingSubComponents to set
   */
  public void setShowingSubComponents( boolean showingSubComponents ) {
    this.showingSubComponents = showingSubComponents;
  }

  public boolean isSetLogfile() {
    return setLogfile;
  }

  public void setSetLogfile( boolean setLogfile ) {
    this.setLogfile = setLogfile;
  }

  public boolean isSetAppendLogfile() {
    return setAppendLogfile;
  }

  public void setSetAppendLogfile( boolean setAppendLogfile ) {
    this.setAppendLogfile = setAppendLogfile;
  }

  public String getLogFileName() {
    return logFileName;
  }

  public void setLogFileName( String fileName ) {
    this.logFileName = fileName;
  }

  public boolean isCreateParentFolder() {
    return createParentFolder;
  }

  public void setCreateParentFolder( boolean createParentFolder ) {
    this.createParentFolder = createParentFolder;
  }

  public Long getPassedBatchId() {
    return passedBatchId;
  }

  public void setPassedBatchId( Long passedBatchId ) {
    this.passedBatchId = passedBatchId;
  }

  public String getRunConfiguration() {
    return runConfiguration;
  }

  public void setRunConfiguration( String runConfiguration ) {
    this.runConfiguration = runConfiguration;
  }

  public boolean isLogRemoteExecutionLocally() {
    return logRemoteExecutionLocally;
  }

  public void setLogRemoteExecutionLocally( boolean logRemoteExecutionLocally ) {
    this.logRemoteExecutionLocally = logRemoteExecutionLocally;
  }
}
