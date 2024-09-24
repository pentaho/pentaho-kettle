/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by mburgess on 10/12/15.
 */
public class LifeEventInfoTest {

  LifeEventInfo info;

  @Before
  public void setUp() throws Exception {
    info = new LifeEventInfo();
  }

  @Test
  public void testSetHasHint() throws Exception {
    LifeEventInfo.Hint hint = LifeEventInfo.Hint.DISPLAY_BROWSER;
    assertFalse( info.hasHint( null ) );
    assertFalse( info.hasHint( hint ) );
    info.setHint( hint );
    assertTrue( info.hasHint( hint ) );
    assertFalse( info.hasHint( LifeEventInfo.Hint.DISPLAY_MSG_BOX ) );
  }

  @Test
  public void testGetSetMessage() throws Exception {
    assertNull( info.getMessage() );
    info.setMessage( "message" );
    assertEquals( "message", info.getMessage() );
  }

  @Test
  public void testGetState() throws Exception {
    assertNull( info.getState() );
    info.setState( LifeEventInfo.State.FAIL );
    assertEquals( LifeEventInfo.State.FAIL, info.getState() );
  }

  @Test
  public void testGetName() throws Exception {
    assertNull( info.getName() );
    info.setName( "name" );
    assertEquals( "name", info.getName() );
  }
}
