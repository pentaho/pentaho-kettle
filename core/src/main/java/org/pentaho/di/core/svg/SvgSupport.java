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
