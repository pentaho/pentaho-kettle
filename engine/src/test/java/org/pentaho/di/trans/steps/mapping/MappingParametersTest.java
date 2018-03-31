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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.StepMockUtil;

public class MappingParametersTest {

  private Mapping step;
  private Trans trans;
  private TransMeta transMeta;

  @Before
  public void setUp() throws Exception {
    step = StepMockUtil.getStep( Mapping.class, MappingMeta.class, "junit" );
    trans = Mockito.mock( Trans.class );
    transMeta = Mockito.mock( TransMeta.class );
  }

  @After
  public void tearDown() throws Exception {
    step = null;
    trans = null;
    transMeta = null;
  }

  /**
   * PDI-3064 Test parent transformation overrides parameters for child transformation.
   * 
   * @throws KettleException
   */
  @Test
  public void testOverrideMappingParameters() throws KettleException {
    MappingParameters param = Mockito.mock( MappingParameters.class );
    Mockito.when( param.getVariable() ).thenReturn( new String[] { "a", "b" } );
    Mockito.when( param.getInputField() ).thenReturn( new String[] { "11", "12" } );
    when( transMeta.listParameters() ).thenReturn( new String[] { "a" } );
    StepWithMappingMeta
      .activateParams( trans, trans, step, transMeta.listParameters(), param.getVariable(), param.getInputField() );
    // parameters was overridden 2 times
    Mockito.verify( trans, Mockito.times( 1 ) ).setParameterValue( Mockito.anyString(), Mockito.anyString() );
    Mockito.verify( trans, Mockito.times( 1 ) ).setVariable( Mockito.anyString(), Mockito.anyString() );
  }

  /**
   * Regression of PDI-3064 : keep correct 'inherit all variables' settings. This is a case for 'do not override'
   * 
   * @throws KettleException
   */
  @Test
  public void testDoNotOverrideMappingParametes() throws KettleException {
    prepareMappingParametesActions( false );
    Mockito.verify( transMeta, never() ).copyVariablesFrom( Mockito.any( VariableSpace.class ) );
  }

  private void prepareMappingParametesActions( boolean override ) throws KettleException {
    MappingMeta meta = new MappingMeta();
    meta.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    Repository rep = Mockito.mock( Repository.class );
    Mockito.when( step.getTransMeta().getRepository() ).thenReturn( rep );
    Mockito.when( rep.loadTransformation( Mockito.any( ObjectId.class ), Mockito.anyString() ) ).thenReturn( transMeta );

    MappingParameters mapPar = new MappingParameters();
    mapPar.setInheritingAllVariables( override );
    meta.setMappingParameters( mapPar );

    MappingData data = new MappingData();
    step.init( meta, data );
  }

}
