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


package org.pentaho.di.trans.step;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StepStatusTest {

  @Test
  public void testOverrideDescription() {
    StepStatus status = new StepStatus();
    status.setStatusDescription( "Empty" );
    String[] overrides = status.getTransLogFields( "Override" );
    assertEquals( "Override", overrides[10] );
  }
}
