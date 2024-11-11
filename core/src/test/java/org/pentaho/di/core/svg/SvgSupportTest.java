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

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for the SvgSupport class
 */
public class SvgSupportTest {

  public static final String svgImage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
    + "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\"\n"
    + "\twidth=\"38\" height=\"32\"  viewBox=\"0 0 39.875 33.6667\">\n"
    + "<path style=\"stroke: none; fill: #323296;\" d=\"M 10,0 L 30.5,0 39.875,17.5 30.5,33.6667 10,33.6667 L 0,17.5"
    + "L 10,0 z\"/>\n</svg>";

  @Before
  public void setUp() throws Exception {
    // This isn't really a setup line, instead it is here to show that we didn't privatize the default constructor.
    // It will stop compiling if we make that change to the code. However it should probably be refactored a bit
    // further than that, to better support unit testing.
    new SvgSupport();

  }

  @Test
  public void testIsSvgEnabled() throws Exception {
    assertTrue( SvgSupport.isSvgEnabled() );
  }

  @Test
  public void testLoadSvgImage() throws Exception {
    SvgImage image = SvgSupport.loadSvgImage( new ByteArrayInputStream( svgImage.getBytes() ) );
    assertNotNull( image );
  }

  @Test
  public void testToPngName() throws Exception {
    assertTrue( SvgSupport.isPngName( "my_file.png" ) );
    assertTrue( SvgSupport.isPngName( "my_file.PNG" ) );
    assertTrue( SvgSupport.isPngName( ".png" ) );
    assertFalse( SvgSupport.isPngName( "png" ) );
    assertFalse( SvgSupport.isPngName( "myFile.svg" ) );
    assertEquals( "myFile.png", SvgSupport.toPngName( "myFile.svg" ) );
  }

  @Test
  public void testToSvgName() throws Exception {
    assertTrue( SvgSupport.isSvgName( "my_file.svg" ) );
    assertTrue( SvgSupport.isSvgName( "my_file.SVG" ) );
    assertTrue( SvgSupport.isSvgName( ".svg" ) );
    assertFalse( SvgSupport.isSvgName( "svg" ) );
    assertFalse( SvgSupport.isSvgName( "myFile.png" ) );
    assertEquals( "myFile.svg", SvgSupport.toSvgName( "myFile.png" ) );
  }
}
