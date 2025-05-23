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

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.value.ValueMetaPluginType;


import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class DatabaseConnectionManagerTest {

  private MemorySharedObjectsIO sharedObjectsIO;
  private DatabaseConnectionManager dbManager;

  @BeforeClass
  public static void setUpOnce() throws Exception {
    // Register Natives to create a default DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
    ValueMetaPluginType.getInstance().searchPlugins();
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    sharedObjectsIO = new MemorySharedObjectsIO();
    dbManager = new DatabaseConnectionManager( sharedObjectsIO );
  }

  @Test
  public void testAddOne() throws Exception {
    DatabaseMeta meta = createDatabase( "meta1" );
    dbManager.add( meta );

    Assert.assertNotNull( dbManager.get( "meta1" ) );
    Assert.assertNotNull( dbManager.get( "META1" ) );
  }

  @Test
  public void testEdit() throws Exception {
    DatabaseMeta meta = createDatabase( "meta1" );
    dbManager.add( meta );

    Assert.assertNotNull( dbManager.get( "meta1" ) );
    meta.setName( "meta2" );
    dbManager.add( meta );

    Assert.assertNotNull( dbManager.get( "meta1" ) );
    Assert.assertEquals( "meta1", dbManager.get( "meta1" ).getName() );
    Assert.assertNotNull( dbManager.get( "meta2" ) );
    Assert.assertEquals( "meta2", dbManager.get( "meta2" ).getName() );

    meta.setName( "META2" );
    dbManager.add( meta );
    Assert.assertNotNull( dbManager.get( "meta1" ) );
    Assert.assertEquals( "meta1", dbManager.get( "meta1" ).getName() );
    Assert.assertNotNull( dbManager.get( "meta2" ) );
    Assert.assertNotNull( dbManager.get( "META2" ) );
    Assert.assertEquals( "META2", dbManager.get( "meta2" ).getName() );
    Assert.assertEquals( 2, dbManager.getAll().size() );

    dbManager.remove( "META1" );
    Assert.assertEquals( 1, dbManager.getAll().size() );
  }

  protected DatabaseMeta createDatabase( String name ) {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    db.getDatabaseInterface().setDatabaseName( name );
    return db;
  }

}
