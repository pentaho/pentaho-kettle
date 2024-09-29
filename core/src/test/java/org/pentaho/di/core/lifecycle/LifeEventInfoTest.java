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
