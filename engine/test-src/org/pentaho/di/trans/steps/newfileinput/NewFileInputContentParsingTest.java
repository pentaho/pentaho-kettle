package org.pentaho.di.trans.steps.newfileinput;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
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
import org.pentaho.di.trans.steps.baseinput.IBaseInputStepControl;

public class NewFileInputContentParsingTest {
  LogChannelInterface log = new LogChannel( "junit" );
  FileSystemManager fs;
  String inPrefix;
  NewFileInputMeta meta;
  NewFileInputData data;
  IBaseInputStepControl stepControl;
  List<Object[]> rows = new ArrayList<>();
  int errorsCount;

  @Before
  public void prepare() throws Exception {

    KettleEnvironment.init();
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );

    inPrefix = '/' + NewFileInputContentParsingTest.class.getPackage().getName().replace( '.', '/' ) + "/texts/";

    fs = VFS.getManager();

    meta = new NewFileInputMeta();

    meta.setDefault();

    data = new NewFileInputData();

    data.outputRowMeta = new RowMeta();

    data.separator = meta.content.separator;
    data.enclosure = meta.content.enclosure;
    data.escapeCharacter = meta.content.escapeCharacter;

    data.filterProcessor = new NewFileFilterProcessor( new NewFileFilter[0] );
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

    stepControl = new IBaseInputStepControl() {

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

  void setFields( NewFileInputField... fields ) throws Exception {
    meta.inputFiles.inputFields = fields;
    meta.getFields( data.outputRowMeta, meta.getName(), null, null, new Variables(), null, null );
    data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
  }

  @Test
  public void defaultOptions() throws Exception {
    setFields( new NewFileInputField(), new NewFileInputField(), new NewFileInputField() );

    try (NewFileInputReader reader = new NewFileInputReader( stepControl, meta, data, getFile( "default.csv" ), log )) {
      while ( reader.readRow() )
        ;
    }

    // compare rows
    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }
}
