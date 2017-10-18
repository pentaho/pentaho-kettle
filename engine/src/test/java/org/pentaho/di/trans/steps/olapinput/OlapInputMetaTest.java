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

package org.pentaho.di.trans.steps.olapinput;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.w3c.dom.Node;

public class OlapInputMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes = Arrays.asList( "url", "username", "password", "mdx", "catalog", "variables_active" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "url", "getOlap4jUrl" );
    getterMap.put( "username", "getUsername" );
    getterMap.put( "password", "getPassword" );
    getterMap.put( "mdx", "getMdx" );
    getterMap.put( "catalog", "getCatalog" );
    getterMap.put( "variables_active", "isVariableReplacementActive" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "url", "setOlap4jUrl" );
    setterMap.put( "username", "setUsername" );
    setterMap.put( "password", "setPassword" );
    setterMap.put( "mdx", "setMdx" );
    setterMap.put( "catalog", "setCatalog" );
    setterMap.put( "variables_active", "setVariableReplacementActive" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( OlapInputMeta.class, attributes, getterMap, setterMap );

    loadSaveTester.testSerialization();
  }

  @Test
  public void checkPasswordEncrypted() throws KettleXMLException {
    OlapInputMeta meta = new OlapInputMeta();
    meta.setPassword( "qwerty" );
    Node stepXML = XMLHandler.getSubNode(
      XMLHandler.loadXMLString( "<step>" + meta.getXML() + "</step>" ), "step" );
    assertFalse( "qwerty".equals( XMLHandler.getTagValue( stepXML, "password" ) ) );
  }
}
