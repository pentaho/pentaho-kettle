/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.api.providers;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
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
  Tree getTree( Bowl bowl );

  /**
   * Optional method to get a filtered tree based on the connection type
   *
   * @param connectionTypes
   * @return filteredTree
   */
  default Tree getTree( Bowl bowl, List<String> connectionTypes ) {
    return getTree( bowl );
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
  List<T> getFiles( Bowl bowl, T file, String filters, VariableSpace space ) throws FileException;

  List<T> searchFiles( Bowl bowl, T file, String filters, String searchString, VariableSpace space ) throws FileException;

  List<T> delete( Bowl bowl, List<T> files, VariableSpace space ) throws FileException;

  T add( Bowl bowl, T folder, VariableSpace space ) throws FileException;

  T getFile( Bowl bowl, T file, VariableSpace space );

  boolean fileExists( Bowl bowl, T dir, String path, VariableSpace space ) throws FileException;

  String getNewName( Bowl bowl, T destDir, String newPath, VariableSpace space ) throws FileException;

  String sanitizeName( Bowl bowl, T destDir, String newPath );

  boolean isSame( Bowl bowl, File file1, File file2 );

  T rename( Bowl bowl, T file, String newPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  T copy( Bowl bowl, T file, String toPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  T move( Bowl bowl, T file, String toPath, OverwriteStatus overwrite, VariableSpace space ) throws FileException;

  InputStream readFile( Bowl bowl, T file, VariableSpace space ) throws FileException;

  T writeFile( Bowl bowl, InputStream inputStream, T destDir, String path, OverwriteStatus overwriteStatus,
               VariableSpace space ) throws FileException, KettleFileException;

  T getParent( Bowl bowl, T file );

  void clearProviderCache();

  void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation );


  default T createDirectory( Bowl bowl, String parentPath, T file, String newDirectoryName )
    throws FileException, KettleFileException {
    throw new UnsupportedOperationException();
  }

  default File getFile( Bowl bowl, String path, boolean isDirectory ) {
    throw new UnsupportedOperationException();
  }
}
