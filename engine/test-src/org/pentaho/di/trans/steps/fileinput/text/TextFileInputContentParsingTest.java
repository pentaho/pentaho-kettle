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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayList;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.IBaseFileInputStepControl;

public class TextFileInputContentParsingTest {
  LogChannelInterface log = new LogChannel( "junit" );
  FileSystemManager fs;
  String inPrefix;
  TextFileInputMeta meta;
  TextFileInputData data;
  IBaseFileInputStepControl stepControl;
  List<Object[]> rows = new ArrayList<>();
  int errorsCount;

  public void init() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();

    init( m );
  }

  public void init( TextFileInputMeta meta ) throws Exception {
    KettleEnvironment.init();
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );

    inPrefix = '/' + TextFileInputContentParsingTest.class.getPackage().getName().replace( '.', '/' ) + "/texts/";

    fs = VFS.getManager();

    this.meta = meta;

    data = new TextFileInputData();

    data.outputRowMeta = new RowMeta();

    data.separator = meta.content.separator;
    data.enclosure = meta.content.enclosure;
    data.escapeCharacter = meta.content.escapeCharacter;

    data.filterProcessor = new TextFileFilterProcessor( new TextFileFilter[0], new Variables() );
    data.dataErrorLineHandler = new FileErrorHandler() {
      @Override
      public void handleNonExistantFile( FileObject file ) throws KettleException {
        errorsCount++;
      }

      @Override
      public void handleNonAccessibleFile( FileObject file ) throws KettleException {
        errorsCount++;
      }

      @Override
      public void handleLineError( long lineNr, String filePart ) throws KettleException {
        errorsCount++;
      }

      @Override
      public void handleFile( FileObject file ) throws KettleException {
      }

      @Override
      public void close() throws KettleException {
      }
    };

    stepControl = new IBaseFileInputStepControl() {

      @Override
      public void stopAll() {
      }

      @Override
      public void setErrors( long e ) {
      }

      @Override
      public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        rows.add( Arrays.copyOf( row, rowMeta.size() ) );
      }

      @Override
      public long incrementLinesUpdated() {
        return 0;
      }

      @Override
      public long incrementLinesInput() {
        return 0;
      }

      @Override
      public long getLinesWritten() {
        return 0;
      }

      @Override
      public long getLinesInput() {
        return 0;
      }

      @Override
      public long getErrors() {
        return 0;
      }

      @Override
      public boolean failAfterBadFile( String errorMsg ) {
        return false;
      }

      @Override
      public boolean checkFeedback( long lines ) {
        return false;
      }
    };
    data.filePlayList = new FilePlayList() {
      public boolean isProcessingNeeded( FileObject file, long lineNr, String filePart ) throws KettleException {
        return true;
      }
    };
  }

  void check( Object[][] expected ) {
    assertEquals( "There are errors", 0, errorsCount );
    assertEquals( "Wrong rows count", expected.length, rows.size() );
    for ( int i = 0; i < expected.length; i++ ) {
      assertArrayEquals( "Wrong row: " + Arrays.asList( rows.get( i ) ), expected[i], rows.get( i ) );
    }
  }

  FileObject getFile( String filename ) throws Exception {
    FileObject file = fs.resolveFile( this.getClass().getResource( inPrefix + filename ).toExternalForm() );
    assertNotNull( "There is no file", file );
    return file;
  }

  void setFields( BaseFileInputField... fields ) throws Exception {
    meta.inputFiles.inputFields = fields;
    meta.getFields( data.outputRowMeta, meta.getName(), null, null, new Variables(), null, null );
    data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
  }

  @Test
  public void defaultOptions() throws Exception {
    init();

    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "default.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void separator() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.separator = ",";
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "separator.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third;third", "3", "3.3" } } );
  }

  @Test
  public void escape() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.escapeCharacter = "\\";
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "escape.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third;third", "3", "3.3" } } );
  }

  @Test
  public void header() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.header = false;
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "default.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "Field 1", "Field 2", "Field 3" }, { "first", "1", "1.1" }, { "second", "2", "2.2" }, {
      "third", "3", "3.3" } } );
  }

  @Test
  public void compression() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.fileCompression = "GZip";
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "default.csv.gz" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
    public void fixed() throws Exception {
      TextFileInputMeta m = new TextFileInputMeta();
      m.setDefault();
      m.content.fileType = "Fixed";
      init( m );
  
      setFields( new BaseFileInputField( "f1", 0, 7 ), new BaseFileInputField( "f2", 8, 7 ), new BaseFileInputField( "f3",
          16, 7 ) );
  
      try (TextFileInputReader reader = new TextFileInputReader( stepControl, meta, data, getFile( "fixed.csv" ), log )) {
        while ( reader.readRow() )
          ;
      }
  
      // compare rows
      check( new Object[][] { { "first  ", "1      ", "1.1" }, { "second ", "2      ", "2.2" }, { "third  ", "3      ",
        "3.3" } } );
    }

  @Test
  public void testFilterVariables() throws Exception {
    init();

    Variables vars = new Variables();
    vars.setVariable( "VAR_TEST", "second" );
    data.filterProcessor =
        new TextFileFilterProcessor( new TextFileFilter[] { new TextFileFilter( 0, "${VAR_TEST}", false, false ) }, vars );
    setFields( new BaseFileInputField(), new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader = new TextFileInputReader( stepControl, meta, data, getFile( "default.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void testBOM_UTF8() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.encoding = "UTF-32LE";
    m.content.header = false;
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "test-BOM-UTF-8.txt" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "data", "1" } } );
  }

  @Test
  public void testBOM_UTF16BE() throws Exception {
    TextFileInputMeta m = new TextFileInputMeta();
    m.setDefault();
    m.content.encoding = "UTF-32LE";
    m.content.header = false;
    init( m );

    setFields( new BaseFileInputField(), new BaseFileInputField() );

    try (TextFileInputReader reader =
        new TextFileInputReader( stepControl, meta, data, getFile( "test-BOM-UTF-16BE.txt" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "data", "1" } } );
  }
}
