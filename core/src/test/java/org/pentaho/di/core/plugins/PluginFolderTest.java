/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.plugins;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleFileException;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class PluginFolderTest {

  private static final String BASE_TEMP_DIR = System.getProperty( "java.io.tmpdir" );
  private static final String PLUGINS_DIR_NAME = "plugins";
  private static final String WITH_JAR_IN_NAME_DIR_NAME = "job.jar";
  private static final String TEST_DIR_NAME = "test_dir";
  /**
   *
   */
  private static final Path PATH_TO_KETTLE_IGNORE_FILE =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, TEST_DIR_NAME, ".kettle-ignore" );
  private static final String JAR_FILE1_NAME = "job.jar";
  private static final String JAR_FILE2_NAME = "test.jar";
  /**
   *
   */
  private static final Path PATH_TO_JAR_IN_LIB_DIR =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, "lib", JAR_FILE2_NAME );
  private static final String NOT_JAR_FILE_NAME = "test.txt";
  private static final Path PATH_TO_PLUGIN_DIR = Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME );
  /**
   * Paths below represent the following structure of the folder and files in them:
   * <p>
   * <TMP_DIR>/plugins/job.jar - folder
   * <p>
   * <TMP_DIR>/plugins/job.jar/job.jar - file
   * <p>
   * <TMP_DIR>/plugins/job.jar/test.txt - file
   */
  private static final Path PATH_TO_DIR_WITH_JAR_IN_NAME =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, WITH_JAR_IN_NAME_DIR_NAME );
  private static final Path PATH_TO_JAR_FILE1 =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, WITH_JAR_IN_NAME_DIR_NAME, JAR_FILE1_NAME );
  private static final Path PATH_TO_NOT_JAR_FILE =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, WITH_JAR_IN_NAME_DIR_NAME, NOT_JAR_FILE_NAME );

  /**
   * Paths below represent the following structure of the folder and files in them:
   * <p>
   * <TMP_DIR>/plugins/test_dir - folder
   * <p>
   * <TMP_DIR>/plugins/test_dir/job.jar - file
   * <p>
   * <TMP_DIR>/plugins/test_dir/test.txt - file
   */
  private static final Path PATH_TO_TEST_DIR_NAME = Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, TEST_DIR_NAME );
  private static final Path PATH_TO_JAR_FILE2 =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, TEST_DIR_NAME, JAR_FILE2_NAME );
  private static final Path PATH_TO_NOT_JAR_FILE_IN_TEST_DIR =
      Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, TEST_DIR_NAME, NOT_JAR_FILE_NAME );

  private PluginFolder plFolder;

  @Before
  public void setUp() throws IOException {
    cleanTempDir( PATH_TO_PLUGIN_DIR );
    plFolder = new PluginFolder( PATH_TO_PLUGIN_DIR.toAbsolutePath().toString(), false, true );
  }

  @After
  public void clean() throws IOException {
    cleanTempDir( PATH_TO_PLUGIN_DIR );
    plFolder = null;
  }

  @Test
  public void testIsPluginXmlFolder_SetPluginXmlFolder() throws IOException, KettleFileException {
    plFolder = new PluginFolder( PLUGINS_DIR_NAME, false, true );
    assertNotNull( plFolder );
    assertFalse( plFolder.isPluginXmlFolder() );
    plFolder.setPluginXmlFolder( true );
    assertTrue( plFolder.isPluginXmlFolder() );
    plFolder.setPluginXmlFolder( false );
    assertFalse( plFolder.isPluginXmlFolder() );
  }

  @Test
  public void testIsPluginAnnotationsFolder_SetPluginAnnotationsFolder() throws IOException, KettleFileException {
    plFolder = new PluginFolder( PLUGINS_DIR_NAME, false, true );
    assertNotNull( plFolder );
    assertTrue( plFolder.isPluginAnnotationsFolder() );
    plFolder.setPluginAnnotationsFolder( false );
    assertFalse( plFolder.isPluginAnnotationsFolder() );
    plFolder.setPluginAnnotationsFolder( true );
    assertTrue( plFolder.isPluginAnnotationsFolder() );
  }

  @Test
  public void testGetFolder_SetFolder() throws IOException, KettleFileException {
    plFolder = new PluginFolder( null, false, true );
    assertNotNull( plFolder );
    assertNull( plFolder.getFolder() );
    plFolder.setFolder( PLUGINS_DIR_NAME );
    assertEquals( PLUGINS_DIR_NAME, plFolder.getFolder() );
  }

  @Test
  public void testFindJarFiles_DirWithJarInNameNotAdded() throws IOException, KettleFileException {
    Files.createDirectories( PATH_TO_DIR_WITH_JAR_IN_NAME );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 0, findJarFiles.length );
  }

  @Test
  public void testFindJarFiles_DirWithJarInNameNotAddedButJarFileAdded() throws IOException, KettleFileException {
    Files.createDirectories( PATH_TO_DIR_WITH_JAR_IN_NAME );
    Files.createFile( PATH_TO_JAR_FILE1 );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 1, findJarFiles.length );
    assertTrue( findJarFiles[0].isFile() );
    assertEquals( PATH_TO_JAR_FILE1.toUri().toString(), findJarFiles[0].getURL().toString() );
  }

  @Test
  public void testFindJarFiles_DirWithJarInNameNotAddedAndTxtFileNotAdded() throws IOException, KettleFileException {
    Files.createDirectories( PATH_TO_DIR_WITH_JAR_IN_NAME );
    Files.createFile( PATH_TO_NOT_JAR_FILE );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 0, findJarFiles.length );
  }

  @Test
  public void testFindJarFiles_SeveralJarsInDifferentDirs() throws IOException, KettleFileException {
    // Files in plugins/job.jar folder
    Files.createDirectories( PATH_TO_DIR_WITH_JAR_IN_NAME );
    Files.createFile( PATH_TO_JAR_FILE1 );
    Files.createFile( PATH_TO_NOT_JAR_FILE );
    // Files in plugins/test_dir folder
    Files.createDirectories( PATH_TO_TEST_DIR_NAME );
    Files.createFile( PATH_TO_JAR_FILE2 );
    Files.createFile( PATH_TO_NOT_JAR_FILE_IN_TEST_DIR );
    // Files in plugins folder
    Files.createFile( Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, JAR_FILE2_NAME ) );
    Files.createFile( Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, NOT_JAR_FILE_NAME ) );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 3, findJarFiles.length );
  }

  @Test
  public void testFindJarFiles_DirWithKettleIgnoreFileIgnored() throws IOException, KettleFileException {
    Files.createDirectories( PATH_TO_TEST_DIR_NAME );
    Files.createFile( PATH_TO_JAR_FILE2 );
    Files.createFile( PATH_TO_KETTLE_IGNORE_FILE );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 0, findJarFiles.length );
  }

  @Test
  public void testFindJarFiles_LibDirIgnored() throws IOException, KettleFileException {
    Files.createDirectories( Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, "lib" ) );
    Files.createFile( PATH_TO_JAR_IN_LIB_DIR );

    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 0, findJarFiles.length );
  }

  @Test
  public void testFindJarFiles_LibDirNOTIgnored() throws IOException, KettleFileException {
    Files.createDirectories( Paths.get( BASE_TEMP_DIR, PLUGINS_DIR_NAME, "lib" ) );
    Files.createFile( PATH_TO_JAR_IN_LIB_DIR );

    plFolder = new PluginFolder( PATH_TO_PLUGIN_DIR.toAbsolutePath().toString(), false, true, true );
    FileObject[] findJarFiles = plFolder.findJarFiles();
    assertNotNull( findJarFiles );
    assertEquals( 1, findJarFiles.length );
    assertTrue( findJarFiles[0].isFile() );
    assertEquals( PATH_TO_JAR_IN_LIB_DIR.toUri().toString(), findJarFiles[0].getURL().toString() );
  }

  @Test
  public void testFindJarFiles_ExceptionThrows() {
    String nullFolder = null;
    String expectedMessage = "Unable to list jar files in plugin folder '" + nullFolder + "'";

    plFolder = new PluginFolder( nullFolder, false, true );
    try {
      plFolder.findJarFiles();
      fail( "KettleFileException was not occured but expected." );
    } catch ( KettleFileException e ) {
      assertTrue( e instanceof KettleFileException );
      assertTrue( e.getLocalizedMessage().trim().startsWith( expectedMessage ) );
    }
  }

  private void cleanTempDir( Path path ) throws IOException {
    Files.walkFileTree( path, new FileVisitor<Path>() {
      @Override
      public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
        Files.delete( dir );
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
        Files.delete( file );
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
        return FileVisitResult.CONTINUE;
      }
    } );
  }

}
