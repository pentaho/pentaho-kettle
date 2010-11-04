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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/**
 * Helps to define the input or output specifications for the Mapping step.
 * 
 * @author matt
 * @version 3.0
 * @since 2007-07-26
 *
 */
public class MappingIODefinition implements Cloneable {

	public static final String XML_TAG = "mapping";
	
	private StepMeta inputStep;
	
	private String inputStepname;

	private String outputStepname;
	
	private String description;

	private List<MappingValueRename> valueRenames;
	
	private boolean mainDataPath;
	
	private boolean renamingOnOutput;

	/**
	 * No input or output step is defined:<br> 
	 * - detect the source step automatically: use all input steps for this mapping step.<br>
	 * - detect the output step automatically: there can only be one MappingInput step in the mapping in this specific case.
	 */
	public MappingIODefinition() {
		super();
		this.inputStepname = null;
		this.outputStepname = null;
		this.valueRenames = new ArrayList<MappingValueRename>();
		this.mainDataPath = false;
		this.renamingOnOutput= false;
	}
	/**
	 * @param inputStepname the name of the step to "connect" to.  
	 *        If no name is given, detect the source step automatically: use all input steps for this mapping step.
	 * @param outputStepname the name of the step in the mapping to accept the data from the input step.
	 *        If no name is given, detect the output step automatically: there can only be one MappingInput step in the mapping in this specific case.
	 */
	public MappingIODefinition(String inputStepname, String outputStepname) {
		this();
		this.inputStepname = inputStepname;
		this.outputStepname = outputStepname;
	}
	
	@Override
	public Object clone() {
		try
		{
			MappingIODefinition definition = (MappingIODefinition) super.clone();
			return definition;
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(e); // We don't want that in our code do we?
		}
	}

	public MappingIODefinition(Node mappingNode) {
		
		this();
		
		inputStepname = XMLHandler.getTagValue(mappingNode, "input_step");  //$NON-NLS-1$
		outputStepname = XMLHandler.getTagValue(mappingNode, "output_step");  //$NON-NLS-1$
		mainDataPath =  "Y".equalsIgnoreCase(XMLHandler.getTagValue(mappingNode, "main_path"));  //$NON-NLS-1$ $NON-NLS-2$
		renamingOnOutput = "Y".equalsIgnoreCase(XMLHandler.getTagValue(mappingNode, "rename_on_output"));  //$NON-NLS-1$ $NON-NLS-2$
		description = XMLHandler.getTagValue(mappingNode, "description");  //$NON-NLS-1$
		
		int nrConnectors  = XMLHandler.countNodes(mappingNode, "connector"); //$NON-NLS-1$
		
        for (int i=0;i<nrConnectors;i++)
        {
            Node inputConnector = XMLHandler.getSubNodeByNr(mappingNode, "connector", i); //$NON-NLS-1$
            String parentField = XMLHandler.getTagValue(inputConnector, "parent"); //$NON-NLS-1$
            String childField = XMLHandler.getTagValue(inputConnector, "child"); //$NON-NLS-1$
            valueRenames.add( new MappingValueRename(parentField, childField) );
        }
	}
	
	public String getXML()
	{
		StringBuffer xml = new StringBuffer(200);
		
		xml.append("    ").append(XMLHandler.openTag(XML_TAG));  //$NON-NLS-1$
		
		xml.append("    ").append(XMLHandler.addTagValue("input_step", inputStepname));
		xml.append("    ").append(XMLHandler.addTagValue("output_step", outputStepname));
		xml.append("    ").append(XMLHandler.addTagValue("main_path", mainDataPath));
		xml.append("    ").append(XMLHandler.addTagValue("rename_on_output", renamingOnOutput));
		xml.append("    ").append(XMLHandler.addTagValue("description", description));
		
		for (MappingValueRename valueRename : valueRenames)
		{
			xml.append("       ").append(XMLHandler.openTag("connector"));  //$NON-NLS-1$ $NON-NLS-2$
			xml.append(XMLHandler.addTagValue("parent", valueRename.getSourceValueName(), false));  //$NON-NLS-1$
			xml.append(XMLHandler.addTagValue("child", valueRename.getTargetValueName(), false));  //$NON-NLS-1$
			xml.append(XMLHandler.closeTag("connector")).append(Const.CR);  //$NON-NLS-1$
		}
		
		xml.append("    ").append(XMLHandler.closeTag(XML_TAG));  //$NON-NLS-1$
		
		return xml.toString();
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step, String prefix, int nr) throws KettleException {
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"input_step", inputStepname); 
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"output_step", outputStepname); 
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"main_path", mainDataPath); 
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"rename_on_output", renamingOnOutput); 
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"description", description); 
		
		rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"nr_renames", valueRenames.size());
		for (int i=0;i<valueRenames.size();i++) {
			rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"rename_parent_"+i, valueRenames.get(i).getSourceValueName());
			rep.saveStepAttribute(id_transformation, id_step, nr, prefix+"rename_child_"+i, valueRenames.get(i).getTargetValueName());
		}
		
	}
	
	public MappingIODefinition(Repository rep, ObjectId id_step, String prefix, int nr) throws KettleException {
		this();
		
		inputStepname = rep.getStepAttributeString(id_step, nr, prefix+"input_step");
		outputStepname = rep.getStepAttributeString(id_step, nr, prefix+"output_step");
		mainDataPath = rep.getStepAttributeBoolean(id_step, nr, prefix+"main_path");
		renamingOnOutput = rep.getStepAttributeBoolean(id_step, nr, prefix+"rename_on_output");
		description = rep.getStepAttributeString(id_step, nr, prefix+"description");
		
		int nrRenames = (int) rep.getStepAttributeInteger(id_step, nr, prefix+"nr_renames");
		for (int i=0;i<nrRenames;i++) {
			String parent = rep.getStepAttributeString(id_step, nr, prefix+"rename_parent_"+i);
			String child = rep.getStepAttributeString(id_step, nr, prefix+"rename_child_"+i);
			valueRenames.add(new MappingValueRename(parent, child));
		}
	}

	/**
	 * @return the stepname, the name of the step to "connect" to.  If no step name is given, detect the Mapping Input/Output step automatically.
	 */
	public String getInputStepname() {
		return inputStepname;
	}

	/**
	 * @param inputStepname the stepname to set
	 */
	public void setInputStepname(String inputStepname) {
		this.inputStepname = inputStepname;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the outputStepname
	 */
	public String getOutputStepname() {
		return outputStepname;
	}

	/**
	 * @param outputStepname the outputStepname to set
	 */
	public void setOutputStepname(String outputStepname) {
		this.outputStepname = outputStepname;
	}
	
	/**
	 * @return true if this is the main data path for the mapping step.
	 */
	public boolean isMainDataPath() {
		return mainDataPath;
	}
	
	/**
	 * @param mainDataPath true if this is the main data path for the mapping step.
	 */
	public void setMainDataPath(boolean mainDataPath) {
		this.mainDataPath = mainDataPath;
	}
	/**
	 * @return the renamingOnOutput
	 */
	public boolean isRenamingOnOutput() {
		return renamingOnOutput;
	}
	/**
	 * @param renamingOnOutput the renamingOnOutput to set
	 */
	public void setRenamingOnOutput(boolean renamingOnOutput) {
		this.renamingOnOutput = renamingOnOutput;
	}
	/**
	 * @return the valueRenames
	 */
	public List<MappingValueRename> getValueRenames() {
		return valueRenames;
	}
	/**
	 * @param valueRenames the valueRenames to set
	 */
	public void setValueRenames(List<MappingValueRename> valueRenames) {
		this.valueRenames = valueRenames;
	}

  /**
   * @return the inputStep
   */
  public StepMeta getInputStep() {
    return inputStep;
  }

  /**
   * @param inputStep the inputStep to set
   */
  public void setInputStep(StepMeta inputStep) {
    this.inputStep = inputStep;
  }
}
