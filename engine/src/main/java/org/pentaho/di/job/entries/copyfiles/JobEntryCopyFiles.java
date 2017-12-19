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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.copyfiles;

import org.apache.commons.vfs2.NameScope;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
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
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'copy files' job entry.
 *
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryCopyFiles extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryCopyFiles.class; // for i18n purposes, needed by Translator2!!

  public static final String SOURCE_CONFIGURATION_NAME = "source_configuration_name";
  public static final String SOURCE_FILE_FOLDER = "source_filefolder";

  public static final String DESTINATION_CONFIGURATION_NAME = "destination_configuration_name";
  public static final String DESTINATION_FILE_FOLDER = "destination_filefolder";

  public static final String LOCAL_SOURCE_FILE = "LOCAL-SOURCE-FILE-";
  public static final String LOCAL_DEST_FILE = "LOCAL-DEST-FILE-";

  public static final String STATIC_SOURCE_FILE = "STATIC-SOURCE-FILE-";
  public static final String STATIC_DEST_FILE = "STATIC-DEST-FILE-";

  public static final String DEST_URL = "EMPTY_DEST_URL-";
  public static final String SOURCE_URL = "EMPTY_SOURCE_URL-";

  public boolean copy_empty_folders;
  public boolean arg_from_previous;
  public boolean overwrite_files;
  public boolean include_subfolders;
  public boolean add_result_filesname;
  public boolean remove_source_files;
  public boolean destination_is_a_file;
  public boolean create_destination_folder;
  public String[] source_filefolder;
  public String[] destination_filefolder;
  public String[] wildcard;
  HashSet<String> list_files_remove = new HashSet<String>();
  HashSet<String> list_add_result = new HashSet<String>();
  int NbrFail = 0;

  private Map<String, String> configurationMappings = new HashMap<String, String>();

  public JobEntryCopyFiles( String n ) {
    super( n, "" );
    copy_empty_folders = true;
    arg_from_previous = false;
    source_filefolder = null;
    remove_source_files = false;
    destination_filefolder = null;
    wildcard = null;
    overwrite_files = false;
    include_subfolders = false;
    add_result_filesname = false;
    destination_is_a_file = false;
    create_destination_folder = false;
  }

  public JobEntryCopyFiles() {
    this( "" );
  }

  public void allocate( int nrFields ) {
    source_filefolder = new String[nrFields];
    destination_filefolder = new String[nrFields];
    wildcard = new String[nrFields];
  }

  public Object clone() {
    JobEntryCopyFiles je = (JobEntryCopyFiles) super.clone();
    if ( source_filefolder != null ) {
      int nrFields = source_filefolder.length;
      je.allocate( nrFields );
      System.arraycopy( source_filefolder, 0, je.source_filefolder, 0, nrFields );
      System.arraycopy( destination_filefolder, 0, je.destination_filefolder, 0, nrFields );
      System.arraycopy( wildcard, 0, je.wildcard, 0, nrFields );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "copy_empty_folders", copy_empty_folders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "arg_from_previous", arg_from_previous ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "overwrite_files", overwrite_files ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", include_subfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remove_source_files", remove_source_files ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_result_filesname", add_result_filesname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "destination_is_a_file", destination_is_a_file ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "create_destination_folder", create_destination_folder ) );

    retval.append( "      <fields>" ).append( Const.CR );

    // Get source and destination files, also wildcard
    String[] vsourcefilefolder = preprocessfilefilder( source_filefolder );
    String[] vdestinationfilefolder = preprocessfilefilder( destination_filefolder );
    if ( source_filefolder != null ) {
      for ( int i = 0; i < source_filefolder.length; i++ ) {
        retval.append( "        <field>" ).append( Const.CR );
        saveSource( retval, source_filefolder[i] );
        saveDestination( retval, destination_filefolder[i] );
        if ( parentJobMeta != null ) {
          parentJobMeta.getNamedClusterEmbedManager().registerUrl( vsourcefilefolder[i] );
          parentJobMeta.getNamedClusterEmbedManager().registerUrl( vdestinationfilefolder[i] );
        }
        retval.append( "          " ).append( XMLHandler.addTagValue( "wildcard", wildcard[i] ) );
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
      copy_empty_folders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "copy_empty_folders" ) );
      arg_from_previous = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "arg_from_previous" ) );
      overwrite_files = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "overwrite_files" ) );
      include_subfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_subfolders" ) );
      remove_source_files = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove_source_files" ) );
      add_result_filesname = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "add_result_filesname" ) );
      destination_is_a_file = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "destination_is_a_file" ) );
      create_destination_folder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "create_destination_folder" ) );

      Node fields = XMLHandler.getSubNode( entrynode, "fields" );

      // How many field arguments?
      int nrFields = XMLHandler.countNodes( fields, "field" );
      allocate( nrFields );

      // Read them all...
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        source_filefolder[i] = loadSource( fnode );
        destination_filefolder[i] = loadDestination( fnode );
        wildcard[i] = XMLHandler.getTagValue( fnode, "wildcard" );
      }
    } catch ( KettleXMLException xe ) {

      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.UnableLoadXML" ), xe );
    }
  }

  protected String loadSource( Node fnode ) {
    String source_filefolder = XMLHandler.getTagValue( fnode, SOURCE_FILE_FOLDER );
    String ncName = XMLHandler.getTagValue( fnode, SOURCE_CONFIGURATION_NAME );
    return loadURL( source_filefolder, ncName, getMetaStore(), configurationMappings );
  }

  protected String loadDestination( Node fnode ) {
    String destination_filefolder = XMLHandler.getTagValue( fnode, DESTINATION_FILE_FOLDER );
    String ncName = XMLHandler.getTagValue( fnode, DESTINATION_CONFIGURATION_NAME );
    return loadURL( destination_filefolder, ncName, getMetaStore(), configurationMappings );
  }

  protected void saveSource( StringBuilder retval, String source ) {
    String namedCluster = configurationMappings.get( source );
    retval.append( "          " ).append( XMLHandler.addTagValue( SOURCE_FILE_FOLDER, source ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( SOURCE_CONFIGURATION_NAME, namedCluster ) );
  }

  protected void saveDestination( StringBuilder retval, String destination ) {
    String namedCluster = configurationMappings.get( destination );
    retval.append( "          " ).append( XMLHandler.addTagValue( DESTINATION_FILE_FOLDER, destination ) );
    retval.append( "          " ).append( XMLHandler.addTagValue( DESTINATION_CONFIGURATION_NAME, namedCluster ) );
  }

  protected String loadSourceRep( Repository rep, ObjectId id_jobentry, int a ) throws KettleException {
    String source_filefolder = rep.getJobEntryAttributeString( id_jobentry, a, SOURCE_FILE_FOLDER );
    String ncName = rep.getJobEntryAttributeString( id_jobentry, a, SOURCE_CONFIGURATION_NAME );
    return loadURL( source_filefolder, ncName, getMetaStore(), configurationMappings );
  }

  protected String loadDestinationRep( Repository rep, ObjectId id_jobentry, int a ) throws KettleException {
    String destination_filefolder = rep.getJobEntryAttributeString( id_jobentry, a, DESTINATION_FILE_FOLDER );
    String ncName = rep.getJobEntryAttributeString( id_jobentry, a, DESTINATION_CONFIGURATION_NAME );
    return loadURL( destination_filefolder, ncName, getMetaStore(), configurationMappings );
  }

  protected void saveSourceRep( Repository rep, ObjectId id_job, ObjectId id_jobentry, int i, String value )
    throws KettleException {
    String namedCluster = configurationMappings.get( value );
    rep.saveJobEntryAttribute( id_job, getObjectId(), i, SOURCE_FILE_FOLDER, value );
    rep.saveJobEntryAttribute( id_job, id_jobentry, i, SOURCE_CONFIGURATION_NAME, namedCluster );
  }

  protected void saveDestinationRep( Repository rep, ObjectId id_job, ObjectId id_jobentry, int i, String value )
    throws KettleException {
    String namedCluster = configurationMappings.get( value );
    rep.saveJobEntryAttribute( id_job, getObjectId(), i, DESTINATION_FILE_FOLDER, value );
    rep.saveJobEntryAttribute( id_job, id_jobentry, i, DESTINATION_CONFIGURATION_NAME, namedCluster );
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      copy_empty_folders = rep.getJobEntryAttributeBoolean( id_jobentry, "copy_empty_folders" );
      arg_from_previous = rep.getJobEntryAttributeBoolean( id_jobentry, "arg_from_previous" );
      overwrite_files = rep.getJobEntryAttributeBoolean( id_jobentry, "overwrite_files" );
      include_subfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "include_subfolders" );
      remove_source_files = rep.getJobEntryAttributeBoolean( id_jobentry, "remove_source_files" );

      add_result_filesname = rep.getJobEntryAttributeBoolean( id_jobentry, "add_result_filesname" );
      destination_is_a_file = rep.getJobEntryAttributeBoolean( id_jobentry, "destination_is_a_file" );
      create_destination_folder = rep.getJobEntryAttributeBoolean( id_jobentry, "create_destination_folder" );

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "source_filefolder" );
      allocate( argnr );

      // Read them all...
      for ( int a = 0; a < argnr; a++ ) {
        source_filefolder[a] = loadSourceRep( rep, id_jobentry, a );
        destination_filefolder[a] = loadDestinationRep( rep, id_jobentry, a );
        wildcard[a] = rep.getJobEntryAttributeString( id_jobentry, a, "wildcard" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.UnableLoadRep" )
        + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "copy_empty_folders", copy_empty_folders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "arg_from_previous", arg_from_previous );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "overwrite_files", overwrite_files );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_subfolders", include_subfolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remove_source_files", remove_source_files );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_result_filesname", add_result_filesname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "destination_is_a_file", destination_is_a_file );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "create_destination_folder", create_destination_folder );

      // save the arguments...
      if ( source_filefolder != null ) {
        for ( int i = 0; i < source_filefolder.length; i++ ) {
          saveSourceRep( rep, id_job, getObjectId(), i, source_filefolder[i] );
          saveDestinationRep( rep, id_job, getObjectId(), i, destination_filefolder[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "wildcard", wildcard[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {

      throw new KettleException( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.UnableSaveRep" )
        + id_job, dbe );
    }
  }

  String[] preprocessfilefilder( String[] folders ) {
    List<String> nfolders = new ArrayList<String>();
    if ( folders != null ) {
      for ( int i = 0; i < folders.length; i++ ) {
        nfolders.add( folders[ i ].replace( JobEntryCopyFiles.SOURCE_URL + i + "-", "" )
          .replace( JobEntryCopyFiles.DEST_URL + i + "-", "" ) );
      }
    }
    return nfolders.toArray( new String[ nfolders.size() ] );
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    int NbrFail = 0;

    NbrFail = 0;

    if ( isBasic() ) {
      logBasic( BaseMessages.getString( PKG, "JobCopyFiles.Log.Starting" ) );
    }

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( parentJobMeta.getNamedClusterEmbedManager() != null ) {
      parentJobMeta.getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( this, parentJobMeta.getEmbeddedMetastoreProviderKey() );
    }

    try {
      // Get source and destination files, also wildcard
      String[] vsourcefilefolder = preprocessfilefilder( source_filefolder );
      String[] vdestinationfilefolder = preprocessfilefilder( destination_filefolder );
      String[] vwildcard = wildcard;

      result.setResult( false );
      result.setNrErrors( 1 );

      if ( arg_from_previous ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.ArgFromPrevious.Found", ( rows != null ? rows
              .size() : 0 )
              + "" ) );
        }
      }

      if ( arg_from_previous && rows != null ) { // Copy the input row to the (command line) arguments
        for ( int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++ ) {
          resultRow = rows.get( iteration );

          // Get source and destination file names, also wildcard
          String vsourcefilefolder_previous = resultRow.getString( 0, null );
          String vdestinationfilefolder_previous = resultRow.getString( 1, null );
          String vwildcard_previous = resultRow.getString( 2, null );

          if ( !Utils.isEmpty( vsourcefilefolder_previous ) && !Utils.isEmpty( vdestinationfilefolder_previous ) ) {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.ProcessingRow", KettleVFS.getFriendlyURI( vsourcefilefolder_previous ),
                      KettleVFS.getFriendlyURI( vdestinationfilefolder_previous ), vwildcard_previous ) );
            }

            if ( !processFileFolder( vsourcefilefolder_previous, vdestinationfilefolder_previous, vwildcard_previous,
                parentJob, result ) ) {
              // The copy process fail
              NbrFail++;
            }
          } else {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.IgnoringRow", KettleVFS.getFriendlyURI( vsourcefilefolder[iteration] ),
                      KettleVFS.getFriendlyURI( vdestinationfilefolder[iteration] ), vwildcard[iteration] ) );
            }
          }
        }
      } else if ( vsourcefilefolder != null && vdestinationfilefolder != null ) {
        for ( int i = 0; i < vsourcefilefolder.length && !parentJob.isStopped(); i++ ) {
          if ( !Utils.isEmpty( vsourcefilefolder[i] ) && !Utils.isEmpty( vdestinationfilefolder[i] ) ) {

            // ok we can process this file/folder

            if ( isBasic() ) {
              logBasic( BaseMessages.getString( PKG, "JobCopyFiles.Log.ProcessingRow", KettleVFS.getFriendlyURI( vsourcefilefolder[i] ),
                      KettleVFS.getFriendlyURI( vdestinationfilefolder[i] ), vwildcard[i] ) );
            }

            if ( !processFileFolder( vsourcefilefolder[i], vdestinationfilefolder[i], vwildcard[i], parentJob, result ) ) {
              // The copy process fail
              NbrFail++;
            }
          } else {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.IgnoringRow", KettleVFS.getFriendlyURI( vsourcefilefolder[i] ),
                      KettleVFS.getFriendlyURI( vdestinationfilefolder[i] ), vwildcard[i] ) );
            }
          }
        }
      }
    } finally {
      list_add_result = null;
      list_files_remove = null;
    }

    // Check if all files was process with success
    if ( NbrFail == 0 ) {
      result.setResult( true );
      result.setNrErrors( 0 );
    } else {
      result.setNrErrors( NbrFail );
    }

    return result;
  }

  boolean processFileFolder( String sourcefilefoldername, String destinationfilefoldername, String wildcard,
      Job parentJob, Result result ) {
    boolean entrystatus = false;
    FileObject sourcefilefolder = null;
    FileObject destinationfilefolder = null;

    // Clear list files to remove after copy process
    // This list is also added to result files name
    list_files_remove.clear();
    list_add_result.clear();

    // Get real source, destination file and wildcard
    String realSourceFilefoldername = environmentSubstitute( sourcefilefoldername );
    String realDestinationFilefoldername = environmentSubstitute( destinationfilefoldername );
    String realWildcard = environmentSubstitute( wildcard );

    try {
      sourcefilefolder = KettleVFS.getFileObject( realSourceFilefoldername, this );
      destinationfilefolder = KettleVFS.getFileObject( realDestinationFilefoldername, this );

      if ( sourcefilefolder.exists() ) {

        // Check if destination folder/parent folder exists !
        // If user wanted and if destination folder does not exist
        // PDI will create it
        if ( CreateDestinationFolder( destinationfilefolder ) ) {

          // Basic Tests
          if ( sourcefilefolder.getType().equals( FileType.FOLDER ) && destination_is_a_file ) {
            // Source is a folder, destination is a file
            // WARNING !!! CAN NOT COPY FOLDER TO FILE !!!

            logError( BaseMessages.getString(
              PKG, "JobCopyFiles.Log.CanNotCopyFolderToFile", KettleVFS.getFriendlyURI( realSourceFilefoldername ),
                    KettleVFS.getFriendlyURI( realDestinationFilefoldername ) ) );

            NbrFail++;

          } else {

            if ( destinationfilefolder.getType().equals( FileType.FOLDER )
              && sourcefilefolder.getType().equals( FileType.FILE ) ) {
              // Source is a file, destination is a folder
              // Copy the file to the destination folder

              destinationfilefolder.copyFrom( sourcefilefolder.getParent(), new TextOneFileSelector(
                sourcefilefolder.getParent().toString(), sourcefilefolder.getName().getBaseName(),
                destinationfilefolder.toString() ) );
              if ( isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.FileCopied", KettleVFS.getFriendlyURI( sourcefilefolder ),
                        KettleVFS.getFriendlyURI( destinationfilefolder ) ) );
              }

            } else if ( sourcefilefolder.getType().equals( FileType.FILE ) && destination_is_a_file ) {
              // Source is a file, destination is a file

              destinationfilefolder.copyFrom( sourcefilefolder, new TextOneToOneFileSelector(
                destinationfilefolder ) );
            } else {
              // Both source and destination are folders
              if ( isDetailed() ) {
                logDetailed( "  " );
                logDetailed( BaseMessages.getString( PKG, "JobCopyFiles.Log.FetchFolder", KettleVFS.getFriendlyURI( sourcefilefolder ) ) );

              }

              TextFileSelector textFileSelector =
                new TextFileSelector( sourcefilefolder, destinationfilefolder, realWildcard, parentJob );
              try {
                destinationfilefolder.copyFrom( sourcefilefolder, textFileSelector );
              } finally {
                textFileSelector.shutdown();
              }
            }

            // Remove Files if needed
            if ( remove_source_files && !list_files_remove.isEmpty() ) {
              String sourceFilefoldername = sourcefilefolder.toString();
              int trimPathLength = sourceFilefoldername.length() + 1;
              FileObject removeFile;

              for ( Iterator<String> iter = list_files_remove.iterator(); iter.hasNext() && !parentJob.isStopped(); ) {
                String fileremoventry = iter.next();
                removeFile = null; // re=null each iteration
                // Try to get the file relative to the existing connection
                if ( fileremoventry.startsWith( sourceFilefoldername ) ) {
                  if ( trimPathLength < fileremoventry.length() ) {
                    removeFile = sourcefilefolder.getChild( fileremoventry.substring( trimPathLength ) );
                  }
                }

                // Unable to retrieve file through existing connection; Get the file through a new VFS connection
                if ( removeFile == null ) {
                  removeFile = KettleVFS.getFileObject( fileremoventry, this );
                }

                // Remove ONLY Files
                if ( removeFile.getType() == FileType.FILE ) {
                  boolean deletefile = removeFile.delete();
                  logBasic( " ------ " );
                  if ( !deletefile ) {
                    logError( "      "
                      + BaseMessages.getString(
                        PKG, "JobCopyFiles.Error.Exception.CanRemoveFileFolder", KettleVFS.getFriendlyURI( fileremoventry ) ) );
                  } else {
                    if ( isDetailed() ) {
                      logDetailed( "      "
                        + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileFolderRemoved", KettleVFS.getFriendlyURI( fileremoventry ) ) );
                    }
                  }
                }
              }
            }

            // Add files to result files name
            if ( add_result_filesname && !list_add_result.isEmpty() ) {
              String destinationFilefoldername = destinationfilefolder.toString();
              int trimPathLength = destinationFilefoldername.length() + 1;
              FileObject addFile;

              for ( Iterator<String> iter = list_add_result.iterator(); iter.hasNext(); ) {
                String fileaddentry = iter.next();
                addFile = null; // re=null each iteration

                // Try to get the file relative to the existing connection
                if ( fileaddentry.startsWith( destinationFilefoldername ) ) {
                  if ( trimPathLength < fileaddentry.length() ) {
                    addFile = destinationfilefolder.getChild( fileaddentry.substring( trimPathLength ) );
                  }
                }

                // Unable to retrieve file through existing connection; Get the file through a new VFS connection
                if ( addFile == null ) {
                  addFile = KettleVFS.getFileObject( fileaddentry, this );
                }

                // Add ONLY Files
                if ( addFile.getType() == FileType.FILE ) {
                  ResultFile resultFile =
                    new ResultFile( ResultFile.FILE_TYPE_GENERAL, addFile, parentJob.getJobname(), toString() );
                  result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
                  if ( isDetailed() ) {
                    logDetailed( " ------ " );
                    logDetailed( "      "
                      + BaseMessages
                        .getString( PKG, "JobCopyFiles.Log.FileAddedToResultFilesName", KettleVFS.getFriendlyURI( fileaddentry ) ) );
                  }
                }
              }
            }
          }
          entrystatus = true;
        } else {
          // Destination Folder or Parent folder is missing
          logError( BaseMessages.getString(
            PKG, "JobCopyFiles.Error.DestinationFolderNotFound", KettleVFS.getFriendlyURI( realDestinationFilefoldername ) ) );
        }
      } else {
        logError( BaseMessages.getString( PKG, "JobCopyFiles.Error.SourceFileNotExists", KettleVFS.getFriendlyURI( realSourceFilefoldername ) ) );

      }
    } catch ( FileSystemException fse ) {
      logError( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.CopyProcessFileSystemException", fse
        .getMessage() ) );
      Throwable throwable = fse.getCause();
      while ( throwable != null ) {
        logError( BaseMessages.getString( PKG, "JobCopyFiles.Log.CausedBy", throwable.getMessage() ) );
        throwable = throwable.getCause();
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString(
        PKG, "JobCopyFiles.Error.Exception.CopyProcess", KettleVFS.getFriendlyURI( realSourceFilefoldername ),
              KettleVFS.getFriendlyURI( realDestinationFilefoldername ), e.getMessage() ), e );
    } finally {
      if ( sourcefilefolder != null ) {
        try {
          sourcefilefolder.close();
          sourcefilefolder = null;
        } catch ( IOException ex ) { /* Ignore */
        }
      }
      if ( destinationfilefolder != null ) {
        try {
          destinationfilefolder.close();
          destinationfilefolder = null;
        } catch ( IOException ex ) { /* Ignore */
        }
      }
    }

    return entrystatus;
  }

  private class TextOneToOneFileSelector implements FileSelector {
    FileObject destfile = null;

    public TextOneToOneFileSelector( FileObject destinationfile ) {

      if ( destinationfile != null ) {
        destfile = destinationfile;
      }
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean resultat = false;
      String fil_name = null;

      try {
        // check if the destination file exists

        if ( destfile.exists() ) {
          if ( isDetailed() ) {
            logDetailed( "      "
              + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( destfile ) ) );
          }

          if ( overwrite_files ) {
            if ( isDetailed() ) {
              logDetailed( "      "
                + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileOverwrite", KettleVFS.getFriendlyURI( destfile ) ) );
            }

            resultat = true;
          }
        } else {
          if ( isDetailed() ) {
            logDetailed( "      "
              + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileCopied", KettleVFS.getFriendlyURI( info.getFile() ), KettleVFS.getFriendlyURI( destfile ) ) );
          }

          resultat = true;
        }

        if ( resultat && remove_source_files ) {
          // add this folder/file to remove files
          // This list will be fetched and all entries files
          // will be removed
          list_files_remove.add( info.getFile().toString() );
        }

        if ( resultat && add_result_filesname ) {
          // add this folder/file to result files name
          list_add_result.add( destfile.toString() );
        }

      } catch ( Exception e ) {

        logError( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.CopyProcess", KettleVFS.getFriendlyURI( info
          .getFile() ), fil_name, e.getMessage() ) );

      }

      return resultat;

    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return false;
    }
  }

  private boolean CreateDestinationFolder( FileObject filefolder ) {
    FileObject folder = null;
    try {
      if ( destination_is_a_file ) {
        folder = filefolder.getParent();
      } else {
        folder = filefolder;
      }

      if ( !folder.exists() ) {
        if ( create_destination_folder ) {
          if ( isDetailed() ) {
            logDetailed( "Folder  " + KettleVFS.getFriendlyURI( folder ) + " does not exist !" );
          }
          folder.createFolder();
          if ( isDetailed() ) {
            logDetailed( "Folder parent was created." );
          }
        } else {
          logError( "Folder  " + KettleVFS.getFriendlyURI( folder ) + " does not exist !" );
          return false;
        }
      }
      return true;
    } catch ( Exception e ) {
      logError( "Couldn't created parent folder " + KettleVFS.getFriendlyURI( folder ), e );
    } finally {
      if ( folder != null ) {
        try {
          folder.close();
          folder = null;
        } catch ( Exception ex ) { /* Ignore */
        }
      }
    }
    return false;
  }

  private class TextFileSelector implements FileSelector {
    String fileWildcard = null;
    String sourceFolder = null;
    String destinationFolder = null;
    Job parentjob;
    Pattern pattern;
    private int traverseCount;

    // Store connection to destination source for improved performance to remote hosts
    FileObject destinationFolderObject = null;

    /**********************************************************
     *
     * @param selectedfile
     * @return True if the selectedfile matches the wildcard
     **********************************************************/
    private boolean GetFileWildcard( String selectedfile ) {
      boolean getIt = true;
      // First see if the file matches the regular expression!
      if ( pattern != null ) {
        Matcher matcher = pattern.matcher( selectedfile );
        getIt = matcher.matches();
      }
      return getIt;
    }

    public TextFileSelector( FileObject sourcefolderin, FileObject destinationfolderin, String filewildcard,
      Job parentJob ) {

      if ( sourcefolderin != null ) {
        sourceFolder = sourcefolderin.toString();
      }
      if ( destinationfolderin != null ) {
        destinationFolderObject = destinationfolderin;
        destinationFolder = destinationFolderObject.toString();
      }
      if ( !Utils.isEmpty( filewildcard ) ) {
        fileWildcard = filewildcard;
        pattern = Pattern.compile( fileWildcard );
      }
      parentjob = parentJob;
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean returncode = false;
      FileObject file_name = null;
      String addFileNameString = null;
      try {

        if ( !info.getFile().toString().equals( sourceFolder ) && !parentjob.isStopped() ) {
          // Pass over the Base folder itself

          String short_filename = info.getFile().getName().getBaseName();
          // Built destination filename
          if ( destinationFolderObject == null ) {
            // Resolve the destination folder
            destinationFolderObject = KettleVFS.getFileObject( destinationFolder, JobEntryCopyFiles.this );
          }

          String fullName = info.getFile().toString();
          String baseFolder = info.getBaseFolder().toString();
          String path = fullName.substring( fullName.indexOf( baseFolder ) + baseFolder.length() + 1 );
          file_name = destinationFolderObject.resolveFile( path, NameScope.DESCENDENT );

          if ( !info.getFile().getParent().equals( info.getBaseFolder() ) ) {

            // Not in the Base Folder..Only if include sub folders
            if ( include_subfolders ) {
              // Folders..only if include subfolders
              if ( info.getFile().getType() == FileType.FOLDER ) {
                if ( include_subfolders && copy_empty_folders && Utils.isEmpty( fileWildcard ) ) {
                  if ( ( file_name == null ) || ( !file_name.exists() ) ) {
                    if ( isDetailed() ) {
                      logDetailed( " ------ " );
                      logDetailed( "      "
                        + BaseMessages.getString( PKG, "JobCopyFiles.Log.FolderCopied", KettleVFS.getFriendlyURI( info
                          .getFile() ), file_name != null ? KettleVFS.getFriendlyURI( file_name ) : "" ) );
                    }
                    returncode = true;
                  } else {
                    if ( isDetailed() ) {
                      logDetailed( " ------ " );
                      logDetailed( "      "
                        + BaseMessages.getString( PKG, "JobCopyFiles.Log.FolderExists", KettleVFS.getFriendlyURI( file_name ) ) );
                    }
                    if ( overwrite_files ) {
                      if ( isDetailed() ) {
                        logDetailed( "      "
                          + BaseMessages.getString( PKG, "JobCopyFiles.Log.FolderOverwrite", KettleVFS.getFriendlyURI( info
                            .getFile() ), KettleVFS.getFriendlyURI( file_name ) ) );
                      }
                      returncode = true;
                    }
                  }
                }

              } else {
                if ( GetFileWildcard( short_filename ) ) {
                  // Check if the file exists
                  if ( ( file_name == null ) || ( !file_name.exists() ) ) {
                    if ( isDetailed() ) {
                      logDetailed( " ------ " );
                      logDetailed( "      "
                        + BaseMessages.getString(
                          PKG, "JobCopyFiles.Log.FileCopied", KettleVFS.getFriendlyURI( info.getFile() ), file_name != null
                            ? KettleVFS.getFriendlyURI( file_name ) : "" ) );
                    }
                    returncode = true;
                  } else {
                    if ( isDetailed() ) {
                      logDetailed( " ------ " );
                      logDetailed( "      "
                        + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( file_name ) ) );
                    }
                    if ( overwrite_files ) {
                      if ( isDetailed() ) {
                        logDetailed( "       "
                          + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( info
                            .getFile() ), KettleVFS.getFriendlyURI( file_name ) ) );
                      }

                      returncode = true;
                    }
                  }
                }
              }
            }
          } else {
            // In the Base Folder...
            // Folders..only if include subfolders
            if ( info.getFile().getType() == FileType.FOLDER ) {
              if ( include_subfolders && copy_empty_folders && Utils.isEmpty( fileWildcard ) ) {
                if ( ( file_name == null ) || ( !file_name.exists() ) ) {
                  if ( isDetailed() ) {
                    logDetailed( "", " ------ " );
                    logDetailed( "      "
                      + BaseMessages.getString(
                        PKG, "JobCopyFiles.Log.FolderCopied", KettleVFS.getFriendlyURI( info.getFile() ), file_name != null
                          ? KettleVFS.getFriendlyURI( file_name ) : "" ) );
                  }

                  returncode = true;
                } else {
                  if ( isDetailed() ) {
                    logDetailed( " ------ " );
                    logDetailed( "      "
                      + BaseMessages.getString( PKG, "JobCopyFiles.Log.FolderExists", KettleVFS.getFriendlyURI( file_name ) ) );
                  }
                  if ( overwrite_files ) {
                    if ( isDetailed() ) {
                      logDetailed( "      "
                        + BaseMessages.getString( PKG, "JobCopyFiles.Log.FolderOverwrite", KettleVFS.getFriendlyURI( info
                          .getFile() ), KettleVFS.getFriendlyURI( file_name ) ) );
                    }

                    returncode = true;
                  }
                }

              }
            } else {
              // file...Check if exists
              file_name = KettleVFS.getFileObject( destinationFolder + Const.FILE_SEPARATOR + short_filename );

              if ( GetFileWildcard( short_filename ) ) {
                if ( ( file_name == null ) || ( !file_name.exists() ) ) {
                  if ( isDetailed() ) {
                    logDetailed( " ------ " );
                    logDetailed( "      "
                      + BaseMessages.getString(
                        PKG, "JobCopyFiles.Log.FileCopied", KettleVFS.getFriendlyURI( info.getFile() ), file_name != null
                          ? KettleVFS.getFriendlyURI( file_name ) : "" ) );
                  }
                  returncode = true;

                } else {
                  if ( isDetailed() ) {
                    logDetailed( " ------ " );
                    logDetailed( "      "
                      + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( file_name ) ) );
                  }

                  if ( overwrite_files ) {
                    if ( isDetailed() ) {
                      logDetailed(
                        "      " + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExistsInfos" ),
                        BaseMessages.getString(
                          PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( info.getFile() ), KettleVFS.getFriendlyURI( file_name ) ) );
                    }

                    returncode = true;
                  }

                }
              }
            }

          }

        }

      } catch ( Exception e ) {

        logError( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.CopyProcess", KettleVFS.getFriendlyURI( info
          .getFile() ), file_name != null ? KettleVFS.getFriendlyURI( file_name ) : null, e.getMessage() ) );

        returncode = false;
      } finally {
        if ( file_name != null ) {
          try {
            if ( returncode && add_result_filesname ) {
              addFileNameString = file_name.toString();
            }
            file_name.close();
            file_name = null;
          } catch ( IOException ex ) { /* Ignore */
          }
        }

      }
      if ( returncode && remove_source_files ) {
        // add this folder/file to remove files
        // This list will be fetched and all entries files
        // will be removed
        list_files_remove.add( info.getFile().toString() );
      }

      if ( returncode && add_result_filesname ) {
        // add this folder/file to result files name
        list_add_result.add( addFileNameString ); // was a NPE before with the file_name=null above in the finally
      }

      return returncode;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return ( traverseCount++ == 0 || include_subfolders );
    }

    public void shutdown() {
      if ( destinationFolderObject != null ) {
        try {
          destinationFolderObject.close();

        } catch ( IOException ex ) { /* Ignore */
        }
      }
    }
  }

  private class TextOneFileSelector implements FileSelector {
    String filename = null;
    String foldername = null;
    String destfolder = null;
    private int traverseCount;

    public TextOneFileSelector( String sourcefolderin, String sourcefilenamein, String destfolderin ) {
      if ( !Utils.isEmpty( sourcefilenamein ) ) {
        filename = sourcefilenamein;
      }

      if ( !Utils.isEmpty( sourcefolderin ) ) {
        foldername = sourcefolderin;
      }
      if ( !Utils.isEmpty( destfolderin ) ) {
        destfolder = destfolderin;
      }
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean resultat = false;
      String fil_name = null;

      try {

        if ( info.getFile().getType() == FileType.FILE ) {
          if ( info.getFile().getName().getBaseName().equals( filename )
            && ( info.getFile().getParent().toString().equals( foldername ) ) ) {
            // check if the file exists
            fil_name = destfolder + Const.FILE_SEPARATOR + filename;

            if ( KettleVFS.getFileObject( fil_name, JobEntryCopyFiles.this ).exists() ) {
              if ( isDetailed() ) {
                logDetailed( "      " + BaseMessages.getString( PKG, "JobCopyFiles.Log.FileExists", KettleVFS.getFriendlyURI( fil_name ) ) );
              }

              if ( overwrite_files ) {
                if ( isDetailed() ) {
                  logDetailed( "      "
                    + BaseMessages.getString(
                      PKG, "JobCopyFiles.Log.FileOverwrite", KettleVFS.getFriendlyURI( info.getFile() ), KettleVFS.getFriendlyURI( fil_name ) ) );
                }

                resultat = true;
              }

            } else {

              if ( isDetailed() ) {
                logDetailed( "      "
                  + BaseMessages.getString(
                    PKG, "JobCopyFiles.Log.FileCopied", KettleVFS.getFriendlyURI( info.getFile() ), KettleVFS.getFriendlyURI( fil_name ) ) );
              }

              resultat = true;
            }

          }

          if ( resultat && remove_source_files ) {
            // add this folder/file to remove files
            // This list will be fetched and all entries files
            // will be removed
            list_files_remove.add( info.getFile().toString() );
          }

          if ( resultat && add_result_filesname ) {
            // add this folder/file to result files name
            list_add_result.add( KettleVFS.getFileObject( fil_name, JobEntryCopyFiles.this ).toString() );
          }
        }

      } catch ( Exception e ) {

        logError( BaseMessages.getString( PKG, "JobCopyFiles.Error.Exception.CopyProcess", KettleVFS.getFriendlyURI( info
          .getFile() ), KettleVFS.getFriendlyURI( fil_name ), e.getMessage() ) );

        resultat = false;
      }

      return resultat;

    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return ( traverseCount++ == 0 || include_subfolders );
    }
  }

  public void setCopyEmptyFolders( boolean copy_empty_foldersin ) {
    this.copy_empty_folders = copy_empty_foldersin;
  }

  public boolean isCopyEmptyFolders() {
    return copy_empty_folders;
  }

  public void setoverwrite_files( boolean overwrite_filesin ) {
    this.overwrite_files = overwrite_filesin;
  }

  public boolean isoverwrite_files() {
    return overwrite_files;
  }

  public void setIncludeSubfolders( boolean include_subfoldersin ) {
    this.include_subfolders = include_subfoldersin;
  }

  public boolean isIncludeSubfolders() {
    return include_subfolders;
  }

  public void setAddresultfilesname( boolean add_result_filesnamein ) {
    this.add_result_filesname = add_result_filesnamein;
  }

  public boolean isAddresultfilesname() {
    return add_result_filesname;
  }

  public void setArgFromPrevious( boolean argfrompreviousin ) {
    this.arg_from_previous = argfrompreviousin;
  }

  public boolean isArgFromPrevious() {
    return arg_from_previous;
  }

  public void setRemoveSourceFiles( boolean remove_source_filesin ) {
    this.remove_source_files = remove_source_filesin;
  }

  public boolean isRemoveSourceFiles() {
    return remove_source_files;
  }

  public void setDestinationIsAFile( boolean destination_is_a_file ) {
    this.destination_is_a_file = destination_is_a_file;
  }

  public boolean isDestinationIsAFile() {
    return destination_is_a_file;
  }

  public void setCreateDestinationFolder( boolean create_destination_folder ) {
    this.create_destination_folder = create_destination_folder;
  }

  public boolean isCreateDestinationFolder() {
    return create_destination_folder;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    boolean res = JobEntryValidatorUtils.andValidator().validate( this, "arguments", remarks, AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    if ( res == false ) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notNullValidator(), JobEntryValidatorUtils.fileExistsValidator() );

    for ( int i = 0; i < source_filefolder.length; i++ ) {
      JobEntryValidatorUtils.andValidator().validate( this, "arguments[" + i + "]", remarks, ctx );
    }
  }

  public boolean evaluates() {
    return true;
  }

  public String loadURL( String url, String ncName, IMetaStore metastore, Map<String, String> mappings ) {
    if ( !Utils.isEmpty( ncName ) && !Utils.isEmpty( url ) ) {
      mappings.put( url, ncName );
    }
    return url;
  }

  public void setConfigurationMappings( Map<String, String> mappings ) {
    this.configurationMappings = mappings;
  }

  public String getConfigurationBy( String url ) {
    return this.configurationMappings.get( url );
  }

  public String getUrlPath( String incomingURL ) {
    String path = null;
    try {
      String noVariablesURL = incomingURL.replaceAll( "[${}]", "/" );
      FileName fileName = KettleVFS.getInstance().getFileSystemManager().resolveURI( noVariablesURL );
      String root = fileName.getRootURI();
      path = incomingURL.substring( root.length() - 1 );
    } catch ( FileSystemException e ) {
      path = null;
    }
    return path;
  }

}
