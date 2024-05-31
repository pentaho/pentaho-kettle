/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.exportrepository;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryExporter;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'Export repository' job entry. Its main use would be export repository objects to a XML file that can
 * be used to control the flow in ETL cycles.
 *
 * @author Samatar
 * @since 04-06-2008
 *
 */

@JobEntry(id = "EXPORT_REPOSITORY",name = "JobEntry.ExportRepository.Tooltip",description = "JobEntry.ExportRepository.TypeDesc",
image = "EREP.svg",categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Repository",documentationUrl = "http://wiki.pentaho.com/display/EAI/Export+repository+to+XML+file",
i18nPackageName = "i18n:org.pentaho.di.job.entry:JobEntry.ExportRepository")
public class JobEntryExportRepository extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryExportRepository.class; // for i18n purposes, needed by Translator2!!

  private String repositoryname;
  private String username;
  private String password;
  private String targetfilename;
  private String iffileexists;
  private String export_type;
  private String directoryPath;

  public String If_FileExists_Skip = "if_file_exists_skip";
  public String If_FileExists_Fail = "if_file_exists_fail";
  public String If_FileExists_Overwrite = "if_file_exists_overwrite";
  public String If_FileExists_Uniquename = "if_file_exists_uniquename";

  public String Export_All = "export_all";
  public String Export_Jobs = "export_jobs";
  public String Export_Trans = "export_trans";
  public String Export_By_Folder = "export_by_folder";
  public String Export_One_Folder = "export_one_folder";

  private boolean add_date;
  private boolean add_time;
  private boolean SpecifyFormat;
  private String date_time_format;
  private boolean createfolder;
  private boolean newfolder;
  private boolean add_result_filesname;
  private String nr_errors_less_than;

  private String success_condition;
  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";
  public String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  FileObject file = null;
  RepositoriesMeta repsinfo = null;
  Repository repository = null;
  RepositoryMeta repositoryMeta = null;

  int NrErrors = 0;
  boolean successConditionBroken = false;
  int limitErr = 0;

  public JobEntryExportRepository( String n ) {
    super( n, "" );
    repositoryname = null;
    targetfilename = null;
    username = null;
    iffileexists = If_FileExists_Skip;
    export_type = Export_All;
    add_date = false;
    add_time = false;
    SpecifyFormat = false;
    date_time_format = null;
    createfolder = false;
    newfolder = false;
    add_result_filesname = false;
    nr_errors_less_than = "10";
    success_condition = SUCCESS_IF_NO_ERRORS;
  }

  public JobEntryExportRepository() {
    this( "" );
  }

  public Object clone() {
    JobEntryExportRepository je = (JobEntryExportRepository) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 400 ); // 300 chars in just tag names and spaces

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "repositoryname", repositoryname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", username ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( getPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targetfilename", targetfilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "iffileexists", iffileexists ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "export_type", export_type ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "directoryPath", directoryPath ) ); // don't loose this
                                                                                                  // info
                                                                                                  // (backup/recovery)
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", add_date ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", add_time ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "SpecifyFormat", SpecifyFormat ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_format", date_time_format ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createfolder", createfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "newfolder", newfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_result_filesname", add_result_filesname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_errors_less_than", nr_errors_less_than ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      repositoryname = XMLHandler.getTagValue( entrynode, "repositoryname" );
      username = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );
      targetfilename = XMLHandler.getTagValue( entrynode, "targetfilename" );
      iffileexists = XMLHandler.getTagValue( entrynode, "iffileexists" );
      export_type = XMLHandler.getTagValue( entrynode, "export_type" );
      directoryPath = XMLHandler.getTagValue( entrynode, "directoryPath" );
      add_date = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_date" ) );
      add_time = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_time" ) );
      SpecifyFormat = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "SpecifyFormat" ) );
      date_time_format = XMLHandler.getTagValue( entrynode, "date_time_format" );
      createfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createfolder" ) );
      newfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "newfolder" ) );
      add_result_filesname = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_result_filesname" ) );
      nr_errors_less_than = XMLHandler.getTagValue( entrynode, "nr_errors_less_than" );
      success_condition = XMLHandler.getTagValue( entrynode, "success_condition" );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobExportRepository.Meta.UnableLoadXML" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      repositoryname = rep.getJobEntryAttributeString( id_jobentry, "repositoryname" );
      username = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );
      targetfilename = rep.getJobEntryAttributeString( id_jobentry, "targetfilename" );
      iffileexists = rep.getJobEntryAttributeString( id_jobentry, "iffileexists" );
      export_type = rep.getJobEntryAttributeString( id_jobentry, "export_type" );
      directoryPath = rep.getJobEntryAttributeString( id_jobentry, "directoryPath" );
      add_date = rep.getJobEntryAttributeBoolean( id_jobentry, "add_date" );
      add_time = rep.getJobEntryAttributeBoolean( id_jobentry, "add_time" );
      SpecifyFormat = rep.getJobEntryAttributeBoolean( id_jobentry, "SpecifyFormat" );
      date_time_format = rep.getJobEntryAttributeString( id_jobentry, "date_time_format" );
      createfolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createfolder" );
      newfolder = rep.getJobEntryAttributeBoolean( id_jobentry, "newfolder" );
      add_result_filesname = rep.getJobEntryAttributeBoolean( id_jobentry, "add_result_filesname" );
      nr_errors_less_than = rep.getJobEntryAttributeString( id_jobentry, "nr_errors_less_than" );

      success_condition = rep.getJobEntryAttributeString( id_jobentry, "success_condition" );

    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobExportRepository.Meta.UnableLoadRep", ""
        + id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "repositoryname", repositoryname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", username );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetfilename", targetfilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "iffileexists", iffileexists );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "export_type", export_type );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "directoryPath", directoryPath );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_date", add_date );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_time", add_time );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "SpecifyFormat", SpecifyFormat );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "date_time_format", date_time_format );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createfolder", createfolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "newfolder", newfolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_result_filesname", add_result_filesname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_errors_less_than", nr_errors_less_than );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobExportRepository.Meta.UnableSaveRep", ""
        + id_job ), dbe );
    }
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
  }

  public void setRepositoryname( String repositoryname ) {
    this.repositoryname = repositoryname;
  }

  public String getRepositoryname() {
    return repositoryname;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setExportType( String export_type ) {
    this.export_type = export_type;
  }

  public String getExportType() {
    return export_type;
  }

  public void setIfFileExists( String iffileexists ) {
    this.iffileexists = iffileexists;
  }

  public String getIfFileExists() {
    return iffileexists;
  }

  public void setTargetfilename( String targetfilename ) {
    this.targetfilename = targetfilename;
  }

  public String getTargetfilename() {
    return targetfilename;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  public String getDirectory() {
    return directoryPath;
  }

  public String getDateTimeFormat() {
    return date_time_format;
  }

  public void setDateTimeFormat( String date_time_format ) {
    this.date_time_format = date_time_format;
  }

  public boolean isSpecifyFormat() {
    return SpecifyFormat;
  }

  public void setSpecifyFormat( boolean SpecifyFormat ) {
    this.SpecifyFormat = SpecifyFormat;
  }

  public void setAddTime( boolean addtime ) {
    this.add_time = addtime;
  }

  public boolean isAddTime() {
    return add_time;
  }

  public boolean isCreateFolder() {
    return createfolder;
  }

  public void setCreateFolder( boolean createfolder ) {
    this.createfolder = createfolder;
  }

  public void setNewFolder( boolean newfolder ) {
    this.newfolder = newfolder;
  }

  public boolean isNewFolder() {
    return newfolder;
  }

  public void setDirectory( String directoryPath ) {
    this.directoryPath = directoryPath;
  }

  public void setAddDate( boolean adddate ) {
    this.add_date = adddate;
  }

  public boolean isAddDate() {
    return add_date;
  }

  public void setAddresultfilesname( boolean add_result_filesnamein ) {
    this.add_result_filesname = add_result_filesnamein;
  }

  public boolean isAddresultfilesname() {
    return add_result_filesname;
  }

  public void setNrLimit( String nr_errors_less_than ) {
    this.nr_errors_less_than = nr_errors_less_than;
  }

  public String getNrLimit() {
    return nr_errors_less_than;
  }

  public String buildFilename( String filename ) {
    String retval = "";
    if ( Utils.isEmpty( filename ) ) {
      return null;
    }

    int lenstring = filename.length();
    int lastindexOfDot = filename.lastIndexOf( '.' );
    if ( lastindexOfDot == -1 ) {
      lastindexOfDot = lenstring;
    }

    retval = filename.substring( 0, lastindexOfDot );

    SimpleDateFormat daf = new SimpleDateFormat();
    Date now = new Date();

    if ( isSpecifyFormat() && !Utils.isEmpty( getDateTimeFormat() ) ) {
      daf.applyPattern( getDateTimeFormat() );
      String dt = daf.format( now );
      retval += dt;
    } else {
      if ( isAddDate() ) {
        daf.applyPattern( "yyyyMMdd" );
        String d = daf.format( now );
        retval += "_" + d;
      }
      if ( isAddTime() ) {
        daf.applyPattern( "HHmmssSSS" );
        String t = daf.format( now );
        retval += "_" + t;
      }
    }
    retval += filename.substring( lastindexOfDot, lenstring );
    return retval;
  }

  public String buildUniqueFilename( String filename ) {
    String retval = "";
    if ( Utils.isEmpty( filename ) ) {
      return null;
    }

    int lenstring = filename.length();
    int lastindexOfDot = filename.lastIndexOf( '.' );
    if ( lastindexOfDot == -1 ) {
      lastindexOfDot = lenstring;
    }
    retval = filename.substring( 0, lastindexOfDot );
    retval += StringUtil.getFormattedDateTimeNow();
    retval += filename.substring( lastindexOfDot, lenstring );

    return retval;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    String realrepName = environmentSubstitute( repositoryname );
    String realusername = environmentSubstitute( username );
    String realpassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
    String realfoldername = environmentSubstitute( directoryPath );

    String realoutfilename = environmentSubstitute( targetfilename );
    if ( export_type.equals( Export_All )
      || export_type.equals( Export_Jobs ) || export_type.equals( Export_Trans )
      || export_type.equals( Export_One_Folder ) ) {
      realoutfilename = buildFilename( realoutfilename );
    }

    NrErrors = 0;
    successConditionBroken = false;
    limitErr = Const.toInt( environmentSubstitute( getNrLimit() ), 10 );

    try {
      file = KettleVFS.getFileObject( realoutfilename, this );
      if ( file.exists() ) {
        if ( export_type.equals( Export_All )
          || export_type.equals( Export_Jobs ) || export_type.equals( Export_Trans )
          || export_type.equals( Export_One_Folder ) ) {
          if ( iffileexists.equals( If_FileExists_Fail ) ) {
            logError( BaseMessages.getString( PKG, "JobExportRepository.Log.Failing", realoutfilename ) );
            return result;
          } else if ( iffileexists.equals( If_FileExists_Skip ) ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.Exit", realoutfilename ) );
            }
            result.setResult( true );
            result.setNrErrors( 0 );
            return result;
          } else if ( iffileexists.equals( If_FileExists_Uniquename ) ) {
            String parentFolder = KettleVFS.getFilename( file.getParent() );
            String shortFilename = file.getName().getBaseName();
            shortFilename = buildUniqueFilename( shortFilename );
            file = KettleVFS.getFileObject( parentFolder + Const.FILE_SEPARATOR + shortFilename, this );
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.NewFilename", file.toString() ) );
            }
          }
        } else if ( export_type.equals( Export_By_Folder ) ) {
          if ( file.getType() != FileType.FOLDER ) {
            logError( BaseMessages.getString( PKG, "JobExportRepository.Log.NotFolder", "" + file.getName() ) );
            return result;
          }
        }
      } else {
        if ( export_type.equals( Export_By_Folder ) ) {
          // create folder?
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.FolderNotExists", ""
              + file.getName() ) );
          }
          if ( !createfolder ) {
            return result;
          }
          file.createFolder();
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.FolderCreated", file.toString() ) );
          }
        } else if ( export_type.equals( Export_All )
          || export_type.equals( Export_Jobs ) || export_type.equals( Export_Trans )
          || export_type.equals( Export_One_Folder ) ) {
          // create parent folder?
          if ( !file.getParent().exists() ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.FolderNotExists", ""
                + file.getParent().toString() ) );
            }
            if ( createfolder ) {
              file.getParent().createFolder();
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.FolderCreated", file
                  .getParent().toString() ) );
              }
            } else {
              return result;
            }
          }
        }
      }

      realoutfilename = KettleVFS.getFilename( this.file );

      // connect to repository
      connectRep( log, realrepName, realusername, realpassword );

      IRepositoryExporter exporter = repository.getExporter();

      if ( export_type.equals( Export_All ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobExportRepository.Log.StartingExportAllRep", realoutfilename ) );
        }
        exporter.exportAllObjects( null, realoutfilename, null, "all" );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.EndExportAllRep", realoutfilename ) );
        }

        if ( add_result_filesname ) {
          addFileToResultFilenames( realoutfilename, log, result, parentJob );
        }
      } else if ( export_type.equals( Export_Jobs ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobExportRepository.Log.StartingExportJobsRep", realoutfilename ) );
        }
        exporter.exportAllObjects( null, realoutfilename, null, "jobs" );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.EndExportJobsRep", realoutfilename ) );
        }

        if ( add_result_filesname ) {
          addFileToResultFilenames( realoutfilename, log, result, parentJob );
        }
      } else if ( export_type.equals( Export_Trans ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobExportRepository.Log.StartingExportTransRep", realoutfilename ) );
        }
        exporter.exportAllObjects( null, realoutfilename, null, "trans" );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.EndExportTransRep", realoutfilename ) );
        }

        if ( add_result_filesname ) {
          addFileToResultFilenames( realoutfilename, log, result, parentJob );
        }
      } else if ( export_type.equals( Export_One_Folder ) ) {
        RepositoryDirectoryInterface directory = new RepositoryDirectory();
        directory = repository.findDirectory( realfoldername );
        if ( directory != null ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobExportRepository.Log.ExpAllFolderRep", directoryPath, realoutfilename ) );
          }
          exporter.exportAllObjects( null, realoutfilename, directory, "all" );
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobExportRepository.Log.EndExpAllFolderRep", directoryPath, realoutfilename ) );
          }

          if ( add_result_filesname ) {
            addFileToResultFilenames( realoutfilename, log, result, parentJob );
          }
        } else {
          logError( BaseMessages.getString(
            PKG, "JobExportRepository.Error.CanNotFindFolderInRep", realfoldername, realrepName ) );
          return result;
        }
      } else if ( export_type.equals( Export_By_Folder ) ) {
        // User must give a destination folder..

        RepositoryDirectoryInterface directory = new RepositoryDirectory();
        directory = this.repository.loadRepositoryDirectoryTree().findRoot();
        // Loop over all the directory id's
        ObjectId[] dirids = directory.getDirectoryIDs();
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobExportRepository.Log.TotalFolders", "" + dirids.length ) );
        }
        for ( int d = 0; d < dirids.length && !parentJob.isStopped(); d++ ) {
          // Success condition broken?
          if ( successConditionBroken ) {
            logError( BaseMessages.getString( PKG, "JobExportRepository.Error.SuccessConditionbroken", ""
              + NrErrors ) );
            throw new Exception( BaseMessages.getString(
              PKG, "JobExportRepository.Error.SuccessConditionbroken", "" + NrErrors ) );
          }

          RepositoryDirectoryInterface repdir = directory.findDirectory( dirids[d] );
          if ( !processOneFolder( parentJob, result, log, repdir, realoutfilename, d, dirids.length ) ) {
            // updateErrors
            updateErrors();
          }
        } // end for
      }

    } catch ( Exception e ) {
      updateErrors();
      logError( BaseMessages.getString( PKG, "JobExportRepository.UnExpectedError", e.toString() ) );
      logError( "Stack trace: " + Const.CR + Const.getStackTracker( e ) );
    } finally {
      if ( this.repository != null ) {
        this.repository.disconnect();
        this.repository = null;
      }
      if ( this.repositoryMeta != null ) {
        this.repositoryMeta = null;
      }
      if ( this.repsinfo != null ) {
        this.repsinfo.clear();
        this.repsinfo = null;
      }
      if ( this.file != null ) {
        try {
          this.file.close();
          this.file = null;
        } catch ( Exception e ) {
          // Ignore close errors
        }
      }
    }

    // Success Condition
    result.setNrErrors( NrErrors );
    if ( getSuccessStatus() ) {
      result.setResult( true );
    }

    return result;
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( this.NrErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( this.NrErrors <= this.limitErr && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  private void updateErrors() {
    this.NrErrors++;
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      this.successConditionBroken = true;
    }
  }

  private boolean processOneFolder( Job parentJob, Result result, LogChannelInterface log,
    RepositoryDirectoryInterface repdir, String realoutfilename, int folderno, int totalfolders ) {
    boolean retval = false;
    try {
      if ( !repdir.isRoot() ) {
        if ( repdir.toString().lastIndexOf( "/" ) == 0 ) {
          String filename = repdir.toString().replace( "/", "" );
          String foldername = realoutfilename;
          if ( newfolder ) {
            foldername = realoutfilename + Const.FILE_SEPARATOR + filename;
            this.file = KettleVFS.getFileObject( foldername, this );
            if ( !this.file.exists() ) {
              this.file.createFolder();
            }
          }

          filename = foldername + Const.FILE_SEPARATOR + buildFilename( filename ) + ".xml";
          this.file = KettleVFS.getFileObject( filename, this );

          if ( this.file.exists() ) {
            if ( iffileexists.equals( If_FileExists_Skip ) ) {
              // Skip this folder
              return true;
            } else if ( iffileexists.equals( If_FileExists_Uniquename ) ) {
              filename = realoutfilename + Const.FILE_SEPARATOR + buildUniqueFilename( filename ) + ".xml";
            } else if ( iffileexists.equals( If_FileExists_Fail ) ) {
              // Fail
              return false;
            }
          }

          // System.out.print(filename + "\n");
          if ( log.isDetailed() ) {
            logDetailed( "---" );
            logDetailed( BaseMessages.getString(
              PKG, "JobExportRepository.Log.FolderProcessing", "" + folderno, "" + totalfolders ) );
            logDetailed( BaseMessages.getString(
              PKG, "JobExportRepository.Log.OutFilename", repdir.toString(), filename ) );
          }

          new RepositoryExporter( this.repository ).exportAllObjects( null, filename, repdir, "all" );
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString(
              PKG, "JobExportRepository.Log.OutFilenameEnd", repdir.toString(), filename ) );
          }

          if ( add_result_filesname ) {
            addFileToResultFilenames( filename, log, result, parentJob );
          }

        }
      } // end if root
      retval = true;
    } catch ( Exception e ) {
      // Update errors
      updateErrors();
      logError( BaseMessages.getString( PKG, "JobExportRepository.ErrorExportingFolder", repdir.toString(), e
        .toString() ) );
    }
    return retval;
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( this.NrErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( this.NrErrors >= this.limitErr && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private void connectRep( LogChannelInterface log, String realrepName, String realusername, String realpassword ) throws Exception {
    this.repsinfo = new RepositoriesMeta();
    this.repsinfo.getLog().setLogLevel( log.getLogLevel() );
    try {
      this.repsinfo.readData();
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobExportRepository.Error.NoRep" ) );
      throw new Exception( BaseMessages.getString( PKG, "JobExportRepository.Error.NoRep" ) );
    }
    this.repositoryMeta = this.repsinfo.findRepository( realrepName );
    if ( this.repositoryMeta == null ) {
      logError( BaseMessages.getString( PKG, "JobExportRepository.Error.NoRepSystem" ) );
      throw new Exception( BaseMessages.getString( PKG, "JobExportRepository.Error.NoRepSystem" ) );
    }

    this.repository =
      PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, this.repositoryMeta, Repository.class );
    this.repository.init( repositoryMeta );

    try {
      this.repository.connect( realusername, realpassword );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobExportRepository.Error.CanNotConnectRep" ) );
      throw new Exception( BaseMessages.getString( PKG, "JobExportRepository.Error.CanNotConnectRep" ), e );
    }
  }

  private void addFileToResultFilenames( String fileaddentry, LogChannelInterface log, Result result, Job parentJob ) {
    try {
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( fileaddentry, this ), parentJob
          .getJobname(), toString() );
      result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobExportRepository.Log.FileAddedToResultFilesName", fileaddentry ) );
      }
    } catch ( Exception e ) {
      log.logError(
        BaseMessages.getString( PKG, "JobExportRepository.Error.AddingToFilenameResult" ), fileaddentry
          + "" + e.getMessage() );
    }
  }

  public boolean evaluates() {
    return true;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "repositoryname", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "targetfilename", remarks, ctx );

    JobEntryValidatorUtils.andValidator().validate( this, "username", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
  }
}
