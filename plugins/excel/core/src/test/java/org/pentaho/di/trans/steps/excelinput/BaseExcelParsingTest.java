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


package org.pentaho.di.trans.steps.excelinput;

import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.fileinput.BaseParsingTest;

/**
 * Base class for all Fixed input step tests.
 */
@Ignore( "No tests in abstract base class" )
public class BaseExcelParsingTest extends BaseParsingTest<ExcelInputMeta, ExcelInputData, ExcelInput> {
  /**
   * Initialize step info.
   */
  @Before
  public void before() {
    inPrefix = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";

    meta = new ExcelInputMeta();
    meta.setDefault();

    data = new ExcelInputData();
    data.outputRowMeta = new RowMeta();
  }

  /**
   * Initialize for processing specified file.
   */
  protected void init( String file ) throws Exception {
    meta.setFileName( new String[] { getFile( file ).getURL().getFile() } );
    meta.setFileMask( new String[] { "" } );
    meta.setExcludeFileMask( new String[] { "" }  );
    meta.setFileRequired( new String[] { "Y" } );
    meta.setIncludeSubFolders( new String[] { "N" } );

    step = new ExcelInput( stepMeta, null, 1, transMeta, trans );
    step.init( meta, data );
    step.addRowListener( rowListener );
  }

  /**
   * Declare fields for test.
   */
  protected void setFields( ExcelInputField... fields ) throws Exception {
    meta.setField( fields );
    meta.getFields( data.outputRowMeta, meta.getName(), null, null, new Variables(), null, null );
  }

  /**
   * For BaseFileInput fields.
   */
  @Override
  protected void setFields( BaseFileField... fields ) throws Exception {
    throw new RuntimeException( "Not implemented" );
  }
}
