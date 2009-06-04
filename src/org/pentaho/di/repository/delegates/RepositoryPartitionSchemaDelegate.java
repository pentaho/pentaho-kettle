package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;

public class RepositoryPartitionSchemaDelegate extends BaseRepositoryDelegate {

//	private static Class<?> PKG = PartitionSchema.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryPartitionSchemaDelegate(Repository repository) {
		super(repository);
	}

    public RowMetaAndData getPartitionSchema(long id_partition_schema) throws KettleException
    {
        return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_PARTITION_SCHEMA), quote(Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), id_partition_schema);
    }
    
    public RowMetaAndData getPartition(long id_partition) throws KettleException
    {
        return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_PARTITION), quote(Repository.FIELD_PARTITION_ID_PARTITION), id_partition);
    }
    
	public synchronized long getPartitionSchemaID(String name) throws KettleException
    {
        return repository.connectionDelegate.getIDWithValue(quoteTable(Repository.TABLE_R_PARTITION_SCHEMA), quote(Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA), quote(Repository.FIELD_PARTITION_SCHEMA_NAME), name);
    }

	public void savePartitionSchema(PartitionSchema partitionSchema) throws KettleException
	{
		savePartitionSchema(partitionSchema, -1L, false);
	}

	public void savePartitionSchema(PartitionSchema partitionSchema, long id_transformation, boolean isUsedByTransformation) throws KettleException
	{
		// see if this partitioning schema is already in the repository...
		partitionSchema.setId( getPartitionSchemaID(partitionSchema.getName()) );
		if (partitionSchema.getId()<0)
		{
			partitionSchema.setId(insertPartitionSchema(partitionSchema));
		}
		else
		{
			updatePartitionSchema(partitionSchema);
			repository.delPartitions(partitionSchema.getId());
		}
        
		// Save the cluster-partition relationships
		//
		for (int i=0;i<partitionSchema.getPartitionIDs().size();i++)
		{
			insertPartition(partitionSchema.getId(), partitionSchema.getPartitionIDs().get(i));
		}
        
		// Save a link to the transformation to keep track of the use of this partition schema
		// Otherwise, we shouldn't bother with this
		//
		if (isUsedByTransformation)
		{
			repository.insertTransformationPartitionSchema(id_transformation, partitionSchema.getId());
		}
	}
    
	public PartitionSchema loadPartitionSchema(long id_partition_schema) throws KettleException
	{
		PartitionSchema partitionSchema = new PartitionSchema();
        
		partitionSchema.setId(id_partition_schema);
        
		RowMetaAndData row = getPartitionSchema(id_partition_schema);
        
		partitionSchema.setName( row.getString("NAME", null) );
        
		long[] pids = repository.getPartitionIDs(id_partition_schema);
		for (int i=0;i<pids.length;i++)
		{
			partitionSchema.getPartitionIDs().add( getPartition(pids[i]).getString("PARTITION_ID", null) );
		}
        
		partitionSchema.setDynamicallyDefined( row.getBoolean("DYNAMIC_DEFINITION", false) );
		partitionSchema.setNumberOfPartitionsPerSlave( row.getString("PARTITIONS_PER_SLAVE", null) );
		
		return partitionSchema;
	}


    public synchronized long insertPartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        long id = repository.connectionDelegate.getNextPartitionSchemaID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING), partitionSchema.getName());
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN), partitionSchema.isDynamicallyDefined());
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING), partitionSchema.getNumberOfPartitionsPerSlave());

        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), Repository.TABLE_R_PARTITION_SCHEMA);
        repository.connectionDelegate.getDatabase().setValuesInsert(table);
        repository.connectionDelegate.getDatabase().insertRow();
        repository.connectionDelegate.getDatabase().closeInsert();

        return id;
    }
    
    public synchronized void updatePartitionSchema(PartitionSchema partitionSchema) throws KettleException
    {
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING), partitionSchema.getName());
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN), partitionSchema.isDynamicallyDefined());
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING), partitionSchema.getNumberOfPartitionsPerSlave());
        
        repository.connectionDelegate.updateTableRow(Repository.TABLE_R_PARTITION_SCHEMA, Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, table, partitionSchema.getId());
    }

    public synchronized long insertPartition(long id_partition_schema, String partition_id) throws KettleException
    {
        long id = repository.connectionDelegate.getNextPartitionID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_ID_PARTITION, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER), new Long(id_partition_schema));
        table.addValue(new ValueMeta(Repository.FIELD_PARTITION_PARTITION_ID, ValueMetaInterface.TYPE_STRING), partition_id);

        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), Repository.TABLE_R_PARTITION);
        repository.connectionDelegate.getDatabase().setValuesInsert(table);
        repository.connectionDelegate.getDatabase().insertRow();
        repository.connectionDelegate.getDatabase().closeInsert();

        return id;
    }

    public synchronized void delPartitionSchema(long id_partition_schema) throws KettleException
    {
        // First, see if the schema is still used by other objects...
        // If so, generate an error!!
        //
        // We look in table R_TRANS_PARTITION_SCHEMA to see if there are any transformations using this schema.
        String[] transList = repository.getTransformationsUsingPartitionSchema(id_partition_schema);

        if (transList.length==0)
        {
            repository.connectionDelegate.getDatabase().execStatement("DELETE FROM "+quoteTable(Repository.TABLE_R_PARTITION)+" WHERE "+quote(Repository.FIELD_PARTITION_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
            repository.connectionDelegate.getDatabase().execStatement("DELETE FROM "+quoteTable(Repository.TABLE_R_PARTITION_SCHEMA)+" WHERE "+quote(Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA)+" = " + id_partition_schema);
        }
        else
        {
            StringBuffer message = new StringBuffer();
            
            message.append("The partition schema is used by the following transformations:").append(Const.CR);
            for (int i = 0; i < transList.length; i++)
            {
                message.append("  ").append(transList[i]).append(Const.CR);
            }
            message.append(Const.CR);
            
            KettleDependencyException e = new KettleDependencyException(message.toString());
            throw new KettleDependencyException("This partition schema is still in use by one or more transformations ("+transList.length+") :", e);
        }
    }
    

}
