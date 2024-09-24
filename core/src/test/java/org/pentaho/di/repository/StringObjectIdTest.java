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

import java.util.UUID;

import org.junit.Test;

public class StringObjectIdTest {

  @Test
  public void testStringObjectId() {
    String expectedId = UUID.randomUUID().toString();
    StringObjectId obj = new StringObjectId( expectedId );
    assertEquals( expectedId, obj.getId() );
    assertEquals( expectedId, obj.toString() );
    assertEquals( expectedId.hashCode(), obj.hashCode() );
    assertFalse( obj.equals( null ) );
    assertTrue( obj.equals( obj ) );
    assertEquals( 0, obj.compareTo( obj ) );

    StringObjectId clone = new StringObjectId( obj );
    assertNotSame( obj, clone );
    assertEquals( obj.getId(), clone.getId() );
  }
}
