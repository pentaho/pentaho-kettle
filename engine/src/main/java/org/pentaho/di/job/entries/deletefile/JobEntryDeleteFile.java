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

package org.pentaho.di.job.entries.deletefile;

import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.FileExistsValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'delete file' job entry. Its main use would be to delete trigger files, but it will delete any file.
 *
 * @author Sven Boden
 * @since 10-02-2007
 *
 */
public class JobEntryDeleteFile extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryDeleteFile.class; // for i18n purposes, needed by Translator2!!

  private String filename;
  private boolean failIfFileNotExists;

  public JobEntryDeleteFile( String n ) {
    super( n, "" );
    filename = null;
    failIfFileNotExists = false;
  }

  public JobEntryDeleteFile() {
    this( "" );
  }

  public Object clone() {
    JobEntryDeleteFile je = (JobEntryDeleteFile) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fail_if_file_not_exists", failIfFileNotExists ) );
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
      failIfFileNotExists = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "fail_if_file_not_exists" ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "JobEntryDeleteFile.Error_0001_Unable_To_Load_Job_From_Xml_Node" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      failIfFileNotExists = rep.getJobEntryAttributeBoolean( id_jobentry, "fail_if_file_not_exists" );
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryDeleteFile.ERROR_0002_Unable_To_Load_From_Repository", id_jobentry ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fail_if_file_not_exists", failIfFileNotExists );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntryDeleteFile.ERROR_0003_Unable_To_Save_Job_To_Repository", id_job ), dbe );
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

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    if ( filename != null ) {
      String realFilename = getRealFilename();

      FileObject fileObject = null;
      try {
        fileObject = KettleVFS.getInstance( getParentJobMeta().getBowl() ).getFileObject( realFilename, this );

        if ( !fileObject.exists() ) {
          if ( isFailIfFileNotExists() ) {
            // File doesn't exist and fail flag is on.
            result.setResult( false );
            logError( BaseMessages.getString(
              PKG, "JobEntryDeleteFile.ERROR_0004_File_Does_Not_Exist", realFilename ) );
          } else {
            // File already deleted, no reason to try to delete it
            result.setResult( true );
            if ( log.isBasic() ) {
              logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFile.File_Already_Deleted", realFilename ) );
            }
          }
        } else {
          boolean deleted = fileObject.delete();
          if ( !deleted ) {
            logError( BaseMessages.getString(
              PKG, "JobEntryDeleteFile.ERROR_0005_Could_Not_Delete_File", realFilename ) );
            result.setResult( false );
            result.setNrErrors( 1 );
          }
          else {
            if ( log.isBasic() ) {
              logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFile.File_Deleted", realFilename ) );
            }
          }
          result.setResult( true );
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString(
          PKG, "JobEntryDeleteFile.ERROR_0006_Exception_Deleting_File", realFilename, e.getMessage() ), e );
        result.setResult( false );
        result.setNrErrors( 1 );
      } finally {
        if ( fileObject != null ) {
          try {
            fileObject.close();
          } catch ( IOException ex ) { /* Ignore */
          }
        }
      }
    } else {
      logError( BaseMessages.getString( PKG, "JobEntryDeleteFile.ERROR_0007_No_Filename_Is_Defined" ) );
    }

    return result;
  }

  public boolean isFailIfFileNotExists() {
    return failIfFileNotExists;
  }

  public void setFailIfFileNotExists( boolean failIfFileExists ) {
    this.failIfFileNotExists = failIfFileExists;
  }

  public boolean evaluates() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( filename ) ) {
      String realFileName = jobMeta.environmentSubstitute( filename );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realFileName, ResourceType.FILE ) );
      references.add( reference );
    }
    return references;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileExistsValidator() );
    if ( isFailIfFileNotExists() ) {
      FileExistsValidator.putFailIfDoesNotExist( ctx, true );
    }
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks, ctx );
  }

  public static void main( String[] args ) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryDeleteFile().check( remarks, null, new Variables(), null, null );
    System.out.printf( "Remarks: %s\n", remarks );
  }

}
