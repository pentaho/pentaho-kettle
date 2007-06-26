package org.pentaho.di.trans.steps.mapping;

/**
 * Helps to define the input or output specifications for the Mapping step.
 * 
 * @author matt
 *
 */
public class MappingIODefinition {
	
	private String stepname;

    private String inputField[];
    private String inputMapping[];
    private String outputField[];
    private String outputMapping[];
    
    /**
	 * @param stepname
	 */
	public MappingIODefinition(String stepname) {
		super();
		this.stepname = stepname;
	}
	
	/**
	 * @return the inputField
	 */
	public String[] getInputField() {
		return inputField;
	}
	/**
	 * @param inputField the inputField to set
	 */
	public void setInputField(String[] inputField) {
		this.inputField = inputField;
	}
	/**
	 * @return the inputMapping
	 */
	public String[] getInputMapping() {
		return inputMapping;
	}
	/**
	 * @param inputMapping the inputMapping to set
	 */
	public void setInputMapping(String[] inputMapping) {
		this.inputMapping = inputMapping;
	}
	/**
	 * @return the outputField
	 */
	public String[] getOutputField() {
		return outputField;
	}
	/**
	 * @param outputField the outputField to set
	 */
	public void setOutputField(String[] outputField) {
		this.outputField = outputField;
	}
	/**
	 * @return the outputMapping
	 */
	public String[] getOutputMapping() {
		return outputMapping;
	}
	/**
	 * @param outputMapping the outputMapping to set
	 */
	public void setOutputMapping(String[] outputMapping) {
		this.outputMapping = outputMapping;
	}
	/**
	 * @return the stepname
	 */
	public String getStepname() {
		return stepname;
	}
	/**
	 * @param stepname the stepname to set
	 */
	public void setStepname(String stepname) {
		this.stepname = stepname;
	}
    
    

}
