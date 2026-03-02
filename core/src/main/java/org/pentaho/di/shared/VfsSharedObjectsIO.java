/*
 * ! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.shared;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.w3c.dom.Document;

/**
 * Provide methods to retrieve and save the shared objects defined in shared.xml that is stored in file system.
 *
 */
public class VfsSharedObjectsIO extends XmlFileSharedObjectsIO {

  private static final Class<?> PKG = VfsSharedObjectsIO.class;

  private final String sharedObjectsFile;
  private final Bowl bowl;

  /**
   * Creates the instance of VfsSharedObjectsIO using the default location of the shared file.
   *
   */
  public VfsSharedObjectsIO() {
    this( getDefaultSharedObjectFileLocation(), DefaultBowl.getInstance() );
  }

  public VfsSharedObjectsIO( String rootFolder, Bowl bowl ) {
    this.bowl = bowl;
    this.sharedObjectsFile = getSharedObjectFilePath( rootFolder );
  }

  @Override
  protected void loadSharedObjectsNodeMap() throws KettleXMLException {
    loadSharedObjectNodeMap( sharedObjectsFile );
  }

  @Override
  protected void saveToFile() throws KettleException {
    try {
      FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( sharedObjectsFile );
      Optional<String> backupFileName = createOrGetFileBackup( fileObject );
      writeToFile( fileObject, backupFileName );
    } catch ( IOException ex ) {
      throw new KettleException( ex );
    }
  }

  private void writeToFile( FileObject fileObject, Optional<String> backupFileName )
    throws IOException, KettleException {
    try ( OutputStream outputStream = KettleVFS.getInstance( bowl ).getOutputStream( fileObject, false ) ) {
      writeTo( outputStream );
    } catch ( Exception e ) {
      // restore file if something wrong
      boolean isRestored = false;
      if ( backupFileName.isPresent() ) {
        restoreFileFromBackup( backupFileName.get() );
        isRestored = true;
      }
      throw new KettleException( BaseMessages.getString( PKG, "SharedOjects.ErrorWritingFile", isRestored ), e );
    }
  }

  /**
   * Loads the shared objects in the map. The map will be of the form <String, Node>
   * where key can be {"connection", "slaveserver", "partitionschema" or clusterschema"} and
   * value will be xml Node.
   *
   * @param pathToSharedObjectFile The path to the shared object file
   * @throws KettleXMLException
   */
  private void loadSharedObjectNodeMap( String pathToSharedObjectFile ) throws KettleXMLException {

    try {
      // Get the FileObject
      FileObject file = KettleVFS.getInstance( bowl ).getFileObject( pathToSharedObjectFile );

      // If we have a shared file, load the content, otherwise, just keep this one empty
      if ( file.exists() ) {
        Document document = XMLHandler.loadXMLFile( file );
        loadSharedObjectsNodeMap( document );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "SharedOjects.ReadingError",
        pathToSharedObjectFile ), e );
    }
  }

  private Optional<String> createOrGetFileBackup( FileObject fileObject ) throws IOException, KettleException {
    String backupFileName = sharedObjectsFile + ".backup";
    boolean isBackupFileExist;
    if ( fileObject.exists() ) {
      isBackupFileExist = createFileBackup( backupFileName );
    } else {
      isBackupFileExist = getBackupFileFromFileSystem( backupFileName );
    }
    return isBackupFileExist ? Optional.ofNullable( backupFileName ) : Optional.empty();
  }

  private boolean createFileBackup( String backupFileName ) throws IOException, KettleFileException {
    return copyFile( sharedObjectsFile, backupFileName );
  }

  private boolean getBackupFileFromFileSystem( String backupFileName ) throws KettleException {
    FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( backupFileName );
    try {
      return fileObject.exists();
    } catch ( FileSystemException e ) {
      return false;
    }
  }

  private boolean copyFile( String src, String dest ) throws IOException, KettleFileException {
    IKettleVFS vfs = KettleVFS.getInstance( bowl );
    FileObject srcFile = vfs.getFileObject( src );
    FileObject destFile = vfs.getFileObject( dest );
    try ( InputStream in = KettleVFS.getInputStream( srcFile );
          OutputStream out = vfs.getOutputStream( destFile, false ) ) {
      IOUtils.copy( in, out );
    }
    return true;
  }

  private void restoreFileFromBackup( String backupFileName ) throws IOException, KettleFileException {
    copyFile( backupFileName, sharedObjectsFile );
  }

  private static final String getSharedObjectFilePath( String path ) {
    String filename = path;
    if ( Utils.isEmpty( path ) ) {
      filename = getDefaultSharedObjectFileLocation();
    } else if ( !path.endsWith( Const.SHARED_DATA_FILE ) ) {
      filename = path + File.separator + Const.SHARED_DATA_FILE;
    }
    return filename;
  }

  private static String getDefaultSharedObjectFileLocation() {
    String filename = Variables.getADefaultVariableSpace().getVariable( Const.KETTLE_SHARED_OBJECTS );
    if ( Utils.isEmpty( filename ) ) {
      filename = Const.getSharedObjectsFile();
    }
    return filename;
  }

}
