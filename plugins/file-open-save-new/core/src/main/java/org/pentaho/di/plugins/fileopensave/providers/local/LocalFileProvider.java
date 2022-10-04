/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2022 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
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

  @Override public Tree getTree() {
    return getTree( new ArrayList<>() );
  }

  /**
   * @param connectionTypes
   * @return
   */
  @Override public Tree getTree( List<String> connectionTypes ) {
    LocalTree localTree = new LocalTree( NAME );
    List<LocalFile> rootFiles = new ArrayList<>();
    ArrayList<Path> paths = new ArrayList<>();
    //TODO: Re-enable this when running on a snapshot build of spoon
    /*
    if ( Const.isRunningOnWebspoonMode() ) {
      Path kettleUserDataDirectoryPath = Paths.get( Const.getUserDataDirectory() );
      paths.add( kettleUserDataDirectoryPath );
    } else { */
    FileSystems.getDefault().getRootDirectories().forEach( paths::add );
    //}
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

  @Override public List<LocalFile> getFiles( LocalFile file, String filters, VariableSpace space )
    throws FileException {
    return getFiles( file, filters );
  }

  // TODO: Filter out certain files from root

  /**
   * @param file
   * @param filters
   * @return
   */
  public List<LocalFile> getFiles( LocalFile file, String filters ) {
    List<LocalFile> files = new ArrayList<>();
    //TODO: Re-enable this when running on a SNAPSHOT build of Spoon
    /*
    if ( Const.isRunningOnWebspoonMode() && !Paths.get( file.getPath() ).toAbsolutePath().startsWith( Paths.get(
    Const.getUserDataDirectory() ) ) ) {
      return files;
    }
    */
    try ( Stream<Path> paths = Files.list( Paths.get( file.getPath() ) ) ) {
      paths.forEach( path -> {
        String name = path.getFileName().toString();
        try {
          if ( path.toFile().isDirectory() ) {
            files.add( LocalDirectory.create( file.getPath(), path ) );
          } else if ( Utils.matches( name, filters ) ) {
            files.add( LocalFile.create( file.getPath(), path ) );
          }
        } catch ( Exception e ) {
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
   * @param space
   * @return
   */
  @Override
  public List<LocalFile> delete( List<LocalFile> files, VariableSpace space ) {
    List<LocalFile> deletedFiles = new ArrayList<>();
    for ( LocalFile file : files ) {
      try {
        // Changed deletion logic to java.io.File as java.nio.file.Files will delete only empty folders
        File indexFile = new File( file.getPath() );
        if(indexFile.isDirectory()) {
          deleteFolder(indexFile);
        }
        else {
          indexFile.delete();
        }
        deletedFiles.add( file );
      } catch ( Exception ignored ) {
        // Don't add file to deleted array
      }
    }
    return deletedFiles;
  }

  public void deleteFolder(File file){
    for (File subFile : file.listFiles()) {
      if(subFile.isDirectory()) {
        deleteFolder(subFile);
      } else {
        subFile.delete();
      }
    }
    file.delete();
  }

  /**
   * @param folder
   * @param space
   * @return
   */
  @Override
  public LocalFile add( LocalFile folder, VariableSpace space ) throws FileException {
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
  public LocalFile rename( LocalFile file, String newPath, boolean overwrite, VariableSpace space )
    throws FileException {
    return doMove( file.getPath(), newPath, overwrite, space );
  }

  /**
   * @param file
   * @param toPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile move( LocalFile file, String toPath, boolean overwrite, VariableSpace space ) throws FileException {
    return doMove( file.getPath(), toPath, overwrite, space );
  }

  /**
   * @param path
   * @param newPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  private LocalFile doMove( String path, String newPath, boolean overwrite, VariableSpace space ) throws FileException {
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
   * @param space
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile copy( LocalFile file, String toPath, boolean overwrite, VariableSpace space ) throws FileException {
    try {
      Path newPath = Paths.get( toPath );
      if ( file instanceof Directory ) {
        Files.walk( Paths.get( file.getPath() ) )
          .forEach( source -> {
            Path destination = Paths.get( toPath, source.toString()
              .substring( file.getPath().length() ) );
            try {
              Files.copy( source, destination );
            } catch ( IOException e ) {
              e.printStackTrace();
            }
          } );
      } else {
        newPath = Files.copy( Paths.get( file.getPath() ), Paths.get( toPath ), StandardCopyOption.REPLACE_EXISTING );
        return LocalFile.create( newPath.getParent().toString(), newPath );
      }
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
   * @param space
   * @return
   */
  @Override public boolean fileExists( LocalFile dir, String path, VariableSpace space ) {
    return Paths.get( path ).toFile().exists();
  }

  /**
   * @param file
   * @return
   */
  @Override
  public InputStream readFile( LocalFile file, VariableSpace space ) {
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
   * @param space
   * @return
   * @throws FileAlreadyExistsException
   */
  @Override
  public LocalFile writeFile( InputStream inputStream, LocalFile destDir, String path, boolean overwrite,
                              VariableSpace space )
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
  public String getNewName( LocalFile destDir, String newPath, VariableSpace space ) {
    String extension = Utils.getExtension( newPath );
    String parent = Utils.getParent( newPath, File.separator );
    String name = Utils.getName( newPath, File.separator ).replace( "." + extension, "" );
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

  @Override public LocalFile createDirectory( String parentPath, LocalFile file, String newFolderName )
    throws FileException {
    LocalFile newLocalFile;
    if ( file instanceof Directory ) {
      newLocalFile = LocalFile.create( parentPath,
        Paths.get( file.getPath() + FileSystems.getDefault().getSeparator() + newFolderName ) );
    } else {
      newLocalFile = LocalFile.create( parentPath,
        Paths.get( file.getParent() + FileSystems.getDefault().getSeparator() + newFolderName ) );
    }
    return this.add( newLocalFile, null );
  }

  @Override public LocalFile getFile( LocalFile file, VariableSpace space ) {
    return null;
  }
}
