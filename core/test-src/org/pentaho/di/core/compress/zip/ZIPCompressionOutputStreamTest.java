package org.pentaho.di.core.compress.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;

public class ZIPCompressionOutputStreamTest {

  public static final String PROVIDER_NAME = "Zip";

  public CompressionProviderFactory factory = null;
  public ZIPCompressionOutputStream outStream = null;

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
    ByteArrayOutputStream in = new ByteArrayOutputStream();
    outStream = new ZIPCompressionOutputStream( in, provider ) {
    };
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
    outStream = new ZIPCompressionOutputStream( out, provider ) {
    };
    outStream.close();
  }

  @Test
  public void testAddEntryAndWrite() throws IOException {
    CompressionProvider provider = outStream.getCompressionProvider();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    outStream = new ZIPCompressionOutputStream( out, provider ) {
    };
    outStream.addEntry( new ZipEntry( "./test.zip" ) );
    outStream.write( "Test".getBytes() );
  }
}
