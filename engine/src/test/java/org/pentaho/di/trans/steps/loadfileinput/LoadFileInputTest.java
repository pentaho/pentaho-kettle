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

package org.pentaho.di.trans.steps.loadfileinput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class LoadFileInputTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private FileSystemManager fs;
  private String filesPath;

  private LoadFileInputMeta stepMetaInterface;
  private FileInputList stepInputFiles;

  private LoadFileInput stepLoadFileInput;

  private StepMetaInterface runtimeSMI;
  private LoadFileInputField inputField;
  private static String wasEncoding;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    if ( Const.isWindows() ) {
      wasEncoding = System.getProperty( "file.encoding" );
      fiddleWithDefaultCharset( "utf8" );
    }
    KettleClientEnvironment.init();
  }

  @AfterClass
  public static void teardownAfterClass() {
    if ( wasEncoding != null ) {
      fiddleWithDefaultCharset( wasEncoding );
    }
  }

  // Yeah, I don't like it much either, but it lets me set file.encoding after
  // the VM has fired up. Remove this code when the backlog ticket BACKLOG-20800 gets fixed.
  private static void fiddleWithDefaultCharset( String fiddleValue ) {
    try {
      Class<Charset> charSet = Charset.class;
      Field defaultCharsetFld = charSet.getDeclaredField( "defaultCharset" );
      defaultCharsetFld.setAccessible( true );
      defaultCharsetFld.set( null, Charset.forName( fiddleValue ) );
    } catch ( Exception ex ) {
      System.out.println( "*** Fiddling with Charset class failed" );
    }
  }

  @Before
  public void setup() throws FileSystemException {
    fs = VFS.getManager();
    filesPath = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";

    String transName = "LoadFileInput";
    TransMeta transMeta = new TransMeta();
    transMeta.setName( transName );
    Trans trans = new Trans( transMeta );

    stepMetaInterface = spy( new LoadFileInputMeta() );
    stepInputFiles = new FileInputList();
    Mockito.doReturn( stepInputFiles ).when( stepMetaInterface ).getFiles( any( VariableSpace.class ) );
    String stepId = PluginRegistry.getInstance().getPluginId( StepPluginType.class, stepMetaInterface );
    StepMeta stepMeta = new StepMeta( stepId, "Load File Input", stepMetaInterface );
    transMeta.addStep( stepMeta );

    StepDataInterface stepDataInterface = new LoadFileInputData();

    int stepCopyNr = 0;

    stepLoadFileInput = new LoadFileInput( stepMeta, stepDataInterface, stepCopyNr, transMeta, trans );

    assertSame( stepMetaInterface, stepMeta.getStepMetaInterface() );

    runtimeSMI = stepMetaInterface;
    StepDataInterface runtimeSDI = runtimeSMI.getStepData();

    inputField = new LoadFileInputField();
    ((LoadFileInputMeta) runtimeSMI).setInputFields( new LoadFileInputField[] { inputField } );
    stepLoadFileInput.init( runtimeSMI, runtimeSDI );
  }

  private FileObject getFile( final String filename ) {
    try {
      return fs.resolveFile( this.getClass().getResource( filesPath + filename ) );
    } catch ( Exception e ) {
      throw new RuntimeException( "fail. " + e.getMessage(), e );
    }
  }

  @Test
  public void testOpenNextFile_noFiles() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_noFiles_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_0_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_000() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );

  }

  @Test
  public void testOpenNextFile_000_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_10_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }


  @Test
  public void testOpenNextFile_01() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_01_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010() {
    assertFalse( stepMetaInterface.isIgnoreEmptyFile() ); // ensure default value

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testOpenNextFile_010_ignoreEmpty() {
    stepMetaInterface.setIgnoreEmptyFile( true );

    stepInputFiles.addFile( getFile( "input0.txt" ) );
    stepInputFiles.addFile( getFile( "input1.txt" ) );
    stepInputFiles.addFile( getFile( "input0.txt" ) );

    assertTrue( stepLoadFileInput.openNextFile() );
    assertFalse( stepLoadFileInput.openNextFile() );
  }

  @Test
  public void testGetOneRow() throws Exception {
    // string without specified encoding
    stepInputFiles.addFile( getFile( "input1.txt" ) );

    assertNotNull( stepLoadFileInput.getOneRow() );
    assertEquals( "input1 - not empty", new String( stepLoadFileInput.data.filecontent ) );
  }

  @Test
  public void testUTF8Encoding() throws KettleException, FileSystemException {
    stepMetaInterface.setIncludeFilename( true );
    stepMetaInterface.setFilenameField( "filename" );
    stepMetaInterface.setIncludeRowNumber( true );
    stepMetaInterface.setRowNumberField( "rownumber" );
    stepMetaInterface.setShortFileNameField( "shortname" );
    stepMetaInterface.setExtensionField( "extension" );
    stepMetaInterface.setPathField( "path" );
    stepMetaInterface.setIsHiddenField( "hidden" );
    stepMetaInterface.setLastModificationDateField( "lastmodified" );
    stepMetaInterface.setUriField( "uri" );
    stepMetaInterface.setRootUriField( "root uri" );

    // string with UTF-8 encoding
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    Object[] result = stepLoadFileInput.getOneRow();
    assertEquals( " UTF-8 string ÕÕÕ€ ", result[0] );
    assertEquals( 1L, result[2] );
    assertEquals( "UTF-8.txt", result[3] );
    assertEquals( "txt", result[4] );
    assertEquals( false, result[6] );
    assertEquals( getFile( "UTF-8.txt" ).getURL().toString(), result[8] );
    assertEquals( getFile( "UTF-8.txt" ).getName().getRootURI(), result[9] );
  }

  @Test
  public void testUTF8TrimLeft() throws KettleException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_LEFT );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( "UTF-8 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[0] );
  }

  @Test
  public void testUTF8TrimRight() throws KettleException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_RIGHT );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( " UTF-8 string ÕÕÕ€", stepLoadFileInput.getOneRow()[0] );
  }

  @Test
  public void testUTF8Trim() throws KettleException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    stepInputFiles.addFile( getFile( "UTF-8.txt" ) );
    assertEquals( "UTF-8 string ÕÕÕ€", stepLoadFileInput.getOneRow()[0] );
  }

  @Test
  public void testWindowsEncoding() throws KettleException {
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "Windows-1252" );
    inputField.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
    stepInputFiles.addFile( getFile( "Windows-1252.txt" ) );
    assertEquals( " Windows-1252 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[0] );
  }

  @Test
  public void testWithNoEncoding() throws KettleException, UnsupportedEncodingException {
    // string with Windows-1252 encoding but with no encoding set
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( null );
    stepInputFiles.addFile( getFile( "Windows-1252.txt" ) );
    assertNotEquals( " Windows-1252 string ÕÕÕ€ ", stepLoadFileInput.getOneRow()[0] );
    assertEquals( " Windows-1252 string ÕÕÕ€ ", new String( stepLoadFileInput.data.filecontent, "Windows-1252" ) );
  }

  @Test
  public void testByteArray() throws Exception {
    RowMetaInterface mockedRowMetaInterface = mock( RowMetaInterface.class );
    stepLoadFileInput.data.outputRowMeta = mockedRowMetaInterface;
    stepLoadFileInput.data.convertRowMeta = mockedRowMetaInterface;
    Mockito.doReturn( new ValueMetaString() ).when( mockedRowMetaInterface ).getValueMeta( anyInt() );

    // byte array
    Mockito.doReturn( new ValueMetaBinary() ).when( mockedRowMetaInterface ).getValueMeta( anyInt() );
    ( (LoadFileInputMeta) runtimeSMI ).setEncoding( "UTF-8" );
    stepInputFiles.addFile( getFile( "pentaho_splash.png" ) );
    inputField = new LoadFileInputField();
    inputField.setType( ValueMetaInterface.TYPE_BINARY );
    ( (LoadFileInputMeta) runtimeSMI ).setInputFields( new LoadFileInputField[]{ inputField } );

    assertNotNull( stepLoadFileInput.getOneRow() );
    assertArrayEquals( IOUtils.toByteArray( getFile( "pentaho_splash.png" ).getContent().getInputStream() ), stepLoadFileInput.data.filecontent );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithSmallerSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size - 1 ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 5,  loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithBiggerSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size + 1 ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 6,  loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

  @Test
  public void testCopyOrCloneArrayFromLoadFileWithSameSizedReadRowArray() {
    int size = 5;
    Object[] rowData = new Object[ size ];
    Object[] readrow = new Object[ size ];
    LoadFileInput loadFileInput = mock( LoadFileInput.class );

    Mockito.when( loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ) ).thenCallRealMethod();

    assertEquals( 5,  loadFileInput.copyOrCloneArrayFromLoadFile( rowData, readrow ).length );
  }

}
