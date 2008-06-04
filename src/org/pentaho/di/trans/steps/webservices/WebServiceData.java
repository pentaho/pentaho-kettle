package org.pentaho.di.trans.steps.webservices;

import java.util.Map;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class WebServiceData extends BaseStepData implements StepDataInterface 
{

	public RowMetaInterface outputRowMeta;
	
	public Map<String,Integer> indexMap;
	
}
