/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.cubeinput;

import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.fileinput.BaseParsingTest;

/**
 * Base class for all Cube Input step tests.
 */
@Ignore( "No tests in abstract base class" )
public class BaseCubeInputParsingTest extends BaseParsingTest<CubeInputMeta, CubeInputData, CubeInput> {
  /**
   * Initialize step info.
   */
  @Before
  public void before() {
    meta = new CubeInputMeta();
    meta.setDefault();

    data = new CubeInputData();
    data.meta = new RowMeta();
  }

  /**
   * Initialize for processing specified file.
   */
  protected void init( String file ) throws Exception {
    meta.setFilename( getFile( file ).getURL().getFile() );

    step = new CubeInput( stepMeta, null, 1, transMeta, trans );
    step.init( meta, data );
    step.addRowListener( rowListener );
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
      for ( int c = 0; c < expected[r].length; c++ ) {
        expected[r][c] = expected[r][c].toString().getBytes( "UTF-8" );
      }
    }
    super.check( expected );
  }
}
