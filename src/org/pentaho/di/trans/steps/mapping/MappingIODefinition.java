package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
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
	
	private String inputStepname;

	private String outputStepname;
	
	private String description;

	private String parentField[];

	private String childField[];
	
	private boolean mainDataPath;

	/**
	 * No input or output step is defined:<br> 
	 * - detect the source step automatically: use all input steps for this mapping step.<br>
	 * - detect the output step automatically: there can only be one MappingInput step in the mapping in this specific case.
	 */
	public MappingIODefinition() {
		super();
		this.inputStepname = null;
		this.outputStepname = null;
		this.parentField = new String[] {};
		this.childField = new String[] {};
		this.mainDataPath = false;
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
		description = XMLHandler.getTagValue(mappingNode, "description");  //$NON-NLS-1$
		
		int nrConnectors  = XMLHandler.countNodes(mappingNode, "connector"); //$NON-NLS-1$
		parentField = new String[nrConnectors];
		childField = new String[nrConnectors];
		
        for (int i=0;i<nrConnectors;i++)
        {
            Node inputConnector = XMLHandler.getSubNodeByNr(mappingNode, "connector", i); //$NON-NLS-1$
            parentField[i]  = XMLHandler.getTagValue(inputConnector, "parent"); //$NON-NLS-1$
            childField[i] = XMLHandler.getTagValue(inputConnector, "child"); //$NON-NLS-1$
        }
	}
	
	public String getXML()
	{
		StringBuffer xml = new StringBuffer(200);
		
		xml.append("    ").append(XMLHandler.openTag(XML_TAG));  //$NON-NLS-1$
		
		xml.append("    ").append(XMLHandler.addTagValue("input_step", inputStepname));
		xml.append("    ").append(XMLHandler.addTagValue("output_step", outputStepname));
		xml.append("    ").append(XMLHandler.addTagValue("main_path", mainDataPath));
		xml.append("    ").append(XMLHandler.addTagValue("description", description));
		
		for (int i=0;i<parentField.length;i++)
		{
			xml.append("       ").append(XMLHandler.openTag("connector"));  //$NON-NLS-1$ $NON-NLS-2$
			xml.append(XMLHandler.addTagValue("parent", parentField[i], false));  //$NON-NLS-1$
			xml.append(XMLHandler.addTagValue("child", childField[i], false));  //$NON-NLS-1$
			xml.append(XMLHandler.closeTag("connector")).append(Const.CR);  //$NON-NLS-1$
		}
		
		xml.append("    ").append(XMLHandler.closeTag(XML_TAG));  //$NON-NLS-1$
		
		return xml.toString();
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
	 * @return the mappingField
	 */
	public String[] getMappingField() {
		return childField;
	}

	/**
	 * @param mappingField the mappingField to set
	 */
	public void setMappingField(String[] mappingField) {
		this.childField = mappingField;
	}

	/**
	 * @return the parentField
	 */
	public String[] getParentField() {
		return parentField;
	}

	/**
	 * @param parentField the parentField to set
	 */
	public void setParentField(String[] parentField) {
		this.parentField = parentField;
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
}
