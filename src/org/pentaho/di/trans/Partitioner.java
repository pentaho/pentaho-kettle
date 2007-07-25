package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;

public interface Partitioner {

	public int getPartition(RowMetaInterface rowMeta, Object[] r ) throws KettleException;
	
}
