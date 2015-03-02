/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.di.ui.svg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.junit.Test;
import org.pentaho.di.core.SwtUniversalImage;

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
