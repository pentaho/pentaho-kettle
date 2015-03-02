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

import java.io.InputStream;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

/**
 * Class for base SVG images processing.
 */
public class SvgSupport {
  private static final SAXSVGDocumentFactory SVG_FACTORY;
  /** True if SVG support enabled in applications. */
  private static boolean svgEnabled = true;

  static {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SVG_FACTORY = new SAXSVGDocumentFactory( parser );
  }

  public static boolean isSvgEnabled() {
    return svgEnabled;
  }

  /**
   * Enable SVG support.
   */
  public static void enable() {
    svgEnabled = true;
  }

  /**
   * Disable SVG support.
   */
  public static void disable() {
    svgEnabled = false;
  }

  /**
   * Load SVG from file.
   */
  public static SvgImage loadSvgImage( InputStream in ) throws Exception {
    Document document = SVG_FACTORY.createDocument( null, in );
    return new SvgImage( document );
  }

  /**
   * Check by file name if image is SVG.
   */
  public static boolean isSvgName( String name ) {
    return name.toLowerCase().endsWith( ".svg" );
  }

  /**
   * Converts SVG file name to PNG.
   */
  public static String toPngName( String name ) {
    if ( isSvgName( name ) ) {
      name = name.substring( 0, name.length() - 4 ) + ".png";
    }
    return name;
  }
}
