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

import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.compress.gzip.GZIPCompressionProvider;
import org.pentaho.di.core.compress.hadoopsnappy.HadoopSnappyCompressionProvider;
import org.pentaho.di.core.compress.snappy.SnappyCompressionProvider;
import org.pentaho.di.core.compress.zip.ZIPCompressionProvider;
import org.pentaho.di.core.plugins.PluginRegistry;

public class CompressionProviderFactoryTest {

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
  public void testGetInstance() {
    assertNotNull( factory );
  }

  @Test
  public void testCreateCoreProviders() {
    CompressionProvider provider = factory.createCompressionProviderInstance( "None" );
    assertNotNull( provider );
    assertTrue( provider.getClass().isAssignableFrom( NoneCompressionProvider.class ) );
    assertEquals( "None", provider.getName() );
    assertEquals( "No compression", provider.getDescription() );

    provider = factory.createCompressionProviderInstance( "Zip" );
    assertNotNull( provider );
    assertTrue( provider.getClass().isAssignableFrom( ZIPCompressionProvider.class ) );
    assertEquals( "Zip", provider.getName() );
    assertEquals( "ZIP compression", provider.getDescription() );

    provider = factory.createCompressionProviderInstance( "GZip" );
    assertNotNull( provider );
    assertTrue( provider.getClass().isAssignableFrom( GZIPCompressionProvider.class ) );
    assertEquals( "GZip", provider.getName() );
    assertEquals( "GZIP compression", provider.getDescription() );

    provider = factory.createCompressionProviderInstance( "Snappy" );
    assertNotNull( provider );
    assertTrue( provider.getClass().isAssignableFrom( SnappyCompressionProvider.class ) );
    assertEquals( "Snappy", provider.getName() );
    assertEquals( "Snappy compression", provider.getDescription() );

    provider = factory.createCompressionProviderInstance( "Hadoop-snappy" );
    assertNotNull( provider );
    assertTrue( provider.getClass().isAssignableFrom( HadoopSnappyCompressionProvider.class ) );
    assertEquals( "Hadoop-snappy", provider.getName() );
    assertEquals( "Hadoop Snappy compression", provider.getDescription() );
  }

  /**
   * Test that all core compression plugins' expected names (None, Zip, GZip) are available via the factory
   */
  @Test
  public void getCoreProviderNames() {
    @SuppressWarnings( "serial" )
    final HashMap<String, Boolean> foundProvider = new HashMap<String, Boolean>() {
      {
        put( "None", false );
        put( "Zip", false );
        put( "GZip", false );
        put( "Snappy", false );
        put( "Hadoop-snappy", false );
      }
    };

    String[] providers = factory.getCompressionProviderNames();
    assertNotNull( providers );
    for ( String provider : providers ) {
      assertNotNull( foundProvider.get( provider ) );
      foundProvider.put( provider, true );
    }

    boolean foundAllProviders = true;
    for ( Boolean b : foundProvider.values() ) {
      foundAllProviders = foundAllProviders && b;
    }

    assertTrue( foundAllProviders );
  }

  /**
   * Test that all core compression plugins (None, Zip, GZip) are available via the factory
   */
  @Test
  public void getCoreProviders() {
    @SuppressWarnings( "serial" )
    final HashMap<String, Boolean> foundProvider = new HashMap<String, Boolean>() {
      {
        put( "None", false );
        put( "Zip", false );
        put( "GZip", false );
        put( "Snappy", false );
        put( "Hadoop-snappy", false );
      }
    };

    Collection<CompressionProvider> providers = factory.getCompressionProviders();
    assertNotNull( providers );
    for ( CompressionProvider provider : providers ) {
      assertNotNull( foundProvider.get( provider.getName() ) );
      foundProvider.put( provider.getName(), true );
    }

    boolean foundAllProviders = true;
    for ( Boolean b : foundProvider.values() ) {
      foundAllProviders = foundAllProviders && b;
    }

    assertTrue( foundAllProviders );
  }

  @Test
  public void getNonExistentProvider() {
    CompressionProvider provider = factory.createCompressionProviderInstance( "Fake" );
    assertNull( provider );
    provider = factory.getCompressionProviderByName( null );
    assertNull( provider );
    provider = factory.getCompressionProviderByName( "Invalid" );
    assertNull( provider );
  }
}
