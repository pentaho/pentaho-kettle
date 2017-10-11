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

package org.pentaho.di.core.compress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.NoneCompressionProvider.NoneCompressionInputStream;
import org.pentaho.di.core.compress.NoneCompressionProvider.NoneCompressionOutputStream;
import org.pentaho.di.core.plugins.PluginRegistry;

public class NoneCompressionProviderTest {

  public static final String PROVIDER_NAME = "None";

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
    NoneCompressionProvider ncp = new NoneCompressionProvider();
    assertNotNull( ncp );
  }

  @Test
  public void testGetName() {
    NoneCompressionProvider provider = (NoneCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    assertNotNull( provider );
    assertEquals( PROVIDER_NAME, provider.getName() );
  }

  @Test
  public void testGetProviderAttributes() {
    NoneCompressionProvider provider = (NoneCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    assertEquals( "No compression", provider.getDescription() );
    assertTrue( provider.supportsInput() );
    assertTrue( provider.supportsOutput() );
    assertNull( provider.getDefaultExtension() );
  }

  @Test
  public void testCreateInputStream() throws IOException {
    NoneCompressionProvider provider = (NoneCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayInputStream in = new ByteArrayInputStream( "Test".getBytes() );
    NoneCompressionInputStream inStream = new NoneCompressionInputStream( in, provider );
    assertNotNull( inStream );
    NoneCompressionInputStream ncis = (NoneCompressionInputStream) provider.createInputStream( in );
    assertNotNull( ncis );
  }

  @Test
  public void testCreateOutputStream() throws IOException {
    NoneCompressionProvider provider = (NoneCompressionProvider) factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    NoneCompressionOutputStream outStream = new NoneCompressionOutputStream( out, provider );
    assertNotNull( outStream );
    NoneCompressionOutputStream ncis = (NoneCompressionOutputStream) provider.createOutputStream( out );
    assertNotNull( ncis );
  }
}
