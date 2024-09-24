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

package org.pentaho.di.trans.steps.xbaseinput;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class XBaseInputContentParsingTest extends BaseXBaseParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testDefaultOptions() throws Exception {
    init( "test.dbf" );

    process();

    check( new Object[][] { { "value11", "value12", 1.0 }, { "value21", "value22", 2.0 } } );
  }
}
