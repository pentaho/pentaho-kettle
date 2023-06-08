/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.cache;

import org.pentaho.di.plugins.fileopensave.api.providers.File;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileCache {
  private Map<File, List<File>> fileCache = new HashMap<>();

  public boolean containsKey( File file ) {
    return fileCache.containsKey( file );
  }

  public List<File> getFiles( File file ) {
    return fileCache.get( file );
  }

  public void setFiles( File file, List<File> files ) {
    fileCache.put( file, files );
  }

  public boolean removeFile( File parent, File file ) {
    if ( fileCache.containsKey( parent ) ) {
      fileCache.get( parent ).remove( file );
      return true;
    }
    return false;
  }

  public boolean addFile( File parent, File file ) {
    if ( fileCache.containsKey( parent ) && !fileCache.containsKey( file ) ) {
        fileCache.get( parent ).add( file );
    }
    return false;
  }

  public boolean move( File oldParent, File oldFile, File newParent, File newFile ) {
    return removeFile( oldParent, oldFile ) && addFile( newParent, newFile );
  }

  public boolean fileExists( File parent, String path ) {
    if ( fileCache.containsKey( parent ) ) {
      return fileCache.get( parent ).stream().anyMatch( file -> file.getPath().equals( path ) );
    }
    return false;
  }

  public boolean clear( File file ) {
    if ( fileCache.containsKey( file ) ) {
      fileCache.remove( file );
      return true;
    }
    return false;
  }

  public void clearAll() {
    fileCache.clear();
  }
}
