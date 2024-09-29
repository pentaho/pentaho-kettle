/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.msaccessbulkload;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.healthmarketscience.jackcess.Database;

/**
 * This defines a 'MS Access Bulk Load' job entry. It will compare to load data from files into Microsoft Access files
 *
 * @author Samatar Hassan
 * @since 24-07-2008
 *
 */

// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@JobEntry( id = "MS_ACCESS_BULK_LOAD", image = "ui/images/deprecated.svg",
//  suggestion = "JobEntryMSAccessBulkLoad.SuggestedEntry",
//  categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Deprecated",
//  i18nPackageName = "org.pentaho.di.job.entries.msaccessbulkload",
//  name = "JobEntryMSAccessBulkLoad.Name.Default", description = "JobEntryMSAccessBulkLoad.Tooltip" )
public class JobEntryMSAccessBulkLoad extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMSAccessBulkLoad.class; // for i18n purposes, needed by Translator2!!

  private boolean add_result_filenames;
  private boolean include_subfolders;
  private boolean is_args_from_previous;
  public String[] source_filefolder;
  public String[] source_wildcard;
  public String[] delimiter;
  public String[] target_Db;
  public String[] target_table;

  private String limit;

  private String success_condition;
  public String SUCCESS_IF_AT_LEAST = "success_when_at_least";
  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";
  public String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  private int NrErrors = 0;
  private int NrSuccess = 0;
  private int NrFilesToProcess = 0;
  private boolean continueProcessing = true;
  int limitFiles = 0;

  public JobEntryMSAccessBulkLoad( String n ) {
    super( n, "" );
    limit = "10";
    success_condition = SUCCESS_IF_NO_ERRORS;
    add_result_filenames = false;
    include_subfolders = false;
    source_filefolder = null;
    source_wildcard = null;
    delimiter = null;
    target_Db = null;
    target_table = null;
  }

  public JobEntryMSAccessBulkLoad() {
    this( "" );
  }

  public Object clone() {
    JobEntryMSAccessBulkLoad je = (JobEntryMSAccessBulkLoad) super.clone();
    return je;
  }

  public void setAddResultFilenames( boolean addtoresultfilenames ) {
    this.add_result_filenames = addtoresultfilenames;
  }

  public boolean isAddResultFilename() {
    return add_result_filenames;
  }

  public void setIncludeSubFoders( boolean includeSubfolders ) {
    this.include_subfolders = includeSubfolders;
  }

  public boolean isIncludeSubFoders() {
    return include_subfolders;
  }

  public void setArgsFromPrevious( boolean isargsfromprevious ) {
    this.is_args_from_previous = isargsfromprevious;
  }

  public boolean isArgsFromPrevious() {
    return is_args_from_previous;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", include_subfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "is_args_from_previous", is_args_from_previous ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "add_result_filenames", add_result_filenames ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "limit", limit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( source_filefolder != null ) {
      for ( int i = 0; i < source_filefolder.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "source_filefolder", source_filefolder[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "source_wildcard", source_wildcard[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "delimiter", delimiter[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "target_db", target_Db[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "target_table", target_table[i] ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      include_subfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_subfolders" ) );
      add_result_filenames = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_result_filenames" ) );
      is_args_from_previous = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "is_args_from_previous" ) );

      limit = XMLHandler.getTagValue( entrynode, "limit" );
      success_condition = XMLHandler.getTagValue( entrynode, "success_condition" );
      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      source_filefolder = new String[nrFields];
      delimiter = new String[nrFields];
      source_wildcard = new String[nrFields];
      target_Db = new String[nrFields];
      target_table = new String[nrFields];

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        source_filefolder[i] = XMLHandler.getTagValue( fnode, "source_filefolder" );
        source_wildcard[i] = XMLHandler.getTagValue( fnode, "source_wildcard" );
        delimiter[i] = XMLHandler.getTagValue( fnode, "delimiter" );
        target_Db[i] = XMLHandler.getTagValue( fnode, "target_db" );
        target_table[i] = XMLHandler.getTagValue( fnode, "target_table" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Meta.UnableLoadXML", xe
        .getMessage() ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      include_subfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "include_subfolders" );
      add_result_filenames = rep.getJobEntryAttributeBoolean( id_jobentry, "add_result_filenames" );
      is_args_from_previous = rep.getJobEntryAttributeBoolean( id_jobentry, "is_args_from_previous" );

      limit = rep.getJobEntryAttributeString( id_jobentry, "limit" );
      success_condition = rep.getJobEntryAttributeString( id_jobentry, "success_condition" );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "source_filefolder" );
      source_filefolder = new String[argnr];
      source_wildcard = new String[argnr];
      delimiter = new String[argnr];
      target_Db = new String[argnr];
      target_table = new String[argnr];

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        source_filefolder[a] = rep.getJobEntryAttributeString( id_jobentry, a, "source_filefolder" );
        source_wildcard[a] = rep.getJobEntryAttributeString( id_jobentry, a, "source_wildcard" );
        delimiter[a] = rep.getJobEntryAttributeString( id_jobentry, a, "delimiter" );
        target_Db[a] = rep.getJobEntryAttributeString( id_jobentry, a, "target_db" );
        target_table[a] = rep.getJobEntryAttributeString( id_jobentry, a, "target_table" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Meta.UnableLoadRep", ""
        + id_jobentry, dbe.getMessage() ), dbe );
    }
  }

  private void displayResults() {
    if ( log.isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.Info.FilesToLoad", ""
        + NrFilesToProcess ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.Info.FilesLoaded", "" + NrSuccess ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.Info.NrErrors", "" + NrErrors ) );
      logDetailed( "=======================================" );
    }
  }

  public void setLimit( String limit ) {
    this.limit = limit;
  }

  public String getLimit() {
    return limit;
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
  }

  private void addFileToResultFilenames( String fileaddentry, Result result, Job parentJob ) {
    try {
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( fileaddentry, this ), parentJob
          .getJobname(), toString() );
      result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

      if ( log.isDebug() ) {
        logDebug( " ------ " );
        logDebug( BaseMessages.getString(
          PKG, "JobEntryMSAccessBulkLoad.Log.FileAddedToResultFilesName", fileaddentry ) );
      }
    } catch ( Exception e ) {
      log.logError(
        BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Error.AddingToFilenameResult" ), fileaddentry
          + "" + e.getMessage() );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_subfolders", include_subfolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_result_filenames", add_result_filenames );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "is_args_from_previous", is_args_from_previous );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "limit", limit );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
      // save the arguments...
      if ( source_filefolder != null ) {
        for ( int i = 0; i < source_filefolder.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "source_filefolder", source_filefolder[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "source_wildcard", source_wildcard[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "delimiter", delimiter[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "target_Db", target_Db[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "target_table", target_table[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Meta.UnableSave", ""
        + id_job, dbe.getMessage() ), dbe );
    }
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean GetFileWildcard( String selectedfile, String wildcard ) {
    Pattern pattern = null;
    boolean getIt = true;

    if ( !Utils.isEmpty( wildcard ) ) {
      pattern = Pattern.compile( wildcard );
      // First see if the file matches the regular expression!
      if ( pattern != null ) {
        Matcher matcher = pattern.matcher( selectedfile );
        getIt = matcher.matches();
      }
    }
    return getIt;
  }

  private boolean processOneRow( String sourceFileFolder, String SourceWildcard, String Delimiter,
    String targetDb, String targetTable, Job parentJob, Result result ) {
    boolean retval = false;
    try {
      File sourcefilefolder = new File( sourceFileFolder );
      if ( !sourcefilefolder.exists() ) {
        logError( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Error.CanNotFindFile", sourceFileFolder ) );
        return retval;
      }
      if ( sourcefilefolder.isFile() ) {
        // source is a file
        retval = importFile( sourceFileFolder, Delimiter, targetDb, targetTable, result, parentJob );
      } else if ( sourcefilefolder.isDirectory() ) {
        // source is a folder
        File[] listFiles = sourcefilefolder.listFiles();
        int nrFiles = listFiles.length;
        if ( nrFiles > 0 ) {
          // let's fetch children...
          for ( int i = 0; i < nrFiles && !parentJob.isStopped() && continueProcessing; i++ ) {
            File child = listFiles[i];
            String childFullName = child.getAbsolutePath();
            if ( child.isFile() ) {
              if ( Utils.isEmpty( SourceWildcard ) ) {
                retval = importFile( childFullName, Delimiter, targetDb, targetTable, result, parentJob );
              } else {
                if ( GetFileWildcard( childFullName, SourceWildcard ) ) {
                  retval = importFile( childFullName, Delimiter, targetDb, targetTable, result, parentJob );
                }
              }
            } else {
              // let's run process for this folder
              if ( include_subfolders ) {
                processOneRow( childFullName, SourceWildcard, Delimiter, targetDb, targetTable, parentJob, result );
              }

            }
          }
        } else {
          logBasic( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.FolderEmpty", sourceFileFolder ) );
        }

      } else {
        logError( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.UnknowType", sourceFileFolder ) );
      }
    } catch ( Exception e ) {
      logError( e.getMessage() );
      incrErrors();
    }
    return retval;
  }

  private boolean importFile( String sourceFilename, String delimiter, String targetFilename, String tablename,
    Result result, Job parentJob ) {
    boolean retval = false;

    try {

      incrFilesToProcess();

      File sourceDataFile = new File( sourceFilename );
      File targetDbFile = new File( targetFilename );

      // create database if needed
      if ( !targetDbFile.exists() ) {
        Database.create( targetDbFile );
        logBasic( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.DbCreated", targetFilename ) );
      } else {
        // Database exists
        Database db = Database.open( targetDbFile );
        logBasic( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.DbOpened", targetFilename ) );
        // Let's check table
        if ( db.getTable( tablename ) != null ) {
          logBasic( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.TableExists", tablename ) );
        }

        // close database
        if ( db != null ) {
          db.close();
        }
        logBasic( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.Log.DbCosed", targetFilename ) );
      }
      // load data from file
      Database.open( targetDbFile ).importFile( tablename, sourceDataFile, delimiter );

      logBasic( BaseMessages.getString(
        PKG, "JobEntryMSAccessBulkLoad.Log.FileImported", sourceFilename, tablename, targetFilename ) );

      // add filename to result filename
      if ( add_result_filenames ) {
        addFileToResultFilenames( sourceFilename, result, parentJob );
      }

      retval = true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString(
        PKG, "JobEntryMSAccessBulkLoad.Error.LoadingDataToFile", sourceFilename, targetFilename, e.getMessage() ) );

    }
    if ( retval ) {
      incrSuccess();
    } else {
      incrErrors();
    }
    return retval;
  }

  private void incrErrors() {
    NrErrors++;
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      continueProcessing = true;
    }
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( NrErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( NrErrors >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private void incrSuccess() {
    NrSuccess++;
  }

  private void incrFilesToProcess() {
    NrFilesToProcess++;
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;
    result.setResult( false );

    NrErrors = 0;
    NrSuccess = 0;
    NrFilesToProcess = 0;
    continueProcessing = true;
    limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );

    // Get source and destination files, also wildcard
    String[] vsourceFilefolder = source_filefolder;
    String[] vsourceWildcard = source_wildcard;
    String[] vsourceDelimiter = delimiter;
    String[] targetDb = target_Db;
    String[] targetTable = target_table;

    try {

      if ( is_args_from_previous ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEntryMSAccessBulkLoad.Log.ArgFromPrevious.Found", ( rows != null ? rows.size() : 0 ) + "" ) );
        }
      }
      if ( is_args_from_previous && rows != null ) {
        for ( int iteration = 0; iteration < rows.size()
          && !parentJob.isStopped()
          && continueProcessing; iteration++ ) {
          resultRow = rows.get( iteration );

          // Get source and destination file names, also wildcard
          String vSourceFileFolder_previous = resultRow.getString( 0, null );
          String vSourceWildcard_previous = resultRow.getString( 1, null );
          String vDelimiter_previous = resultRow.getString( 2, null );
          String vTargetDb_previous = resultRow.getString( 3, null );
          String vTargetTable_previous = resultRow.getString( 4, null );

          processOneRow(
            vSourceFileFolder_previous, vSourceWildcard_previous, vDelimiter_previous, vTargetDb_previous,
            vTargetTable_previous, parentJob, result );

        }
      } else if ( vsourceFilefolder != null && targetDb != null && targetTable != null ) {
        for ( int i = 0; i < vsourceFilefolder.length && !parentJob.isStopped() && continueProcessing; i++ ) {
          // get real values
          String realSourceFileFolder = environmentSubstitute( vsourceFilefolder[i] );
          String realSourceWildcard = environmentSubstitute( vsourceWildcard[i] );
          String realSourceDelimiter = environmentSubstitute( vsourceDelimiter[i] );
          String realTargetDb = environmentSubstitute( targetDb[i] );
          String realTargetTable = environmentSubstitute( targetTable[i] );

          processOneRow(
            realSourceFileFolder, realSourceWildcard, realSourceDelimiter, realTargetDb, realTargetTable,
            parentJob, result );
        }
      }
    } catch ( Exception e ) {
      incrErrors();
      logError( BaseMessages.getString( PKG, "JobEntryMSAccessBulkLoad.UnexpectedError", e.getMessage() ) );
    }

    // Success Condition
    result.setNrErrors( NrErrors );
    result.setNrLinesInput( NrFilesToProcess );
    result.setNrLinesWritten( NrSuccess );
    if ( getSuccessStatus() ) {
      result.setResult( true );
    }

    displayResults();
    return result;
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( NrErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( NrSuccess >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_AT_LEAST ) )
      || ( NrErrors <= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  public boolean evaluates() {
    return true;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = andValidator().validate( this, "arguments", remarks, putValidators( notNullValidator() ) );

    if ( res == false ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace( ctx, getVariables() );
    putValidators( ctx, notNullValidator(), fileExistsValidator() );

    for ( int i = 0; i < source_filefolder.length; i++ ) {
      andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

}
