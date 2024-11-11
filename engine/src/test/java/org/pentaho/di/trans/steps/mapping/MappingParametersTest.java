/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
    Mockito.when(  param.isInheritingAllVariables() ).thenReturn( true );
    when( transMeta.listParameters() ).thenReturn( new String[] { "a" } );
    StepWithMappingMeta
      .activateParams( trans, trans, step, transMeta.listParameters(), param.getVariable(), param.getInputField(), param.isInheritingAllVariables() );
    // parameters was overridden 2 times
    // new call of setParameterValue added in StepWithMappingMeta - wantedNumberOfInvocations is now to 2
    Mockito.verify( trans, Mockito.times( 2 ) ).setParameterValue( Mockito.anyString(), Mockito.anyString() );
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
