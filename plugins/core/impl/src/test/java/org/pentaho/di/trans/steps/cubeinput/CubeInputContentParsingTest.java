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

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

@Ignore( "Ignored, not running with ant build. Investigate." )
public class CubeInputContentParsingTest extends BaseCubeInputParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void test() throws Exception {
    init( "input.ser" );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" }, {
        "\u043d\u0435-\u043b\u0430\u0446\u0456\u043d\u043a\u0430(non-latin)", "4", "4" } } );
  }
}
