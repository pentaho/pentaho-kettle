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

  private Node toNode( String type, String value ) throws Exception {
    String xml = "<" + type + "><key>" + value + "</key></" + type + ">";
    Document doc = XMLHandler.loadXMLString( xml );
    return XMLHandler.getSubNode( doc, type );
  }

  private String toValue( Node node, String type ) throws Exception {
    return XMLHandler.getTagValue( node, "key" );
  }
}

