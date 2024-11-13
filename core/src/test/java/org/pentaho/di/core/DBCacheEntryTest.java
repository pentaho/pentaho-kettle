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
