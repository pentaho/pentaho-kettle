package org.pentaho.di.core;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
