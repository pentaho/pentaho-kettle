package org.pentaho.di.core.logging;

import java.text.MessageFormat;

public class LogMessage implements LogMessageInterface {
	private String    logChannelId;
	private String    message;
	private String	  subject;
	private Object[]  arguments;
	private int       level;
	private String	  copy;

	/**
	 * Backward compatibility : no registry used, just log the subject as part of the message
	 * 
	 * @param message
	 * @param logChannelId
	 */
	public LogMessage(String subject, int level) {
		this.subject = subject;
		this.level = level;
		this.message = null;
		this.logChannelId = null;
	}

	/**
	 * Recommended use : 
	 * @param message
	 * @param logChannelId
	 */
	public LogMessage(String message, String logChannelId, int level) {
		this.message = message;
		this.logChannelId = logChannelId;
		this.level = level;
		lookupSubject();
	}
	
	public LogMessage(String message, String logChannelId, Object[] arguments, int level) {
		this.message = message;
		this.logChannelId = logChannelId;	
		this.arguments = arguments;
		this.level = level;
		lookupSubject();
	}
	
	private void lookupSubject() {
		// Derive the subject from the registry
		//
		LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(logChannelId);
		if (loggingObject==null) {
			System.out.println("!!!!!!!!!!!!!!!!!!!OOPS!!!!!!!!!!!!!!!!!!");
		}
		subject = loggingObject.getObjectName();
		copy = loggingObject.getObjectCopy();
	}

	public String toString() {
		if (message==null) return subject;
		if (arguments!=null && arguments.length>0) {
			return subject + " - " + MessageFormat.format(message, arguments);
		} else {
			return subject + " - " + message;
		}
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
		
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the logChannelId
	 */
	public String getLogChannelId() {
		return logChannelId;
	}

	/**
	 * @param logChannelId the logChannelId to set
	 */
	public void setLogChannelId(String logChannelId) {
		this.logChannelId = logChannelId;
	}

	/**
	 * @return the arguments
	 */
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
    
    public boolean isError()
    {
        return level==LogWriter.LOG_LEVEL_ERROR;
    }
    
    public String getCopy() {
		return copy;
	}
    
    public void setCopy(String copy) {
		this.copy = copy;
	}
}
