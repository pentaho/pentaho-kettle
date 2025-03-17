/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * JobEntryInterface is the main Java interface that a plugin implements. The responsibilities of the implementing class
 * are listed below:
 * <p>
 * <ul>
 * <li><b>Maintain job entry settings</b><br/>
 * The implementing class typically keeps track of job entry settings using private fields with corresponding getters
 * and setters. The dialog class implementing JobEntryDialogInterface is using the getters and setters to copy the user
 * supplied configuration in and out of the dialog.<br/>
 * <br/>
 * The following interface method also falls into the area of maintaining settings:<br/>
 * <br/>
 * <a href="#clone()"><code>Object clone()</code></a> <br/>
 * This method is called when a job entry is duplicated in Spoon. It needs to return a deep copy of this job entry
 * object. It is essential that the implementing class creates proper deep copies if the job entry configuration is
 * stored in modifiable objects, such as lists or custom helper objects. This interface does not extend Cloneable, but
 * the implementing class will provide a similar method due to this interface.</li><br/>
 * <li><b>Serialize job entry settings</b><br>
 * The plugin needs to be able to serialize its settings to both XML and a PDI repository. The interface methods are as
 * follows:<br/>
 * <br/>
 * <a href="#getXML()"><code>String getXML()</code></a><br/>
 * This method is called by PDI whenever a job entry needs to serialize its settings to XML. It is called when saving a
 * job in Spoon. The method returns an XML string, containing the serialized settings. The string contains a series of
 * XML tags, typically one tag per setting. The helper class org.pentaho.di.core.xml.XMLHandler is typically used to
 * construct the XML string.<br/>
 * <br/>
 * <a href="#loadXML(org.w3c.dom.Node, java.util.List, java.util.List, org.pentaho.di.repository.Repository)">
 * <code>void loadXML(...)</code></a></br> This method is called by PDI whenever a job entry needs to read its
 * settings from XML. The XML node containing the job entry's settings is passed in as an argument. Again, the helper
 * class org.pentaho.di.core.xml.XMLHandler is typically used to conveniently read the settings from the XML node.<br/>
 * <br/>
 * <a href="#saveRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId)">
 * <code>void saveRep(...)</code></a><br/>
 * This method is called by PDI whenever a job entry needs to save its settings to a PDI repository. The repository
 * object passed in as the first argument provides a convenient set of methods for serializing job entry settings. When
 * calling repository serialization methods, job id and job entry id are required. The job id is passed in to saveRep()
 * as an argument, and the job entry id can be obtained by a call to getObjectId() inherited from the base class.<br/>
 * <br/>
 * <a href=
 * "#loadRep(org.pentaho.di.repository.Repository, org.pentaho.di.repository.ObjectId, java.util.List,
 * java.util.List)">
 * <code>void loadRep(...)</code></a><br/>
 * This method is called by PDI whenever a job entry needs to read its configuration from a PDI repository. The job
 * entry id given in the arguments should be used as the identifier when using the repository's serialization methods.
 * <br/>
 * <br/>
 * <quote>Hint: When developing plugins, make sure the serialization code is in synch with the settings available from
 * the job entry dialog. When testing a plugin in Spoon, PDI will internally first save and load a copy of the
 * job.</quote></li><br/>
 * <li><b>Provide access to dialog class</b><br/>
 * PDI needs to know which class will take care of the settings dialog for the job entry. The interface method
 * getDialogClassName() must return the name of the class implementing the JobEntryDialogInterface.</li></br>
 * <li><b>Provide information about possible outcomes</b><br/>
 * A job entry may support up to three types of outgoing hops: true, false, and unconditional. Sometimes it does not
 * make sense to support all three possibilities. For instance, if the job entry performs a task that does not produce a
 * boolean outcome, like the dummy job entry, it may make sense to suppress the true and false outgoing hops. There are
 * other job entries, which carry an inherent boolean outcome, like the "file exists" job entry for instance. It may
 * make sense in such cases to suppress the unconditional outgoing hop.<br/>
 * <br/>
 * The job entry plugin class must implement two methods to indicate to PDI which outgoing hops it supports:<br/>
 * <br/>
 * <a href="#evaluates()"><code>boolean evaluates()</code></a><br/>
 * This method must return true if the job entry supports the true/false outgoing hops. If the job entry does not
 * support distinct outcomes, it must return false.<br/>
 * <br/>
 * <a href="#isUnconditional()"><code>boolean isUnconditional()</code></a><br/>
 * This method must return true if the job entry supports the unconditional outgoing hop. If the job entry does not
 * support the unconditional hop, it must return false.</li><br/>
 * <li><b>Execute a job entry task</b><br/>
 * The class implementing JobEntryInterface executes the actual job entry task by implementing the following method:
 * <br/>
 * <br/>
 * <a href="#execute(org.pentaho.di.core.Result, int)"><code>Result execute(..)</code></a><br/>
 * The execute() method is going to be called by PDI when it is time for the job entry to execute its logic. The
 * arguments are a result object, which is passed in from the previously executed job entry and an integer number
 * indicating the distance of the job entry from the start entry of the job.<br/>
 * <br/>
 * The job entry should execute its configured task, and report back on the outcome. A job entry does that by calling
 * certain methods on the passed in <a href="../../core/Result.html">Result</a> object:<br/>
 * <br/>
 * <code>prev_result.setNrErrors(..)</code><br/>
 * The job entry needs to indicate whether it has encountered any errors during execution. If there are errors,
 * setNrErrors must be called with the number of errors encountered (typically this is 1). If there are no errors,
 * setNrErrors must be called with an argument of 0.<br/>
 * <br/>
 * <code>prev_result.setResult(..)</code><br/>
 * The job entry must indicate the outcome of the task. This value determines which output hops can be followed next. If
 * a job entry does not support evaluation, it need not call prev_result.setResult().<br/>
 * <br/>
 * Finally, the passed in prev_result object must be returned.</li>
 * </ul>
 * </p>
 *
 * @author Matt Casters
 * @since 18-06-04
 *
 */

public interface JobEntryInterface {

  String ACTION_STATUS = "actionStatus";
  String ERROR_DETAILS = "errorDetails";
  String SUCCESS_RESPONSE = "Action successful";
  String FAILURE_RESPONSE = "Action failed";
  String FAILURE_METHOD_NOT_RESPONSE = "Action failed with method not found";
  String STATUS = "Status";
  int SUCCESS_STATUS = 1;
  int FAILURE_STATUS = -1;
  int NOT_EXECUTED_STATUS = 0;

  /**
   * Execute the job entry. The previous result and number of rows are provided to the method for the purpose of
   * chaining job entries, transformations, etc.
   *
   * @param prev_result
   *          the previous result
   * @param nr
   *          the number of rows
   * @return the Result object from execution of this job entry
   * @throws KettleException
   *           if any Kettle exceptions occur
   */
  Result execute( Result prev_result, int nr ) throws KettleException;

  /**
   * Sets the parent job.
   *
   * @param job
   *          the parent job
   */
  void setParentJob( Job job );

  /**
   * Gets the parent job.
   *
   * @return the parent job
   */
  Job getParentJob();

  /**
   * Gets the log channel.
   *
   * @return the log channel
   */
  LogChannelInterface getLogChannel();

  /**
   * Sets the repository.
   *
   * @param repository
   *          the new repository
   */
  void setRepository( Repository repository );

  /**
   * Sets the MetaStore
   *
   * @param metaStore
   *          The new MetaStore to use
   */
  void setMetaStore( IMetaStore metaStore );

  /**
   * This method should clear out any variables, objects, etc. used by the job entry.
   */
  void clear();

  /**
   * Gets the object id.
   *
   * @return the object id
   */
  ObjectId getObjectId();

  /**
   * Sets the object id.
   *
   * @param id
   *          the new object id
   */
  void setObjectId( ObjectId id );

  /**
   * Gets the name of this job entry.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the name for this job entry.
   *
   * @param name
   *          the new name
   */
  void setName( String name );

  /**
   * Gets the plugin ID
   *
   * deprecated in favor of getPluginId()
   *
   * @see JobEntryInterface#getPluginId()
   */
  @Deprecated
  String getTypeId();

  /**
   * Gets the plugin id.
   *
   * @return the plugin id
   */
  String getPluginId();

  /**
   * Sets the plugin id.
   *
   * @param pluginId
   *          the new plugin id
   */
  void setPluginId( String pluginId );

  /**
   * Gets the description of this job entry
   *
   * @return the description
   */
  String getDescription();

  /**
   * Sets the description of this job entry
   *
   * @param description
   *          the new description
   */
  void setDescription( String description );

  /**
   * Sets whether the job entry has changed
   */
  void setChanged();

  /**
   * Sets whether the job entry has changed
   *
   * @param ch
   *          true if the job entry has changed, false otherwise
   */
  void setChanged( boolean ch );

  /**
   * Checks whether the job entry has changed
   *
   * @return true if whether the job entry has changed
   */
  boolean hasChanged();

  /**
   * This method is called by PDI whenever a job entry needs to read its settings from XML. The XML node containing the
   * job entry's settings is passed in as an argument. Again, the helper class org.pentaho.di.core.xml.XMLHandler is
   * typically used to conveniently read the settings from the XML node.
   *
   * @deprecated use {@link #loadXML(Node, List, List, Repository, IMetaStore)}
   *
   * @param entrynode
   *          the top-level XML node
   * @param databases
   *          the list of databases
   * @param slaveServers
   *          the list of slave servers
   * @param rep
   *          the repository object
   * @throws KettleXMLException
   *           if any errors occur during the loading of the XML
   */
  @Deprecated
  void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep ) throws KettleXMLException;

  /**
   * This method is called by PDI whenever a job entry needs to read its settings from XML. The XML node containing the
   * job entry's settings is passed in as an argument. Again, the helper class org.pentaho.di.core.xml.XMLHandler is
   * typically used to conveniently read the settings from the XML node.
   *
   * @param entrynode
   *          the top-level XML node
   * @param databases
   *          the list of databases
   * @param slaveServers
   *          the list of slave servers
   * @param rep
   *          the repository object
   * @param metaStore
   *          The metaStore to optionally load from.
   * @throws KettleXMLException
   *           if any errors occur during the loading of the XML
   */
  void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException;

  /**
   * This method is called by PDI whenever a job entry needs to serialize its settings to XML. It is called when saving
   * a job in Spoon. The method returns an XML string, containing the serialized settings. The string contains a series
   * of XML tags, typically one tag per setting. The helper class org.pentaho.di.core.xml.XMLHandler is typically used
   * to construct the XML string.
   *
   * @return the xml representation of the job entry
   */
  String getXML();

  /**
   * This method is called by PDI whenever a job entry needs to save its settings to a PDI repository. The repository
   * object passed in as the first argument provides a convenient set of methods for serializing job entry settings.
   * When calling repository serialization methods, job id and job entry id are required. The job id is passed in to
   * saveRep() as an argument, and the job entry id can be obtained by a call to getObjectId() inherited from the base
   * class.
   *
   * @deprecated use {@link #saveRep(Repository, IMetaStore, ObjectId)}
   *
   * @param rep
   *          the repository object
   * @param id_job
   *          the id_job
   * @throws KettleException
   *           if any errors occur during the save
   */
  @Deprecated
  void saveRep( Repository rep, ObjectId id_job ) throws KettleException;

  /**
   * This method is called by PDI whenever a job entry needs to save its settings to a PDI repository. The repository
   * object passed in as the first argument provides a convenient set of methods for serializing job entry settings.
   * When calling repository serialization methods, job id and job entry id are required. The job id is passed in to
   * saveRep() as an argument, and the job entry id can be obtained by a call to getObjectId() inherited from the base
   * class.
   *
   * @param rep
   *          the repository
   * @param metaStore
   *          the MetaStore to use
   * @param id_job
   *          the id_job
   * @throws KettleException
   *           if any errors occur during the save
   */
  void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException;

  /**
   * This method is called by PDI whenever a job entry needs to read its configuration from a PDI repository. The job
   * entry id given in the arguments should be used as the identifier when using the repository's serialization methods.
   *
   * @deprecated use {@link #loadRep(Repository, IMetaStore, ObjectId, List, List)}
   *
   * @param rep
   *          the repository object
   * @param id_jobentry
   *          the id of the job entry
   * @param databases
   *          the list of databases
   * @param slaveServers
   *          the list of slave servers
   * @throws KettleException
   *           if any errors occur during the load
   */
  @Deprecated
  void loadRep( Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException;

  /**
   * This method is called by PDI whenever a job entry needs to read its configuration from a PDI repository. The job
   * entry id given in the arguments should be used as the identifier when using the repository's serialization methods.
   *
   * @param rep
   *          the repository object
   * @param metaStore
   *          the MetaStore to use
   * @param id_jobentry
   *          the id of the job entry
   * @param databases
   *          the list of databases
   * @param slaveServers
   *          the list of slave servers
   * @throws KettleException
   *           if any errors occur during the load
   */
  void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException;

  /**
   * Checks if the job entry has started
   *
   * @return true if started, false otherwise
   */
  boolean isStart();

  /**
   * Checks if this job entry is a dummy entry
   *
   * @return true if this job entry is a dummy entry, false otherwise
   */
  boolean isDummy();

  /**
   * This method is called when a job entry is duplicated in Spoon. It needs to return a deep copy of this job entry
   * object. It is essential that the implementing class creates proper deep copies if the job entry configuration is
   * stored in modifiable objects, such as lists or custom helper objects.
   *
   * @return a clone of the object
   */
  Object clone();

  /**
   * Checks whether a reset of the number of errors is required before execution.
   *
   * @return true if a reset of the number of errors is required before execution, false otherwise
   */
  boolean resetErrorsBeforeExecution();

  /**
   * This method must return true if the job entry supports the true/false outgoing hops. If the job entry does not
   * support distinct outcomes, it must return false.
   *
   * @return true if the job entry supports the true/false outgoing hops, false otherwise
   */
  boolean evaluates();

  /**
   * This method must return true if the job entry supports the unconditional outgoing hop. If the job entry does not
   * support the unconditional hop, it must return false.
   *
   * @return true if the job entry supports the unconditional outgoing hop, false otherwise
   */
  boolean isUnconditional();

  /**
   * Checks if the job entry is an evaluation
   *
   * @return true if the job entry is an evaluation, false otherwise
   */
  boolean isEvaluation();

  /**
   * Checks if this job entry executes a transformation
   *
   * @return true if this job entry executes a transformation, false otherwise
   */
  boolean isTransformation();

  /**
   * Checks if the job entry executes a job
   *
   * @return true if the job entry executes a job, false otherwise
   */
  boolean isJob();

  /**
   * Checks if the job entry executes a shell program
   *
   * @return true if the job entry executes a shell program, false otherwise
   */
  boolean isShell();

  /**
   * Checks if the job entry sends email
   *
   * @return true if the job entry sends email, false otherwise
   */
  boolean isMail();

  /**
   * Checks if the job entry is of a special type (Start, Dummy, etc.)
   *
   * @return true if the job entry is of a special type, false otherwise
   */
  boolean isSpecial();

  /**
   * Gets the SQL statements needed by this job entry to execute successfully.
   *
   * @deprecated use {@link #getSQLStatements(Repository, IMetaStore, VariableSpace)}
   *
   * @param repository
   *          the repository
   * @return a list of SQL statements
   * @throws KettleException
   *           if any errors occur during the generation of SQL statements
   */
  @Deprecated
  List<SQLStatement> getSQLStatements( Repository repository ) throws KettleException;

  /**
   * Gets the SQL statements needed by this job entry to execute successfully, given a set of variables.
   *
   * @deprecated use {@link #getSQLStatements(Repository, IMetaStore, VariableSpace)}
   *
   * @param repository
   *          the repository object
   * @param space
   *          a variable space object containing variable bindings
   * @return a list of SQL statements
   * @throws KettleException
   *           if any errors occur during the generation of SQL statements
   */
  @Deprecated
  List<SQLStatement> getSQLStatements( Repository repository, VariableSpace space ) throws KettleException;

  /**
   * Gets the SQL statements needed by this job entry to execute successfully, given a set of variables.
   *
   * @param repository
   *          the repository object
   * @param metaStore
   *          the MetaStore to use
   * @param space
   *          a variable space object containing variable bindings
   * @return a list of SQL statements
   * @throws KettleException
   *           if any errors occur during the generation of SQL statements
   */
  List<SQLStatement> getSQLStatements( Repository repository, IMetaStore metaStore, VariableSpace space ) throws KettleException;

  /**
   * Get the name of the class that implements the dialog for the job entry JobEntryBase provides a default
   *
   * @return the name of the class implementing the dialog for the job entry
   *
   * @deprecated  As of release 8.1, use annotated-based dialog instead {@see org.pentaho.di.core.annotations.PluginDialog}
   */
  @Deprecated
  String getDialogClassName();

  /**
   * Gets the filename of the job entry. This method is used by job entries and transformations that call or refer to
   * other job entries.
   *
   * @return the filename
   */
  String getFilename();

  /**
   * Gets the real filename of the job entry, by substituting any environment variables present in the filename.
   *
   * @return the real (resolved) filename for the job entry
   */
  String getRealFilename();

  /**
   * This method returns all the database connections that are used by the job entry.
   *
   * @return an array of database connections meta-data, which is empty if no connections are used.
   */
  DatabaseMeta[] getUsedDatabaseConnections();

  /**
   * Allows JobEntry objects to check themselves for consistency
   *
   * @param remarks
   *          List of CheckResult objects indicating consistency status
   * @param jobMeta
   *          the metadata object for the job entry
   * @deprecated use {@link #check(List, JobMeta, VariableSpace, Repository, IMetaStore)}
   */
  @Deprecated
  void check( List<CheckResultInterface> remarks, JobMeta jobMeta );

  /**
   * Allows JobEntry objects to check themselves for consistency
   *
   * @param remarks
   *          List of CheckResult objects indicating consistency status
   * @param jobMeta
   *          the metadata object for the job entry
   * @param space
   *          the variable space to resolve string expressions with variables with
   * @param repository
   *          the repository to load Kettle objects from
   * @param metaStore
   *          the MetaStore to load common elements from
   */
  void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore );

  /**
   * Get a list of all the resource dependencies that the step is depending on.
   *
   * @return a list of all the resource dependencies that the step is depending on
   */
  List<ResourceReference> getResourceDependencies( JobMeta jobMeta );

  /**
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. The supplied
   * resource naming interface allows the object to name appropriately without worrying about those parts of the
   * implementation specific details.
   *
   * @param space
   *          The variable space to resolve (environment) variables with.
   * @param definitions
   *          The map containing the filenames and content
   * @param namingInterface
   *          The resource naming interface allows the object to be named appropriately
   * @param repository
   *          The repository to load resources from
   *
   * @return The filename for this object. (also contained in the definitions map)
   * @throws KettleException
   *           in case something goes wrong during the export
   * @deprecated use {@link #exportResources(VariableSpace, Map, ResourceNamingInterface, Repository, IMetaStore)}
   */
  @Deprecated
  String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository ) throws KettleException;

  /**
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. The supplied
   * resource naming interface allows the object to name appropriately without worrying about those parts of the
   * implementation specific details.
   *
   * @param space
   *          The variable space to resolve (environment) variables with.
   * @param definitions
   *          The map containing the filenames and content
   * @param namingInterface
   *          The resource naming interface allows the object to be named appropriately
   * @param repository
   *          The repository to load resources from
   * @param metaStore
   *          the metaStore to load external metadata from
   *
   * @return The filename for this object. (also contained in the definitions map)
   * @throws KettleException
   *           in case something goes wrong during the export
   */
  String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore ) throws KettleException;

  /**
   * Checks whether the job entry defines one or more references to a repository object
   *
   * @return true if the job entry defines one or more references to a repository object, false otherwise
   */
  boolean hasRepositoryReferences();

  /**
   * Look up the references after import
   *
   * @param repository
   *          the repository to reference.
   * @throws KettleException
   *           if any errors occur during the lookup
   */
  void lookupRepositoryReferences( Repository repository ) throws KettleException;

  /**
   * @return The objects referenced in the step, like a a transformation, a job, a mapper, a reducer, a combiner, ...
   */
  String[] getReferencedObjectDescriptions();

  /**
   * @return true for each referenced object that is enabled or has a valid reference definition.
   */
  boolean[] isReferencedObjectEnabled();

  /**
   * Load the referenced object
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   * @deprecated use {@link #loadReferencedObject(int, Repository, IMetaStore, VariableSpace)}
   */
  @Deprecated
  Object loadReferencedObject( int index, Repository rep, VariableSpace space ) throws KettleException;

  /**
   * Load the referenced object
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param metaStore
   *          the metaStore
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException;

  /**
   * At save and run time, the system will attempt to set the jobMeta so that it can be accessed by the jobEntries
   * if necessary.  This default method will be applied to all JobEntries that do not need to be aware of the parent
   * JobMeta
   *
   * @param jobMeta the JobMeta to which this JobEntryInterface belongs
   */
  default void setParentJobMeta( JobMeta jobMeta ) {
  }

  /**
   * Return Gets the parent jobMeta.  This default method will throw an exception if a job entry attempts to call the
   * getter when not implemented.
   */
  default JobMeta getParentJobMeta( ) {
    throw new UnsupportedOperationException( "Attemped access of getJobMet not supported by JobEntryInterface implementation" );
  }

  /**
   * Dynamically invokes a method in the JobEntry class based on the `fieldName` parameter using java reflection
   *
   * @param fieldName         the name of the field to be used to determine the method to invoke
   * @param jobEntryInterface the job entry interface
   * @param jobMeta           the job metadata
   * @param job               the job
   * @param queryParams       the query parameters to be passed to the invoked method
   * @return a `JSONObject` containing the response of the invoked method and the action status
   */
  default JSONObject doAction( String fieldName, JobEntryInterface jobEntryInterface, JobMeta jobMeta,
                               Job job, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = this.getClass().getDeclaredMethod( fieldName + "Action", Map.class );
      actionMethod.setAccessible( true );
      this.setRepository( job.getRep() );
      response = (JSONObject) actionMethod.invoke( this, queryParams );
      response.put( JobEntryInterface.ACTION_STATUS, JobEntryInterface.SUCCESS_RESPONSE );

    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {
      if ( ex.getCause() instanceof KettleException ) {
        response.put( JobEntryInterface.ACTION_STATUS, JobEntryInterface.FAILURE_RESPONSE );
      } else {
        response.put( JobEntryInterface.ACTION_STATUS, JobEntryInterface.FAILURE_METHOD_NOT_RESPONSE );
      }
      getLogChannel().logError( ex.getMessage() );
    }
    return response;
  }

}
