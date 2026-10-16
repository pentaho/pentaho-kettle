/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * User: Dzmitry Stsiapanau Date: 2/6/14 Time: 2:29 PM
 */
public class StepDefinitionTest {
  @Test
  public void testClone() throws Exception {
    try {
      StepDefinition stepDefinition = new StepDefinition( "tag", "stepName", null, "" );
      stepDefinition.clone();
    } catch ( NullPointerException npe ) {
      fail( "Null value is not handled" );
    }
  }

}
