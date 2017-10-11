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

package org.pentaho.di.job.entries.dostounix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'Dos to Unix' job entry.
 *
 * @author Samatar Hassan
 * @since 26-03-2008
 */

public class JobEntryDosToUnix extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static final int LF = 0x0a;
  private static final int CR = 0x0d;

  private static Class<?> PKG = JobEntryDosToUnix.class; // for i18n purposes, needed by Translator2!!

  public static final String[] ConversionTypeDesc = new String[] {
    BaseMessages.getString( PKG, "JobEntryDosToUnix.ConversionType.Guess.Label" ),
    BaseMessages.getString( PKG, "JobEntryDosToUnix.ConversionType.DosToUnix.Label" ),
    BaseMessages.getString( PKG, "JobEntryDosToUnix.ConversionType.UnixToDos.Label" ) };
  public static final String[] ConversionTypeCode = new String[] { "guess", "dostounix", "unixtodos" };

  public static final int CONVERTION_TYPE_GUESS = 0;
  public static final int CONVERTION_TYPE_DOS_TO_UNIX = 1;
  public static final int CONVERTION_TYPE_UNIX_TO_DOS = 2;

  private static final int TYPE_DOS_FILE = 0;
  private static final int TYPE_UNIX_FILE = 1;
  private static final int TYPE_BINAY_FILE = 2;

  public static final String ADD_NOTHING = "nothing";
  public static final String SUCCESS_IF_AT_LEAST_X_FILES_PROCESSED = "success_when_at_least";
  public static final String SUCCESS_IF_ERROR_FILES_LESS = "success_if_error_files_less";
  public static final String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  public static final String ADD_ALL_FILENAMES = "all_filenames";
  public static final String ADD_PROCESSED_FILES_ONLY = "only_processed_filenames";
  public static final String ADD_ERROR_FILES_ONLY = "only_error_filenames";

  public boolean arg_from_previous;
  public boolean include_subfolders;

  public String[] source_filefolder;
  public String[] wildcard;
  public int[] conversionTypes;

  private String nr_errors_less_than;
  private String success_condition;
  private String resultfilenames;

  int nrAllErrors = 0;
  int nrErrorFiles = 0;
  int nrProcessedFiles = 0;
  int limitFiles = 0;
  int nrErrors = 0;

  boolean successConditionBroken = false;
  boolean successConditionBrokenExit = false;

  private static String tempFolder;

  public JobEntryDosToUnix( String n ) {
    super( n, "" );
    resultfilenames = ADD_ALL_FILENAMES;
    arg_from_previous = false;
    source_filefolder = null;
    conversionTypes = null;
    wildcard = null;
    include_subfolders = false;
    nr_errors_less_than = "10";
    success_condition = SUCCESS_IF_NO_ERRORS;
  }

  public JobEntryDosToUnix() {
    this( "" );
  }

  public void allocate( int nrFields ) {
    source_filefolder = new String[nrFields];
    wildcard = new String[nrFields];
    conversionTypes = new int[nrFields];
  }

  public Object clone() {
    JobEntryDosToUnix je = (JobEntryDosToUnix) super.clone();
    if ( source_filefolder != null ) {
      int nrFields = source_filefolder.length;
      je.allocate( nrFields );
      System.arraycopy( source_filefolder, 0, je.source_filefolder, 0, nrFields );
      System.arraycopy( wildcard, 0, je.wildcard, 0, nrFields );
      System.arraycopy( conversionTypes, 0, je.conversionTypes, 0, nrFields );
    }
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
    retval.append( "      <fields>" ).append( Const.CR );
    if ( source_filefolder != null ) {
      for ( int i = 0; i < source_filefolder.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "source_filefolder", source_filefolder[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "wildcard", wildcard[i] ) );
        retval.append( "          " ).append(
          XMLHandler.addTagValue( "ConversionType", getConversionTypeCode( conversionTypes[i] ) ) );
        retval.append( "        </field>" ).append( Const.CR );
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );

    return retval.toString();
  }

  private static String getConversionTypeCode( int i ) {
    if ( i < 0 || i >= ConversionTypeCode.length ) {
      return ConversionTypeCode[0];
    }
    return ConversionTypeCode[i];
  }

  public static String getConversionTypeDesc( int i ) {
    if ( i < 0 || i >= ConversionTypeDesc.length ) {
      return ConversionTypeDesc[0];
    }
    return ConversionTypeDesc[i];
  }

  public static int getConversionTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ConversionTypeDesc.length; i++ ) {
      if ( ConversionTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getConversionTypeByCode( tt );
  }

  private static int getConversionTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ConversionTypeCode.length; i++ ) {
      if ( ConversionTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
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
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        source_filefolder[i] = XMLHandler.getTagValue( fnode, "source_filefolder" );
        wildcard[i] = XMLHandler.getTagValue( fnode, "wildcard" );
        conversionTypes[i] =
          getConversionTypeByCode( Const.NVL( XMLHandler.getTagValue( fnode, "ConversionType" ), "" ) );
      }
    } catch ( KettleXMLException xe ) {

      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobDosToUnix.Error.Exception.UnableLoadXML" ), xe );
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
      allocate( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        source_filefolder[a] = rep.getJobEntryAttributeString( id_jobentry, a, "source_filefolder" );
        wildcard[a] = rep.getJobEntryAttributeString( id_jobentry, a, "wildcard" );
        conversionTypes[a] =
          getConversionTypeByCode( Const.NVL(
            rep.getJobEntryAttributeString( id_jobentry, "ConversionType" ), "" ) );
      }
    } catch ( KettleException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobDosToUnix.Error.Exception.UnableLoadRep" )
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
          rep.saveJobEntryAttribute(
            id_job, getObjectId(), "ConversionType", getConversionTypeCode( conversionTypes[i] ) );
        }
      }
    } catch ( KettleDatabaseException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobDosToUnix.Error.Exception.UnableSaveRep" )
        + id_job, dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    List<RowMetaAndData> rows = previousResult.getRows();
    RowMetaAndData resultRow = null;

    nrErrors = 0;
    nrProcessedFiles = 0;
    nrErrorFiles = 0;
    limitFiles = Const.toInt( environmentSubstitute( getNrErrorsLessThan() ), 10 );
    successConditionBroken = false;
    successConditionBrokenExit = false;
    tempFolder = environmentSubstitute( "%%java.io.tmpdir%%" );

    // Get source and destination files, also wildcard
    String[] vsourcefilefolder = source_filefolder;
    String[] vwildcard = wildcard;

    if ( arg_from_previous ) {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.ArgFromPrevious.Found", ( rows != null ? rows
          .size() : 0 )
          + "" ) );
      }

    }
    if ( arg_from_previous && rows != null ) {
      // Copy the input row to the (command line) arguments
      for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
        if ( successConditionBroken ) {
          if ( !successConditionBrokenExit ) {
            logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.SuccessConditionbroken", "" + nrAllErrors ) );
            successConditionBrokenExit = true;
          }
          result.setEntryNr( nrAllErrors );
          result.setNrLinesRejected( nrErrorFiles );
          result.setNrLinesWritten( nrProcessedFiles );
          return result;
        }

        resultRow = rows.get( iteration );

        // Get source and destination file names, also wildcard
        String vsourcefilefolder_previous = resultRow.getString( 0, null );
        String vwildcard_previous = resultRow.getString( 1, null );
        int convertion_type = JobEntryDosToUnix.getConversionTypeByCode( resultRow.getString( 2, null ) );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobDosToUnix.Log.ProcessingRow", vsourcefilefolder_previous, vwildcard_previous ) );
        }

        processFileFolder( vsourcefilefolder_previous, vwildcard_previous, convertion_type, parentJob, result );
      }
    } else if ( vsourcefilefolder != null ) {
      for ( int i = 0; i < vsourcefilefolder.length && !parentJob.isStopped(); i++ ) {
        if ( successConditionBroken ) {
          if ( !successConditionBrokenExit ) {
            logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.SuccessConditionbroken", "" + nrAllErrors ) );
            successConditionBrokenExit = true;
          }
          result.setEntryNr( nrAllErrors );
          result.setNrLinesRejected( nrErrorFiles );
          result.setNrLinesWritten( nrProcessedFiles );
          return result;
        }

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobDosToUnix.Log.ProcessingRow", vsourcefilefolder[i], vwildcard[i] ) );
        }

        processFileFolder( vsourcefilefolder[i], vwildcard[i], conversionTypes[i], parentJob, result );

      }
    }

    // Success Condition
    result.setNrErrors( nrAllErrors );
    result.setNrLinesRejected( nrErrorFiles );
    result.setNrLinesWritten( nrProcessedFiles );
    if ( getSuccessStatus() ) {
      result.setNrErrors( 0 );
      result.setResult( true );
    }

    displayResults();

    return result;
  }

  private void displayResults() {
    if ( isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.Info.Errors", nrErrors ) );
      logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.Info.ErrorFiles", nrErrorFiles ) );
      logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.Info.FilesProcessed", nrProcessedFiles ) );
      logDetailed( "=======================================" );
    }
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( nrAllErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( nrErrorFiles >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERROR_FILES_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( nrAllErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
      || ( nrProcessedFiles >= limitFiles && getSuccessCondition()
        .equals( SUCCESS_IF_AT_LEAST_X_FILES_PROCESSED ) )
      || ( nrErrorFiles < limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERROR_FILES_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  private void updateErrors() {
    nrErrors++;
    updateAllErrors();
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      successConditionBroken = true;
    }
  }

  private void updateAllErrors() {
    nrAllErrors = nrErrors + nrErrorFiles;
  }

  private static int getFileType( FileObject file ) throws Exception {
    int aCount = 0; // occurences of LF
    int dCount = 0; // occurences of CR
    FileInputStream in = null;
    try {
      in = new FileInputStream( file.getName().getPathDecoded() );
      while ( in.available() > 0 ) {
        int b = in.read();
        if ( b == CR ) {
          dCount++;
          if ( in.available() > 0 ) {
            b = in.read();
            if ( b == LF ) {
              aCount++;
            } else {
              return TYPE_BINAY_FILE;
            }
          }
        } else if ( b == LF ) {
          aCount++;
        }
      }
    } finally {
      in.close();
    }

    if ( aCount == dCount ) {
      return TYPE_DOS_FILE;
    } else {
      return TYPE_UNIX_FILE;
    }
  }

  @VisibleForTesting
  boolean convert( FileObject file, boolean toUnix ) {
    boolean retval = false;
    // CR = CR
    // LF = LF
    try {
      String localfilename = KettleVFS.getFilename( file );
      File source = new File( localfilename );
      if ( isDetailed() ) {
        if ( toUnix ) {
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.ConvertingFileToUnix", source
            .getAbsolutePath() ) );
        } else {
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.ConvertingFileToDos", source
            .getAbsolutePath() ) );
        }
      }
      File tempFile = new File( tempFolder, source.getName() + ".tmp" );

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobDosToUnix.Log.CreatingTempFile", tempFile.getAbsolutePath() ) );
      }

      final int FOUR_KB = 4 * 1024;
      byte[] buffer = new byte[ FOUR_KB ];
      try ( FileOutputStream out = new FileOutputStream( tempFile );
            FileInputStream in = new FileInputStream( localfilename ) ) {

        ConversionAutomata automata = new ConversionAutomata( out, toUnix );
        int read;
        while ( ( read = in.read( buffer ) ) > 0 ) {
          automata.convert( buffer, read );
        }
      }

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobDosToUnix.Log.DeletingSourceFile", localfilename ) );
      }
      if ( isDebug() ) {
        logDebug( BaseMessages.getString(
          PKG, "JobDosToUnix.Log.RenamingTempFile", tempFile.getAbsolutePath(), source.getAbsolutePath() ) );
      }
      Files.move( tempFile.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING );
      retval = true;

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobDosToUnix.Log.ErrorConvertingFile", file.toString(), e
        .getMessage() ) );
    }

    return retval;
  }

  private boolean processFileFolder( String sourcefilefoldername, String wildcard, int convertion, Job parentJob,
    Result result ) {
    boolean entrystatus = false;
    FileObject sourcefilefolder = null;
    FileObject CurrentFile = null;

    // Get real source file and wilcard
    String realSourceFilefoldername = environmentSubstitute( sourcefilefoldername );
    if ( Utils.isEmpty( realSourceFilefoldername ) ) {
      logError( BaseMessages.getString( PKG, "JobDosToUnix.log.FileFolderEmpty", sourcefilefoldername ) );
      // Update Errors
      updateErrors();

      return entrystatus;
    }
    String realWildcard = environmentSubstitute( wildcard );

    try {
      sourcefilefolder = KettleVFS.getFileObject( realSourceFilefoldername );

      if ( sourcefilefolder.exists() ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.FileExists", sourcefilefolder.toString() ) );
        }
        if ( sourcefilefolder.getType() == FileType.FILE ) {
          entrystatus = convertOneFile( sourcefilefolder, convertion, result, parentJob );

        } else if ( sourcefilefolder.getType() == FileType.FOLDER ) {
          FileObject[] fileObjects = sourcefilefolder.findFiles( new AllFileSelector() {
            public boolean traverseDescendents( FileSelectInfo info ) {
              return info.getDepth() == 0 || include_subfolders;
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
                  } catch ( IOException ex ) { /* Ignore */
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
                  logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.SuccessConditionbroken", ""
                    + nrAllErrors ) );
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
                    convertOneFile( CurrentFile, convertion, result, parentJob );
                  }
                }

              } else {
                // In the base folder
                if ( GetFileWildcard( CurrentFile.toString(), realWildcard ) ) {
                  convertOneFile( CurrentFile, convertion, result, parentJob );
                }
              }
            }
          }
        } else {
          logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.UnknowFileFormat", sourcefilefolder
            .toString() ) );
          // Update Errors
          updateErrors();
        }
      } else {
        logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.SourceFileNotExists", realSourceFilefoldername ) );
        // Update Errors
        updateErrors();
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.Exception.Processing", realSourceFilefoldername
        .toString(), e.getMessage() ) );
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
        } catch ( IOException ex ) { /* Ignore */
        }
      }
    }
    return entrystatus;
  }

  private boolean convertOneFile( FileObject file, int convertion, Result result, Job parentJob ) throws KettleException {
    boolean retval = false;
    try {
      // We deal with a file..

      boolean convertToUnix = true;

      if ( convertion == CONVERTION_TYPE_GUESS ) {
        // Get file Type
        int fileType = getFileType( file );
        if ( fileType == TYPE_DOS_FILE ) {
          // File type is DOS
          // We need to convert it to UNIX
          convertToUnix = true;
        } else {
          // File type is not DOS
          // so let's convert it to DOS
          convertToUnix = false;
        }
      } else if ( convertion == CONVERTION_TYPE_DOS_TO_UNIX ) {
        convertToUnix = true;
      } else {
        convertToUnix = false;
      }

      retval = convert( file, convertToUnix );

      if ( !retval ) {
        logError( BaseMessages.getString( PKG, "JobDosToUnix.Error.FileNotConverted", file.toString() ) );
        // Update Bad files number
        updateBadFormed();
        if ( resultfilenames.equals( ADD_ALL_FILENAMES ) || resultfilenames.equals( ADD_ERROR_FILES_ONLY ) ) {
          addFileToResultFilenames( file, result, parentJob );
        }
      } else {
        if ( isDetailed() ) {
          logDetailed( "---------------------------" );
          logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Error.FileConverted", file, convertToUnix
            ? "UNIX" : "DOS" ) );
        }
        // Update processed files number
        updateProcessedFormed();
        if ( resultfilenames.equals( ADD_ALL_FILENAMES ) || resultfilenames.equals( ADD_PROCESSED_FILES_ONLY ) ) {
          addFileToResultFilenames( file, result, parentJob );
        }
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to convert file '" + file.toString() + "'", e );
    }
    return retval;
  }

  private void updateProcessedFormed() {
    nrProcessedFiles++;
  }

  private void updateBadFormed() {
    nrErrorFiles++;
    updateAllErrors();
  }

  private void addFileToResultFilenames( FileObject fileaddentry, Result result, Job parentJob ) {
    try {
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, fileaddentry, parentJob.getJobname(), toString() );
      result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobDosToUnix.Log.FileAddedToResultFilesName", fileaddentry ) );
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString(
        PKG, "JobDosToUnix.Error.AddingToFilenameResult", fileaddentry.toString(), e.getMessage() ) );
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

  public void setIncludeSubfolders( boolean include_subfoldersin ) {
    this.include_subfolders = include_subfoldersin;
  }

  public void setArgFromPrevious( boolean argfrompreviousin ) {
    this.arg_from_previous = argfrompreviousin;
  }

  public void setNrErrorsLessThan( String nr_errors_less_than ) {
    this.nr_errors_less_than = nr_errors_less_than;
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

  private static class ConversionAutomata {

    private final OutputStream os;
    private final boolean toUnix;
    private byte state;

    ConversionAutomata( OutputStream os, boolean toUnix ) {
      this.os = os;
      this.toUnix = toUnix;
      this.state = 0;
    }

    void convert( byte[] input, int amount ) throws IOException {
      if ( toUnix ) {
        toUnix( input, amount );
      } else {
        toDos( input, amount );
      }
    }

    private void toUnix( byte[] input, int amount ) throws IOException {
      // [0]:
      //     read CR -> goto [1];
      //     read __ -> write __;
      // [1]:
      //     read LF -> write LF;           goto [0];
      //     read CR -> write CR;                    // two CRs in a row -- write the first and hold the second
      //     read __ -> write CR; write __; goto [0];

      int index = 0;
      while ( index < amount ) {
        int b = input[ index++ ];
        switch ( state ) {
          case 0:
            if ( b == CR ) {
              state = 1;
            } else {
              os.write( b );
            }
            break;
          case 1:
            if ( b == LF ) {
              os.write( LF );
              state = 0;
            } else {
              os.write( CR );
              if ( b != CR ) {
                os.write( b );
                state = 0;
              }
            }
            break;
          default:
            throw unknownStateException();
        }
      }
    }

    private void toDos( byte[] input, int amount ) throws IOException {
      // [0]:
      //     read CR -> goto [1];
      //     read LF -> write CR; write LF;
      //     read __ -> write __;
      // [1]:
      //     read LF -> write CR; write LF; goto [0]; // read CR,LF -> write them
      //     read CR -> write CR;
      //     read __ -> write CR; write __; goto [0];

      int index = 0;
      while ( index < amount ) {
        int b = input[ index++ ];
        switch ( state ) {
          case 0:
            if ( b == CR ) {
              state = 1;
            } else if ( b == LF ) {
              os.write( CR );
              os.write( LF );
            } else {
              os.write( b );
            }
            break;
          case 1:
            os.write( CR );
            if ( b != CR ) {
              os.write( b );
              state = 0;
            }
            break;
          default:
            throw unknownStateException();
        }
      }
    }

    private IllegalStateException unknownStateException() {
      return new IllegalStateException( "Unknown state: " + state );
    }
  }
}
