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
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class DBCacheEntryTest {
  @Test
  public void testClass() throws IOException, KettleFileException {
    final String dbName = "dbName";
    final String sql = "sql query";
    DBCacheEntry entry = new DBCacheEntry( dbName, sql );
    assertTrue( entry.sameDB( "dbName" ) );
    assertFalse( entry.sameDB( "otherDb" ) );
    assertEquals( dbName.toLowerCase().hashCode() ^ sql.toLowerCase().hashCode(), entry.hashCode() );
    DBCacheEntry otherEntry = new DBCacheEntry();
    assertFalse( otherEntry.sameDB( "otherDb" ) );
    assertEquals( 0, otherEntry.hashCode() );
    assertFalse( entry.equals( otherEntry ) );
    assertFalse( entry.equals( new Object() ) );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream( baos );

    dos.writeUTF( dbName );
    dos.writeUTF( sql );

    byte[] bytes = baos.toByteArray();
    InputStream is = new ByteArrayInputStream( bytes );
    DataInputStream dis = new DataInputStream( is );
    DBCacheEntry disEntry = new DBCacheEntry( dis );
    assertTrue( disEntry.equals( entry ) );
    try {
      new DBCacheEntry( dis );
      fail( "Should throw KettleEOFException on EOFException" );
    } catch ( KettleEOFException keofe ) {
      // Ignore
    }

    baos.reset();

    assertTrue( disEntry.write( dos ) );
    assertArrayEquals( bytes, baos.toByteArray() );
  }
}
