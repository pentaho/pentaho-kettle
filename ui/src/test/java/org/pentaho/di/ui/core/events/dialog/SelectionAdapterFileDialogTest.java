/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.events.dialog;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.swt.events.SelectionEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SelectionAdapterFileDialogTest {

  SelectionAdapterFileDialog testInstance;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setup() {
    testInstance = createTestInstance();
  }


  @Test
  public void testWidgetSelectedHelper() {
    //SETUP
    LogChannelInterface log = mock( LogChannelInterface.class );
    StringBuilder textVar = new StringBuilder();
    AbstractMeta meta = mock( AbstractMeta.class );
    RepositoryUtility repositoryUtility = mock( RepositoryUtility.class );
    ExtensionPointWrapper extensionPointWrapper = mock( ExtensionPointWrapper.class );
    SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE );
    SelectionEvent event = mock( SelectionEvent.class );

    String testPath = "/home/devuser/some/path";
    when( meta.environmentSubstitute( testPath ) ).thenReturn( testPath );

    SelectionAdapterFileDialog testInstance1 = createTestInstance( log, textVar, meta, options, repositoryUtility,
      extensionPointWrapper );
    testInstance1.setText( testPath );
    testInstance1.widgetSelected( event );

    assertEquals( testInstance1.getText(), testPath );
  }

  @Test
  public void testResolveFile() throws Exception {
    String unresolvedPath = "{SOME_VAR}/some/path";
    String resolvedPath = "/home/devuser/some/path";
    AbstractMeta abstractMeta = mock( AbstractMeta.class );
    when( abstractMeta.environmentSubstitute( unresolvedPath ) ).thenReturn( resolvedPath );

    assertNotNull( testInstance.resolveFile( abstractMeta, unresolvedPath ) );
  }

  @Test
  public void testApplyRelativePathEnvVar() throws IOException {
    //SETUP
    LogChannelInterface log = mock( LogChannelInterface.class );
    StringBuilder textVar = new StringBuilder();
    AbstractMeta meta = mock( AbstractMeta.class );
    RepositoryUtility repositoryUtility = mock( RepositoryUtility.class );
    ExtensionPointWrapper extensionPointWrapper = mock( ExtensionPointWrapper.class );
    SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE );
    File testFile = temporaryFolder.newFile( "testFileName" );

    String testPath = testFile.getPath();
    when( meta.environmentSubstitute( testPath ) ).thenReturn( testPath );

    SelectionAdapterFileDialog testInstance1 = createTestInstance( log, textVar, meta, options, repositoryUtility,
      extensionPointWrapper );
    testInstance1.setText( testPath );
    String pathWithCurrentDirVar = testInstance1.applyRelativePathEnvVar( testPath );
    assertEquals( testPath, pathWithCurrentDirVar );

    when( meta.getFilename() ).thenReturn( testPath );
    pathWithCurrentDirVar =  testInstance1.applyRelativePathEnvVar( testPath );
    assertEquals( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/testFileName", pathWithCurrentDirVar );
  }


  @Test
  public void testCreateFileDialogOperation() {
    // TEST : SELECT file
    FileDialogOperation fdo1 = testInstance.createFileDialogOperation( SelectionOperation.FILE );
    assertNotNull( fdo1 );
    assertEquals( FileDialogOperation.SELECT_FILE, fdo1.getCommand() );
    assertEquals( FileDialogOperation.ORIGIN_SPOON, fdo1.getOrigin() );

    // TEST : SELECT folder
    FileDialogOperation fdo2 = testInstance.createFileDialogOperation( SelectionOperation.FOLDER );
    assertNotNull( fdo2 );
    assertEquals( FileDialogOperation.SELECT_FOLDER, fdo2.getCommand() );
    assertEquals( FileDialogOperation.ORIGIN_SPOON, fdo2.getOrigin() );

    // TEST : SELECT folder
    FileDialogOperation fdo3 = testInstance.createFileDialogOperation( SelectionOperation.FILE_OR_FOLDER );
    assertNotNull( fdo3 );
    assertEquals( FileDialogOperation.SELECT_FILE_FOLDER, fdo3.getCommand() );
    assertEquals( FileDialogOperation.ORIGIN_SPOON, fdo3.getOrigin() );
  }

  @Test
  public void testIsUrl() {
    assertTrue( testInstance.isUrl( "hc://test/test.txt" ) );
    assertTrue( testInstance.isUrl( "pvfs://test/test.txt" ) );
    assertTrue( testInstance.isUrl( "s3://test/test.txt" ) );
    assertFalse( testInstance.isUrl( "C:\\\\test\\test.txt " ) );
    assertFalse( testInstance.isUrl( "/test/test.txt " ) );

  }

  @Test
  public void testSetPath() throws Exception {
    // TEST : is file
    FileDialogOperation fileDialogOperation1 = createFileDialogOperation();
    FileObject fileObject1 = mock( FileObject.class );
    String absoluteFilePath = "/home/someuser/somedir";
    when( fileObject1.isFile() ).thenReturn( true );
    when( fileObject1.toString() ).thenReturn( absoluteFilePath );

    testInstance.setPath( fileDialogOperation1, fileObject1, absoluteFilePath );

    assertEquals( absoluteFilePath, fileDialogOperation1.getPath() );

    // TEST : is not file
    FileDialogOperation fileDialogOperation2 = createFileDialogOperation();
    FileObject fileObject2 = mock( FileObject.class );
    when( fileObject2.isFile() ).thenReturn( false );

    testInstance.setPath( fileDialogOperation2, fileObject2, absoluteFilePath );

    assertNull( fileDialogOperation2.getPath() );
  }

  @Test( expected = KettleException.class )
  public void testSetPath_Exception() throws Exception {
    // TEST : is file
    FileDialogOperation fileDialogOperation1 = createFileDialogOperation();
    FileObject fileObject1 = mock( FileObject.class );
    String absoluteFilePath = "/home/someuser/somedir";
    when( fileObject1.isFile() ).thenThrow(  new FileSystemException( "some error" ) );

    testInstance.setPath( fileDialogOperation1, fileObject1, absoluteFilePath );
  }

  @Test
  public void testSetStartDir() throws Exception {
    // TEST : is file
    FileDialogOperation fileDialogOperation1 = createFileDialogOperation();
    FileObject fileObject1 = mock( FileObject.class );
    String absoluteFilePath = "/home/someuser/somedir";
    when( fileObject1.isFolder() ).thenReturn( false );

    testInstance.setStartDir( fileDialogOperation1, fileObject1, absoluteFilePath );

    assertNull( fileDialogOperation1.getStartDir() );

    // TEST : is not file
    FileDialogOperation fileDialogOperation2 = createFileDialogOperation();
    FileObject fileObject2 = mock( FileObject.class );
    when( fileObject2.isFolder() ).thenReturn( true );
    when( fileObject2.toString() ).thenReturn( absoluteFilePath );

    testInstance.setStartDir( fileDialogOperation2, fileObject2, absoluteFilePath );

    assertEquals( absoluteFilePath, fileDialogOperation2.getStartDir() );
  }

  @Test( expected = KettleException.class )
  public void testSetStartDir_Exception() throws Exception {
    // TEST : is file
    FileDialogOperation fileDialogOperation1 = createFileDialogOperation();
    String absoluteFilePath = "/home/someuser/somedir";
    FileObject fileObject1 = mock( FileObject.class );
    when( fileObject1.isFolder() ).thenThrow(  new FileSystemException( "some error" ) );

    testInstance.setStartDir( fileDialogOperation1, fileObject1, absoluteFilePath );
  }

  @Test
  public void testGetRepositoryFilePath() {
    RepositoryDirectoryInterface repositoryDirectory = mock( RepositoryDirectoryInterface.class );
    when( repositoryDirectory.getPath() ).thenReturn( "/home/devuser/files" );
    RepositoryObject repositoryObject = mock( RepositoryObject.class );
    when( repositoryObject.getRepositoryDirectory() ).thenReturn( repositoryDirectory );
    when( repositoryObject.getName() ).thenReturn( "food.txt" );
    FileDialogOperation fileDialogOperation = createFileDialogOperation();
    fileDialogOperation.setRepositoryObject( repositoryObject );

    assertEquals( "/home/devuser/files/food.txt",
      testInstance.getRepositoryFilePath( fileDialogOperation ).replace( '\\', '/' ) );
  }

  @Test
  public void testConcat() {

    assertEquals( "/home/devuser/files/food.txt",
      testInstance.concat( "/home/devuser/files", "food.txt" ).replace('\\', '/') );

    assertEquals( "/home/devuser/files/food.txt",
      testInstance.concat( "/home/devuser/files/", "food.txt" ).replace('\\', '/') );

    assertEquals( "/home/devuser/files/food.txt",
      testInstance.concat( "/", "home/devuser/files/food.txt" ).replace('\\', '/') );

    assertEquals( "/",
      testInstance.concat( "/", "" ).replace('\\', '/') );

    assertEquals( "/home/devuser/files/",
      testInstance.concat( "/home/devuser/files", "" ).replace('\\', '/') );
  }

  @Test
  public void testSetFilter() {

    // TEST : null filter
    FileDialogOperation fileDialogOperation1 = createFileDialogOperation();
    testInstance.setFilter( fileDialogOperation1, null );

    assertEquals( FilterType.ALL.toString(), fileDialogOperation1.getFilter() );

    // TEST: empty array filter
    FileDialogOperation fileDialogOperation2 = createFileDialogOperation();
    testInstance.setFilter( fileDialogOperation2, new String[]{} );

    assertEquals( FilterType.ALL.toString(), fileDialogOperation2.getFilter() );

    // TEST : one item filter
    FileDialogOperation fileDialogOperation3 = createFileDialogOperation();
    testInstance.setFilter( fileDialogOperation3, new String[]{ FilterType.TXT.toString() } );

    assertEquals( FilterType.TXT.toString(), fileDialogOperation3.getFilter() );

    // TEST : multiple filters
    FileDialogOperation fileDialogOperation4 = createFileDialogOperation();
    testInstance.setFilter( fileDialogOperation4, new String[]{ FilterType.TXT.toString(), FilterType.CSV.toString(), FilterType.JSON.toString() } );

    assertEquals( "TXT,CSV,JSON", fileDialogOperation4.getFilter() );
  }

  @Test
  public void testCleanFilters() {

    String[] EMPTY_ARRAY = new String[]{};

    assertNull( testInstance.cleanFilters( null ) );

    assertNull( testInstance.cleanFilters( new String[]{} ) );

    assertArrayEquals( EMPTY_ARRAY, testInstance.cleanFilters( new String[]{ null } ) );

    assertArrayEquals( EMPTY_ARRAY, testInstance.cleanFilters( new String[]{ null, null, null } ) );

    assertArrayEquals( EMPTY_ARRAY, testInstance.cleanFilters( new String[]{ "     ", null, "" } ) );

    assertArrayEquals( new String[]{ "TXT", "CSV" }, testInstance.cleanFilters( new String[]{ FilterType.TXT.toString(), null,
      FilterType.CSV.toString(), "" } ) );

    assertArrayEquals( new String[]{ "TXT", "CSV" }, testInstance.cleanFilters( new String[]{ FilterType.TXT.toString(),
      FilterType.CSV.toString() } ) );
  }

  @Test
  public void testIsConnectedToRepository() {

    //SETUP
    LogChannelInterface log = mock( LogChannelInterface.class );
    StringBuilder textVar = new StringBuilder();
    AbstractMeta meta = mock( AbstractMeta.class );
    ExtensionPointWrapper extensionPointWrapper = mock( ExtensionPointWrapper.class );
    SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE );

    // True case:
    RepositoryUtility repositoryUtilityTrue = mock( RepositoryUtility.class );
    when( repositoryUtilityTrue.isConnectedToRepository() ).thenReturn( true );

    SelectionAdapterFileDialog testInstance1 = createTestInstance( log, textVar, meta, options, repositoryUtilityTrue,
      extensionPointWrapper );

    assertTrue( testInstance1.isConnectedToRepository() );

    // False case:
    RepositoryUtility repositoryUtilityFalse = mock( RepositoryUtility.class );
    when( repositoryUtilityFalse.isConnectedToRepository() ).thenReturn( false );

    SelectionAdapterFileDialog testInstance2 = createTestInstance( log, textVar, meta, options, repositoryUtilityFalse,
      extensionPointWrapper );

    assertFalse( testInstance2.isConnectedToRepository() );
  }

  @Test
  public void testSetProvider() throws Exception {

    // TEST PVFS , ProviderFilter null
    String pvfsPath = "pvfs://someConnectionName/dirA/dir2/dirC/randomFile.txt";
    URI uriPvfs = new URI( pvfsPath );
    FileObject foPvfs = mock( FileObject.class );
    when( foPvfs.getURI() ).thenReturn( uriPvfs );

    FileDialogOperation fileDialogOperationPvfs_ProviderFilterNull = createFileDialogOperation();
    fileDialogOperationPvfs_ProviderFilterNull.setProviderFilter( null );

    testInstance.setProvider( fileDialogOperationPvfs_ProviderFilterNull, foPvfs );

    assertEquals( ProviderFilterType.VFS.toString(), fileDialogOperationPvfs_ProviderFilterNull.getProvider() );


    // TEST PVFS , ProviderFilter Default
    FileDialogOperation fileDialogOperationPvfs_ProviderFilterDefault = createFileDialogOperation();
    fileDialogOperationPvfs_ProviderFilterDefault.setProviderFilter( ProviderFilterType.DEFAULT.toString() );

    testInstance.setProvider( fileDialogOperationPvfs_ProviderFilterDefault, foPvfs );

    assertEquals( ProviderFilterType.VFS.toString(), fileDialogOperationPvfs_ProviderFilterDefault.getProvider() );

  }

  protected FileDialogOperation createFileDialogOperation() {
    return new FileDialogOperation( FileDialogOperation.SELECT_FILE, FileDialogOperation.ORIGIN_SPOON );
  }

  protected SelectionAdapterFileDialog createTestInstance() {
    LogChannelInterface log = mock( LogChannelInterface.class );
    StringBuilder textWidget = new StringBuilder();
    AbstractMeta meta = mock( AbstractMeta.class );
    RepositoryUtility repositoryUtility = mock( RepositoryUtility.class );
    ExtensionPointWrapper extensionPointWrapper = mock( ExtensionPointWrapper.class );
    SelectionAdapterOptions options = new SelectionAdapterOptions( SelectionOperation.FILE, new String[]{}, "" );

    return createTestInstance( log, textWidget, meta, options, repositoryUtility, extensionPointWrapper );
  }

  protected SelectionAdapterFileDialog createTestInstance( LogChannelInterface log, StringBuilder textWidget,
                                                           AbstractMeta meta, SelectionAdapterOptions options,
                                                           RepositoryUtility repositoryUtility,
                                                           ExtensionPointWrapper extensionPointWrapper  ) {
    return new TestSelectionAdapterFileDialog( log, textWidget, meta, options, repositoryUtility, extensionPointWrapper );
  }

  /**
   * Test class to abstract class logic under test.
   */
  public static class TestSelectionAdapterFileDialog extends SelectionAdapterFileDialog<StringBuilder> {

    public TestSelectionAdapterFileDialog( LogChannelInterface log, StringBuilder textWidget, AbstractMeta meta,
                                           SelectionAdapterOptions options,
                                           RepositoryUtility repositoryUtility, ExtensionPointWrapper extensionPointWrapper  )  {
      super( log, textWidget, meta, options, repositoryUtility, extensionPointWrapper  );
    }

    @Override protected String getText() {
      return this.getTextWidget().toString();
    }

    @Override protected void setText( String text ) {
      this.getTextWidget().setLength( 0 );
      this.getTextWidget().append( text );
    }
  }
}
