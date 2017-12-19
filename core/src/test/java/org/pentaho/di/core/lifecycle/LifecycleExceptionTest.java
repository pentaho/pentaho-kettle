/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
