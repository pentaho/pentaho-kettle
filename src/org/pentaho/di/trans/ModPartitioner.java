package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public class ModPartitioner extends BasePartitioner {

	public ModPartitioner( StepPartitioningMeta meta ) {
		super( meta );
	}

		public int getPartition(RowMetaInterface rowMeta, Object[] row ) throws KettleException
		{
			init(rowMeta);

            Long value = rowMeta.getInteger(row, partitionColumnIndex);
            
            int targetLocation = (int)(value.longValue() % nrPartitions);

			return targetLocation;
		}

	
}
