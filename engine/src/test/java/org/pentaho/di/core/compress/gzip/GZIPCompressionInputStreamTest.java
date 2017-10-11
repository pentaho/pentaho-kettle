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

package org.pentaho.di.core.compress.gzip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;

public class GZIPCompressionInputStreamTest {

  public static final String PROVIDER_NAME = "GZip";

  protected CompressionProviderFactory factory = null;
  protected GZIPCompressionInputStream inStream = null;
  protected CompressionProvider provider = null;

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
    provider = factory.getCompressionProviderByName( PROVIDER_NAME );
    inStream = new GZIPCompressionInputStream( createGZIPInputStream(), provider ) {
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
    assertNull( inStream.nextEntry() );
  }

  @Test
  public void testClose() throws IOException {
    inStream = new GZIPCompressionInputStream( createGZIPInputStream(), provider ) {
    };
    inStream.close();
  }

  @Test
  public void testRead() throws IOException {
    inStream = new GZIPCompressionInputStream( createGZIPInputStream(), provider ) {
    };
    inStream.read( new byte[100], 0, inStream.available() );
  }

  protected InputStream createGZIPInputStream() throws IOException {
    // Create an in-memory GZIP output stream for use by the input stream (to avoid exceptions)
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gos = new GZIPOutputStream( baos );
    byte[] testBytes = "Test".getBytes();
    gos.write( testBytes );
    ByteArrayInputStream in = new ByteArrayInputStream( baos.toByteArray() );
    return in;
  }
}
