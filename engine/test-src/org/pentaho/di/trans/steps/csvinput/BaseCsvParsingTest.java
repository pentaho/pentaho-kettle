package org.pentaho.di.trans.steps.csvinput;

import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.BaseParsingTest;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Base class for all CSV input step tests.
 */
@Ignore( "No tests in abstract base class" )
public abstract class BaseCsvParsingTest extends BaseParsingTest<CsvInputMeta, CsvInputData, CsvInput> {
  /**
   * Initialize step info.
   */
  @Before
  public void before() {
    meta = new CsvInputMeta();
    meta.setDefault();

    data = new CsvInputData();
    data.outputRowMeta = new RowMeta();
  }

  /**
   * Initialize for processing specified file.
   */
  protected void init( String file ) throws Exception {
    meta.setFilename( getFile( file ).getURL().getFile() );

    step = new CsvInput( stepMeta, null, 1, transMeta, trans );
    step.init( meta, data );
    step.addRowListener( rowListener );
  }

  /**
   * Declare fields for test.
   */
  protected void setFields( TextFileInputField... fields ) throws Exception {
    meta.setInputFields( fields );
    meta.getFields( data.outputRowMeta, meta.getName(), null, null, new Variables(), null, null );
    data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
  }

  /**
   * For BaseFileInput fields.
   */
  @Override
  protected void setFields( BaseFileInputField... fields ) throws Exception {
    throw new RuntimeException( "Not implemented" );
  }

  /**
   * CSV input step produces byte arrays instead strings.
   */
  @Override
  protected void check( Object[][] expected ) throws Exception {
    for ( int r = 0; r < expected.length; r++ ) {
      for ( int c = 0; c < expected[r].length; c++ ) {
        expected[r][c] = expected[r][c].toString().getBytes( "UTF-8" );
      }
    }
    super.check( expected );
  }
}
