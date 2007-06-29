package org.pentaho.di.trans.steps.mapping;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * We need out mapping to be parameterized.<br>
 * This we do with the use of environment variables.<br>
 * To make this easier, we allow another step to provide the values OR we allow the values to be specified with strings, optionally containing other variables.<br>
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
	
	/**
	 * The step to receive the parameters from (Set Variables variant) This is
	 * optional. If not specified (null or empty) it's going to be simply using
	 * environment variables, not fields
	 */
	private String stepname;

	/** The name of the variable to set in the sub-transformation */
	private String variable[];

	/** This is either an input field or a String (with optionally variables in them) **/
	private String input[];
	
	public MappingParameters() {
		super();
	}

	public MappingParameters(String stepname) {
		super();
		this.stepname = stepname;
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
		
		stepname = XMLHandler.getTagValue(paramNode, "stepname");  //$NON-NLS-1$
		
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
	 * @return the stepname
	 */
	public String getStepname() {
		return stepname;
	}

	/**
	 * @param stepname
	 *            the stepname to set
	 */
	public void setStepname(String stepname) {
		this.stepname = stepname;
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
