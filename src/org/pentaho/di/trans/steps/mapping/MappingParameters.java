package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
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
	
	public MappingParameters() {
		super();
		
		variable = new String[] {};
		input = new String[] {};
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
		
		xml.append("    ").append(XMLHandler.closeTag(XML_TAG));  //$NON-NLS-1$
		
		return xml.toString();
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

}
