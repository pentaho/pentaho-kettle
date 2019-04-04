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

import java.io.InputStream;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

/**
 * Class for base SVG images processing.
 */
public class SvgSupport {

  private static final String SVG_EXTENSION = ".svg";

  private static final String PNG_EXTENSION = ".png";

  private static final String PARSER = XMLResourceDescriptor.getXMLParserClassName();

  private static final ThreadLocal<SAXSVGDocumentFactory> SVG_FACTORY_THREAD_LOCAL = new ThreadLocal<SAXSVGDocumentFactory>();

  private static SAXSVGDocumentFactory createFactory() {
    return new SAXSVGDocumentFactory( PARSER );
  }

  private static SAXSVGDocumentFactory getSvgFactory() {
    SAXSVGDocumentFactory factory = SVG_FACTORY_THREAD_LOCAL.get();
    if ( factory == null ) {
      factory = createFactory();
      SVG_FACTORY_THREAD_LOCAL.set( factory );
    }
    return factory;
  }

  public static boolean isSvgEnabled() {
    return true;
  }

  /**
   * Load SVG from file.
   */
  public static SvgImage loadSvgImage( InputStream in ) throws Exception {
    Document document = getSvgFactory().createDocument( null, in );
    return new SvgImage( document );
  }

  /**
   * Check by file name if image is SVG.
   */
  public static boolean isSvgName( String name ) {
    return name.toLowerCase().endsWith( SVG_EXTENSION );
  }

  /**
   * Converts SVG file name to PNG.
   */
  public static String toPngName( String name ) {
    if ( isSvgName( name ) ) {
      name = name.substring( 0, name.length() - 4 ) + PNG_EXTENSION;
    }
    return name;
  }

  /**
   * Check by file name if image is PNG.
   */
  public static boolean isPngName( String name ) {
    return name.toLowerCase().endsWith( PNG_EXTENSION );
  }

  /**
   * Converts PNG file name to SVG.
   */
  public static String toSvgName( String name ) {
    if ( isPngName( name ) ) {
      name = name.substring( 0, name.length() - 4 ) + SVG_EXTENSION;
    }
    return name;
  }
}
