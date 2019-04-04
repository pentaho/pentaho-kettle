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
