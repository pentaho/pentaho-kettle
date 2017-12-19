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

package org.pentaho.di.job.entries.deletefiles;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
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
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'delete files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryDeleteFiles.class; // for i18n purposes, needed by Translator2!!

  private boolean argFromPrevious;

  private boolean includeSubfolders;

  private String[] arguments;

  private String[] filemasks;

  public JobEntryDeleteFiles( String jobName ) {
    super( jobName, "" );
    argFromPrevious = false;
    arguments = null;

    includeSubfolders = false;
  }

  public JobEntryDeleteFiles() {
    this( "" );
  }

  public void allocate( int numberOfFields ) {
    arguments = new String[ numberOfFields ];
    filemasks = new String[ numberOfFields ];
  }

  public Object clone() {
    JobEntryDeleteFiles jobEntry = (JobEntryDeleteFiles) super.clone();
    if ( arguments != null ) {
      int nrFields = arguments.length;
      jobEntry.allocate( nrFields );
      System.arraycopy( arguments, 0, jobEntry.arguments, 0, nrFields );
      System.arraycopy( filemasks, 0, jobEntry.filemasks, 0, nrFields );
    }
    return jobEntry;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", argFromPrevious ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", includeSubfolders ) );

    retval.append( "      <fields>" ).append( Const.CR );
    if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "name", arguments[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "filemask", filemasks[i] ) );
        retval.append( "        </field>" ).append( Const.CR );

        if ( getParentJobMeta() != null ) {
          getParentJobMeta().getNamedClusterEmbedManager().registerUrl( arguments[i] );
        }
      }
    }
    retval.append( "      </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      includeSubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_subfolders" ) );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      int numberOfFields = XMLHandler.countNodes( fields, "field" );
      allocate( numberOfFields );

      for ( int i = 0; i < numberOfFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        arguments[i] = XMLHandler.getTagValue( fnode, "name" );
        filemasks[i] = XMLHandler.getTagValue( fnode, "filemask" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToLoadFromXml" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      argFromPrevious = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      includeSubfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "include_subfolders" );

      int numberOfArgs = rep.countNrJobEntryAttributes( id_jobentry, "name" );
      allocate( numberOfArgs );

      for ( int i = 0; i < numberOfArgs; i++ ) {
        arguments[i] = rep.getJobEntryAttributeString( id_jobentry, i, "name" );
        filemasks[i] = rep.getJobEntryAttributeString( id_jobentry, i, "filemask" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToLoadFromRepo", String
        .valueOf( id_jobentry ) ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", argFromPrevious );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_subfolders", includeSubfolders );

      // save the arguments...
      if ( arguments != null ) {
        for ( int i = 0; i < arguments.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "name", arguments[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "filemask", filemasks[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryDeleteFiles.UnableToSaveToRepo", String
        .valueOf( id_job ) ), dbe );
    }
  }

  public Result execute( Result result, int nr ) throws KettleException {
    List<RowMetaAndData> resultRows = result.getRows();

    int numberOfErrFiles = 0;
    result.setResult( false );
    result.setNrErrors( 1 );

    if ( argFromPrevious && log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FoundPreviousRows", String
        .valueOf( ( resultRows != null ? resultRows.size() : 0 ) ) ) );
    }

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    Multimap<String, String> pathToMaskMap = populateDataForJobExecution( resultRows );

    for ( Map.Entry<String, String> pathToMask : pathToMaskMap.entries() ) {
      final String filePath = environmentSubstitute( pathToMask.getKey() );
      if ( filePath.trim().isEmpty() ) {
        // Relative paths are permitted, and providing an empty path means deleting all files inside a root pdi-folder.
        // It is much more likely to be a mistake than a desirable action, so we don't delete anything (see PDI-15181)
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.NoPathProvided" ) );
        }
      } else {
        final String fileMask = environmentSubstitute( pathToMask.getValue() );

        if ( parentJob.isStopped() ) {
          break;
        }

        if ( !processFile( filePath, fileMask, parentJob ) ) {
          numberOfErrFiles++;
        }
      }
    }

    if ( numberOfErrFiles == 0 ) {
      result.setResult( true );
      result.setNrErrors( 0 );
    } else {
      result.setNrErrors( numberOfErrFiles );
      result.setResult( false );
    }

    return result;
  }

  /**
   * For job execution path to files and file masks should be provided.
   * These values can be obtained in two ways:
   * 1. As an argument of a current job entry
   * 2. As a table, that comes as a result of execution previous job/transformation.
   *
   * As the logic of processing this data is the same for both of this cases, we first
   * populate this data (in this method) and then process it.
   *
   * We are using guava multimap here, because if allows key duplication and there could be a
   * situation where two paths to one folder with different wildcards are provided.
   */
  private Multimap<String, String> populateDataForJobExecution( List<RowMetaAndData> rowsFromPreviousMeta ) throws KettleValueException {
    Multimap<String, String> pathToMaskMap = ArrayListMultimap.create();
    if ( argFromPrevious && rowsFromPreviousMeta != null ) {
      for ( RowMetaAndData resultRow : rowsFromPreviousMeta ) {
        if ( resultRow.size() < 2 ) {
          logError( BaseMessages.getString(
            PKG, "JobDeleteFiles.Error.InvalidNumberOfRowsFromPrevMeta", resultRow.size() ) );
          return pathToMaskMap;
        }
        String pathToFile = resultRow.getString( 0, null );
        String fileMask = resultRow.getString( 1, null );

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEntryDeleteFiles.ProcessingRow", pathToFile, fileMask ) );
        }

        pathToMaskMap.put( pathToFile, fileMask );
      }
    } else if ( arguments != null ) {
      for ( int i = 0; i < arguments.length; i++ ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString(
            PKG, "JobEntryDeleteFiles.ProcessingArg", arguments[ i ], filemasks[ i ] ) );
        }
        pathToMaskMap.put( arguments[ i ], filemasks[ i ] );
      }
    }

    return pathToMaskMap;
  }

  boolean processFile( String path, String wildcard, Job parentJob ) {
    boolean isDeleted = false;
    FileObject fileFolder = null;

    try {
      fileFolder = KettleVFS.getFileObject( path, this );

      if ( fileFolder.exists() ) {
        if ( fileFolder.getType() == FileType.FOLDER ) {

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.ProcessingFolder", path ) );
          }

          int totalDeleted = fileFolder.delete( new TextFileSelector( fileFolder.toString(), wildcard, parentJob ) );

          if ( log.isDetailed() ) {
            logDetailed(
              BaseMessages.getString( PKG, "JobEntryDeleteFiles.TotalDeleted", String.valueOf( totalDeleted ) ) );
          }
          isDeleted = true;
        } else {

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.ProcessingFile", path ) );
          }
          isDeleted = fileFolder.delete();
          if ( !isDeleted ) {
            logError( BaseMessages.getString( PKG, "JobEntryDeleteFiles.CouldNotDeleteFile", path ) );
          } else {
            if ( log.isBasic() ) {
              logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FileDeleted", path ) );
            }
          }
        }
      } else {
        // File already deleted, no reason to try to delete it
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "JobEntryDeleteFiles.FileAlreadyDeleted", path ) );
        }
        isDeleted = true;
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JobEntryDeleteFiles.CouldNotProcess", path, e
        .getMessage() ), e );
    } finally {
      if ( fileFolder != null ) {
        try {
          fileFolder.close();
        } catch ( IOException ex ) {
          // Ignore
        }
      }
    }

    return isDeleted;
  }

  private class TextFileSelector implements FileSelector {
    String fileWildcard = null;
    String sourceFolder = null;
    Job parentjob;

    public TextFileSelector( String sourcefolderin, String filewildcard, Job parentJob ) {

      if ( !Utils.isEmpty( sourcefolderin ) ) {
        sourceFolder = sourcefolderin;
      }

      if ( !Utils.isEmpty( filewildcard ) ) {
        fileWildcard = filewildcard;
      }
      parentjob = parentJob;
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean doReturnCode = false;
      try {

        if ( !info.getFile().toString().equals( sourceFolder ) && !parentjob.isStopped() ) {
          // Pass over the Base folder itself
          String shortFilename = info.getFile().getName().getBaseName();

          if ( !info.getFile().getParent().equals( info.getBaseFolder() ) ) {
            // Not in the Base Folder..Only if include sub folders
            if ( includeSubfolders
              && ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( shortFilename, fileWildcard ) ) {
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.DeletingFile", info
                  .getFile().toString() ) );
              }
              doReturnCode = true;
            }
          } else {
            // In the Base Folder...
            if ( ( info.getFile().getType() == FileType.FILE ) && GetFileWildcard( shortFilename, fileWildcard ) ) {
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobEntryDeleteFiles.DeletingFile", info
                  .getFile().toString() ) );
              }
              doReturnCode = true;
            }
          }
        }
      } catch ( Exception e ) {
        log.logError(
          BaseMessages.getString( PKG, "JobDeleteFiles.Error.Exception.DeleteProcessError" ), BaseMessages
            .getString( PKG, "JobDeleteFiles.Error.Exception.DeleteProcess", info.getFile().toString(), e
              .getMessage() ) );

        doReturnCode = false;
      }

      return doReturnCode;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return true;
    }
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean GetFileWildcard( String selectedfile, String wildcard ) {
    boolean getIt = true;

    if ( !Utils.isEmpty( wildcard ) ) {
      Pattern pattern = Pattern.compile( wildcard );
      // First see if the file matches the regular expression!
      Matcher matcher = pattern.matcher( selectedfile );
      getIt = matcher.matches();
    }

    return getIt;
  }

  public void setIncludeSubfolders( boolean includeSubfolders ) {
    this.includeSubfolders = includeSubfolders;
  }

  public void setPrevious( boolean argFromPrevious ) {
    this.argFromPrevious = argFromPrevious;
  }

  public boolean evaluates() {
    return true;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean isValid = JobEntryValidatorUtils.andValidator().validate( this, "arguments", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    if ( !isValid ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );

    for ( int i = 0; i < arguments.length; i++ ) {
      JobEntryValidatorUtils.andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( arguments != null ) {
      ResourceReference reference = null;
      for ( int i = 0; i < arguments.length; i++ ) {
        String filename = jobMeta.environmentSubstitute( arguments[i] );
        if ( reference == null ) {
          reference = new ResourceReference( this );
          references.add( reference );
        }
        reference.getEntries().add( new ResourceEntry( filename, ResourceType.FILE ) );
      }
    }
    return references;
  }

  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  public void setFilemasks( String[] filemasks ) {
    this.filemasks = filemasks;
  }

  public void setArgFromPrevious( boolean argFromPrevious ) {
    this.argFromPrevious = argFromPrevious;
  }

  public boolean isArgFromPrevious() {
    return argFromPrevious;
  }

  public String[] getArguments() {
    return arguments;
  }

  public String[] getFilemasks() {
    return filemasks;
  }

  public boolean isIncludeSubfolders() {
    return includeSubfolders;
  }

}
