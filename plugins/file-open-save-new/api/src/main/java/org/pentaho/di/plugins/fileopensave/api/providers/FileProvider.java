/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
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

  List<T> getFiles( T file, String filters ) throws FileException;

  List<T> delete( List<T> files ) throws FileException;

  T add( T folder ) throws FileException;

  T getFile( T file );

  boolean fileExists( T dir, String path ) throws FileException;

  String getNewName( T destDir, String newPath ) throws FileException;

  String sanitizeName( T destDir, String newPath );

  boolean isSame( File file1, File file2 );

  T rename( T file, String newPath, boolean overwrite ) throws FileException;

  T copy( T file, String toPath, boolean overwrite ) throws FileException;

  T move( T file, String toPath, boolean overwrite ) throws FileException;

  InputStream readFile( T file ) throws FileException;

  T writeFile( InputStream inputStream, T destDir, String path, boolean overwrite ) throws FileException;

  T getParent( T file );

  void clearProviderCache();

  void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation );
}
