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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

public class TransDelegateTest {

  private PurRepository mockPurRepository;

  private IUnifiedRepository mockIUnifiedRepository;

  private DataNode expectedNode;

  private TransMeta expectedElement;

  private final String PRIVATE_DATASOURCE = "privateDatasource";

  private final long PRIVATE_STEP_COPIES = 1L;

  private final long PRIVATE_GUI_LOCATION_X = 0L;

  private final long PRIVATE_GUI_LOCATION_Y = 0L;

  private final String PRIVATE_STEP_TYPE = "Dummy";

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    mockPurRepository = mock(PurRepository.class );
    mockIUnifiedRepository = mock( IUnifiedRepository.class );

    createExpectedDataNode();
    createExpectedElement();
  }

  @Test
  public void testDataNodeToElement() throws KettleException {
    TransDelegate transDelegate = new TransDelegate( mockPurRepository, mockIUnifiedRepository );
    TransMeta actualElement = (TransMeta) transDelegate.dataNodeToElement( expectedNode );
    assertEquals( expectedElement.getXML(), actualElement.getXML() );
  }

  private void createExpectedDataNode() {
    expectedNode = new DataNode( TransDelegate.NODE_TRANS );
    DataNode privateDatabaseNode = expectedNode.addNode( TransDelegate.NODE_TRANS_PRIVATE_DATABASES );
    privateDatabaseNode.addNode( PRIVATE_DATASOURCE );
    DataNode stepsNode = expectedNode.addNode( TransDelegate.NODE_STEPS );
    DataNode stepNode = stepsNode.addNode( "step" );
    stepNode.setId( TransDelegate.PROP_STEP_TYPE );
    stepNode.setProperty( TransDelegate.PROP_NAME, TransDelegate.PROP_NAME );
    stepNode.setProperty( TransDelegate.PROP_DESCRIPTION, TransDelegate.PROP_DESCRIPTION );
    stepNode.setProperty( TransDelegate.PROP_STEP_TYPE, PRIVATE_STEP_TYPE );
    stepNode.setProperty( TransDelegate.PROP_STEP_DISTRIBUTE, TransDelegate.PROP_STEP_DISTRIBUTE );
    stepNode.setProperty( TransDelegate.PROP_STEP_ROW_DISTRIBUTION, TransDelegate.PROP_STEP_ROW_DISTRIBUTION );
    stepNode.setProperty( TransDelegate.PROP_STEP_COPIES, PRIVATE_STEP_COPIES );
    stepNode.setProperty( TransDelegate.PROP_STEP_COPIES_STRING, String.valueOf( PRIVATE_STEP_COPIES ) );
    stepNode.setProperty( TransDelegate.PROP_STEP_GUI_LOCATION_X, PRIVATE_GUI_LOCATION_X );
    stepNode.setProperty( TransDelegate.PROP_STEP_GUI_LOCATION_Y, PRIVATE_GUI_LOCATION_Y );
    stepNode.setProperty( TransDelegate.PROP_STEP_GUI_DRAW, TransDelegate.PROP_STEP_GUI_DRAW );
    DataNode notesNode = expectedNode.addNode( TransDelegate.NODE_NOTES );
    notesNode.setProperty( TransDelegate.PROP_NR_NOTES, 0 );
    DataNode hopsNode = expectedNode.addNode( TransDelegate.NODE_HOPS );
    hopsNode.setProperty( TransDelegate.PROP_NR_HOPS, 0 );
    expectedNode.setProperty( TransDelegate.PROP_TRANS_STATUS, 0L );
    expectedNode.setProperty( TransDelegate.PROP_USE_BATCHID, true );
    expectedNode.setProperty( TransDelegate.PROP_USE_LOGFIELD, true );
    expectedNode.setProperty( TransDelegate.PROP_OFFSET_MAXDATE, 0 );
    expectedNode.setProperty( TransDelegate.PROP_DIFF_MAXDATE, 0 );
    expectedNode.setProperty( TransDelegate.PROP_SIZE_ROWSET, 0L );
    expectedNode.setProperty( TransDelegate.PROP_UNIQUE_CONNECTIONS, false );
    expectedNode.setProperty( TransDelegate.PROP_FEEDBACK_SIZE, 0L );
    DataNode paramsNode = expectedNode.addNode( TransDelegate.NODE_PARAMETERS );
    paramsNode.setProperty( TransDelegate.PROP_NR_PARAMETERS, 0L );
  }

  private void createExpectedElement() {
    expectedElement = new TransMeta();
    expectedElement.setPrivateTransformationDatabases( Arrays.asList( PRIVATE_DATASOURCE ) );
    DummyTransMeta dummyTransMeta = new DummyTransMeta();
    StepMeta stepMeta = new StepMeta( TransDelegate.PROP_NAME, dummyTransMeta  );
    stepMeta.setDescription( TransDelegate.PROP_DESCRIPTION );
    stepMeta.setStepID( PRIVATE_STEP_TYPE );
    stepMeta.setDistributes( false );
    expectedElement.addStep( stepMeta );
    expectedElement.setTransstatus( 0 );
    expectedElement.setFeedbackSize( 0 );
    expectedElement.setCapturingStepPerformanceSnapShots( true );
    expectedElement.setStepPerformanceCapturingDelay( 0 );
    expectedElement.setStepPerformanceCapturingSizeLimit( null );
    expectedElement.setCreatedUser( null );
    expectedElement.setCreatedDate( null );
    expectedElement.setModifiedUser( null );
    expectedElement.setModifiedDate( null );
  }

}
