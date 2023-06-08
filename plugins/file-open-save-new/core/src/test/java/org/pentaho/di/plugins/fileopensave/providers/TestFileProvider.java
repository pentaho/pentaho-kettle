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

package org.pentaho.di.plugins.fileopensave.providers;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.providers.model.TestDirectory;
import org.pentaho.di.plugins.fileopensave.providers.model.TestFile;
import org.pentaho.di.plugins.fileopensave.providers.model.TestTree;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFileProvider extends BaseFileProvider<TestFile> {

  public static final String NAME = "Test";
  public static final String TYPE = "test";
  private static Map<String, List<TestFile>> testFileSystem = getFileSystem();

  @Override public Class<TestFile> getFileClass() {
    return TestFile.class;
  }

  @Override public String getName() {
    return NAME;
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public boolean isAvailable() {
    return true;
  }

  @Override public Tree getTree() {
    TestTree testTree = new TestTree( NAME );
    TestDirectory testDirectory = new TestDirectory();
    testDirectory.setPath( "/" );
    testTree.setFiles( getFiles( testDirectory, null, null ) );
    return testTree;
  }

  @Override public List<TestFile> getFiles( TestFile file, String filters, VariableSpace space ) {
    List<TestFile> files = new ArrayList<>();
    testFileSystem.get( file.getPath() ).forEach( testFile -> {
      if ( testFile instanceof TestDirectory ) {
        files.add( TestDirectory.create( testFile.getName(), testFile.getPath(), testFile.getParent() ) );
      } else {
        files.add( TestFile.create( testFile.getName(), testFile.getPath(), testFile.getParent() ) );
      }
    } );
    return files;
  }

  @Override public List<TestFile> delete( List<TestFile> files, VariableSpace space ) throws FileException {
    List<TestFile> deletedFiles = new ArrayList<>();
    for ( TestFile testFile : files ) {
      if ( doDelete( testFile ) ) {
        deletedFiles.add( testFile );
      }
    }
    return deletedFiles;
  }

  private boolean doDelete( TestFile file ) {
    TestFile findFile = findFile( file );
    if ( findFile != null ) {
      testFileSystem.get( file.getParent() ).remove( findFile );
      return true;
    }
    return false;
  }

  @Override public TestFile add( TestFile folder, VariableSpace space ) throws FileException {
    testFileSystem.get( folder.getParent() ).add( folder );
    return folder;
  }

  @Override public boolean fileExists( TestFile dir, String path, VariableSpace space ) throws FileException {
    return findFile( dir, path ) != null;
  }

  @Override public String getNewName( TestFile destDir, String newPath, VariableSpace space ) throws FileException {
    return newPath + " " + 1;
  }

  @Override public boolean isSame( File file1, File file2 ) {
    return file1.getProvider().equals( file2.getProvider() );
  }

  @Override public TestFile rename( TestFile file, String newPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    TestFile findFile = findFile( file );
    if ( findFile != null ) {
      findFile.setPath( newPath );
      findFile.setName( newPath.substring( newPath.lastIndexOf( "/" ) + 1 ) );
      findFile.setParent( newPath.substring( 0, newPath.lastIndexOf( "/" ) ) );
    }
    return findFile;
  }

  @Override public TestFile copy( TestFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    TestFile findFile = findFile( file );
    if ( findFile != null ) {
      String parent = toPath.substring( 0, toPath.lastIndexOf( "/" ) );
      if ( findFile instanceof TestDirectory ) {
        TestDirectory newDirectory = TestDirectory.create( findFile.getName(), toPath, parent );
        testFileSystem.get( newDirectory.getParent() ).add( newDirectory );
        return newDirectory;
      } else {
        TestFile newFile = TestFile.create( findFile.getName(), toPath, parent );
        testFileSystem.get( newFile.getParent() ).add( newFile );
        return newFile;
      }
    }
    return null;
  }

  @Override public TestFile move( TestFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    TestFile newFile = copy( file, toPath, overwriteStatus, space );
    if ( newFile != null ) {
      doDelete( newFile );
    }
    return newFile;
  }

  @Override public InputStream readFile( TestFile file, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public TestFile writeFile( InputStream inputStream, TestFile destDir, String path,
                                       OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    return null;
  }

  @Override public TestFile getParent( TestFile file ) {
    return TestDirectory.create( null, file.getParent(), "" );
  }

  private TestFile findFile( TestFile testFile ) {
    List<TestFile> testFiles = testFileSystem.get( testFile.getParent() );
    for ( TestFile testFile1 : testFiles ) {
      if ( testFile.getPath().equals( testFile1.getPath() ) ) {
        return testFile1;
      }
    }
    return null;
  }

  private TestFile findFile( TestFile testFile, String path ) {
    List<TestFile> testFiles = testFileSystem.get( testFile.getPath() );
    for ( TestFile testFile1 : testFiles ) {
      if ( testFile1.getPath().equals( path ) ) {
        return testFile1;
      }
    }
    return null;
  }

  private static Map<String, List<TestFile>> getFileSystem() {
    Map<String, List<TestFile>> fileSystem = new HashMap<>();
    List<TestFile> files1 = new ArrayList<>();
    files1.add( TestFile.create( "file1", "/file1", "/" ) );
    files1.add( TestFile.create( "file1", "/file1", "/" ) );
    files1.add( TestFile.create( "file3", "/file3", "/" ) );
    files1.add( TestFile.create( "file4", "/file4", "/" ) );
    files1.add( TestDirectory.create( "directory1", "/directory1", "/" ) );
    files1.add( TestDirectory.create( "directory2", "/directory2", "/" ) );
    files1.add( TestDirectory.create( "directory3", "/directory3", "/" ) );
    files1.add( TestDirectory.create( "directory4", "/directory4", "/" ) );
    fileSystem.put( "/", files1 );

    List<TestFile> files2 = new ArrayList<>();
    files2.add( TestFile.create( "file1", "/directory1/file1", "/" ) );
    files2.add( TestFile.create( "file2", "/directory1/file2", "/" ) );
    files2.add( TestFile.create( "file3", "/directory1/file3", "/" ) );
    files2.add( TestFile.create( "file4", "/directory1/file4", "/" ) );
    files2.add( TestDirectory.create( "directory1", "/directory1/directory1", "/" ) );
    files2.add( TestDirectory.create( "directory2", "/directory1/directory2", "/" ) );
    files2.add( TestDirectory.create( "directory3", "/directory1/directory3", "/" ) );
    files2.add( TestDirectory.create( "directory4", "/directory1/directory4", "/" ) );
    fileSystem.put( "/directory1", files2 );

    List<TestFile> files3 = new ArrayList<>();
    files3.add( TestFile.create( "file1", "/directory2/file1", "/" ) );
    files3.add( TestFile.create( "file2", "/directory2/file2", "/" ) );
    files3.add( TestFile.create( "file3", "/directory2/file3", "/" ) );
    files3.add( TestFile.create( "file4", "/directory2/file4", "/" ) );
    files3.add( TestDirectory.create( "directory1", "/directory2/directory1", "/" ) );
    files3.add( TestDirectory.create( "directory2", "/directory2/directory2", "/" ) );
    files3.add( TestDirectory.create( "directory3", "/directory2/directory3", "/" ) );
    files3.add( TestDirectory.create( "directory4", "/directory2/directory4", "/" ) );
    fileSystem.put( "/directory2", files3 );

    List<TestFile> files4 = new ArrayList<>();
    files4.add( TestFile.create( "file1", "/directory3/file1", "/" ) );
    files4.add( TestFile.create( "file2", "/directory3/file2", "/" ) );
    files4.add( TestFile.create( "file3", "/directory3/file3", "/" ) );
    files4.add( TestFile.create( "file4", "/directory3/file4", "/" ) );
    files4.add( TestDirectory.create( "directory1", "/directory3/directory1", "/" ) );
    files4.add( TestDirectory.create( "directory2", "/directory3/directory2", "/" ) );
    files4.add( TestDirectory.create( "directory3", "/directory3/directory3", "/" ) );
    files4.add( TestDirectory.create( "directory4", "/directory3/directory4", "/" ) );
    fileSystem.put( "/directory3", files4 );

    List<TestFile> files5 = new ArrayList<>();
    files5.add( TestDirectory.create( "directory1", "/directory4/directory1", "/" ) );
    files5.add( TestDirectory.create( "directory2", "/directory4/directory2", "/" ) );
    files5.add( TestDirectory.create( "directory3", "/directory4/directory3", "/" ) );
    files5.add( TestDirectory.create( "directory4", "/directory4/directory4", "/" ) );
    fileSystem.put( "/directory4", files5 );

    return fileSystem;
  }

  public void clearProviderCache() {
    //Any local caches that this provider might use should be cleared here.
  }

  @Override public TestFile createDirectory( String parentPath, TestFile file, String newDirectoryName )
    throws FileException, KettleFileException {
    return null;
  }

  @Override public TestFile getFile( TestFile file, VariableSpace space ) {
    return null;
  }
}
