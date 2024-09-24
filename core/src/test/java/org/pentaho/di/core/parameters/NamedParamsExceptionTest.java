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
package org.pentaho.di.core.parameters;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mburgess on 10/13/15.
 */
public class NamedParamsExceptionTest {

  NamedParamsException exception;

  @Test
  public void testConstructors() {
    exception = new NamedParamsException();
    assertNotNull( exception );
    assertMessage( "null", exception );

    exception = new NamedParamsException( "message" );
    assertMessage( "message", exception );

    Throwable t = mock( Throwable.class );
    when( t.getStackTrace() ).thenReturn( new StackTraceElement[ 0 ] );
    exception = new NamedParamsException( t );
    assertEquals( t, exception.getCause() );

    exception = new NamedParamsException( "message", t );
    assertMessage( "message", exception );
    assertEquals( t, exception.getCause() );
  }

  static void assertMessage( String expected, NamedParamsException exception ) {
    String surrounded = System.lineSeparator() + expected + System.lineSeparator();
    assertEquals( surrounded, exception.getMessage() );
  }
}
