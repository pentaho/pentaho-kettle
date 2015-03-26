/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    when( mockHelper.transMeta.findNextSteps( mockHelper.stepMeta ) ).thenReturn( asList( singleMeta, copiedMeta ) );

    StepInterface single = mock( StepInterface.class );
    when( mockHelper.trans.findStepInterfaces( "single" ) ).thenReturn( singletonList( single ) );

    StepInterface copy1 = mock( StepInterface.class );
    StepInterface copy2 = mock( StepInterface.class );
    when( mockHelper.trans.findStepInterfaces( "copied" ) ).thenReturn( asList( copy1, copy2 ) );

    MappingIODefinition definition = new MappingIODefinition( null, null );
    StepInterface[] targetSteps = mapping.pickupTargetStepsFor( definition );

    assertThat( asList( targetSteps ), hasItems( is( single ), is( copy1 ), is( copy2 ) ) );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void pickupTargetStepsFor_OutputIsDefined() throws Exception {
    StepInterface copy1 = mock( StepInterface.class );
    StepInterface copy2 = mock( StepInterface.class );
    when( mockHelper.trans.findStepInterfaces( "copied" ) ).thenReturn( asList( copy1, copy2 ) );

    MappingIODefinition definition = new MappingIODefinition( null, "copied" );
    StepInterface[] targetSteps = mapping.pickupTargetStepsFor( definition );

    assertThat( asList( targetSteps ), hasItems( is( copy1 ), is( copy2 ) ) );
  }

  @Test(expected = KettleException.class)
  public void pickupTargetStepsFor_OutputIsDefined_ThrowsExceptionIfFindsNone() throws Exception {
    MappingIODefinition definition = new MappingIODefinition( null, "non-existing" );
    mapping.pickupTargetStepsFor( definition );
  }
}
