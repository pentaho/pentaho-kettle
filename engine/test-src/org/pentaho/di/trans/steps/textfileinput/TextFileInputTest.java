/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileinput;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.mockito.Mockito;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.steps.StepMockUtil;

import static org.junit.Assert.*;

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
    String output = TextFileInput.getLine( null, getInputStreamReader( input ),
      TextFileInputMeta.FILE_FORMAT_DOS, new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineUnix() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String expected = "col1\tcol2\tcol3";
    String output = TextFileInput.getLine( null, getInputStreamReader( input ),
      TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineOSX() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output = TextFileInput.getLine( null, getInputStreamReader( input ),
      TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test
  public void testGetLineMixed() throws KettleFileException, UnsupportedEncodingException {
    String input = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";
    String output = TextFileInput.getLine( null, getInputStreamReader( input ),
      TextFileInputMeta.FILE_FORMAT_MIXED, new StringBuilder( 1000 ) );
    assertEquals( expected, output );
  }

  @Test( timeout = 100 )
  public void test_PDI695() throws KettleFileException, UnsupportedEncodingException {
    String inputDOS = "col1\tcol2\tcol3\r\ndata1\tdata2\tdata3\r\n";
    String inputUnix = "col1\tcol2\tcol3\ndata1\tdata2\tdata3\n";
    String inputOSX = "col1\tcol2\tcol3\rdata1\tdata2\tdata3\r";
    String expected = "col1\tcol2\tcol3";

    assertEquals( expected, TextFileInput.getLine( null, getInputStreamReader( inputDOS ),
      TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInput.getLine( null, getInputStreamReader( inputUnix ),
      TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
    assertEquals( expected, TextFileInput.getLine( null, getInputStreamReader( inputOSX ),
      TextFileInputMeta.FILE_FORMAT_UNIX, new StringBuilder( 1000 ) ) );
  }

  @Test
  public void readWrappedInputWithoutHeaders() throws Exception {
    final String virtualFile = "ram://pdi-2607.txt";
    KettleVFS.getFileObject( virtualFile ).createFile();

    final String content = new StringBuilder()
      .append( "r1c1" ).append( '\n' ).append( ";r1c2\n" )
      .append( "r2c1" ).append( '\n' ).append( ";r2c2" )
      .toString();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write( content.getBytes() );

    OutputStream os = KettleVFS.getFileObject( virtualFile ).getContent().getOutputStream();
    IOUtils.copy( new ByteArrayInputStream( bos.toByteArray() ), os );
    os.close();


    TextFileInputMeta meta = new TextFileInputMeta();
    meta.setLineWrapped( true );
    meta.setNrWraps( 1 );
    meta.setInputFields( new TextFileInputField[] {
      new TextFileInputField( "col1", -1, -1 ),
      new TextFileInputField( "col2", -1, -1 )
    } );
    meta.setFileCompression( "None" );
    meta.setFileType( "CSV" );
    meta.setHeader( false );
    meta.setNrHeaderLines( -1 );
    meta.setFooter( false );
    meta.setNrFooterLines( -1 );

    TextFileInputData data = new TextFileInputData();
    data.setFiles( new FileInputList() );
    data.getFiles().addFile( KettleVFS.getFileObject( virtualFile ) );

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    data.outputRowMeta.addValueMeta( new ValueMetaString( "col2" ) );

    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );
    data.fileFormatType = TextFileInputMeta.FILE_FORMAT_UNIX;
    data.separator = ";";
    data.filterProcessor = new TextFileFilterProcessor( new TextFileFilter[ 0 ] );
    data.filePlayList = new FilePlayListAll();


    RowSet output = new BlockingRowSet( 5 );
    TextFileInput input = StepMockUtil.getStep( TextFileInput.class, TextFileInputMeta.class, "test" );
    input.setOutputRowSets( Collections.singletonList( output ) );
    while ( input.processRow( meta, data ) ) {
      // wait until the step completes executing
    }

    Object[] row1 = output.getRowImmediate();
    assertRow( row1, "r1c1", "r1c2" );

    Object[] row2 = output.getRowImmediate();
    assertRow( row2, "r2c1", "r2c2" );


    KettleVFS.getFileObject( virtualFile ).delete();
  }

  private static void assertRow( Object[] row, Object... values ) {
    assertNotNull( row );
    assertTrue( String.format( "%d < %d", row.length, values.length ), row.length >= values.length );
    int i = 0;
    while ( i < values.length ) {
      assertEquals( values[ i ], row[ i ] );
      i++;
    }
    while ( i < row.length ) {
      assertNull( row[ i ] );
      i++;
    }
  }

  @Test
  public void testNullForMissingValues() throws KettleException {
    int COLUMNS = 3;
    String input = "col1,,col3";
    Object[] passThruFields = null;
    int nrPassThruFields = 0;
    String fname = "dummy";
    long rowNr = 1;
    String delimiter = ",";
    String enclosure = "\"";
    String escapeCharacters = null;
    FileErrorHandler errorHandler = null;
    boolean addShortFilename = false;
    boolean addExtension = false;
    boolean addPath = false;
    boolean addSize = false;
    boolean addishidden = false;
    boolean addLastModificationDate = false;
    boolean addUri = false;
    boolean addRootUri = false;
    String shortFileName = "dummy";
    String path = "dummy";
    boolean hidden = false;
    Date modificationDateTime = null;
    String uri = "dummy";
    String rooturi = "dummy";
    String extension = "dummy";
    long size = 0;


    // Create textfile line
    TextFileLine textFileLine = new TextFileLine( input, 1, null );

    // mock LogChannelInterface
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isRowLevel() ).thenReturn( false );

    // mock text file input field
    TextFileInputField inputfield = mock( TextFileInputField.class );
    when( inputfield.getNullString() ).thenReturn( null );
    when( inputfield.getIfNullValue() ).thenReturn( null );
    when( inputfield.getTrimType() ).thenReturn( ValueMetaInterface.TRIM_TYPE_NONE );


    // mock InputFileMetaInterface
    InputFileMetaInterface info = mock( InputFileMetaInterface.class );
    when( info.getFileType() ).thenReturn( "CSV" );
    TextFileInputField[] inputFields = new TextFileInputField[COLUMNS];
    for ( int i = 0; i < COLUMNS; i++ ) {
      inputFields[i] = inputfield;
    }
    when( info.getInputFields() ).thenReturn( inputFields );
    when( info.getEscapeCharacter() ).thenReturn( null );
    when( info.isErrorIgnored() ).thenReturn( false );
    when( info.isErrorLineSkipped() ).thenReturn( false );
    when( info.includeFilename() ).thenReturn( false );
    when( info.includeRowNumber() ).thenReturn( false );
    when( info.includeFilename() ).thenReturn( false );

    // create outputRowMeta
    RowMetaInterface outputRowMeta = new RowMeta();
    ValueMetaInterface v = new ValueMeta( "dummy", ValueMeta.TYPE_STRING );
    for ( int i = 0; i < COLUMNS; i++ ) {
      outputRowMeta.addValueMeta( v.clone() );
    }

    // create convert row meta
    RowMetaInterface convertRowMeta = outputRowMeta.clone();


    // test nullForMissingValue = true
    Object[] nullResultActual = TextFileInput.convertLineToRow( log, textFileLine, info, passThruFields, nrPassThruFields,
        outputRowMeta, convertRowMeta, fname, rowNr, delimiter, enclosure, escapeCharacters, errorHandler,
        addShortFilename, addExtension, addPath, addSize, addishidden, addLastModificationDate, addUri, addRootUri,
        shortFileName, path, hidden, modificationDateTime, uri, rooturi, extension, size, true );
    assertEquals( "Missing values should be null", null, nullResultActual[1] );

    // test nullForMissingValue = false
    Object[] stringResultActual = TextFileInput.convertLineToRow( log, textFileLine, info, passThruFields, nrPassThruFields,
        outputRowMeta, convertRowMeta, fname, rowNr, delimiter, enclosure, escapeCharacters, errorHandler,
        addShortFilename, addExtension, addPath, addSize, addishidden, addLastModificationDate, addUri, addRootUri,
        shortFileName, path, hidden, modificationDateTime, uri, rooturi, extension, size, false );
    assertEquals( "Missing values should be NULL_STRING constant", Const.NULL_STRING, stringResultActual[1] );
  }
}
