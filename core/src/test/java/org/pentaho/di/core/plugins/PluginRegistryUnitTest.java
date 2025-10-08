/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.plugins;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginClassMapException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.extension.PluginMockInterface;
import org.pentaho.di.core.logging.LoggingPluginType;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PluginRegistryUnitTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void getGetPluginInformation() throws KettlePluginException {
    PluginRegistry.getInstance().reset();
    RowBuffer result = PluginRegistry.getInstance().getPluginInformation( BasePluginType.class );
    assertNotNull( result );
    assertEquals( 8, result.getRowMeta().size() );

    for ( ValueMetaInterface vmi : result.getRowMeta().getValueMetaList() ) {
      assertEquals( ValueMetaInterface.TYPE_STRING, vmi.getType() );
    }
  }

  /**
   * Test that additional plugin mappings can be added via the PluginRegistry.
   */
  @Test
  public void testSupplementalPluginMappings() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface mockPlugin = mock( PluginInterface.class );
    when( mockPlugin.getIds() ).thenReturn( new String[] { "mockPlugin"} );
    when( mockPlugin.matches( "mockPlugin" ) ).thenReturn( true );
    when( mockPlugin.getName() ).thenReturn( "mockPlugin" );
    doReturn( LoggingPluginType.class ).when( mockPlugin ).getPluginType();
    registry.registerPlugin( LoggingPluginType.class, mockPlugin );

    registry.addClassFactory( LoggingPluginType.class, String.class, "mockPlugin", () -> "Foo" );
    String result = registry.loadClass( LoggingPluginType.class, "mockPlugin", String.class );
    assertEquals( "Foo", result );
    assertEquals( 2, registry.getPlugins( LoggingPluginType.class ).size() );

    // Now add another mapping and verify that it works and the existing supplementalPlugin was reused.
    UUID uuid = UUID.randomUUID();
    registry.addClassFactory( LoggingPluginType.class, UUID.class, "mockPlugin", () -> uuid );
    UUID out = registry.loadClass( LoggingPluginType.class, "mockPlugin", UUID.class );
    assertEquals( uuid, out );
    assertEquals( 2, registry.getPlugins( LoggingPluginType.class ).size() );
  }

  /**
   * Test that several plugin jar can share the same classloader.
   */
  @Test
  public void testPluginClassloaderGroup() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface mockPlugin1 = mock( PluginInterface.class );
    when( mockPlugin1.getIds() ).thenReturn( new String[] { "mockPlugin"} );
    when( mockPlugin1.matches( "mockPlugin" ) ).thenReturn( true );
    when( mockPlugin1.getName() ).thenReturn( "mockPlugin" );
    when( mockPlugin1.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( PluginTypeInterface.class, String.class.getName() );
      }} );
    when( mockPlugin1.getClassLoaderGroup() ).thenReturn( "groupPlugin" );
    doReturn( BasePluginType.class ).when( mockPlugin1 ).getPluginType();

    PluginInterface mockPlugin2 = mock( PluginInterface.class );
    when( mockPlugin2.getIds() ).thenReturn( new String[] { "mockPlugin2"} );
    when( mockPlugin2.matches( "mockPlugin2" ) ).thenReturn( true );
    when( mockPlugin2.getName() ).thenReturn( "mockPlugin2" );
    when( mockPlugin2.getClassMap() ).thenReturn( new HashMap<Class<?>, String>() {{
        put( PluginTypeInterface.class, Integer.class.getName() );
      }} );
    when( mockPlugin2.getClassLoaderGroup() ).thenReturn( "groupPlugin" );
    doReturn( BasePluginType.class ).when( mockPlugin2 ).getPluginType();

    registry.registerPlugin( BasePluginType.class, mockPlugin1 );
    registry.registerPlugin( BasePluginType.class, mockPlugin2 );

    // test they share the same classloader
    ClassLoader ucl = registry.getClassLoader( mockPlugin1 );
    assertEquals( ucl, registry.getClassLoader( mockPlugin2 ) );

    // test removing a shared plugin creates a new classloader
    registry.removePlugin( BasePluginType.class, mockPlugin2 );
    assertNotEquals( ucl, registry.getClassLoader( mockPlugin1 ) );
  }

  @Test( expected = KettlePluginClassMapException.class )
  public void testClassloadingPluginNoClassRegistered() throws KettlePluginException {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginMockInterface plugin = mock( PluginMockInterface.class );
    when( plugin.loadClass( any() ) ).thenReturn( null );
    registry.loadClass( plugin, Class.class );
  }

  @Test
  public void testMergingPluginFragment() throws KettlePluginException {
    // setup
    // initialize Fragment Type
    PluginRegistry registry = PluginRegistry.getInstance();
    BaseFragmentType fragmentType = new BaseFragmentType( Annotation.class, "", "", ValueMetaPluginType.class ) {
      @Override protected void initListeners( Class<? extends PluginTypeInterface> aClass,
                                              Class<? extends PluginTypeInterface> typeToTrack ) {
        super.initListeners( BaseFragmentType.class, typeToTrack );
      }
      @Override protected String extractID( Annotation annotation ) {
        return null;
      }
      @Override protected String extractImageFile( Annotation annotation ) {
        return null;
      }
      @Override protected String extractDocumentationUrl( Annotation annotation ) {
        return null;
      }
      @Override protected String extractCasesUrl( Annotation annotation ) {
        return null;
      }
      @Override protected String extractForumUrl( Annotation annotation ) {
        return null;
      }
      @Override protected String extractSuggestion( Annotation annotation ) {
        return null;
      }
    };
    assertTrue( fragmentType.isFragment() );

    PluginInterface plugin = mock( PluginInterface.class );
    when( plugin.getIds() ).thenReturn( new String[] { "mock" } );
    when( plugin.matches( any() ) ).thenReturn( true );
    doReturn( ValueMetaPluginType.class ).when( plugin ).getPluginType();
    doAnswer( invocationOnMock -> null ).when( plugin ).merge( any( PluginInterface.class ) );

    PluginInterface fragment = mock( PluginInterface.class );
    when( fragment.getIds() ).thenReturn( new String[] { "mock" } );
    when( fragment.matches( any() ) ).thenReturn( true );
    doReturn( BaseFragmentType.class ).when( fragment ).getPluginType();
    doAnswer( invocationOnMock -> null ).when( fragment ).merge( any( PluginInterface.class ) );

    // test
    registry.registerPlugin( ValueMetaPluginType.class, plugin );
    verify( plugin, atLeastOnce() ).merge( any() );

    registry.registerPlugin( BaseFragmentType.class, fragment );
    verify( fragment, never() ).merge( any() );
    verify( plugin, atLeast( 2 ) ).merge( any() );

    // verify that the order doesn't influence
    registry.removePlugin( ValueMetaPluginType.class, plugin );
    registry.registerPlugin( ValueMetaPluginType.class, plugin );
    verify( plugin, atLeast( 3 ) ).merge( any() );

    // verify plugin changes
    registry.registerPlugin( ValueMetaPluginType.class, plugin );
    verify( plugin, atLeast( 4 ) ).merge( any() );
  }
  
  @Test
  public void testCategoryMap() throws KettlePluginException {
    
    PluginRegistry registry = PluginRegistry.getInstance();
    
    PluginInterface plugin = mock( PluginInterface.class );
    when( plugin.getIds() ).thenReturn( new String[] { "mock" } );
    when( plugin.getCategory() ).thenReturn( "mockCategory" );
    when( plugin.matches( any() ) ).thenReturn( true );
    doReturn( ValueMetaPluginType.class ).when( plugin ).getPluginType();

    //Register
    registry.registerPlugin( ValueMetaPluginType.class, plugin );
    
    //Test compute
    assertEquals( 1, registry.getCategories( ValueMetaPluginType.class ).size() );
    
    //Test recall cache
    assertEquals( 1, registry.getCategories( ValueMetaPluginType.class ).size() );
    
    //Test cache clear
    registry.removePlugin( ValueMetaPluginType.class, plugin );
    assertEquals( 0, registry.getCategories( ValueMetaPluginType.class ).size() );
    
    //Test re-cache
    registry.registerPlugin( ValueMetaPluginType.class, plugin );
    assertEquals( 1, registry.getCategories( ValueMetaPluginType.class ).size() );
  }
}
