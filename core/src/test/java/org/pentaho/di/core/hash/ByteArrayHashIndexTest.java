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
