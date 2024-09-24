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

package org.pentaho.di.core.hash;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;

public class ByteArrayHashIndexTest {

  @Test
  public void testArraySizeConstructor() {
    ByteArrayHashIndex obj = new ByteArrayHashIndex( new RowMeta(), 1 );
    assertEquals( 1, obj.getSize() );

    obj = new ByteArrayHashIndex( new RowMeta(), 2 );
    assertEquals( 2, obj.getSize() );

    obj = new ByteArrayHashIndex( new RowMeta(), 3 );
    assertEquals( 4, obj.getSize() );

    obj = new ByteArrayHashIndex( new RowMeta(), 12 );
    assertEquals( 16, obj.getSize() );

    obj = new ByteArrayHashIndex( new RowMeta(), 99 );
    assertEquals( 128, obj.getSize() );
  }

  @Test
  public void testGetAndPut() throws KettleValueException {
    ByteArrayHashIndex obj = new ByteArrayHashIndex( new RowMeta(), 10 );
    assertNull( obj.get( new byte[]{ 10 } ) );

    obj.put( new byte[]{ 10 }, new byte[]{ 53, 12 } );
    assertNotNull( obj.get( new byte[]{ 10 } ) );
    assertArrayEquals( new byte[]{ 53, 12 }, obj.get( new byte[]{ 10 } ) );
  }
}
