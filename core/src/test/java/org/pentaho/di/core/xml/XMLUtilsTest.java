/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
