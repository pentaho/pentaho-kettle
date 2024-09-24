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
