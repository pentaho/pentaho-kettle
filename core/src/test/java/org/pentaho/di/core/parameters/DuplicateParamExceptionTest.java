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

package org.pentaho.di.core.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by mburgess on 10/13/15.
 */
public class DuplicateParamExceptionTest {
  DuplicateParamException exception;

  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testConstructors() {
    exception = new DuplicateParamException();
    assertNotNull( exception );
    NamedParamsExceptionTest.assertMessage( "null", exception );

    exception = new DuplicateParamException( "message" );
    NamedParamsExceptionTest.assertMessage( "message", exception );

    Throwable t = mock( Throwable.class );
    when( t.getStackTrace() ).thenReturn( new StackTraceElement[0] );
    exception = new DuplicateParamException( t );
    assertEquals( t, exception.getCause() );

    exception = new DuplicateParamException( "message", t );
    NamedParamsExceptionTest.assertMessage( "message", exception );
    assertEquals( t, exception.getCause() );
  }
}
