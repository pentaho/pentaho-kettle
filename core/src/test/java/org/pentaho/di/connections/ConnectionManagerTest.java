/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections;

import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsProvider;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelperTest;
import org.pentaho.di.connections.vfs.VFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.metastore.util.PentahoDefaults.NAMESPACE;

/**
 * Created by bmorrise on 3/10/19.
 * <p>
 * See also {@link VFSConnectionManagerHelperTest} for tests regarding VFS-specific functionality
 * exposed by the connection manager.
 */
public class ConnectionManagerTest {

  private static final String EXAMPLE = "example";
  private static final String DOES_NOT_EXIST = "Does not exist";
  private static final String DESCRIPTION = "Connection Description";
  private static final String CONNECTION_NAME = "Connection Name";
  private static final String PASSWORD = "testpassword";
  private static final String PASSWORD2 = "testpassword2";
  private static final String ROLE1 = "role1";
  private static final String ROLE2 = "role2";

  private VFSConnectionManagerHelper vfsConnectionManagerHelper;
  private ConnectionManager connectionManager;

  private Bowl bowl;

  private MemoryMetaStore memoryMetaStore;


  @BeforeClass
  public static void setupClass() throws Exception {
    KettleClientEnvironment.init();
    DefaultBowl defaultBowl = DefaultBowl.getInstance();
    assertNotNull( defaultBowl );

    // Make sure that the default bowl has a meta-store, as this is a requirement of the connection manager,
    // and otherwise would affect the default connection manager.
    IMetaStore defaultMemoryMetaStore = new MemoryMetaStore();
    defaultBowl.setMetastoreSupplier( () -> defaultMemoryMetaStore );
  }

  @Before
  public void setup() throws Exception {
    bowl = mock( Bowl.class );
    vfsConnectionManagerHelper = mock( VFSConnectionManagerHelper.class );
    memoryMetaStore = new MemoryMetaStore();

    connectionManager = new ConnectionManager( () -> memoryMetaStore, bowl, vfsConnectionManagerHelper );
  }

  // region Construction and Singleton
  @Test
  public void testGetInstanceHasDefaultBowlAndMetaStoreAndUsesDefaultConnectionHelper() {
    DefaultBowl defaultBowl = DefaultBowl.getInstance();
    assertNotNull( defaultBowl );

    VFSConnectionManagerHelper defaultVfsConnectionManagerHelper = VFSConnectionManagerHelper.getInstance();
    assertNotNull( defaultVfsConnectionManagerHelper );

    ConnectionManager defaultConnectionManager = ConnectionManager.getInstance();

    assertNotNull( defaultConnectionManager );

    assertSame( defaultBowl, defaultConnectionManager.getBowl() );

    // Unfortunately, it's not currently possible to test the metastore of the connection manager in unit testing.
    // The problem is:
    // 1. In the unit test environment, the DefaultBowl metastore is null, and needs to be explicitly initialized
    //    by tests.
    // 2. When the static ConnectionManager class is initialized, or first call to ConnectionManager.getInstance() is
    //    made, the default bowl's metastore is still null.
    //
    // Testing the metastore value would fail unpredictably, depending on the order tests run.
    // assertEquals( DefaultBowl.getInstance().getMetastore(), defaultConnectionManager.getMetastoreSupplier().get() );
    // assertNotNull( defaultConnectionManager.getMetastoreSupplier().get() );

    assertSame( defaultVfsConnectionManagerHelper, defaultConnectionManager.getVfsConnectionManagerHelper() );
  }

  @Test
  public void testGetInstanceOfBowlRespectsGivenBowlAndMetaStoreAndUsesDefaultConnectionHelper() {
    IMetaStore metaStore = mock( IMetaStore.class );
    Bowl bowl = mock( Bowl.class );

    VFSConnectionManagerHelper defaultVfsConnectionManagerHelper = VFSConnectionManagerHelper.getInstance();
    assertNotNull( defaultVfsConnectionManagerHelper );

    ConnectionManager adhocConnectionManager = ConnectionManager.getInstance( () -> metaStore, bowl );

    assertSame( bowl, adhocConnectionManager.getBowl() );
    assertSame( metaStore, adhocConnectionManager.getMetastoreSupplier().get() );
    assertSame( defaultVfsConnectionManagerHelper, adhocConnectionManager.getVfsConnectionManagerHelper() );
  }
  // endregion

  @Test
  public void testAddConnectionProvider() {
    addProvider();

    TestConnectionWithBucketsProvider testConnectionProvider1 =
      (TestConnectionWithBucketsProvider) connectionManager.getConnectionProvider( TestConnectionWithBucketsProvider.SCHEME );

    assertNotNull( testConnectionProvider1 );
  }

  @Test
  public void testSaveConnection() {
    addOne();

    TestConnectionWithBucketsDetails testConnectionDetails1 =
      (TestConnectionWithBucketsDetails) connectionManager
        .getConnectionDetails( TestConnectionWithBucketsProvider.SCHEME, CONNECTION_NAME );
    assertEquals( CONNECTION_NAME, testConnectionDetails1.getName() );
  }

  @Test
  public void testEncryptedField() throws Exception {
    addOne();

    TestConnectionWithBucketsDetails testConnectionDetails1 =
      (TestConnectionWithBucketsDetails) connectionManager
        .getConnectionDetails( TestConnectionWithBucketsProvider.SCHEME, CONNECTION_NAME );
    assertEquals( PASSWORD, testConnectionDetails1.getPassword() );
    assertEquals( PASSWORD2, testConnectionDetails1.getPassword1() );

    MetaStoreFactory<TestConnectionWithBucketsDetails> metaStoreFactory =
      new MetaStoreFactory<>( TestConnectionWithBucketsDetails.class, memoryMetaStore, NAMESPACE );
    TestConnectionWithBucketsDetails testConnectionDetails = metaStoreFactory.loadElement( CONNECTION_NAME );
    Assert.assertTrue( testConnectionDetails.getPassword().startsWith( "Encrypted " ) );
    Assert.assertTrue( testConnectionDetails.getPassword1().startsWith( "Encrypted " ) );
  }

  @Test
  public void testSaveConnectionError() {
    assertFalse( connectionManager.save( new BadConnectionDetails() ) );
  }

  @Test
  public void testLookupFilter() {
    VFSLookupFilter vfsLookupFilter = new VFSLookupFilter();
    vfsLookupFilter.addKeyLookup( EXAMPLE, TestConnectionWithBucketsProvider.SCHEME );
    connectionManager.addLookupFilter( vfsLookupFilter );

    assertEquals( TestConnectionWithBucketsProvider.SCHEME, connectionManager.getLookupKey( EXAMPLE ) );
  }

  @Test
  public void testLookupFilterEmpty() {
    assertEquals( EXAMPLE, connectionManager.getLookupKey( EXAMPLE ) );
  }

  @Test
  public void testGetConnectionDetailsNull() {
    Assert.assertNull( connectionManager.getConnectionDetails( DOES_NOT_EXIST ) );
  }

  @Test
  public void testDelete() {
    addOne();
    connectionManager.delete( CONNECTION_NAME );

    Assert.assertNull( connectionManager.getConnectionDetails( TestConnectionWithBucketsProvider.SCHEME, CONNECTION_NAME ) );
  }

  @Test
  public void testGetProviders() {
    addProvider();

    assertEquals( 1, connectionManager.getProviders().size() );
    assertEquals( TestConnectionWithBucketsProvider.SCHEME, connectionManager.getProviders().get( 0 ).getKey() );
  }

  @Test
  public void testGetProvidersByType() {
    addProvider();

    assertEquals( 1, connectionManager.getProviders().size() );
    assertEquals( TestConnectionWithBucketsProvider.SCHEME,
      connectionManager.getProvidersByType( TestConnectionWithBucketsProvider.class ).get( 0 ).getKey() );
  }

  @Test
  public void testGetNames() {
    addOne();

    List<String> names = connectionManager.getNames();
    assertEquals( 1, names.size() );
    assertEquals( CONNECTION_NAME, names.get( 0 ) );
  }

  @Test
  public void testGetNamesEmpty() {
    addProvider();

    List<String> names = connectionManager.getNames();
    assertEquals( 0, names.size() );
  }

  @Test
  public void testExists() {
    addOne();

    assertTrue( connectionManager.exists( CONNECTION_NAME ) );
  }

  @Test
  public void testGetNamesByType() {
    addOne();

    List<String> names = connectionManager.getNamesByType( TestConnectionWithBucketsProvider.class );
    assertEquals( 1, names.size() );
    assertEquals( CONNECTION_NAME, names.get( 0 ) );
  }

  @Test
  public void testGetNamesByTypeEmpty() {
    addProvider();

    List<String> names = connectionManager.getNamesByType( ConnectionProvider.class );
    assertEquals( 0, names.size() );
  }

  @Test
  public void testGetNamesByKeyEmpty() {
    addProvider();

    List<String> names = connectionManager.getNamesByKey( "not there" );
    assertEquals( 0, names.size() );
  }

  @Test
  public void testCreateConnectionDetails() {
    addProvider();
    assertNotNull( connectionManager.createConnectionDetails( TestConnectionWithBucketsProvider.SCHEME ) );
  }

  @Test
  public void testGetConnectionDetailsByScheme() {
    addOne();
    assertEquals( 1, connectionManager.getConnectionDetailsByScheme( TestConnectionWithBucketsProvider.SCHEME ).size() );
  }

  @Test
  public void testGetItems() {
    addOne();

    List<ConnectionManager.Type> types = connectionManager.getItems();
    assertEquals( 1, types.size() );
  }

  @Test
  public void testCreateConnectionDetailsNull() {
    addProvider();
    Assert.assertNull( connectionManager.createConnectionDetails( DOES_NOT_EXIST ) );
  }

  @Test
  public void testGetConnectionDetailsBySchemeEmpty() {
    addOne();
    assertEquals( 0, connectionManager.getConnectionDetailsByScheme( DOES_NOT_EXIST ).size() );
  }

  @Test
  public void testNullConnectionName() throws Exception {
    FileSystemOptions fileSystemOptions =
      VFSHelper.getOpts( DefaultBowl.getInstance(), "file://fakefile.ktr", null, null );
    Assert.assertNull( fileSystemOptions );
  }

  @Test
  public void testBaRolesNotNull() {
    addOne();
    TestConnectionWithBucketsDetails connectionDetails =
      (TestConnectionWithBucketsDetails) connectionManager.getConnectionDetails( CONNECTION_NAME );
    assertNotNull( connectionDetails );
    assertNotNull( connectionDetails.getBaRoles() );
  }

  @Test
  public void testDefaultPropertiesNotNull() {
    addOne();
    TestConnectionWithBucketsDetails connectionDetails =
      (TestConnectionWithBucketsDetails) connectionManager.getConnectionDetails( CONNECTION_NAME );
    assertNotNull( connectionDetails );
    assertNotNull( connectionDetails.getProperties() );
    assertNotNull( connectionDetails.getProperties().get( "baRoles" ) );
  }

  // region test
  @Test
  public void testTestVFSConnectionIsDynamicallyDelegatedToVFSConnectionManagerHelperWithNullOptions()
    throws KettleException {
    @SuppressWarnings( "unchecked" )
    VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
      (VFSConnectionProvider<VFSConnectionDetails>) mock( VFSConnectionProvider.class );

    VFSConnectionDetails vfsConnectionDetails = mock( VFSConnectionDetails.class );

    connectionManager.addConnectionProvider( "test", vfsConnectionProvider );
    when( vfsConnectionDetails.getType() ).thenReturn( "test" );

    when( vfsConnectionManagerHelper.test( connectionManager, vfsConnectionDetails, null ) )
      .thenReturn( true );

    boolean result = connectionManager.test( vfsConnectionDetails );

    assertTrue( result );

    verify( vfsConnectionManagerHelper, times( 1 ) )
      .test( connectionManager, vfsConnectionDetails, null );
  }
  // endregion

  private void addProvider() {
    connectionManager.addConnectionProvider( TestConnectionWithBucketsProvider.SCHEME, new TestConnectionWithBucketsProvider() );
  }

  private void addOne() {
    addProvider();
    TestConnectionWithBucketsDetails testConnectionDetails = new TestConnectionWithBucketsDetails();
    testConnectionDetails.setDescription( DESCRIPTION );
    testConnectionDetails.setName( CONNECTION_NAME );
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword1( PASSWORD2 );
    testConnectionDetails.getBaRoles().add( ROLE1 );
    testConnectionDetails.getBaRoles().add( ROLE2 );
    connectionManager.save( testConnectionDetails );
  }

  public static class BadConnectionDetails implements ConnectionDetails {
    @Override public String getName() {
      return null;
    }

    @Override public void setName( String name ) {

    }

    @Override public String getType() {
      return null;
    }

    @Override public String getDescription() {
      return null;
    }

    @Override public VariableSpace getSpace() {
      return null;
    }

    @Override public void setSpace( VariableSpace space ) {

    }
  }
}
