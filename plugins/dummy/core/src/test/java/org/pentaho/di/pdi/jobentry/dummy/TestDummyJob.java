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


/*
 * Created on 17-Feb-07
 * Actualis Center
 *
 */
package org.pentaho.di.pdi.jobentry.dummy;

public class TestDummyJob {

  // @Test
  public void testDummyJob() throws Exception {
    DummyJob sp = new DummyJob( "testdata", "testout", ".*.xml" );
    sp.process();
  }
}
