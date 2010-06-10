/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.logging;

import java.text.MessageFormat;

public class LogMessage implements LogMessageInterface {
	private String    logChannelId;
	private String    message;
	private String	  subject;
	private Object[]  arguments;
	private LogLevel  level;
	private String	  copy;

	/**
	 * Backward compatibility : no registry used, just log the subject as part of the message
	 * 
	 * @param message
	 * @param logChannelId
	 */
	public LogMessage(String subject, LogLevel level) {
		this.subject = subject;
		this.level = level;
		this.message = null;
		this.logChannelId = null;
	}

	/**
	 * Recommended use : 
	 * @param message
	 * @param logChannelId
	 * @param level the log level
	 */
	public LogMessage(String message, String logChannelId, LogLevel level) {
		this.message = message;
		this.logChannelId = logChannelId;
		this.level = level;
		lookupSubject();
	}
	
	public LogMessage(String message, String logChannelId, Object[] arguments, LogLevel level) {
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
		if (loggingObject!=null) {
			subject = loggingObject.getObjectName();
			copy = loggingObject.getObjectCopy();
		}
	}

	public String toString() {
		if (message==null) return subject;
		if (arguments!=null && arguments.length>0) {
			return subject + " - " + MessageFormat.format(message, arguments);
		} else {
			return subject + " - " + message;
		}
	}
	
	public LogLevel getLevel() {
		return level;
	}
	
	public void setLevel(LogLevel level) {
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
        return level.isError();
    }
    
    public String getCopy() {
		return copy;
	}
    
    public void setCopy(String copy) {
		this.copy = copy;
	}
}
