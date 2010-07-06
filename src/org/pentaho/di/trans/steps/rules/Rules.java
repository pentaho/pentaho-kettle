package org.pentaho.di.trans.steps.rules;

import java.util.Arrays;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This Transformation Step allows a user to execute a rule set against
 * an individual rule or a collection of rules.
 * 
 * Additional columns can be added to the output from the rules and these
 * (of course) can be used for routing if desired.
 * 
 * @author cboyden
 *
 */

public class Rules extends BaseStep implements StepInterface {
	private static Class<?> PKG = Rules.class; // for i18n purposes
	
	private RulesMeta meta;
	private RulesData data;
	
	public static class Column {
		private String          name;
	    private String          type;
	    private Object          payload;
	    private Boolean			external;
	    
	    public Column() {
	    	this.external = false;
	    }
	    
	    public Column(Boolean external) {
	    	this.external = external;
	    }
		
	    public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Object getPayload() {
			return payload;
		}
		public void setPayload(Object payload) {
			this.payload = payload;
		}
		public void setExternalSource(Boolean external) {
			this.external = external;
		}
		public Boolean isExternalSource() {
			return external;
		}
	}

	public Rules(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {

        meta = (RulesMeta) smi;
        data = (RulesData) sdi;

        if (super.init(smi, sdi)) {
            return true;
        }
        return false;
    }
	
	public boolean runtimeInit() throws KettleStepException {
    	data.setOutputRowMeta(getInputRowMeta().clone()); 
        meta.getFields(data.getOutputRowMeta(), getStepname(), null, null, this);
		
		data.setRuleFilePath(meta.getRuleFile());
		data.setRuleString(meta.getRuleDefinition());
		
		data.initializeRules();
		data.initializeColumns(getInputRowMeta());
		
		return true;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (RulesMeta) smi;
        data = (RulesData) sdi;

        super.dispose(smi, sdi);
    }
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        meta = (RulesMeta) smi;
        data = (RulesData) sdi;
        
        Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			data.shutdown();
			setOutputDone();
			return false;
		}
		
		if(first) {
			if(!runtimeInit()) {
				return false;
			}
			
			first = false;
		}
		
		// Load the column objects
		data.loadRow(r);
		
		data.execute();

		Object[] outputRow;
		int beginOutputRowFill = 0;
		
		String[] expectedResults = meta.getExpectedResultList();

		if(meta.isKeepInputFields()) {
			int inputRowSize = getInputRowMeta().size();
			outputRow = Arrays.copyOf(r,  inputRowSize + expectedResults.length);
			beginOutputRowFill = inputRowSize;
		} else {
			outputRow = new Object[expectedResults.length];
		}
		
		Rules.Column result = null;
		for(int i = 0; i < expectedResults.length; i++) {
			result = (Rules.Column)data.fetchResult(expectedResults[i]);
			outputRow[i + beginOutputRowFill] = result == null ? null : result.getPayload();
		}
		
		putRow(data.getOutputRowMeta(), outputRow);
        
        return true;
    }
}
