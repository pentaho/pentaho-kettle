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


package org.pentaho.di.core.lifecycle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by mburgess on 10/12/15.
 */
public class LifecycleExceptionTest {

  LifecycleException exception;

  @Before
  public void setUp() throws Exception {
    exception = new LifecycleException( true );
  }

  @Test
  public void testIsSevere() throws Exception {
    assertTrue( exception.isSevere() );
  }

  @Test
  public void testMessage() {
    exception = new LifecycleException( "message", false );
    assertEquals( "message", exception.getMessage() );
  }

  @Test
  public void testThrowableCtor() {
    Throwable t = mock( Throwable.class );
    exception = new LifecycleException( t, true );
    assertEquals( t, exception.getCause() );
  }

  @Test
  public void testThrowableMessageCtor() {
    Throwable t = mock( Throwable.class );
    exception = new LifecycleException( "message", t, true );
    assertEquals( t, exception.getCause() );
    assertEquals( "message", exception.getMessage() );
    assertTrue( exception.isSevere() );
  }
}
