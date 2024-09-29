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
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.compress.NoneCompressionProvider.NoneCompressionInputStream;
import org.pentaho.di.core.compress.NoneCompressionProvider.NoneCompressionOutputStream;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class NoneCompressionProviderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  public static final String PROVIDER_NAME = "None";

  public CompressionProviderFactory factory = null;

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
