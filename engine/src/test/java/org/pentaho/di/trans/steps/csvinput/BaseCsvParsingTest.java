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


package org.pentaho.di.trans.steps.csvinput;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.file.BaseFileField;
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
    init( file, false );
  }

  protected void init( String file, boolean absolutePath ) throws Exception {

    if ( absolutePath ) {
      meta.setFilename( file );
    } else {
      meta.setFilename( getFile( file ).getURL().getFile() );
    }

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
  protected void setFields( BaseFileField... fields ) throws Exception {
    throw new RuntimeException( "Not implemented" );
  }

  /**
   * CSV input step produces byte arrays instead strings.
   */
  @Override
  protected void check( Object[][] expected ) throws Exception {

    for ( int r = 0; r < expected.length; r++ ) {
      if ( expected[r].length != 0 ) {
        for ( int c = 0; c < expected[ r ].length; c++ ) {
          if ( expected[ r ][ c ] == "" ) {
            expected[ r ][ c ] = StringUtils.EMPTY.getBytes( "UTF-8" );
          } else if ( expected[ r ][ c ] == null ) {
            expected[ r ][ c ] = null;
          } else {
            expected[ r ][ c ] = expected[ r ][ c ].toString().getBytes( "UTF-8" );
          }
        }
      } else {
        expected[r] = new Object[ data.fieldsMapping.size() ];
        expected[r][0] = StringUtils.EMPTY.getBytes( "UTF-8" );
      }
    }
    super.check( expected );
  }
}
