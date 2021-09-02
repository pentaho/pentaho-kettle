/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.attributes;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;


@PrepareForTest( AttributesUtil.class )
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class AttributesUtilTest {

  private static final String CUSTOM_TAG = "customTag";
  private static final String A_KEY = "aKEY";
  private static final String A_VALUE = "aVALUE";
  private static final String A_GROUP = "attributesGroup";

  @Before
  public void setUp() {
    PowerMockito.mockStatic( AttributesUtil.class );
  }

  @Test
  public void testGetAttributesXml_DefaultTag() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ) ) ).thenCallRealMethod();
    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    Map<String, String> attributesGroup = new HashMap<>();
    Map<String, Map<String, String>> attributesMap = new HashMap<>();
    attributesGroup.put( A_KEY, A_VALUE );
    attributesMap.put( A_GROUP, attributesGroup );

    String attributesXml = AttributesUtil.getAttributesXml( attributesMap );

    assertNotNull( attributesXml );

    // The default tag was used
    assertTrue( attributesXml.contains( AttributesUtil.XML_TAG ) );

    // The group is present
    assertTrue( attributesXml.contains( A_GROUP ) );

    // Both Key and Value are present
    assertTrue( attributesXml.contains( A_KEY ) );
    assertTrue( attributesXml.contains( A_VALUE ) );

    // Verify that getAttributesXml was invoked once (and with the right parameters)
    PowerMockito.verifyStatic( AttributesUtil.class );
    AttributesUtil.getAttributesXml( attributesMap, AttributesUtil.XML_TAG );
  }

  @Test
  public void testGetAttributesXml_CustomTag() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    Map<String, String> attributesGroup = new HashMap<>();
    Map<String, Map<String, String>> attributesMap = new HashMap<>();
    attributesGroup.put( A_KEY, A_VALUE );
    attributesMap.put( A_GROUP, attributesGroup );

    String attributesXml = AttributesUtil.getAttributesXml( attributesMap, CUSTOM_TAG );

    assertNotNull( attributesXml );

    // The custom tag was used
    assertTrue( attributesXml.contains( CUSTOM_TAG ) );

    // The group is present
    assertTrue( attributesXml.contains( A_GROUP ) );

    // Both Key and Value are present
    assertTrue( attributesXml.contains( A_KEY ) );
    assertTrue( attributesXml.contains( A_VALUE ) );
  }

  @Test
  public void testGetAttributesXml_DefaultTag_NullParameter() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ) ) ).thenCallRealMethod();
    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    String attributesXml = AttributesUtil.getAttributesXml( null );

    assertNotNull( attributesXml );

    // Check that it's not an empty XML fragment
    assertTrue( attributesXml.contains( AttributesUtil.XML_TAG ) );
  }

  @Test
  public void testGetAttributesXml_CustomTag_NullParameter() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    String attributesXml = AttributesUtil.getAttributesXml( null, CUSTOM_TAG );

    assertNotNull( attributesXml );

    // Check that it's not an empty XML fragment
    assertTrue( attributesXml.contains( CUSTOM_TAG ) );
  }

  @Test
  public void testGetAttributesXml_DefaultTag_EmptyMap() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ) ) ).thenCallRealMethod();
    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    Map<String, Map<String, String>> attributesMap = new HashMap<>();

    String attributesXml = AttributesUtil.getAttributesXml( attributesMap );

    assertNotNull( attributesXml );

    // Check that it's not an empty XML fragment
    assertTrue( attributesXml.contains( AttributesUtil.XML_TAG ) );
  }

  @Test
  public void testGetAttributesXml_CustomTag_EmptyMap() {

    PowerMockito.when( AttributesUtil.getAttributesXml( any( Map.class ), anyString() ) ).thenCallRealMethod();

    Map<String, Map<String, String>> attributesMap = new HashMap<>();

    String attributesXml = AttributesUtil.getAttributesXml( attributesMap, CUSTOM_TAG );

    assertNotNull( attributesXml );

    // Check that it's not an empty XML fragment
    assertTrue( attributesXml.contains( CUSTOM_TAG ) );
  }

  @Test
  public void testLoadAttributes_NullParameter() {

    PowerMockito.when( AttributesUtil.loadAttributes( any( Node.class ) ) ).thenCallRealMethod();

    Map<String, Map<String, String>> attributesMap = AttributesUtil.loadAttributes( null );

    assertNotNull( attributesMap );
    assertTrue( attributesMap.isEmpty() );
  }
}
