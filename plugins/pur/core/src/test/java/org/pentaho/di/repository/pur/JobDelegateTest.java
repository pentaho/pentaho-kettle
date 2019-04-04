/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

public class JobDelegateTest {
  private PurRepository mockPurRepository;

  @BeforeClass
  public static void before() throws KettlePluginException {
    JobEntryPluginType.getInstance().searchPlugins();
  }

  private abstract class JobEntryBaseAndInterface extends JobEntryBase implements JobEntryInterface {

  }

  @Before
  public void setup() {
    mockPurRepository = mock( PurRepository.class );
  }

  public static DataNode addSubnode( DataNode rootNode, String name ) {
    DataNode newNode = mock( DataNode.class );
    when( rootNode.getNode( name ) ).thenReturn( newNode );
    return newNode;
  }

  public static DataProperty addDataProperty( DataNode node, String name ) {
    DataProperty dataProperty = mock( DataProperty.class );
    when( node.hasProperty( name ) ).thenReturn( true );
    when( node.getProperty( name ) ).thenReturn( dataProperty );
    return dataProperty;
  }

  public static DataNode setProperty( DataNode node, String name, long value ) {
    when( addDataProperty( node, name ).getLong() ).thenReturn( value );
    return node;
  }

  public static DataNode setProperty( DataNode node, String name, String value ) {
    when( addDataProperty( node, name ).getString() ).thenReturn( value );
    return node;
  }

  public static DataNode setProperty( DataNode node, String name, boolean value ) {
    when( addDataProperty( node, name ).getBoolean() ).thenReturn( value );
    return node;
  }

  public static DataNode setNodes( DataNode node, String nrProperty, Iterable<DataNode> nodes ) {
    List<DataNode> list = new ArrayList<DataNode>();
    for ( DataNode subNode : nodes ) {
      list.add( subNode );
    }
    if ( nrProperty != null ) {
      setProperty( node, nrProperty, list.size() );
    }
    when( node.getNodes() ).thenReturn( list );
    return node;
  }

  @Test
  public void testElementToDataNodeSavesCopyAttributes() throws KettleException {
    JobMeta mockJobMeta = mock( JobMeta.class );
    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    JobDelegate jobDelegate = new JobDelegate( mockPurRepository, mockUnifiedRepository );
    JobLogTable mockJobLogTable = mock( JobLogTable.class );
    JobEntryCopy mockJobEntryCopy = mock( JobEntryCopy.class );
    Map<String, Map<String, String>> attributes = new HashMap<>();
    Map<String, String> group = new HashMap<>();
    final String mockGroup = "MOCK_GROUP";
    final String mockProperty = "MOCK_PROPERTY";
    final String mockValue = "MOCK_VALUE";
    group.put( mockProperty, mockValue );
    attributes.put( mockGroup, group );
    when( mockJobEntryCopy.getAttributesMap() ).thenReturn( attributes );
    JobEntryBaseAndInterface mockJobEntry = mock( JobEntryBaseAndInterface.class );

    when( mockJobMeta.listParameters() ).thenReturn( new String[] {} );
    when( mockJobMeta.getJobLogTable() ).thenReturn( mockJobLogTable );
    when( mockJobMeta.nrJobEntries() ).thenReturn( 1 );
    when( mockJobMeta.getJobEntry( 0 ) ).thenReturn( mockJobEntryCopy );
    when( mockJobEntryCopy.getName() ).thenReturn( "MOCK_NAME" );
    when( mockJobEntryCopy.getLocation() ).thenReturn( new Point( 0, 0 ) );
    when( mockJobEntryCopy.getEntry() ).thenReturn( mockJobEntry );

    DataNode dataNode = jobDelegate.elementToDataNode( mockJobMeta );
    DataNode groups =
      dataNode.getNode( "entries" ).getNodes().iterator().next().getNode( JobDelegate.PROP_ATTRIBUTES_JOB_ENTRY_COPY );
    DataNode mockGroupNode = groups.getNode( mockGroup );
    assertEquals( mockValue, mockGroupNode.getProperty( mockProperty ).getString() );
  }

  @Test
  public void testElementToDataNodeSavesAttributes() throws KettleException {
    JobMeta mockJobMeta = mock( JobMeta.class );
    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    JobDelegate jobDelegate = new JobDelegate( mockPurRepository, mockUnifiedRepository );
    JobLogTable mockJobLogTable = mock( JobLogTable.class );
    JobEntryCopy mockJobEntryCopy = mock( JobEntryCopy.class );
    Map<String, Map<String, String>> attributes = new HashMap<String, Map<String, String>>();
    Map<String, String> group = new HashMap<String, String>();
    final String mockGroup = "MOCK_GROUP";
    final String mockProperty = "MOCK_PROPERTY";
    final String mockValue = "MOCK_VALUE";
    group.put( mockProperty, mockValue );
    attributes.put( mockGroup, group );
    JobEntryBaseAndInterface mockJobEntry = mock( JobEntryBaseAndInterface.class );
    when( mockJobEntry.getAttributesMap() ).thenReturn( attributes );

    when( mockJobMeta.listParameters() ).thenReturn( new String[] {} );
    when( mockJobMeta.getJobLogTable() ).thenReturn( mockJobLogTable );
    when( mockJobMeta.nrJobEntries() ).thenReturn( 1 );
    when( mockJobMeta.getJobEntry( 0 ) ).thenReturn( mockJobEntryCopy );
    when( mockJobEntryCopy.getName() ).thenReturn( "MOCK_NAME" );
    when( mockJobEntryCopy.getLocation() ).thenReturn( new Point( 0, 0 ) );
    when( mockJobEntryCopy.getEntry() ).thenReturn( mockJobEntry );

    DataNode dataNode = jobDelegate.elementToDataNode( mockJobMeta );
    DataNode groups =
        dataNode.getNode( "entries" ).getNodes().iterator().next().getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS );
    DataNode mockGroupNode = groups.getNode( mockGroup );
    assertEquals( mockValue, mockGroupNode.getProperty( mockProperty ).getString() );
  }
}
