/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This interface allows custom steps to talk to Kettle. The StepMetaInterface is the main Java interface that a plugin
 * implements. The responsibilities of the implementing class are listed below:
 * <p>
 * <ul>
 * <li><b>Keep track of the step settings</b></br> The implementing class typically keeps track of step settings using
 * private fields with corresponding getters and setters. The dialog class implementing StepDialogInterface is using the
 * getters and setters to copy the user supplied configuration in and out of the dialog.
 * <p>
 * The following interface methods also fall into the area of maintaining settings:
 * <p>
 * <i><a href="#setDefault()">void setDefault()</a></i>
 * <p>
 * This method is called every time a new step is created and should allocate or set the step configuration to sensible
 * defaults. The values set here will be used by Spoon when a new step is created. This is often a good place to ensure
 * that the step&#8217;s settings are initialized to non-null values. Null values can be cumbersome to deal with in
 * serialization and dialog population, so most PDI step implementations stick to non-null values for all step settings.
 * <p>
 * <i><a href="#clone()">public Object clone()</a></i>
 * <p>
 * This method is called when a step is duplicated in Spoon. It needs to return a deep copy of this step meta object. It
 * is essential that the implementing class creates proper deep copies if the step configuration is stored in modifiable
 * objects, such as lists or custom helper objects. See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone()
 * for an example on creating a deep copy.
 * <p></li>
 * <li>
 * <b>Serialize step settings</b><br/>
 * The plugin needs to be able to serialize its settings to both XML and a PDI repository. The interface methods are as
 * follows.
 * <p>
 * <i><a href="#getXML()">public String getXML()</a></i>
 * <p>
 * This method is called by PDI whenever a step needs to serialize its settings to XML. It is called when saving a
 * transformation in Spoon. The method returns an XML string, containing the serialized step settings. The string
 * contains a series of XML tags, typically one tag per setting. The helper class org.pentaho.di.core.xml.XMLHandler is
 * typically used to construct the XML string.
 * <p>
 * <i><a href="#loadXML(org.w3c.dom.Node, java.util.List, java.util.Map)">public void loadXML(...)</a></i>
 * <p>
 * This method is called by PDI whenever a step needs to read its settings from XML. The XML node containing the step's
 * settings is passed in as an argument. Again, the helper class org.pentaho.di.core.xml.XMLHandler is typically used to
 * conveniently read the step settings from the XML node.
 * <p>
 * <i><a href=
 * "#saveRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId,
 *   org.pentaho.di.repository.ObjectId)"
 * >public void saveRep(...)</a></i>
 * <p>
 * This method is called by PDI whenever a step needs to save its settings to a PDI repository. The repository object
 * passed in as the first argument provides a convenient set of methods for serializing step settings. The
 * transformation id and step id passed in should be used by the step as identifiers when calling the repository
 * serialization methods.
 * <p>
 * <i><a href=
 * "#readRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId, java.util.List, java.util.Map)"
 * >public void readRep(...)</a></i>
 * <p>
 * This method is called by PDI whenever a step needs to read its configuration from a PDI repository. The step id given
 * in the arguments should be used as the identifier when using the repositories serialization methods.
 * <p></li>
 * <li>
 * <b>Provide instances of other plugin classes</b><br/>
 * The StepMetaInterface plugin class is the main class tying in with the rest of PDI architecture. It is responsible
 * for supplying instances of the other plugin classes implementing StepDialogInterface, StepInterface and
 * StepDataInterface. The following methods cover these responsibilities. Each of the method&#8217;s implementation is
 * typically constructing a new instance of the corresponding class forwarding the passed in arguments to the
 * constructor. The methods are as follows.
 * <p>
 * public StepDialogInterface getDialog(...)<br/>
 * public StepInterface getStep(...)<br/>
 * public StepDataInterface getStepData()<br/>
 * <p>
 * Each of the above methods returns a new instance of the plugin class implementing StepDialogInterface, StepInterface
 * and StepDataInterface.</li>
 * <li>
 * <b>Report the step&#8217;s changes to the row stream</b> PDI needs to know how a step affects the row structure. A
 * step may be adding or removing fields, as well as modifying the metadata of a field. The method implementing this
 * aspect of a step plugin is getFields().
 * <p>
 * <i><a href= "#getFields(org.pentaho.di.core.row.RowMetaInterface, java.lang.String,
 * org.pentaho.di.core.row.RowMetaInterface[], org.pentaho.di.trans.step.StepMeta,
 * org.pentaho.di.core.variables.VariableSpace)" >public void getFields(...)</a></i>
 * <p>
 * Given a description of the input rows, the plugin needs to modify it to match the structure for its output fields.
 * The implementation modifies the passed in RowMetaInterface object to reflect any changes to the row stream. Typically
 * a step adds fields to the row structure, which is done by creating ValueMeta objects (PDI&#8217;s default
 * implementation of ValueMetaInterface), and appending them to the RowMetaInterface object. The section Working with
 * Fields goes into deeper detail on ValueMetaInterface.</li>
 * <li>
 * <b>Validate step settings</b><br>
 * Spoon supports a &#8220;validate transformation&#8221; feature, which triggers a self-check of all steps. PDI invokes
 * the check() method of each step on the canvas allowing each step to validate its settings.
 * <p>
 * <i><a href= "#check(java.util.List, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.step.StepMeta,
 * org.pentaho.di.core.row.RowMetaInterface, java.lang.String[], java.lang.String[],
 * org.pentaho.di.core.row.RowMetaInterface)" >public void check()</a></i>
 * <p>
 * Each step has the opportunity to validate its settings and verify that the configuration given by the user is
 * reasonable. In addition to that a step typically checks if it is connected to preceding or following steps, if the
 * nature of the step requires that kind of connection. An input step may expect to not have a preceding step for
 * example. The check method passes in a list of check remarks that the method should append its validation results to.
 * Spoon then displays the list of remarks collected from the steps, allowing the user to take corrective action in case
 * of validation warnings or errors.</li>
 * Given a description of the input rows, the plugin needs to modify it to match the structure for its output fields.
 * The implementation modifies the passed in RowMetaInterface object to reflect any changes to the row stream. Typically
 * a step adds fields to the row structure, which is done by creating ValueMeta objects (PDI&#8217;s default
 * implementation of ValueMetaInterface), and appending them to the RowMetaInterface object. The section Working with
 * Fields goes into deeper detail on ValueMetaInterface.
 * </ul>
 *
 * @since 4-aug-2004
 * @author Matt
 */

public interface StepMetaInterface {
  /**
   * Set default values
   */
  public void setDefault();

  /**
   * Gets the fields.
   *
   * @param inputRowMeta
   *          the input row meta that is modified in this method to reflect the output row metadata of the step
   * @param name
   *          Name of the step to use as input for the origin field in the values
   * @param info
   *          Fields used as extra lookup information
   * @param nextStep
   *          the next step that is targeted
   * @param space
   *          the space The variable space to use to replace variables
   * @throws KettleStepException
   *           the kettle step exception
   * @deprecated use {@link #getFields(RowMetaInterface, String, RowMetaInterface[], StepMeta, VariableSpace, Repository, IMetaStore)}
   */
  @Deprecated
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space ) throws KettleStepException;

  /**
   * Gets the fields.
   *
   * @param inputRowMeta
   *          the input row meta that is modified in this method to reflect the output row metadata of the step
   * @param name
   *          Name of the step to use as input for the origin field in the values
   * @param info
   *          Fields used as extra lookup information
   * @param nextStep
   *          the next step that is targeted
   * @param space
   *          the space The variable space to use to replace variables
   * @param repository
   *          the repository to use to load Kettle metadata objects impacting the output fields
   * @param metaStore
   *          the MetaStore to use to load additional external data or metadata impacting the output fields
   * @throws KettleStepException
   *           the kettle step exception
   */
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException;

  /**
   * Get the XML that represents the values in this step
   *
   * @return the XML that represents the metadata in this step
   * @throws KettleException
   *           in case there is a conversion or XML encoding error
   */
  public String getXML() throws KettleException;

  /**
   * Load the values for this step from an XML Node
   *
   * @param stepnode
   *          the Node to get the info from
   * @param databases
   *          The available list of databases to reference to
   * @param counters
   *          Counters to reference.
   * @throws KettleXMLException
   *           When an unexpected XML error occurred. (malformed etc.)
   * @deprecated use {@link #loadXML(Node, List, IMetaStore)}
   */
  @Deprecated
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters ) throws KettleXMLException;

  /**
   * Load the values for this step from an XML Node
   *
   * @param stepnode
   *          the Node to get the info from
   * @param databases
   *          The available list of databases to reference to
   * @param metaStore
   *          the metastore to optionally load external reference metadata from
   * @throws KettleXMLException
   *           When an unexpected XML error occurred. (malformed etc.)
   */
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException;

  /**
   * Save the steps data into a Kettle repository
   *
   * @param rep
   *          The Kettle repository to save to
   * @param id_transformation
   *          The transformation ID
   * @param id_step
   *          The step ID
   * @throws KettleException
   *           When an unexpected error occurred (database, network, etc)
   * @deprecated use {@link #saveRep(Repository, IMetaStore, ObjectId, ObjectId)}
   */
  @Deprecated
  public void saveRep( Repository rep, ObjectId id_transformation, ObjectId id_step ) throws KettleException;

  /**
   * Save the steps data into a Kettle repository
   *
   * @param rep
   *          The Kettle repository to save to
   * @param metaStore
   *          the metaStore to optionally write to
   * @param id_transformation
   *          The transformation ID
   * @param id_step
   *          The step ID
   * @throws KettleException
   *           When an unexpected error occurred (database, network, etc)
   */
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException;

  /**
   * Read the steps information from a Kettle repository
   *
   * @param rep
   *          The repository to read from
   * @param id_step
   *          The step ID
   * @param databases
   *          The databases to reference
   * @param counters
   *          The counters to reference
   * @throws KettleException
   *           When an unexpected error occurred (database, network, etc)
   * @deprecated use {@link #readRep(Repository, IMetaStore, ObjectId, List)}
   */
  @Deprecated
  public void readRep( Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
    Map<String, Counter> counters ) throws KettleException;

  /**
   * Read the steps information from a Kettle repository
   *
   * @param rep
   *          The repository to read from
   * @param metaStore
   *          The MetaStore to read external information from
   * @param id_step
   *          The step ID
   * @param databases
   *          The databases to reference
   * @throws KettleException
   *           When an unexpected error occurred (database, network, etc)
   */
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException;

  /**
   * Checks the settings of this step and puts the findings in a remarks List.
   *
   * @param remarks
   *          The list to put the remarks in @see org.pentaho.di.core.CheckResult
   * @param stepMeta
   *          The stepMeta to help checking
   * @param prev
   *          The fields coming from the previous step
   * @param input
   *          The input step names
   * @param output
   *          The output step names
   * @param info
   *          The fields that are used as information by the step
   * @deprecated use {@link #check(List, TransMeta, StepMeta, RowMetaInterface, String[], String[], RowMetaInterface, VariableSpace, Repository, IMetaStore)}
   */
  @Deprecated
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info );

  /**
   * Checks the settings of this step and puts the findings in a remarks List.
   *
   * @param remarks
   *          The list to put the remarks in @see org.pentaho.di.core.CheckResult
   * @param stepMeta
   *          The stepMeta to help checking
   * @param prev
   *          The fields coming from the previous step
   * @param input
   *          The input step names
   * @param output
   *          The output step names
   * @param info
   *          The fields that are used as information by the step
   * @param space
   *          the variable space to resolve variable expressions with
   * @param repository
   *          the repository to use to load Kettle metadata objects impacting the output fields
   * @param metaStore
   *          the MetaStore to use to load additional external data or metadata impacting the output fields
   */
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore );

  /**
   * Make an exact copy of this step, make sure to explicitly copy Collections etc.
   *
   * @return an exact copy of this step
   */
  public Object clone();

  /**
   * @return The fields used by this step, this is being used for the Impact analyses.
   */
  public RowMetaInterface getTableFields();

  /**
   * This method is added to exclude certain steps from layout checking.
   *
   * @since 2.5.0
   */
  public boolean excludeFromRowLayoutVerification();

  /**
   * This method is added to exclude certain steps from copy/distribute checking.
   *
   * @since 4.0.0
   */
  public boolean excludeFromCopyDistributeVerification();

  /**
   * Get the name of the class that implements the dialog for this job entry JobEntryBase provides a default
   */
  public String getDialogClassName();

  /**
   * Get the executing step, needed by Trans to launch a step.
   *
   * @param stepMeta
   *          The step info
   * @param stepDataInterface
   *          the step data interface linked to this step. Here the step can store temporary data, database connections,
   *          etc.
   * @param copyNr
   *          The copy nr to get
   * @param transMeta
   *          The transformation info
   * @param trans
   *          The launching transformation
   */
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans );

  /**
   * Get a new instance of the appropriate data class. This data class implements the StepDataInterface. It basically
   * contains the persisting data that needs to live on, even if a worker thread is terminated.
   *
   * @return The appropriate StepDataInterface class.
   */
  public StepDataInterface getStepData();

  /**
   * Each step must be able to report on the impact it has on a database, table field, etc.
   *
   * @param impact
   *          The list of impacts @see org.pentaho.di.transMeta.DatabaseImpact
   * @param transMeta
   *          The transformation information
   * @param stepMeta
   *          The step information
   * @param prev
   *          The fields entering this step
   * @param input
   *          The previous step names
   * @param output
   *          The output step names
   * @param info
   *          The fields used as information by this step
   * @deprecated use {@link #analyseImpact(List, TransMeta, StepMeta, RowMetaInterface, String[], String[], RowMetaInterface, Repository, IMetaStore)}
   */
  @Deprecated
  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info ) throws KettleStepException;

  /**
   * Each step must be able to report on the impact it has on a database, table field, etc.
   *
   * @param impact
   *          The list of impacts @see org.pentaho.di.transMeta.DatabaseImpact
   * @param transMeta
   *          The transformation information
   * @param stepMeta
   *          The step information
   * @param prev
   *          The fields entering this step
   * @param input
   *          The previous step names
   * @param output
   *          The output step names
   * @param info
   *          The fields used as information by this step
   * @param repository
   *          the repository to use to load Kettle metadata objects impacting the output fields
   * @param metaStore
   *          the MetaStore to use to load additional external data or metadata impacting the output fields
   */
  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException;

  /**
   * Standard method to return an SQLStatement object with SQL statements that the step needs in order to work
   * correctly. This can mean "create table", "create index" statements but also "alter table ... add/drop/modify"
   * statements.
   *
   * @return The SQL Statements for this step. If nothing has to be done, the SQLStatement.getSQL() == null. @see
   *         SQLStatement
   * @param transMeta
   *          TransInfo object containing the complete transformation
   * @param stepMeta
   *          StepMeta object containing the complete step
   * @param prev
   *          Row containing meta-data for the input fields (no data)
   * @deprecated use {@link #getSQLStatements(TransMeta, StepMeta, RowMetaInterface, Repository, IMetaStore)}
   */
  @Deprecated
  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev ) throws KettleStepException;

  /**
   * Standard method to return an SQLStatement object with SQL statements that the step needs in order to work
   * correctly. This can mean "create table", "create index" statements but also "alter table ... add/drop/modify"
   * statements.
   *
   * @return The SQL Statements for this step. If nothing has to be done, the SQLStatement.getSQL() == null. @see
   *         SQLStatement
   * @param transMeta
   *          TransInfo object containing the complete transformation
   * @param stepMeta
   *          StepMeta object containing the complete step
   * @param prev
   *          Row containing meta-data for the input fields (no data)
   * @param repository
   *          the repository to use to load Kettle metadata objects impacting the output fields
   * @param metaStore
   *          the MetaStore to use to load additional external data or metadata impacting the output fields
   */
  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) throws KettleStepException;

  /**
   * Call this to cancel trailing database queries (too long running, etc)
   */
  public void cancelQueries() throws KettleDatabaseException;

  /**
   * Default a step doesn't use any arguments. Implement this to notify the GUI that a window has to be displayed BEFORE
   * launching a transformation. You can also use this to specify certain Environment variable values.
   *
   * @return A Map of argument values. (name and optionally a default value) Put 10 values in the map for the possible
   *         10 arguments.
   */
  public Map<String, String> getUsedArguments();

  /**
   * The natural way of data flow in a transformation is source-to-target. However, this makes mapping to target tables
   * difficult to do. To help out here, we supply information to the transformation meta-data model about which fields
   * are required for a step. This allows us to automate certain tasks like the mapping to pre-defined tables. The Table
   * Output step in this case will output the fields in the target table using this method.
   *
   * @param space
   *          the variable space to reference
   * @return the required fields for this steps metadata.
   * @throws KettleException
   *           in case the required fields can't be determined.
   */
  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException;

  /**
   * This method returns all the database connections that are used by the step.
   *
   * @return an array of database connections meta-data. Return an empty array if no connections are used.
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
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepMeta );

  /**
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   *
   * @return the filename of the exported resource
   * @deprecated use {@link #exportResources(VariableSpace, Map, ResourceNamingInterface, Repository, IMetaStore)}
   */
  @Deprecated
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository ) throws KettleException;

  /**
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException;

  /**
   * @return The StepMeta object to which this metadata class belongs. With this, we can see to which transformation
   *         metadata (etc) this metadata pertains to. (hierarchy)
   */
  public StepMeta getParentStepMeta();

  /**
   * Provide original lineage for this metadata object
   *
   * @param parentStepMeta
   *          the parent step metadata container object
   */
  public void setParentStepMeta( StepMeta parentStepMeta );

  /**
   * Returns the Input/Output metadata for this step.
   */
  public StepIOMetaInterface getStepIOMeta();

  /**
   * @return The list of optional input streams. It allows the user to select f rom a list of possible actions like
   *         "New target step"
   */
  public List<StreamInterface> getOptionalStreams();

  /**
   * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
   *
   * @param stream
   *          The optional stream to handle.
   */
  public void handleStreamSelection( StreamInterface stream );

  /**
   * For steps that handle dynamic input (info) or output (target) streams, it is useful to be able to force the
   * recreate the StepIoMeta definition. By default this definition is cached.
   */
  public void resetStepIoMeta();

  /**
   * Change step names into step objects to allow them to be name-changed etc.
   *
   * @param steps
   *          the steps to reference
   */
  public void searchInfoAndTargetSteps( List<StepMeta> steps );

  /**
   * @return Optional interface that allows an external program to inject step metadata in a standardized fasion. This
   *         method will return null if the interface is not available for this step.
   * @deprecated Use annotation-based injection instead
   */
  @Deprecated
  public StepMetaInjectionInterface getStepMetaInjectionInterface();

  /**
   * @return The step metadata itself, not the metadata description.
   * For lists it will have 0 entries in case there are no entries.
   * @throws KettleException
   */
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException;

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
   *
   * @param repository
   *          the repository to reference.
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException;

  public void setChanged();

  public boolean hasChanged();

  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ...
   */
  public String[] getReferencedObjectDescriptions();

  /**
   * @return true for each referenced object that is enabled or has a valid reference definition.
   */
  public boolean[] isReferencedObjectEnabled();

  /**
   * @return A description of the active referenced object in a transformation.
   * Null if nothing special needs to be done or if the active metadata isn't different from design.
   */
  public String getActiveReferencedObjectDescription();

  /**
   * Load the referenced object
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param metaStore
   *          the MetaStore to use
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException;

  /**
   * Action remove hop from this step
   * @return step was changed
   */
  public default boolean cleanAfterHopFromRemove() {
    return false;
  }

  /**
   * True if the step passes it's result data straight to the servlet output. See exposing Kettle data over a web service
   * <a href="http://wiki.pentaho.com/display/EAI/PDI+data+over+web+services">http://wiki.pentaho.com/display/EAI/PDI+data+over+web+services</a>
   *
   * @return True if the step passes it's result data straight to the servlet output, false otherwise
   */
  default boolean passDataToServletOutput() {
    return false;
  }
}
