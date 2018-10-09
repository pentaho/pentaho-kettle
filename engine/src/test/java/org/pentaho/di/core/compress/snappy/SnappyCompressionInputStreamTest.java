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

package org.pentaho.di.core.compress.snappy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;


public class SnappyCompressionInputStreamTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static final String PROVIDER_NAME = "Snappy";

  protected CompressionProviderFactory factory = null;
  protected SnappyCompressionInputStream inStream = null;
  protected CompressionProvider provider = null;

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
    provider = factory.getCompressionProviderByName( PROVIDER_NAME );
    inStream = new SnappyCompressionInputStream( createSnappyInputStream(), provider ) {
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
  public void getCompressionProvider() {
    assertEquals( provider.getName(), PROVIDER_NAME );
  }

  @Test
  public void testNextEntry() throws IOException {
    assertNull( inStream.nextEntry() );
  }

  @Test
  public void testClose() throws IOException {
    inStream = new SnappyCompressionInputStream( createSnappyInputStream(), provider );
    inStream.close();
  }

  @Test
  public void testRead() throws IOException {
    assertEquals( inStream.available(), inStream.read( new byte[100], 0, inStream.available() ) );
  }

  /**
   * Test reading header bytes from a single file 'given' a sz file format in apache VFS2 URI format.
   * @throws Exception
   */
  @Test
  public void testReadByte_Single_File() throws Exception {
    CompressionProvider provider = inStream.getCompressionProvider();
    String expectedText = "id,username";
    String vfsPath = getTestCustomerSz().getPublicURIString();
    FileObject foSzFile  = KettleVFS.getFileObject( vfsPath );

    inStream = new SnappyCompressionInputStream( foSzFile.getContent().getInputStream(), provider ) {
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
   * Helper method to get test sz file object.
   * @return
   * @throws Exception
   */
  private FileObject getTestCustomerSz() throws Exception {
    String relativePath = "src/test/resources/org/pentaho/di/core/compress/snappy/test_customer2.csv.sz";
    return KettleVFS.getFileObject( relativePath );
  }

  private SnappyInputStream createSnappyInputStream() throws IOException {
    // Create an in-memory ZIP output stream for use by the input stream (to avoid exceptions)
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    SnappyOutputStream sos = new SnappyOutputStream( baos );
    byte[] testBytes = "Test".getBytes();
    sos.write( testBytes );
    ByteArrayInputStream in = new ByteArrayInputStream( baos.toByteArray() );
    sos.close();

    return new SnappyInputStream( in );
  }
}
