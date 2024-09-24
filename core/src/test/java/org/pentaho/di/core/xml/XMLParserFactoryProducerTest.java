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


import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XMLParserFactoryProducerTest {
  @Test
  public void secureTransformerFactory() throws TransformerConfigurationException {
    TransformerFactory factory = XMLParserFactoryProducer.createSecureTransformerFactory();
    assertTrue( factory.getFeature( XMLConstants.FEATURE_SECURE_PROCESSING ) );
  }

  @Test
  public void secureXmlInputStream() {
    XMLInputFactory factory = XMLParserFactoryProducer.createSecureXMLInputFactory();
    assertEquals( false, factory.getProperty( IS_SUPPORTING_EXTERNAL_ENTITIES ) );
  }
}
