package org.pentaho.di.trans.steps.setsrs;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class SetSRSData extends BaseStepData implements StepDataInterface {
	private RowMetaInterface outputRowMeta;
	private RowSet rowset;
	
	public void setRowset(RowSet rowset) {
		this.rowset = rowset;
	}
	
	public RowSet getRowset() {
		return rowset;
	}
	
	public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
		this.outputRowMeta = outputRowMeta;
	}
	
	public RowMetaInterface getOutputRowMeta() {
		return outputRowMeta;
	}

}
