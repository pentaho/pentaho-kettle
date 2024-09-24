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

package org.pentaho.di.core.compress.gzip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class GZIPCompressionOutputStreamTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static final String PROVIDER_NAME = "GZip";

  public CompressionProviderFactory factory = null;
  public GZIPCompressionOutputStream outStream = null;

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
    ByteArrayOutputStream in = new ByteArrayOutputStream();
    outStream = new GZIPCompressionOutputStream( in, provider );
  }

  @After
  public void tearDown() throws Exception {
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
    outStream = new GZIPCompressionOutputStream( out, provider ) {
    };
    outStream.close();
    try {
      outStream.write( "This will throw an Exception if the stream is already closed".getBytes() );
      fail();
    } catch ( IOException e ) {
      //Success, The Output Stream was already closed
    }
  }

  @Test
  public void testWrite() throws IOException {
    CompressionProvider provider = outStream.getCompressionProvider();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outStream = new GZIPCompressionOutputStream( out, provider );
    outStream.write( "Test".getBytes() );
  }

  @Test
  public void testAddEntry() throws IOException {
    CompressionProvider provider = outStream.getCompressionProvider();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outStream = new GZIPCompressionOutputStream( out, provider );
    outStream.addEntry( null, null );
  }
}
