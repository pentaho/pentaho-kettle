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


package org.pentaho.di.trans.steps.salesforce;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SalesforceStepDataTest {

  @Test
  public void testConstructor() {
    SalesforceStepData data = new SalesforceStepData();
    assertNull( data.connection );
  }
}
