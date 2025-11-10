/*
 * ! ******************************************************************************
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

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MemorySharedObjectsIOTest {

  private MemorySharedObjectsIO sharedObjectsIO;
  private String db_type;

  @BeforeClass
  public static void setUpOnce() throws KettlePluginException, KettleException {
    // Register Natives to create a default DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
    ValueMetaPluginType.getInstance().searchPlugins();
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() throws Exception {
    db_type = String.valueOf( SharedObjectsIO.SharedObjectType.CONNECTION );
    sharedObjectsIO = new MemorySharedObjectsIO();

  }

  @Test
  public void testSaveSharedObject() throws Exception {
    try {
      sharedObjectsIO.lock();

      String connectionName = "NewConn";
      // Create a new DatabaseMeta object
      DatabaseMeta dbMeta = createDatabaseMeta( connectionName );
      sharedObjectsIO.saveSharedObject( db_type, dbMeta.getName(), dbMeta.toNode() );

      // Create another connection with same name different case
      dbMeta = createDatabaseMeta( "NEWCONN" );
      sharedObjectsIO.saveSharedObject( db_type, dbMeta.getName(), dbMeta.toNode() );

      Map<String, Node> nodeMap = sharedObjectsIO.getSharedObjects( db_type );
      assertEquals( 1, nodeMap.size() );

      Node node = sharedObjectsIO.getSharedObject( db_type, dbMeta.getName() );
      assertEquals( "NEWCONN", XMLHandler.getTagValue( node, "name" ) );
    } finally {
      sharedObjectsIO.unlock();
    }
  }

  @Test
  public void testGetSharedObjectCaseInsensitive() throws Exception {
    try {
      sharedObjectsIO.lock();
      String connectionName = "NewConn";
      // Create a new DatabaseMeta object
      DatabaseMeta dbMeta = createDatabaseMeta( connectionName );
      sharedObjectsIO.saveSharedObject( db_type, dbMeta.getName(), dbMeta.toNode() );

      Node node = sharedObjectsIO.getSharedObject( db_type, "NEWCONN" );
      assertEquals( connectionName, XMLHandler.getTagValue( node, "name" ) );
    } finally {
      sharedObjectsIO.unlock();
    }
  }

  private DatabaseMeta createDatabaseMeta( String name ) {
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setName( name );
    return dbMeta;
  }

}
