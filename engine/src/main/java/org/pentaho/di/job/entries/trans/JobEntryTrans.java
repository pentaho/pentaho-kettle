/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.trans;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.base.IMetaFileLoader;
import org.pentaho.di.base.MetaFileLoaderImpl;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.listeners.CurrentDirectoryChangedListener;
import org.pentaho.di.core.listeners.impl.EntryCurrentDirectoryChangedListener;
import org.pentaho.di.core.logging.LogChannelFileWriter;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.util.FileUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.JobEntryRunConfigurableInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.HasRepositoryDirectories;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportLocation;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * This is the job entry that defines a transformation to be run.
 *
 * @author Matt Casters
 * @since 1-Oct-2003, rewritten on 18-June-2004
 */
public class JobEntryTrans extends JobEntryBase implements Cloneable, JobEntryInterface, HasRepositoryDirectories, JobEntryRunConfigurableInterface {
  private static Class<?> PKG = JobEntryTrans.class; // for i18n purposes, needed by Translator2!!
  public static final int IS_PENTAHO = 1;

  private String transname;

  private String filename;

  private String directory;

  private ObjectId transObjectId;

  private ObjectLocationSpecificationMethod specificationMethod;

  public String[] arguments;

  public boolean argFromPrevious;

  public boolean paramsFromPrevious;

  public boolean execPerRow;

  public String[] parameters;

  public String[] parameterFieldNames;

  public String[] parameterValues;

  public boolean clearResultRows;

  public boolean clearResultFiles;

  public boolean createParentFolder;

  public boolean setLogfile;

  public boolean setAppendLogfile;

  public boolean suppressResultData;

  public String logfile, logext;

  public boolean addDate, addTime;

  public LogLevel logFileLevel;

  private String directoryPath;

  private boolean clustering;

  public boolean waitingToFinish = true;

  public boolean followingAbortRemotely;

  private String remoteSlaveServerName;

  private boolean passingAllParameters = true;

  private boolean loggingRemoteWork;

  private String runConfiguration;

  private Trans trans;

  private CurrentDirectoryChangedListener currentDirListener = new EntryCurrentDirectoryChangedListener(
      this::getSpecificationMethod,
      this::getDirectory,
      this::setDirectory );

  public JobEntryTrans( String name ) {
    super( name, "" );
  }

  public JobEntryTrans() {
    this( "" );
    clear();
  }

  private void allocateArgs( int nrArgs ) {
    arguments = new String[nrArgs];
  }

  private void allocateParams( int nrParameters ) {
    parameters = new String[nrParameters];
    parameterFieldNames = new String[nrParameters];
    parameterValues = new String[nrParameters];
  }

  @Override
  public Object clone() {
    JobEntryTrans je = (JobEntryTrans) super.clone();
    if ( arguments != null ) {
      int nrArgs = arguments.length;
      je.allocateArgs( nrArgs );
      System.arraycopy( arguments, 0, je.arguments, 0, nrArgs );
    }
    if ( parameters != null ) {
      int nrParameters = parameters.length;
      je.allocateParams( nrParameters );
      System.arraycopy( parameters, 0, je.parameters, 0, nrParameters );
      System.arraycopy( parameterFieldNames, 0, je.parameterFieldNames, 0, nrParameters );
      System.arraycopy( parameterValues, 0, je.parameterValues, 0, nrParameters );
    }
    return je;
  }

  public void setFileName( String n ) {
    filename = n;
  }

  /**
   * @return the filename
   * @deprecated use getFilename() instead
   */
  @Deprecated
  public String getFileName() {
    return filename;
  }

  @Override
  public String getFilename() {
    return filename;
  }

  @Override
  public String getRealFilename() {
    return environmentSubstitute( getFilename() );
  }

  public void setTransname( String transname ) {
    this.transname = transname;
  }

  public String getTransname() {
    return transname;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory( String directory ) {
    this.directory = directory;
  }

  @Override
  public String[] getDirectories() {
    return new String[]{ directory };
  }

  @Override
  public void setDirectories( String[] directories ) {
    this.directory = directories[0];
  }

  public String getLogFilename() {
    String retval = "";
    if ( setLogfile ) {
      retval += logfile == null ? "" : logfile;
      Calendar cal = Calendar.getInstance();
      if ( addDate ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
        retval += "_" + sdf.format( cal.getTime() );
      }
      if ( addTime ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "HHmmss" );
        retval += "_" + sdf.format( cal.getTime() );
      }
      if ( logext != null && logext.length() > 0 ) {
        retval += "." + logext;
      }
    }
    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );

    // specificationMethod
    //
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "specification_method", specificationMethod == null ? null : specificationMethod
        .getCode() )
    );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "trans_object_id", transObjectId == null ? null : transObjectId.toString() ) );
    // Export a little bit of extra information regarding the reference since it doesn't really matter outside the same
    // repository.
    //
    if ( rep != null && transObjectId != null ) {
      try {
        RepositoryObject objectInformation =
          rep.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
        if ( objectInformation != null ) {
          transname = objectInformation.getName();
          directory = objectInformation.getRepositoryDirectory().getPath();
        }
      } catch ( KettleException e ) {
        // Ignore object reference problems. It simply means that the reference is no longer valid.
      }
    }

    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "transname", transname ) );
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( filename );
    }
    if ( directory != null ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "directory", directory ) );
    } else if ( directoryPath != null ) {
      // don't loose this info (backup/recovery)
      //
      retval.append( "      " ).append( XMLHandler.addTagValue( "directory", directoryPath ) );
    }
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "params_from_previous", paramsFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "exec_per_row", execPerRow ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "clear_rows", clearResultRows ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "clear_files", clearResultFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_logfile", setLogfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logfile", logfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logext", logext ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", addDate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", addTime ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "loglevel", logFileLevel != null ? logFileLevel.getCode() : null ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "cluster", clustering ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "slave_server_name", remoteSlaveServerName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_append_logfile", setAppendLogfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wait_until_finished", waitingToFinish ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "follow_abort_remote", followingAbortRemotely ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "create_parent_folder", createParentFolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logging_remote_work", loggingRemoteWork ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "run_configuration", runConfiguration ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "suppress_result_data", isSuppressResultData() ) );

    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        // This is a very very bad way of making an XML file, don't use it (or
        // copy it). Sven Boden
        retval.append( "      " ).append( XMLHandler.addTagValue( "argument" + i, arguments[ i ] ) );
      }
    }

    if ( parameters != null ) {
      retval.append( "      " ).append( XMLHandler.openTag( "parameters" ) ).append( Const.CR );

      retval.append( "        " ).append( XMLHandler.addTagValue( "pass_all_parameters", passingAllParameters ) );

      for ( int i = 0; i < parameters.length; i++ ) {
        // This is a better way of making the XML file than the arguments.
        retval.append( "        " ).append( XMLHandler.openTag( "parameter" ) ).append( Const.CR );

        retval.append( "          " ).append( XMLHandler.addTagValue( "name", parameters[ i ] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "stream_name", parameterFieldNames[ i ] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "value", parameterValues[ i ] ) );

        retval.append( "        " ).append( XMLHandler.closeTag( "parameter" ) ).append( Const.CR );
      }
      retval.append( "      " ).append( XMLHandler.closeTag( "parameters" ) ).append( Const.CR );
    }

    return retval.toString();
  }

  private void checkObjectLocationSpecificationMethod() {
    if ( specificationMethod == null ) {
      // Backward compatibility
      //
      // Default = Filename
      //
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;

      if ( !Utils.isEmpty( filename ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
      } else if ( transObjectId != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
      } else if ( !Utils.isEmpty( transname ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    }
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
                       Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      String method = XMLHandler.getTagValue( entrynode, "specification_method" );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );

      String transId = XMLHandler.getTagValue( entrynode, "trans_object_id" );
      transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );
      filename = XMLHandler.getTagValue( entrynode, "filename" );
      transname = XMLHandler.getTagValue( entrynode, "transname" );
      directory = XMLHandler.getTagValue( entrynode, "directory" );

      if ( rep != null && rep.isConnected() && !Utils.isEmpty( transname ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }

      // Backward compatibility check for object specification
      //
      checkObjectLocationSpecificationMethod();

      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      paramsFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "params_from_previous" ) );
      execPerRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "exec_per_row" ) );
      clearResultRows = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "clear_rows" ) );
      clearResultFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "clear_files" ) );
      setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_logfile" ) );
      addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_date" ) );
      addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_time" ) );
      logfile = XMLHandler.getTagValue( entrynode, "logfile" );
      logext = XMLHandler.getTagValue( entrynode, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue( entrynode, "loglevel" ) );
      clustering = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "cluster" ) );
      createParentFolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "create_parent_folder" ) );
      loggingRemoteWork = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "logging_remote_work" ) );
      runConfiguration = XMLHandler.getTagValue( entrynode, "run_configuration" );
      setSuppressResultData( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "suppress_result_data" ) ) );

      remoteSlaveServerName = XMLHandler.getTagValue( entrynode, "slave_server_name" );

      setAppendLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_append_logfile" ) );
      String wait = XMLHandler.getTagValue( entrynode, "wait_until_finished" );
      if ( Utils.isEmpty( wait ) ) {
        waitingToFinish = true;
      } else {
        waitingToFinish = "Y".equalsIgnoreCase( wait );
      }

      followingAbortRemotely = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "follow_abort_remote" ) );

      // How many arguments?
      int argnr = 0;
      while ( XMLHandler.getTagValue( entrynode, "argument" + argnr ) != null ) {
        argnr++;
      }
      allocateArgs( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[ a ] = XMLHandler.getTagValue( entrynode, "argument" + a );
      }

      Node parametersNode = XMLHandler.getSubNode( entrynode, "parameters" );

      String passAll = XMLHandler.getTagValue( parametersNode, "pass_all_parameters" );
      passingAllParameters = Utils.isEmpty( passAll ) || "Y".equalsIgnoreCase( passAll );

      int nrParameters = XMLHandler.countNodes( parametersNode, "parameter" );
      allocateParams( nrParameters );

      for ( int i = 0; i < nrParameters; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( parametersNode, "parameter", i );

        parameters[ i ] = XMLHandler.getTagValue( knode, "name" );
        parameterFieldNames[ i ] = XMLHandler.getTagValue( knode, "stream_name" );
        parameterValues[ i ] = XMLHandler.getTagValue( knode, "value" );
      }
    } catch ( KettleException e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'trans' from XML node", e );
    }
  }

  // Load the jobentry from repository
  //
  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
                       List<SlaveServer> slaveServers ) throws KettleException {
    try {
      String method = rep.getJobEntryAttributeString( id_jobentry, "specification_method" );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
      String transId = rep.getJobEntryAttributeString( id_jobentry, "trans_object_id" );
      transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );
      transname = rep.getJobEntryAttributeString( id_jobentry, "name" );
      directory = rep.getJobEntryAttributeString( id_jobentry, "dir_path" );
      filename = rep.getJobEntryAttributeString( id_jobentry, "file_name" );

      // Backward compatibility check for object specification
      //
      checkObjectLocationSpecificationMethod();

      argFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      paramsFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "params_from_previous" );
      execPerRow = rep.getJobEntryAttributeBoolean( id_jobentry, "exec_per_row" );
      clearResultRows = rep.getJobEntryAttributeBoolean( id_jobentry, "clear_rows", true );
      clearResultFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "clear_files", true );
      setLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_logfile" );
      addDate = rep.getJobEntryAttributeBoolean( id_jobentry, "add_date" );
      addTime = rep.getJobEntryAttributeBoolean( id_jobentry, "add_time" );
      logfile = rep.getJobEntryAttributeString( id_jobentry, "logfile" );
      logext = rep.getJobEntryAttributeString( id_jobentry, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( rep.getJobEntryAttributeString( id_jobentry, "loglevel" ) );
      clustering = rep.getJobEntryAttributeBoolean( id_jobentry, "cluster" );
      createParentFolder = rep.getJobEntryAttributeBoolean( id_jobentry, "create_parent_folder" );

      remoteSlaveServerName = rep.getJobEntryAttributeString( id_jobentry, "slave_server_name" );
      setAppendLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_append_logfile" );
      waitingToFinish = rep.getJobEntryAttributeBoolean( id_jobentry, "wait_until_finished", true );
      followingAbortRemotely = rep.getJobEntryAttributeBoolean( id_jobentry, "follow_abort_remote" );
      loggingRemoteWork = rep.getJobEntryAttributeBoolean( id_jobentry, "logging_remote_work" );
      runConfiguration = rep.getJobEntryAttributeString( id_jobentry, "run_configuration" );
      setSuppressResultData( rep.getJobEntryAttributeBoolean( id_jobentry, "suppress_result_data", false ) );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "argument" );
      allocateArgs( argnr );

      // Read all arguments...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[ a ] = rep.getJobEntryAttributeString( id_jobentry, a, "argument" );
      }

      // How many arguments?
      int parameternr = rep.countNrJobEntryAttributes( id_jobentry, "parameter_name" );
      allocateParams( parameternr );

      // Read all parameters ...
      for ( int a = 0; a < parameternr; a++ ) {
        parameters[ a ] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_name" );
        parameterFieldNames[ a ] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_stream_name" );
        parameterValues[ a ] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_value" );
      }

      passingAllParameters = rep.getJobEntryAttributeBoolean( id_jobentry, "pass_all_parameters", true );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'trans' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  // Save the attributes of this job entry
  //
  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "specification_method", specificationMethod == null
        ? null : specificationMethod.getCode() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "trans_object_id", transObjectId == null
        ? null : transObjectId.toString() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "name", getTransname() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "dir_path", getDirectory() != null ? getDirectory() : "" );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "file_name", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", argFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "params_from_previous", paramsFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "exec_per_row", execPerRow );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "clear_rows", clearResultRows );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "clear_files", clearResultFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_logfile", setLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_date", addDate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_time", addTime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logfile", logfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logext", logext );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "loglevel", logFileLevel != null
        ? logFileLevel.getCode() : null );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "cluster", clustering );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "slave_server_name", remoteSlaveServerName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_append_logfile", setAppendLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wait_until_finished", waitingToFinish );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "follow_abort_remote", followingAbortRemotely );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "create_parent_folder", createParentFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logging_remote_work", loggingRemoteWork );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "run_configuration", runConfiguration );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "suppress_result_data", isSuppressResultData() );

      // Save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "argument", arguments[ i ] );
        }
      }

      // Save the parameters...
      if ( parameters != null ) {
        for ( int i = 0; i < parameters.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_name", parameters[ i ] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_stream_name", Const.NVL(
            parameterFieldNames[ i ], "" ) );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_value", Const.NVL(
            parameterValues[ i ], "" ) );
        }
      }

      rep.saveJobEntryAttribute( id_job, getObjectId(), "pass_all_parameters", passingAllParameters );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'trans' to the repository for id_job=" + id_job, dbe );
    }
  }

  @Override
  public void clear() {
    super.clear();

    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    transname = null;
    filename = null;
    directory = null;
    arguments = null;
    argFromPrevious = false;
    execPerRow = false;
    addDate = false;
    addTime = false;
    logfile = null;
    logext = null;
    setLogfile = false;
    clearResultRows = false;
    clearResultFiles = false;
    remoteSlaveServerName = null;
    setAppendLogfile = false;
    waitingToFinish = true;
    followingAbortRemotely = false; // backward compatibility reasons
    createParentFolder = false;
    setSuppressResultData( false );
    logFileLevel = LogLevel.BASIC;
  }

  /**
   * Execute this job entry and return the result. In this case it means, just set the result boolean in the Result
   * class.
   *
   * @param result The result of the previous execution
   * @param nr     the job entry number
   * @return The Result of the execution.
   */
  @Override
  public Result execute( Result result, int nr ) throws KettleException {
    result.setEntryNr( nr );

    LogChannelFileWriter logChannelFileWriter = null;

    LogLevel transLogLevel = parentJob.getLogLevel();

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    String realLogFilename = "";
    if ( setLogfile ) {
      transLogLevel = logFileLevel;

      realLogFilename = environmentSubstitute( getLogFilename() );

      // We need to check here the log filename
      // if we do not have one, we must fail
      if ( Utils.isEmpty( realLogFilename ) ) {
        logError( BaseMessages.getString( PKG, "JobTrans.Exception.LogFilenameMissing" ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
      // create parent folder?
      if ( !FileUtil.createParentFolder( PKG, realLogFilename, createParentFolder, this.getLogChannel(), this ) ) {
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
      try {
        logChannelFileWriter =
          new LogChannelFileWriter(
            this.getLogChannelId(), KettleVFS.getFileObject( realLogFilename, this ), setAppendLogfile );
        logChannelFileWriter.startLogging();
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "JobTrans.Error.UnableOpenAppender", realLogFilename, e.toString() ) );

        logError( Const.getStackTracker( e ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
    }

    // Open the transformation...
    //
    switch ( specificationMethod ) {
      case FILENAME:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobTrans.Log.OpeningTrans", environmentSubstitute( getFilename() ) ) );
        }
        break;
      case REPOSITORY_BY_NAME:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobTrans.Log.OpeningTransInDirec", environmentSubstitute( getFilename() ),
            environmentSubstitute( directory ) ) );
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobTrans.Log.OpeningTransByReference", transObjectId ) );
        }
        break;
      default:
        break;
    }

    // Load the transformation only once for the complete loop!
    // Throws an exception if it was not possible to load the transformation. For example, the XML file doesn't exist or
    // the repository is down.
    // Log the stack trace and return an error condition from this
    //
    TransMeta transMeta = null;
    try {
      transMeta = getTransMeta( rep, metaStore, this );
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "JobTrans.Exception.UnableToRunJob", parentJobMeta.getName(),
        getName(), StringUtils.trim( e.getMessage() ) ), e );
      result.setNrErrors( 1 );
      result.setResult( false );
      return result;
    }

    int iteration = 0;
    String[] args1 = arguments;
    if ( args1 == null || args1.length == 0 ) { // No arguments set, look at the parent job.
      args1 = parentJob.getArguments();
    }
    // initializeVariablesFrom(parentJob);

    //
    // For the moment only do variable translation at the start of a job, not
    // for every input row (if that would be switched on). This is for safety,
    // the real argument setting is later on.
    //
    String[] args = null;
    if ( args1 != null ) {
      args = new String[ args1.length ];
      for ( int idx = 0; idx < args1.length; idx++ ) {
        args[ idx ] = environmentSubstitute( args1[ idx ] );
      }
    }

    RowMetaAndData resultRow = null;
    boolean first = true;
    List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>( result.getRows() );

    while ( ( first && !execPerRow )
      || ( execPerRow && !rows.isEmpty() && iteration < rows.size() && result.getNrErrors() == 0 )
      || ( execPerRow && rows.isEmpty() && iteration <= rows.size() && shouldConsiderOldBehaviourForEveryInputRow() )
      && !parentJob.isStopped() ) {
      // Clear the result rows of the result
      // Otherwise we double the amount of rows every iteration in the simple cases.
      //
      if ( execPerRow ) {
        result.getRows().clear();
      }

      if ( rows != null && execPerRow && !rows.isEmpty() ) {
        resultRow = rows.get( iteration );
      } else {
        resultRow = null;
      }

      NamedParams namedParam = new NamedParamsDefault();
      if ( parameters != null ) {
        for ( int idx = 0; idx < parameters.length; idx++ ) {
          if ( !Utils.isEmpty( parameters[ idx ] ) ) {
            // We have a parameter
            //
            namedParam.addParameterDefinition( parameters[ idx ], "", "Job entry runtime" );
            if ( Utils.isEmpty( Const.trim( parameterFieldNames[ idx ] ) ) ) {
              // There is no field name specified.
              //
              String value = Const.NVL( environmentSubstitute( parameterValues[ idx ] ), "" );
              namedParam.setParameterValue( parameters[ idx ], value );
            } else {
              // something filled in, in the field column...
              //
              String value = "";
              if ( resultRow != null ) {
                value = resultRow.getString( parameterFieldNames[ idx ], "" );
              }
              namedParam.setParameterValue( parameters[ idx ], value );
            }
          }
        }
      }

      first = false;

      Result previousResult = result;

      try {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobTrans.StartingTrans", getFilename(), getName(), getDescription() ) );
        }

        if ( clearResultRows ) {
          previousResult.setRows( new ArrayList<RowMetaAndData>() );
        }

        if ( clearResultFiles ) {
          previousResult.getResultFiles().clear();
        }

        /*
         * Set one or more "result" rows on the transformation...
         */
        if ( execPerRow ) {
          // Execute for each input row

          if ( argFromPrevious ) {
            // Copy the input row to the (command line) arguments

            args = null;
            if ( resultRow != null ) {
              args = new String[ resultRow.size() ];
              for ( int i = 0; i < resultRow.size(); i++ ) {
                args[ i ] = resultRow.getString( i, null );
              }
            }
          } else {
            // Just pass a single row
            List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
            newList.add( resultRow );

            // This previous result rows list can be either empty or not.
            // Depending on the checkbox "clear result rows"
            // In this case, it would execute the transformation with one extra row each time
            // Can't figure out a real use-case for it, but hey, who am I to decide that, right?
            // :-)
            //
            previousResult.getRows().addAll( newList );
          }

          if ( paramsFromPrevious ) { // Copy the input the parameters

            if ( parameters != null ) {
              for ( int idx = 0; idx < parameters.length; idx++ ) {
                if ( !Utils.isEmpty( parameters[ idx ] ) ) {
                  // We have a parameter
                  if ( Utils.isEmpty( Const.trim( parameterFieldNames[ idx ] ) ) ) {
                    namedParam.setParameterValue( parameters[ idx ], Const.NVL(
                      environmentSubstitute( parameterValues[ idx ] ), "" ) );
                  } else {
                    String fieldValue = "";

                    if ( resultRow != null ) {
                      fieldValue = resultRow.getString( parameterFieldNames[ idx ], "" );
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue( parameters[ idx ], Const.NVL( fieldValue, "" ) );
                  }
                }
              }
            }
          }
        } else {
          if ( argFromPrevious ) {
            // Only put the first Row on the arguments
            args = null;
            if ( resultRow != null ) {
              args = new String[ resultRow.size() ];
              for ( int i = 0; i < resultRow.size(); i++ ) {
                args[ i ] = resultRow.getString( i, null );
              }
            }
          }

          if ( paramsFromPrevious ) {
            // Copy the input the parameters
            if ( parameters != null ) {
              for ( int idx = 0; idx < parameters.length; idx++ ) {
                if ( !Utils.isEmpty( parameters[ idx ] ) ) {
                  // We have a parameter
                  if ( Utils.isEmpty( Const.trim( parameterFieldNames[ idx ] ) ) ) {
                    namedParam.setParameterValue( parameters[ idx ], Const.NVL(
                      environmentSubstitute( parameterValues[ idx ] ), "" ) );
                  } else {
                    String fieldValue = "";

                    if ( resultRow != null ) {
                      fieldValue = resultRow.getString( parameterFieldNames[ idx ], "" );
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue( parameters[ idx ], Const.NVL( fieldValue, "" ) );
                  }
                }
              }
            }
          }
        }

        // Handle the parameters...
        //
        transMeta.clearParameters();
        String[] parameterNames = transMeta.listParameters();

        prepareFieldNamesParameters( parameters, parameterFieldNames, parameterValues, namedParam, this );

        StepWithMappingMeta.activateParams( transMeta, transMeta, this, parameterNames,
          parameters, parameterValues, isPassingAllParameters() );
        boolean doFallback = true;
        SlaveServer remoteSlaveServer = null;
        TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
        if ( !Utils.isEmpty( runConfiguration ) ) {
          runConfiguration = environmentSubstitute( runConfiguration );
          log.logBasic( BaseMessages.getString( PKG, "JobTrans.RunConfig.Message" ), runConfiguration );
          executionConfiguration.setRunConfiguration( runConfiguration );
          try {
            ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransBeforeStart.id, new Object[] {
              executionConfiguration, parentJob.getJobMeta(), transMeta, rep
            } );
            List<Object> items = Arrays.asList( runConfiguration, false );
            try {
              ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint
                      .RunConfigurationSelection.id, items );
              if ( waitingToFinish && (Boolean) items.get( IS_PENTAHO ) ) {
                String jobName = parentJob.getJobMeta().getName();
                String name = transMeta.getName();
                logBasic( BaseMessages.getString( PKG, "JobTrans.Log.InvalidRunConfigurationCombination", jobName,
                        name, jobName ) );
              }
            } catch ( Exception ignored ) {
              // Ignored
            }
            if ( !executionConfiguration.isExecutingLocally() && !executionConfiguration.isExecutingRemotely() && !executionConfiguration.isExecutingClustered() ) {
              result.setResult( true );
              return result;
            }
            clustering = executionConfiguration.isExecutingClustered();
            remoteSlaveServer = executionConfiguration.getRemoteServer();
            doFallback = false;
          } catch ( KettleException e ) {
            log.logError( e.getMessage(), getName() );
            result.setNrErrors( 1 );
            result.setResult( false );
            return result;
          }
        }

        if ( doFallback ) {
          // Figure out the remote slave server...
          //
          if ( !Utils.isEmpty( remoteSlaveServerName ) ) {
            String realRemoteSlaveServerName = environmentSubstitute( remoteSlaveServerName );
            remoteSlaveServer = parentJob.getJobMeta().findSlaveServer( realRemoteSlaveServerName );
            if ( remoteSlaveServer == null ) {
              throw new KettleException( BaseMessages.getString(
                PKG, "JobTrans.Exception.UnableToFindRemoteSlaveServer", realRemoteSlaveServerName ) );
            }
          }
        }


        // Execute this transformation across a cluster of servers
        //
        if ( clustering ) {
          executionConfiguration.setClusterPosting( true );
          executionConfiguration.setClusterPreparing( true );
          executionConfiguration.setClusterStarting( true );
          executionConfiguration.setClusterShowingTransformation( false );
          executionConfiguration.setSafeModeEnabled( false );
          executionConfiguration.setRepository( rep );
          executionConfiguration.setLogLevel( transLogLevel );
          executionConfiguration.setPreviousResult( previousResult );

          // Also pass the variables from the transformation into the execution configuration
          // That way it can go over the HTTP connection to the slave server.
          //
          executionConfiguration.setVariables( transMeta );

          // Also set the arguments...
          //
          executionConfiguration.setArgumentStrings( args );

          if ( parentJob.getJobMeta().isBatchIdPassed() ) {
            executionConfiguration.setPassedBatchId( parentJob.getPassedBatchId() );
          }

          TransSplitter transSplitter = null;
          long errors = 0;
          try {
            transSplitter = Trans.executeClustered( transMeta, executionConfiguration );

            // Monitor the running transformations, wait until they are done.
            // Also kill them all if anything goes bad
            // Also clean up afterwards...
            //
            errors += Trans.monitorClusteredTransformation( log, transSplitter, parentJob );

          } catch ( Exception e ) {
            logError( "Error during clustered execution. Cleaning up clustered execution.", e );
            // In case something goes wrong, make sure to clean up afterwards!
            //
            errors++;
            if ( transSplitter != null ) {
              Trans.cleanupCluster( log, transSplitter );
            } else {
              // Try to clean anyway...
              //
              SlaveServer master = null;
              for ( StepMeta stepMeta : transMeta.getSteps() ) {
                if ( stepMeta.isClustered() ) {
                  for ( SlaveServer slaveServer : stepMeta.getClusterSchema().getSlaveServers() ) {
                    if ( slaveServer.isMaster() ) {
                      master = slaveServer;
                      break;
                    }
                  }
                }
              }
              if ( master != null ) {
                master.deAllocateServerSockets( transMeta.getName(), null );
              }
            }
          }

          result.clear();

          if ( transSplitter != null ) {
            Result clusterResult = Trans.getClusteredTransformationResult( log, transSplitter, parentJob,
              executionConfiguration.isLogRemoteExecutionLocally() );
            result.add( clusterResult );
          }

          result.setNrErrors( result.getNrErrors() + errors );

        } else if ( remoteSlaveServer != null ) {
          // Execute this transformation remotely
          //

          // Make sure we can parameterize the slave server connection
          //
          remoteSlaveServer.shareVariablesWith( this );

          // Remote execution...
          //
          executionConfiguration.setPreviousResult( previousResult.clone() );
          executionConfiguration.setArgumentStrings( args );
          executionConfiguration.setVariables( this );
          executionConfiguration.setRemoteServer( remoteSlaveServer );
          executionConfiguration.setLogLevel( transLogLevel );
          executionConfiguration.setRepository( rep );
          executionConfiguration.setLogFileName( realLogFilename );
          executionConfiguration.setSetAppendLogfile( setAppendLogfile );
          executionConfiguration.setSetLogfile( setLogfile );

          Map<String, String> params = executionConfiguration.getParams();
          for ( String param : transMeta.listParameters() ) {
            String value =
              Const.NVL( transMeta.getParameterValue( param ), Const.NVL(
                transMeta.getParameterDefault( param ), transMeta.getVariable( param ) ) );
            params.put( param, value );
          }

          if ( parentJob.getJobMeta().isBatchIdPassed() ) {
            executionConfiguration.setPassedBatchId( parentJob.getPassedBatchId() );
          }

          // Send the XML over to the slave server
          // Also start the transformation over there...
          //
          String carteObjectId = Trans.sendToSlaveServer( transMeta, executionConfiguration, rep, metaStore );

          // Now start the monitoring...
          //
          SlaveServerTransStatus transStatus = null;
          while ( !parentJob.isStopped() && waitingToFinish ) {
            try {
              transStatus = remoteSlaveServer.getTransStatus( transMeta.getName(), carteObjectId, 0 );
              if ( !transStatus.isRunning() ) {
                // The transformation is finished, get the result...
                //
                //get the status with the result ( we don't do it above because of changing PDI-15781)
                transStatus = remoteSlaveServer.getTransStatus( transMeta.getName(), carteObjectId, 0,
                  !isSuppressResultData() );
                Result remoteResult = transStatus.getResult();
                result.clear();
                result.add( remoteResult );

                // In case you manually stop the remote trans (browser etc), make sure it's marked as an error
                //
                if ( remoteResult.isStopped() ) {
                  result.setNrErrors( result.getNrErrors() + 1 ); //
                }

                // Make sure to clean up : write a log record etc, close any left-over sockets etc.
                //
                remoteSlaveServer.cleanupTransformation( transMeta.getName(), carteObjectId );

                break;
              }
            } catch ( Exception e1 ) {

              logError( BaseMessages.getString( PKG, "JobTrans.Error.UnableContactSlaveServer", ""
                + remoteSlaveServer, transMeta.getName() ), e1 );
              result.setNrErrors( result.getNrErrors() + 1L );
              break; // Stop looking too, chances are too low the server will come back on-line
            }

            // sleep for 2 seconds
            try {
              Thread.sleep( 2000 );
            } catch ( InterruptedException e ) {
              // Ignore
            }
          }

          if ( parentJob.isStopped() ) {
            // See if we have a status and if we need to stop the remote execution here...
            //
            if ( transStatus == null || transStatus.isRunning() ) {
              // Try a remote abort ...
              //
              remoteSlaveServer.stopTransformation( transMeta.getName(), transStatus.getId() );

              // And a cleanup...
              //
              remoteSlaveServer.cleanupTransformation( transMeta.getName(), transStatus.getId() );

              // Set an error state!
              //
              result.setNrErrors( result.getNrErrors() + 1L );
            }
          }

        } else {

          // Execute this transformation on the local machine
          //

          // Create the transformation from meta-data
          //
          //trans = new Trans( transMeta, this );
          final TransMeta meta = transMeta;
          trans = new Trans( meta, this );

          // Pass the socket repository as early as possible...
          //
          trans.setSocketRepository( parentJob.getSocketRepository() );

          if ( parentJob.getJobMeta().isBatchIdPassed() ) {
            trans.setPassedBatchId( parentJob.getPassedBatchId() );
          }

          // set the parent job on the transformation, variables are taken from here...
          //
          trans.setParentJob( parentJob );
          trans.setParentVariableSpace( parentJob );
          trans.setLogLevel( transLogLevel );
          trans.setPreviousResult( previousResult );
          trans.setArguments( arguments );

          // Mappings need the repository to load from
          //
          trans.setRepository( rep );

          // inject the metaStore
          trans.setMetaStore( metaStore );

          // set gathering metrics state
          trans.setGatheringMetrics( parentJob.isGatheringMetrics() );

          // First get the root job
          //
          Job rootJob = parentJob;
          while ( rootJob.getParentJob() != null ) {
            rootJob = rootJob.getParentJob();
          }

          // Get the start and end-date from the root job...
          //
          trans.setJobStartDate( rootJob.getStartDate() );
          trans.setJobEndDate( rootJob.getEndDate() );

          // Inform the parent job we started something here...
          //
          for ( DelegationListener delegationListener : parentJob.getDelegationListeners() ) {
            // TODO: copy some settings in the job execution configuration, not strictly needed
            // but the execution configuration information is useful in case of a job re-start
            //
            delegationListener.transformationDelegationStarted( trans, new TransExecutionConfiguration() );
          }

          try {
            // Start execution...
            //
            trans.execute( args );

            // Wait until we're done with it...
            //TODO is it possible to implement Observer pattern to avoid Thread.sleep here?
            while ( !trans.isFinished() && trans.getErrors() == 0 ) {
              if ( parentJob.isStopped() ) {
                trans.stopAll();
                break;
              } else {
                try {
                  Thread.sleep( 0, 500 );
                } catch ( InterruptedException e ) {
                  // Ignore errors
                }
              }
            }
            trans.waitUntilFinished();

            if ( parentJob.isStopped() || trans.getErrors() != 0 ) {
              trans.stopAll();
              result.setNrErrors( 1 );
            }
            updateResult( result );
            if ( setLogfile ) {
              ResultFile resultFile =
                new ResultFile(
                  ResultFile.FILE_TYPE_LOG, KettleVFS.getFileObject( realLogFilename, this ), parentJob
                  .getJobname(), toString()
                );
              result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
            }
          } catch ( KettleException e ) {

            logError( BaseMessages.getString( PKG, "JobTrans.Error.UnablePrepareExec" ), e );
            result.setNrErrors( 1 );
          }
        }
      } catch ( Exception e ) {

        logError( BaseMessages.getString( PKG, "JobTrans.ErrorUnableOpenTrans", e.getMessage() ) );
        logError( Const.getStackTracker( e ) );
        result.setNrErrors( 1 );
      }
      iteration++;
    }

    if ( setLogfile ) {
      if ( logChannelFileWriter != null ) {
        logChannelFileWriter.stopLogging();

        ResultFile resultFile =
          new ResultFile(
            ResultFile.FILE_TYPE_LOG, logChannelFileWriter.getLogFile(), parentJob.getJobname(), getName() );
        result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

        // See if anything went wrong during file writing...
        //
        if ( logChannelFileWriter.getException() != null ) {
          logError( "Unable to open log file [" + getLogFilename() + "] : " );
          logError( Const.getStackTracker( logChannelFileWriter.getException() ) );
          result.setNrErrors( 1 );
          result.setResult( false );
          return result;
        }
      }
    }

    if ( result.getNrErrors() == 0 ) {
      result.setResult( true );
    } else {
      result.setResult( false );
    }

    return result;
  }

  private boolean shouldConsiderOldBehaviourForEveryInputRow() {
    boolean shouldExecuteWithZeroRows = "Y".equalsIgnoreCase( System.getProperty( Const.COMPATIBILITY_TRANS_EXECUTE_FOR_EVERY_ROW_ON_NO_INPUT, "N" ) );
    if ( "Y".equalsIgnoreCase( System.getProperty( Const.COMPATIBILITY_SHOW_WARNINGS_EXECUTE_EVERY_INPUT_ROW, "N" ) ) ) {
      if ( shouldExecuteWithZeroRows ) {
        log.logBasic(
          "WARN Detected \"Execute for every row\" but no rows were detected, applying desired behavior, to execute. In case this is not desired behavior, please read property COMPATIBILITY_TRANS_EXECUTE_FOR_EVERY_ROW_ON_NO_INPUT" );
      } else {
        log.logBasic(
          "WARN Detected \"Execute for every row\" but no rows were detected, applying default behavior, not to execute. In case this is not desired behavior, please read property COMPATIBILITY_TRANS_EXECUTE_FOR_EVERY_ROW_ON_NO_INPUT" );
      }
    }
    return shouldExecuteWithZeroRows;
  }

  protected void updateResult( Result result ) {
    Result newResult = trans.getResult();
    result.clear(); // clear only the numbers, NOT the files or rows.
    result.add( newResult );
    if ( !Utils.isEmpty( newResult.getRows() ) || trans.isResultRowsSet() ) {
      result.setRows( newResult.getRows() );
    }
  }

  /**
   * @deprecated use {@link #getTransMeta(Repository, IMetaStore, VariableSpace)}
   * @param rep
   * @param space
   * @return
   * @throws KettleException
   */
  @Deprecated
  public TransMeta getTransMeta( Repository rep, VariableSpace space ) throws KettleException {
    return getTransMeta( rep, null, space );
  }

  public TransMeta getTransMeta( Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    try {
      IMetaFileLoader<TransMeta> metaFileLoader = new MetaFileLoaderImpl<>( this, specificationMethod );
      TransMeta transMeta = metaFileLoader.getMetaForEntry( rep, metaStore, space );

      if ( transMeta != null ) {
        // set Internal.Entry.Current.Directory again because it was changed
        transMeta.setInternalKettleVariables();
        //  When the child parameter does exist in the parent parameters, overwrite the child parameter by the
        // parent parameter.

        StepWithMappingMeta.replaceVariableValues( transMeta, space, "Trans" );
        if ( isPassingAllParameters() ) {
          // All other parent parameters need to get copied into the child parameters  (when the 'Inherit all
          // variables from the transformation?' option is checked)
          StepWithMappingMeta.addMissingVariables( transMeta, space );
        }
        // Pass repository and metastore references
        //
        transMeta.setRepository( rep );
        transMeta.setMetaStore( metaStore );
      }

      return transMeta;
    } catch ( final KettleException ke ) {
      // if we get a KettleException, simply re-throw it
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobTrans.Exception.MetaDataLoad" ), e );
    }
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return true;
  }

  @Override
  public List<SQLStatement> getSQLStatements( Repository repository, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    this.copyVariablesFrom( space );
    TransMeta transMeta = getTransMeta( repository, metaStore, this );

    return transMeta.getSQLStatements();
  }

  /**
   * @return Returns the directoryPath.
   */
  public String getDirectoryPath() {
    return directoryPath;
  }

  /**
   * @param directoryPath The directoryPath to set.
   */
  public void setDirectoryPath( String directoryPath ) {
    this.directoryPath = directoryPath;
  }

  /**
   * @return the clustering
   */
  public boolean isClustering() {
    return clustering;
  }

  /**
   * @param clustering the clustering to set
   */
  public void setClustering( boolean clustering ) {
    this.clustering = clustering;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
                     Repository repository, IMetaStore metaStore ) {
    if ( setLogfile ) {
      JobEntryValidatorUtils.andValidator().validate( this, "logfile", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    }
    if ( !Utils.isEmpty( filename ) ) {
      JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    } else {
      JobEntryValidatorUtils.andValidator().validate( this, "transname", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
      JobEntryValidatorUtils.andValidator().validate( this, "directory", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    }
  }

  @Override
  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( filename ) ) {
      // During this phase, the variable space hasn't been initialized yet - it seems
      // to happen during the execute. As such, we need to use the job meta's resolution
      // of the variables.
      String realFileName = jobMeta.environmentSubstitute( filename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realFileName, ResourceType.ACTIONFILE ) );
      references.add( reference );
    }
    return references;
  }

  /**
   * We're going to load the transformation meta data referenced here. Then we're going to give it a new filename,
   * modify that filename in this entries. The parent caller will have made a copy of it, so it should be OK to do so.
   * <p/>
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. The supplied
   * resource naming interface allows the object to name appropriately without worrying about those parts of the
   * implementation specific details.
   *
   * @param space           The variable space to resolve (environment) variables with.
   * @param definitions     The map containing the filenames and content
   * @param namingInterface The resource naming interface allows the object to be named appropriately
   * @param repository      The repository to load resources from
   * @param metaStore       the metaStore to load external metadata from
   * @return The filename for this object. (also contained in the definitions map)
   * @throws KettleException in case something goes wrong during the export
   */
  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
                                 ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    // Try to load the transformation from repository or file.
    // Modify this recursively too...
    //
    // AGAIN: there is no need to clone this job entry because the caller is responsible for this.
    //
    // First load the transformation metadata...
    //
    copyVariablesFrom( space );
    TransMeta transMeta = getTransMeta( repository, space );

    // Also go down into the transformation and export the files there. (mapping recursively down)
    //
    String proposedNewFilename =
      transMeta.exportResources( transMeta, definitions, namingInterface, repository, metaStore );

    // To get a relative path to it, we inject ${Internal.Entry.Current.Directory}
    //
    String newFilename = "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + proposedNewFilename;

    // Set the correct filename inside the XML.
    //
    transMeta.setFilename( newFilename );

    // exports always reside in the root directory, in case we want to turn this into a file repository...
    //
    transMeta.setRepositoryDirectory( new RepositoryDirectory() );

    // export to filename ALWAYS (this allows the exported XML to be executed remotely)
    //
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

    // change it in the job entry
    //
    filename = newFilename;

    return proposedNewFilename;
  }

  protected String getLogfile() {
    return logfile;
  }

  /**
   * @return the remote slave server name
   */
  public String getRemoteSlaveServerName() {
    return remoteSlaveServerName;
  }

  /**
   * @param remoteSlaveServerName the remote slave server name to set
   */
  public void setRemoteSlaveServerName( String remoteSlaveServerName ) {
    this.remoteSlaveServerName = remoteSlaveServerName;
  }

  /**
   * @return the waitingToFinish
   */
  public boolean isWaitingToFinish() {
    return waitingToFinish;
  }

  /**
   * @param waitingToFinish the waitingToFinish to set
   */
  public void setWaitingToFinish( boolean waitingToFinish ) {
    this.waitingToFinish = waitingToFinish;
  }

  /**
   * @return the followingAbortRemotely
   */
  public boolean isFollowingAbortRemotely() {
    return followingAbortRemotely;
  }

  /**
   * @param followingAbortRemotely the followingAbortRemotely to set
   */
  public void setFollowingAbortRemotely( boolean followingAbortRemotely ) {
    this.followingAbortRemotely = followingAbortRemotely;
  }

  public boolean isLoggingRemoteWork() {
    return loggingRemoteWork;
  }

  public void setLoggingRemoteWork( boolean loggingRemoteWork ) {
    this.loggingRemoteWork = loggingRemoteWork;
  }

  /**
   * @return the passingAllParameters
   */
  public boolean isPassingAllParameters() {
    return passingAllParameters;
  }

  /**
   * @param passingAllParameters the passingAllParameters to set
   */
  public void setPassingAllParameters( boolean passingAllParameters ) {
    this.passingAllParameters = passingAllParameters;
  }

  public String getRunConfiguration() {
    return runConfiguration;
  }

  public void setRunConfiguration( String runConfiguration ) {
    this.runConfiguration = runConfiguration;
  }

  public Trans getTrans() {
    return trans;
  }

  /**
   * @return the transObjectId
   */
  public ObjectId getTransObjectId() {
    return transObjectId;
  }

  /**
   * @param transObjectId the transObjectId to set
   */
  public void setTransObjectId( ObjectId transObjectId ) {
    this.transObjectId = transObjectId;
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  @Override
  public ObjectLocationSpecificationMethod[] getSpecificationMethods() {
    return new ObjectLocationSpecificationMethod[] { specificationMethod };
  }

  /**
   * @param specificationMethod the specificationMethod to set
   */
  public void setSpecificationMethod( ObjectLocationSpecificationMethod specificationMethod ) {
    this.specificationMethod = specificationMethod;
  }

  @Override
  public boolean hasRepositoryReferences() {
    return specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
  }

  /**
   * Look up the references after import
   *
   * @param repository the repository to reference.
   */
  @Override
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    // The correct reference is stored in the trans name and directory attributes...
    //
    RepositoryDirectoryInterface repositoryDirectoryInterface =
      RepositoryImportLocation.getRepositoryImportLocation().findDirectory( directory );
    transObjectId = repository.getTransformationID( transname, repositoryDirectoryInterface );
  }

  /**
   * @return The objects referenced in the step, like a a transformation, a job, a mapper, a reducer, a combiner, ...
   */
  @Override
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString( PKG, "JobEntryTrans.ReferencedObject.Description" ), };
  }

  private boolean isTransformationDefined() {
    return !Utils.isEmpty( filename )
      || transObjectId != null || ( !Utils.isEmpty( this.directory ) && !Utils.isEmpty( transname ) );
  }

  @Override
  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isTransformationDefined(), };
  }

  /**
   * Load the referenced object
   *
   * @param index     the referenced object index to load (in case there are multiple references)
   * @param rep       the repository
   * @param metaStore metaStore
   * @param space     the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  @Override
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return getTransMeta( rep, metaStore, space );
  }

  @Override
  public void setParentJobMeta( JobMeta parentJobMeta ) {
    JobMeta previous = getParentJobMeta();
    super.setParentJobMeta( parentJobMeta );
    if ( parentJobMeta !=  null ) {
      parentJobMeta.addCurrentDirectoryChangedListener( currentDirListener );
      variables.setParentVariableSpace( parentJobMeta );
    } else if ( previous != null ) {
      previous.removeCurrentDirectoryChangedListener( currentDirListener );
    }
  }

  public void prepareFieldNamesParameters( String[] parameters, String[] parameterFieldNames, String[] parameterValues,
                                                    NamedParams namedParam, JobEntryTrans jobEntryTrans )
    throws UnknownParamException {
    for ( int idx = 0; idx < parameters.length; idx++ ) {
      // Grab the parameter value set in the Trans job entry
      // Set fieldNameParameter only if exists and if it is not declared any staticValue( parameterValues array )
      //
      String thisValue = namedParam.getParameterValue( parameters[ idx ] );
      // multiple executions on the same jobEntryTrans variableSpace need to be updated even for nulls or blank values.
      // so we have to ask if that same variable had a value before and if it had - and the new value is empty -
      // we should set it as a blank value instead of ignoring it.
      // NOTE: we should only replace it if we have a parameterFieldNames defined -> parameterFieldNames[ idx ] ) != null
      if ( !Utils.isEmpty( jobEntryTrans.getVariable( parameters[ idx ] ) ) && Utils.isEmpty( thisValue )
        && idx < parameterFieldNames.length && !Utils.isEmpty( Const.trim( parameterFieldNames[ idx ] ) ) ) {
        jobEntryTrans.setVariable( parameters[ idx ], "" );
      }
      // Set value only if is not empty at namedParam and exists in parameterFieldNames
      if ( !Utils.isEmpty( thisValue ) && idx < parameterFieldNames.length ) {
        // If exists then ask if is not empty
        if ( !Utils.isEmpty( Const.trim( parameterFieldNames[ idx ] ) ) ) {
          // If is not empty then we have to ask if it exists too in parameterValues array, since the values in
          // parameterValues prevail over parameterFieldNames
          if ( idx < parameterValues.length ) {
            // If is empty at parameterValues array, then we can finally add that variable with that value
            if ( Utils.isEmpty( Const.trim( parameterValues[ idx ] ) ) ) {
              jobEntryTrans.setVariable( parameters[ idx ], thisValue );
            }
          } else {
            // Or if not in parameterValues then we can add that variable with that value too
            jobEntryTrans.setVariable( parameters[ idx ], thisValue );
          }
        }
      }
    }
  }

  /*
   * Users may define a named parameter with a name matching SUPPRESS_TRANS_RESULT_DATA_* with a value matching the name
   * of a JobEntryTrans step.  If such a parameter is found and its value matches this step's name, then do not request
   * the result row data of the transformation when it finishes running.  Only applies to remote slave servers.
   */
  public boolean isSuppressResultData() {
    boolean returnVal = suppressResultData;
    if ( !suppressResultData && parentJobMeta != null ) {
      String[] params = parentJobMeta.listParameters();
      for ( String param : params ) {
        if ( param.startsWith( "SUPPRESS_TRANS_RESULT_DATA_" ) ) {
          try {
            String paramVal = parentJobMeta.getParameterValue( param );
            if ( paramVal != null ) {
              returnVal |= paramVal.equals( this.getName() );
            }
          } catch ( UnknownParamException e ) {
            logError( BaseMessages.getString( PKG, "JobTrans.Exception.SuppressResultParam" ), e );
          }
        }
      }
    }
    return returnVal;
  }

  public void setSuppressResultData( boolean suppressResultData ) {
    this.suppressResultData = suppressResultData;
  }

  @Override public void callBeforeLog() {
    if ( parentJob != null ) {
      parentJob.callBeforeLog();
    }
  }

  @Override
  public void callAfterLog() {
    if ( parentJob != null ) {
      parentJob.callAfterLog();
    }
  }
}
