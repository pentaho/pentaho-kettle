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


package org.pentaho.di.trans.steps.xmlinputstream;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class XmlInputStreamInputContentParsingTest extends BaseXmlInputStreamParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Test
  public void testDefaultOptions() throws Exception {
    init( "default.xml" );

    process();

    check( new Object[][] { { "START_DOCUMENT", 0L, null, 0L, "", null, null, null },
      { "START_ELEMENT", 1L, 0L, 1L, "/xml", "", "xml", null },
      { "START_ELEMENT", 2L, 1L, 2L, "/xml/tag", "/xml", "tag", null },
      { "ATTRIBUTE", 2L, 1L, 2L, "/xml/tag", "/xml", "a", "1" },
      { "CHARACTERS", 2L, 1L, 2L, "/xml/tag", "/xml", "tag", "zz" },
      { "END_ELEMENT", 2L, 1L, 2L, "/xml/tag", "/xml", "tag", null },
      { "END_ELEMENT", 1L, 0L, 1L, "/xml", "", "xml", null }, { "END_DOCUMENT", 0L, null, 0L, "", null, null, null } } );
  }
}
