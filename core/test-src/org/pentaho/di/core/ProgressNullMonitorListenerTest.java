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
