package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

public interface KettleRepositoryInterface {
	
	// Loading methods for shared objects : Slave Server, Cluster Schema, Database Connection
	
	public DatabaseMeta loadDatabaseMeta(long id_database) throws KettleException;

	public ClusterSchema loadClusterSchema(long id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException;
    
}
