/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.providers.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.File;

/**
 * Note there is no way to rename files on the fly using the FileObject.copyFrom method.  If we don't use it then it
 * will probably slow down the copy, therefore only overwrite, skip, cancel should be allowed and it will carry for the
 * whole copy.
 */
public class OverwriteAwareFileSelector implements FileSelector {
  OverwriteStatus overwriteStatus;
  FileObject copyFrom;
  FileObject copyTo;
  String connection; //The VFS connection name
  VariableSpace space;

  OverwriteAwareFileSelector( OverwriteStatus overwriteStatus, FileObject copyFrom, FileObject copyTo,
                              String connection, VariableSpace space ) {
    this.overwriteStatus = overwriteStatus;
    this.copyFrom = copyFrom;
    this.copyTo = copyTo;
    this.connection = connection;
    this.space = space;
  }

  @Override public boolean includeFile( FileSelectInfo fileInfo ) throws Exception {
    return promptIfDuplicated( fileInfo );
  }

  //This will fire for each folder along the way
  @Override public boolean traverseDescendents( FileSelectInfo fileInfo ) throws Exception {
    return promptIfDuplicated( fileInfo );
  }

  private boolean promptIfDuplicated( FileSelectInfo fileInfo ) throws FileSystemException, KettleFileException {
    String destinationFile = convertFileInfoToOutputFile( fileInfo );
    overwriteStatus.setCurrentFileInProgressDialog( destinationFile );
    FileObject destinationFileObject = KettleVFS
      .getFileObject( destinationFile, space, VFSHelper.getOpts( destinationFile, connection, space ) );
    if ( fileInfo.getFile().equals( copyFrom ) ) {
      // If the file being worked is the original source then we already answered the overwrite question before the
      // copy started.  So if this file is a duplicate we already said to overwrite it, or we wouldn't be here.
      overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.OVERWRITE );
    } else {
      overwriteStatus.promptOverwriteIfNecessary( destinationFileObject.exists(), destinationFile, "folder",
        new OverwriteStatus.OverwriteMode[] { OverwriteStatus.OverwriteMode.RENAME },
        "Note: rename is not available." );
    }
    if ( overwriteStatus.isCancel() ) {
      // We have to throw an exception or it will keep going through the tree
      throw new KettleFileException( "Aborted by user." );
    }
    if ( overwriteStatus.isSkip() ) {
      return false;
    }
    if ( overwriteStatus.isRename() ) {
      //should not happen
      throw new IllegalArgumentException(
        "Sorry, can not rename on the fly while copying in the same VFS Connection." );
    }
    return true;
  }

  private String convertFileInfoToOutputFile( FileSelectInfo fileInfo ) {
    String sourceFile = fileInfo.getFile().getName().toString();
    if ( !sourceFile.startsWith( copyFrom.getName().toString() ) ) {
      throw new IllegalArgumentException( "The incoming file did not start with the source path" );
    }
    return copyTo.getName().toString() + sourceFile.substring( copyFrom.getName().toString().length() );
  }
}
