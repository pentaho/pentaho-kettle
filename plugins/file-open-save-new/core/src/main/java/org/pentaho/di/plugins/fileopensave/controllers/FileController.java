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

import org.pentaho.di.core.util.Utils;

import org.pentaho.di.plugins.fileopensave.providers.ProviderService;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 2/13/19.
 */
public class FileController {

  protected final FileCache fileCache;
  private final ProviderService providerService;

  public FileController( FileCache fileCache, ProviderService providerService ) {
    this.fileCache = fileCache;
    this.providerService = providerService;
  }

  public boolean clearCache( File file ) {
    boolean isCleared = fileCache.clear( file );
    try {
      FileProvider fileProvider = providerService.get( file.getProvider() );
      if ( fileProvider != null ) {
        fileProvider.clearProviderCache();
      }
    } catch ( InvalidFileProviderException ignored ) {
      // ignored
    }
    return isCleared;
  }

  List<Tree> load() {
    return load( null, null );
  }

  public List<Tree> load( String filter ) {
    return load( filter, new ArrayList<>() );
  }

  public List<Tree> load( String filter, List<String> connectionTypes ) {
    List<Tree> trees = new ArrayList<>();
    List<String> filters = Utils.isEmpty( filter ) || filter.equalsIgnoreCase( ProviderFilterType.DEFAULT.toString() )
      ? Arrays.asList( ProviderFilterType.getDefaults() ) : Arrays.asList( filter.split( "[,]" ) );
    // If there are no filters or default filter, use default list of providers. Else load only providers found in
    // filter
    if ( filters.contains( ProviderFilterType.ALL_PROVIDERS.toString() ) ) {
      for ( FileProvider fileProvider : providerService.get() ) {
        if ( fileProvider.isAvailable() ) {
          trees.add( fileProvider.getTree( connectionTypes ) );
        }
      }
    } else {
      for ( FileProvider fileProvider : providerService.get() ) {
        if ( fileProvider.isAvailable() && filters.contains( fileProvider.getType() ) ) {
          trees.add( fileProvider.getTree( connectionTypes ) );
        }
      }
    }
    return trees;
  }

  public List<File> getFiles( File file, String filters, boolean useCache ) throws FileException {
    try {
      FileProvider<File> fileProvider = providerService.get( file.getProvider() );
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
      FileProvider<File> fileProvider = providerService.get( dir.getProvider() );
      return fileProvider.fileExists( dir, path );
    } catch ( InvalidFileProviderException | FileException e ) {
      return false;
    }
  }

  public Result getNewName( File destDir, String newPath ) {
    try {
      FileProvider<File> fileProvider = providerService.get( destDir.getProvider() );
      return Result.success( "", fileProvider.getNewName( destDir, newPath ) );
    } catch ( InvalidFileProviderException | FileException e ) {
      return null;
    }
  }

  public Result delete( List<File> files ) {
    try {
      FileProvider<File> fileProvider = providerService.get( files.get( 0 ).getProvider() );
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
      FileProvider<File> fileProvider = providerService.get( folder.getProvider() );
      File newFile = fileProvider.add( folder );
      if ( newFile != null ) {
        fileCache.addFile( fileProvider.getParent( folder ), newFile );
        return Result.success( "", newFile );
      } else {
        return Result.error( "Unable to create folder", folder );
      }
    } catch ( FileExistsException fee ) {
      return Result.fileCollision( "", folder );
    } catch ( FileException | InvalidFileProviderException fe ) {
      return Result.error( "", folder );
    }
  }

  public Result rename( File file, String newPath, boolean overwrite ) {
    try {
      FileProvider<File> fileProvider = providerService.get( file.getProvider() );
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
      FileProvider<File> fileProvider = providerService.get( file.getProvider() );
      File newFile;
      if ( fileProvider.isSame( file, destDir ) ) {
        newFile = fileProvider.move( file, newPath, overwrite );
      } else {
        newFile = moveBetweenProviders( file, destDir, newPath, overwrite );
      }
      if ( newFile != null ) {
        FileProvider newFileProvider = providerService.get( newFile.getProvider() );
        fileCache.move( fileProvider.getParent( file ), file, newFileProvider.getParent( newFile ), newFile );
        return Result.success( "Move file complete", newFile );
      }
    } catch ( InvalidFileProviderException | FileException e ) {
      return Result.error( "Unable to move file", file );
    }
    return Result.error( "Unable to move file", file );
  }

  public File getFile( File file ) {
    try {
      FileProvider<File> fileProvider = providerService.get( file.getProvider() );
      return fileProvider.getFile( file );
    } catch ( InvalidFileProviderException e ) {
      return null;
    }
  }

  public Result copyFile( File file, File destDir, String path, Boolean overwrite ) {
    try {
      FileProvider<File> fileProvider = providerService.get( file.getProvider() );
      File newFile;
      if ( fileProvider.isSame( file, destDir ) ) {
        newFile = fileProvider.copy( file, path, overwrite );
      } else {
        newFile = copyFileBetweenProviders( file, destDir, path, overwrite );
      }
      if ( newFile != null ) {
        FileProvider newFileProvider = providerService.get( newFile.getProvider() );
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
      FileProvider<File> fromFileProvider = providerService.get( file.getProvider() );
      FileProvider<File> toFileProvider = providerService.get( destDir.getProvider() );
      path = toFileProvider.sanitizeName( destDir, path );
      writeFile( fromFileProvider, toFileProvider, file, destDir, path, overwrite );
    } catch ( InvalidFileProviderException | FileException ignored ) {
      // Don't add it to the list
    }
    return null;
  }

  private File writeFile( FileProvider<File> fromFileProvider, FileProvider<File> toFileProvider, File file,
                          File destDir, String path, boolean overwrite ) throws FileException {
    try ( InputStream inputStream = fromFileProvider.readFile( file ) ) {
      return toFileProvider.writeFile( inputStream, destDir, path, overwrite );
    } catch ( IOException e ) {
      return null;
    }
  }

  public File moveBetweenProviders( File file, File destDir, String path, boolean overwrite ) {
    File newFile = copyFileBetweenProviders( file, destDir, path, overwrite );
    if ( newFile != null ) {
      delete( Collections.singletonList( file ) );
    }
    return newFile;
  }
}
