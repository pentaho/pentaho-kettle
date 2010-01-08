package org.pentaho.di.core.gui;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for keeping track of the execution of a job.
 * It does this by keeping a Map in memory.
 * This map has the Unique job entry name has key.
 * The value stored in there includes:
 *  - The execution state (waiting, running, finished)
 *  - The  
 *  
 * @author matt
 *
 */
public class JobExecutionHistory {
	private Map<String, JobEntryExecutionResult> executionMap;
	
	public JobExecutionHistory() {
		this.executionMap = new HashMap<String, JobEntryExecutionResult>();
	}

	/**
	 * @return the executionMap
	 */
	public Map<String, JobEntryExecutionResult> getExecutionMap() {
		return executionMap;
	}

	/**
	 * @param executionMap the executionMap to set
	 */
	public void setExecutionMap(Map<String, JobEntryExecutionResult> executionMap) {
		this.executionMap = executionMap;
	}
	
	
}
