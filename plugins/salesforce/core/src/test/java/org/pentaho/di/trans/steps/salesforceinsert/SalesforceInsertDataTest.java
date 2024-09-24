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

package org.pentaho.di.trans.steps.salesforceinsert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceInsertDataTest {

  @Test
  public void testConstructor() {
    SalesforceInsertData data = new SalesforceInsertData();
    assertNull( data.inputRowMeta );
    assertNull( data.outputRowMeta );
    assertEquals( 0, data.nrfields );
    assertNull( data.fieldnrs );
    assertNull( data.saveResult );
    assertNull( data.sfBuffer );
    assertNull( data.outputBuffer );
    assertEquals( 0, data.iBufferPos );
    assertNull( data.realSalesforceFieldName );
  }
}
