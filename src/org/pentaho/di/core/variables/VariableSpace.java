package org.pentaho.di.core.variables;

import java.util.Properties;

/**
 * Interface to implement variable sensitive objects.
 * 
 * @author Sven Boden 
 */
public interface VariableSpace
{
	/**
	 * Initialize variable space using the defaults, copy over the variables 
	 * from the parent (using copyVariablesFrom()), after this the "injected" 
	 * variables should be inserted (injectVariables()). 
	 * 
	 * The parent is set as parent variable space.
	 * 
	 * @param parent the parent to start from, or null if root.
	 */
    void initializeVariablesFrom(VariableSpace parent);
    
    /**
     * Copy the variables from another space, without initializing with the
     * defaults. This does not affect any parent relationship.
     * 
     * @param space the space to copy the variables from.
     */
    void copyVariablesFrom(VariableSpace space);
    
    /**
     * Share a variable space from another variable space. This means
     * that the object should take over the space used as argument.
     * 
     * @param space Variable space to be shared.
     */
    void shareVariablesWith(VariableSpace space);
    
    /**
     * Get the parent of the variable space.
     * 
     * @return the parent.
     */
    VariableSpace getParentVariableSpace();
    
    /**
     * Sets a variable in the Kettle Variables list.
     * 
     * @param variableName The name of the variable to set
     * @param variableValue The value of the variable to set.  If the 
     *                      variableValue is null, the variable is cleared 
     *                      from the list. 
     */
    void setVariable(String variableName, String variableValue);

    /**
     * Get the value of a variable with a default in case the variable 
     * is not found.
     * 
     * @param variableName The name of the variable
     * @param defaultValue The default value in case the variable could not be 
     *                     found
     * @return the String value of a variable
     */
    String getVariable(String variableName, String defaultValue);

    /**
     * Get the value of a variable.
     * 
     * @param variableName The name of the variable
     * @return the String value of a variable or null in case the variable could not be found.
     */
    String getVariable(String variableName);
    
    /**
     * List the variables (not the values) that are currently in the
     * variable space.
     * 
     * @return Array of String variable names.
     */
    String[] listVariables();
    
    /**
     * Substitute the string using the current variable space.
     * 
     * @param aString The string to substitute.
     * 
     * @return The substituted string.
     */
    String environmentSubstitute(String aString);
    
    /**
     * Inject variables. The behaviour should be that the properties
     * object will be stored and at the time the VariableSpace is
     * initialized (or upon calling this method if the space is already 
     * initialized).
     * After injecting the link of the properties object should be removed.
     *  
     * @param prop Properties object containing key-value pairs.
     */
    void injectVariables(Properties prop);
}