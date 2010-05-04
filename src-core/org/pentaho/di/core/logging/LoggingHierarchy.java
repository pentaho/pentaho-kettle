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


/**
 * The logging hierarchy of a transformation or job
 * 
 * @author matt
 *
 */
public class LoggingHierarchy {
	private String rootChannelId; // from the xform or job
	private long   batchId; // from the xform or job
	private LoggingObjectInterface loggingObject;
	/**
	 * @return the rootChannelId
	 */
	public String getRootChannelId() {
		return rootChannelId;
	}
	/**
	 * @param rootChannelId the rootChannelId to set
	 */
	public void setRootChannelId(String rootChannelId) {
		this.rootChannelId = rootChannelId;
	}
	/**
	 * @return the batchId
	 */
	public long getBatchId() {
		return batchId;
	}
	/**
	 * @param batchId the batchId to set
	 */
	public void setBatchId(long batchId) {
		this.batchId = batchId;
	}
	/**
	 * @return the loggingObject
	 */
	public LoggingObjectInterface getLoggingObject() {
		return loggingObject;
	}
	/**
	 * @param loggingObject the loggingObject to set
	 */
	public void setLoggingObject(LoggingObjectInterface loggingObject) {
		this.loggingObject = loggingObject;
	}
	/**
	 * @param rootChannelId
	 * @param batchId
	 * @param loggingObject
	 */
	public LoggingHierarchy(String rootChannelId, long batchId, LoggingObjectInterface loggingObject) {
		this.rootChannelId = rootChannelId;
		this.batchId = batchId;
		this.loggingObject = loggingObject;
	}

	
}
