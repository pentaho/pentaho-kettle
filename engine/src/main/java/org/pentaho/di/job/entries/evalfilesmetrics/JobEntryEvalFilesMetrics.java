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

package org.pentaho.di.job.entries.evalfilesmetrics;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
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
import org.pentaho.di.job.entries.simpleeval.JobEntrySimpleEval;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'evaluate files metrics' job entry.
 *
 * @author Samatar Hassan
 * @since 26-02-2010
 */

public class JobEntryEvalFilesMetrics extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryEvalFilesMetrics.class; // for i18n purposes, needed by Translator2!!

  public static final BigDecimal ONE = new BigDecimal( 1 );

  public static final String[] IncludeSubFoldersDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] IncludeSubFoldersCodes = new String[] { "N", "Y" };
  private static final String YES = "Y";
  private static final String NO = "N";

  public static final String[] scaleDesc = new String[] {
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.Bytes.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.KBytes.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.MBytes.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.GBytes.Label" ) };
  public static final String[] scaleCodes = new String[] { "bytes", "kbytes", "mbytes", "gbytes" };
  public static final int SCALE_BYTES = 0;
  public static final int SCALE_KBYTES = 1;
  public static final int SCALE_MBYTES = 2;
  public static final int SCALE_GBYTES = 3;

  public int scale;

  public static final String[] SourceFilesDesc = new String[] {
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.SourceFiles.Files.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.SourceFiles.FilenamesResult.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.SourceFiles.PreviousResult.Label" ), };
  public static final String[] SourceFilesCodes = new String[] { "files", "filenamesresult", "previousresult" };
  public static final int SOURCE_FILES_FILES = 0;
  public static final int SOURCE_FILES_FILENAMES_RESULT = 1;
  public static final int SOURCE_FILES_PREVIOUS_RESULT = 2;

  public int sourceFiles;

  public static final String[] EvaluationTypeDesc = new String[] {
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.EvaluationType.Size.Label" ),
    BaseMessages.getString( PKG, "JobEvalFilesMetrics.EvaluationType.Count.Label" ), };
  public static final String[] EvaluationTypeCodes = new String[] { "size", "count", };
  public static final int EVALUATE_TYPE_SIZE = 0;
  public static final int EVALUATE_TYPE_COUNT = 1;

  public int evaluationType;

  private String comparevalue;
  private String minvalue;
  private String maxvalue;
  private int successConditionType;

  private String resultFilenamesWildcard;

  public boolean arg_from_previous;

  private String[] sourceFileFolder;
  private String[] sourceWildcard;
  private String[] sourceIncludeSubfolders;

  private BigDecimal evaluationValue;
  private BigDecimal filesCount;
  private long nrErrors;

  private String ResultFieldFile;
  private String ResultFieldWildcard;
  private String ResultFieldIncludesubFolders;

  private BigDecimal compareValue;
  private BigDecimal minValue;
  private BigDecimal maxValue;

  public JobEntryEvalFilesMetrics( String n ) {
    super( n, "" );
    sourceFileFolder = null;
    sourceWildcard = null;
    sourceIncludeSubfolders = null;
    scale = SCALE_BYTES;
    sourceFiles = SOURCE_FILES_FILES;
    evaluationType = EVALUATE_TYPE_SIZE;
    successConditionType = JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_GREATER;
    resultFilenamesWildcard = null;
    ResultFieldFile = null;
    ResultFieldWildcard = null;
    ResultFieldIncludesubFolders = null;
  }

  public JobEntryEvalFilesMetrics() {
    this( "" );
  }

  public void allocate( int nrFields ) {
    sourceFileFolder = new String[nrFields];
    sourceWildcard = new String[nrFields];
    sourceIncludeSubfolders = new String[nrFields];
  }

  public Object clone() {
    JobEntryEvalFilesMetrics je = (JobEntryEvalFilesMetrics) super.clone();
    if ( sourceFileFolder != null ) {
      int nrFields = sourceFileFolder.length;
      je.allocate( nrFields );
      System.arraycopy( sourceFileFolder, 0, je.sourceFileFolder, 0, nrFields );
      System.arraycopy( sourceWildcard, 0, je.sourceWildcard, 0, nrFields );
      System.arraycopy( sourceIncludeSubfolders, 0, je.sourceIncludeSubfolders, 0, nrFields );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "result_filenames_wildcard", resultFilenamesWildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "Result_field_file", ResultFieldFile ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "Result_field_wildcard", ResultFieldWildcard ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "Result_field_includesubfolders", ResultFieldIncludesubFolders ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( sourceFileFolder != null ) {
      for ( int i = 0; i < sourceFileFolder.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "source_filefolder", sourceFileFolder[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "wildcard", sourceWildcard[i] ) );
        retval
          .append( "          " ).append( XMLHandler.addTagValue( "include_subFolders", sourceIncludeSubfolders[i] ) );
        retval.append( "        </field>" ).append( Const.CR );
        if ( parentJobMeta != null ) {
          parentJobMeta.getNamedClusterEmbedManager().registerUrl( sourceFileFolder[i] );
        }
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "comparevalue", comparevalue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "minvalue", minvalue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maxvalue", maxvalue ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "successnumbercondition", JobEntrySimpleEval
        .getSuccessNumberConditionCode( successConditionType ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "source_files", getSourceFilesCode( sourceFiles ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "evaluation_type", getEvaluationTypeCode( evaluationType ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "scale", getScaleCode( scale ) ) );
    return retval.toString();
  }

  public static String getIncludeSubFolders( String tt ) {
    if ( tt == null ) {
      return IncludeSubFoldersCodes[0];
    }
    if ( tt.equals( IncludeSubFoldersDesc[1] ) ) {
      return IncludeSubFoldersCodes[1];
    } else {
      return IncludeSubFoldersCodes[0];
    }
  }

  public static String getIncludeSubFoldersDesc( String tt ) {
    if ( tt == null ) {
      return IncludeSubFoldersDesc[0];
    }
    if ( tt.equals( IncludeSubFoldersCodes[1] ) ) {
      return IncludeSubFoldersDesc[1];
    } else {
      return IncludeSubFoldersDesc[0];
    }
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        sourceFileFolder[i] = XMLHandler.getTagValue( fnode, "source_filefolder" );
        sourceWildcard[i] = XMLHandler.getTagValue( fnode, "wildcard" );
        sourceIncludeSubfolders[i] = XMLHandler.getTagValue( fnode, "include_subFolders" );
      }

      resultFilenamesWildcard = XMLHandler.getTagValue( entrynode, "result_filenames_wildcard" );
      ResultFieldFile = XMLHandler.getTagValue( entrynode, "result_field_file" );
      ResultFieldWildcard = XMLHandler.getTagValue( entrynode, "result_field_wildcard" );
      ResultFieldIncludesubFolders = XMLHandler.getTagValue( entrynode, "result_field_includesubfolders" );
      comparevalue = XMLHandler.getTagValue( entrynode, "comparevalue" );
      minvalue = XMLHandler.getTagValue( entrynode, "minvalue" );
      maxvalue = XMLHandler.getTagValue( entrynode, "maxvalue" );
      successConditionType =
        JobEntrySimpleEval.getSuccessNumberConditionByCode( Const.NVL( XMLHandler.getTagValue(
          entrynode, "successnumbercondition" ), "" ) );
      sourceFiles = getSourceFilesByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "source_files" ), "" ) );
      evaluationType =
        getEvaluationTypeByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "evaluation_type" ), "" ) );
      scale = getScaleByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "scale" ), "" ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEvalFilesMetrics.Error.Exception.UnableLoadXML" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "source_filefolder" );
      allocate( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        sourceFileFolder[a] = rep.getJobEntryAttributeString( id_jobentry, a, "source_filefolder" );
        sourceWildcard[a] = rep.getJobEntryAttributeString( id_jobentry, a, "wildcard" );
        sourceIncludeSubfolders[a] = rep.getJobEntryAttributeString( id_jobentry, a, "include_subFolders" );
      }

      resultFilenamesWildcard = rep.getJobEntryAttributeString( id_jobentry, "result_filenames_wildcard" );
      ResultFieldFile = rep.getJobEntryAttributeString( id_jobentry, "result_field_file" );
      ResultFieldWildcard = rep.getJobEntryAttributeString( id_jobentry, "result_field_wild" );
      ResultFieldIncludesubFolders =
        rep.getJobEntryAttributeString( id_jobentry, "result_field_includesubfolders" );
      comparevalue = rep.getJobEntryAttributeString( id_jobentry, "comparevalue" );
      minvalue = rep.getJobEntryAttributeString( id_jobentry, "minvalue" );
      maxvalue = rep.getJobEntryAttributeString( id_jobentry, "maxvalue" );
      successConditionType =
        JobEntrySimpleEval.getSuccessNumberConditionByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "successnumbercondition" ), "" ) );
      sourceFiles =
        getSourceFilesByCode( Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "source_files" ), "" ) );
      evaluationType =
        getEvaluationTypeByCode( Const
          .NVL( rep.getJobEntryAttributeString( id_jobentry, "evaluation_type" ), "" ) );
      scale = getScaleByCode( Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "scale" ), "" ) );
    } catch ( KettleException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.Exception.UnableLoadRep" )
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {

      // save the arguments...
      if ( sourceFileFolder != null ) {
        for ( int i = 0; i < sourceFileFolder.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "source_filefolder", sourceFileFolder[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "wildcard", sourceWildcard[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "include_subFolders", sourceIncludeSubfolders[i] );
        }
      }

      rep.saveJobEntryAttribute( id_job, getObjectId(), "result_filenames_wildcard", resultFilenamesWildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "result_field_file", ResultFieldFile );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "result_field_wild", ResultFieldWildcard );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "result_field_includesubfolders", ResultFieldIncludesubFolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "comparevalue", comparevalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "minvalue", minvalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maxvalue", maxvalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "successnumbercondition", JobEntrySimpleEval
        .getSuccessNumberConditionCode( successConditionType ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "scale", getScaleCode( scale ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "source_files", getSourceFilesCode( sourceFiles ) );
      rep
        .saveJobEntryAttribute( id_job, getObjectId(), "evaluation_type", getEvaluationTypeCode( evaluationType ) );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.Exception.UnableSaveRep" )
        + id_job, dbe );
    }
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    try {
      initMetrics();
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.Init", e.toString() ) );
      return result;
    }

    // Get source and destination files, also wildcard
    String[] vsourcefilefolder = sourceFileFolder;
    String[] vwildcard = sourceWildcard;
    String[] vincludeSubFolders = sourceIncludeSubfolders;

    switch ( getSourceFiles() ) {
      case SOURCE_FILES_PREVIOUS_RESULT:
        // Filenames are retrieved from previous result rows

        String realResultFieldFile = environmentSubstitute( getResultFieldFile() );
        String realResultFieldWildcard = environmentSubstitute( getResultFieldWildcard() );
        String realResultFieldIncluseSubfolders = environmentSubstitute( getResultFieldIncludeSubfolders() );

        int indexOfResultFieldFile = -1;
        if ( Utils.isEmpty( realResultFieldFile ) ) {
          logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.ResultFieldsFileMissing" ) );
          return result;
        }

        int indexOfResultFieldWildcard = -1;
        int indexOfResultFieldIncludeSubfolders = -1;

        // as such we must get rows
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEvalFilesMetrics.Log.ArgFromPrevious.Found", ( rows != null ? rows.size() : 0 ) + "" ) );
        }

        if ( rows != null && rows.size() > 0 ) {
          // We get rows
          RowMetaAndData firstRow = rows.get( 0 );
          indexOfResultFieldFile = firstRow.getRowMeta().indexOfValue( realResultFieldFile );
          if ( indexOfResultFieldFile == -1 ) {
            logError( BaseMessages.getString(
              PKG, "JobEvalFilesMetrics.Error.CanNotFindField", realResultFieldFile ) );
            return result;
          }
          if ( !Utils.isEmpty( realResultFieldWildcard ) ) {
            indexOfResultFieldWildcard = firstRow.getRowMeta().indexOfValue( realResultFieldWildcard );
            if ( indexOfResultFieldWildcard == -1 ) {
              logError( BaseMessages.getString(
                PKG, "JobEvalFilesMetrics.Error.CanNotFindField", realResultFieldWildcard ) );
              return result;
            }
          }
          if ( !Utils.isEmpty( realResultFieldIncluseSubfolders ) ) {
            indexOfResultFieldIncludeSubfolders =
              firstRow.getRowMeta().indexOfValue( realResultFieldIncluseSubfolders );
            if ( indexOfResultFieldIncludeSubfolders == -1 ) {
              logError( BaseMessages.getString(
                PKG, "JobEvalFilesMetrics.Error.CanNotFindField", realResultFieldIncluseSubfolders ) );
              return result;
            }
          }

          for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {

            resultRow = rows.get( iteration );

            // Get source and destination file names, also wildcard
            String vsourcefilefolder_previous = resultRow.getString( indexOfResultFieldFile, null );
            String vwildcard_previous = null;
            if ( indexOfResultFieldWildcard > -1 ) {
              vwildcard_previous = resultRow.getString( indexOfResultFieldWildcard, null );
            }
            String vincludeSubFolders_previous = NO;
            if ( indexOfResultFieldIncludeSubfolders > -1 ) {
              vincludeSubFolders_previous = resultRow.getString( indexOfResultFieldIncludeSubfolders, NO );
            }

            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString(
                PKG, "JobEvalFilesMetrics.Log.ProcessingRow", vsourcefilefolder_previous, vwildcard_previous ) );
            }

            ProcessFileFolder(
              vsourcefilefolder_previous, vwildcard_previous, vincludeSubFolders_previous, parentJob, result );
          }
        }

        break;
      case SOURCE_FILES_FILENAMES_RESULT:
        List<ResultFile> resultFiles = result.getResultFilesList();
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEvalFilesMetrics.Log.ResultFilenames.Found",
            ( resultFiles != null ? resultFiles.size() : 0 ) + "" ) );
        }

        if ( resultFiles != null && resultFiles.size() > 0 ) {
          // Let's check wildcard
          Pattern pattern = null;
          String realPattern = environmentSubstitute( getResultFilenamesWildcard() );
          if ( !Utils.isEmpty( realPattern ) ) {
            pattern = Pattern.compile( realPattern );
          }

          for ( Iterator<ResultFile> it = resultFiles.iterator(); it.hasNext() && !parentJob.isStopped(); ) {
            ResultFile resultFile = it.next();
            FileObject file = resultFile.getFile();
            try {
              if ( file != null && file.exists() ) {
                boolean getIt = true;
                if ( pattern != null ) {
                  Matcher matcher = pattern.matcher( file.getName().getBaseName() );
                  getIt = matcher.matches();
                }
                if ( getIt ) {
                  getFileSize( file, result, parentJob );
                }
              }
            } catch ( Exception e ) {
              incrementErrors();
              logError( BaseMessages.getString(
                PKG, "JobEvalFilesMetrics.Error.GettingFileFromResultFilenames", file.toString(), e.toString() ) );
            } finally {
              if ( file != null ) {
                try {
                  file.close();
                } catch ( Exception e ) { /* Ignore */
                }
              }
            }
          }
        }
        break;
      default:
        // static files/folders
        // from grid entered by user
        if ( vsourcefilefolder != null && vsourcefilefolder.length > 0 ) {
          for ( int i = 0; i < vsourcefilefolder.length && !parentJob.isStopped(); i++ ) {

            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString(
                PKG, "JobEvalFilesMetrics.Log.ProcessingRow", vsourcefilefolder[i], vwildcard[i] ) );
            }

            ProcessFileFolder( vsourcefilefolder[i], vwildcard[i], vincludeSubFolders[i], parentJob, result );
          }
        } else {
          logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.FilesGridEmpty" ) );
          return result;
        }
        break;
    }

    result.setResult( isSuccess() );
    result.setNrErrors( getNrError() );
    displayResults();

    return result;
  }

  private void displayResults() {
    if ( isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.Info.FilesCount", String
        .valueOf( getFilesCount() ) ) );
      if ( evaluationType == EVALUATE_TYPE_SIZE ) {
        logDetailed( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.Info.FilesSize", String
          .valueOf( getEvaluationValue() ) ) );
      }
      logDetailed( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.Info.NrErrors", String
        .valueOf( getNrError() ) ) );
      logDetailed( "=======================================" );
    }
  }

  private long getNrError() {
    return this.nrErrors;
  }

  private BigDecimal getEvaluationValue() {
    return this.evaluationValue;
  }

  private BigDecimal getFilesCount() {
    return this.filesCount;
  }

  public int getSuccessConditionType() {
    return successConditionType;
  }

  public void setSuccessConditionType( int successConditionType ) {
    this.successConditionType = successConditionType;
  }

  private boolean isSuccess() {
    boolean retval = false;

    switch ( successConditionType ) {
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_EQUAL: // equal
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) == 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_DIFFERENT: // different
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) != 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_SMALLER: // smaller
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) < 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_SMALLER_EQUAL: // smaller or equal
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) <= 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_GREATER: // greater
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) > 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_GREATER_EQUAL: // greater or equal
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValue", String
            .valueOf( evaluationValue ), String.valueOf( compareValue ) ) );
        }
        retval = ( getEvaluationValue().compareTo( compareValue ) >= 0 );
        break;
      case JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_BETWEEN: // between min and max
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.CompareWithValues", String
            .valueOf( evaluationValue ), String.valueOf( minValue ), String.valueOf( maxValue ) ) );
        }
        retval =
          ( getEvaluationValue().compareTo( minValue ) >= 0 && getEvaluationValue().compareTo( maxValue ) <= 0 );
        break;
      default:
        break;
    }

    return retval;
  }

  private void initMetrics() throws Exception {
    evaluationValue = new BigDecimal( 0 );
    filesCount = new BigDecimal( 0 );
    nrErrors = 0;

    if ( successConditionType == JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_BETWEEN ) {
      minValue = new BigDecimal( environmentSubstitute( getMinValue() ) );
      maxValue = new BigDecimal( environmentSubstitute( getMaxValue() ) );
    } else {
      compareValue = new BigDecimal( environmentSubstitute( getCompareValue() ) );
    }

    if ( evaluationType == EVALUATE_TYPE_SIZE ) {
      int multyply = 1;
      switch ( getScale() ) {
        case SCALE_KBYTES:
          multyply = 1024;
          break;
        case SCALE_MBYTES:
          multyply = 1048576;
          break;
        case SCALE_GBYTES:
          multyply = 1073741824;
          break;
        default:
          break;
      }

      if ( successConditionType == JobEntrySimpleEval.SUCCESS_NUMBER_CONDITION_BETWEEN ) {
        minValue = minValue.multiply( BigDecimal.valueOf( multyply ) );
        maxValue = maxValue.multiply( BigDecimal.valueOf( multyply ) );
      } else {
        compareValue = compareValue.multiply( BigDecimal.valueOf( multyply ) );
      }
    }
    arg_from_previous = ( getSourceFiles() == SOURCE_FILES_PREVIOUS_RESULT );
  }

  private void incrementErrors() {
    nrErrors++;
  }

  public int getSourceFiles() {
    return this.sourceFiles;
  }

  private void incrementFilesCount() {
    filesCount = filesCount.add( ONE );
  }

  public String[] getSourceFileFolder() {
    return sourceFileFolder;
  }

  public void setSourceFileFolder( String[] sourceFileFolder ) {
    this.sourceFileFolder = sourceFileFolder;
  }

  public String[] getSourceWildcard() {
    return sourceWildcard;
  }

  public void setSourceWildcard( String[] sourceWildcard ) {
    this.sourceWildcard = sourceWildcard;
  }

  public String[] getSourceIncludeSubfolders() {
    return sourceIncludeSubfolders;
  }

  public void setSourceIncludeSubfolders( String[] sourceIncludeSubfolders ) {
    this.sourceIncludeSubfolders = sourceIncludeSubfolders;
  }

  public void setSourceFiles( int sourceFiles ) {
    this.sourceFiles = sourceFiles;
  }

  public String getResultFieldFile() {
    return this.ResultFieldFile;
  }

  public void setResultFieldFile( String field ) {
    this.ResultFieldFile = field;
  }

  public String getResultFieldWildcard() {
    return this.ResultFieldWildcard;
  }

  public void setResultFieldWildcard( String field ) {
    this.ResultFieldWildcard = field;
  }

  public String getResultFieldIncludeSubfolders() {
    return this.ResultFieldIncludesubFolders;
  }

  public void setResultFieldIncludeSubfolders( String field ) {
    this.ResultFieldIncludesubFolders = field;
  }

  private void ProcessFileFolder( String sourcefilefoldername, String wildcard, String includeSubfolders,
    Job parentJob, Result result ) {

    FileObject sourcefilefolder = null;
    FileObject CurrentFile = null;

    // Get real source file and wildcard
    String realSourceFilefoldername = environmentSubstitute( sourcefilefoldername );
    if ( Utils.isEmpty( realSourceFilefoldername ) ) {
      // Filename is empty!
      logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.log.FileFolderEmpty" ) );
      incrementErrors();
      return;
    }
    String realWildcard = environmentSubstitute( wildcard );
    final boolean include_subfolders = YES.equalsIgnoreCase( includeSubfolders );

    try {
      sourcefilefolder = KettleVFS.getFileObject( realSourceFilefoldername, this );

      if ( sourcefilefolder.exists() ) {
        // File exists
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.FileExists", sourcefilefolder
            .toString() ) );
        }

        if ( sourcefilefolder.getType() == FileType.FILE ) {
          // We deals here with a file
          // let's get file size
          getFileSize( sourcefilefolder, result, parentJob );

        } else if ( sourcefilefolder.getType() == FileType.FOLDER ) {
          // We have a folder
          // we will fetch and extract files
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
              // Fetch files in list one after one ...
              CurrentFile = fileObjects[j];

              if ( !CurrentFile.getParent().toString().equals( sourcefilefolder.toString() ) ) {
                // Not in the Base Folder..Only if include sub folders
                if ( include_subfolders ) {
                  if ( GetFileWildcard( CurrentFile.getName().getBaseName(), realWildcard ) ) {
                    getFileSize( CurrentFile, result, parentJob );
                  }
                }
              } else {
                // In the base folder
                if ( GetFileWildcard( CurrentFile.getName().getBaseName(), realWildcard ) ) {
                  getFileSize( CurrentFile, result, parentJob );
                }
              }
            }
          }
        } else {
          incrementErrors();
          logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.UnknowFileFormat", sourcefilefolder
            .toString() ) );
        }
      } else {
        incrementErrors();
        logError( BaseMessages.getString(
          PKG, "JobEvalFilesMetrics.Error.SourceFileNotExists", realSourceFilefoldername ) );
      }
    } catch ( Exception e ) {
      incrementErrors();
      logError( BaseMessages.getString(
        PKG, "JobEvalFilesMetrics.Error.Exception.Processing", realSourceFilefoldername.toString(), e
          .getMessage() ) );

    } finally {
      if ( sourcefilefolder != null ) {
        try {
          sourcefilefolder.close();
        } catch ( IOException ex ) { /* Ignore */
        }

      }
      if ( CurrentFile != null ) {
        try {
          CurrentFile.close();
        } catch ( IOException ex ) { /* Ignore */
        }
      }
    }
  }

  private void getFileSize( FileObject file, Result result, Job parentJob ) {
    try {

      incrementFilesCount();
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.GetFile", file.toString(), String
          .valueOf( getFilesCount() ) ) );
      }
      switch ( evaluationType ) {
        case EVALUATE_TYPE_SIZE:
          BigDecimal fileSize = BigDecimal.valueOf( file.getContent().getSize() );
          evaluationValue = evaluationValue.add( fileSize );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Log.AddedFileSize", String
              .valueOf( fileSize ), file.toString() ) );
          }
          break;
        default:
          evaluationValue = evaluationValue.add( ONE );
          break;
      }
    } catch ( Exception e ) {
      incrementErrors();
      logError( BaseMessages.getString( PKG, "JobEvalFilesMetrics.Error.GettingFileSize", file.toString(), e
        .toString() ) );
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

  public void setMinValue( String minvalue ) {
    this.minvalue = minvalue;
  }

  public String getMinValue() {
    return minvalue;
  }

  public void setCompareValue( String comparevalue ) {
    this.comparevalue = comparevalue;
  }

  public String getCompareValue() {
    return comparevalue;
  }

  public void setResultFilenamesWildcard( String resultwildcard ) {
    this.resultFilenamesWildcard = resultwildcard;
  }

  public String getResultFilenamesWildcard() {
    return this.resultFilenamesWildcard;
  }

  public void setMaxValue( String maxvalue ) {
    this.maxvalue = maxvalue;
  }

  public String getMaxValue() {
    return maxvalue;
  }

  public static int getScaleByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < scaleDesc.length; i++ ) {
      if ( scaleDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getScaleByCode( tt );
  }

  public static int getSourceFilesByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < SourceFilesDesc.length; i++ ) {
      if ( SourceFilesDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getSourceFilesByCode( tt );
  }

  public static int getEvaluationTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < EvaluationTypeDesc.length; i++ ) {
      if ( EvaluationTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getEvaluationTypeByCode( tt );
  }

  private static int getScaleByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < scaleCodes.length; i++ ) {
      if ( scaleCodes[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getSourceFilesByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < SourceFilesCodes.length; i++ ) {
      if ( SourceFilesCodes[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getEvaluationTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < EvaluationTypeCodes.length; i++ ) {
      if ( EvaluationTypeCodes[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getScaleDesc( int i ) {
    if ( i < 0 || i >= scaleDesc.length ) {
      return scaleDesc[0];
    }
    return scaleDesc[i];
  }

  public static String getEvaluationTypeDesc( int i ) {
    if ( i < 0 || i >= EvaluationTypeDesc.length ) {
      return EvaluationTypeDesc[0];
    }
    return EvaluationTypeDesc[i];
  }

  public static String getSourceFilesDesc( int i ) {
    if ( i < 0 || i >= SourceFilesDesc.length ) {
      return SourceFilesDesc[0];
    }
    return SourceFilesDesc[i];
  }

  public static String getScaleCode( int i ) {
    if ( i < 0 || i >= scaleCodes.length ) {
      return scaleCodes[0];
    }
    return scaleCodes[i];
  }

  public static String getSourceFilesCode( int i ) {
    if ( i < 0 || i >= SourceFilesCodes.length ) {
      return SourceFilesCodes[0];
    }
    return SourceFilesCodes[i];
  }

  public static String getEvaluationTypeCode( int i ) {
    if ( i < 0 || i >= EvaluationTypeCodes.length ) {
      return EvaluationTypeCodes[0];
    }
    return EvaluationTypeCodes[i];
  }

  public int getScale() {
    return this.scale;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = JobEntryValidatorUtils.andValidator().validate( this, "arguments", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    if ( res == false ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );

    for ( int i = 0; i < sourceFileFolder.length; i++ ) {
      JobEntryValidatorUtils.andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

  public boolean evaluates() {
    return true;
  }

}
