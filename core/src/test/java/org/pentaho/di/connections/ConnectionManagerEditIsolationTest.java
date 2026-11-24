/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.connections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsProvider;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConnectionManagerEditIsolationTest {
  private ConnectionManager manager;

  @BeforeClass
  public static void initializeKettle() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    Bowl bowl = mock( Bowl.class );
    MemoryMetaStore metaStore = new MemoryMetaStore();
    manager = new ConnectionManager( () -> metaStore, bowl, new VFSConnectionManagerHelper() );
    manager.addConnectionProvider( TestConnectionWithBucketsProvider.SCHEME,
      new TestConnectionWithBucketsProvider() );
    TestConnectionWithBucketsDetails details = new TestConnectionWithBucketsDetails();
    details.setName( "original" );
    details.setPassword( "original-token" );
    assertTrue( manager.save( details ) );
  }

  @Test
  public void pdi21074CancelDiscardsUnsavedCredentialEdit() {
    TestConnectionWithBucketsDetails edit = manager.getDetails( "original" );
    edit.setPassword( "wrong-token" );

    TestConnectionWithBucketsDetails original = manager.getDetails( "original" );
    assertEquals( "original-token", original.getPassword() );
  }

  @Test
  public void pdi20299CancelDiscardsUnsavedRenameFromSchemeLookup() {
    TestConnectionWithBucketsDetails edit = (TestConnectionWithBucketsDetails)
      manager.getConnectionDetails( TestConnectionWithBucketsProvider.SCHEME, "original" );
    edit.setName( "cancelled-rename" );

    TestConnectionWithBucketsDetails original = manager.getDetails( "original" );
    assertNotNull( original );
    assertEquals( "original", original.getName() );
    assertEquals( "original-token", original.getPassword() );
  }

  @Test
  public void pdi21074SavingRenamedConnectionDoesNotCorruptOriginal() {
    TestConnectionWithBucketsDetails edit = manager.getDetails( "original" );
    edit.setName( "renamed" );
    assertTrue( manager.save( edit ) );

    TestConnectionWithBucketsDetails original = manager.getDetails( "original" );
    assertNotNull( original );
    assertEquals( "original", original.getName() );
    assertEquals( "original-token", original.getPassword() );
    TestConnectionWithBucketsDetails renamed = manager.getDetails( "renamed" );
    assertEquals( "renamed", renamed.getName() );
    assertEquals( "original-token", renamed.getPassword() );
  }
}
