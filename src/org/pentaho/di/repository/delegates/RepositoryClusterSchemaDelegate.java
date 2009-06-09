package org.pentaho.di.repository.delegates;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.KettleDatabaseRepository;

public class RepositoryClusterSchemaDelegate extends BaseRepositoryDelegate {

//	private static Class<?> PKG = ClusterSchema.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryClusterSchemaDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
    public RowMetaAndData getClusterSchema(long id_cluster_schema) throws KettleException
    {
        return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER), quote(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER), id_cluster_schema);
    }
	
    public synchronized long getClusterID(String name) throws KettleException
    {
        return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER), quote(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER), quote(KettleDatabaseRepository.FIELD_CLUSTER_NAME), name);
    }

    public ClusterSchema loadClusterSchema(long id_cluster_schema, List<SlaveServer> slaveServers) throws KettleException
    {
    	ClusterSchema clusterSchema = new ClusterSchema();
        RowMetaAndData row = getClusterSchema(id_cluster_schema);
            
        clusterSchema.setName( row.getString(KettleDatabaseRepository.FIELD_CLUSTER_NAME, null) );
        clusterSchema.setBasePort( row.getString(KettleDatabaseRepository.FIELD_CLUSTER_BASE_PORT, null) );
        clusterSchema.setSocketsBufferSize( row.getString(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_BUFFER_SIZE, null) );
        clusterSchema.setSocketsFlushInterval( row.getString(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL, null) );
        clusterSchema.setSocketsCompressed( row.getBoolean(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_COMPRESSED, true) );
        clusterSchema.setDynamic( row.getBoolean(KettleDatabaseRepository.FIELD_CLUSTER_DYNAMIC, true) );
            
        long[] pids = repository.getSlaveIDs(id_cluster_schema);
        for (int i=0;i<pids.length;i++)
        {
            SlaveServer slaveServer = repository.loadSlaveServer(pids[i]);
            SlaveServer reference = SlaveServer.findSlaveServer(slaveServers, slaveServer.getName());
            if (reference!=null) 
                clusterSchema.getSlaveServers().add(reference);
            else 
                clusterSchema.getSlaveServers().add(slaveServer);
        }
        
        return clusterSchema;
    }

    public void saveClusterSchema(ClusterSchema clusterSchema) throws KettleException
    {
        saveClusterSchema(clusterSchema, -1L, false);
    }

    public void saveClusterSchema(ClusterSchema clusterSchema, long id_transformation, boolean isUsedByTransformation) throws KettleException
    {
        clusterSchema.setID(getClusterID(clusterSchema.getName()));
        if (clusterSchema.getID()<0)
        {
            // Save the cluster
        	clusterSchema.setID(insertCluster(clusterSchema));
        }
        else
        {
            repository.delClusterSlaves(clusterSchema.getID());
        }
        
        // Also save the used slave server references.
        for (int i=0;i<clusterSchema.getSlaveServers().size();i++)
        {
            SlaveServer slaveServer = clusterSchema.getSlaveServers().get(i);
            if (slaveServer.getID()<0) // oops, not yet saved!
            {
            	repository.saveSlaveServer(slaveServer, id_transformation, isUsedByTransformation);
            }
            repository.insertClusterSlave(clusterSchema, slaveServer);
        }
        
        // Save a link to the transformation to keep track of the use of this partition schema
        // Only save it if it's really used by the transformation
        if (isUsedByTransformation)
        {
            repository.insertTransformationCluster(id_transformation, clusterSchema.getID());
        }
    }

    private synchronized long insertCluster(ClusterSchema clusterSchema) throws KettleException
    {
        long id = repository.connectionDelegate.getNextClusterID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_NAME, ValueMetaInterface.TYPE_STRING), clusterSchema.getName());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_BASE_PORT, ValueMetaInterface.TYPE_STRING), clusterSchema.getBasePort());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_BUFFER_SIZE, ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsBufferSize());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL, ValueMetaInterface.TYPE_STRING), clusterSchema.getSocketsFlushInterval());
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_SOCKETS_COMPRESSED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(clusterSchema.isSocketsCompressed()));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_CLUSTER_DYNAMIC, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(clusterSchema.isDynamic()));

        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_CLUSTER);
        repository.connectionDelegate.getDatabase().setValuesInsert(table);
        repository.connectionDelegate.getDatabase().insertRow();
        repository.connectionDelegate.getDatabase().closeInsert();

        return id;
    }

    public synchronized void delClusterSchema(long id_cluster) throws KettleException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_CLUSTER to see if there are any transformations using this schema.
        String[] transList = repository.getTransformationsUsingCluster(id_cluster);

        if (transList.length==0)
        {
            repository.connectionDelegate.getDatabase().execStatement("DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_CLUSTER)+" WHERE "+quote(KettleDatabaseRepository.FIELD_CLUSTER_ID_CLUSTER)+" = " + id_cluster);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The cluster schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This cluster schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }


}
