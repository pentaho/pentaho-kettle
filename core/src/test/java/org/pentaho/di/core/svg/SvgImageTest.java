/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
