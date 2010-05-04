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
package org.pentaho.di.core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class KettleVariablesList {
	
	private static KettleVariablesList kettleVariablesList;
	
	public static KettleVariablesList getInstance() {
		if (kettleVariablesList==null) {
			kettleVariablesList = new KettleVariablesList();
		}
		return kettleVariablesList;
	}
	
	private Map<String, String> variablesMap;
	
	public static void init() throws KettleException {
		
		KettleVariablesList variablesList = getInstance();
		
		InputStream inputStream = variablesList.getClass().getResourceAsStream(Const.KETTLE_VARIABLES_FILE);
		if (inputStream==null) {
			inputStream =  variablesList.getClass().getResourceAsStream("/"+Const.KETTLE_VARIABLES_FILE);
		}
		if (inputStream==null) {
			throw new KettlePluginException("Unable to find standard kettle variables definition file: "+Const.KETTLE_VARIABLES_FILE);
		}

		try {
			Document doc = XMLHandler.loadXMLFile(inputStream, null, false, false);
			Node varsNode = XMLHandler.getSubNode(doc, "kettle-variables");
			int nrVars = XMLHandler.countNodes(varsNode, "kettle-variable");
			for (int i=0;i<nrVars;i++) {
				Node varNode = XMLHandler.getSubNodeByNr(varsNode, "kettle-variable", i);
				String description = XMLHandler.getTagValue(varNode, "description");
				String variable = XMLHandler.getTagValue(varNode, "variable");
				
				variablesList.getVariablesMap().put(variable, description);
			}
			
		} catch(Exception e) {
			throw new KettleException("Unable to read file '"+Const.KETTLE_VARIABLES_FILE+"'", e);
		}
	}
	
	private KettleVariablesList() {
		variablesMap=new HashMap<String, String>();
	}
	
	/**
	 * @return A mapping between the name of a standard kettle variable and its description.
	 */
	public Map<String, String> getVariablesMap() {
		return variablesMap;
	}
}
