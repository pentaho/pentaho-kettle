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

package org.pentaho.di.trans.steps.fixedinput;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class FixedInputContentParsingTest extends BaseFixedParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testDefaultOptions() throws Exception {
    meta.setLineWidth( "24" );
    init( "default.txt" );

    FixedFileInputField f1 = new FixedFileInputField( "f1" );
    FixedFileInputField f2 = new FixedFileInputField( "f2" );
    FixedFileInputField f3 = new FixedFileInputField( "f2" );
    f1.setWidth( 8 );
    f2.setWidth( 8 );
    f3.setWidth( 8 );
    setFields( f1, f2, f3 );

    process();

    check( new Object[][] { { "first   ", "1       ", "1.1     " }, { "second  ", "2       ", "2.2     " }, {
        "third   ", "3       ", "3.3     " } } );
  }
}
