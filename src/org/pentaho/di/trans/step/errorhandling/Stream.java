package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.trans.step.StepMeta;


public class Stream implements StreamInterface {

	private String description;
	private StreamType streamType;
	private String stepname;
	private StepMeta stepMeta;
	
	/**
	 * @param streamType
	 * @param stepname
	 * @param description
	 */
	public Stream(StreamType streamType, String stepname, StepMeta stepMeta, String description) {
		this.streamType = streamType;
		this.stepname = stepname;
		this.stepMeta = stepMeta;
		this.description = description;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the streamType
	 */
	public StreamType getStreamType() {
		return streamType;
	}
	/**
	 * @param streamType the streamType to set
	 */
	public void setStreamType(StreamType streamType) {
		this.streamType = streamType;
	}
	/**
	 * @return the stepname
	 */
	public String getStepname() {
		if (stepMeta!=null) return stepMeta.getName();
		return stepname;
	}
	/**
	 * @param stepname the stepname to set
	 */
	public void setStepname(String stepname) {
		this.stepname = stepname;
	}

	/**
	 * @return the stepMeta
	 */
	public StepMeta getStepMeta() {
		return stepMeta;
	}

	/**
	 * @param stepMeta the stepMeta to set
	 */
	public void setStepMeta(StepMeta stepMeta) {
		this.stepMeta = stepMeta;
	}
	

}
