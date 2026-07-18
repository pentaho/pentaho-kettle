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



package org.pentaho.di.trans.steps.salesforceupsert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceUpsertDataTest {

  @Test
  public void testConstructor() {
    SalesforceUpsertData data = new SalesforceUpsertData();
    assertNull( data.inputRowMeta );
    assertNull( data.outputRowMeta );
    assertEquals( 0, data.nrfields );
    assertNull( data.fieldnrs );
    assertNull( data.upsertResult );
    assertNull( data.sfBuffer );
    assertNull( data.outputBuffer );
    assertEquals( 0, data.iBufferPos );
    assertNull( data.realSalesforceFieldName );
  }
}
