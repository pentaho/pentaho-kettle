package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

public class RowAdapter implements RowListener {

	public RowAdapter() {
	}
	
	public void errorRowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

	public void rowReadEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

	public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
	}

}
