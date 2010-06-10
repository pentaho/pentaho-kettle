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
package org.pentaho.di.lineage;

import java.util.List;

/**
 * This describes how a field gets renamed in a certain step.<br>
 * It helps us to do the complete lineage from source to target and back.<br>
 *  
 * @author matt
 *
 */
public class FieldnameLineage {
	private String inputFieldname;
	private String outputFieldname;
	
	/**Create a new field lineage object
	 * @param inputFieldname The input field name 
	 * @param outputFieldname The output field name
	 */
	public FieldnameLineage(String inputFieldname, String outputFieldname) {
		super();
		this.inputFieldname = inputFieldname;
		this.outputFieldname = outputFieldname;
	}

	/**
	 * @return the input Field name
	 */
	public String getInputFieldname() {
		return inputFieldname;
	}

	/**
	 * @param inputFieldname the input Field name to set
	 */
	public void setInputFieldname(String inputFieldname) {
		this.inputFieldname = inputFieldname;
	}

	/**
	 * @return the output Field name
	 */
	public String getOutputFieldname() {
		return outputFieldname;
	}

	/**
	 * @param outputFieldname the output Field name to set
	 */
	public void setOutputFieldname(String outputFieldname) {
		this.outputFieldname = outputFieldname;
	}
	
	/**
	 * Search for a field name lineage object in a list.
	 * @param lineages The list
	 * @param input the input field name to look for
	 * @return The first encountered field name lineage object where the input field name matches.  If nothing is found null is returned.
	 */
	public static final FieldnameLineage findFieldnameLineageWithInput(List<FieldnameLineage> lineages, String input) {
		for (FieldnameLineage lineage : lineages) {
			if (lineage.getInputFieldname().equalsIgnoreCase(input)) {
				return lineage;
			}
		}
		return null;
	}
}
