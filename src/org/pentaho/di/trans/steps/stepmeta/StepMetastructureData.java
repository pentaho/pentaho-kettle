package org.pentaho.di.trans.steps.stepmeta;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class StepMetastructureData extends BaseStepData implements StepDataInterface{
	
	public RowMetaInterface outputRowMeta;   
	public RowMetaInterface inputRowMeta; 
	
	public int rowCount;
	
	/**
	 * Default constructor.
	 */
	public StepMetastructureData()
	{
		super();
	}
}
