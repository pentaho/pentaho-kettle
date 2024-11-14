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


package org.pentaho.di.trans.steps.salesforcedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceDeleteDataTest {

  @Test
  public void testConstructor() {
    SalesforceDeleteData data = new SalesforceDeleteData();
    assertNull( data.inputRowMeta );
    assertNull( data.outputRowMeta );
    assertNull( data.deleteResult );
    assertNull( data.deleteId );
    assertNull( data.outputBuffer );
    assertEquals( 0, data.iBufferPos );
    assertEquals( -1, data.indexOfKeyField );
  }
}
