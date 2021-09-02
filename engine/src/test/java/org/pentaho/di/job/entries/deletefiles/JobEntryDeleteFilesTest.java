/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.deletefiles;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { JobEntryDeleteFiles.class } )
public class JobEntryDeleteFilesTest {
  private final String PATH_TO_FILE = "path/to/file";
  private final String STRING_SPACES_ONLY = "   ";

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private JobEntryDeleteFiles jobEntry;
  private NamedClusterEmbedManager mockNamedClusterEmbedManager;

  // Temporary folders
  private static final String[] FIRST_LEVEL_FOLDERS = new String[] { "aa1", "aa2", "aa 3" };
  private static final String[] SECOND_LEVEL_FOLDERS = new String[] { "bb1", "bb2", "bb 3" };
  private static final int TOTAL_NUMBER_OF_FOLDERS_NO_ROOT = FIRST_LEVEL_FOLDERS.length // First level folders
    + ( FIRST_LEVEL_FOLDERS.length * SECOND_LEVEL_FOLDERS.length ); // Second level folders
  private static final int TOTAL_NUMBER_OF_FOLDERS_WITH_ROOT = TOTAL_NUMBER_OF_FOLDERS_NO_ROOT + 1;

  // The temporary files
  private static final String[] FILE_NAMES = new String[] {
    // Some examples without spaces in the name
    "xpto_without_spaces.txt",
    "xpto_another_without_spaces.txt",
    "doesnt_start_with_xpto_and_has_no_spaces.txt",
    // And some examples with spaces in the name
    "xpto spaces.txt",
    "xpto another spaces.txt",
    "doesnt start with xpto but has spaces.txt"
  };
  private static final int NUMBER_OF_FILES_PER_FOLDER = FILE_NAMES.length;
  private static final int NUMBER_OF_XPTO_FILES_PER_FOLDER = 4;
  private static final int NUMBER_OF_NOT_XPTO_FILES_PER_FOLDER =
    NUMBER_OF_FILES_PER_FOLDER - NUMBER_OF_XPTO_FILES_PER_FOLDER;
  private static final int TOTAL_NUMBER_OF_FILES = TOTAL_NUMBER_OF_FOLDERS_WITH_ROOT * NUMBER_OF_FILES_PER_FOLDER;
  private static final int TOTAL_NUMBER_OF_XPTO_FILES =
    TOTAL_NUMBER_OF_FOLDERS_WITH_ROOT * NUMBER_OF_XPTO_FILES_PER_FOLDER;
  private static final int TOTAL_NUMBER_OF_NOT_XPTO_FILES =
    TOTAL_NUMBER_OF_FOLDERS_WITH_ROOT * NUMBER_OF_NOT_XPTO_FILES_PER_FOLDER;

  @Before
  public void setUp() throws Exception {
    jobEntry = new JobEntryDeleteFiles();
    Job parentJob = mock( Job.class );
    doReturn( false ).when( parentJob ).isStopped();

    LogChannel mockLogChannel = mock( LogChannel.class );
    when( mockLogChannel.isDebug() ).thenReturn( false );
    when( mockLogChannel.isDetailed() ).thenReturn( false );
    doNothing().when( mockLogChannel ).logDebug( anyString() );
    doNothing().when( mockLogChannel ).logDetailed( anyString() );
    PowerMockito.whenNew( LogChannel.class ).withAnyArguments().thenReturn( mockLogChannel );

    jobEntry.setParentJob( parentJob );
    JobMeta mockJobMeta = mock( JobMeta.class );
    mockNamedClusterEmbedManager = mock( NamedClusterEmbedManager.class );
    when( mockJobMeta.getNamedClusterEmbedManager() ).thenReturn( mockNamedClusterEmbedManager );
    jobEntry.setParentJobMeta( mockJobMeta );
    jobEntry = spy( jobEntry );
  }

  @Test
  public void filesWithNoPath_AreNotProcessed_ArgsOfCurrentJob() throws Exception {
    jobEntry.setArguments( new String[] { Const.EMPTY_STRING, STRING_SPACES_ONLY } );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );
    verify( jobEntry, never() ).processFile( anyString(), anyString(), any( Job.class ) );
  }


  @Test
  public void filesWithPath_AreProcessed_ArgsOfCurrentJob() throws Exception {
    // Complete JobEntryDeleteFiles mocking
    doReturn( true ).when( jobEntry ).processFile( anyString(), anyString(), any( Job.class ) );

    String[] args = new String[] { PATH_TO_FILE };
    jobEntry.setArguments( args );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );
    verify( jobEntry, times( args.length ) ).processFile( anyString(), anyString(), any( Job.class ) );
    verify( mockNamedClusterEmbedManager ).passEmbeddedMetastoreKey( anyObject(), anyString() );
  }


  @Test
  public void filesWithNoPath_AreNotProcessed_ArgsOfPreviousMeta() throws Exception {
    jobEntry.setArgFromPrevious( true );

    Result prevMetaResult = new Result();
    List<RowMetaAndData> metaAndDataList = new ArrayList<>();

    metaAndDataList.add( constructRowMetaAndData( Const.EMPTY_STRING, null ) );
    metaAndDataList.add( constructRowMetaAndData( STRING_SPACES_ONLY, null ) );

    prevMetaResult.setRows( metaAndDataList );

    jobEntry.execute( prevMetaResult, 0 );
    verify( jobEntry, never() ).processFile( anyString(), anyString(), any( Job.class ) );
  }

  @Test
  public void filesPath_AreProcessed_ArgsOfPreviousMeta() throws Exception {
    // Complete JobEntryDeleteFiles mocking
    doReturn( true ).when( jobEntry ).processFile( anyString(), anyString(), any( Job.class ) );

    jobEntry.setArgFromPrevious( true );

    Result prevMetaResult = new Result();
    List<RowMetaAndData> metaAndDataList = new ArrayList<>();

    metaAndDataList.add( constructRowMetaAndData( PATH_TO_FILE, null ) );
    prevMetaResult.setRows( metaAndDataList );

    jobEntry.execute( prevMetaResult, 0 );
    verify( jobEntry, times( metaAndDataList.size() ) ).processFile( anyString(), anyString(), any( Job.class ) );
  }

  @Test
  public void filesPathVariables_AreProcessed_OnlyIfValueIsNotBlank() throws Exception {
    // Complete JobEntryDeleteFiles mocking
    doReturn( true ).when( jobEntry ).processFile( anyString(), anyString(), any( Job.class ) );

    final String pathToFileBlankValue = "pathToFileBlankValue";
    final String pathToFileValidValue = "pathToFileValidValue";

    jobEntry.setVariable( pathToFileBlankValue, Const.EMPTY_STRING );
    jobEntry.setVariable( pathToFileValidValue, PATH_TO_FILE );

    jobEntry.setArguments( new String[] { asVariable( pathToFileBlankValue ), asVariable( pathToFileValidValue ) } );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );

    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), anyString(), any( Job.class ) );
  }

  @Test
  public void specifyingTheSamePath_WithDifferentWildcards() throws Exception {
    // Complete JobEntryDeleteFiles mocking
    doReturn( true ).when( jobEntry ).processFile( anyString(), anyString(), any( Job.class ) );

    final String fileExtensionTxt = ".txt";
    final String fileExtensionXml = ".xml";

    String[] args = new String[] { PATH_TO_FILE, PATH_TO_FILE };
    jobEntry.setArguments( args );
    jobEntry.setFilemasks( new String[] { fileExtensionTxt, fileExtensionXml } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );

    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), eq( fileExtensionTxt ), any( Job.class ) );
    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), eq( fileExtensionXml ), any( Job.class ) );
  }

  private RowMetaAndData constructRowMetaAndData( Object... data ) {
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "filePath" ) );
    meta.addValueMeta( new ValueMetaString( "wildcard" ) );

    return new RowMetaAndData( meta, data );
  }

  private String asVariable( String variable ) {
    return "${" + variable + "}";
  }

  @Test
  public void testExecute_MaskMatch_WithSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "xpto.*txt", true );

    // 'xpto' files should have been deleted from the first level folder
    assertEquals( NUMBER_OF_NOT_XPTO_FILES_PER_FOLDER, countFiles( tempFolder.getRoot(), false ) );
    // 'xpto' files should have been deleted from under level folder
    assertEquals( TOTAL_NUMBER_OF_NOT_XPTO_FILES, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_MaskMatch_WithoutSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "xpto.*txt", false );

    // 'xpto' files should have been deleted from the first level folder
    assertEquals( NUMBER_OF_NOT_XPTO_FILES_PER_FOLDER, countFiles( tempFolder.getRoot(), false ) );
    // 'xpto' files should have NOT been deleted from under level folder
    assertEquals( TOTAL_NUMBER_OF_FILES - NUMBER_OF_XPTO_FILES_PER_FOLDER, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_MaskNoMatch_WithSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "something that won't match", true );

    // No files should have been deleted from any folder
    assertEquals( TOTAL_NUMBER_OF_FILES, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_MaskNoMatch_WithoutSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "something that won't match", false );

    // No files should have been deleted from any folder
    assertEquals( TOTAL_NUMBER_OF_FILES, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_EmptyMask_WithSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "", true );

    // No files should have been deleted from any folder
    assertEquals( 0, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_EmptyMask_WithoutSubfolders() throws Exception {

    testExecute_Mask_SubFolder( "", false );

    // All files should have been deleted from the first level folder
    assertEquals( 0, countFiles( tempFolder.getRoot(), false ) );
    // No files should have been deleted from under level folder
    assertEquals( TOTAL_NUMBER_OF_FILES - NUMBER_OF_FILES_PER_FOLDER, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_NullMask_WithSubfolders() throws Exception {

    testExecute_Mask_SubFolder( null, true );

    // No files should have been deleted from any folder
    assertEquals( 0, countFiles( tempFolder.getRoot(), true ) );
  }

  @Test
  public void testExecute_NullMask_WithoutSubfolders() throws Exception {

    testExecute_Mask_SubFolder( null, false );

    // All files should have been deleted from the first level folder
    assertEquals( 0, countFiles( tempFolder.getRoot(), false ) );
    // No files should have been deleted from under level folder
    assertEquals( TOTAL_NUMBER_OF_FILES - NUMBER_OF_FILES_PER_FOLDER, countFiles( tempFolder.getRoot(), true ) );
  }

  private void testExecute_Mask_SubFolder( String mask, boolean includeSubfolders ) throws Exception {

    when( jobEntry.processFile( anyString(), anyString(), any( Job.class ) ) ).thenCallRealMethod();

    buildTestFolderTree();

    String[] args = new String[] { tempFolder.getRoot().getPath() };
    jobEntry.setArguments( args );
    jobEntry.setFilemasks( new String[] { mask } );
    jobEntry.setArgFromPrevious( false );
    jobEntry.setIncludeSubfolders( includeSubfolders );

    jobEntry.execute( new Result(), 0 );
  }

  private int countFiles( File file, boolean countSubFolders ) {
    int count = 0;
    if ( file.isDirectory() ) {
      // It's a directory, so 'listFiles' will never return 'null'
      for ( File child : file.listFiles() ) {
        if ( child.isFile() ) {
          ++count;
          continue;
        }
        if ( countSubFolders ) {
          count += countFiles( child, countSubFolders );
        }
      }
    } else if ( file.isFile() ) {
      ++count;
    }
    return count;
  }


  /**
   * <p>Creates the following folder structure:</p>
   * <pre>
   *   <files>
   *   \aa1
   *      <files>
   *      \bb1
   *         <files>
   *      \bb2
   *         <files>
   *      \bb 3
   *         <files>
   *   \aa2
   *      <files>
   *      \bb1
   *         <files>
   *      \bb2
   *         <files>
   *      \bb 3
   *         <files>
   *   \aa 3
   *      <files>
   *      \bb1
   *         <files>
   *      \bb2
   *         <files>
   *      \bb 3
   *         <files>
   * </pre>
   */
  private void buildTestFolderTree() throws IOException {
    // Create files on root
    createFilesInFolder( tempFolder.getRoot() );

    for ( String folder1Name : FIRST_LEVEL_FOLDERS ) {
      // Create first level folders
      File folder1 = tempFolder.newFolder( folder1Name );

      // Create files on first level folders
      createFilesInFolder( folder1 );

      for ( String folder2Name : SECOND_LEVEL_FOLDERS ) {
        // Create second level folders
        File folder2 = tempFolder.newFolder( folder1Name, folder2Name );

        // Create files on first level folders
        createFilesInFolder( folder2 );
      }
    }

    // Just to guarantee that the numbers are correct
    assertEquals( NUMBER_OF_FILES_PER_FOLDER, NUMBER_OF_XPTO_FILES_PER_FOLDER + NUMBER_OF_NOT_XPTO_FILES_PER_FOLDER );
  }

  private void createFilesInFolder( File folder ) throws IOException {
    for ( String fileName : FILE_NAMES ) {
      File file = new File( folder, fileName );
      file.createNewFile();
    }
  }
}
