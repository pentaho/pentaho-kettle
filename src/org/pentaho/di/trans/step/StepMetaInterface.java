 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.step;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.w3c.dom.Node;


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
	 * @param inputRowMeta The fields that are entering the step.  These are changed to reflect the output metadata.
	 * @param name The name of the step to be used as origin
	 * @param info The input rows metadata that enters the step through the specified channels in the same order as in method getInfoSteps().  The step metadata can then choose what to do with it: ignore it or not.
     *        Interesting is also that in case of database lookups, the layout of the target database table is put in info[0]
	 * @param nextStep if this is a non-null value, it's the next step in the transformation.  The one who's asking, the step where the data is targetted towards.
	 * @param space TODO
	 * @throws KettleStepException when an error occurred searching for the fields.
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException;

	/**
	 * Get the XML that represents the values in this step
	 * @return the XML that represents the metadata in this step
	 * @throws KettleException in case there is a conversion or XML encoding error
	 */
	public String getXML() throws KettleException;

	/**
	 * Load the values for this step from an XML Node
	 * @param stepnode the Node to get the info from
	 * @param databases The available list of databases to reference to
	 * @param counters Counters to reference.
	 * @throws KettleXMLException When an unexpected XML error occurred. (malformed etc.)
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException;

	/**
	 * Save the steps data into a Kettle repository
	 * @param rep The Kettle repository to save to
	 * @param id_transformation The transformation ID
	 * @param id_step The step ID
	 * @throws KettleException When an unexpected error occurred (database, network, etc)
	 */
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException;

	/**
	 * Read the steps information from a Kettle repository
	 * @param rep The repository to read from
	 * @param id_step The step ID
	 * @param databases The databases to reference
	 * @param counters The counters to reference
	 * @throws KettleException When an unexpected error occurred (database, network, etc)
	 */
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException;
    
	/**
	 * Checks the settings of this step and puts the findings in a remarks List.
	 * @param remarks The list to put the remarks in @see org.pentaho.di.core.CheckResult
	 * @param stepMeta The stepMeta to help checking
	 * @param prev The fields coming from the previous step
	 * @param input The input step names
	 * @param output The output step names
	 * @param info The fields that are used as information by the step
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info);

	/**
	 * Make an exact copy of this step, make sure to explicitly copy Collections etc.
	 * @return an exact copy of this step
	 */
	public Object clone();

	/**
	 * @return The fields used by this step, this is being used for the Impact analyses.
	 */
	public RowMetaInterface getTableFields();
    
    /**
     * This method is added to exclude certain steps from layout checking.  
     * @since 2.5.0
     */
    public boolean excludeFromRowLayoutVerification();

    /**
     * This method is added to exclude certain steps from copy/distribute checking.  
     * @since 4.0.0
     */
    public boolean excludeFromCopyDistributeVerification();
    
    /**
     * Get the name of the class that implements the dialog for this job entry
     * JobEntryBase provides a default
     */
    public String getDialogClassName();
    
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
	 * @param impact The list of impacts @see org.pentaho.di.transMeta.DatabaseImpact
	 * @param transMeta The transformation information
	 * @param stepMeta The step information
	 * @param prev The fields entering this step
	 * @param input The previous step names
	 * @param output The output step names
	 * @param info The fields used as information by this step
	 */
	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException;

	/**
	 * Standard method to return an SQLStatement object with SQL statements that the step needs in order to work correctly.
	 * This can mean "create table", "create index" statements but also "alter table ... add/drop/modify" statements.
	 *
	 * @return The SQL Statements for this step. If nothing has to be done, the SQLStatement.getSQL() == null. @see SQLStatement 
	 * @param transMeta TransInfo object containing the complete transformation
	 * @param stepMeta StepMeta object containing the complete step
	 * @param prev Row containing meta-data for the input fields (no data)
	 */
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) throws KettleStepException;
    
	/**
     *  Call this to cancel trailing database queries (too long running, etc)
	 */
    public void cancelQueries() throws KettleDatabaseException;
    
    /**
     * Default a step doesn't use any arguments.
     * Implement this to notify the GUI that a window has to be displayed BEFORE launching a transformation.
     * You can also use this to specify certain Environment variable values.
     * 
     * @return A Map of argument values. (name and optionally a default value)
     *         Put 10 values in the map for the possible 10 arguments.
     */
    public Map<String, String> getUsedArguments();
    
    /**
     * The natural way of data flow in a transformation is source-to-target.
     * However, this makes mapping to target tables difficult to do.
     * To help out here, we supply information to the transformation meta-data model about which fields are required for a step.
     * This allows us to automate certain tasks like the mapping to pre-defined tables.
     * The Table Output step in this case will output the fields in the target table using this method. 
     * 
     * @param space the variable space to reference
     * @return the required fields for this steps metadata.
     * @throws KettleException in case the required fields can't be determined.
     */
    public RowMetaInterface getRequiredFields(VariableSpace space) throws KettleException;
 
    /**
     * This method returns all the database connections that are used by the step.
     * @return an array of database connections meta-data.
     *         Return an empty array if no connections are used.
     */
    public DatabaseMeta[] getUsedDatabaseConnections();
    
    /**
     * @return the libraries that this step or plugin uses.
     */
    public String[] getUsedLibraries();
    
    /**
     * @return true if this step supports error "reporting" on rows: the ability to send rows to a certain target step.
     */
    public boolean supportsErrorHandling();
    
    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @param transMeta
     * @param stepMeta
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepMeta);
    
    /**
     * @param space the variable space to use 
     * @param definitions
     * @param resourceNamingInterface
     * @param repository The repository to optionally load other resources from (to be converted to XML) 
     * 
     * @return the filename of the exported resource
     */
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException ;
    
    /**
     * @return The StepMeta object to which this metadata class belongs. 
     * With this, we can see to which transformation metadata (etc) this metadata pertains to. (hierarchy) 
     */
    public StepMeta getParentStepMeta();
    
    /**
     * Provide original lineage for this metadata object
     * @param parentStepMeta the parent step metadata container object
     */
    public void setParentStepMeta(StepMeta parentStepMeta);
    
    /**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta();

    /**
     * @return The list of optional input streams.  
     * It allows the user to select from a list of possible actions like "New target step" 
     */
    public List<StreamInterface> getOptionalStreams();

    /**
     * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
     * @param stream The optional stream to handle.
     */
	public void handleStreamSelection(StreamInterface stream);
	
	/**
	 * For steps that handle dynamic input (info) or output (target) streams, it is useful to be able to force the recreate the StepIoMeta definition.
	 * By default this definition is cached. 
	 */
	public void resetStepIoMeta();

	/**
	 * Change step names into step objects to allow them to be name-changed etc.
	 * @param steps the steps to reference
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps);
	
	/**
	 * @return Optional interface that allows an external program to inject step metadata in a standardized fasion.
	 * This method will return null if the interface is not available for this step.
	 */
	public StepMetaInjectionInterface getStepMetaInjectionInterface();
	
	/**
	 * @return The supported transformation types that this step supports.
	 */
	public TransformationType[] getSupportedTransformationTypes();
	
	 /**
   * @return True if the job entry defines one or more references to a repository object.
   */
  public boolean hasRepositoryReferences();

  /**
   * Look up the references after import
   * @param repository the repository to reference.
   */
  public void lookupRepositoryReferences(Repository repository) throws KettleException;
  
  
  public void setChanged();
  public boolean hasChanged();
}