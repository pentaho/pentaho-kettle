/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.local;

import org.pentaho.di.core.Const;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileExistsException;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalDirectory;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalTree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by bmorrise on 2/16/19.
 */
public class LocalFileProvider extends BaseFileProvider<LocalFile> {

  public static final String NAME = "Local";
  public static final String TYPE = "local";

  @Override public Class<LocalFile> getFileClass() {
    return LocalFile.class;
  }

  /**
   * @return
   */
  @Override public String getName() {
    return NAME;
  }

  /**
   * @return
   */
  @Override public String getType() {
    return TYPE;
  }

  /**
   * @return
   */
  @Override public Tree getTree() {
    LocalTree localTree = new LocalTree( NAME );
    List<LocalFile> rootFiles = new ArrayList<>();
    ArrayList<Path> paths = new ArrayList<>();
    if ( Const.isRunningOnWebspoonMode() ) {
      Path kettleUserDataDirectoryPath = Paths.get( Const.getUserDataDirectory() );
      paths.add( kettleUserDataDirectoryPath );
    } else {
      FileSystems.getDefault().getRootDirectories().forEach( paths::add );
    }
    paths.forEach( path -> {
      LocalDirectory localDirectory = new LocalDirectory();
      localDirectory.setPath( path.toString() );
      localDirectory.setName( path.toString() );
      localDirectory.setRoot( NAME );
      localDirectory.setHasChildren( true );
      rootFiles.add( localDirectory );
    } );
    localTree.setFiles( rootFiles );

    return localTree;
  }

  // TODO: Filter out certain files from root
  /**
   * @param file
   * @param filters
   * @return
   */
  public List<LocalFile> getFiles( LocalFile file, String filters ) {
    List<LocalFile> files = new ArrayList<>();
    if ( Const.isRunningOnWebspoonMode() && !Paths.get( file.getPath() ).toAbsolutePath().startsWith( Paths.get( Const.getUserDataDirectory() ) ) ) {
      return files;
    }
    try ( Stream<Path> paths = Files.list( Paths.get( file.getPath() ) ) ) {
      paths.forEach( path -> {
        String name = path.getFileName().toString();
        try {
          if ( path.toFile().isDirectory() && !Files.isHidden( path ) ) {
            files.add( LocalDirectory.create( file.getPath(), path ) );
          } else if ( !Files.isHidden( path ) && Utils.matches( name, filters ) ) {
            files.add( LocalFile.create( file.getPath(), path ) );
          }
        } catch ( IOException e ) {
          // Do nothing yet
        }
      } );
    } catch ( IOException e ) {
      // Do nothing yet
    }
    return files;
  }

  //TODO: Handle directories with files in them
  /**
   * @return
   */
  @Override public boolean isAvailable() {
    return true;
  }

  /**
   * @param files
   * @return
   */
  public List<LocalFile> delete( List<LocalFile> files ) {
    List<LocalFile> deletedFiles = new ArrayList<>();
    for ( LocalFile file : files ) {
      try {
        Files.delete( Paths.get( file.getPath() ) );
        deletedFiles.add( file );
      } catch ( IOException ignored ) {
        // Don't add file to deleted array
      }
    }
    return deletedFiles;
  }

  /**
   * @param folder
   * @return
   */
  @Override
  public LocalFile add( LocalFile folder ) throws FileException {
    Path folderPath = Paths.get( folder.getPath() );
    if ( folderPath.toFile().exists() ) {
      throw new FileExistsException();
    }
    try {
      Path newPath = Files.createDirectories( Paths.get( folder.getPath() ) );
      LocalDirectory localDirectory = new LocalDirectory();
      localDirectory.setName( newPath.getFileName().toString() );
      localDirectory.setPath( newPath.getFileName().toString() );

      localDirectory.setDate( new Date( Files.getLastModifiedTime( newPath ).toMillis() ) );
      localDirectory.setRoot( NAME );
      localDirectory.setCanAddChildren( true );
      localDirectory.setCanEdit( true );

      return localDirectory;
    } catch ( IOException e ) {
      return null;
    }
  }

  /**
   * @param file
   * @param newPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile rename( LocalFile file, String newPath, boolean overwrite ) throws FileException {
    return doMove( file.getPath(), newPath, overwrite );
  }

  /**
   * @param file
   * @param toPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile move( LocalFile file, String toPath, boolean overwrite ) throws FileException {
    return doMove( file.getPath(), toPath, overwrite );
  }

  /**
   * @param path
   * @param newPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  private LocalFile doMove( String path, String newPath, boolean overwrite ) throws FileException {
    try {
      Path movePath;
      if ( overwrite ) {
        movePath = Files.move( Paths.get( path ), Paths.get( newPath ), StandardCopyOption.REPLACE_EXISTING );
      } else {
        movePath = Files.move( Paths.get( path ), Paths.get( newPath ) );
      }
      if ( Paths.get( path ).toFile().isDirectory() ) {
        return LocalDirectory.create( movePath.getParent().toString(), movePath );
      } else {
        return LocalFile.create( movePath.getParent().toString(), movePath );
      }
    } catch ( IOException e ) {
      return null;
    }
  }

  /**
   * @param file
   * @param toPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile copy( LocalFile file, String toPath, boolean overwrite ) throws FileException {
    try {
      Path newPath = Files.copy( Paths.get( file.getPath() ), Paths.get( toPath ), StandardCopyOption.REPLACE_EXISTING );
      if ( newPath.toFile().isDirectory() ) {
        return LocalDirectory.create( newPath.getParent().toString(), newPath );
      } else {
        return LocalFile.create( newPath.getParent().toString(), newPath );
      }
    } catch ( IOException e ) {
      return null;
    }
  }

  /**
   * @param dir
   * @param path
   * @return
   */
  @Override public boolean fileExists( LocalFile dir, String path ) {
    return Paths.get( path ).toFile().exists();
  }

  /**
   * @param file
   * @return
   */
  @Override
  public InputStream readFile( LocalFile file ) {
    try {
      return new BufferedInputStream( new FileInputStream( new File( file.getPath() ) ) );
    } catch ( FileNotFoundException e ) {
      return null;
    }
  }

  /**
   * @param inputStream
   * @param destDir
   * @param path
   * @param overwrite
   * @return
   * @throws FileAlreadyExistsException
   */
  @Override
  public LocalFile writeFile( InputStream inputStream, LocalFile destDir, String path, boolean overwrite )
    throws FileException {
    try {
      Files.copy( inputStream, Paths.get( path ) );
      return LocalFile.create( destDir.getPath(), Paths.get( path ) );
    } catch ( FileAlreadyExistsException e ) {
      throw new FileExistsException();
    } catch ( IOException e ) {
      return null;
    }
  }

  /**
   * @param file1
   * @param file2
   * @return
   */
  @Override
  public boolean isSame( org.pentaho.di.plugins.fileopensave.api.providers.File file1,
                         org.pentaho.di.plugins.fileopensave.api.providers.File file2 ) {
    return file1.getProvider().equals( file2.getProvider() );
  }

  /**
   * @param destDir
   * @param newPath
   * @return
   */
  @Override
  public String getNewName( LocalFile destDir, String newPath ) {
    String extension = Utils.getExtension( newPath );
    String parent = Utils.getParent( newPath );
    String name = Utils.getName( newPath ).replace( "." + extension, "" );
    int i = 1;
    String testName = newPath;
    while ( Paths.get( testName ).toFile().exists() ) {
      if ( Utils.isValidExtension( extension ) ) {
        testName = parent + name + " " + i + "." + extension;
      } else {
        testName = newPath + " " + i;
      }
      i++;
    }
    return testName;
  }

  @Override public LocalFile getParent( LocalFile file ) {
    return null;
  }

  public void clearProviderCache() {
    //Any local caches that this provider might use should be cleared here.
  }

  @Override public LocalFile getFile( LocalFile file ) {
    return null;
  }
}
