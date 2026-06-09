package org.pentaho.di.repository.pur;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.repository.StringObjectId;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PurRepositoryImporterTest {

  @Test
  public void equalsClusterSchemaUsesSlaveServerNameRatherThanObjectId() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );

    assertTrue( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaUsesSlaveServerNameWhenObjectIdIsNull() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.getSlaveServers().get( 0 ).setObjectId( null );

    assertTrue( importer.equalsClusterSchema( left, right ) );

    left.getSlaveServers().get( 0 ).setObjectId( null );

    assertTrue( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenSlaveNamesDiffer() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-b", "right-id" );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenNonNameFieldsDiffer() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.setBasePort( "45000" );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenSocketsBufferSizeDiffers() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.setSocketsBufferSize( "9000" );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenSocketsFlushIntervalDiffers() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.setSocketsFlushInterval( "3000" );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenSocketsCompressedDiffers() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.setSocketsCompressed( true );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  @Test
  public void equalsClusterSchemaReturnsFalseWhenDynamicDiffers() {
    TestablePurRepositoryImporter importer = new TestablePurRepositoryImporter( mock( PurRepository.class ) );

    ClusterSchema left = createClusterSchema( "cluster", "slave-a", "left-id" );
    ClusterSchema right = createClusterSchema( "cluster", "slave-a", "right-id" );
    right.setDynamic( true );

    assertFalse( importer.equalsClusterSchema( left, right ) );
  }

  private ClusterSchema createClusterSchema( String clusterName, String slaveName, String slaveId ) {
    SlaveServer slaveServer = new SlaveServer();
    slaveServer.setName( slaveName );
    slaveServer.setObjectId( new StringObjectId( slaveId ) );

    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.setName( clusterName );
    clusterSchema.setBasePort( "40000" );
    clusterSchema.setSocketsBufferSize( "5000" );
    clusterSchema.setSocketsFlushInterval( "1000" );
    clusterSchema.setSocketsCompressed( false );
    clusterSchema.setDynamic( false );
    clusterSchema.setSlaveServers( Collections.singletonList( slaveServer ) );
    return clusterSchema;
  }

  private static class TestablePurRepositoryImporter extends PurRepositoryImporter {

    TestablePurRepositoryImporter( PurRepository repository ) {
      super( repository );
    }

    boolean equalsClusterSchema( ClusterSchema left, ClusterSchema right ) {
      return equals( left, right );
    }
  }
}
