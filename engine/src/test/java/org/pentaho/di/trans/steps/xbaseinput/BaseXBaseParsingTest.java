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


package org.pentaho.di.trans.steps.xbaseinput;

import org.apache.commons.vfs2.FileObject;
import org.junit.Before;
import org.junit.Ignore;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.fileinput.BaseParsingTest;

/**
 * Base class for all XBase input step tests.
 */
@Ignore( "No tests in abstract base class" )
public class BaseXBaseParsingTest extends BaseParsingTest<XBaseInputMeta, XBaseInputData, XBaseInput> {
  /**
   * Initialize step info.
   */
  @Before
  public void before() {
    inPrefix = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";

    meta = new XBaseInputMeta();
    meta.setDefault();

    data = new XBaseInputData();
    data.outputRowMeta = new RowMeta();
  }

  /**
   * Initialize for processing specified file.
   */
  protected void init( String file ) throws Exception {
    FileObject fo = getFile( file );
    meta.setDbfFileName( fo.getName().getPath() );

    init();
  }

  /**
   * Initialize for processing specified file with compression.
   */
  protected void init( String file, String fileCompression ) throws Exception {
    FileObject fo = getFile( file );
    meta.setDbfFileName( fo.getName().getPath() );
    meta.setFileCompression( fileCompression );

    init();
  }

  /**
   * Initialize XBase input from file defined in meta.
   */
  private void init() {
    step = new XBaseInput( stepMeta, null, 1, transMeta, trans );
    step.init( meta, data );
    step.addRowListener( rowListener );
  }

  /**
   * For BaseFileInput fields.
   */
  @Override
  protected void setFields( BaseFileField... fields ) throws Exception {
    throw new RuntimeException( "Will be implemented after switch to the BaseFileInput" );
  }
}
