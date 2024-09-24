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
package org.pentaho.di.core.svg;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the SvgImage class
 */
public class SvgImageTest {

  SvgImage image;
  Document document;

  @Before
  public void setUp() throws Exception {
    document = mock( Document.class );
    image = new SvgImage( document );
  }

  @Test
  public void testGetDocument() throws Exception {
    assertEquals( document, image.getDocument() );
  }
}
