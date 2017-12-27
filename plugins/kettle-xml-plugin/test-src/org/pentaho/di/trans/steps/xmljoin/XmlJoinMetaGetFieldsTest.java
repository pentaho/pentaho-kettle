/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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

    when( transMeta.findStep( sourceXmlStep ) ).thenReturn( sourceStepMeta );
    when( transMeta.getStepFields( sourceStepMeta, null, null ) ).thenReturn( rowMetaPreviousSteps );

    RowMeta rowMeta = new RowMeta();
    ValueMetaString keepValueMeta = new ValueMetaString( targetStepField );
    ValueMetaString removeValueMeta = new ValueMetaString( sourceStepField );
    rowMeta.addValueMeta( keepValueMeta );
    rowMeta.addValueMeta( removeValueMeta );

    xmlJoinMeta.getFields( rowMeta, "testStepName", null, null, transMeta, null, null );
    Assert.assertEquals( 2, rowMeta.size() );
    String[] strings = rowMeta.getFieldNames();
    Assert.assertEquals( targetStepField, strings[0] );
    Assert.assertEquals( resultXmlFieldName, strings[1] );
  }
}
