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



package org.pentaho.di.trans.steps.salesforceinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceInputDataTest {

  @Test
  public void testConstructor() {
    SalesforceInputData data = new SalesforceInputData();
    assertEquals( 0, data.nr_repeats );
    assertEquals( 0, data.rownr );
    assertNull( data.previousRow );
    assertNull( data.inputRowMeta );
    assertNull( data.outputRowMeta );
    assertNull( data.convertRowMeta );
    assertEquals( 0, data.recordcount );
    assertEquals( 0, data.nrfields );
    assertEquals( false, data.limitReached );
    assertEquals( 0, data.limit );
    assertEquals( 0, data.nrRecords );
    assertEquals( 0, data.recordIndex );
    assertNull( data.startCal );
    assertNull( data.endCal );
    assertEquals( false, data.finishedRecord );
  }
}
