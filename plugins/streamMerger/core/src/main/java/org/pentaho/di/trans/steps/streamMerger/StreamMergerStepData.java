package org.pentaho.di.trans.steps.streamMerger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class StreamMergerStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta, inRowMeta; 

	public StreamMergerStepData()
	{
		super();
	}


	public HashMap<String,ValueMetaInterface> leadingFields;
	public HashMap<String,ValueMetaInterface> additionalFields;
	public ArrayList<String> leadingFieldsIndex;
	public ArrayList<String> additionalFieldsIndex;
	
	RowMetaInterface outputMeta;
	
	
	public ArrayList<ValueMetaInterface> resultFields;
	public ArrayList<String> resultFieldsIndex;
	public ArrayList<Integer> resultTypesIndex;
	
	public HashMap<String,String[]> sourceMapping;
	public HashMap<String,Integer[]> sourceMappingType;
	public HashMap<String,int[]> targetMapping;
	
	public List<StreamInterface> infoStreams;
	
}
	
