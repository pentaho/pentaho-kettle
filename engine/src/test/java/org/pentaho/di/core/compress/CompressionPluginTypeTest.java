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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompressionPluginTypeTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetInstance() {
    CompressionPluginType instance = CompressionPluginType.getInstance();
    CompressionPluginType instance2 = CompressionPluginType.getInstance();
    assertTrue( instance == instance2 );
    assertNotNull( instance );
    CompressionPluginType.pluginType = null;
    CompressionPluginType instance3 = CompressionPluginType.getInstance();
    assertFalse( instance == instance3 );
  }

  @Test
  public void testGetPluginInfo() {
    CompressionPluginType instance = CompressionPluginType.getInstance();
    Annotation a = new FakePlugin().getClass().getAnnotation( CompressionPlugin.class );
    assertNotNull( a );
    assertEquals( "", instance.extractCategory( a ) );
    assertEquals( "Fake", instance.extractID( a ) );
    assertEquals( "FakePlugin", instance.extractName( a ) );
    assertEquals( "", instance.extractCasesUrl( a ) );
    assertEquals( "Compression Plugin", instance.extractDesc( a ) );
    assertEquals( "", instance.extractDocumentationUrl( a ) );
    assertEquals( "", instance.extractForumUrl( a ) );
    assertEquals( "", instance.extractI18nPackageName( a ) );
    assertNull( instance.extractImageFile( a ) );
    assertFalse( instance.extractSeparateClassLoader( a ) );
  }

  @CompressionPlugin( id = "Fake", name = "FakePlugin" )
  private class FakePlugin {
  }
}
