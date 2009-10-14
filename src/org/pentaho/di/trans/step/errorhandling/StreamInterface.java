package org.pentaho.di.trans.step.errorhandling;

import java.util.List;

import org.pentaho.di.trans.step.StepMeta;

public interface StreamInterface {
	
	public enum StreamType {
		INPUT, OUTPUT, INFO, TARGET,
	};
	
	public void setStepname(String stepname);
	public String getStepname();
	public void setStepMeta(StepMeta stepMeta);
	public StepMeta getStepMeta();
	
	public StreamType getStreamType();
	public String getDescription();
	
	/**
	 * Change step names into step objects to allow them to be name-changed etc.
	 * @param steps the steps to reference
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps);
}
