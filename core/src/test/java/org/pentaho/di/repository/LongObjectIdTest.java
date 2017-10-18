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
