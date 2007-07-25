package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public abstract class BasePartitioner implements Partitioner {

	protected int partitionColumnIndex = -1;
	protected StepPartitioningMeta meta;
	protected int nrPartitions = -1;
	
	public BasePartitioner( StepPartitioningMeta meta ) {
		this.meta = meta;
	}

	public int getNrPartitions() {
		return nrPartitions;
	}

	public void setNrPartitions(int nrPartitions) {
		this.nrPartitions = nrPartitions;
	}

	public void init(RowMetaInterface rowMeta ) throws KettleException
	{

        if (partitionColumnIndex < 0)
        {
            partitionColumnIndex = rowMeta.indexOfValue(meta.getFieldName());
            if (partitionColumnIndex < 0) { 
            	throw new KettleStepException("Unable to find partitioning field name [" + meta.getFieldName() + "] in the output row..." + rowMeta); 
            }
        }
        if( nrPartitions < 0 ) {
        	nrPartitions = meta.getPartitionSchema().getPartitionIDs().size();
        }

	}
}
