/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.namedconfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.namedconfig.model.Property;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.xml.XmlMetaStore;

public class NamedConfigurationTest {

  private IMetaStore metaStore = null;
  
  @Before
  public void before() throws IOException, MetaStoreException {
    File f = File.createTempFile( "NamedConfigurationTest", "before" );
    f.deleteOnExit();
    
    metaStore = new XmlMetaStore( f.getParent() );
  }

  @After
  public void after() throws IOException {
    File f = File.createTempFile( "NamedConfigurationTest", "after" );
    f.deleteOnExit();
    File metaStoreDir = new File( f.getParentFile().getAbsolutePath() + File.separator + "metastore" );
    FileUtils.deleteDirectory( metaStoreDir );
  }

  private NamedConfiguration createNamedConfiguration( String name ) {
    NamedConfiguration nc = new NamedConfiguration();
    nc.setName( name );
    return nc;
  }
  
  @Test
  public void testSetActiveShim() {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    
    String activeShimClass = "com.pentaho.test.ActiveShimClass";
    manager.setActiveShimClass( activeShimClass );
    assertEquals( activeShimClass, manager.getActiveShimClass() );
  }

  @Test
  public void testAddConfigurationTemplate() {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    
    NamedConfiguration nc = createNamedConfiguration( "" + System.currentTimeMillis() );
    manager.addConfigurationTemplate( nc );
    assertTrue( manager.getConfigurationTemplate( nc.getName() ) != null );
  }
  
  @Test
  public void testGetConfigurationTemplates() {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    
    // test lookup name that does not exist
    NamedConfiguration configuration = manager.getConfigurationTemplate( "does-not-exist" );
    assertTrue( configuration == null );
    
    // add config, test that we can look it up
    NamedConfiguration nc = createNamedConfiguration( "" + System.currentTimeMillis() );
    manager.addConfigurationTemplate( nc );
    assertTrue( manager.getConfigurationTemplate( nc.getName() ) != null );
  }

  @Test
  public void testGetConfigurationTemplatesByType() {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    
    NamedConfiguration nc = createNamedConfiguration( "" + System.currentTimeMillis() );
    nc.setType( "test" );
    manager.addConfigurationTemplate( nc );
    
    assertNotNull( manager.getConfigurationTemplates( "test" ) );
    assertEquals( 1, manager.getConfigurationTemplates( "test" ).size() );
    
    assertNotNull( manager.getConfigurationTemplates( "does-not-exist" ) );
    assertEquals( 0, manager.getConfigurationTemplates( "does-not-exist" ).size() );
  }
  
  @Test
  public void testEmptyStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    assertEquals( 0, manager.list( metaStore ).size() );
  }  

  @Test
  public void testLargeStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    int count = 100;
    for ( int i=0; i < count; i++ ) {
      manager.create( createNamedConfiguration( "config-" + i ), metaStore );
    }
    assertEquals( count, manager.list( metaStore ).size() );
  }  
  
  @Test
  public void testCreateUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    manager.create( createNamedConfiguration( "config-1" ), metaStore );
    assertNotNull( manager.read( "config-1", metaStore ) );
  }
  
  @Test
  public void testUpdateUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();
    NamedConfiguration nc = createNamedConfiguration( "config-1" );
    manager.create( nc, metaStore );
    assertNotNull( manager.read( "config-1", metaStore ) );
    nc.setSubType( "test-sub-type" );
    manager.update( nc, metaStore );
    nc = manager.read( nc.getName(), metaStore );
    assertNotNull( nc );
    assertEquals( "test-sub-type", nc.getSubType() );
  }
  
  @Test
  public void testDeleteUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.create( nc, metaStore );
    
    assertNotNull( manager.read( name, metaStore ) );
    manager.delete( name, metaStore );
    assertEquals( null, manager.read( name, metaStore ) );
  }
  
  @Test
  public void testListUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.create( nc, metaStore );

    List<NamedConfiguration> list = manager.list( metaStore );
    assertTrue( list.contains( nc ) );
    assertNotNull( list );
  }

  @Test
  public void testListNamesUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.create( nc, metaStore );

    List<String> list = manager.listNames( metaStore );
    assertTrue( list.contains( name ) );
    assertNotNull( list );
  }
  
  @Test
  public void testContainsUsingMetaStore() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.create( nc, metaStore );

    assertNotNull( manager.contains( name, metaStore ) );
  }
  
  @Test
  public void testTemplateIsCloned() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.addConfigurationTemplate( nc );
    
    NamedConfiguration clone = manager.getConfigurationTemplate( name );
    assertNotNull( clone );
    clone.setName( "test-changed" );
    assertNotSame( clone, nc.getName() );
    assertEquals( name, nc.getName() );
  }
  
  @Test
  public void testTemplatesListImmutable() throws MetaStoreException {
    INamedConfigurationManager manager = NamedConfigurationManager.getInstance();

    String name = "" + System.currentTimeMillis();
    NamedConfiguration nc = createNamedConfiguration( name );
    manager.addConfigurationTemplate( nc );
    
    List<NamedConfiguration> list = manager.getConfigurationTemplates();
    assertNotNull( list );
    int size = list.size();
    try {
      // should not be allowed
      list.add( createNamedConfiguration( System.currentTimeMillis() + "" ) );
    } catch ( UnsupportedOperationException ignored ) {
    }
    assertEquals( size, list.size() );
  }
  
  @Test
  public void testCanProvide() throws MetaStoreException {
    NamedConfiguration nc = createNamedConfiguration( "" + System.currentTimeMillis() );
    Property property1 = new Property();
    property1.setPropertyName( "property-name" );
    property1.setPropertyValue( "property-value" );
    property1.setDisplayName( "property-one" );
    nc.addProperty( "group-1", property1 );
    
    Map<String, String[]> required = new HashMap<String, String[]>();
    required.put( "group-1", new String[] { "property-name" } );
    assertTrue( nc.canProvide( required ) );
    required.put( "not-found", new String[] { "property-name" } );
    assertFalse( nc.canProvide( required ) );
    required.clear();
    required.put( "group-1", new String[] { "not-found" } );
    assertFalse( nc.canProvide( required ) );
  }
  
  @Test
  public void testHasValuesFor() throws MetaStoreException {
    NamedConfiguration nc = createNamedConfiguration( "" + System.currentTimeMillis() );
    Property property1 = new Property();
    property1.setPropertyName( "property-name-1" );
    property1.setPropertyValue( "property-value" );
    property1.setDisplayName( "property-one" );
    nc.addProperty( "group-1", property1 );
    Property property2 = new Property();
    property2.setPropertyName( "property-name-2" );
    property2.setDisplayName( "property-two" );
    nc.addProperty( "group-1", property2 );
    
    Map<String, String[]> required = new HashMap<String, String[]>();
    required.put( "group-1", new String[] { "property-name-1" } );
    assertTrue( nc.hasValuesFor( required ) );
    required.put( "not-found", new String[] { "property-name-1" } );
    assertFalse( nc.hasValuesFor( required ) );
    required.clear();
    required.put( "group-1", new String[] { "not-found" } );
    assertFalse( nc.hasValuesFor( required ) );
    required.clear();
    required.put( "group-1", new String[] { "property-name-2" } );
    // do not have values for
    assertFalse( nc.hasValuesFor( required ) );
    // but the config does have property..
    assertTrue( nc.canProvide( required ) );
  }  
  
  
}
