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

public class ObjectUsageCountTest {
  @Test
  public void testClass() {
    final String name = "Object name";
    final int nrUses = 9;
    ObjectUsageCount count = new ObjectUsageCount( name, nrUses );
    assertSame( name, count.getObjectName() );
    assertEquals( nrUses, count.getNrUses() );
    assertEquals( name + ";" + nrUses, count.toString() );
    assertEquals( nrUses + 1, count.increment() );
    count.reset();
    assertEquals( 0, count.getNrUses() );
    count.setObjectName( null );
    assertNull( count.getObjectName() );
    count.setNrUses( nrUses );
    assertEquals( nrUses, count.getNrUses() );

    assertEquals( -1, count.compare( ObjectUsageCount.fromString( "Obj1;2" ), ObjectUsageCount.fromString( "Obj2" ) ) );
  }
}
