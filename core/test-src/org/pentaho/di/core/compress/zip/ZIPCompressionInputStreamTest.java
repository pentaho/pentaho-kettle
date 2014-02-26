package org.pentaho.di.core.compress.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;

public class ZIPCompressionInputStreamTest {

  public static final String PROVIDER_NAME = "Zip";

  public CompressionProviderFactory factory = null;
  public ZIPCompressionInputStream inStream = null;

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
    assertNull( inStream.nextEntry() );
  }

  @Test
  public void testClose() throws IOException {
    CompressionProvider provider = inStream.getCompressionProvider();
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    inStream = new ZIPCompressionInputStream( in, provider ) {
    };
    inStream.close();
  }

  @Test
  public void testRead() throws IOException {
    CompressionProvider provider = inStream.getCompressionProvider();
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    inStream = new ZIPCompressionInputStream( in, provider ) {
    };
    inStream.read( new byte[100], 0, inStream.available() );
  }
}
