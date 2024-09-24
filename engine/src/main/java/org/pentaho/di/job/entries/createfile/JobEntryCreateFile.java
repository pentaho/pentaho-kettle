/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.createfile;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'create file' job entry. Its main use would be to create empty trigger files that can be used to
 * control the flow in ETL cycles.
 *
 * @author Sven Boden
 * @since 28-01-2007
 *
 */
public class JobEntryCreateFile extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryCreateFile.class; // for i18n purposes, needed by Translator2!!
  private String filename;

  private boolean failIfFileExists;
  private boolean addfilenameresult;

  public JobEntryCreateFile( String n ) {
    super( n, "" );
    filename = null;
    failIfFileExists = true;
    addfilenameresult = false;
  }

  public JobEntryCreateFile() {
    this( "" );
  }

  public Object clone() {
    JobEntryCreateFile je = (JobEntryCreateFile) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fail_if_file_exists", failIfFileExists ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_filename_result", addfilenameresult ) );
    if ( parentJobMeta != null ) {
      parentJobMeta.getNamedClusterEmbedManager().registerUrl( filename );
    }
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      filename = XMLHandler.getTagValue( entrynode, "filename" );
      failIfFileExists = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "fail_if_file_exists" ) );
      addfilenameresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_filename_result" ) );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'create file' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      failIfFileExists = rep.getJobEntryAttributeBoolean( id_jobentry, "fail_if_file_exists" );
      addfilenameresult = rep.getJobEntryAttributeBoolean( id_jobentry, "add_filename_result" );

    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'create file' from the repository for id_jobentry=" + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fail_if_file_exists", failIfFileExists );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_filename_result", addfilenameresult );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'create file' to the repository for id_job="
        + id_job, dbe );
    }
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public String getRealFilename() {
    return environmentSubstitute( getFilename() );
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setResult( false );

    if ( filename != null ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
        parentJobMeta.getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
      }

      String realFilename = getRealFilename();
      FileObject fileObject = null;
      try {
        fileObject = KettleVFS.getInstance( getParentJobMeta().getBowl() ).getFileObject( realFilename, this );

        if ( fileObject.exists() ) {
          if ( isFailIfFileExists() ) {
            // File exists and fail flag is on.
            result.setResult( false );
            logError( "File [" + realFilename + "] exists, failing." );
          } else {
            // File already exists, no reason to try to create it
            result.setResult( true );
            logBasic( "File [" + realFilename + "] already exists, not recreating." );
          }
          // add filename to result filenames if needed
          if ( isAddFilenameToResult() ) {
            addFilenameToResult( realFilename, result, parentJob );
          }
        } else {
          // No file yet, create an empty file.
          fileObject.createFile();
          logBasic( "File [" + realFilename + "] created!" );
          // add filename to result filenames if needed
          if ( isAddFilenameToResult() ) {
            addFilenameToResult( realFilename, result, parentJob );
          }
          result.setResult( true );
        }
      } catch ( IOException e ) {
        logError( "Could not create file [" + realFilename + "], exception: " + e.getMessage() );
        result.setResult( false );
        result.setNrErrors( 1 );
      } finally {
        if ( fileObject != null ) {
          try {
            fileObject.close();
            fileObject = null;
          } catch ( IOException ex ) {
            // Ignore
          }
        }
      }
    } else {
      logError( "No filename is defined." );
    }

    return result;
  }

  private void addFilenameToResult( String targetFilename, Result result, Job parentJob ) throws KettleException {
    FileObject targetFile = null;
    try {
      targetFile = KettleVFS.getInstance( getParentJobMeta().getBowl() ).getFileObject( targetFilename, this );

      // Add to the result files...
      ResultFile resultFile =
        new ResultFile( ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString() );
      resultFile.setComment( "" );
      result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryCreateFile.FileAddedToResult", targetFilename ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      try {
        if ( targetFile != null ) {
          targetFile.close();
        }
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isFailIfFileExists() {
    return failIfFileExists;
  }

  public void setFailIfFileExists( boolean failIfFileExists ) {
    this.failIfFileExists = failIfFileExists;
  }

  public boolean isAddFilenameToResult() {
    return addfilenameresult;
  }

  public void setAddFilenameToResult( boolean addfilenameresult ) {
    this.addfilenameresult = addfilenameresult;
  }

  public static void main( String[] args ) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryCreateFile().check( remarks, null, new Variables(), null, null );
    System.out.printf( "Remarks: %s\n", remarks );
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileDoesNotExistValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );
  }

}
