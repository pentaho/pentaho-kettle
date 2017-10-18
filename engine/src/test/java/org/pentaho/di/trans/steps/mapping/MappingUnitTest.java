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

package org.pentaho.di.trans.steps.mapping;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.matchers.JUnitMatchers;

/**
 * @author Andrey Khayrutdinov
 */
public class MappingUnitTest {

  private StepMockHelper<MappingMeta, StepDataInterface> mockHelper;
  private Mapping mapping;

  @Before
  public void setUp() throws Exception {
    mockHelper = StepMockUtil.getStepMockHelper( MappingMeta.class, "MappingUnitTest" );
    mapping =
      new Mapping( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
  }


  @SuppressWarnings( "unchecked" )
  @Test
  public void pickupTargetStepsFor_OutputIsNotDefined() throws Exception {
    StepMeta singleMeta = new StepMeta( "single", null );
    StepMeta copiedMeta = new StepMeta( "copied", null );
    Mockito.when( mockHelper.transMeta.findNextSteps( mockHelper.stepMeta ) ).thenReturn( Arrays.asList( singleMeta, copiedMeta ) );

    StepInterface single = Mockito.mock( StepInterface.class );
    Mockito.when( mockHelper.trans.findStepInterfaces( "single" ) ).thenReturn( Collections.singletonList( single ) );

    StepInterface copy1 = Mockito.mock( StepInterface.class );
    StepInterface copy2 = Mockito.mock( StepInterface.class );
    Mockito.when( mockHelper.trans.findStepInterfaces( "copied" ) ).thenReturn( Arrays.asList( copy1, copy2 ) );

    MappingIODefinition definition = new MappingIODefinition( null, null );
    StepInterface[] targetSteps = mapping.pickupTargetStepsFor( definition );

    assertThat( Arrays.asList( targetSteps ), JUnitMatchers.hasItems( is( single ), is( copy1 ), is( copy2 ) ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void pickupTargetStepsFor_OutputIsDefined() throws Exception {
    StepInterface copy1 = Mockito.mock( StepInterface.class );
    StepInterface copy2 = Mockito.mock( StepInterface.class );
    Mockito.when( mockHelper.trans.findStepInterfaces( "copied" ) ).thenReturn( Arrays.asList( copy1, copy2 ) );

    MappingIODefinition definition = new MappingIODefinition( null, "copied" );
    StepInterface[] targetSteps = mapping.pickupTargetStepsFor( definition );

    assertThat( Arrays.asList( targetSteps ), JUnitMatchers.hasItems( is( copy1 ), is( copy2 ) ) );
  }

  @Test( expected = KettleException.class )
  public void pickupTargetStepsFor_OutputIsDefined_ThrowsExceptionIfFindsNone() throws Exception {
    MappingIODefinition definition = new MappingIODefinition( null, "non-existing" );
    mapping.pickupTargetStepsFor( definition );
  }

  @Test
  public void testDispose( ) throws Exception {

    MappingMeta meta = Mockito.mock( MappingMeta.class );
    MappingData data = Mockito.mock( MappingData.class );

    Mockito.when( data.getMappingTrans() ).thenReturn( mockHelper.trans );

    MappingInput[] mappingInputs = { Mockito.mock( MappingInput.class ) };
    MappingOutput[] mappingOutputs = { Mockito.mock( MappingOutput.class ) };
    Mockito.when( mockHelper.trans.findMappingInput() ).thenReturn( mappingInputs );
    Mockito.when( mockHelper.trans.findMappingOutput() ).thenReturn( mappingOutputs );

    data.mappingTransMeta = mockHelper.transMeta;
    Mockito.when( data.mappingTransMeta.getTransformationType() ).thenReturn( TransMeta.TransformationType.SingleThreaded );

    data.singleThreadedTransExcecutor = Mockito.mock( SingleThreadedTransExecutor.class );
    Mockito.when( data.singleThreadedTransExcecutor.oneIteration() ).thenReturn( true );

    data.mappingTrans = mockHelper.trans;
    Mockito.when( mockHelper.trans.isFinished() ).thenReturn( false );
    Mapping mapping = Mockito.spy( new Mapping( mockHelper.stepMeta, data, 0, mockHelper.transMeta, mockHelper.trans ) );
    String stepName = "StepName";
    mapping.setStepname( stepName );


    mapping.processRow( meta, data );
    mapping.dispose( meta, data );
    Mockito.verify( mockHelper.trans, Mockito.times( 1 ) ).removeActiveSubTransformation( stepName );

  }
}
