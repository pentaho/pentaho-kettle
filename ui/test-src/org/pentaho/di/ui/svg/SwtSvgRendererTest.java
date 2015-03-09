/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.svg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.junit.Test;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.core.svg.SvgImage;
import org.pentaho.di.core.svg.SvgSupport;

public class SwtSvgRendererTest {
  @Test
  public final void testRendering() throws Exception {
    InputStream in = SvgSupportTest.class.getResourceAsStream( "/resource/svg/test.svg" );
    SvgImage svg = SvgSupport.loadSvgImage( in );
    assertNotNull( svg.getDocument() );

    Image img = new SwtUniversalImage( svg ).getAsBitmap( null );
    assertNotNull( img );
  }

  @Test
  public final void testRenderingSize() throws Exception {
    InputStream in = SvgSupportTest.class.getResourceAsStream( "/resource/svg/test.svg" );
    SvgImage svg = SvgSupport.loadSvgImage( in );
    assertNotNull( svg.getDocument() );

    Image img = new SwtUniversalImage( svg ).getAsBitmapForSize( null, 20, 30 );
    assertNotNull( img );
    assertEquals( 20, img.getImageData().width );
    assertEquals( 30, img.getImageData().height );
  }
}
