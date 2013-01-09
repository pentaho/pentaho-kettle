package org.pentaho.di.trans.steps.edi2xml;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class Edi2XmlData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public RowMetaInterface inputRowMeta;
	
	public int inputFieldIndex = -1;
	public int outputFieldIndex = -1;
	public ValueMetaInterface inputMeta;
	public ValueMetaInterface outputMeta;
	
    public Edi2XmlData()
	{
		super();
	}
}
	
