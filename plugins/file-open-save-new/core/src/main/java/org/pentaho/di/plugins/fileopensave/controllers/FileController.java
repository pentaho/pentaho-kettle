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

package org.pentaho.di.plugins.fileopensave.controllers;

import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.FileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Result;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileExistsException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileProviderException;
import org.pentaho.di.plugins.fileopensave.cache.FileCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 2/13/19.
 */
// TODO: Possible refactor into individual controllers that can be extended to remove "type" functionality
public class FileController {

  protected final FileCache fileCache;
  private final List<FileProvider> fileProviders;

  public FileController( FileCache fileCache, List<FileProvider> fileProviders ) {
    this.fileCache = fileCache;
    this.fileProviders = fileProviders;
  }

  public boolean clearCache( File file ) {
    return fileCache.clear( file );
  }

  public FileProvider getFileProvider( String provider ) throws InvalidFileProviderException {
    return fileProviders.stream().filter( fileProvider1 ->
      fileProvider1.getType().equalsIgnoreCase( provider ) && fileProvider1.isAvailable() )
      .findFirst()
      .orElseThrow( InvalidFileProviderException::new );
  }

  public List<Tree> load() {
    List<Tree> trees = new ArrayList<>();
    for ( FileProvider fileProvider : fileProviders ) {
      if ( fileProvider.isAvailable() ) {
        trees.add( fileProvider.getTree() );
      }
    }
    return trees;
  }

  // TODO: Make cache account for filters
  public List<File> getFiles( File file, String filters, Boolean useCache ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( file.getProvider() );
      if ( fileCache.containsKey( file ) && useCache ) {
        return fileCache.getFiles( file );
      } else {
        List<File> files = fileProvider.getFiles( file, filters );
        fileCache.setFiles( file, files );
        return files;
      }
    } catch ( InvalidFileProviderException e ) {
      return Collections.emptyList();
    }
  }

  public Boolean fileExists( File dir, String path ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( dir.getProvider() );
      return fileProvider.fileExists( dir, path );
    } catch ( InvalidFileProviderException | FileException e ) {
      return null;
    }
  }

  public Result getNewName( File destDir, String newPath ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( destDir.getProvider() );
      return Result.success( "", fileProvider.getNewName( destDir, newPath ) );
    } catch ( InvalidFileProviderException | FileException e ) {
      return null;
    }
  }

  public Result delete( List<File> files ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( files.get( 0 ).getProvider() );
      List<File> deletedFiles = fileProvider.delete( files );
      for ( File file : deletedFiles ) {
        fileCache.removeFile( fileProvider.getParent( file ), file );
      }
      return Result.success( "", deletedFiles );
    } catch ( InvalidFileProviderException | FileException e ) {
      return null;
    }
  }

  public Result add( File folder ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( folder.getProvider() );
      File newFile = fileProvider.add( folder );
      if ( newFile != null ) {
        fileCache.addFile( fileProvider.getParent( folder ), newFile );
      }
      return Result.success( "", newFile );
    } catch ( FileExistsException fee ) {
      return Result.fileCollision( "", folder );
    } catch ( FileException | InvalidFileProviderException fe ) {
      return Result.error( "", folder );
    }
  }

  public Result rename( File file, String newPath, boolean overwrite ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( file.getProvider() );
      File newFile = fileProvider.rename( file, newPath, overwrite );
      if ( newFile != null ) {
        fileCache.move( fileProvider.getParent( file ), file, fileProvider.getParent( newFile ), newFile );
      }
      return Result.success( "", newFile );
    } catch ( InvalidFileProviderException | FileException e ) {
      return null;
    }
  }

  public Result moveFile( File file, File destDir, String newPath, boolean overwrite ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( file.getProvider() );
      File newFile;
      if ( fileProvider.isSame( file, destDir ) ) {
        newFile = fileProvider.move( file, newPath, overwrite );
      } else {
        newFile = moveBetweenProviders( file, destDir, newPath, overwrite );
      }
      if ( newFile != null ) {
        FileProvider newFileProvider = getFileProvider( newFile.getProvider() );
        fileCache.move( fileProvider.getParent( file ), file, newFileProvider.getParent( newFile ), newFile );
        return Result.success( "Move file complete", newFile );
      }
    } catch ( InvalidFileProviderException | FileException e ) {
      return Result.error( "Unable to move file", file );
    }
    return Result.error( "Unable to move file", file );
  }

  public Result copyFile( File file, File destDir, String path, Boolean overwrite ) {
    try {
      FileProvider<File> fileProvider = getFileProvider( file.getProvider() );
      File newFile;
      if ( fileProvider.isSame( file, destDir ) ) {
        newFile = fileProvider.copy( file, path, overwrite );
      } else {
        newFile = copyFileBetweenProviders( file, destDir, path, overwrite );
      }
      if ( newFile != null ) {
        FileProvider newFileProvider = getFileProvider( newFile.getProvider() );
        fileCache.addFile( newFileProvider.getParent( newFile ), newFile );
        return Result.success( "Copy file complete", newFile );
      }
    } catch ( InvalidFileProviderException | FileException e ) {
      return Result.error( "Unable to copy file", file );
    }
    return Result.error( "Unable to copy file", file );
  }

  public File copyFileBetweenProviders( File file, File destDir, String path, boolean overwrite ) {
    try {
      FileProvider<File> fromFileProvider = getFileProvider( file.getProvider() );
      FileProvider<File> toFileProvider = getFileProvider( destDir.getProvider() );
      path = toFileProvider.sanitizeName( destDir, path );
      try ( InputStream inputStream = fromFileProvider.readFile( file ) ) {
        return toFileProvider.writeFile( inputStream, destDir, path, overwrite );
      } catch ( IOException e ) {
        return null;
      }
    } catch ( InvalidFileProviderException | FileException ignored ) {
      // Don't add it to the list
    }
    return null;
  }

  public File moveBetweenProviders( File file, File destDir, String path, boolean overwrite ) {
    File newFile = copyFileBetweenProviders( file, destDir, path, overwrite );
    if ( newFile != null ) {
      delete( Collections.singletonList( file ) );
    }
    return newFile;
  }
}
