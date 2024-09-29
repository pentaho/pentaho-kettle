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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class XMLUtilsTest {
  @Test
  public void secureFeatureEnabledAfterDocBuilderFactoryCreation() throws Exception {
    DocumentBuilderFactory documentBuilderFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();

    assertTrue( documentBuilderFactory.getFeature( XMLConstants.FEATURE_SECURE_PROCESSING ) );
  }

  @Test
  public void secureFeatureEnabledAfterSAXParserFactoryCreation() throws Exception {
    SAXParserFactory saxParserFactory = XMLParserFactoryProducer.createSecureSAXParserFactory();

    assertTrue( saxParserFactory.getFeature( XMLConstants.FEATURE_SECURE_PROCESSING ) );
    assertFalse( saxParserFactory.getFeature( "http://xml.org/sax/features/external-general-entities" ) );
    assertFalse( saxParserFactory.getFeature( "http://xml.org/sax/features/external-parameter-entities" ) );
    assertFalse( saxParserFactory.getFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd" ) );
    assertTrue( saxParserFactory.getFeature( "http://apache.org/xml/features/disallow-doctype-decl" ) );
  }

}
