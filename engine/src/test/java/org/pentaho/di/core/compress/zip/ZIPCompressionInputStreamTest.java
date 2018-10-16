/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;


public class ZIPCompressionInputStreamTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static final String PROVIDER_NAME = "Zip";

  public CompressionProviderFactory factory = null;
  public ZIPCompressionInputStream inStream = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( false );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    factory = CompressionProviderFactory.getInstance();
    CompressionProvider provider = factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    inStream = new ZIPCompressionInputStream( in, provider ) {
    };
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCtor() {
    assertNotNull( inStream );
  }

  @Test
  public void getZIPCompressionProvider() {
    CompressionProvider provider = inStream.getCompressionProvider();
    assertEquals( provider.getName(), PROVIDER_NAME );
  }

  @Test
  public void testNextEntry() throws IOException {
    assertNotNull( createZIPInputStream().getNextEntry() );
  }

  @Test
  public void testClose() throws IOException {
    createZIPInputStream().close();
  }

  @Test
  public void testRead() throws IOException {
    CompressionProvider provider = inStream.getCompressionProvider();
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    inStream = new ZIPCompressionInputStream( in, provider ) {
    };
    inStream.read( new byte[100], 0, inStream.available() );
  }

  /**
   * Test reading header bytes from a single file 'given' a nested zip file format in apache VFS2 URI format.
   * @throws Exception
   */
  @Test
  public void testReadByte_Single_File() throws Exception {
    CompressionProvider provider = inStream.getCompressionProvider();
    String expectedText = "id,username";
    String vfsPath = "zip:" + getTestCustomerZip().getPublicURIString() + "!/test_customer2.csv";
    FileObject foZipNestedFile  = KettleVFS.getFileObject( vfsPath );

    inStream = new ZIPCompressionInputStream( foZipNestedFile.getContent().getInputStream(), provider ) {
    };

    StringBuilder sb  = new StringBuilder();

    for ( int i = 0; i < expectedText.length(); ++i ) {
      int c = inStream.read();
      if ( c < 0 ) {
        fail( "It doesn't read!!!!" );
      }
      sb.append( (char) c );
    }

    assertEquals( expectedText, sb.toString() );
  }

  /**
   * Test reading header bytes from a single file 'given' a zip parent directory in apache VFS2 URI format.
   * @throws Exception
   */
  @Test
  public void testReadByte_Zip() throws Exception {
    CompressionProvider provider = inStream.getCompressionProvider();
    String expectedText = "id,username";
    String vfsPath = "zip:" + getTestCustomerZip().getPublicURIString();
    FileObject foZip  = KettleVFS.getFileObject( vfsPath );
    FileObject[] files = foZip.findFiles( new AllFileSelector() );

    /* SANITY CHECK
     * expected 3 files: parent directory, test_customer1.csv, test_customer2.csv
     */
    assertTrue( files.length >= 3 );

    FileObject zipCsvFile = Arrays.stream( files )
            .filter( f -> f.getPublicURIString().endsWith( "csv" ) ).findFirst().get();

    inStream = new ZIPCompressionInputStream( zipCsvFile.getContent().getInputStream(), provider ) {
    };


    StringBuilder sb  = new StringBuilder();

    for ( int i = 0; i < expectedText.length(); ++i ) {
      int c = inStream.read();
      if ( c < 0 ) {
        fail( "It doesn't read!!!!" );
      }
      sb.append( (char) c );
    }

    assertEquals( expectedText, sb.toString() );
  }

  /**
   * Helper method to get test zip file object.
   * @return
   * @throws Exception
   */
  private FileObject getTestCustomerZip() throws Exception {
    String relativePath = "src/test/resources/org/pentaho/di/core/compress/zip/test_customer.zip";
    return KettleVFS.getFileObject( relativePath );
  }

  private ZipInputStream createZIPInputStream() throws IOException {
    // Create an in-memory ZIP output stream for use by the input stream (to avoid exceptions)
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream gos = new ZipOutputStream( baos );
    gos.putNextEntry( new ZipEntry( "./test.txt" ) );
    byte[] testBytes = "Test".getBytes();
    gos.write( testBytes );
    ByteArrayInputStream in = new ByteArrayInputStream( baos.toByteArray() );

    return new ZipInputStream( in );
  }
}
