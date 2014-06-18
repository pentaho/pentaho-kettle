package org.pentaho.di.trans.steps.mapping;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.StepMockUtil;

public class MappingTestParameters {

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
   * PDI-3064 Test copy variables from actually copy variables from parent to child transformation
   * 
   * @throws Exception
   */
  @Test
  public void testInheritAllParametersCopy() throws Exception {
    MappingParameters param = new MappingParameters();
    step.setVariable( "a", "1" );
    step.setVariable( "b", "2" );
    param.setInheritingAllVariables( true );
    when( transMeta.listParameters() ).thenReturn( new String[] { "a" } );
    step.setMappingParameters( trans, transMeta, param );
    verify( trans ).setVariable( "b", "2" );
    verify( trans ).setParameterValue( "a", "1" );
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
    step.setMappingParameters( trans, transMeta, param );

    // parameters was overridden 2 times
    Mockito.verify( trans, Mockito.times( 1 ) )
      .setParameterValue( Mockito.anyString(), Mockito.anyString() );
    Mockito.verify( trans, Mockito.times( 1 ) )
      .setVariable( Mockito.anyString(), Mockito.anyString() );
  }
}
