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


package org.pentaho.di.core.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;

public class XMLParserFactoryProducer {

  private static final Log logger = LogFactory.getLog( XMLParserFactoryProducer.class );

  private XMLParserFactoryProducer() { }

  /**
   * Creates an instance of {@link DocumentBuilderFactory} class with enabled {@link XMLConstants#FEATURE_SECURE_PROCESSING} property.
   * Enabling this feature prevents from some XXE attacks (e.g. XML bomb)
   * See PPP-3506 for more details.
   *
   * @throws ParserConfigurationException if feature can't be enabled
   *
   */
  public static DocumentBuilderFactory createSecureDocBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setFeature( FEATURE_SECURE_PROCESSING, true );
    docBuilderFactory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );

    return docBuilderFactory;
  }

  /**
   * Creates an instance of {@link SAXParserFactory} class with enabled {@link XMLConstants#FEATURE_SECURE_PROCESSING} property.
   * Enabling this feature prevents from some XXE attacks (e.g. XML bomb)
   *
   * @throws ParserConfigurationException if a parser cannot
   *     be created which satisfies the requested configuration.
   *
   * @throws SAXNotRecognizedException When the underlying XMLReader does
   *            not recognize the property name.
   *
   * @throws SAXNotSupportedException When the underlying XMLReader
   *            recognizes the property name but doesn't support the
   *            property.
   */
  public static SAXParserFactory createSecureSAXParserFactory()
    throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setFeature( FEATURE_SECURE_PROCESSING, true );
    factory.setFeature( "http://xml.org/sax/features/external-general-entities", false );
    factory.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
    factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
    factory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );

    return factory;
  }

  public static SAXReader getSAXReader( final EntityResolver resolver ) {
    SAXReader reader = new SAXReader();
    if ( resolver != null ) {
      reader.setEntityResolver( resolver );
    }
    try {
      reader.setFeature( FEATURE_SECURE_PROCESSING, true );
      reader.setFeature( "http://xml.org/sax/features/external-general-entities", false );
      reader.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
      reader.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
    } catch ( SAXException e ) {
      logger.error( "Some parser properties are not supported." );
    }
    reader.setIncludeExternalDTDDeclarations( false );
    reader.setIncludeInternalDTDDeclarations( false );
    return reader;
  }

  public static XMLInputFactory createSecureXMLInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty( IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE );
    return factory;
  }

  public static TransformerFactory createSecureTransformerFactory() throws TransformerConfigurationException {
    TransformerFactory factory = TransformerFactory.newInstance();
    //using explicit string here because Sonar is still reporting a violation when using FEATURE_SECURE_PROCESSING
    factory.setFeature( "http://javax.xml.XMLConstants/feature/secure-processing", true );
    return factory;
  }
}
