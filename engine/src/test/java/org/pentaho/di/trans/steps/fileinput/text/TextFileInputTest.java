/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;
import org.pentaho.di.utils.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TextFileInputTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private static InputStreamReader getInputStreamReader( String data ) throws UnsupportedEncodingException {
    return new InputStreamReader( new ByteArrayInputStream( data.getBytes( ( "UTF-8" ) ) ) );
  }

  @Test
  public void testGetLineDOS() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r\n";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_DOS,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineUnix() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_UNIX,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineOSX() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_UNIX,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineMixed() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output =
        TextFileInputUtils.getLine( null, getInputStreamReader( input ), TextFileInputMeta.FILE_FORMAT_MIXED,
            new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test( timeout = 100 )
  public void test_PDI695() throws KettleFileException, UnsupportedEncodingException {
    String inputDOS = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r\n";
    String inputUnix = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String inputOSX = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";

    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputDOS ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputUnix ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInputUtils.getLine( null, getInputStreamReader( inputOSX ),
        TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
  }

  @Test
  public void readWrappedInputWithoutHeaders() throws Exception {
    final String content = new StringBuilder()
      .append( "r1c1" ).append( '\n' ).append( ";r1c2\n" )
      .append( "r2c1" ).append( '\n' ).append( ";r2c2" )
      .toString();
    final String virtualFile = createVirtualFile( "pdi-2607.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field( "col2" ) );
    meta.content.lineWrapped = true;
    meta.content.nrWraps = 1;

    TextFileInputData data = createDataObject( virtualFile, ";", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "r1c1", "r1c2" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "r2c1", "r2c2" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithMissedValues() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14172.txt", "1,1,1\n", "2,,2\n" );

    BaseFileField field2 = field( "col2" );
    field2.setRepeated( true );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), field2, field( "col3" ) );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2", "col3" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 2, false );
    TransTestingUtil.assertResult( new Object[] { "1", "1", "1" }, output.get( 0 ) );
    TransTestingUtil.assertResult( new Object[] { "2", "1", "2" }, output.get( 1 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithNonEmptyNullif() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14358.txt", "-,-\n" );

    BaseFileField col2 = field( "col2" );
    col2.setNullString( "-" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 1, false );
    TransTestingUtil.assertResult( new Object[] { "-" }, output.get( 0 ) );

    deleteVfsFile( virtualFile );
  }

  @Test
  public void readInputWithDefaultValues() throws Exception {
    final String virtualFile = createVirtualFile( "pdi-14832.txt", "1,\n" );

    BaseFileField col2 = field( "col2" );
    col2.setIfNullValue( "DEFAULT" );

    TextFileInputMeta meta = createMetaObject( field( "col1" ), col2 );
    TextFileInputData data = createDataObject( virtualFile, ",", "col1", "col2" );

    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 1, false );
    TransTestingUtil.assertResult( new Object[] { "1", "DEFAULT" }, output.get( 0 ) );

    deleteVfsFile( virtualFile );
  }
  @Test
  public void testErrorHandlerLineNumber() throws Exception {
    final String content = new StringBuilder()
      .append( "123" ).append( '\n' ).append( "333\n" )
      .append( "345" ).append( '\n' ).append( "773\n" )
      .append( "aaa" ).append( '\n' ).append( "444" )
      .toString();
    final String virtualFile = createVirtualFile( "pdi-2607.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ) );

    meta.inputFields[0].setType( 1 );
    meta.content.lineWrapped = false;
    meta.content.nrWraps = 1;
    meta.errorHandling.errorIgnored = true;
    TextFileInputData data = createDataObject( virtualFile, ";", "col1" );
    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );
    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );

    List<Object[]> output = TransTestingUtil.execute( input, meta, data, 4, false );

    Mockito.verify( data.dataErrorLineHandler ).handleLineError( 4, AbstractFileErrorHandler.NO_PARTS );
    deleteVfsFile( virtualFile );
  }

  @Test
  public void testHandleOpenFileException() throws Exception {
    final String content = new StringBuilder()
      .append( "123" ).append( '\n' ).append( "333\n" ).toString();
    final String virtualFile = createVirtualFile( "pdi-16697.txt", content );

    TextFileInputMeta meta = createMetaObject( field( "col1" ) );

    meta.inputFields[ 0 ].setType( 1 );
    meta.errorHandling.errorIgnored = true;
    meta.errorHandling.skipBadFiles = true;

    TextFileInputData data = createDataObject( virtualFile, ";", "col1" );
    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );

    TestTextFileInput textFileInput = StepMockUtil.getStep( TestTextFileInput.class, TextFileInputMeta.class, "test" );
    StepMeta stepMeta = textFileInput.getStepMeta();
    Mockito.doReturn( true ).when( stepMeta ).isDoingErrorHandling();

    List<Object[]> output = TransTestingUtil.execute( textFileInput, meta, data, 0, false );

    deleteVfsFile( virtualFile );

    assertEquals( 1, data.rejectedFiles.size() );
    assertEquals( 0, textFileInput.getErrors() );
  }

  private TextFileInputMeta createMetaObject( BaseFileField... fields ) {
    TextFileInputMeta meta = new TextFileInputMeta();
    meta.content.fileCompression = "None";
    meta.content.fileType = "CSV";
    meta.content.header = false;
    meta.content.nrHeaderLines = -1;
    meta.content.footer = false;
    meta.content.nrFooterLines = -1;

    meta.inputFields = fields;
    return meta;
  }

  private TextFileInputData createDataObject( String file,
                                              String separator,
                                              String... outputFields ) throws Exception {
    TextFileInputData data = new TextFileInputData();
    data.files = new FileInputList();
    data.files.addFile( KettleVFS.getFileObject( file ) );

    data.separator = separator;

    data.outputRowMeta = new RowMeta();
    if ( outputFields != null ) {
      for ( String field : outputFields ) {
        data.outputRowMeta.addValueMeta( new ValueMetaString( field ) );
      }
    }

    data.dataErrorLineHandler = mock( FileErrorHandler.class );
    data.fileFormatType = TextFileInputMeta.FILE_FORMAT_UNIX;
    data.filterProcessor = new TextFileFilterProcessor( new TextFileFilter[ 0 ], new Variables() );
    data.filePlayList = new FilePlayListAll();
    return data;
  }
  private static String createVirtualFile( String filename, String... rows ) throws Exception {
    String virtualFile = TestUtils.createRamFile( filename );

    StringBuilder content = new StringBuilder();
    if ( rows != null ) {
      for ( String row : rows ) {
        content.append( row );
      }
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write( content.toString().getBytes() );

    try ( OutputStream os = KettleVFS.getFileObject( virtualFile ).getContent().getOutputStream() ) {
      IOUtils.copy( new ByteArrayInputStream( bos.toByteArray() ), os );
    }

    return virtualFile;
  }

  private static void deleteVfsFile( String path ) throws Exception {
    TestUtils.getFileObject( path ).delete();
  }

  private static BaseFileField field( String name ) {
    return new BaseFileField( name, -1, -1 );
  }

  public static class TestTextFileInput extends TextFileInput {
    public TestTextFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                              Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    @Override
    protected IBaseFileInputReader createReader( TextFileInputMeta meta, TextFileInputData data, FileObject file )
      throws Exception {
      throw new Exception( "Can not create reader for the file object " + file );
    }
  }
}
