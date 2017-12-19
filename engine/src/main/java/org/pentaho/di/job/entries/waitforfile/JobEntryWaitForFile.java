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

package org.pentaho.di.job.entries.waitforfile;

import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

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
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'wait for file' job entry. Its use is to wait for a file to appear.
 *
 * @author Sven Boden
 * @since 10-02-2007
 *
 */
public class JobEntryWaitForFile extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryWaitForFile.class; // for i18n purposes, needed by Translator2!!

  private String filename;
  private String maximumTimeout; // maximum timeout in seconds
  private String checkCycleTime; // cycle time in seconds
  private boolean successOnTimeout;
  private boolean fileSizeCheck;
  private boolean addFilenameToResult;

  private static String DEFAULT_MAXIMUM_TIMEOUT = "0"; // infinite timeout
  private static String DEFAULT_CHECK_CYCLE_TIME = "60"; // 1 minute

  public JobEntryWaitForFile( String n ) {
    super( n, "" );
    filename = null;
    maximumTimeout = DEFAULT_MAXIMUM_TIMEOUT;
    checkCycleTime = DEFAULT_CHECK_CYCLE_TIME;
    successOnTimeout = false;
    fileSizeCheck = false;
    addFilenameToResult = false;
  }

  public JobEntryWaitForFile() {
    this( "" );
  }

  public Object clone() {
    JobEntryWaitForFile je = (JobEntryWaitForFile) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maximum_timeout", maximumTimeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "check_cycle_time", checkCycleTime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_on_timeout", successOnTimeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "file_size_check", fileSizeCheck ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_filename_result", addFilenameToResult ) );
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
      maximumTimeout = XMLHandler.getTagValue( entrynode, "maximum_timeout" );
      checkCycleTime = XMLHandler.getTagValue( entrynode, "check_cycle_time" );
      successOnTimeout = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "success_on_timeout" ) );
      fileSizeCheck = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "file_size_check" ) );
      addFilenameToResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_filename_result" ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'wait for file' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      filename = rep.getJobEntryAttributeString( id_jobentry, "filename" );
      maximumTimeout = rep.getJobEntryAttributeString( id_jobentry, "maximum_timeout" );
      checkCycleTime = rep.getJobEntryAttributeString( id_jobentry, "check_cycle_time" );
      successOnTimeout = rep.getJobEntryAttributeBoolean( id_jobentry, "success_on_timeout" );
      fileSizeCheck = rep.getJobEntryAttributeBoolean( id_jobentry, "file_size_check" );
      addFilenameToResult = rep.getJobEntryAttributeBoolean( id_jobentry, "add_filename_result" );
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'wait for file' from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filename", filename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maximum_timeout", maximumTimeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "check_cycle_time", checkCycleTime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_on_timeout", successOnTimeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "file_size_check", fileSizeCheck );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_filename_result", addFilenameToResult );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'wait for file' to the repository for id_job="
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

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    // starttime (in seconds)
    long timeStart = System.currentTimeMillis() / 1000;

    if ( filename != null ) {
      FileObject fileObject = null;
      String realFilename = getRealFilename();

      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
        parentJobMeta.getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
      }

      try {
        fileObject = KettleVFS.getFileObject( realFilename, this );

        long iMaximumTimeout = Const.toInt( getRealMaximumTimeout(), Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 ) );
        long iCycleTime = Const.toInt( getRealCheckCycleTime(), Const.toInt( DEFAULT_CHECK_CYCLE_TIME, 0 ) );

        //
        // Sanity check on some values, and complain on insanity
        //
        if ( iMaximumTimeout < 0 ) {
          iMaximumTimeout = Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 );
          if ( log.isBasic() ) {
            logBasic( "Maximum timeout invalid, reset to " + iMaximumTimeout );
          }
        }

        if ( iCycleTime < 1 ) {
          // If lower than 1 set to the default
          iCycleTime = Const.toInt( DEFAULT_CHECK_CYCLE_TIME, 1 );
          if ( log.isBasic() ) {
            logBasic( "Check cycle time invalid, reset to " + iCycleTime );
          }
        }

        if ( iMaximumTimeout == 0 ) {
          if ( log.isBasic() ) {
            logBasic( "Waiting indefinitely for file [" + realFilename + "]" );
          }
        } else {
          if ( log.isBasic() ) {
            logBasic( "Waiting " + iMaximumTimeout + " seconds for file [" + realFilename + "]" );
          }
        }

        boolean continueLoop = true;
        while ( continueLoop && !parentJob.isStopped() ) {
          fileObject = KettleVFS.getFileObject( realFilename, this );

          if ( fileObject.exists() ) {
            // file exists, we're happy to exit
            if ( log.isBasic() ) {
              logBasic( "Detected file [" + realFilename + "] within timeout" );
            }
            result.setResult( true );
            continueLoop = false;

            // add filename to result filenames
            if ( addFilenameToResult && fileObject.getType() == FileType.FILE ) {
              ResultFile resultFile =
                new ResultFile( ResultFile.FILE_TYPE_GENERAL, fileObject, parentJob.getJobname(), toString() );
              resultFile.setComment( BaseMessages.getString( PKG, "JobWaitForFile.FilenameAdded" ) );
              result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
            }
          } else {
            long now = System.currentTimeMillis() / 1000;

            if ( ( iMaximumTimeout > 0 ) && ( now > ( timeStart + iMaximumTimeout ) ) ) {
              continueLoop = false;

              // file doesn't exist after timeout, either true or false
              if ( isSuccessOnTimeout() ) {
                if ( log.isBasic() ) {
                  logBasic( "Didn't detect file [" + realFilename + "] before timeout, success" );
                }
                result.setResult( true );
              } else {
                if ( log.isBasic() ) {
                  logBasic( "Didn't detect file [" + realFilename + "] before timeout, failure" );
                }
                result.setResult( false );
              }
            }

            // sleep algorithm
            long sleepTime = 0;

            if ( iMaximumTimeout == 0 ) {
              sleepTime = iCycleTime;
            } else {
              if ( ( now + iCycleTime ) < ( timeStart + iMaximumTimeout ) ) {
                sleepTime = iCycleTime;
              } else {
                sleepTime = iCycleTime - ( ( now + iCycleTime ) - ( timeStart + iMaximumTimeout ) );
              }
            }

            try {
              if ( sleepTime > 0 ) {
                if ( log.isDetailed() ) {
                  logDetailed( "Sleeping "
                    + sleepTime + " seconds before next check for file [" + realFilename + "]" );
                }
                Thread.sleep( sleepTime * 1000 );
              }
            } catch ( InterruptedException e ) {
              // something strange happened
              result.setResult( false );
              continueLoop = false;
            }
          }
        }

        if ( !parentJob.isStopped() && fileObject.exists() && isFileSizeCheck() ) {
          long oldSize = -1;
          long newSize = fileObject.getContent().getSize();

          if ( log.isDetailed() ) {
            logDetailed( "File [" + realFilename + "] is " + newSize + " bytes long" );
          }
          if ( log.isBasic() ) {
            logBasic( "Waiting until file [" + realFilename + "] stops growing for " + iCycleTime + " seconds" );
          }
          while ( oldSize != newSize && !parentJob.isStopped() ) {
            try {
              if ( log.isDetailed() ) {
                logDetailed( "Sleeping "
                  + iCycleTime + " seconds, waiting for file [" + realFilename + "] to stop growing" );
              }
              Thread.sleep( iCycleTime * 1000 );
            } catch ( InterruptedException e ) {
              // something strange happened
              result.setResult( false );
              continueLoop = false;
            }
            oldSize = newSize;
            newSize = fileObject.getContent().getSize();
            if ( log.isDetailed() ) {
              logDetailed( "File [" + realFilename + "] is " + newSize + " bytes long" );
            }
          }
          if ( log.isBasic() ) {
            logBasic( "Stopped waiting for file [" + realFilename + "] to stop growing" );
          }
        }

        if ( parentJob.isStopped() ) {
          result.setResult( false );
        }
      } catch ( Exception e ) {
        logBasic( "Exception while waiting for file [" + realFilename + "] to stop growing", e );
      } finally {
        if ( fileObject != null ) {
          try {
            fileObject.close();
          } catch ( Exception e ) {
            // Ignore errors
          }
        }
      }
    } else {
      logError( "No filename is defined." );
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isSuccessOnTimeout() {
    return successOnTimeout;
  }

  public void setSuccessOnTimeout( boolean successOnTimeout ) {
    this.successOnTimeout = successOnTimeout;
  }

  public String getCheckCycleTime() {
    return checkCycleTime;
  }

  public String getRealCheckCycleTime() {
    return environmentSubstitute( getCheckCycleTime() );
  }

  public void setCheckCycleTime( String checkCycleTime ) {
    this.checkCycleTime = checkCycleTime;
  }

  public String getMaximumTimeout() {
    return maximumTimeout;
  }

  public String getRealMaximumTimeout() {
    return environmentSubstitute( getMaximumTimeout() );
  }

  public void setMaximumTimeout( String maximumTimeout ) {
    this.maximumTimeout = maximumTimeout;
  }

  public boolean isFileSizeCheck() {
    return fileSizeCheck;
  }

  public void setFileSizeCheck( boolean fileSizeCheck ) {
    this.fileSizeCheck = fileSizeCheck;
  }

  public boolean isAddFilenameToResult() {
    return addFilenameToResult;
  }

  public void setAddFilenameToResult( boolean addFilenameToResult ) {
    this.addFilenameToResult = addFilenameToResult;
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

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "filename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "maximumTimeout", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "checkCycleTime", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

}
