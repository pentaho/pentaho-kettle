/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Test;
import org.pentaho.di.connections.common.bucket.TestConnectionDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionProvider;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

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

  private ConnectionManager connectionManager;

  private MemoryMetaStore memoryMetaStore = new MemoryMetaStore();

  @Before
  public void setup() throws Exception {
    KettleClientEnvironment.init();
    connectionManager = new ConnectionManager();
    connectionManager.setMetastoreSupplier( () -> memoryMetaStore );
  }

  @Test
  public void testAddConnectionProvider() {
    addProvider();

    TestConnectionProvider testConnectionProvider1 =
      (TestConnectionProvider) connectionManager.getConnectionProvider( TestConnectionProvider.SCHEME );

    Assert.assertNotNull( testConnectionProvider1 );
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
  public void testCreateConnectionDetails() {
    addProvider();
    Assert.assertNotNull( connectionManager.createConnectionDetails( TestConnectionProvider.SCHEME ) );
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
  public void testNullConnectionName() {
    FileSystemOptions fileSystemOptions = VFSHelper.getOpts( "file://fakefile.ktr", null );
    Assert.assertNull( fileSystemOptions );
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
  }


}
