/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.docker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Points the SSH steps of a KTR to the services of the test network.
 * The KTRs in ssh-test-environment use localhost ports for Spoon. Inside the network the same servers have container names.
 */
final class SshKtrRewriter {

  static final String KEY_IN_CONTAINER = "/tmp/docker-it/ssh/test_rsa";

  private static final Map<String, String> ENDPOINTS = Map.of(
    "localhost:2222", "pdi-ssh-password:22",
    "localhost:2223", "pdi-ssh-publickey:22",
    "localhost:3128", "pdi-http-proxy:3128",
    "localhost:3129", "pdi-http-proxy-auth:3128" );

  private SshKtrRewriter() {
  }

  static byte[] rewrite( byte[] xml, String keyPath ) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse( new ByteArrayInputStream( xml ) );
      NodeList steps = doc.getElementsByTagName( "step" );
      for ( int i = 0; i < steps.getLength(); i++ ) {
        Element step = (Element) steps.item( i );
        if ( !"SSH".equals( childText( step, "type" ) ) ) {
          continue;
        }
        remap( step, "servername", "port" );
        remap( step, "proxyHost", "proxyPort" );
        if ( !childText( step, "keyFileName" ).isEmpty() ) {
          setChildText( step, "keyFileName", keyPath );
        }
      }
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      transformer.transform( new DOMSource( doc ), new StreamResult( out ) );
      return out.toByteArray();
    } catch ( Exception e ) {
      throw new IllegalStateException( "Cannot rewrite the SSH endpoints of the KTR", e );
    }
  }

  private static void remap( Element step, String hostTag, String portTag ) {
    String target = ENDPOINTS.get( childText( step, hostTag ) + ":" + childText( step, portTag ) );
    if ( target == null ) {
      return;
    }
    int separator = target.lastIndexOf( ':' );
    setChildText( step, hostTag, target.substring( 0, separator ) );
    setChildText( step, portTag, target.substring( separator + 1 ) );
  }

  private static String childText( Element parent, String tag ) {
    Element child = child( parent, tag );
    return child == null ? "" : child.getTextContent().trim();
  }

  private static void setChildText( Element parent, String tag, String value ) {
    child( parent, tag ).setTextContent( value );
  }

  private static Element child( Element parent, String tag ) {
    for ( Node node = parent.getFirstChild(); node != null; node = node.getNextSibling() ) {
      if ( node instanceof Element element && tag.equals( element.getTagName() ) ) {
        return element;
      }
    }
    return null;
  }
}
