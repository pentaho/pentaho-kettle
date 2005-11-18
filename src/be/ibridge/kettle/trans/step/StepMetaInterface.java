 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;


/**
 * This interface allows custom steps to talk to Kettle. 
 * 
 * @since 4-aug-2004
 * @author Matt
 */

public interface StepMetaInterface
{
	/**
	 * Set default values
	 */
	public void setDefault();

	/**
	 * Get the fields that are emitted by this step
	 * @param r The fields that are entering the step
	 * @param name The name of the step to be used as origin
	 * @param info The fields that are used as information by the step
	 * @return The fields that are being emitted by this step.
	 * @throws KettleStepException when an error occurred searching for the fields.
	 */
	public Row getFields(Row r, String name, Row info)
		throws KettleStepException;

	/**
	 * Get the XML that represents the values in this step
	 * @return the XML that represents the metadata in this step
	 */
	public String getXML();

	/**
	 * Load the values for this step from an XML Node
	 * @param stepnode the Node to get the info from
	 * @param databases The available list of databases to reference to
	 * @param counters Counters to reference.
	 * @throws KettleXMLException When an unexpected XML error occurred. (malformed etc.)
	 */
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException;

	/**
	 * Save the steps data into a Kettle repository
	 * @param rep The Kettle repository to save to
	 * @param id_transformation The transformation ID
	 * @param id_step The step ID
	 * @throws KettleException When an unexpected error occurred (database, network, etc)
	 */
	public void saveRep(Repository rep, long id_transformation, long id_step) 
		throws KettleException;

	/**
	 * Read the steps information from a Kettle repository
	 * @param rep The repository to read from
	 * @param id_step The step ID
	 * @param databases The databases to reference
	 * @param counters The counters to reference
	 * @throws KettleException When an unexpected error occurred (database, network, etc)
	 */
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException;

	/**
	 * Checks the settings of this step and puts the findings in a remarks List.
	 * @param remarks The list to put the remarks in @see be.ibridge.kettle.core.CheckResult
	 * @param stepMeta The stepMeta to help checking
	 * @param prev The fields coming from the previous step
	 * @param input The input step names
	 * @param output The output step names
	 * @param info The fields that are used as information by the step
	 */
	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info);

	/**
	 * Make an exact copy of this step, make sure to explicitly copy Collections etc.
	 * @return an exact copy of this step
	 */
	public Object clone();

	/**
	 * @return The fields used by this step, this is being used for the Impact analyses.
	 */
	public Row getTableFields();

	/**
	 * @return the informational source steps, if any. Null is the default: none.
	 */
	public String[] getInfoSteps();

	/**
	 * @return the chosen target steps, if any. Null is the default: automatically chosen target steps.
	 */
	public String[] getTargetSteps();

	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(ArrayList steps);
	
	/**
	 * We know which dialog to open...
	 * @param shell The shell to open the dialog on
	 * @param meta The step info
	 * @param transMeta The transformation meta-data 
	 * @param stepname The name of the step
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String stepname);

	/**
	 * Get the executing step, needed by Trans to launch a step.
	 * @param stepMeta The step info
	 * @param stepDataInterface the step data interface linked to this step.  Here the step can store temporary data, database connections, etc.
	 * @param copyNr The copy nr to get
	 * @param transMeta The transformation info
	 * @param trans The launching transformation
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans);

	/**
	 * Get a new instance of the appropriate data class.
	 * This data class implements the StepDataInterface.
	 * It basically contains the persisting data that needs to live on, even if a worker thread is terminated.
	 * 
	 * @return The appropriate StepDataInterface class.
	 */
	public StepDataInterface getStepData();

	/**
	 * Each step must be able to report on the impact it has on a database, table field, etc.
	 * @param impact The list of impacts @see be.ibridge.kettle.transMeta.DatabaseImpact
	 * @param transMeta The transformation information
	 * @param stepMeta The step information
	 * @param prev The fields entering this step
	 * @param input The previous step names
	 * @param output The output step names
	 * @param info The fields used as information by this step
	 */
	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev,
			String input[], String output[], Row info)
		throws KettleStepException;

	/**
	 * Standard method to return an SQLStatement object with SQL statements that the step needs in order to work correctly.
	 * This can mean "create table", "create index" statements but also "alter table ... add/drop/modify" statements.
	 *
	 * @return The SQL Statements for this step. If nothing has to be done, the SQLStatement.getSQL() == null. @see SQLStatement 
	 * @param transMeta TransInfo object containing the complete transformation
	 * @param stepMeta StepMeta object containing the complete step
	 * @param prev Row containing meta-data for the input fields (no data)
	 */
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
		throws KettleStepException;
    
	/**
     *  Call this to cancel trailing database queries (too long running, etc)
	 */
    public void cancelQueries() throws KettleDatabaseException;
    
    /**
     * Default a step doesn't use any arguments.
     * Implement this to notify the GUI that a window has to be displayed BEFORE launching a transformation.
     * You can also use this to specify certain Environment variable values.
     * 
     * @return A row of argument values. (name and optionally a default value)
     *         Put 10 values in the row for the possible 10 arguments.
     *         Set the type to Value.VALUE_TYPE_NONE if it's not used!
     */
    public Row getUsedArguments();
}