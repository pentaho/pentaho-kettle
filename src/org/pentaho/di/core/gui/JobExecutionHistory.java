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
