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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.plugins.PluginRegistry;

public class ZIPCompressionProviderTest {

  public static final String PROVIDER_NAME = "Zip";

  public CompressionProviderFactory factory = null;

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
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCtor() {
    ZIPCompressionProvider ncp = new ZIPCompressionProvider();
    assertNotNull( ncp );
  }

  @Test
  public void testGetName() {
    ZIPCompressionProvider provider = (ZIPCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    assertNotNull( provider );
    assertEquals( PROVIDER_NAME, provider.getName() );
  }

  @Test
  public void testGetProviderAttributes() {
    ZIPCompressionProvider provider = (ZIPCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    assertEquals( "ZIP compression", provider.getDescription() );
    assertTrue( provider.supportsInput() );
    assertTrue( provider.supportsOutput() );
    assertEquals( "zip", provider.getDefaultExtension() );
  }

  @Test
  public void testCreateInputStream() throws IOException {
    ZIPCompressionProvider provider = (ZIPCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    ZipInputStream zis = new ZipInputStream( in );
    ZIPCompressionInputStream inStream = new ZIPCompressionInputStream( in, provider );
    assertNotNull( inStream );
    ZIPCompressionInputStream ncis = provider.createInputStream( in );
    assertNotNull( ncis );
    ZIPCompressionInputStream ncis2 = provider.createInputStream( zis );
    assertNotNull( ncis2 );
  }

  @Test
  public void testCreateOutputStream() throws IOException {
    ZIPCompressionProvider provider = (ZIPCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream( out );
    ZIPCompressionOutputStream outStream = new ZIPCompressionOutputStream( out, provider );
    assertNotNull( outStream );
    ZIPCompressionOutputStream ncis = provider.createOutputStream( out );
    assertNotNull( ncis );
    ZIPCompressionOutputStream ncis2 = provider.createOutputStream( zos );
    assertNotNull( ncis2 );
  }
}
