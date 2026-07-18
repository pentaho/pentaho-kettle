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
