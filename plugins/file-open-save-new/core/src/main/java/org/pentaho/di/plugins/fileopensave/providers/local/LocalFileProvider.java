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

package org.pentaho.di.plugins.fileopensave.providers.local;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileExistsException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileTypeException;
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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  @Override
  public List<LocalFile> searchFiles( LocalFile file, String filters, String searchString, VariableSpace space ) {

    final List<LocalFile> files = new ArrayList<>();
    FileVisitor<Path> visitor = new FileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
        return Files.isReadable( dir ) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE ;
      }

      @Override
      public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
        if ( Files.isReadable( file ) ) {
          addReadableFiles( files, file, searchString, filters );
          return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
      }

      @Override
      public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
      }

      @Override
      public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
        if ( Files.isReadable( dir ) ) {
          addReadableFiles( files, dir, searchString, filters );
          return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
      }
    };

    try {
      Files.walkFileTree( Paths.get( file.getPath() ), visitor );
    } catch ( IOException e ) {
      return Collections.emptyList();
    }

    return files;
  }

  private void addReadableFiles( List<LocalFile> files, Path path, String searchString, String filters ) {
    if( path.getFileName() != null && Files.isReadable( path ) ){
      String name = path.getFileName().toString();
      if ( Utils.matches( name, searchString ) || name.toLowerCase().contains( searchString.toLowerCase() ) ) {
        if ( path.toFile().isDirectory() ) {
          files.add( LocalDirectory.create( Utils.getParent( path.toString(), FileSystems.getDefault().getSeparator() ), path ) );
        } else if ( Utils.matches( name, filters ) ) {
          files.add( LocalFile.create( Utils.getParent( path.toString(), FileSystems.getDefault().getSeparator() ), path ) );
        }
      }
    }
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
        if ( indexFile.isDirectory() ) {
          deleteFolder( indexFile );
        } else {
          indexFile.delete();
        }
        deletedFiles.add( file );
      } catch ( Exception ignored ) {
        // Don't add file to deleted array
      }
    }
    return deletedFiles;
  }

  public void deleteFolder( File file ) {
    for ( File subFile : file.listFiles() ) {
      if ( subFile.isDirectory() ) {
        deleteFolder( subFile );
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
      localDirectory.setPath( newPath.toString() );

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
   * @param overwriteStatus
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile rename( LocalFile file, String newPath, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    return doMove( file.getPath(), newPath, overwriteStatus, space );
  }

  /**
   * @param file
   * @param toPath
   * @param overwriteStatus
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile move( LocalFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    return doMove( file.getPath(), toPath, overwriteStatus, space );
  }

  /**
   * @param path
   * @param newPath
   * @param overwriteStatus
   * @return
   * @throws FileException
   */
  @SuppressWarnings( "squid:S01172" )
  private LocalFile doMove( String path, String newPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    try {
      Path movePath;
      //Should not be opening the dialog here
      overwriteStatus.promptOverwriteIfNecessary( "Any duplicate files/folders encountered", "Files/Folders" );
      if ( overwriteStatus.isOverwrite() ) {
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
   * @param overwriteStatus
   * @param space
   * @return
   * @throws FileException
   */
  @Override
  public LocalFile copy( LocalFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    try {
      Path newPath = Paths.get( toPath );
      if ( file instanceof Directory ) {
        Files.walkFileTree( Paths.get( file.getPath() ), new LocalFileVisitor( file, toPath, overwriteStatus ) );
      } else {
        newPath = singleFileCopy( Paths.get( file.getPath() ), Paths.get( toPath ), overwriteStatus );
        if ( newPath == null ) {
          // If here it is likely the user hit cancel or skip
          return null;
        }
        return LocalFile.create( newPath.getParent().toString(), newPath );
      }
      if ( newPath.toFile().isDirectory() ) {
        return LocalDirectory.create( newPath.getParent().toString(), newPath );
      } else {
        return LocalFile.create( newPath.getParent().toString(), newPath );
      }
    } catch ( IOException e ) {
      e.printStackTrace();
      return null;
    }
  }

  private Path singleFileCopy( Path source, Path destination, OverwriteStatus overwriteStatus ) {
    try {
      StandardCopyOption sco = null;
      overwriteStatus.promptOverwriteIfNecessary( destination.toFile().exists(), destination.toString(), "file" );
      if ( overwriteStatus.isOverwrite() ) {
        sco = StandardCopyOption.REPLACE_EXISTING;
      } else if ( overwriteStatus.isCancel() || overwriteStatus.isSkip() ) {
        return null;
      } else if ( overwriteStatus.isRename() ) {
        LocalFile parentDir =
          LocalFile.create( destination.getParent().getParent().toString(), destination.getParent() );
        destination = Paths.get( getNewName( parentDir, destination.toString(), new Variables() ) );
      }
      return sco == null ? Files.copy( source, destination ) : Files.copy( source, destination, sco );
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
   * @param overwriteStatus
   * @param space
   * @return
   * @throws FileAlreadyExistsException
   */
  @Override
  public LocalFile writeFile( InputStream inputStream, LocalFile destDir, String path, OverwriteStatus overwriteStatus,
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
    for( Path path : FileSystems.getDefault().getRootDirectories() ){
      if( file.getParent() == null
              || path.toString().equals( file.getParent() )
              || path.toString().equals( file.getParent() + FileSystems.getDefault().getSeparator() ) )
        return null;
    }
    return LocalDirectory.create( Utils.getParent( file.getParent(), FileSystems.getDefault().getSeparator() ),
            Paths.get( file.getParent() ) );
  }

  public void clearProviderCache() {
    //Any local caches that this provider might use should be cleared here.
  }

  //Should do nothing if the directory already exists, or create any directories that do not exist
  @Override public LocalFile createDirectory( String parentPath, LocalFile file, String newFolderName )
    throws FileException {
    LocalDirectory newLocalDirectory;
    if ( file instanceof Directory ) {
        newLocalDirectory = LocalDirectory.create( parentPath,
          Paths.get( file.getPath() + FileSystems.getDefault().getSeparator() + newFolderName ) );
    } else {
      throw new InvalidFileTypeException( "Illegal attempt to create directory under a file" );
    }
    try {
      return this.add( newLocalDirectory, null );
    } catch ( FileExistsException e ) {
      //The file already exists.  Suppress the error
    }
    return newLocalDirectory;
  }

  @Override public LocalFile getFile( LocalFile file, VariableSpace space ) {
     Paths.get( file.getPath() );
    return null;
  }

  public class LocalFileVisitor extends SimpleFileVisitor<Path> {
    private final OverwriteStatus overwriteStatus;
    private Map<Path, Path> folderTransversedMap = new HashMap<>();

    public LocalFileVisitor ( LocalFile originalSourceFile, String toPath, OverwriteStatus overwriteStatus ) {
      super();
      this.overwriteStatus = overwriteStatus;
      folderTransversedMap.put( Paths.get( originalSourceFile.getPath() ), Paths.get( toPath ) ); //seed the map
    }

    @Override
    public FileVisitResult preVisitDirectory( Path source, BasicFileAttributes attrs ) throws IOException {
      Objects.requireNonNull( source );
      Objects.requireNonNull( attrs );
      Path destination = convertSourceToDestination( source );
      overwriteStatus.setCurrentFileInProgressDialog( source.toString() );
      //Even if we do not have a duplicate we have to make this call to reset the mode, if not apply to all
      overwriteStatus.promptOverwriteIfNecessary( destination.toFile().exists(), destination.toString(), "folder" );
      if ( overwriteStatus.isCancel() ) {
        return FileVisitResult.TERMINATE;
      }
      if ( overwriteStatus.isSkip() ) {
        return FileVisitResult.SKIP_SUBTREE;
      }
      if ( overwriteStatus.isRename() ) {
        LocalFile parentDir = LocalFile.create( destination.getParent().toString(), destination );
        Path newDestination = Paths.get( getNewName( parentDir, destination.toString(), new Variables() ) );
        newDestination.toFile().mkdirs();
        folderTransversedMap.put( source, newDestination ); //We changed the destination folder, update the map
        return FileVisitResult.CONTINUE;
      }
      destination.toFile().mkdirs();
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile( Path source, BasicFileAttributes attrs )
      throws IOException {
      Objects.requireNonNull( source );
      Objects.requireNonNull( attrs );
      overwriteStatus.setCurrentFileInProgressDialog( source.toString() );

      Path nPath = singleFileCopy( source, convertSourceToDestination( source ), overwriteStatus );
      if ( nPath == null ) {
        if ( overwriteStatus.isCancel() ) {
          return FileVisitResult.TERMINATE;
        }
        if ( overwriteStatus.isSkip() ) {
          return FileVisitResult.CONTINUE;
        }

      }

      return FileVisitResult.CONTINUE;
    }

    private Path convertSourceToDestination( Path source ) {
      Path destinationPath = null;
      if ( source.toFile().isDirectory() ) {
        destinationPath = folderTransversedMap.computeIfAbsent( source,
          k -> Paths.get( folderTransversedMap.get( k.getParent() ).toString(), k.getFileName().toString() ) );
      } else {
        // If here it is a file and its parent dir is already processed
        destinationPath = Paths.get( folderTransversedMap.get( Paths.get( source.getParent().toString() ) ).toString(),
          source.getFileName().toString() );
      }
      return destinationPath;
    }
  }

}
