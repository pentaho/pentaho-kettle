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
 * This enumeration describes the logging status in a logging table for transformations and jobs.
 * 
 * @author matt
 *
 */
public enum LogStatus {

	START("start"), 
	END("end"), 
	STOP("stop"), 
	ERROR("error"), 
	RUNNING("running"),
	PAUSED("paused"),
	;

	private String	status;

	private LogStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	
	public String toString() {
		return status;
	}
	
	public boolean equals(LogStatus logStatus) {
		return status.equalsIgnoreCase(logStatus.status);
	}
	
	/**
	 * Find the LogStatus based on the string description of the status.
	 * 
	 * @param status the status string to search for
	 * @return the LogStatus or null if none is found 
	 */
	public static LogStatus findStatus(String status) {
		for (LogStatus logStatus : values()) {
			if (logStatus.status.equalsIgnoreCase(status)) {
				return logStatus;
			}
		}
		return null;
	}
}