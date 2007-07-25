package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public class NoPartitioner extends BasePartitioner {

	public NoPartitioner( StepPartitioningMeta meta ) {
		super( meta );
	}

		public int getPartition(RowMetaInterface rowMeta, Object[] row ) throws KettleException
		{
			return 0;
		}

	
}
