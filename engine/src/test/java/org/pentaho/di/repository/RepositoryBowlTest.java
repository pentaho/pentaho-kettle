package org.pentaho.di.repository;

import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsDetails;
import org.pentaho.di.connections.common.bucket.TestConnectionWithBucketsProvider;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.utils.EncryptUtils;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class RepositoryBowlTest {

  private static final String DESCRIPTION = "Connection Description";
  private static final String CONNECTION_NAME = "Connection Name";
  private static final String PASSWORD = "testpassword";
  private static final String PASSWORD2 = "testpassword2";
  private static final String ROLE1 = "role1";
  private static final String ROLE2 = "role2";

  @Test
  public void testChangeSubscriber() throws Exception {
    // metastore shared between bowls like they would while connected to a Repository.
    MemoryMetaStore metastore = new MemoryMetaStore();
    metastore.setName( "RepositoryBowlTest" );
    DefaultBowl mockDefaultBowl = spy( DefaultBowl.class );

    Repository mockRepository = mock( Repository.class );
    RepositoryBowl realRepositoryBowl = new RepositoryBowl( mockRepository );
    RepositoryBowl mockRepositoryBowl = spy( realRepositoryBowl );
    mockDefaultBowl.setMetastoreSupplier( () -> metastore );
    when( mockRepositoryBowl.getMetastore() ).thenReturn( metastore );

    try ( MockedStatic<DefaultBowl> defaultBowlMockedStatic = mockStatic( DefaultBowl.class );
          MockedStatic<EncryptUtils> encryptUtils = mockStatic( EncryptUtils.class ) ) {
      when( DefaultBowl.getInstance() ).thenReturn( mockDefaultBowl );
      ConnectionManager repoManager = mockRepositoryBowl.getManager( ConnectionManager.class );
      ConnectionManager defaultManager = mockDefaultBowl.getManager( ConnectionManager.class );

      addOne( repoManager );
      addProvider( defaultManager );

      //initialize caches
      ConnectionDetails repoRead = repoManager.getConnectionDetails( CONNECTION_NAME );
      ConnectionDetails defRead = defaultManager.getConnectionDetails( CONNECTION_NAME );
      assertNotNull( repoRead );
      assertNotNull( defRead );

      ConnectionDetails clone = repoRead.cloneDetails();
      clone.setDescription( "Not the original" );
      repoManager.save( clone );
      ConnectionDetails defRead2 = defaultManager.getConnectionDetails( CONNECTION_NAME );
      assertNotNull( defRead2 );
      assertEquals( clone.getDescription(), defRead2.getDescription() );
    }
  }


  private void addProvider( ConnectionManager connectionManager ) {
    connectionManager.addConnectionProvider( TestConnectionWithBucketsProvider.SCHEME,
      new TestConnectionWithBucketsProvider() );
  }

  private void addOne( ConnectionManager connectionManager ) {
    addProvider( connectionManager );
    TestConnectionWithBucketsDetails testConnectionDetails = new TestConnectionWithBucketsDetails();
    testConnectionDetails.setDescription( DESCRIPTION );
    testConnectionDetails.setName( CONNECTION_NAME );
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword1( PASSWORD2 );
    testConnectionDetails.getBaRoles().add( ROLE1 );
    testConnectionDetails.getBaRoles().add( ROLE2 );
    connectionManager.save( testConnectionDetails );
  }
}
