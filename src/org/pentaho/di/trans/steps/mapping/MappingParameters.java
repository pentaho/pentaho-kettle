/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

/**
 * We need out mapping to be parameterized.<br>
 * This we do with the use of environment variables.<br>
 * That way we can set one variable to another, etc.<br>
 * 
 * @author matt
 * @version 3.0
 * @since 2007-06-27
 *
 */
public class MappingParameters implements Cloneable {
	
	public static final String XML_TAG = "parameters";   //$NON-NLS-1$
	
	private static final String XML_VARIABLES_TAG = "variablemapping";  //$NON-NLS-1$
	
	/** The name of the variable to set in the sub-transformation */
	private String variable[];

	/** This is a simple String with optionally variables in them **/
	private String input[];
	
	/** This flag causes the sub-transformation to inherit all variables from the parent */
	private boolean inheritingAllVariables;
	
	public MappingParameters() {
		super();
		
		variable = new String[] {};
		input = new String[] {};
		
		inheritingAllVariables = true;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new RuntimeException(e); // Nope, we don't want that in our code.
		}
	}
	
	public MappingParameters(Node paramNode) {
		
		int nrVariables  = XMLHandler.countNodes(paramNode, XML_VARIABLES_TAG);
		variable = new String[nrVariables];
		input    = new String[nrVariables];
		
		for (int i=0;i<variable.length;i++) {
			Node variableMappingNode = XMLHandler.getSubNodeByNr(paramNode, XML_VARIABLES_TAG, i);
            
			variable[i] = XMLHandler.getTagValue(variableMappingNode, "variable");
			input[i]    = XMLHandler.getTagValue(variableMappingNode, "input");
		}
		
		inheritingAllVariables = "Y".equalsIgnoreCase(XMLHandler.getTagValue(paramNode, "inherit_all_vars"));
	}
	
	public String getXML() {
		StringBuffer xml = new StringBuffer(200);
		
		xml.append("    ").append(XMLHandler.openTag(XML_TAG));  //$NON-NLS-1$
		
		for (int i=0;i<variable.length;i++)
		{
			xml.append("       ").append(XMLHandler.openTag(XML_VARIABLES_TAG));  //$NON-NLS-1$
			xml.append(XMLHandler.addTagValue("variable", variable[i], false));  //$NON-NLS-1$
			xml.append(XMLHandler.addTagValue("input", input[i], false));  //$NON-NLS-1$
			xml.append(XMLHandler.closeTag(XML_VARIABLES_TAG)).append(Const.CR);
		}
		xml.append("    ").append(XMLHandler.addTagValue("inherit_all_vars", inheritingAllVariables));
		xml.append("    ").append(XMLHandler.closeTag(XML_TAG));  //$NON-NLS-1$
		
		return xml.toString();
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		for (int i=0;i<variable.length;i++)
		{
			rep.saveStepAttribute(id_transformation, id_step, i, "mapping_parameter_variable", variable[i]);  //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, i, "mapping_parameter_input", input[i]);  //$NON-NLS-1$
		}
		rep.saveStepAttribute(id_transformation, id_step, "mapping_parameter_inherit_all_vars", inheritingAllVariables);
	}

	public MappingParameters(Repository rep, ObjectId id_step) throws KettleException {
		int nrVariables = rep.countNrStepAttributes(id_step, "mapping_parameter_variable");
		
		variable = new String[nrVariables];
		input    = new String[nrVariables];
		
		for (int i=0;i<nrVariables;i++)
		{
			variable[i] = rep.getStepAttributeString(id_step, i, "mapping_parameter_variable");  //$NON-NLS-1$
			input[i]    = rep.getStepAttributeString(id_step, i, "mapping_parameter_input");  //$NON-NLS-1$
		}
		inheritingAllVariables = rep.getStepAttributeBoolean(id_step, "mapping_parameter_inherit_all_vars");
	}


	/**
	 * @return the inputField
	 */
	public String[] getInputField() {
		return input;
	}

	/**
	 * @param inputField
	 *            the inputField to set
	 */
	public void setInputField(String[] inputField) {
		this.input = inputField;
	}

	/**
	 * @return the variable
	 */
	public String[] getVariable() {
		return variable;
	}

	/**
	 * @param variable
	 *            the variable to set
	 */
	public void setVariable(String[] variable) {
		this.variable = variable;
	}

	/**
	 * @return the inheritingAllVariables
	 */
	public boolean isInheritingAllVariables() {
		return inheritingAllVariables;
	}

	/**
	 * @param inheritingAllVariables the inheritingAllVariables to set
	 */
	public void setInheritingAllVariables(boolean inheritingAllVariables) {
		this.inheritingAllVariables = inheritingAllVariables;
	}


}
