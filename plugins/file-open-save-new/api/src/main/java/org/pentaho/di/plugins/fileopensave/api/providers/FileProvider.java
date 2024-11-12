/*! ******************************************************************************
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


package org.pentaho.di.plugins.fileopensave.api.providers;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.ui.core.FileDialogOperation;

import java.io.InputStream;
import java.util.List;

/**
 * Created by bmorrise on 2/14/19.
 */
public interface FileProvider<T extends File> {

  Class<T> getFileClass();

  String getName();

  String getType();

  boolean isAvailable();

  /**
   * Get entire tree (no filter)
   *
   * @return
   */
  Tree getTree();

  /**
   * Optional method to get a filtered tree based on the connection type
   *
   * @param connectionTypes
   * @return filteredTree
   */
  default Tree getTree( List<String> connectionTypes ) {
    return getTree();
  }

  /**
   * Works kind of like a resolve.  It will return a mewly created file object if the file received exists physically,
   * or null if the file specified does not exist.
   *
   * @param file
   * @param filters
   * @param space
   * @return
   * @throws FileException
   */
  List<T> getFiles( T file, String filters, VariableSpace space ) throws FileException;

  List<T> searchFiles( T file, String filters, String searchString, VariableSpace space ) throws FileException;

  List<T> delete( List<T> files, VariableSpace space ) throws FileException;

  T add( T folder, VariableSpace space ) throws FileException;

  T getFile( T file, VariableSpace space );

  boolean fileExists( T dir, String path, VariableSpace space ) throws FileException;

  String getNewName( T destDir, String newPath, VariableSpace space ) throws FileException;

  String sanitizeName( T destDir, String newPath );

  boolean isSame( File file1, File file2 );

  T rename( T file, String newPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  T copy( T file, String toPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  T move( T file, String toPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  InputStream readFile( T file, VariableSpace space ) throws FileException;

  T writeFile( InputStream inputStream, T destDir, String path, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException, KettleFileException;

  T getParent( T file );

  void clearProviderCache();

  void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation );


  default T createDirectory( String parentPath, T file, String newDirectoryName )
    throws FileException, KettleFileException {
    throw new UnsupportedOperationException();
  }

  default File getFile( String path, boolean isDirectory ) {
    throw new UnsupportedOperationException();
  }
}
