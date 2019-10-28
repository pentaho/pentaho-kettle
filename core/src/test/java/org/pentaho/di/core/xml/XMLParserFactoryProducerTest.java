/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
