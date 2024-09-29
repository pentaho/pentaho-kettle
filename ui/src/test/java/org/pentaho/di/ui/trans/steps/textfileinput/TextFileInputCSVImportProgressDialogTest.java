/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.textfileinput;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

public class TextFileInputCSVImportProgressDialogTest {

  private TextFileCSVImportProgressDialog textFileCSVImportProgressDialog;
  private Shell shell;
  private CsvInputMeta meta;
  private TransMeta transMeta;
  private InputStreamReader reader;
  private IProgressMonitor monitor;

  @Before
  public void setup() {
    shell = mock( Shell.class );
    meta = mock( CsvInputMeta.class );
    transMeta = mock( TransMeta.class );
    monitor = mock( IProgressMonitor.class );
  }

  @BeforeClass
  public static void beforeClassSetUp() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void fileDOSFormatTestWithNoEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_DOS );
  }

  @Test
  public void fileDOSFormatTestWithEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "\"レコ\r\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あ\r\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_DOS );
  }

  @Test
  public void fileUnixFormatTestWithNoEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_UNIX );
  }

  @Test
  public void fileUnixFormatTestWithEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "\"レコ\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あ\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_UNIX );
  }

  @Test
  public void fileMixFormatTestWithNoEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_MIXED );
  }

  @Test
  public void fileMixFormatTestWithEnclosures() throws Exception {
    TextFileInputField[] inputFileFields = createInputFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "\"レコ\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あ\r\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, inputFileFields, TextFileInputMeta.FILE_FORMAT_MIXED );
  }

  private void fileContentTest( String fileContent, TextFileInputField[] inputFileFields, int fileFormat ) throws Exception {
    initiateVariables( fileContent, inputFileFields, fileFormat );
    String result = textFileCSVImportProgressDialog.doScan( monitor );

    assertEquals( inputFileFields[0].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[0].getType() ) );
    assertEquals( inputFileFields[1].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[1].getType() ) );
    assertEquals( inputFileFields[2].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[2].getType() ) );
    assertEquals( inputFileFields[3].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[3].getType() ) );
  }

  private void initiateVariables( String fileContent, TextFileInputField[] inputFileFields, int fileFormat ) throws Exception {
    //reader
    reader = new InputStreamReader( new ByteArrayInputStream( fileContent.getBytes( StandardCharsets.UTF_8 ) ) );
    //meta
    when( meta.getInputFields() ).thenReturn( inputFileFields );
    doCallRealMethod().when( meta ).getFields( any(), any(), any(), any(), any(), any(), any() );
    setInternalState( meta, "inputFields",  inputFileFields );
    when( meta.hasHeader() ).thenReturn( true );
    when( meta.getNrHeaderLines() ).thenReturn( 1 );
    when( meta.clone() ).thenReturn( meta );
    when( meta.getSeparator() ).thenReturn( "," );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.getFileFormatTypeNr() ).thenReturn( fileFormat );
    when( meta.getFileType() ).thenReturn( "CSV" );
    when( meta.getFilePaths( any( VariableSpace.class ) ) ).thenReturn( new String[] {"empty"} );
    //transmeta
    when( transMeta.environmentSubstitute( "," ) ).thenReturn( "," );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( transMeta.getObjectType() ).thenReturn( LoggingObjectType.TRANSMETA );
    //textFileCSVImportProgressDialog
    textFileCSVImportProgressDialog = spy( new TextFileCSVImportProgressDialog(
      shell, meta, transMeta, reader, 100, true ) );
  }

  private TextFileInputField[] createInputFileFields( String... names ) {
    TextFileInputField[] fields = new TextFileInputField[ names.length ];
    for ( int i = 0; i < names.length; i++ ) {
      fields[ i ] = createField( names[ i ] );
    }
    return fields;
  }

  private TextFileInputField createField( String name ) {
    TextFileInputField field = new TextFileInputField();
    field.setName( name );
    field.setType( ValueMetaInterface.TYPE_NONE );
    return field;
  }
}
