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
	RUNNING("running");

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
}