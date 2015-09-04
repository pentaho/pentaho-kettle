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

package org.pentaho.di.trans.steps.fileinput.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;

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
    final String virtualFile = "ram://pdi-2607.txt";
    KettleVFS.getFileObject( virtualFile ).createFile();

    final String content =
        new StringBuilder().append( "r1c1" ).append( '\n' ).append( ";r1c2\n" ).append( "r2c1" ).append( '\n' ).append(
            ";r2c2" ).toString();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write( content.getBytes() );

    OutputStream os = KettleVFS.getFileObject( virtualFile ).getContent().getOutputStream();
    IOUtils.copy( new ByteArrayInputStream( bos.toByteArray() ), os );
    os.close();

    TextFileInputMeta meta = new TextFileInputMeta();
    meta.content.lineWrapped = true ;
    meta.content.nrWraps = 1;
    meta.inputFiles.inputFields =
        new BaseFileInputField[] { new BaseFileInputField( "col1", -1, -1 ), new BaseFileInputField( "col2", -1, -1 ) };
    meta.content.fileCompression = "None" ;
    meta.content.fileType =  "CSV" ;
    meta.content.header= false ;
    meta.content.nrHeaderLines= -1 ;
    meta.content.footer =false ;
    meta.content.nrFooterLines =  -1 ;

    TextFileInputData data = new TextFileInputData();
    data.files = new FileInputList();
    data.files.addFile( KettleVFS.getFileObject( virtualFile ) );

    data.outputRowMeta = new RowMeta();
    data.outputRowMeta.addValueMeta( new ValueMetaString( "col1" ) );
    data.outputRowMeta.addValueMeta( new ValueMetaString( "col2" ) );

    data.dataErrorLineHandler = Mockito.mock( FileErrorHandler.class );
    data.fileFormatType = TextFileInputMeta.FILE_FORMAT_UNIX;
    data.separator = ";";
    data.filterProcessor = new TextFileFilterProcessor( new TextFileFilter[0], new Variables() );
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
      assertEquals( values[i], row[i] );
      i++;
    }
    while ( i < row.length ) {
      assertNull( row[i] );
      i++;
    }
  }
}
