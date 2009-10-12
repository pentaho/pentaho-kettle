package org.pentaho.di.trans.step;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public interface StepIOMetaInterface {
	
	public boolean isInputAcceptor();
	public boolean isOutputProducer();
	public boolean isInputOptional();
	public boolean isSortedDataRequired();
	public StreamInterface[] getInfoStreams();
	public StreamInterface[] getTargetStreams();
	
	public String[] getInfoStepnames();
	public String[] getTargetStepnames();
	
    /**
     * Replace the info steps with the supplied source steps.
     * 
     * @param infoSteps
     */
	public void setInfoSteps(StepMeta[] infoSteps);
	
}
