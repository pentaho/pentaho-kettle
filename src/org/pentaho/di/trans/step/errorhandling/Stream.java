package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.core.Const;
import org.pentaho.di.trans.step.StepMeta;


public class Stream implements StreamInterface {

	private String description;
	private StreamType streamType;
	private StepMeta stepMeta;
	private StreamIcon	streamIcon;
	private Object subject;
	
	/**
	 * @param streamType
	 * @param stepname
	 * @param stepMeta
	 * @param description
	 */
	public Stream(StreamType streamType, StepMeta stepMeta, String description, StreamIcon streamIcon, Object subject) {
		this.streamType = streamType;
		this.stepMeta = stepMeta;
		this.description = description;
		this.streamIcon = streamIcon;
		this.subject = subject;
	}

	public String toString() {
		if (stepMeta==null) {
			return "Stream type "+streamType+Const.CR+description;
		} else {
			return "Stream type "+streamType+" for step '"+stepMeta.getName()+"'"+Const.CR+description;
		}
	}
	
	public String getStepname() {
		if (stepMeta==null) return null;
		return stepMeta.getName();
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

	/**
	 * @return the streamIcon
	 */
	public StreamIcon getStreamIcon() {
		return streamIcon;
	}

	/**
	 * @param streamIcon the streamIcon to set
	 */
	public void setStreamIcon(StreamIcon streamIcon) {
		this.streamIcon = streamIcon;
	}

	/**
	 * @return the subject
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}
	

}
