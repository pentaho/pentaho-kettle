package org.pentaho.di.trans.steps.streamMerger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class StreamMergerStep extends BaseStep implements StepInterface {


	public StreamMergerStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
        dis.setSafeModeEnabled(false);
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		StreamMergerStepMeta meta = (StreamMergerStepMeta) smi;
		StreamMergerStepData data = (StreamMergerStepData) sdi;

        data.leadingFields    = new HashMap<String,ValueMetaInterface>();
        data.additionalFields = new HashMap<String,ValueMetaInterface>();        
        data.sourceMapping    = new HashMap<String,String[]>();
        data.sourceMappingType    = new HashMap<String,Integer[]>();
        
        data.leadingFieldsIndex = new ArrayList<String>();
        data.additionalFieldsIndex = new ArrayList<String>();
        data.resultFieldsIndex = new ArrayList<String>();
        data.resultTypesIndex = new ArrayList<Integer>();

        data.infoStreams = meta.getStepIOMeta().getInfoStreams();
  
        boolean superResult = super.init(meta, data);
        log.logBasic("Finished initialization");
        
		return superResult;
	}


	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		StreamMergerStepMeta meta = (StreamMergerStepMeta) smi;
		StreamMergerStepData data = (StreamMergerStepData) sdi;
		
		if (first) {
			first = false;
			
			log.logBasic("Get field data types to create outputMeta");
			data.outputMeta = new RowMeta();
			
            for (int i = 0; i < data.infoStreams.size(); i++) {
            	List<ValueMetaInterface> listVmi = getTransMeta().getStepFields(data.infoStreams.get(i).getStepMeta()).getValueMetaList();
            	
            	String[] fields = new String[listVmi.size()];
            	Integer[] types = new Integer[listVmi.size()];
            	
            	int fieldCount = 0;
				for (ValueMetaInterface vmi : listVmi) {
					
					fields[fieldCount] = vmi.getName();
					types[fieldCount] = vmi.getType();
					fieldCount++;
					
					
					if(meta.getStepsToMerge()[0].equals(data.infoStreams.get(i).getStepname()) && !data.leadingFields.containsKey(vmi.getName())){
						data.leadingFields.put(vmi.getName(),vmi);
						data.leadingFieldsIndex.add(vmi.getName());
					}
						
					
					if (!data.leadingFields.containsKey(vmi.getName()) && !data.additionalFields.containsKey(vmi.getName())) {
						data.additionalFields.put(vmi.getName(),vmi);
						data.additionalFieldsIndex.add(vmi.getName());
					}
				}
				
				data.sourceMapping.put(data.infoStreams.get(i).getStepname(), fields);
				data.sourceMappingType.put(data.infoStreams.get(i).getStepname(), types);
            }
            
            data.resultFields = new ArrayList<ValueMetaInterface>();
            for(int x = 0 ; x < data.leadingFieldsIndex.size(); x++){
            	data.resultFields.add(data.leadingFields.get(data.leadingFieldsIndex.get(x)));
            	data.outputMeta.addValueMeta(data.leadingFields.get(data.leadingFieldsIndex.get(x)));
            	data.resultFieldsIndex.add(data.leadingFieldsIndex.get(x));
            	data.resultTypesIndex.add(data.leadingFields.get(data.leadingFieldsIndex.get(x)).getType());
            	
            }
            
            int alreadyKnownFields = data.leadingFieldsIndex.size();
            
            for(int x = alreadyKnownFields; x < data.additionalFieldsIndex.size() + alreadyKnownFields; x++){
            	data.resultFields.add(data.additionalFields.get(data.additionalFieldsIndex.get(x-alreadyKnownFields)));
            	data.outputMeta.addValueMeta(data.additionalFields.get(data.additionalFieldsIndex.get(x-alreadyKnownFields)));
            	data.resultFieldsIndex.add(data.additionalFieldsIndex.get(x-alreadyKnownFields));
            	data.resultTypesIndex.add(data.additionalFields.get(data.additionalFieldsIndex.get(x-alreadyKnownFields)).getType());
            }
            
            	
            data.targetMapping = new HashMap<>();
        	for(String stream : data.sourceMapping.keySet()){
        		int[] fields = new int[data.sourceMapping.get(stream).length];
        		for(int streamPor = 0; streamPor < data.sourceMapping.get(stream).length; streamPor++){ 
        			for(int resultPos = 0; resultPos < data.resultFieldsIndex.size(); resultPos++){
        				if(data.sourceMapping.get(stream)[streamPor].equals(data.resultFieldsIndex.get(resultPos))){
        					fields[streamPor] = resultPos;
        					
            				if(data.sourceMappingType.get(stream)[streamPor] != data.resultTypesIndex.get(resultPos)){
            					
            					log.logError("Can not merge fields from different streams with different data type! [" +
            							ValueMetaInterface.typeCodes[data.resultTypesIndex.get(resultPos)] + "] expected but [" +
            							ValueMetaInterface.typeCodes[data.sourceMappingType.get(stream)[streamPor]] + "] found in " + 
            							"[Stream:Field] [" +stream+":"+data.resultFieldsIndex.get(resultPos)+"]");
            					
            					throw new KettleException("Can not merge fields from different streams with different data type! [" +
            							ValueMetaInterface.typeCodes[data.resultTypesIndex.get(resultPos)] + "] expected but [" +
            							ValueMetaInterface.typeCodes[data.sourceMappingType.get(stream)[streamPor]] + "] found in " + 
            							"[Stream:Field] [" +stream+":"+data.resultFieldsIndex.get(resultPos)+"]");            					
            				}
        				}
        			}
        		}
        		data.targetMapping.put(stream, fields);
        	}
        	
        	log.logBasic("outputMeta created");
		}

		Object[] incomingRow = getRow(); 
		
		if (incomingRow == null) { 
			setOutputDone();
			return false;
		}
		
		Object[] outputRow = RowDataUtil.allocateRowData(data.resultFields.size());
		
		int[] streamFieldPos = data.targetMapping.get(getInputRowSets().get(getCurrentInputRowSetNr()).getOriginStepName());
		
		for(int sourcePos= 0; sourcePos < streamFieldPos.length; sourcePos++){
			outputRow[streamFieldPos[sourcePos]] = incomingRow[sourcePos];
		}

		putRow(data.outputMeta, outputRow);
		incrementLinesInput();
		
		if (checkFeedback(getLinesInput())) {
			if (log.isBasic()) 
				logBasic("LineNr: " + getLinesInput());
		}

		return true;
	}


    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        StreamMergerStepMeta meta = (StreamMergerStepMeta) smi;
        StreamMergerStepData data = (StreamMergerStepData) sdi;
        
    	data.leadingFields = null;
    	data.additionalFields = null;
    	data.leadingFieldsIndex = null;
    	data.additionalFieldsIndex = null;
    	data. outputMeta = null;
    	data.resultFields = null;
    	data.resultFieldsIndex = null;
    	data.sourceMapping = null;
    	data.targetMapping = null;
    	data.infoStreams = null;

        super.dispose(meta, data);
    }

}
