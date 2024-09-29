/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 - 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.pur;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mockStatic;

public class AttributesMapUtilTest {

  private static final String SAVE_ATTRIBUTES_MAP_METHOD = "saveAttributesMap";
  private static final String LOAD_ATTRIBUTES_MAP_METHOD = "loadAttributesMap";
  private static final String CUSTOM_TAG = "customTag";
  private static final String A_KEY = "aKEY";
  private static final String A_VALUE = "aVALUE";
  private static final String A_GROUP = "aGROUP";
  public static final String CNST_DUMMY = "dummy";

  @Test
  public void testSaveAttributesMap_DefaultTag() throws Exception {
    try( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class) ) {

      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();

      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      JobEntryCopy jobEntryCopy = new JobEntryCopy();
      jobEntryCopy.setAttributesMap( new HashMap<>() );
      jobEntryCopy.setAttributes( A_GROUP, new HashMap<>() );
      jobEntryCopy.setAttribute( A_GROUP, A_KEY, A_VALUE );

      DataNode dataNode = new DataNode( CNST_DUMMY );

      AttributesMapUtil.saveAttributesMap( dataNode, jobEntryCopy );

      assertNotNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
      assertNotNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ).getNode( A_GROUP ) );
      assertNotNull(
        dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ).getNode( A_GROUP ).getProperty( A_KEY ) );
      assertEquals( A_VALUE,
        dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ).getNode( A_GROUP ).getProperty( A_KEY )
          .getString() );
    }
  }

  @Test
  public void testSaveAttributesMap_CustomTag() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {

      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      JobEntryCopy jobEntryCopy = new JobEntryCopy();
      jobEntryCopy.setAttributesMap( new HashMap<>() );
      jobEntryCopy.setAttributes( A_GROUP, new HashMap<>() );
      jobEntryCopy.setAttribute( A_GROUP, A_KEY, A_VALUE );

      DataNode dataNode = new DataNode( CNST_DUMMY );

      AttributesMapUtil.saveAttributesMap( dataNode, jobEntryCopy, CUSTOM_TAG );

      assertNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
      assertNotNull( dataNode.getNode( CUSTOM_TAG ) );
      assertNotNull( dataNode.getNode( CUSTOM_TAG ).getNode( A_GROUP ) );
      assertNotNull(
        dataNode.getNode( CUSTOM_TAG ).getNode( A_GROUP ).getProperty( A_KEY ) );
      assertEquals( A_VALUE,
        dataNode.getNode( CUSTOM_TAG ).getNode( A_GROUP ).getProperty( A_KEY ).getString() );
    }
  }

  @Test
  public void testSaveAttributesMap_DefaultTag_NullParameter() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );

      AttributesMapUtil.saveAttributesMap( dataNode, null );

      assertNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
    }
  }

  @Test
  public void testSaveAttributesMap_CustomTag_NullParameter() throws Exception {

    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );

      AttributesMapUtil.saveAttributesMap( dataNode, null, CUSTOM_TAG );

      assertNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
      assertNull( dataNode.getNode( CUSTOM_TAG ) );
    }
  }

  @Test
  public void testSaveAttributesMap_DefaultTag_NoAttributes() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.saveAttributesMap( dataNode, jobEntryCopy );

      assertNotNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
      Iterable<DataNode> dataNodeIterable = dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ).getNodes();
      assertFalse( dataNodeIterable.iterator().hasNext() );
    }
  }

  @Test
  public void testSaveAttributesMap_CustomTag_NoAttributes() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.saveAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.saveAttributesMap( dataNode, jobEntryCopy, CUSTOM_TAG );

      assertNull( dataNode.getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS ) );
      assertNotNull( dataNode.getNode( CUSTOM_TAG ) );
      Iterable<DataNode> dataNodeIterable = dataNode.getNode( CUSTOM_TAG ).getNodes();
      assertFalse( dataNodeIterable.iterator().hasNext() );
    }
  }

  @Test
  public void testLoadAttributesMap_DefaultTag() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      DataNode groupsDataNode = dataNode.addNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS );
      DataNode aGroupDataNode = groupsDataNode.addNode( A_GROUP );
      aGroupDataNode.setProperty( A_KEY, A_VALUE );

      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy );

      assertNotNull( jobEntryCopy.getAttributesMap() );
      assertNotNull( jobEntryCopy.getAttributes( A_GROUP ) );
      assertEquals( A_VALUE, jobEntryCopy.getAttribute( A_GROUP, A_KEY ) );
    }
  }

  @Test
  public void testLoadAttributesMap_CustomTag() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      DataNode groupsDataNode = dataNode.addNode( CUSTOM_TAG );
      DataNode aGroupDataNode = groupsDataNode.addNode( A_GROUP );
      aGroupDataNode.setProperty( A_KEY, A_VALUE );

      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy, CUSTOM_TAG );

      assertNotNull( jobEntryCopy.getAttributesMap() );
      assertNotNull( jobEntryCopy.getAttributes( A_GROUP ) );
      assertEquals( A_VALUE, jobEntryCopy.getAttribute( A_GROUP, A_KEY ) );
    }
  }

  @Test
  public void testLoadAttributesMap_DefaultTag_NullParameter() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( null, jobEntryCopy );
    }
  }

  @Test
  public void testLoadAttributesMap_CustomTag_NullParameter() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( null, jobEntryCopy, CUSTOM_TAG );
    }
  }

  @Test
  public void testLoadAttributesMap_DefaultTag_EmptyDataNode() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ) ).thenCallRealMethod();
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy );

      assertNotNull( jobEntryCopy.getAttributesMap() );
    }
  }

  @Test
  public void testLoadAttributesMap_CustomTag_EmptyDataNode() throws Exception {
    try ( MockedStatic<AttributesMapUtil> mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      mockedAttributesMapUtil.when( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), anyString() ) ).thenCallRealMethod();

      DataNode dataNode = new DataNode( CNST_DUMMY );
      JobEntryCopy jobEntryCopy = new JobEntryCopy();

      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy, CUSTOM_TAG );

      assertNotNull( jobEntryCopy.getAttributesMap() );
    }
  }
}
