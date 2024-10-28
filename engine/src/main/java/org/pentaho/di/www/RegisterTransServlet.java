/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.www;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.dom.DeferredTextImpl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class RegisterTransServlet extends BaseJobServlet {

  private static final long serialVersionUID = 468054102740138751L;
  public static final String CONTEXT_PATH = "/kettle/registerTrans";

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws IOException, KettleException  {

    final String xml = IOUtils.toString( request.getInputStream() );

    try {
      // Parse the XML, create a transformation configuration
      validateTransformation( new ByteArrayInputStream( xml.getBytes() ) );
      TransConfiguration transConfiguration = TransConfiguration.fromXML( xml );

      Trans trans = createTrans( transConfiguration );

      String message =
        "Transformation '" + trans.getName() + "' was added to Carte with id " + trans.getContainerObjectId();
      return new WebResult( WebResult.STRING_OK, message, trans.getContainerObjectId() );
    } catch ( KettleXMLException | SAXException ex ) {
      response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
      return new WebResult( WebResult.STRING_ERROR, ex.getMessage(), "" );
    } catch ( Exception ex ) {
      response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      return new WebResult( WebResult.STRING_ERROR, ex.getMessage(), "" );
    }
  }

  public void validateTransformation( InputStream is ) throws IOException, ParserConfigurationException, SAXException,
    XPathExpressionException {
    DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
    df.setFeature( "http://xml.org/sax/features/external-general-entities", false );
    df.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
    DocumentBuilder builder = df.newDocumentBuilder();
    Document doc = builder.parse( is );
    if ( !doc.getDocumentElement().getNodeName().equals( "transformation_configuration" ) ) {
      throw new SAXException( "Invalid Transformation - Missing transformation_configuration tag" );
    }
    XPath xPath = XPathFactory.newInstance().newXPath();
    Node node = (Node) xPath.evaluate( "/transformation_configuration/transformation/info/name", doc, XPathConstants.NODE );
    if ( node == null  || node.getChildNodes().getLength() > 1 || !( node.getFirstChild() instanceof DeferredTextImpl ) ) {
      throw new SAXException( "Invalid Transformation Name" );
    }
  }

}
