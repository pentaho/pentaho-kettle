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


package org.pentaho.di.shared;

import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.xml.XMLHandler;

import java.util.Map;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DelegatingSharedObjectsIOTest {
  private static final String DB_TYPE = SharedObjectsIO.SharedObjectType.CONNECTION.getName();

  SharedObjectsIO primary;
  SharedObjectsIO secondary;

  @BeforeClass
  public static void setUpOnce() throws Exception {
    // Register Natives to create a default DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
    ValueMetaPluginType.getInstance().searchPlugins();
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() throws Exception {
    // these will be in-memory only.
    primary = new MemorySharedObjectsIO();
    secondary = new MemorySharedObjectsIO();
  }

  @Test
  public void testBasicCombos() throws Exception {
    primary.saveSharedObject( DB_TYPE, "a", toNode( DB_TYPE, "valuea" ) );
    primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
    secondary.saveSharedObject( DB_TYPE, "c", toNode( DB_TYPE, "valuec" ) );

    DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );

    Map<String, Node> combined = sharedIO.getSharedObjects( DB_TYPE );
    assertEquals( 3, combined.size() );
  }

  @Test
  public void testOverrides() throws Exception {
    primary.saveSharedObject( DB_TYPE, "a", toNode( DB_TYPE, "valuea" ) );
    primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
    secondary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valuec" ) );

    DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );

    Map<String, Node> combined = sharedIO.getSharedObjects( DB_TYPE );
    assertEquals( 2, combined.size() );

    Node nodeB = combined.get( "b" );
    assertNotNull( nodeB );
    String valueB = toValue( nodeB, DB_TYPE );
    assertEquals( "valueb", valueB );
  }

  @Test
  public void testSaveDisabled() throws Exception {
    try {
      primary.saveSharedObject( DB_TYPE, "a", toNode( DB_TYPE, "valuea" ) );
      primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
      secondary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valuec" ) );

      DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );
      sharedIO.saveSharedObject( DB_TYPE, "a", toNode( DB_TYPE, "valuea" ) );
    } catch ( UnsupportedOperationException ex ) {
      // expected
      return;
    }
  }

  @Test
  public void testDeleteDisabled() throws Exception {
    try {
      primary.saveSharedObject( DB_TYPE, "a", toNode( DB_TYPE, "valuea" ) );
      primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
      secondary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valuec" ) );

      DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );
      sharedIO.delete( DB_TYPE, "a" );
    } catch ( UnsupportedOperationException ex ) {
      // expected
      return;
    }
  }

  /**
   * Test to verify that getSharedObjects() returns the deduplicated list of items from
   * different SharedObjectsIO
   * @throws Exception
   */
  @Test
  public void testGetSharedObjectsDeduplicated() throws Exception {
    primary.saveSharedObject( DB_TYPE, "foo", toNode( DB_TYPE, "valuefoo" ) );
    primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
    secondary.saveSharedObject( DB_TYPE, "FOO", toNode( DB_TYPE, "valueFOO" ) );

    DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );
    // This  will return the list of sharedObjects from primary + any objects from secondary that do not
    // have the same name (case insensitive)
    Map<String, Node> combined = sharedIO.getSharedObjects( DB_TYPE );
    assertEquals( 2, combined.size() );

    Node nodeFoo = combined.get( "foo" );
    assertNotNull( nodeFoo );
    String valueFoo = toValue( nodeFoo, DB_TYPE );
    assertEquals( "valuefoo", valueFoo );
    // Node "FOO" should not be added to combined  map
    Node nodeFOO = combined.get( "FOO" );
    assertNull( nodeFOO );

  }

  @Test
  public void testGetSharedObjectCaseInsensitive() throws Exception {
    primary.saveSharedObject( DB_TYPE, "foo", toNode( DB_TYPE, "valuefoo" ) );
    primary.saveSharedObject( DB_TYPE, "b", toNode( DB_TYPE, "valueb" ) );
    secondary.saveSharedObject( DB_TYPE, "c", toNode( DB_TYPE, "valuec" ) );

    DelegatingSharedObjectsIO sharedIO = new DelegatingSharedObjectsIO( primary, secondary );
    // getSharedObject is case insensitive. Both "foo" and "FOO" can be used to retrieve the value "valuefoo"
    Node node = sharedIO.getSharedObject( DB_TYPE, "foo" );
    assertNotNull( node );

    String value = toValue( node, DB_TYPE );
    assertEquals( "valuefoo", value );
    // key "FOO" can also be used to access the same entry
    Node nodeFOO = sharedIO.getSharedObject( DB_TYPE, "FOO" );
    value = toValue( nodeFOO, DB_TYPE );
    assertEquals( "valuefoo", value );

  }

  private Node toNode( String type, String value ) throws Exception {
    String xml = "<" + type + "><key>" + value + "</key></" + type + ">";
    Document doc = XMLHandler.loadXMLString( xml );
    return XMLHandler.getSubNode( doc, type );
  }

  private String toValue( Node node, String type ) throws Exception {
    return XMLHandler.getTagValue( node, "key" );
  }
}

