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

package org.pentaho.di.core.svg;

import java.io.InputStream;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

/**
 * Class for base SVG images processing.
 */
public class SvgSupport {
  private static final SAXSVGDocumentFactory SVG_FACTORY;

  static {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SVG_FACTORY = new SAXSVGDocumentFactory( parser );
  }

  public static boolean isSvgEnabled() {
    return true;
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
