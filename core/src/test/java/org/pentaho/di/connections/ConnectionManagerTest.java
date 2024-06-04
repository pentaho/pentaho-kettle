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
import org.pentaho.di.connections.common.bucket.TestConnectionDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionProvider;
import org.pentaho.di.connections.utils.VFSConnectionTestOptions;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.metastore.util.PentahoDefaults.NAMESPACE;

/**
 * Created by bmorrise on 3/10/19.
 */
public class ConnectionManagerTest {

  public static final String EXAMPLE = "example";
  public static final String DOES_NOT_EXIST = "Does not exist";
  private static String DESCRIPTION = "Connection Description";
  private static String CONNECTION_NAME = "Connection Name";
  private static String PASSWORD = "testpassword";
  private static String PASSWORD2 = "testpassword2";
  private static String ROLE1 = "role1";
  private static String ROLE2 = "role2";

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
    memoryMetaStore = new MemoryMetaStore();
    connectionManager = new ConnectionManager( () -> memoryMetaStore, bowl );
  }

  @Test
  public void testGetInstanceHasDefaultBowlAndMetaStore() {
    DefaultBowl defaultBowl = DefaultBowl.getInstance();
    assertNotNull( defaultBowl );

    ConnectionManager defaultConnectionManager = ConnectionManager.getInstance();
    assertNotNull( defaultConnectionManager );

    assertSame( defaultBowl, defaultConnectionManager.getBowl() );

    // Testing that the value is DefaultBowl.getInstance().getMetastore() wouldn't work due to tests running in
    // parallel. The actual meta-store captured by the default connection manager instance can vary due to multiple
    // threads calling the @BeforeClass method.
    assertNotNull( defaultConnectionManager.getMetastoreSupplier().get() );
  }

  @Test
  public void testGetInstanceOfBowlRespectsGivenBowlAndMetaStore() {
    IMetaStore metaStore = mock( IMetaStore.class );
    Bowl bowl = mock( Bowl.class );

    ConnectionManager adhocConnectionManager = ConnectionManager.getInstance( () -> metaStore, bowl );

    assertSame( bowl, adhocConnectionManager.getBowl() );
    assertSame( metaStore, adhocConnectionManager.getMetastoreSupplier().get() );
  }

  @Test
  public void testAddConnectionProvider() {
    addProvider();

    TestConnectionProvider testConnectionProvider1 =
      (TestConnectionProvider) connectionManager.getConnectionProvider( TestConnectionProvider.SCHEME );

    assertNotNull( testConnectionProvider1 );
  }

  @Test
  public void testSaveConnection() {
    addOne();

    TestConnectionDetails testConnectionDetails1 =
      (TestConnectionDetails) connectionManager
        .getConnectionDetails( TestConnectionProvider.SCHEME, CONNECTION_NAME );
    Assert.assertEquals( CONNECTION_NAME, testConnectionDetails1.getName() );
  }

  @Test
  public void testEncryptedField() throws Exception {
    addOne();

    TestConnectionDetails testConnectionDetails1 =
      (TestConnectionDetails) connectionManager
        .getConnectionDetails( TestConnectionProvider.SCHEME, CONNECTION_NAME );
    Assert.assertEquals( PASSWORD, testConnectionDetails1.getPassword() );
    Assert.assertEquals( PASSWORD2, testConnectionDetails1.getPassword1() );

    MetaStoreFactory<TestConnectionDetails> metaStoreFactory =
      new MetaStoreFactory<>( TestConnectionDetails.class, memoryMetaStore, NAMESPACE );
    TestConnectionDetails testConnectionDetails = metaStoreFactory.loadElement( CONNECTION_NAME );
    Assert.assertTrue( testConnectionDetails.getPassword().startsWith( "Encrypted " ) );
    Assert.assertTrue( testConnectionDetails.getPassword1().startsWith( "Encrypted " ) );
  }

  @Test
  public void testSaveConnectionError() {
    Assert.assertEquals( false, connectionManager.save( new BadConnectionDetails() ) );
  }

  @Test
  public void testLookupFilter() {
    VFSLookupFilter vfsLookupFilter = new VFSLookupFilter();
    vfsLookupFilter.addKeyLookup( EXAMPLE, TestConnectionProvider.SCHEME );
    connectionManager.addLookupFilter( vfsLookupFilter );

    Assert.assertEquals( TestConnectionProvider.SCHEME, connectionManager.getLookupKey( EXAMPLE ) );
  }

  @Test
  public void testLookupFilterEmpty() {
    Assert.assertEquals( EXAMPLE, connectionManager.getLookupKey( EXAMPLE ) );
  }

  @Test
  public void testGetConnectionDetailsNull() {
    Assert.assertNull( connectionManager.getConnectionDetails( DOES_NOT_EXIST ) );
  }

  @Test
  public void testDelete() {
    addOne();
    connectionManager.delete( CONNECTION_NAME );

    Assert.assertNull( connectionManager.getConnectionDetails( TestConnectionProvider.SCHEME, CONNECTION_NAME ) );
  }

  @Test
  public void testGetProviders() {
    addProvider();

    Assert.assertEquals( 1, connectionManager.getProviders().size() );
    Assert.assertEquals( TestConnectionProvider.SCHEME, connectionManager.getProviders().get( 0 ).getKey() );
  }

  @Test
  public void testGetProvidersByType() {
    addProvider();

    Assert.assertEquals( 1, connectionManager.getProviders().size() );
    Assert.assertEquals( TestConnectionProvider.SCHEME,
      connectionManager.getProvidersByType( TestConnectionProvider.class ).get( 0 ).getKey() );
  }

  @Test
  public void testGetNames() {
    addOne();

    List<String> names = connectionManager.getNames();
    Assert.assertEquals( 1, names.size() );
    Assert.assertEquals( CONNECTION_NAME, names.get( 0 ) );
  }

  @Test
  public void testGetNamesEmpty() {
    addProvider();

    List<String> names = connectionManager.getNames();
    Assert.assertEquals( 0, names.size() );
  }

  @Test
  public void testExists() {
    addOne();

    Assert.assertEquals( true, connectionManager.exists( CONNECTION_NAME ) );
  }

  @Test
  public void testGetNamesByType() {
    addOne();

    List<String> names = connectionManager.getNamesByType( TestConnectionProvider.class );
    Assert.assertEquals( 1, names.size() );
    Assert.assertEquals( CONNECTION_NAME, names.get( 0 ) );
  }

  @Test
  public void testGetNamesByTypeEmpty() {
    addProvider();

    List<String> names = connectionManager.getNamesByType( ConnectionProvider.class );
    Assert.assertEquals( 0, names.size() );
  }

  @Test
  public void testGetNamesByKeyEmpty() {
    addProvider();

    List<String> names = connectionManager.getNamesByKey( "not there" );
    Assert.assertEquals( 0, names.size() );
  }

  @Test
  public void testCreateConnectionDetails() {
    addProvider();
    assertNotNull( connectionManager.createConnectionDetails( TestConnectionProvider.SCHEME ) );
  }

  @Test
  public void testGetConnectionDetailsByScheme() {
    addOne();
    Assert.assertEquals( 1, connectionManager.getConnectionDetailsByScheme( TestConnectionProvider.SCHEME ).size() );
  }

  @Test
  public void testGetItems() {
    addOne();

    List<ConnectionManager.Type> types = connectionManager.getItems();
    Assert.assertEquals( 1, types.size() );
  }

  @Test
  public void testCreateConnectionDetailsNull() {
    addProvider();
    Assert.assertNull( connectionManager.createConnectionDetails( DOES_NOT_EXIST ) );
  }

  @Test
  public void testGetConnectionDetailsBySchemeEmpty() {
    addOne();
    Assert.assertEquals( 0, connectionManager.getConnectionDetailsByScheme( DOES_NOT_EXIST ).size() );
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
    TestConnectionDetails connectionDetails =
      (TestConnectionDetails) connectionManager.getConnectionDetails( CONNECTION_NAME );
    assertNotNull( connectionDetails );
    assertNotNull( connectionDetails.getBaRoles() );
  }

  @Test
  public void testDefaultPropertiesNotNull() {
    addOne();
    TestConnectionDetails connectionDetails =
      (TestConnectionDetails) connectionManager.getConnectionDetails( CONNECTION_NAME );
    assertNotNull( connectionDetails );
    assertNotNull( connectionDetails.getProperties() );
    assertNotNull( connectionDetails.getProperties().get( "baRoles" ) );
  }

  @Test
  public void testConnection() throws KettleException {

    VFSConnectionTestOptions vfsConnectionTestOptions = new VFSConnectionTestOptions( true );
    VFSConnectionProvider vfsConnectionProvider = mock( VFSConnectionProvider.class );
    VFSConnectionDetails vfsConnectionDetails = mock( VFSConnectionDetails.class );

    connectionManager.addConnectionProvider( "test", vfsConnectionProvider );
    when( vfsConnectionDetails.getType() ).thenReturn( "test" );

    connectionManager.test( vfsConnectionDetails, vfsConnectionTestOptions );
    verify( vfsConnectionProvider, times( 1 ) ).test( vfsConnectionDetails, vfsConnectionTestOptions );
  }

  @Test
  public void testConnectionWithEmptyVFSTestOptions() throws KettleException {
    VFSConnectionProvider vfsConnectionProvider = mock( VFSConnectionProvider.class );
    VFSConnectionDetails vfsConnectionDetails = mock( VFSConnectionDetails.class );

    connectionManager.addConnectionProvider( "test", vfsConnectionProvider );
    when( vfsConnectionDetails.getType() ).thenReturn( "test" );
    when( vfsConnectionProvider.test( vfsConnectionDetails ) ).thenReturn( true );
    connectionManager.test( vfsConnectionDetails, null );
    verify( vfsConnectionProvider, times( 1 ) ).test( eq( vfsConnectionDetails ),
      any( VFSConnectionTestOptions.class ) );
  }

  private void addProvider() {
    TestConnectionProvider testConnectionProvider = new TestConnectionProvider( connectionManager );
    connectionManager.addConnectionProvider( TestConnectionProvider.SCHEME, testConnectionProvider );
  }

  private void addOne() {
    addProvider();
    TestConnectionDetails testConnectionDetails = new TestConnectionDetails();
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
