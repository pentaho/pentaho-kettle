/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
import java.util.Arrays;
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
  public void testDataNodeToElementCopiesAttributesToJobEntryCopyAndJobEntry() throws KettleException {
    IUnifiedRepository mockUnifiedRepository = mock( IUnifiedRepository.class );
    JobDelegate jobDelegate = new JobDelegate( mockPurRepository, mockUnifiedRepository );
    DataNode mockDataNode = mock( DataNode.class );
    DataNode entriesNode = addSubnode( mockDataNode, JobDelegate.NODE_ENTRIES );
    DataNode copyNode = mock( DataNode.class );
    setNodes( entriesNode, JobDelegate.PROP_NR_JOB_ENTRY_COPIES, Arrays.asList( copyNode ) );
    DataNode nodeCustom = addSubnode( copyNode, JobDelegate.NODE_CUSTOM );
    DataNode notesNode = addSubnode( mockDataNode, JobDelegate.NODE_NOTES );
    DataNode hopsNode = addSubnode( mockDataNode, JobDelegate.NODE_HOPS );
    DataNode paramsNode = addSubnode( mockDataNode, JobDelegate.NODE_PARAMETERS );
    DataNode groupsNode = addSubnode( copyNode, AttributesMapUtil.NODE_ATTRIBUTE_GROUPS );
    DataNode groupNode = mock( DataNode.class );
    setNodes( groupsNode, null, Arrays.asList( groupNode ) );
    JobMeta mockJobMeta = mock( JobMeta.class );
    JobLogTable mockJobLogTable = mock( JobLogTable.class );
    List<JobEntryCopy> jobCopies = new ArrayList<JobEntryCopy>();
    DataProperty mockDataProperty = mock( DataProperty.class );
    List<DataProperty> dataProperties = Arrays.asList( mockDataProperty );

    setProperty( mockDataNode, JobDelegate.PROP_JOB_STATUS, 0L );
    setProperty( mockDataNode, JobDelegate.PROP_USE_BATCH_ID, false );
    setProperty( mockDataNode, JobDelegate.PROP_PASS_BATCH_ID, false );
    setProperty( mockDataNode, JobDelegate.PROP_USE_LOGFIELD, false );
    setProperty( copyNode, JobDelegate.PROP_JOBENTRY_TYPE, "WRITE_TO_LOG" );
    when( copyNode.getId() ).thenReturn( "COPYNODE_ID" );
    setProperty( copyNode, JobDelegate.PROP_NR, 0L );
    setProperty( copyNode, JobDelegate.PROP_GUI_LOCATION_X, 0L );
    setProperty( copyNode, JobDelegate.PROP_GUI_LOCATION_Y, 0L );
    setProperty( copyNode, JobDelegate.PROP_GUI_DRAW, false );
    setProperty( copyNode, JobDelegate.PROP_PARALLEL, false );
    setProperty( nodeCustom, "logmessage_#_0", (String) null );
    setNodes( notesNode, JobDelegate.PROP_NR_NOTES, Arrays.<DataNode> asList() );
    setNodes( hopsNode, JobDelegate.PROP_NR_HOPS, Arrays.<DataNode> asList() );
    setProperty( paramsNode, JobDelegate.PROP_NR_PARAMETERS, 0L );
    when( mockJobMeta.getJobCopies() ).thenReturn( jobCopies );
    when( mockJobMeta.getJobLogTable() ).thenReturn( mockJobLogTable );
    when( groupNode.getName() ).thenReturn( "GROUP_NODE_NAME" );
    when( groupNode.getProperties() ).thenReturn( dataProperties );
    when( mockDataProperty.getName() ).thenReturn( "MOCK_PROPERTY" );
    when( mockDataProperty.getString() ).thenReturn( "MOCK_VALUE" );

    jobDelegate.dataNodeToElement( mockDataNode, mockJobMeta );
    assertEquals( jobCopies.get( 0 ).getAttributesMap(), ( (JobEntryBase) jobCopies.get( 0 ).getEntry() )
        .getAttributesMap() );
  }

  @Test
  public void testElementToDataNodeSavesCopyAttributes() throws KettleException {
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
        dataNode.getNode( "entries" ).getNodes().iterator().next().getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS );
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
