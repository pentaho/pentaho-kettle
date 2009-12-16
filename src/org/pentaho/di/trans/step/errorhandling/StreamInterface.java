package org.pentaho.di.trans.step.errorhandling;

import org.pentaho.di.trans.step.StepMeta;

public interface StreamInterface {
	
	public enum StreamType {
		INPUT, OUTPUT, INFO, TARGET, ERROR,
	};
	
	public String getStepname();
	
	public void setStepMeta(StepMeta stepMeta);
	public StepMeta getStepMeta();
	
	public StreamType getStreamType();
	public void setStreamType(StreamType streamType);
	
	public String getDescription();
	
	public StreamIcon getStreamIcon();
	public void setStreamIcon(StreamIcon streamIcon);
	
	public void setSubject(Object subject);
	public Object getSubject();
}
