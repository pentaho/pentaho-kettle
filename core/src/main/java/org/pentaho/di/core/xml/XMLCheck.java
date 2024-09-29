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


package org.pentaho.di.core.xml;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLCheck {

  public static class XMLTreeHandler extends DefaultHandler {

  }

  /**
   * Checks an xml file is well formed.
   *
   * @param file
   *          The file to check
   * @return true if the file is well formed.
   */
  public static final boolean isXMLFileWellFormed( FileObject file ) throws KettleException {
    boolean retval = false;
    try {
      retval = isXMLWellFormed( file.getContent().getInputStream() );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }

    return retval;
  }

  /**
   * Checks an xml string is well formed.
   *
   * @param is
   *          inputstream
   * @return true if the xml is well formed.
   */
  public static boolean isXMLWellFormed( InputStream is ) throws KettleException {
    boolean retval = false;
    try {
      SAXParserFactory factory = XMLParserFactoryProducer.createSecureSAXParserFactory();
      XMLTreeHandler handler = new XMLTreeHandler();

      // Parse the input.
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse( is, handler );
      retval = true;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
    return retval;
  }

}
