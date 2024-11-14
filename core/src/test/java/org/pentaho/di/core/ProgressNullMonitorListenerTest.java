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

package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProgressNullMonitorListenerTest {

  @Test
  public void testClass() throws Exception {
    ProgressNullMonitorListener listener = new ProgressNullMonitorListener();
    listener.beginTask( "", 0 );
    listener.subTask( "" );
    assertFalse( listener.isCanceled() );
    listener.worked( 0 );
    listener.done();
    listener.setTaskName( "" );
  }
}
