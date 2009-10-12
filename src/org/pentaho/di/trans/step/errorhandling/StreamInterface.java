package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.trans.step.StepMeta;

public interface StreamInterface {
	
	public enum StreamType {
		INPUT, OUTPUT, INFO, TARGET
	};
	
	public void setStepname(String stepname);
	public String getStepname();
	public void setStepMeta(StepMeta stepMeta);
	public StepMeta getStepMeta();
	
	public StreamType getStreamType();
	public String getDescription();
}
