/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.job;

import org.apache.commons.vfs2.FileObject;
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
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
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
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.w3c.dom.Node;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Recursive definition of a Job. This step means that an entire Job has to be executed. It can be the same Job, but
 * just make sure that you don't get an endless loop. Provide an escape routine using JobEval.
 *
 * @author Matt
 * @since 01-10-2003, Rewritten on 18-06-2004
 *
 */
public class JobEntryJob extends JobEntryBase implements Cloneable, JobEntryInterface, HasRepositoryDirectories, JobEntryRunConfigurableInterface {
  private static Class<?> PKG = JobEntryJob.class; // for i18n purposes, needed by Translator2!!
  public static final int IS_PENTAHO = 1;

  private String filename;
  private String jobname;
  private String directory;
  private ObjectId jobObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  public String[] arguments;
  public boolean argFromPrevious;
  public boolean paramsFromPrevious;
  public boolean execPerRow;

  public String[] parameters;
  public String[] parameterFieldNames;
  public String[] parameterValues;

  public boolean setLogfile;
  public String logfile, logext;
  public boolean addDate, addTime;
  public LogLevel logFileLevel;

  public boolean parallel;
  public boolean setAppendLogfile;
  public boolean createParentFolder;

  public boolean waitingToFinish = true;
  public boolean followingAbortRemotely;

  public boolean expandingRemoteJob;

  private String remoteSlaveServerName;
  public boolean passingAllParameters = true;

  private boolean passingExport;

  private String runConfiguration;

  public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.NOTHING;

  private Job job;

  private CurrentDirectoryChangedListener dirListener = new EntryCurrentDirectoryChangedListener(
      this::getSpecificationMethod,
      this::getDirectory,
      this::setDirectory );

  public JobEntryJob( String name ) {
    super( name, "" );
  }

  public JobEntryJob() {
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
    JobEntryJob je = (JobEntryJob) super.clone();
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
   * @deprecated use getFilename() instead.
   * @return the filename
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

  public void setJobName( String jobname ) {
    this.jobname = jobname;
  }

  public String getJobName() {
    return jobname;
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

  public boolean isPassingExport() {
    return passingExport;
  }

  public void setPassingExport( boolean passingExport ) {
    this.passingExport = passingExport;
  }

  public String getRunConfiguration() {
    return runConfiguration;
  }

  public void setRunConfiguration( String runConfiguration ) {
    this.runConfiguration = runConfiguration;
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
    StringBuilder retval = new StringBuilder( 400 );

    retval.append( super.getXML() );

    // specificationMethod - export as is, we may be importing it later. in runtime we'll find the actual spec method
    //
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "specification_method", specificationMethod == null ? null : specificationMethod
        .getCode() ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "job_object_id", jobObjectId == null ? null : jobObjectId.toString() ) );
    // Export a little bit of extra information regarding the reference since it doesn't really matter outside the same
    // repository.
    //
    if ( rep != null && jobObjectId != null ) {
      try {
        RepositoryObject objectInformation = rep.getObjectInformation( jobObjectId, RepositoryObjectType.JOB );
        if ( objectInformation != null ) {
          jobname = objectInformation.getName();
          directory = objectInformation.getRepositoryDirectory().getPath();
        }
      } catch ( KettleException e ) {
        // Ignore object reference problems. It simply means that the reference is no longer valid.
      }
    }
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( filename );
    }

    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "jobname", jobname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "directory", directory ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "params_from_previous", paramsFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "exec_per_row", execPerRow ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_logfile", setLogfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logfile", logfile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "logext", logext ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", addDate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", addTime ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "loglevel", logFileLevel != null ? logFileLevel.getCode() : DEFAULT_LOG_LEVEL
        .getCode() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "slave_server_name", remoteSlaveServerName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wait_until_finished", waitingToFinish ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "follow_abort_remote", followingAbortRemotely ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "expand_remote_job", expandingRemoteJob ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "create_parent_folder", createParentFolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "pass_export", passingExport ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "run_configuration", runConfiguration ) );

    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        // This is a very very bad way of making an XML file, don't use it (or
        // copy it). Sven Boden
        retval.append( "      " ).append( XMLHandler.addTagValue( "argument" + i, arguments[i] ) );
      }
    }

    if ( parameters != null ) {
      retval.append( "      " ).append( XMLHandler.openTag( "parameters" ) );

      retval.append( "        " ).append( XMLHandler.addTagValue( "pass_all_parameters", passingAllParameters ) );

      for ( int i = 0; i < parameters.length; i++ ) {
        // This is a better way of making the XML file than the arguments.
        retval.append( "            " ).append( XMLHandler.openTag( "parameter" ) );

        retval.append( "            " ).append( XMLHandler.addTagValue( "name", parameters[i] ) );
        retval.append( "            " ).append( XMLHandler.addTagValue( "stream_name", parameterFieldNames[i] ) );
        retval.append( "            " ).append( XMLHandler.addTagValue( "value", parameterValues[i] ) );

        retval.append( "            " ).append( XMLHandler.closeTag( "parameter" ) );
      }
      retval.append( "      " ).append( XMLHandler.closeTag( "parameters" ) );
    }
    retval.append( "      " ).append( XMLHandler.addTagValue( "set_append_logfile", setAppendLogfile ) );

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
      } else if ( jobObjectId != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
      } else if ( !Utils.isEmpty( jobname ) ) {
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

      String jobId = XMLHandler.getTagValue( entrynode, "job_object_id" );
      jobObjectId = Utils.isEmpty( jobId ) ? null : new StringObjectId( jobId );
      filename = XMLHandler.getTagValue( entrynode, "filename" );
      jobname = XMLHandler.getTagValue( entrynode, "jobname" );
      directory = XMLHandler.getTagValue( entrynode, "directory" );

      /*
       * if we're connected to a repository and jobname is present use REPOSITORY_BY_NAME
       * if we're connected to a repository and jobobjectid is present use REPOSITORY_BY_REFERENCE
       * if we're connected to a repository and filename is present use FILENAME
       * if we're connected and no other data is present, default to REPOSITORY_BY_NAME
       * if we're not connected to a repository, always use FILENAME
       *
       * no other options are supported
       */

      if ( rep != null && rep.isConnected() ) {
        if ( !Utils.isEmpty( jobname ) ) {
          specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        } else if ( jobObjectId != null ) {
          specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        } else if ( !Utils.isEmpty( filename ) ) {
          specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        } else {
          specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        }
      } else {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        if ( Utils.isEmpty( filename ) && !Utils.isEmpty( directory ) && !Utils.isEmpty( jobname ) ) {
          // this job was exported from a repository and is being loaded locally
          // need to create a well formatted filename
          filename = directory + RepositoryFile.SEPARATOR + jobname;
          if ( !filename.toLowerCase().endsWith( RepositoryObjectType.JOB.getExtension() ) ) {
            filename = filename + RepositoryObjectType.JOB.getExtension();
          }
        }
      }

      checkObjectLocationSpecificationMethod( );

      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      paramsFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "params_from_previous" ) );
      execPerRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "exec_per_row" ) );
      setLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_logfile" ) );
      addDate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_date" ) );
      addTime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_time" ) );
      logfile = XMLHandler.getTagValue( entrynode, "logfile" );
      logext = XMLHandler.getTagValue( entrynode, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( XMLHandler.getTagValue( entrynode, "loglevel" ) );
      setAppendLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "set_append_logfile" ) );
      remoteSlaveServerName = XMLHandler.getTagValue( entrynode, "slave_server_name" );
      passingExport = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "pass_export" ) );
      createParentFolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "create_parent_folder" ) );
      runConfiguration = XMLHandler.getTagValue( entrynode, "run_configuration" );

      String wait = XMLHandler.getTagValue( entrynode, "wait_until_finished" );
      if ( Utils.isEmpty( wait ) ) {
        waitingToFinish = true;
      } else {
        waitingToFinish = "Y".equalsIgnoreCase( wait );
      }

      followingAbortRemotely = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "follow_abort_remote" ) );
      expandingRemoteJob = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "expand_remote_job" ) );

      // How many arguments?
      int argnr = 0;
      while ( XMLHandler.getTagValue( entrynode, "argument" + argnr ) != null ) {
        argnr++;
      }
      allocateArgs( argnr );

      // Read them all... This is a very BAD way to do it by the way. Sven
      // Boden.
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = XMLHandler.getTagValue( entrynode, "argument" + a );
      }

      Node parametersNode = XMLHandler.getSubNode( entrynode, "parameters" );

      String passAll = XMLHandler.getTagValue( parametersNode, "pass_all_parameters" );
      passingAllParameters = Utils.isEmpty( passAll ) || "Y".equalsIgnoreCase( passAll );

      int nrParameters = XMLHandler.countNodes( parametersNode, "parameter" );
      allocateParams( nrParameters );

      for ( int i = 0; i < nrParameters; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( parametersNode, "parameter", i );

        parameters[i] = XMLHandler.getTagValue( knode, "name" );
        parameterFieldNames[i] = XMLHandler.getTagValue( knode, "stream_name" );
        parameterValues[i] = XMLHandler.getTagValue( knode, "value" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load 'job' job entry from XML node", xe );
    }
  }

  /**
   * Load the jobentry from repository
   */
  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      String method = rep.getJobEntryAttributeString( id_jobentry, "specification_method" );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
      String jobId = rep.getJobEntryAttributeString( id_jobentry, "job_object_id" );
      jobObjectId = Utils.isEmpty( jobId ) ? null : new StringObjectId( jobId );
      jobname = rep.getJobEntryAttributeString( id_jobentry, "name" );
      directory = rep.getJobEntryAttributeString( id_jobentry, "dir_path" );
      filename = rep.getJobEntryAttributeString( id_jobentry, "file_name" );

      /*
       * when loaded from a repository and jobname is present use REPOSITORY_BY_NAME
       * if filename is not present and jobObjectId use REPOSITORY_BY_REFERENCE for backwards compatibility
       * if filename is present use FILENAME
       * if nothing else, default to REPOSITORY_BY_NAME
       *
       * no other options are supported
       */

      if ( !Utils.isEmpty( jobname ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      } else if ( jobObjectId != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
      } else if ( !Utils.isEmpty( filename ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
      } else {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }

      argFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      paramsFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "params_from_previous" );
      execPerRow = rep.getJobEntryAttributeBoolean( id_jobentry, "exec_per_row" );
      setLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_logfile" );
      addDate = rep.getJobEntryAttributeBoolean( id_jobentry, "add_date" );
      addTime = rep.getJobEntryAttributeBoolean( id_jobentry, "add_time" );
      logfile = rep.getJobEntryAttributeString( id_jobentry, "logfile" );
      logext = rep.getJobEntryAttributeString( id_jobentry, "logext" );
      logFileLevel = LogLevel.getLogLevelForCode( rep.getJobEntryAttributeString( id_jobentry, "loglevel" ) );
      setAppendLogfile = rep.getJobEntryAttributeBoolean( id_jobentry, "set_append_logfile" );
      remoteSlaveServerName = rep.getJobEntryAttributeString( id_jobentry, "slave_server_name" );
      passingExport = rep.getJobEntryAttributeBoolean( id_jobentry, "pass_export" );
      waitingToFinish = rep.getJobEntryAttributeBoolean( id_jobentry, "wait_until_finished", true );
      followingAbortRemotely = rep.getJobEntryAttributeBoolean( id_jobentry, "follow_abort_remote" );
      expandingRemoteJob = rep.getJobEntryAttributeBoolean( id_jobentry, "expand_remote_job" );
      createParentFolder = rep.getJobEntryAttributeBoolean( id_jobentry, "create_parent_folder" );
      runConfiguration = rep.getJobEntryAttributeString( id_jobentry, "run_configuration" );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "argument" );
      allocateArgs( argnr );

      // Read all arguments ...
      for ( int a = 0; a < argnr; a++ ) {
        arguments[a] = rep.getJobEntryAttributeString( id_jobentry, a, "argument" );
      }

      // How many arguments?
      int parameternr = rep.countNrJobEntryAttributes( id_jobentry, "parameter_name" );
      allocateParams( parameternr );

      // Read all parameters ...
      for ( int a = 0; a < parameternr; a++ ) {
        parameters[a] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_name" );
        parameterFieldNames[a] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_stream_name" );
        parameterValues[a] = rep.getJobEntryAttributeString( id_jobentry, a, "parameter_value" );
      }

      passingAllParameters = rep.getJobEntryAttributeBoolean( id_jobentry, "pass_all_parameters", true );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'job' from the repository with id_jobentry="
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
      rep.saveJobEntryAttribute( id_job, getObjectId(), "job_object_id", jobObjectId == null ? null : jobObjectId
        .toString() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "name", getJobName() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "dir_path", getDirectory() != null ? getDirectory() : "" );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "file_name", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", argFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "params_from_previous", paramsFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "exec_per_row", execPerRow );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_logfile", setLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_date", addDate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_time", addTime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logfile", logfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "logext", logext );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "set_append_logfile", setAppendLogfile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "loglevel", logFileLevel != null
        ? logFileLevel.getCode() : JobEntryJob.DEFAULT_LOG_LEVEL.getCode() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "slave_server_name", remoteSlaveServerName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "pass_export", passingExport );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wait_until_finished", waitingToFinish );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "follow_abort_remote", followingAbortRemotely );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "expand_remote_job", expandingRemoteJob );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "create_parent_folder", createParentFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "run_configuration", runConfiguration );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "argument", arguments[i] );
        }
      }

      // save the parameters...
      if ( parameters != null ) {
        for ( int i = 0; i < parameters.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_name", parameters[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_stream_name", Const.NVL(
            parameterFieldNames[i], "" ) );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "parameter_value", Const.NVL(
            parameterValues[i], "" ) );
        }
      }

      rep.saveJobEntryAttribute( id_job, getObjectId(), "pass_all_parameters", passingAllParameters );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type job to the repository with id_job=" + id_job, dbe );
    }
  }

  @Override
  public Result execute( Result result, int nr ) throws KettleException {
    result.setEntryNr( nr );

    LogChannelFileWriter logChannelFileWriter = null;

    LogLevel jobLogLevel = parentJob.getLogLevel();
    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    if ( setLogfile ) {
      String realLogFilename = environmentSubstitute( getLogFilename() );
      // We need to check here the log filename
      // if we do not have one, we must fail
      if ( Utils.isEmpty( realLogFilename ) ) {
        logError( BaseMessages.getString( PKG, "JobJob.Exception.LogFilenameMissing" ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }

      // create parent folder?
      if ( !createParentFolder( realLogFilename ) ) {
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
      try {
        logChannelFileWriter =
          new LogChannelFileWriter(
            this.getLogChannelId(), KettleVFS.getFileObject( realLogFilename ), setAppendLogfile );
        logChannelFileWriter.startLogging();
      } catch ( KettleException e ) {
        logError( "Unable to open file appender for file [" + getLogFilename() + "] : " + e.toString() );
        logError( Const.getStackTracker( e ) );
        result.setNrErrors( 1 );
        result.setResult( false );
        return result;
      }
      jobLogLevel = logFileLevel;
    }

    try {
      // First load the job, outside of the loop...
      if ( parentJob.getJobMeta() != null ) {
        // reset the internal variables again.
        // Maybe we should split up the variables even more like in UNIX shells.
        // The internal variables need to be reset to be able use them properly
        // in 2 sequential sub jobs.
        parentJob.getJobMeta().setInternalKettleVariables();
      }

      // Explain what we are loading...
      //
      switch ( specificationMethod ) {
        case REPOSITORY_BY_NAME:
          if ( log.isDetailed() ) {
            logDetailed( "Loading job from repository : ["
              + directory + " : " + environmentSubstitute( jobname ) + "]" );
          }
          break;
        case FILENAME:
          if ( log.isDetailed() ) {
            logDetailed( "Loading job from XML file : [" + environmentSubstitute( filename ) + "]" );
          }
          break;
        case REPOSITORY_BY_REFERENCE:
          if ( log.isDetailed() ) {
            logDetailed( "Loading job from repository by reference : [" + jobObjectId + "]" );
          }
          break;
        default:
          break;
      }

      JobMeta jobMeta = getJobMeta( rep, this );

      // Verify that we loaded something, complain if we did not...
      //
      if ( jobMeta == null ) {
        throw new KettleException(
          "Unable to load the job: please specify the name and repository directory OR a filename" );
      }

      verifyRecursiveExecution( parentJob, jobMeta );

      int iteration = 0;
      String[] args1 = arguments;
      // no arguments? Check the parent jobs arguments
      if ( args1 == null || args1.length == 0 ) {
        args1 = parentJob.getArguments();
      }

      copyVariablesFrom( parentJob );
      setParentVariableSpace( parentJob );

      //
      // For the moment only do variable translation at the start of a job, not
      // for every input row (if that would be switched on)
      //
      String[] args = null;
      if ( args1 != null ) {
        args = new String[args1.length];
        for ( int idx = 0; idx < args1.length; idx++ ) {
          args[idx] = environmentSubstitute( args1[idx] );
        }
      }

      RowMetaAndData resultRow = null;
      boolean first = true;
      List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>( result.getRows() );

      while ( ( first && !execPerRow )
        || ( execPerRow && rows != null && iteration < rows.size() && result.getNrErrors() == 0 ) ) {

        first = false;
        // Clear the result rows of the result
        // Otherwise we double the amount of rows every iteration in the simple cases.
        //
        if ( execPerRow ) {
          result.getRows().clear();
        }

        // PDI-18776: Get the result row from rows based on iteration index if rows is not empty otherwise result row is null
        if ( rows != null && execPerRow && !rows.isEmpty() ) {
          // This check is for avoiding OutOfBoundException in next statement as iteration <= rows.size() condition is not leaving while loop
          // after processing the last row. Otherwise if iteration == rows.size() indicates that we processed already the last row and just need to break from the loop
          if ( iteration != rows.size() ) {
            resultRow = rows.get( iteration );
          } else {
            break;
          }
        } else {
          resultRow = null;
        }

        NamedParams namedParam = new NamedParamsDefault();

        // First (optionally) copy all the parameter values from the parent job
        //
        if ( paramsFromPrevious ) {
          String[] parentParameters = parentJob.listParameters();
          for ( int idx = 0; idx < parentParameters.length; idx++ ) {
            String par = parentParameters[idx];
            String def = parentJob.getParameterDefault( par );
            String val = parentJob.getParameterValue( par );
            String des = parentJob.getParameterDescription( par );

            namedParam.addParameterDefinition( par, def, des );
            namedParam.setParameterValue( par, val );
          }
        }

        // Now add those parameter values specified by the user in the job entry
        //
        if ( parameters != null ) {
          for ( int idx = 0; idx < parameters.length; idx++ ) {
            if ( !Utils.isEmpty( parameters[idx] ) ) {

              // If it's not yet present in the parent job, add it...
              //
              if ( Const.indexOfString( parameters[idx], namedParam.listParameters() ) < 0 ) {
                // We have a parameter
                try {
                  namedParam.addParameterDefinition( parameters[idx], "", "Job entry runtime" );
                } catch ( DuplicateParamException e ) {
                  // Should never happen
                  //
                  logError( "Duplicate parameter definition for " + parameters[idx] );
                }
              }

              if ( Utils.isEmpty( Const.trim( parameterFieldNames[idx] ) ) ) {
                namedParam.setParameterValue( parameters[idx], Const.NVL(
                  environmentSubstitute( parameterValues[idx] ), "" ) );
              } else {
                // something filled in, in the field column...
                //
                String value = "";
                if ( resultRow != null ) {
                  value = resultRow.getString( parameterFieldNames[idx], "" );
                }
                namedParam.setParameterValue( parameters[idx], value );
              }
            }
          }
        }

        Result oneResult = new Result();

        List<RowMetaAndData> sourceRows = null;

        if ( execPerRow ) {
          // Execute for each input row

          if ( argFromPrevious ) {
            // Copy the input row to the (command line) arguments

            args = null;
            if ( resultRow != null ) {
              args = new String[resultRow.size()];
              for ( int i = 0; i < resultRow.size(); i++ ) {
                args[i] = resultRow.getString( i, null );
              }
            }
          } else {
            // Just pass a single row
            List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
            if ( resultRow != null ) {
              newList.add( resultRow );
            }
            sourceRows = newList;
          }

          if ( paramsFromPrevious ) { // Copy the input the parameters

            if ( parameters != null ) {
              for ( int idx = 0; idx < parameters.length; idx++ ) {
                if ( !Utils.isEmpty( parameters[idx] ) ) {
                  // We have a parameter
                  if ( Utils.isEmpty( Const.trim( parameterFieldNames[idx] ) ) ) {
                    namedParam.setParameterValue( parameters[idx], Const.NVL(
                      environmentSubstitute( parameterValues[idx] ), "" ) );
                  } else {
                    String fieldValue = "";

                    if ( resultRow != null ) {
                      fieldValue = resultRow.getString( parameterFieldNames[idx], "" );
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue( parameters[idx], Const.NVL( fieldValue, "" ) );
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
              args = new String[resultRow.size()];
              for ( int i = 0; i < resultRow.size(); i++ ) {
                args[i] = resultRow.getString( i, null );
              }
            }
          } else {
            // Keep it as it was...
            sourceRows = result.getRows();
          }

          if ( paramsFromPrevious ) { // Copy the input the parameters

            if ( parameters != null ) {
              for ( int idx = 0; idx < parameters.length; idx++ ) {
                if ( !Utils.isEmpty( parameters[idx] ) ) {
                  // We have a parameter
                  if ( Utils.isEmpty( Const.trim( parameterFieldNames[idx] ) ) ) {
                    namedParam.setParameterValue( parameters[idx], Const.NVL(
                      environmentSubstitute( parameterValues[idx] ), "" ) );
                  } else {
                    String fieldValue = "";

                    if ( resultRow != null ) {
                      fieldValue = resultRow.getString( parameterFieldNames[idx], "" );
                    }
                    // Get the value from the input stream
                    namedParam.setParameterValue( parameters[idx], Const.NVL( fieldValue, "" ) );
                  }
                }
              }
            }
          }
        }

        boolean doFallback = true;
        SlaveServer remoteSlaveServer = null;
        JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
        if ( !Utils.isEmpty( runConfiguration ) ) {
          runConfiguration = environmentSubstitute( runConfiguration );
          log.logBasic( BaseMessages.getString( PKG, "JobJob.RunConfig.Message" ), runConfiguration );
          executionConfiguration.setRunConfiguration( runConfiguration );
          try {
            ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.SpoonTransBeforeStart.id, new Object[] {
              executionConfiguration, parentJob.getJobMeta(), jobMeta, rep
            } );
            List<Object> items = Arrays.asList( runConfiguration, false );
            try {
              ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint
                      .RunConfigurationSelection.id, items );
              if ( waitingToFinish && (Boolean) items.get( IS_PENTAHO ) ) {
                String jobName = parentJob.getJobMeta().getName();
                String name = jobMeta.getName();
                logBasic( BaseMessages.getString( PKG, "JobJob.Log.InvalidRunConfigurationCombination", jobName,
                        name, jobName ) );
              }
            } catch ( Exception ignored ) {
              // Ignored
            }
            if ( !executionConfiguration.isExecutingLocally() && !executionConfiguration.isExecutingRemotely() ) {
              result.setResult( true );
              return result;
            }
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

        if ( remoteSlaveServer == null ) {
          // Local execution...
          //

          // Create a new job
          //
          job = new Job( rep, jobMeta, this );
          job.setParentJob( parentJob );
          job.setLogLevel( jobLogLevel );
          job.shareVariablesWith( this );
          job.setInternalKettleVariables( this );
          job.copyParametersFrom( jobMeta );
          job.setInteractive( parentJob.isInteractive() );
          job.setGatheringMetrics( parentJob.isGatheringMetrics() );
          if ( job.isInteractive() ) {
            job.getJobEntryListeners().addAll( parentJob.getJobEntryListeners() );
          }

          // Pass the socket repository all around.
          //
          job.setSocketRepository( parentJob.getSocketRepository() );

          // Set the parameters calculated above on this instance.
          //
          job.clearParameters();
          String[] parameterNames = job.listParameters();
          for ( int idx = 0; idx < parameterNames.length; idx++ ) {
            // Grab the parameter value set in the job entry
            //
            String thisValue = namedParam.getParameterValue( parameterNames[idx] );
            if ( !Utils.isEmpty( thisValue ) ) {
              // Set the value as specified by the user in the job entry
              //
              job.setParameterValue( parameterNames[idx], thisValue );
            } else {
              // See if the parameter had a value set in the parent job...
              // This value should pass down to the sub-job if that's what we
              // opted to do.
              //
              if ( isPassingAllParameters() ) {
                String parentValue = parentJob.getParameterValue( parameterNames[idx] );
                if ( Utils.isEmpty( parentValue ) ) {
                  parentValue = parentJob.getParameterDefault( parameterNames[idx] );
                }
                if ( !Utils.isEmpty( parentValue ) ) {
                  job.setParameterValue( parameterNames[idx], parentValue );
                }
              }
            }
          }
          job.activateParameters();

          // Set the source rows we calculated above...
          //
          job.setSourceRows( sourceRows );

          // Don't forget the logging...
          job.beginProcessing();

          // Link the job with the sub-job
          parentJob.getJobTracker().addJobTracker( job.getJobTracker() );

          // Link both ways!
          job.getJobTracker().setParentJobTracker( parentJob.getJobTracker() );

          if ( parentJob.getJobMeta().isBatchIdPassed() ) {
            job.setPassedBatchId( parentJob.getBatchId() );
          }

          job.setArguments( args );

          // Inform the parent job we started something here...
          //
          for ( DelegationListener delegationListener : parentJob.getDelegationListeners() ) {
            // TODO: copy some settings in the job execution configuration, not strictly needed
            // but the execution configuration information is useful in case of a job re-start
            //
            delegationListener.jobDelegationStarted( job, new JobExecutionConfiguration() );
          }

          JobEntryJobRunner runner = new JobEntryJobRunner( job, result, nr, log );
          Thread jobRunnerThread = new Thread( runner );
          // PDI-6518
          // added UUID to thread name, otherwise threads do share names if jobs entries are executed in parallel in a
          // parent job
          // if that happens, contained transformations start closing each other's connections
          jobRunnerThread.setName( Const.NVL( job.getJobMeta().getName(), job.getJobMeta().getFilename() )
            + " UUID: " + UUID.randomUUID().toString() );
          jobRunnerThread.start();

          // Keep running until we're done.
          //
          while ( !runner.isFinished() && !parentJob.isStopped() ) {
            try {
              Thread.sleep( 0, 1 );
            } catch ( InterruptedException e ) {
              // Ignore
            }
          }

          // if the parent-job was stopped, stop the sub-job too...
          if ( parentJob.isStopped() ) {
            job.stopAll();
            runner.waitUntilFinished(); // Wait until finished!
          }

          oneResult = runner.getResult();

        } else {

          // Make sure we can parameterize the slave server connection
          //
          remoteSlaveServer.shareVariablesWith( this );

          // Remote execution...
          //
          JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
          jobExecutionConfiguration.setPreviousResult( result.lightClone() ); // lightClone() because rows are
                                                                              // overwritten in next line.
          jobExecutionConfiguration.getPreviousResult().setRows( sourceRows );
          jobExecutionConfiguration.setArgumentStrings( args );
          jobExecutionConfiguration.setVariables( this );
          jobExecutionConfiguration.setRemoteServer( remoteSlaveServer );
          jobExecutionConfiguration.setRepository( rep );
          jobExecutionConfiguration.setLogLevel( jobLogLevel );
          jobExecutionConfiguration.setPassingExport( passingExport );
          jobExecutionConfiguration.setExpandingRemoteJob( expandingRemoteJob );
          for ( String param : namedParam.listParameters() ) {
            String defValue = namedParam.getParameterDefault( param );
            String value = namedParam.getParameterValue( param );
            jobExecutionConfiguration.getParams().put( param, Const.NVL( value, defValue ) );
          }
          if ( parentJob.getJobMeta().isBatchIdPassed() ) {
            jobExecutionConfiguration.setPassedBatchId( parentJob.getBatchId() );
          }

          // Send the XML over to the slave server
          // Also start the job over there...
          //
          String carteObjectId = null;
          try {
            carteObjectId = Job.sendToSlaveServer( jobMeta, jobExecutionConfiguration, rep, metaStore );
          } catch ( KettleException e ) {
            // Perhaps the job exists on the remote server, carte is down, etc.
            // This is an abort situation, stop the parent job...
            // We want this in case we are running in parallel. The other job
            // entries can stop running now.
            //
            parentJob.stopAll();

            // Pass the exception along
            //
            throw e;
          }

          // Now start the monitoring...
          //
          SlaveServerJobStatus jobStatus = null;
          while ( !parentJob.isStopped() && waitingToFinish ) {
            try {
              jobStatus = remoteSlaveServer.getJobStatus( jobMeta.getName(), carteObjectId, 0 );
              if ( jobStatus.getResult() != null ) {
                // The job is finished, get the result...
                //
                oneResult = jobStatus.getResult();
                break;
              }
            } catch ( Exception e1 ) {
              logError( "Unable to contact slave server ["
                + remoteSlaveServer + "] to verify the status of job [" + jobMeta.getName() + "]", e1 );
              oneResult.setNrErrors( 1L );
              break; // Stop looking too, chances are too low the server will
                     // come back on-line
            }

            // sleep for 1 second
            try {
              Thread.sleep( 1000 );
            } catch ( InterruptedException e ) {
              // Ignore
            }
          }

          // PDI-14781
          // Write log from carte to file
          if ( setLogfile && jobStatus != null ) {
            String logFromCarte = jobStatus.getLoggingString();
            if ( !Utils.isEmpty( logFromCarte ) ) {
              FileObject logfile = logChannelFileWriter.getLogFile();
              OutputStream logFileOutputStream = null;
              try {
                logFileOutputStream = KettleVFS.getOutputStream( logfile, setAppendLogfile );
                logFileOutputStream.write( logFromCarte.getBytes() );
                logFileOutputStream.flush();
              } catch ( Exception e ) {
                logError( "There was an error logging to file '" + logfile + "'", e );
              } finally {
                try {
                  if ( logFileOutputStream != null ) {
                    logFileOutputStream.close();
                    logFileOutputStream = null;
                  }
                } catch ( Exception e ) {
                  logError( "There was an error closing log file file '" + logfile + "'", e );
                }
              }
            }
          }

          if ( !waitingToFinish ) {
            // Since the job was posted successfully, the result is true...
            //
            oneResult = new Result();
            oneResult.setResult( true );
          }

          if ( parentJob.isStopped() ) {
            try {
              // See if we have a status and if we need to stop the remote
              // execution here...
              //
              if ( jobStatus == null || jobStatus.isRunning() ) {
                // Try a remote abort ...
                //
                remoteSlaveServer.stopJob( jobMeta.getName(), carteObjectId );
              }
            } catch ( Exception e1 ) {
              logError( "Unable to contact slave server ["
                + remoteSlaveServer + "] to stop job [" + jobMeta.getName() + "]", e1 );
              oneResult.setNrErrors( 1L );
              break; // Stop looking too, chances are too low the server will
                     // come back on-line
            }
          }

        }

        result.clear(); // clear only the numbers, NOT the files or rows.
        result.add( oneResult );

        // Set the result rows too, if any ...
        if ( !Utils.isEmpty( oneResult.getRows() ) ) {
          result.setRows( new ArrayList<RowMetaAndData>( oneResult.getRows() ) );
        }

        // if one of them fails (in the loop), increase the number of errors
        //
        if ( oneResult.getResult() == false ) {
          result.setNrErrors( result.getNrErrors() + 1 );
        }

        iteration++;
      }

    } catch ( KettleException ke ) {
      logError( "Error running job entry 'job' : ", ke );

      result.setResult( false );
      result.setNrErrors( 1L );
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

    if ( result.getNrErrors() > 0 ) {
      result.setResult( false );
    } else {
      result.setResult( true );
    }

    return result;
  }

  private boolean createParentFolder( String filename ) {
    // Check for parent folder
    FileObject parentfolder = null;
    boolean resultat = true;
    try {
      // Get parent folder
      parentfolder = KettleVFS.getFileObject( filename, this ).getParent();
      if ( !parentfolder.exists() ) {
        if ( createParentFolder ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobJob.Log.ParentLogFolderNotExist", parentfolder
              .getName().toString() ) );
          }
          parentfolder.createFolder();
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobJob.Log.ParentLogFolderCreated", parentfolder
              .getName().toString() ) );
          }
        } else {
          log.logError( BaseMessages.getString( PKG, "JobJob.Log.ParentLogFolderNotExist", parentfolder
            .getName().toString() ) );
          resultat = false;
        }
      } else {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "JobJob.Log.ParentLogFolderExists", parentfolder
            .getName().toString() ) );
        }
      }
    } catch ( Exception e ) {
      resultat = false;
      log.logError( BaseMessages.getString( PKG, "JobJob.Error.ChekingParentLogFolderTitle" ), BaseMessages
        .getString( PKG, "JobJob.Error.ChekingParentLogFolder", parentfolder.getName().toString() ), e );
    } finally {
      if ( parentfolder != null ) {
        try {
          parentfolder.close();
          parentfolder = null;
        } catch ( Exception ex ) {
          // Ignore
        }
      }
    }

    return resultat;
  }

  /**
   * Make sure that we are not loading jobs recursively...
   *
   * @param parentJobMeta
   *          the parent job metadata
   * @param jobMeta
   *          the job metadata
   * @throws KettleException
   *           in case both jobs are loaded from the same source
   */
  private void verifyRecursiveExecution( Job parentJob, JobMeta jobMeta ) throws KettleException {

    if ( parentJob == null ) {
      return; // OK!
    }

    JobMeta parentJobMeta = parentJob.getJobMeta();

    if ( parentJobMeta.getName() == null && jobMeta.getName() != null ) {
      return; // OK
    }
    if ( parentJobMeta.getName() != null && jobMeta.getName() == null ) {
      return; // OK as well.
    }

    // Not from the repository? just verify the filename
    //
    if ( jobMeta.getFilename() != null && jobMeta.getFilename().equals( parentJobMeta.getFilename() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobJobError.Recursive", jobMeta.getFilename() ) );
    }

    // Different directories: OK
    if ( parentJobMeta.getRepositoryDirectory() == null && jobMeta.getRepositoryDirectory() != null ) {
      return;
    }
    if ( parentJobMeta.getRepositoryDirectory() != null && jobMeta.getRepositoryDirectory() == null ) {
      return;
    }
    if ( jobMeta.getRepositoryDirectory().getObjectId() != parentJobMeta.getRepositoryDirectory().getObjectId() ) {
      return;
    }

    // Same names, same directories : loaded from same location in the
    // repository:
    // --> recursive loading taking place!
    //
    if ( parentJobMeta.getName().equals( jobMeta.getName() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobJobError.Recursive", jobMeta.getFilename() ) );
    }

    // Also compare with the grand-parent (if there is any)
    verifyRecursiveExecution( parentJob.getParentJob(), jobMeta );
  }

  @Override
  public void clear() {
    super.clear();

    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    jobname = null;
    filename = null;
    directory = null;
    arguments = null;
    argFromPrevious = false;
    addDate = false;
    addTime = false;
    logfile = null;
    logext = null;
    setLogfile = false;
    setAppendLogfile = false;
    runConfiguration = null;
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
    JobMeta jobMeta = getJobMeta( repository, metaStore, space );
    return jobMeta.getSQLStatements( repository, null );
  }

  /**
   * @deprecated use {@link #getJobMeta(Repository, IMetaStore, VariableSpace)}
   * @param rep
   * @param space
   * @return
   * @throws KettleException
   */
  @Deprecated
  public JobMeta getJobMeta( Repository rep, VariableSpace space ) throws KettleException {
    parentJobMeta.getMetaFileCache( ); //Get the cache from the parent or create it
    if ( rep != null ) {
      return getJobMeta( rep, rep.getMetaStore(), space );
    } else {
      return getJobMeta( rep, getMetaStore(), space );
    }
  }

  protected JobMeta getJobMetaFromRepository( Repository rep, CurrentDirectoryResolver r, String transPath, VariableSpace tmpSpace ) throws KettleException {
    String realJobName = "";
    String realDirectory = "/";

    int index = transPath.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( index != -1 ) {
      realJobName = transPath.substring( index + 1 );
      realDirectory = index == 0 ? RepositoryFile.SEPARATOR : transPath.substring( 0, index );
    }

    realDirectory = r.normalizeSlashes( realDirectory );
    RepositoryDirectoryInterface repositoryDirectory = rep.loadRepositoryDirectoryTree().findDirectory( realDirectory );
    if ( repositoryDirectory == null ) {
      throw new KettleException( "Unable to find repository directory [" + Const.NVL( realDirectory, "" ) + "]" );
    }
    JobMeta jobMeta = rep.loadJob( realJobName, repositoryDirectory, null, null ); //reads
    if ( jobMeta != null ) {
      jobMeta.initializeVariablesFrom( tmpSpace );
    }
    return jobMeta;
  }

  public JobMeta getJobMeta( Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    try {
      IMetaFileLoader<JobMeta> metaFileLoader = new MetaFileLoaderImpl<>( this, specificationMethod );
      JobMeta jobMeta = metaFileLoader.getMetaForEntry( rep, metaStore, space );

      if ( jobMeta != null ) {
        jobMeta.setRepository( rep );
        jobMeta.setMetaStore( metaStore );
      }

      return jobMeta;
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error during job metadata load", e );
    }

  }

  /**
   * @return Returns the runEveryResultRow.
   */
  public boolean isExecPerRow() {
    return execPerRow;
  }

  /**
   * @param runEveryResultRow
   *          The runEveryResultRow to set.
   */
  public void setExecPerRow( boolean runEveryResultRow ) {
    this.execPerRow = runEveryResultRow;
  }

  @Override
  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( filename ) ) {
      String realFileName = jobMeta.environmentSubstitute( filename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realFileName, ResourceType.ACTIONFILE ) );
      references.add( reference );
    }
    return references;
  }

  /**
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. The supplied
   * resource naming interface allows the object to name appropriately without worrying about those parts of the
   * implementation specific details.
   *
   * @param space
   *          The variable space to resolve (environment) variables with.
   * @param definitions
   *          The map containing the filenames and content
   * @param namingInterface
   *          The resource naming interface allows the object to be named appropriately
   * @param repository
   *          The repository to load resources from
   * @param metaStore
   *          the metaStore to load external metadata from
   *
   * @return The filename for this object. (also contained in the definitions map)
   * @throws KettleException
   *           in case something goes wrong during the export
   */
  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    // Try to load the transformation from repository or file.
    // Modify this recursively too...
    //
    // AGAIN: there is no need to clone this job entry because the caller is
    // responsible for this.
    //
    // First load the job meta data...
    //
    copyVariablesFrom( space ); // To make sure variables are available.
    JobMeta jobMeta = getJobMeta( repository, metaStore, space );

    // Also go down into the job and export the files there. (going down
    // recursively)
    //
    String proposedNewFilename =
      jobMeta.exportResources( jobMeta, definitions, namingInterface, repository, metaStore );

    // To get a relative path to it, we inject
    // ${Internal.Entry.Current.Directory}
    //
    String newFilename = "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + proposedNewFilename;

    // Set the filename in the job
    //
    jobMeta.setFilename( newFilename );

    // exports always reside in the root directory, in case we want to turn this
    // into a file repository...
    //
    jobMeta.setRepositoryDirectory( new RepositoryDirectory() );

    // export to filename ALWAYS (this allows the exported XML to be executed remotely)
    //
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

    // change it in the job entry
    //
    filename = newFilename;

    return proposedNewFilename;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    if ( setLogfile ) {
      JobEntryValidatorUtils.andValidator().validate( this, "logfile", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    }

    if ( null != directory ) {
      // if from repo
      JobEntryValidatorUtils.andValidator().validate( this, "directory", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
      JobEntryValidatorUtils.andValidator().validate( this, "jobName", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    } else {
      // else from xml file
      JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    }
  }

  public static void main( String[] args ) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryJob().check( remarks, null, new Variables(), null, null );
    System.out.printf( "Remarks: %s\n", remarks );
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
   * @param remoteSlaveServerName
   *          the remoteSlaveServer to set
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
   * @param waitingToFinish
   *          the waitingToFinish to set
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
   * @param followingAbortRemotely
   *          the followingAbortRemotely to set
   */
  public void setFollowingAbortRemotely( boolean followingAbortRemotely ) {
    this.followingAbortRemotely = followingAbortRemotely;
  }

  public void setLoggingRemoteWork( boolean loggingRemoteWork ) {
    // do nothing. for compatibility with JobEntryRunConfigurableInterface
  }

  /**
   * @return the passingAllParameters
   */
  public boolean isPassingAllParameters() {
    return passingAllParameters;
  }

  /**
   * @param passingAllParameters
   *          the passingAllParameters to set
   */
  public void setPassingAllParameters( boolean passingAllParameters ) {
    this.passingAllParameters = passingAllParameters;
  }

  public Job getJob() {
    return job;
  }

  /**
   * @return the jobObjectId
   */
  public ObjectId getJobObjectId() {
    return jobObjectId;
  }

  /**
   * @param jobObjectId
   *          the jobObjectId to set
   */
  public void setJobObjectId( ObjectId jobObjectId ) {
    this.jobObjectId = jobObjectId;
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  @Override
  public ObjectLocationSpecificationMethod[] getSpecificationMethods() {
    return new ObjectLocationSpecificationMethod[]{ specificationMethod };
  }

  /**
   * @param specificationMethod
   *          the specificationMethod to set
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
   * @param repository
   *          the repository to reference.
   */
  @Override
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    // The correct reference is stored in the job name and directory attributes...
    //
    RepositoryDirectoryInterface repositoryDirectoryInterface =
      RepositoryImportLocation.getRepositoryImportLocation().findDirectory( directory );
    jobObjectId = repository.getJobId( jobname, repositoryDirectoryInterface );
  }

  private boolean isJobDefined() {
    return !Utils.isEmpty( filename )
      || jobObjectId != null || ( !Utils.isEmpty( this.directory ) && !Utils.isEmpty( jobname ) );
  }

  @Override
  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isJobDefined(), };
  }

  /**
   * @return The objects referenced in the step, like a a transformation, a job, a mapper, a reducer, a combiner, ...
   */
  @Override
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString( PKG, "JobEntryJob.ReferencedObject.Description" ), };
  }

  /**
   * Load the referenced object
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param metaStore
   *          the metaStore
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  @Override
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return getJobMeta( rep, metaStore, space );
  }

  public boolean isExpandingRemoteJob() {
    return expandingRemoteJob;
  }

  public void setExpandingRemoteJob( boolean expandingRemoteJob ) {
    this.expandingRemoteJob = expandingRemoteJob;
  }

  @Override
  public void setParentJobMeta( JobMeta parentJobMeta ) {
    JobMeta previous = getParentJobMeta();
    super.setParentJobMeta( parentJobMeta );
    if ( previous != null ) {
      previous.removeCurrentDirectoryChangedListener( this.dirListener );
    }
    if ( parentJobMeta !=  null ) {
      parentJobMeta.addCurrentDirectoryChangedListener( this.dirListener );
    }
  }

}
