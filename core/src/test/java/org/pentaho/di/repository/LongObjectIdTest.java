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

package org.pentaho.di.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class LongObjectIdTest {

  @Test
  public void testStringObjectId() {
    Long expectedId = new Random().nextLong();
    LongObjectId obj = new LongObjectId( expectedId );
    assertEquals( expectedId.toString(), obj.getId() );
    assertEquals( expectedId.toString(), obj.toString() );
    assertEquals( expectedId.hashCode(), obj.hashCode() );
    assertFalse( obj.equals( null ) );
    assertTrue( obj.equals( obj ) );
    assertEquals( 0, obj.compareTo( obj ) );

    LongObjectId clone = new LongObjectId( obj );
    assertNotSame( obj, clone );
    assertEquals( obj.getId(), clone.getId() );
  }
}
