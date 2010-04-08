package org.pentaho.di.core.logging;

public interface LogMessageInterface {
	public String getLogChannelId();
	public String getMessage();
	public LogLevel getLevel();
	public String getSubject();
	public String getCopy();
	public Object[] getArguments();
	
}
