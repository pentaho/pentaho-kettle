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

package org.pentaho.di.core.attributes;


import org.junit.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;


public class AttributesUtilTest {

  private static final String CUSTOM_TAG = "customTag";
  private static final String A_KEY = "aKEY";
  private static final String A_VALUE = "aVALUE";
  private static final String A_GROUP = "attributesGroup";

  @Test
  public void testGetAttributesXml_DefaultTag() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap() ) ).thenCallRealMethod();
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) ).thenCallRealMethod();

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
    }
  }

  @Test
  public void testGetAttributesXml_CustomTag() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) )
        .thenCallRealMethod();

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
  }

  @Test
  public void testGetAttributesXml_DefaultTag_NullParameter() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap() ) ).thenCallRealMethod();
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) )
        .thenCallRealMethod();

      String attributesXml = AttributesUtil.getAttributesXml( new HashMap<>() );

      assertNotNull( attributesXml );

      // Check that it's not an empty XML fragment
      assertTrue( attributesXml.contains( AttributesUtil.XML_TAG ) );
    }
  }

  @Test
  public void testGetAttributesXml_CustomTag_NullParameter() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) )
        .thenCallRealMethod();

      String attributesXml = AttributesUtil.getAttributesXml( new HashMap<>(), CUSTOM_TAG );

      assertNotNull( attributesXml );

      // Check that it's not an empty XML fragment
      assertTrue( attributesXml.contains( CUSTOM_TAG ) );
    }
  }

  @Test
  public void testGetAttributesXml_DefaultTag_EmptyMap() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap() ) ).thenCallRealMethod();
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) )
        .thenCallRealMethod();

      Map<String, Map<String, String>> attributesMap = new HashMap<>();

      String attributesXml = AttributesUtil.getAttributesXml( attributesMap );

      assertNotNull( attributesXml );

      // Check that it's not an empty XML fragment
      assertTrue( attributesXml.contains( AttributesUtil.XML_TAG ) );
    }
  }

  @Test
  public void testGetAttributesXml_CustomTag_EmptyMap() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.getAttributesXml( anyMap(), anyString() ) )
        .thenCallRealMethod();
      Map<String, Map<String, String>> attributesMap = new HashMap<>();

      String attributesXml = AttributesUtil.getAttributesXml( attributesMap, CUSTOM_TAG );

      assertNotNull( attributesXml );

      // Check that it's not an empty XML fragment
      assertTrue( attributesXml.contains( CUSTOM_TAG ) );
    }
  }

  @Test
  public void testLoadAttributes_NullParameter() {
    try ( MockedStatic<AttributesUtil> attributesUtilMockedStatic = mockStatic( AttributesUtil.class ) ) {
      attributesUtilMockedStatic.when( () -> AttributesUtil.loadAttributes( any( Node.class ) ) ).thenCallRealMethod();
      Map<String, Map<String, String>> attributesMap = AttributesUtil.loadAttributes( null );

      assertNotNull( attributesMap );
      assertTrue( attributesMap.isEmpty() );
    }
  }
}
