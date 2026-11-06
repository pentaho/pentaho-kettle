/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ScriptValueAddFunctions_SetVariableScopeTest {
  private static final String VARIABLE_NAME = "variable-name";
  private static final String VARIABLE_VALUE = "variable-value";

  @Test
  public void setParentScopeVariable_ParentIsTrans() {
    Trans parent = createTrans();
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setParentScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setParentScopeVariable_ParentIsJob() {
    Job parent = createJob();
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setParentScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setParentScopeVariable_NoParent() {
    Trans trans = createTrans( );

    ScriptValuesAddedFunctions.setParentScopeVariable( trans, VARIABLE_NAME, VARIABLE_VALUE );

    verify( trans ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }


  @Test
  public void setGrandParentScopeVariable_TwoLevelHierarchy() {
    Trans parent = createTrans( );
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setGrandParentScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }


  @Test
  public void setGrandParentScopeVariable_ThreeLevelHierarchy() {
    Job grandParent = createJob();
    Trans parent = createTrans( grandParent );
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setGrandParentScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( grandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setGrandParentScopeVariable_FourLevelHierarchy() {
    Job grandGrandParent = createJob();
    Trans grandParent = createTrans( grandGrandParent );
    Trans parent = createTrans( grandParent );
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setGrandParentScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( grandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( grandGrandParent, never() ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setGrandParentScopeVariable_NoParent() {
    Trans trans = createTrans( );

    ScriptValuesAddedFunctions.setGrandParentScopeVariable( trans, VARIABLE_NAME, VARIABLE_VALUE );

    verify( trans ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setRootScopeVariable_TwoLevelHierarchy() {
    Trans parent = createTrans( );
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setRootScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setRootScopeVariable_FourLevelHierarchy() {
    Job grandGrandParent = createJob();
    Trans grandParent = createTrans( grandGrandParent );
    Trans parent = createTrans( grandParent );
    Trans child = createTrans( parent );

    ScriptValuesAddedFunctions.setRootScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

    verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( grandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    verify( grandGrandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }

  @Test
  public void setRootScopeVariable_NoParent() {
    Trans trans = createTrans( );

    ScriptValuesAddedFunctions.setRootScopeVariable( trans, VARIABLE_NAME, VARIABLE_VALUE );

    verify( trans ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
  }


  @Test
  public void setSystemScopeVariable_NoParent() {
    Trans trans = createTrans();

    Assert.assertNull( System.getProperty( VARIABLE_NAME ) );

    try {
      ScriptValuesAddedFunctions.setSystemScopeVariable( trans, VARIABLE_NAME, VARIABLE_VALUE );

      Assert.assertEquals( System.getProperty( VARIABLE_NAME ), VARIABLE_VALUE );
      verify( trans ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    } finally {
      System.clearProperty( VARIABLE_NAME );
    }

  }

  @Test
  public void setSystemScopeVariable_FourLevelHierarchy() {
    Job grandGrandParent = createJob();
    Trans grandParent = createTrans( grandGrandParent );
    Trans parent = createTrans( grandParent );
    Trans child = createTrans( parent );

    Assert.assertNull( System.getProperty( VARIABLE_NAME ) );

    try {
      ScriptValuesAddedFunctions.setSystemScopeVariable( child, VARIABLE_NAME, VARIABLE_VALUE );

      Assert.assertEquals( System.getProperty( VARIABLE_NAME ), VARIABLE_VALUE );

      verify( child ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
      verify( parent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
      verify( grandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
      verify( grandGrandParent ).setVariable( eq( VARIABLE_NAME ), eq( VARIABLE_VALUE ) );
    } finally {
      System.clearProperty( VARIABLE_NAME );
    }
  }




  private Trans createTrans( Trans parent ) {
    Trans trans = createTrans();

    trans.setParent( parent );
    trans.setParentVariableSpace( parent );

    return trans;
  }


  private Trans createTrans( Job parent ) {
    Trans trans = createTrans();

    trans.setParentJob( parent );
    trans.setParentVariableSpace( parent );

    return trans;
  }

  private Trans createTrans() {
    Trans trans = new Trans();
    trans.setLog( mock( LogChannelInterface.class ) );

    trans = spy( trans );

    return trans;
  }

  private Job createJob() {
    Job job = new Job(  );
    job = spy( job );

    return job;
  }
}
