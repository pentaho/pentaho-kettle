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
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.metastore.persist.MetaStoreElementType;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

/**
 * Created by bmorrise on 3/10/19.
 */
public class ConnectionManagerTest {

  public static final String EXAMPLE = "example";
  public static final String DOES_NOT_EXIST = "Does not exist";
  private static String CONNECTION_NAME = "Connection Name";

  private ConnectionManager connectionManager;

  private MemoryMetaStore memoryMetaStore = new MemoryMetaStore();

  @Before
  public void setup() {
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
    testConnectionDetails.setName( CONNECTION_NAME );
    connectionManager.save( testConnectionDetails );
  }

  @MetaStoreElementType(
    name = "Test VFS Connection",
    description = "Defines the connection details for a test vfs connection" )
  public static class TestConnectionDetails implements ConnectionDetails {

    private static String TYPE = "test";

    private String name;
    private String description;

    @Override public String getName() {
      return name;
    }

    @Override public void setName( String name ) {
      this.name = name;
    }

    @Override public String getType() {
      return TYPE;
    }

    @Override public String getDescription() {
      return description;
    }

    public void setDescription( String description ) {
      this.description = description;
    }
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

  public static class TestConnectionProvider implements ConnectionProvider<TestConnectionDetails> {

    private ConnectionManager connectionManager;

    public TestConnectionProvider( ConnectionManager connectionManager ) {
      this.connectionManager = connectionManager;
    }

    public static final String NAME = "Test";
    public static final String SCHEME = "test";

    @Override public String getName() {
      return NAME;
    }

    @Override public String getKey() {
      return SCHEME;
    }

    @Override public Class<TestConnectionDetails> getClassType() {
      return TestConnectionDetails.class;
    }

    @Override public List<String> getNames() {
      return connectionManager.getNamesByType( getClass() );
    }

    @SuppressWarnings( "unchecked" )
    @Override public List<TestConnectionDetails> getConnectionDetails() {
      return (List<TestConnectionDetails>) connectionManager.getConnectionDetailsByScheme( getKey() );
    }

    @Override public boolean test( TestConnectionDetails connectionDetails ) {
      return true;
    }

    @Override public TestConnectionDetails prepare( TestConnectionDetails connectionDetails ) {
      return connectionDetails;
    }
  }


}
