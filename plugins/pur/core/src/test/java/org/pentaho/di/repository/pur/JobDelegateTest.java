/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.AttributesInterface;
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

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@RunWith( MockitoJUnitRunner.class )
public class JobDelegateTest {

  private static final String MOCK_GROUP = "MOCK_GROUP";
  private static final String MOCK_PROPERTY = "MOCK_PROPERTY";
  private static final String MOCK_VALUE = "MOCK_VALUE";

  @Mock private PurRepository mockPurRepository;
  @Mock private JobMeta mockJobMeta;
  @Mock private IUnifiedRepository mockUnifiedRepository;
  @Mock private JobEntryBaseAndInterface mockJobEntry;
  @Mock private JobLogTable mockJobLogTable;
  @Mock private JobEntryCopy mockJobEntryCopy;

  private JobDelegate jobDelegate = new JobDelegate( mockPurRepository, mockUnifiedRepository );
  private Map<String, Map<String, String>> attributes = new HashMap<>();
  private Map<String, String> group = new HashMap<>();

  @BeforeClass
  public static void before() throws KettlePluginException {
    JobEntryPluginType.getInstance().searchPlugins();
  }

  private abstract class JobEntryBaseAndInterface extends JobEntryBase implements JobEntryInterface {

  }

  @Before
  public void setup() {
    when( mockJobMeta.listParameters() ).thenReturn( new String[] {} );
    when( mockJobMeta.getJobLogTable() ).thenReturn( mockJobLogTable );
    when( mockJobMeta.nrJobEntries() ).thenReturn( 1 );
    when( mockJobMeta.getJobEntry( 0 ) ).thenReturn( mockJobEntryCopy );
    when( mockJobEntryCopy.getName() ).thenReturn( "MOCK_NAME" );
    when( mockJobEntryCopy.getLocation() ).thenReturn( new Point( 0, 0 ) );
    when( mockJobEntryCopy.getEntry() ).thenReturn( mockJobEntry );

    group.put( MOCK_PROPERTY, MOCK_VALUE );
    attributes.put( MOCK_GROUP, group );
  }

  @Test
  public void testElementToDataNodeSavesCopyAttributes() throws KettleException {
    when( mockJobEntryCopy.getAttributesMap() ).thenReturn( attributes );

    DataNode dataNode = jobDelegate.elementToDataNode( mockJobMeta );
    DataNode groups =
      dataNode.getNode( "entries" ).getNodes().iterator().next().getNode( JobDelegate.PROP_ATTRIBUTES_JOB_ENTRY_COPY );
    DataNode mockGroupNode = groups.getNode( MOCK_GROUP );
    assertEquals( MOCK_VALUE, mockGroupNode.getProperty( MOCK_PROPERTY ).getString() );
  }

  @Test
  public void testElementToDataNodeSavesAttributes() throws KettleException {
    when( mockJobEntry.getAttributesMap() ).thenReturn( attributes );

    DataNode dataNode = jobDelegate.elementToDataNode( mockJobMeta );
    DataNode groups =
      dataNode.getNode( "entries" ).getNodes().iterator().next().getNode( AttributesMapUtil.NODE_ATTRIBUTE_GROUPS );
    DataNode mockGroupNode = groups.getNode( MOCK_GROUP );
    assertEquals( MOCK_VALUE, mockGroupNode.getProperty( MOCK_PROPERTY ).getString() );
  }

  @Test
  public void testDataNodeToElement() throws KettleException {
    DataNode dataNode = jobDelegate.elementToDataNode( mockJobMeta );
    setIds( dataNode );
    JobMeta jobMeta = new JobMeta();
    jobDelegate.dataNodeToElement( dataNode, jobMeta );
    assertThat( jobMeta.getJobCopies().size(), equalTo( 1 ) );
    assertThat( jobMeta.getJobEntry( 0 ).getName(), equalTo( "MOCK_NAME" ) );

    assertTrue( "Job Entry should have link back to parent job meta.",
      jobMeta.getJobEntry( 0 ).getParentJobMeta() == jobMeta );
  }

  @Test
  public void loadAttributesMapWithAttributesJobEntryCopyTest() throws Exception {
    DataNode dataNode = mock( DataNode.class );
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    when( dataNode.getNode( JobDelegate.PROP_ATTRIBUTES_JOB_ENTRY_COPY ) ).thenReturn( null );
    try ( MockedStatic<AttributesMapUtil> dummyAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      JobDelegate.loadAttributesMap( dataNode, jobEntryCopy );
      dummyAttributesMapUtil.verify( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ) ), VerificationModeFactory.times( 1 ) );
      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy );
    }
  }

  @Test
  public void loadAttributesMapWithoutAttributesJobEntryCopyTest() throws Exception {
    DataNode dataNode = mock( DataNode.class );
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    when( dataNode.getNode( JobDelegate.PROP_ATTRIBUTES_JOB_ENTRY_COPY ) ).thenReturn( dataNode );
    try ( MockedStatic<AttributesMapUtil> dummyAttributesMapUtil = mockStatic( AttributesMapUtil.class ) ) {
      JobDelegate.loadAttributesMap( dataNode, jobEntryCopy );
      dummyAttributesMapUtil.verify( () -> AttributesMapUtil.loadAttributesMap( any( DataNode.class ),
        any( AttributesInterface.class ), any() ), VerificationModeFactory.times( 1 ) );
      AttributesMapUtil.loadAttributesMap( dataNode, jobEntryCopy, JobDelegate.PROP_ATTRIBUTES_JOB_ENTRY_COPY );
    }
  }

  private void setIds( DataNode node ) {
    node.setId( "mockid" );
    stream( node.getNodes().spliterator(), false ).forEach( this::setIds );
  }
}
