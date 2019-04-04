/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.xmljoin;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class XmlJoinMetaGetFieldsTest {

  XMLJoinMeta xmlJoinMeta;
  TransMeta transMeta;

  @Before
  public void setup() throws Exception {
    xmlJoinMeta = new XMLJoinMeta();
    transMeta = mock( TransMeta.class );
  }

  @Test
  public void testGetFieldsReturnTargetStepFieldsPlusResultXmlField() throws Exception {
    String sourceXmlStep = "source xml step name";
    String sourceStepField = "source field test name";
    String targetStepField = "target field test name";
    String resultXmlFieldName = "result xml field name";
    RowMeta rowMetaPreviousSteps = new RowMeta();
    rowMetaPreviousSteps.addValueMeta( new ValueMeta( sourceStepField, ValueMetaInterface.TYPE_STRING ) );
    xmlJoinMeta.setSourceXMLstep( sourceXmlStep );
    xmlJoinMeta.setValueXMLfield( "result xml field name" );
    StepMeta sourceStepMeta = new StepMeta();
    sourceStepMeta.setName( sourceXmlStep );

    doReturn( sourceStepMeta ).when( transMeta ).findStep( sourceXmlStep );
    doReturn( rowMetaPreviousSteps ).when( transMeta ).getStepFields( sourceStepMeta, null, null );


    RowMeta rowMeta = new RowMeta();
    ValueMetaString keepValueMeta = new ValueMetaString( targetStepField );
    ValueMetaString removeValueMeta = new ValueMetaString( sourceStepField );
    rowMeta.addValueMeta( keepValueMeta );
    rowMeta.addValueMeta( removeValueMeta );

    xmlJoinMeta.getFields( rowMeta, "testStepName", null, null, transMeta, null, null );
    assertEquals( 2, rowMeta.size() );
    String[] strings = rowMeta.getFieldNames();
    assertEquals( targetStepField, strings[0] );
    assertEquals( resultXmlFieldName, strings[1] );
  }

  @Test
  public void testGetFieldsReturnTargetStepFieldsWithDuplicates() throws Exception {
    // Source Step
    String sourceXmlStep = "source xml step name";
    String sourceStepField1 = "a";
    String sourceStepField2 = "b";

    // Target Step
    String targetXmlStep = "target xml step name";
    String targetStepField1 = "b";
    String targetStepField2 = "c";

    // XML Join Result
    String resultXmlFieldName = "result xml field name";

    // Source Row Meta
    RowMeta rowMetaPreviousSourceStep = new RowMeta();
    rowMetaPreviousSourceStep.addValueMeta( new ValueMeta( sourceStepField1, ValueMetaInterface.TYPE_STRING ) );
    rowMetaPreviousSourceStep.addValueMeta( new ValueMeta( sourceStepField2, ValueMetaInterface.TYPE_STRING ) );

    // Set source step in XML Join step.
    xmlJoinMeta.setSourceXMLstep( sourceXmlStep );
    StepMeta sourceStepMeta = new StepMeta();
    sourceStepMeta.setName( sourceXmlStep );

    doReturn( sourceStepMeta ).when( transMeta ).findStep( sourceXmlStep );
    doReturn( rowMetaPreviousSourceStep ).when( transMeta ).getStepFields( sourceStepMeta, null, null );

    // Target Row Meta
    RowMeta rowMetaPreviousTargetStep = new RowMeta();
    rowMetaPreviousTargetStep.addValueMeta( new ValueMeta( targetStepField1, ValueMetaInterface.TYPE_STRING ) );
    rowMetaPreviousTargetStep.addValueMeta( new ValueMeta( targetStepField2, ValueMetaInterface.TYPE_STRING ) );

    // Set target step in XML Join step.
    xmlJoinMeta.setTargetXMLstep( targetXmlStep );
    StepMeta targetStepMeta = new StepMeta();
    targetStepMeta.setName( targetXmlStep );

    doReturn( targetStepMeta ).when( transMeta ).findStep( targetXmlStep );
    doReturn( rowMetaPreviousTargetStep ).when( transMeta ).getStepFields( targetStepMeta, null, null );

    // Set result field name
    xmlJoinMeta.setValueXMLfield( resultXmlFieldName );

    RowMeta rowMeta = new RowMeta();
    ValueMetaString removeValueMeta1 = new ValueMetaString( "a" );
    rowMeta.addValueMeta( removeValueMeta1 );
    ValueMetaString keepValueMeta1 = new ValueMetaString( "b" );
    rowMeta.addValueMeta( keepValueMeta1 );
    ValueMetaString keepValueMeta2 = new ValueMetaString( "c" );
    rowMeta.addValueMeta( keepValueMeta2 );

    // Get output fields
    xmlJoinMeta.getFields( rowMeta, "testStepName", null, null, transMeta, null, null );
    assertEquals( 3, rowMeta.size() );
    String[] strings = rowMeta.getFieldNames();
    assertEquals( "b", strings[0] );
    assertEquals( "c", strings[1] );
    assertEquals( "result xml field name", strings[2] );
  }
}
