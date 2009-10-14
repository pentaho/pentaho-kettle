package org.pentaho.di.trans.step;

import java.util.List;

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
	
	/**
	 * Add a stream to the steps I/O interface
	 * 
	 * @param stream The stream to add
	 */
	public void addStream(StreamInterface stream);
	
	/**
	 * Change step names into step objects for the info and target streams.  We do this to allow them to be name-changed etc.
	 * @param steps the steps to reference
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps);	
}
