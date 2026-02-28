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
        return true;
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
