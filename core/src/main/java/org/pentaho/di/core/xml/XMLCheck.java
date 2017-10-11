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
