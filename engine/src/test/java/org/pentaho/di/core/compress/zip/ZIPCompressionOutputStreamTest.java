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

package org.pentaho.di.core.compress.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;

public class ZIPCompressionOutputStreamTest {

  public static final String PROVIDER_NAME = "Zip";

  public CompressionProviderFactory factory = null;
  public ZIPCompressionOutputStream outStream = null;

  private ByteArrayOutputStream internalStream;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    factory = CompressionProviderFactory.getInstance();
    CompressionProvider provider = factory.getCompressionProviderByName( PROVIDER_NAME );
    internalStream = new ByteArrayOutputStream();
    outStream = new ZIPCompressionOutputStream( internalStream, provider );
  }

  @After
  public void tearDown() throws Exception {
    internalStream = null;
  }

  @Test
  public void testCtor() {
    assertNotNull( outStream );
  }

  @Test
  public void getCompressionProvider() {
    CompressionProvider provider = outStream.getCompressionProvider();
    assertEquals( provider.getName(), PROVIDER_NAME );
  }

  @Test
  public void testClose() throws IOException {
    CompressionProvider provider = outStream.getCompressionProvider();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outStream = new ZIPCompressionOutputStream( out, provider );
    outStream.close();
  }

  @Test
  public void testAddEntryAndWrite() throws IOException {
    CompressionProvider provider = outStream.getCompressionProvider();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outStream = new ZIPCompressionOutputStream( out, provider );
    outStream.addEntry( "./test.zip", null );
    outStream.write( "Test".getBytes() );
  }

  @Test
  public void directoriesHierarchyIsIgnored() throws Exception {
    outStream.addEntry( createFilePath( "1", "~", "pentaho", "dir" ), "txt" );
    outStream.close();

    Map<String, String> map = readArchive( internalStream.toByteArray() );
    assertEquals( 1, map.size() );
    assertEquals( "1.txt", map.keySet().iterator().next() );
  }

  @Test
  public void extraZipExtensionIsIgnored() throws Exception {
    outStream.addEntry( createFilePath( "1.zip", "~", "pentaho", "dir" ), "txt" );
    outStream.close();

    Map<String, String> map = readArchive( internalStream.toByteArray() );
    assertEquals( 1, map.size() );
    assertEquals( "1.txt", map.keySet().iterator().next() );
  }

  @Test
  public void absentExtensionIsOk() throws Exception {
    outStream.addEntry( createFilePath( "1", "~", "pentaho", "dir" ), null );
    outStream.close();

    Map<String, String> map = readArchive( internalStream.toByteArray() );
    assertEquals( 1, map.size() );
    assertEquals( "1", map.keySet().iterator().next() );
  }

  @Test
  public void createsWellFormedArchive() throws Exception {
    outStream.addEntry( "1", "txt" );
    outStream.write( "1.txt".getBytes() );
    outStream.addEntry( "2", "txt" );
    outStream.write( "2.txt".getBytes() );
    outStream.close();

    Map<String, String> map = readArchive( internalStream.toByteArray() );
    assertEquals( "1.txt", map.remove( "1.txt" ) );
    assertEquals( "2.txt", map.remove( "2.txt" ) );
    assertTrue( map.isEmpty() );
  }


  private static String createFilePath( String file, String... directories ) {
    StringBuilder sb = new StringBuilder(  );
    for ( String dir : directories ) {
      sb.append( dir ).append( Const.FILE_SEPARATOR );
    }
    return sb.append( file ).toString();
  }

  private static Map<String, String> readArchive( byte[] bytes ) throws Exception {
    Map<String, String> result = new HashMap<String, String>();

    ZipInputStream stream = new ZipInputStream( new ByteArrayInputStream( bytes ) );
    byte[] buf = new byte[ 256 ];
    ZipEntry entry;
    while ( ( entry = stream.getNextEntry() ) != null ) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      int read;
      while ( ( read = stream.read( buf ) ) > 0 ) {
        os.write( buf, 0, read );
      }
      result.put( entry.getName(), new String( os.toByteArray() ) );
    }

    return result;
  }
}
