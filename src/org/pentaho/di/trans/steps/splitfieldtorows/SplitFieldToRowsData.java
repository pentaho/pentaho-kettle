package org.pentaho.di.trans.steps.splitfieldtorows;


import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class SplitFieldToRowsData extends BaseStepData implements StepDataInterface
{
	public int fieldnr;
	public RowMetaInterface outputRowMeta;
	public ValueMetaInterface splitMeta;
	public SplitFieldToRowsData()
	{
		super();
	}

}
