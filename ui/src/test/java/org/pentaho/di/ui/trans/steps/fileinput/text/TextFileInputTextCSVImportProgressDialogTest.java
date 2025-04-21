/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.steps.fileinput.text;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.file.BaseFileErrorHandling;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.mockito.ArgumentMatchers.any;

public class TextFileInputTextCSVImportProgressDialogTest {

  private TextFileCSVImportProgressDialog textFileCSVImportProgressDialog;
  private Shell shell;
  private TextFileInputMeta meta;
  private TransMeta transMeta;
  private InputStreamReader reader;
  private IProgressMonitor monitor;
  private TextFileInputMeta.Content content;
  private BaseFileInputFiles baseFileInputFiles;
  private BaseFileErrorHandling baseFileErrorHandling;
  private BaseFileInputAdditionalField baseFileInputAdditionalField;
  private static MockedStatic<FileInputList> fileInputList;

  @Before
  public void setup() {
    fileInputList = mockStatic( FileInputList.class );

    shell = mock( Shell.class );
    meta = mock( TextFileInputMeta.class );
    transMeta = mock( TransMeta.class );
    when( transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    monitor = mock( IProgressMonitor.class );
    content = mock( TextFileInputMeta.Content.class );
    baseFileInputFiles =  mock( BaseFileInputFiles.class );
    baseFileErrorHandling = mock( BaseFileErrorHandling.class );
    baseFileInputAdditionalField = mock( BaseFileInputAdditionalField.class );
  }

  @After
  public void cleanUp() {
    fileInputList.close();
  }

  @BeforeClass
  public static void beforeClassSetUp() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void fileDOSFormatTestWithNoEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_DOS );
  }

  @Test
  public void fileDOSFormatTestWithEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "\"レコ\r\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あ\r\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_DOS );
  }

  @Test
  public void fileUnixFormatTestWithNoEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_UNIX );
  }

  @Test
  public void fileUnixFormatTestWithEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "\"レコ\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あ\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_UNIX );
  }

  @Test
  public void fileMixFormatTestWithNoEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\n"
      + "レコード名1,1.7976E308,true,9999/12/31 23:59:59.999,\r\n"
      + "\"あa1\",123456789,false,2016/1/5 12:00:00.000\r\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_MIXED );
  }

  @Test
  public void fileMixFormatTestWithEnclosures() throws Exception {
    BaseFileField[] baseFileFields = BaseFileFields( "String", "BigNumber", "Boolean", "Date" );
    String fileContent = "String, int, boolean, date\r\n"
      + "\"レコ\n"
      + "ード名1\",1.7976E308,true,9999/12/31 23:59:59.999,\n"
      + "\"あ\r\n"
      + "a1\",123456789,false,2016/1/5 12:00:00.000\n";

    fileContentTest( fileContent, baseFileFields, TextFileInputMeta.FILE_FORMAT_MIXED );
  }

  private void fileContentTest( String fileContent, BaseFileField[] baseFileFields, int fileFormat ) throws Exception {
    initiateVariables( fileContent, baseFileFields, fileFormat );
    textFileCSVImportProgressDialog.doScan( monitor );

    assertEquals( baseFileFields[0].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[0].getType() ) );
    assertEquals( baseFileFields[1].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[1].getType() ) );
    assertEquals( baseFileFields[2].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[2].getType() ) );
    assertEquals( baseFileFields[3].getName(), ValueMetaFactory.getValueMetaName( meta.getInputFields()[3].getType() ) );
  }

  private void initiateVariables( String fileContent, BaseFileField[] baseFileFields, int fileFormat ) throws Exception {
    //reader
    reader = new InputStreamReader( new ByteArrayInputStream( fileContent.getBytes( StandardCharsets.UTF_8 ) ) );
    //Content
    setInternalState( content, "header",  true );
    setInternalState( content, "nrHeaderLines",  1 );
    setInternalState( content, "escapeCharacter",  "" );
    //baseFileInputFiles
    setInternalState( baseFileInputFiles, "fileName", new String[]{} );
    setInternalState( baseFileInputFiles, "fileMask", new String[]{} );
    setInternalState( baseFileInputFiles, "excludeFileMask", new String[]{} );
    setInternalState( baseFileInputFiles, "fileRequired", new String[]{} );
    when( baseFileInputFiles.includeSubFolderBoolean() ).thenReturn( new boolean[] {} );
    when( FileInputList.createFilePathList( any( Bowl.class), any( VariableSpace.class ),
      any( String[].class ), any( String[].class ), any( String[].class ), any( String[].class ),
      any( boolean[].class ) ) ).thenReturn( new String[]{""} );
    //meta
    when( meta.getInputFields() ).thenReturn( baseFileFields );
    doCallRealMethod().when( meta ).getFields( any(), any(), any(), any(), any(), any(), any(), any() );
    setInternalState( meta, "inputFields",  baseFileFields );
    setInternalState( meta, "content",  content );
    when( meta.clone() ).thenReturn( meta );
    setInternalState( content, "separator",  "," );
    setInternalState( content, "enclosure",  "\"" );
    when( meta.getEnclosure() ).thenReturn( "\"" );
    when( meta.getFileFormatTypeNr() ).thenReturn( fileFormat );
    setInternalState( content, "fileType",  "CSV" );
    setInternalState( meta, "inputFiles", baseFileInputFiles );
    setInternalState( meta, "errorHandling", baseFileErrorHandling );
    setInternalState( meta, "additionalOutputFields", baseFileInputAdditionalField );
    //transmeta
    when( transMeta.environmentSubstitute( "," ) ).thenReturn( "," );
    when( transMeta.environmentSubstitute( "\"" ) ).thenReturn( "\"" );
    when( transMeta.getObjectType() ).thenReturn( LoggingObjectType.TRANSMETA );
    //textFileCSVImportProgressDialog
    textFileCSVImportProgressDialog = spy( new TextFileCSVImportProgressDialog(
      shell, meta, transMeta, reader, 100, true ) );
  }

  private BaseFileField[] BaseFileFields( String... names ) {
    BaseFileField[] fields = new BaseFileField[ names.length ];
    for ( int i = 0; i < names.length; i++ ) {
      fields[ i ] = createBaseFileField( names[ i ] );
    }
    return fields;
  }

  private BaseFileField createBaseFileField( String name ) {
    BaseFileField field = new BaseFileField();
    field.setName( name );
    field.setType( ValueMetaInterface.TYPE_NONE );
    return field;
  }
}
