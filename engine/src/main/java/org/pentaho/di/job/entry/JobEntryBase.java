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

package org.pentaho.di.job.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ExtensionDataInterface;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Base class for the different types of job-entries. Job entries can extend this base class to get access to common
 * member variables and default method behavior. However, JobEntryBase does not implement JobEntryInterface (although it
 * implements most of the same methods), so individual job entry classes must implement JobEntryInterface and
 * specifically the <code>execute()</code> method.
 *
 * @author Matt Created on 18-jun-04
 *
 */
public class JobEntryBase implements Cloneable, VariableSpace, CheckResultSourceInterface,
  ResourceHolderInterface, LoggingObjectInterface, AttributesInterface, ExtensionDataInterface {

  /** The name of the job entry */
  private String name;

  /** The description of the job entry */
  private String description;

  /**
   * ID as defined in the xml or annotation.
   */
  private String configId;

  /** Whether the job entry has changed. */
  private boolean changed;

  /** The object id for the job entry. Should be unique in most cases. Used to distinguish
   * Logging channels for objects. */
  private ObjectId id;

  /** The variable bindings for the job entry */
  protected VariableSpace variables = new Variables();

  /** The repository */
  protected Repository rep;

  /** The parent job */
  protected Job parentJob;

  /** The log channel interface object, used for logging */
  protected LogChannelInterface log;

  /** The log level */
  private LogLevel logLevel = DefaultLogLevel.getLogLevel();

  /** The container object id */
  protected String containerObjectId;

  protected IMetaStore metaStore;

  protected Map<String, Map<String, String>> attributesMap;

  protected Map<String, Object> extensionDataMap;

  protected JobMeta parentJobMeta;

  /**
   * Instantiates a new job entry base object.
   */
  public JobEntryBase() {
    name = null;
    description = null;
    log = new LogChannel( this );
    attributesMap = new HashMap<String, Map<String, String>>();
    extensionDataMap = new HashMap<String, Object>();
  }

  /**
   * Instantiates a new job entry base object with the given name and description.
   *
   * @param name
   *          the name of the job entry
   * @param description
   *          the description of the job entry
   */
  public JobEntryBase( String name, String description ) {
    setName( name );
    setDescription( description );
    setObjectId( null );
    log = new LogChannel( this );
    attributesMap = new HashMap<String, Map<String, String>>();
    extensionDataMap = new HashMap<String, Object>();
  }

  /**
   * Checks if the JobEntry object is equal to the specified object
   *
   * @return true if the two objects are equal, false otherwise
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof JobEntryBase ) ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }

    return name.equalsIgnoreCase( ( (JobEntryBase) obj ).getName() );
  }

  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }

  /**
   * Clears all variable values
   */
  public void clear() {
    name = null;
    description = null;
    changed = false;
  }

  /**
   * Sets the object id.
   *
   * @param id
   *          the new object id
   */
  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  /**
   * Sets the id for the job entry
   *
   * @param id
   *          the new id
   */
  public void setID( long id ) {
    this.id = new LongObjectId( id );
  }

  /**
   * Gets the object id
   *
   * @return the object id
   * @see org.pentaho.di.core.CheckResultSourceInterface#getObjectId()
   */
  @Override
  public ObjectId getObjectId() {
    return id;
  }

  /**
   * Gets the plug-in type description
   *
   * @return the plug-in type description
   */
  public String getTypeDesc() {
    PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId( JobEntryPluginType.class, configId );
    return plugin.getDescription();
  }

  /**
   * Sets the name of the job entry
   *
   * @param name
   *          the new name
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Gets the name of the job entry
   *
   * @return the name of the job entry
   * @see org.pentaho.di.core.CheckResultSourceInterface#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the description for the job entry.
   *
   * @param Description
   *          the new description
   */
  public void setDescription( String Description ) {
    this.description = Description;
  }

  /**
   * Gets the description of the job entry
   *
   * @return the description of the job entry
   * @see org.pentaho.di.core.CheckResultSourceInterface#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Sets that the job entry has changed (i.e. a call to setChanged(true))
   *
   * @see JobEntryBase#setChanged(boolean)
   */
  public void setChanged() {
    setChanged( true );
  }

  /**
   * Sets whether the job entry has changed
   *
   * @param ch
   *          true if the job entry has changed, false otherwise
   */
  public void setChanged( boolean ch ) {
    changed = ch;
  }

  /**
   * Checks whether the job entry has changed
   *
   * @return true if the job entry has changed, false otherwise
   */
  public boolean hasChanged() {
    return changed;
  }

  /**
   * Checks if the job entry has started
   *
   * @return true if the job entry has started, false otherwise
   */
  public boolean isStart() {
    return false;
  }

  /**
   * Checks if the job entry is a dummy entry
   *
   * @return true if the job entry is a dummy entry, false otherwise
   */
  public boolean isDummy() {
    return false;
  }

  /**
   * Checks if the job entry is an evaluation.
   *
   * @return true if the job entry is an evaluation, false otherwise
   */
  public boolean isEvaluation() {
    return true;
  }

  /**
   * Checks if the job entry executes a job
   *
   * @return true if the job entry executes a job, false otherwise
   */
  public boolean isJob() {
    return "JOB".equals( configId );
  }

  /**
   * Checks if the job entry sends email
   *
   * @return true if the job entry sends email, false otherwise
   */
  public boolean isMail() {
    return "MAIL".equals( configId );
  }

  /**
   * Checks if the job entry executes a shell program
   *
   * @return true if the job entry executes a shell program, false otherwise
   */
  public boolean isShell() {
    return "SHELL".equals( configId );
  }

  /**
   * Checks if the job entry is of a special type (Start, Dummy, etc.)
   *
   * @return true if the job entry is of a special type, false otherwise
   */
  public boolean isSpecial() {
    return "SPECIAL".equals( configId );
  }

  /**
   * Checks if this job entry executes a transformation
   *
   * @return true if this job entry executes a transformation, false otherwise
   */
  public boolean isTransformation() {
    return "TRANS".equals( configId );
  }

  /**
   * Checks if this job entry performs an FTP operation
   *
   * @return true if this job entry performs an FTP operation, false otherwise
   */
  public boolean isFTP() {
    return "FTP".equals( configId );
  }

  /**
   * Checks if this job entry performs an SFTP operation
   *
   * @return true if this job entry performs an SFTP operation, false otherwise
   */
  public boolean isSFTP() {
    return "SFTP".equals( configId );
  }

  /**
   * Checks if this job entry performs an HTTP operation
   *
   * @return true if this job entry performs an HTTP operation, false otherwise
   */
  public boolean isHTTP() {
    return "HTTP".equals( configId );
  }

  // Add here for the new types?

  /**
   * This method is called by PDI whenever a job entry needs to serialize its settings to XML. It is called when saving
   * a job in Spoon. The method returns an XML string, containing the serialized settings. The string contains a series
   * of XML tags, typically one tag per setting. The helper class org.pentaho.di.core.xml.XMLHandler is typically used
   * to construct the XML string.
   *
   * @return the xml representation of the job entry
   */
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "description", getDescription() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "type", configId ) );

    retval.append( AttributesUtil.getAttributesXml( attributesMap ) );

    return retval.toString();
  }

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
   * @throws KettleXMLException
   *           if any errors occur during the loading of the XML
   */
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers ) throws KettleXMLException {
    try {
      setName( XMLHandler.getTagValue( entrynode, "name" ) );
      setDescription( XMLHandler.getTagValue( entrynode, "description" ) );

      // Load the attribute groups map
      //
      attributesMap = AttributesUtil.loadAttributes( XMLHandler.getSubNode( entrynode, AttributesUtil.XML_TAG ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load base info for job entry", e );
    }
  }

  /**
   * @deprecated use {@link #loadXML(Node, List, List, Repository, IMetaStore)}
   * @param entrynode
   * @param databases
   * @param slaveServers
   * @param repository
   * @throws KettleXMLException
   */
  @Deprecated
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository repository ) throws KettleXMLException {
    // Provided for compatibility with v4 code
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    // Provided for compatibility with v4 code
  }

  /**
   * Parses the repository objects. For JobEntryBase, this is a stub (empty) method
   *
   * @param rep
   *          the repository
   * @throws KettleException
   *           if any errors occur during parsing
   */
  public void parseRepositoryObjects( Repository rep ) throws KettleException {
  }

  /**
   * This method is called by PDI whenever a job entry needs to read its configuration from a PDI repository. For
   * JobEntryBase, this method performs no operations.
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
  public void loadRep( Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    // Nothing by default, provided for API and runtime compatibility against v4 code
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    // Nothing by default, provided for API and runtime compatibility against v4 code
  }

  /**
   * This method is called by PDI whenever a job entry needs to save its settings to a PDI repository. For JobEntryBase,
   * this method performs no operations.
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
  public void saveRep( Repository rep, ObjectId id_job ) throws KettleException {
    // Nothing by default, provided for API and runtime compatibility against v4 code
  }

  /**
   * @param rep
   * @param metaStore
   * @param id_job
   * @throws KettleException
   */
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    // Nothing by default, provided for API and runtime compatibility against v4 code
  }

  /**
   * This method is called when a job entry is duplicated in Spoon. It needs to return a deep copy of this job entry
   * object. It is essential that the implementing class creates proper deep copies if the job entry configuration is
   * stored in modifiable objects, such as lists or custom helper objects.
   *
   * @return a clone of the object
   */
  @Override
  public Object clone() {
    JobEntryBase je;
    try {
      je = (JobEntryBase) super.clone();
    } catch ( CloneNotSupportedException cnse ) {
      return null;
    }
    return je;
  }

  /**
   * Returns a string representation of the object. For JobEntryBase, this method returns the name
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * Checks whether a reset of the number of errors is required before execution.
   *
   * @return true if a reset of the number of errors is required before execution, false otherwise
   */
  public boolean resetErrorsBeforeExecution() {
    return true;
  }

  /**
   * This method must return true if the job entry supports the true/false outgoing hops. For JobEntryBase, this method
   * always returns false
   *
   * @return false
   */
  public boolean evaluates() {
    return false;
  }

  /**
   * This method must return true if the job entry supports the unconditional outgoing hop. For JobEntryBase, this
   * method always returns true
   *
   * @return true
   */
  public boolean isUnconditional() {
    return true;
  }

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
  public List<SQLStatement> getSQLStatements( Repository repository ) throws KettleException {
    return new ArrayList<SQLStatement>();
  }

  /**
   * Gets the SQL statements needed by this job entry to execute successfully, given a set of variables. For
   * JobEntryBase, this method returns an empty list.
   *
   * @deprecated use {@link #getSQLStatements(Repository, IMetaStore, VariableSpace)}
   *
   * @param repository
   *          the repository object
   * @param space
   *          a variable space object containing variable bindings
   * @return an empty list
   * @throws KettleException
   *           if any errors occur during the generation of SQL statements
   */
  @Deprecated
  public List<SQLStatement> getSQLStatements( Repository repository, VariableSpace space ) throws KettleException {
    return new ArrayList<SQLStatement>();
  }

  /**
   * Gets the SQL statements needed by this job entry to execute successfully, given a set of variables. For
   * JobEntryBase, this method returns an empty list.
   *
   * @param repository
   *          the repository object
   * @param space
   *          a variable space object containing variable bindings
   * @return an empty list
   * @throws KettleException
   *           if any errors occur during the generation of SQL statements
   */
  public List<SQLStatement> getSQLStatements( Repository repository, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return new ArrayList<SQLStatement>();
  }

  /**
   * Gets the filename of the job entry. For JobEntryBase, this method always returns null
   *
   * @return null
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getFilename()
   */
  @Override
  public String getFilename() {
    return null;
  }

  /**
   * Gets the real filename of the job entry, by substituting any environment variables present in the filename. For
   * JobEntryBase, this method always returns null
   *
   * @return null
   */
  public String getRealFilename() {
    return null;
  }

  /**
   * Gets all the database connections that are used by the job entry. For JobEntryBase, this method returns an empty
   * (non-null) array
   *
   * @return an empty (non-null) array
   */
  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] {};
  }

  /**
   * Copies variables from a given variable space to this job entry
   *
   * @see org.pentaho.di.core.variables.VariableSpace#copyVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  /**
   * Substitutes any variable values into the given string, and returns the resolved string
   *
   * @return the string with any environment variables resolved and substituted
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String)
   */
  @Override
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  /**
   * Substitutes any variable values into each of the given strings, and returns an array containing the resolved
   * string(s)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String[])
   */
  @Override
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  /**
   * Gets the parent variable space
   *
   * @return the parent variable space
   * @see org.pentaho.di.core.variables.VariableSpace#getParentVariableSpace()
   */
  @Override
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  /**
   * Sets the parent variable space
   *
   * @see org.pentaho.di.core.variables.VariableSpace#setParentVariableSpace(
   *   org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  /**
   * Gets the value of the specified variable, or returns a default value if no such variable exists
   *
   * @return the value of the specified variable, or returns a default value if no such variable exists
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String, java.lang.String)
   */
  @Override
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  /**
   * Gets the value of the specified variable, or returns a default value if no such variable exists
   *
   * @return the value of the specified variable, or returns a default value if no such variable exists
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String)
   */
  @Override
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  /**
   * Returns a boolean representation of the specified variable after performing any necessary substitution. Truth
   * values include case-insensitive versions of "Y", "YES", "TRUE" or "1".
   *
   * @param variableName
   *          the name of the variable to interrogate
   * @boolean defaultValue the value to use if the specified variable is unassigned.
   * @return a boolean representation of the specified variable after performing any necessary substitution
   * @see org.pentaho.di.core.variables.VariableSpace#getBooleanValueOfVariable(java.lang.String, boolean)
   */
  @Override
  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        return ValueMetaString.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  /**
   * Sets the values of the job entry's variables to the values from the parent variables
   *
   * @see org.pentaho.di.core.variables.VariableSpace#initializeVariablesFrom(
   *   org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  /**
   * Gets a list of variable names for the job entry
   *
   * @return a list of variable names
   * @see org.pentaho.di.core.variables.VariableSpace#listVariables()
   */
  @Override
  public String[] listVariables() {
    return variables.listVariables();
  }

  /**
   * Sets the value of the specified variable to the specified value
   *
   * @see org.pentaho.di.core.variables.VariableSpace#setVariable(java.lang.String, java.lang.String)
   */
  @Override
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  /**
   * Shares a variable space from another variable space. This means that the object should take over the space used as
   * argument.
   *
   * @see org.pentaho.di.core.variables.VariableSpace#shareVariablesWith(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  /**
   * Injects variables using the given Map. The behavior should be that the properties object will be stored and at the
   * time the VariableSpace is initialized (or upon calling this method if the space is already initialized). After
   * injecting the link of the properties object should be removed.
   *
   * @see org.pentaho.di.core.variables.VariableSpace#injectVariables(java.util.Map)
   */
  @Override
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  /**
   * Support for overrides not having to put in a check method. For JobEntryBase, this method performs no operations.
   *
   * @param remarks
   *          CheckResults from checking the job entry
   * @param jobMeta
   *          JobMeta information letting threading back to the JobMeta possible
   * @deprecated use {@link #check(List, JobMeta, VariableSpace, Repository, IMetaStore)}
   */
  @Deprecated
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta ) {
  }

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
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

  }

  /**
   * Gets a list of all the resource dependencies that the step is depending on. In JobEntryBase, this method returns an
   * empty resource dependency list.
   *
   * @return an empty list of ResourceReferences
   * @see ResourceReference
   */
  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    return new ArrayList<ResourceReference>( 5 ); // default: return an empty resource dependency list. Lower the
                                                  // initial capacity
  }

  /**
   * Exports the object to a flat-file system, adding content with filename keys to a set of definitions. For
   * JobEntryBase, this method simply returns null
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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository ) throws KettleException {
    return null;
  }

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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    return null;
  }

  /**
   * Gets the plugin id.
   *
   * @return the plugin id
   */
  public String getPluginId() {
    return configId;
  }

  /**
   * Sets the plugin id.
   *
   * @param configId
   *          the new plugin id
   */
  public void setPluginId( String configId ) {
    this.configId = configId;
  }

  /**
   * Gets the plugin id.
   *
   * @deprecated use {@link #getPluginId()}
   */
  @Override
  @Deprecated
  public String getTypeId() {
    return getPluginId();
  }

  /**
   * Sets the plugin id.
   *
   * @deprecated use {@link #setPluginId(String)}
   */
  @Deprecated
  public void setTypeId( String typeId ) {
    setPluginId( typeId );
  }

  /**
   * This returns the expected name for the dialog that edits a job entry. The expected name is in the org.pentaho.di.ui
   * tree and has a class name that is the name of the job entry with 'Dialog' added to the end.
   *
   * e.g. if the job entry is org.pentaho.di.job.entries.zipfile.JobEntryZipFile the dialog would be
   * org.pentaho.di.ui.job.entries.zipfile.JobEntryZipFileDialog
   *
   * If the dialog class for a job entry does not match this pattern it should override this method and return the
   * appropriate class name
   *
   * @return full class name of the dialog
   */
  public String getDialogClassName() {
    String className = getClass().getCanonicalName();
    className = className.replaceFirst( "\\.di\\.", ".di.ui." );
    className += "Dialog";
    return className;
  }

  /**
   * Returns the holder type for the job entry
   *
   * @return the holder type for the job entry
   * @see org.pentaho.di.resource.ResourceHolderInterface#getHolderType()
   */
  @Override
  public String getHolderType() {
    return "JOBENTRY";
  }

  /**
   * Gets the variable bindings for the job entry.
   *
   * @return the variable bindings for the job entry.
   */
  protected VariableSpace getVariables() {
    return variables;
  }

  /**
   * Sets the repository for the job entry.
   *
   * @param repository
   *          the repository
   */
  public void setRepository( Repository repository ) {
    this.rep = repository;
  }

  /**
   * Gets the repository for the job entry.
   *
   * @return the repository
   */
  public Repository getRepository() {
    return rep;
  }

  /**
   * Sets the parent job.
   *
   * @param parentJob
   *          the new parent job
   */
  public void setParentJob( Job parentJob ) {
    this.parentJob = parentJob;
    this.logLevel = parentJob.getLogLevel();
    this.log = new LogChannel( this, parentJob );
    this.containerObjectId = parentJob.getContainerObjectId();
  }

  /**
   * Gets the parent job.
   *
   * @return the parent job
   */
  public Job getParentJob() {
    return parentJob;
  }

  /**
   * Checks if the logging level is basic.
   *
   * @return true if the logging level is basic, false otherwise
   */
  public boolean isBasic() {
    return log.isBasic();
  }

  /**
   * Checks if the logging level is detailed.
   *
   * @return true if the logging level is detailed, false otherwise
   */
  public boolean isDetailed() {
    return log.isDetailed();
  }

  /**
   * Checks if the logging level is debug.
   *
   * @return true if the logging level is debug, false otherwise
   */
  public boolean isDebug() {
    return log.isDebug();
  }

  /**
   * Checks if the logging level is rowlevel.
   *
   * @return true if the logging level is rowlevel, false otherwise
   */
  public boolean isRowlevel() {
    return log.isRowLevel();
  }

  /**
   * Logs the specified string at the minimal level.
   *
   * @param message
   *          the message
   */
  public void logMinimal( String message ) {
    log.logMinimal( message );
  }

  /**
   * Logs the specified string and arguments at the minimal level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logMinimal( String message, Object... arguments ) {
    log.logMinimal( message, arguments );
  }

  /**
   * Logs the specified string at the basic level.
   *
   * @param message
   *          the message
   */
  public void logBasic( String message ) {
    log.logBasic( message );
  }

  /**
   * Logs the specified string and arguments at the basic level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logBasic( String message, Object... arguments ) {
    log.logBasic( message, arguments );
  }

  /**
   * Logs the specified string at the detailed level.
   *
   * @param message
   *          the message
   */
  public void logDetailed( String message ) {
    log.logDetailed( message );
  }

  /**
   * Logs the specified string and arguments at the detailed level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logDetailed( String message, Object... arguments ) {
    log.logDetailed( message, arguments );
  }

  /**
   * Logs the specified string at the debug level.
   *
   * @param message
   *          the message
   */
  public void logDebug( String message ) {
    log.logDebug( message );
  }

  /**
   * Logs the specified string and arguments at the debug level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logDebug( String message, Object... arguments ) {
    log.logDebug( message, arguments );
  }

  /**
   * Logs the specified string at the row level.
   *
   * @param message
   *          the message
   */
  public void logRowlevel( String message ) {
    log.logRowlevel( message );
  }

  /**
   * Logs the specified string and arguments at the row level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logRowlevel( String message, Object... arguments ) {
    log.logRowlevel( message, arguments );
  }

  /**
   * Logs the specified string at the error level.
   *
   * @param message
   *          the message
   */
  public void logError( String message ) {
    log.logError( message );
  }

  /**
   * Logs the specified string and Throwable object at the error level.
   *
   * @param message
   *          the message
   * @param e
   *          the e
   */
  public void logError( String message, Throwable e ) {
    log.logError( message, e );
  }

  /**
   * Logs the specified string and arguments at the error level.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logError( String message, Object... arguments ) {
    log.logError( message, arguments );
  }

  /**
   * Gets the log channel.
   *
   * @return the log channel
   */
  public LogChannelInterface getLogChannel() {
    return log;
  }

  /**
   * Gets the logging channel id
   *
   * @return the log channel id
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogChannelId()
   */
  @Override
  public String getLogChannelId() {
    return log.getLogChannelId();
  }

  /**
   * Gets the object name
   *
   * @return the object name
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectName()
   */
  @Override
  public String getObjectName() {
    return getName();
  }

  /**
   * Gets a string identifying a copy in a series of steps
   *
   * @return a string identifying a copy in a series of steps
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectCopy()
   */
  @Override
  public String getObjectCopy() {
    return null;
  }

  /**
   * Gets the revision of the object with respect to a repository
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectRevision()
   */
  @Override
  public ObjectRevision getObjectRevision() {
    return null;
  }

  /**
   * Gets the logging object type
   *
   * @return the logging object type
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectType()
   */
  @Override
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.JOBENTRY;
  }

  /**
   * Gets the logging object interface's parent
   *
   * @return the logging object interface's parent
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getParent()
   */
  @Override
  public LoggingObjectInterface getParent() {
    return parentJob;
  }

  /**
   * Gets the directory of the job entry in the repository. For JobEntryBase, this returns null
   *
   * @return null
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getRepositoryDirectory()
   */
  @Override
  public RepositoryDirectory getRepositoryDirectory() {
    return null;
  }

  /**
   * Gets the logging level for the job entry
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogLevel()
   */
  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the logging level for the job entry
   *
   * @param logLevel
   *          the new log level
   */
  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
    log.setLogLevel( logLevel );
  }

  /**
   * Gets the container object id
   *
   * @return the container object id
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * Sets the container object id
   *
   * @param containerObjectId
   *          the container object id to set
   */
  public void setContainerObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Returns the registration date for the job entry. For JobEntryBase, this method always returns null
   *
   * @return null
   */
  @Override
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * Checks whether the job entry has repository references. For JobEntryBase, this method always returns false
   *
   * @return false
   */
  public boolean hasRepositoryReferences() {
    return false;
  }

  /**
   * Looks up the references after import
   *
   * @param repository
   *          the repository to reference.
   * @throws KettleException
   *           if any errors occur during the lookup
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
  }

  /**
   * @return The objects referenced in the step, like a a transformation, a job, a mapper, a reducer, a combiner, ...
   */
  public String[] getReferencedObjectDescriptions() {
    return null;
  }

  /**
   * @return true for each referenced object that is enabled or has a valid reference definition.
   */
  public boolean[] isReferencedObjectEnabled() {
    return null;
  }

  /**
   * Load the referenced object
   *
   * @deprecated use {@link #loadReferencedObject(int, Repository, IMetaStore, VariableSpace)}
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  @Deprecated
  public Object loadReferencedObject( int index, Repository rep, VariableSpace space ) throws KettleException {
    return null;
  }

  /**
   * Load the referenced object
   *
   * @param index
   *          the referenced object index to load (in case there are multiple references)
   * @param rep
   *          the repository
   * @param metaStore
   *          the metaStore to load from
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return null;
  }

  @Override
  public boolean isGatheringMetrics() {
    return log != null && log.isGatheringMetrics();
  }

  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    if ( log != null ) {
      log.setGatheringMetrics( gatheringMetrics );
    }
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return log != null && log.isForcingSeparateLogging();
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    if ( log != null ) {
      log.setForcingSeparateLogging( forcingSeparateLogging );
    }
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  @Override
  public void setAttributesMap( Map<String, Map<String, String>> attributesMap ) {
    this.attributesMap = attributesMap;
  }

  @Override
  public Map<String, Map<String, String>> getAttributesMap() {
    return attributesMap;
  }

  @Override
  public void setAttribute( String groupName, String key, String value ) {
    Map<String, String> attributes = getAttributes( groupName );
    if ( attributes == null ) {
      attributes = new HashMap<String, String>();
      attributesMap.put( groupName, attributes );
    }
    attributes.put( key, value );
  }

  @Override
  public void setAttributes( String groupName, Map<String, String> attributes ) {
    attributesMap.put( groupName, attributes );
  }

  @Override
  public Map<String, String> getAttributes( String groupName ) {
    return attributesMap.get( groupName );
  }

  @Override
  public String getAttribute( String groupName, String key ) {
    Map<String, String> attributes = attributesMap.get( groupName );
    if ( attributes == null ) {
      return null;
    }
    return attributes.get( key );
  }

  @Override
  public Map<String, Object> getExtensionDataMap() {
    return extensionDataMap;
  }

  /**
   * @return The parent jobMeta at save and during execution.
   */
  public JobMeta getParentJobMeta() {
    return parentJobMeta;
  }

  /**
   * At save and run time, the system will attempt to set the jobMeta so that it can be accessed by the jobEntries
   * if necessary.
   *
   * @param parentJobMeta the JobMeta to which this JobEntryInterface belongs
   */
  public void setParentJobMeta( JobMeta parentJobMeta ) {
    this.parentJobMeta = parentJobMeta;
  }
}
