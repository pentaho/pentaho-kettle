/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
    dbManager.addDatabase( meta );

    Assert.assertNotNull( dbManager.getDatabase( "meta1" ) );
  }

  @Test
  public void testEdit() throws Exception {
    DatabaseMeta meta = createDatabase( "meta1" );
    dbManager.addDatabase( meta );

    Assert.assertNotNull( dbManager.getDatabase( "meta1" ) );
    meta.setName( "meta2" );
    dbManager.addDatabase( meta );

    Assert.assertNotNull( dbManager.getDatabase( "meta1" ) );
    Assert.assertEquals( "meta1", dbManager.getDatabase( "meta1" ).getName() );
    Assert.assertNotNull( dbManager.getDatabase( "meta2" ) );
    Assert.assertEquals( "meta2", dbManager.getDatabase( "meta2" ).getName() );
  }

  protected DatabaseMeta createDatabase( String name ) {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    db.getDatabaseInterface().setDatabaseName( name );
    return db;
  }

}
