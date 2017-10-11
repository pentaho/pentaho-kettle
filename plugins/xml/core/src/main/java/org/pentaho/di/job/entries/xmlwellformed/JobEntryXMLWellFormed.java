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

package org.pentaho.di.job.entries.xmlwellformed;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLCheck;
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
import org.xml.sax.helpers.DefaultHandler;

/**
 * This defines a 'xml well formed' job entry.
 * 
 * @author Samatar Hassan
 * @since 26-03-2008
 */
@JobEntry( id = "XML_WELL_FORMED", i18nPackageName = "org.pentaho.di.job.entries.xmlwellformed", image = "XFC.svg",
    name = "XML_WELL_FORMED.Name", description = "XML_WELL_FORMED.Description",
    categoryDescription = "XML_WELL_FORMED.Category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/Check+if+XML+file+is+well+formed" )
public class JobEntryXMLWellFormed extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryXMLWellFormed.class; // for i18n purposes, needed by Translator2!!

  public static String SUCCESS_IF_AT_LEAST_X_FILES_WELL_FORMED = "success_when_at_least";
  public static String SUCCESS_IF_BAD_FORMED_FILES_LESS = "success_if_bad_formed_files_less";
  public static String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  public static String ADD_ALL_FILENAMES = "all_filenames";
  public static String ADD_WELL_FORMED_FILES_ONLY = "only_well_formed_filenames";
  public static String ADD_BAD_FORMED_FILES_ONLY = "only_bad_formed_filenames";

  @Deprecated
  public boolean arg_from_previous;
  @Deprecated
  public boolean include_subfolders;

  @Deprecated
  public String[] source_filefolder;
  @Deprecated
  public String[] wildcard;
  private String nr_errors_less_than;
  private String success_condition;
  private String resultfilenames;

  int NrAllErrors = 0;
  int NrBadFormed = 0;
  int NrWellFormed = 0;
  int limitFiles = 0;
  int NrErrors = 0;

  boolean successConditionBroken = false;
  boolean successConditionBrokenExit = false;

  public JobEntryXMLWellFormed( String n ) {
    super( n, "" );
    resultfilenames = ADD_ALL_FILENAMES;
    arg_from_previous = false;
    source_filefolder = null;
    wildcard = null;
    include_subfolders = false;
    nr_errors_less_than = "10";
    success_condition = SUCCESS_IF_NO_ERRORS;
  }

  public JobEntryXMLWellFormed() {
    this( "" );
  }

  public Object clone() {
    JobEntryXMLWellFormed je = (JobEntryXMLWellFormed) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", arg_from_previous ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", include_subfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_errors_less_than", nr_errors_less_than ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "resultfilenames", resultfilenames ) );
    retval.append( "      " ).append( XMLHandler.openTag( "fields" ) ).append( Const.CR );
    if ( source_filefolder != null ) {
      for ( int i = 0; i < source_filefolder.length; i++ ) {
        retval.append( "        " ).append( XMLHandler.openTag( "field" ) ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "source_filefolder", source_filefolder[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "wildcard", wildcard[i] ) );
        retval.append( "        " ).append( XMLHandler.closeTag( "field" ) ).append( Const.CR );
      }
    }
    retval.append( "      " ).append( XMLHandler.closeTag( "fields" ) ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      arg_from_previous = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      include_subfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_subfolders" ) );

      nr_errors_less_than = XMLHandler.getTagValue( entrynode, "nr_errors_less_than" );
      success_condition = XMLHandler.getTagValue( entrynode, "success_condition" );
      resultfilenames = XMLHandler.getTagValue( entrynode, "resultfilenames" );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      source_filefolder = new String[nrFields];
      wildcard = new String[nrFields];

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        source_filefolder[i] = XMLHandler.getTagValue( fnode, "source_filefolder" );
        wildcard[i] = XMLHandler.getTagValue( fnode, "wildcard" );
      }
    } catch ( KettleXMLException xe ) {

      throw new KettleXMLException( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.Exception.UnableLoadXML" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      arg_from_previous = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      include_subfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "include_subfolders" );

      nr_errors_less_than = rep.getJobEntryAttributeString( id_jobentry, "nr_errors_less_than" );
      success_condition = rep.getJobEntryAttributeString( id_jobentry, "success_condition" );
      resultfilenames = rep.getJobEntryAttributeString( id_jobentry, "resultfilenames" );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "source_filefolder" );
      source_filefolder = new String[argnr];
      wildcard = new String[argnr];

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        source_filefolder[a] = rep.getJobEntryAttributeString( id_jobentry, a, "source_filefolder" );
        wildcard[a] = rep.getJobEntryAttributeString( id_jobentry, a, "wildcard" );
      }
    } catch ( KettleException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.Exception.UnableLoadRep" )
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", arg_from_previous );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_subfolders", include_subfolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_errors_less_than", nr_errors_less_than );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "resultfilenames", resultfilenames );

      // save the arguments...
      if ( source_filefolder != null ) {
        for ( int i = 0; i < source_filefolder.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "source_filefolder", source_filefolder[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "wildcard", wildcard[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.Exception.UnableSaveRep" )
          + id_job, dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    NrErrors = 0;
    NrWellFormed = 0;
    NrBadFormed = 0;
    limitFiles = Const.toInt( environmentSubstitute( getNrErrorsLessThan() ), 10 );
    successConditionBroken = false;
    successConditionBrokenExit = false;

    // Get source and destination files, also wildcard
    String[] vsourcefilefolder = source_filefolder;
    String[] vwildcard = wildcard;

    if ( arg_from_previous ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.ArgFromPrevious.Found", ( rows != null ? rows
            .size() : 0 )
            + "" ) );
      }

    }
    if ( arg_from_previous && rows != null ) {
      // Copy the input row to the (command line) arguments
      for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
        if ( successConditionBroken ) {
          if ( !successConditionBrokenExit ) {
            logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.SuccessConditionbroken", "" + NrAllErrors ) );
            successConditionBrokenExit = true;
          }
          result.setEntryNr( NrAllErrors );
          result.setNrLinesRejected( NrBadFormed );
          result.setNrLinesWritten( NrWellFormed );
          return result;
        }

        resultRow = rows.get( iteration );

        // Get source and destination file names, also wildcard
        String vsourcefilefolder_previous = resultRow.getString( 0, null );
        String vwildcard_previous = resultRow.getString( 1, null );

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.ProcessingRow", vsourcefilefolder_previous,
              vwildcard_previous ) );
        }

        processFileFolder( vsourcefilefolder_previous, vwildcard_previous, parentJob, result );
      }
    } else if ( vsourcefilefolder != null ) {
      for ( int i = 0; i < vsourcefilefolder.length && !parentJob.isStopped(); i++ ) {
        if ( successConditionBroken ) {
          if ( !successConditionBrokenExit ) {
            logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.SuccessConditionbroken", "" + NrAllErrors ) );
            successConditionBrokenExit = true;
          }
          result.setEntryNr( NrAllErrors );
          result.setNrLinesRejected( NrBadFormed );
          result.setNrLinesWritten( NrWellFormed );
          return result;
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.ProcessingRow", vsourcefilefolder[i],
              vwildcard[i] ) );
        }

        processFileFolder( vsourcefilefolder[i], vwildcard[i], parentJob, result );

      }
    }

    // Success Condition
    result.setNrErrors( NrAllErrors );
    result.setNrLinesRejected( NrBadFormed );
    result.setNrLinesWritten( NrWellFormed );
    if ( getSuccessStatus() ) {
      result.setNrErrors( 0 );
      result.setResult( true );
    }

    displayResults();

    return result;
  }

  private void displayResults() {
    if ( log.isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.Info.FilesInError", "" + NrErrors ) );
      logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.Info.FilesInBadFormed", "" + NrBadFormed ) );
      logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.Info.FilesInWellFormed", "" + NrWellFormed ) );
      logDetailed( "=======================================" );
    }
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( NrAllErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
        || ( NrBadFormed >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_BAD_FORMED_FILES_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( NrAllErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
        || ( NrWellFormed >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_AT_LEAST_X_FILES_WELL_FORMED ) )
        || ( NrBadFormed < limitFiles && getSuccessCondition().equals( SUCCESS_IF_BAD_FORMED_FILES_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  private void updateErrors() {
    NrErrors++;
    updateAllErrors();
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      successConditionBroken = true;
    }
  }

  private void updateAllErrors() {
    NrAllErrors = NrErrors + NrBadFormed;
  }

  public static class XMLTreeHandler extends DefaultHandler {

  }

  private boolean CheckFile( FileObject file ) {
    boolean retval = false;
    try {
      retval = XMLCheck.isXMLFileWellFormed( file );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.ErrorCheckingFile", file.toString(), e.getMessage() ) );
    }

    return retval;
  }

  private boolean processFileFolder( String sourcefilefoldername, String wildcard, Job parentJob, Result result ) {
    boolean entrystatus = false;
    FileObject sourcefilefolder = null;
    FileObject CurrentFile = null;

    // Get real source file and wilcard
    String realSourceFilefoldername = environmentSubstitute( sourcefilefoldername );
    if ( Utils.isEmpty( realSourceFilefoldername ) ) {
      logError( BaseMessages.getString( PKG, "JobXMLWellFormed.log.FileFolderEmpty", sourcefilefoldername ) );
      // Update Errors
      updateErrors();

      return entrystatus;
    }
    String realWildcard = environmentSubstitute( wildcard );

    try {
      sourcefilefolder = KettleVFS.getFileObject( realSourceFilefoldername, this );

      if ( sourcefilefolder.exists() ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.FileExists", sourcefilefolder.toString() ) );
        }
        if ( sourcefilefolder.getType() == FileType.FILE ) {
          entrystatus = checkOneFile( sourcefilefolder, result, parentJob );

        } else if ( sourcefilefolder.getType() == FileType.FOLDER ) {
          FileObject[] fileObjects = sourcefilefolder.findFiles( new AllFileSelector() {
            public boolean traverseDescendents( FileSelectInfo info ) {
              return true;
            }

            public boolean includeFile( FileSelectInfo info ) {

              FileObject fileObject = info.getFile();
              try {
                if ( fileObject == null ) {
                  return false;
                }
                if ( fileObject.getType() != FileType.FILE ) {
                  return false;
                }
              } catch ( Exception ex ) {
                // Upon error don't process the file.
                return false;
              } finally {
                if ( fileObject != null ) {
                  try {
                    fileObject.close();
                  } catch ( IOException ex ) {
                    /* Ignore */
                  }
                }

              }
              return true;
            }
          } );

          if ( fileObjects != null ) {
            for ( int j = 0; j < fileObjects.length && !parentJob.isStopped(); j++ ) {
              if ( successConditionBroken ) {
                if ( !successConditionBrokenExit ) {
                  logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.SuccessConditionbroken", ""
                      + NrAllErrors ) );
                  successConditionBrokenExit = true;
                }
                return false;
              }
              // Fetch files in list one after one ...
              CurrentFile = fileObjects[j];

              if ( !CurrentFile.getParent().toString().equals( sourcefilefolder.toString() ) ) {
                // Not in the Base Folder..Only if include sub folders
                if ( include_subfolders ) {
                  if ( GetFileWildcard( CurrentFile.toString(), realWildcard ) ) {
                    checkOneFile( CurrentFile, result, parentJob );
                  }
                }

              } else {
                // In the base folder
                if ( GetFileWildcard( CurrentFile.toString(), realWildcard ) ) {
                  checkOneFile( CurrentFile, result, parentJob );
                }
              }
            }
          }
        } else {
          logError( BaseMessages
              .getString( PKG, "JobXMLWellFormed.Error.UnknowFileFormat", sourcefilefolder.toString() ) );
          // Update Errors
          updateErrors();
        }
      } else {
        logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.SourceFileNotExists", realSourceFilefoldername ) );
        // Update Errors
        updateErrors();
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.Exception.Processing", realSourceFilefoldername
          .toString(), e ) );
      // Update Errors
      updateErrors();
    } finally {
      if ( sourcefilefolder != null ) {
        try {
          sourcefilefolder.close();
        } catch ( IOException ex ) {
          /* Ignore */
        }

      }
      if ( CurrentFile != null ) {
        try {
          CurrentFile.close();
        } catch ( IOException ex ) {
          /* Ignore */
        }
      }
    }
    return entrystatus;
  }

  private boolean checkOneFile( FileObject file, Result result, Job parentJob ) throws KettleException {
    boolean retval = false;
    try {
      // We deal with a file..so let's check if it's well formed
      boolean retformed = CheckFile( file );
      if ( !retformed ) {
        logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.FileBadFormed", file.toString() ) );
        // Update Bad formed files number
        updateBadFormed();
        if ( resultfilenames.equals( ADD_ALL_FILENAMES ) || resultfilenames.equals( ADD_BAD_FORMED_FILES_ONLY ) ) {
          addFileToResultFilenames( KettleVFS.getFilename( file ), result, parentJob );
        }
      } else {
        if ( log.isDetailed() ) {
          logDetailed( "---------------------------" );
          logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.FileWellFormed", file.toString() ) );
        }
        // Update Well formed files number
        updateWellFormed();
        if ( resultfilenames.equals( ADD_ALL_FILENAMES ) || resultfilenames.equals( ADD_WELL_FORMED_FILES_ONLY ) ) {
          addFileToResultFilenames( KettleVFS.getFilename( file ), result, parentJob );
        }
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to verify file '" + file + "'", e );
    }
    return retval;
  }

  private void updateWellFormed() {
    NrWellFormed++;
  }

  private void updateBadFormed() {
    NrBadFormed++;
    updateAllErrors();
  }

  private void addFileToResultFilenames( String fileaddentry, Result result, Job parentJob ) {
    try {
      ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( fileaddentry, this ), parentJob
              .getJobname(), toString() );
      result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobXMLWellFormed.Log.FileAddedToResultFilesName", fileaddentry ) );
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobXMLWellFormed.Error.AddingToFilenameResult", fileaddentry, e
          .getMessage() ) );
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

  public boolean isIncludeSubfolders() {
    return include_subfolders;
  }

  public void setIncludeSubfolders( boolean include_subfoldersin ) {
    this.include_subfolders = include_subfoldersin;
  }

  public boolean isArgFromPrevious() {
    return arg_from_previous;
  }

  public void setArgFromPrevious( boolean argfrompreviousin ) {
    this.arg_from_previous = argfrompreviousin;
  }

  public void setNrErrorsLessThan( String nr_errors_less_than ) {
    this.nr_errors_less_than = nr_errors_less_than;
  }

  public String[] getSourceFileFolders() {
    return source_filefolder;
  }

  public void setSourceFileFolders( String[] filefolders ) {
    this.source_filefolder = filefolders;
  }

  public String[] getSourceWildcards() {
    return wildcard;
  }

  public void setSourceWildcards( String[] wildcards ) {
    this.wildcard = wildcards;
  }

  public String getNrErrorsLessThan() {
    return nr_errors_less_than;
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
  }

  public void setResultFilenames( String resultfilenames ) {
    this.resultfilenames = resultfilenames;
  }

  public String getResultFilenames() {
    return resultfilenames;
  }

  public boolean evaluates() {
    return true;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
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
