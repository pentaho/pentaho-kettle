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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.*;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.steps.StepMockUtil;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrea Torre
 */
public class ScriptValuesAddedFunctionsTest {
  ScriptValuesMod step;
  ScriptValuesMetaMod meta;
  ScriptValuesModData data;
  String variableKey;
  String variableValue;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    step = StepMockUtil.getStep( ScriptValuesMod.class, ScriptValuesMetaMod.class, "test" );
    meta = new ScriptValuesMetaMod();
    data = new ScriptValuesModData();

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaString( "variable_name" ) );
    input.addValueMeta( new ValueMetaString( "variable_data" ) );
    step.setInputRowMeta( input );
    step = spy( step );

    variableKey = "var_1";
    variableValue = "dont panic";
    doReturn( new Object[] {variableKey, variableValue} ).when( step ).getRow();
    meta.setCompatible( false );
    meta.allocate( 0 );
  }

  private void setTestMetaAtVariableLevel( String level ) {
    meta.setJSScripts( new ScriptValuesScript[] {
      new ScriptValuesScript( ScriptValuesScript.TRANSFORM_SCRIPT, "script",
          "setVariable(variable_name, variable_data, '" + level + "')" )
    } );
  }

  @Test
  public void shouldSetVariableAtSystemLevel() throws KettleException {
    setTestMetaAtVariableLevel( "s" );

    step.init( meta, data );
    Trans m_trans = mock( Trans.class );
    when( step.getTrans() ).thenReturn( m_trans );

    Trans m_parentTrans = mock( Trans.class );
    when( m_trans.getParentTrans() ).thenReturn( m_parentTrans );

    Job m_parentJob = mock( Job.class );
    when( m_parentTrans.getParentJob() ).thenReturn( m_parentJob );

    Job m_grandParentJob = mock( Job.class );
    when( m_parentJob.getParentJob() ).thenReturn( m_grandParentJob );

    Job m_rootJob = mock( Job.class );
    when( m_grandParentJob.getParentJob() ).thenReturn( m_rootJob );

    step.processRow( meta, data );

    // check the variable set in the step
    assertTrue( step.getVariable( variableKey ).equals( variableValue ) );

    // check variable has been set in the transformation where the step is defined
    verify( m_trans ).setVariable( variableKey, variableValue );

    // check variable has been set in a parent transformation (in case of sub-trans)
    verify( m_parentTrans ).setVariable( variableKey, variableValue );

    // check the variable set in the JVM
    assertTrue( System.getProperty( variableKey ).equals( variableValue ) );

    // check the variable set in the parent Job
    verify( m_parentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the grand parent Job if any
    verify( m_grandParentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the root Job if any
    verify( m_rootJob ).setVariable( variableKey, variableValue );
  }

  @Test
  public void shouldSetVariableAtRootJobLevel() throws KettleException {
    setTestMetaAtVariableLevel( "r" );

    step.init( meta, data );
    Trans m_trans = mock( Trans.class );
    when( step.getTrans() ).thenReturn( m_trans );

    Trans m_parentTrans = mock( Trans.class );
    when( m_trans.getParentTrans() ).thenReturn( m_parentTrans );

    Job m_parentJob = mock( Job.class );
    when( m_parentTrans.getParentJob() ).thenReturn( m_parentJob );

    Job m_grandParentJob = mock( Job.class );
    when( m_parentJob.getParentJob() ).thenReturn( m_grandParentJob );

    Job m_rootJob = mock( Job.class );
    when( m_grandParentJob.getParentJob() ).thenReturn( m_rootJob );

    step.processRow( meta, data );

    // check the variable set in the step
    assertTrue( step.getVariable( variableKey ).equals( variableValue ) );

    // check variable has been set in the transformation where the step is defined
    verify( m_trans ).setVariable( variableKey, variableValue );

    // check variable has been set in a parent transformation (in case of sub-trans)
    verify( m_parentTrans ).setVariable( variableKey, variableValue );

    // check the variable is NOT set in the JVM
    assertNull( "Variable should not be set in the JVM", System.getProperty( variableKey ) );

    // check the variable set in the parent Job
    verify( m_parentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the grand parent Job if any
    verify( m_grandParentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the root Job if any
    verify( m_rootJob ).setVariable( variableKey, variableValue );
  }

  @Test
  public void shouldSetVariableAtGrandParentJobLevel() throws KettleException {
    setTestMetaAtVariableLevel( "g" );

    step.init( meta, data );
    Trans m_trans = mock( Trans.class );
    when( step.getTrans() ).thenReturn( m_trans );

    Trans m_parentTrans = mock( Trans.class );
    when( m_trans.getParentTrans() ).thenReturn( m_parentTrans );

    Job m_parentJob = mock( Job.class );
    when( m_parentTrans.getParentJob() ).thenReturn( m_parentJob );

    Job m_grandParentJob = mock( Job.class );
    when( m_parentJob.getParentJob() ).thenReturn( m_grandParentJob );

    Job m_rootJob = mock( Job.class );
    when( m_grandParentJob.getParentJob() ).thenReturn( m_rootJob );

    step.processRow( meta, data );

    // check the variable set in the step
    assertTrue( step.getVariable( variableKey ).equals( variableValue ) );

    // check variable has been set in the transformation where the step is defined
    verify( m_trans ).setVariable( variableKey, variableValue );

    // check variable has been set in a parent transformation (in case of sub-trans)
    verify( m_parentTrans ).setVariable( variableKey, variableValue );

    // check the variable is NOT set in the JVM
    assertNull( "Variable should not be set in the JVM", System.getProperty( variableKey ) );

    // check the variable set in the parent Job
    verify( m_parentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the grand parent Job if any
    verify( m_grandParentJob ).setVariable( variableKey, variableValue );

    // check the setVariable method has NOT been called on a root Job
    verify( m_rootJob, never() ).setVariable( variableKey, variableValue );
  }

  @Test
  public void shouldSetVariableAtParentJobLevel() throws KettleException {
    setTestMetaAtVariableLevel( "p" );

    step.init( meta, data );
    Trans m_trans = mock( Trans.class );
    when( step.getTrans() ).thenReturn( m_trans );

    Trans m_parentTrans = mock( Trans.class );
    when( m_trans.getParentTrans() ).thenReturn( m_parentTrans );

    Job m_parentJob = mock( Job.class );
    when( m_parentTrans.getParentJob() ).thenReturn( m_parentJob );

    Job m_grandParentJob = mock( Job.class );
    when( m_parentJob.getParentJob() ).thenReturn( m_grandParentJob );

    Job m_rootJob = mock( Job.class );
    when( m_grandParentJob.getParentJob() ).thenReturn( m_rootJob );

    step.processRow( meta, data );

    // check the variable set in the step
    assertTrue( step.getVariable( variableKey ).equals( variableValue ) );

    // check variable has been set in the transformation where the step is defined
    verify( m_trans ).setVariable( variableKey, variableValue );

    // check variable has been set in a parent transformation (in case of sub-trans)
    verify( m_parentTrans ).setVariable( variableKey, variableValue );

    // check the variable is NOT set in the JVM
    assertNull( "Variable should not be set in the JVM", System.getProperty( variableKey ) );

    // check the variable set in the parent Job
    verify( m_parentJob ).setVariable( variableKey, variableValue );

    // check the variable set in the grand parent Job if any
    verify( m_grandParentJob, never() ).setVariable( variableKey, variableValue );

    // check the setVariable method has NOT been called on a root Job
    verify( m_rootJob, never() ).setVariable( variableKey, variableValue );
  }


  @After
  public void tearDown() throws Exception {
    // remove the variable from the JVM
    System.clearProperty( variableKey );
  }
}
