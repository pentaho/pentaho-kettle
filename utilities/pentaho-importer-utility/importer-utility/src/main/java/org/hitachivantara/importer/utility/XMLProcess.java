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
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
 */

package org.hitachivantara.importer.utility;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class XMLProcess {

  private DocumentBuilder docBuilder;
  private XPath xpath;
  private Transformer transformer;
  private int count;

  public XMLProcess() {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      docFactory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
      docBuilder = docFactory.newDocumentBuilder();
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
      transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
      transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty( OutputKeys.DOCTYPE_PUBLIC, "yes" );
      transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
      transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
      transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      XPathFactory xpathfactory = XPathFactory.newInstance();
      xpath = xpathfactory.newXPath();
    } catch ( Exception e ) {
      System.exit( 1 );
    }
  }

  public String process( Enumeration<File> files, String content ) {
    count = 0;
    StringBuilder log = new StringBuilder();
    while ( files.hasMoreElements() ) {
      File file = files.nextElement();
      try {
        Document doc = docBuilder.parse( file );
        doc.getDocumentElement().normalize();
        doc.setXmlStandalone( true );

        Node extendedDescriptionNode = doc.getElementsByTagName( "extended_description" ).item( 0 );
        if ( extendedDescriptionNode != null ) {
          extendedDescriptionNode.setTextContent( content );
        } else {
          String expression =
            file.getName().toLowerCase().endsWith( ".ktr" ) ? "/transformation/info/name" : "/job/name";
          XPathExpression expr = xpath.compile( expression );
          NodeList nodeList = (NodeList) expr.evaluate( doc, XPathConstants.NODESET );
          Node nameNode = nodeList.item( 0 );
          extendedDescriptionNode = doc.createElement( "extended_description" );
          extendedDescriptionNode.setTextContent( content );
          nameNode.getParentNode().insertBefore( extendedDescriptionNode, nameNode.getNextSibling() );
        }
        DOMSource source = new DOMSource( doc );
        StreamResult result = new StreamResult( file );
        transformer.transform( source, result );
        log.append( "Done processing File: " + file.getName() + "\n" );
        count++;
      } catch ( SAXException e ) {
        log.append( "File is not a valid XML document: " + file.getName() + " <=============== ERROR!!!\n" );
      } catch ( IOException e ) {
        log.append( "File can not be read or does not exist: " + file.getName() + " <=============== ERROR!!!\n" );
      } catch ( Exception e ) {
        log.append( "Error processing File: " + file.getName() + " reason: <=============== ERROR!!!\n" );
        log.append( e.getMessage() + "\n" );
      }
    }
    return log.toString();
  }

  public int getCount() {
    return count;
  }
}
