/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.KettleAttribute;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class is responsible for implementing common functionality regarding step meta, such as logging. All Kettle
 * steps have an extension of this where private fields have been added with public accessors.
 * <p>
 * For example, the "Text File Output" step's TextFileOutputMeta class extends BaseStepMeta by adding fields for the
 * output file name, compression, file format, etc...
 * <p>
 *
 * @created 19-June-2003
 */
public class BaseStepMeta implements Cloneable, StepAttributesInterface {
  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Step metadata", LoggingObjectType.STEPMETA, null );

  public static final String STEP_ATTRIBUTES_FILE = "step-attributes.xml";

  private boolean changed;

  /** database connection object to use for searching fields & checking steps */
  protected Database[] databases;

  /** The repository that is being used for this step */
  protected Repository repository;

  protected StepMeta parentStepMeta;

  protected StepIOMetaInterface ioMeta;

  /**
   * Instantiates a new base step meta.
   */
  public BaseStepMeta() {
    changed = false;

    try {
      loadStepAttributes();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    try {
      BaseStepMeta retval = (BaseStepMeta) super.clone();

      // PDI-15799: Makes a copy of the StepMeta. This copy can be used within the same Transformation.
      // That means than inner step references are copied rather then cloned.
      // If the copy is acquired for another Transformation (e.g. this method is called from Transformation.clone() )
      // then the step references must be corrected.
      if ( ioMeta != null ) {
        StepIOMetaInterface stepIOMeta = new StepIOMeta( ioMeta.isInputAcceptor(), ioMeta.isOutputProducer(), ioMeta.isInputOptional(), ioMeta.isSortedDataRequired(), ioMeta.isInputDynamic(), ioMeta.isOutputDynamic() );

        List<StreamInterface> infoStreams = ioMeta.getInfoStreams();
        for ( StreamInterface infoStream : infoStreams ) {
          stepIOMeta.addStream( new Stream( infoStream ) );
        }

        List<StreamInterface> targetStreams = ioMeta.getTargetStreams();
        for ( StreamInterface targetStream : targetStreams ) {
          stepIOMeta.addStream( new Stream( targetStream ) );
        }

        retval.ioMeta = stepIOMeta;
      }

      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * Sets the changed.
   *
   * @param ch
   *          the new changed
   */
  public void setChanged( boolean ch ) {
    changed = ch;
  }

  /**
   * Sets the changed.
   */
  public void setChanged() {
    changed = true;
  }

  /**
   * Checks for changed.
   *
   * @return true, if successful
   */
  public boolean hasChanged() {
    return changed;
  }

  /**
   * Gets the table fields.
   *
   * @return the table fields
   */
  public RowMetaInterface getTableFields() {
    return null;
  }

  /**
   * Produces the XML string that describes this step's information.
   *
   * @return String containing the XML describing this step.
   * @throws KettleException
   *           in case there is an XML conversion or encoding error
   */
  public String getXML() throws KettleException {
    String retval = "";

    return retval;
  }

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
    VariableSpace space ) throws KettleStepException {
    // Default: no values are added to the row in the step
  }

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
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: no values are added to the row in the step
  }

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
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info ) throws KettleStepException {

  }

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
    IMetaStore metaStore ) throws KettleStepException {

  }

  /**
   * Standard method to return one or more SQLStatement objects that the step needs in order to work correctly. This can
   * mean "create table", "create index" statements but also "alter table ... add/drop/modify" statements.
   *
   * @return The SQL Statements for this step or null if an error occurred. If nothing has to be done, the
   *         SQLStatement.getSQL() == null.
   * @param transMeta
   *          TransInfo object containing the complete transformation
   * @param stepMeta
   *          StepMeta object containing the complete step
   * @param prev
   *          Row containing meta-data for the input fields (no data)
   * @deprecated use {@link #getSQLStatements(TransMeta, StepMeta, RowMetaInterface, Repository, IMetaStore)}
   */
  @Deprecated
  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev ) throws KettleStepException {
    // default: this doesn't require any SQL statements to be executed!
    return new SQLStatement( stepMeta.getName(), null, null );
  }

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
    Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // default: this doesn't require any SQL statements to be executed!
    return new SQLStatement( stepMeta.getName(), null, null );
  }

  /**
   * Call this to cancel trailing database queries (too long running, etc)
   */
  public void cancelQueries() throws KettleDatabaseException {
    //
    // Cancel all defined queries...
    //
    if ( databases != null ) {
      for ( int i = 0; i < databases.length; i++ ) {
        if ( databases[i] != null ) {
          databases[i].cancelQuery();
        }
      }
    }
  }

  /**
   * Default a step doesn't use any arguments. Implement this to notify the GUI that a window has to be displayed BEFORE
   * launching a transformation.
   *
   * @return A row of argument values. (name and optionally a default value)
   */
  public Map<String, String> getUsedArguments() {
    return null;
  }

  /**
   * The natural way of data flow in a transformation is source-to-target. However, this makes mapping to target tables
   * difficult to do. To help out here, we supply information to the transformation meta-data model about which fields
   * are required for a step. This allows us to automate certain tasks like the mapping to pre-defined tables. The Table
   * Output step in this case will output the fields in the target table using this method.
   *
   * This default implementation returns an empty row meaning that no fields are required for this step to operate.
   *
   * @return the required fields for this steps meta data.
   * @throws KettleException
   *           in case the required fields can't be determined
   * @deprecated use {@link #getRequiredFields(VariableSpace)}
   */
  @Deprecated
  public RowMetaInterface getRequiredFields() throws KettleException {
    return new RowMeta();
  }

  /**
   * The natural way of data flow in a transformation is source-to-target. However, this makes mapping to target tables
   * difficult to do. To help out here, we supply information to the transformation meta-data model about which fields
   * are required for a step. This allows us to automate certain tasks like the mapping to pre-defined tables. The Table
   * Output step in this case will output the fields in the target table using this method.
   *
   * This default implementation returns an empty row meaning that no fields are required for this step to operate.
   *
   * @param space
   *          the variable space to use to do variable substitution.
   * @return the required fields for this steps meta data.
   * @throws KettleException
   *           in case the required fields can't be determined
   */
  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    return new RowMeta();
  }

  /**
   * This method returns all the database connections that are used by the step.
   *
   * @return an array of database connections meta-data. Return an empty array if no connections are used.
   */
  public DatabaseMeta[] getUsedDatabaseConnections() {
    return new DatabaseMeta[] {};
  }

  /**
   * @return the libraries that this step or plug-in uses.
   */
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  /**
   * @return true if this step supports error "reporting" on rows: the ability to send rows to a certain target step.
   */
  public boolean supportsErrorHandling() {
    return false;
  }

  /**
   * This method is added to exclude certain steps from layout checking.
   *
   * @since 2.5.0
   */
  public boolean excludeFromRowLayoutVerification() {
    return false;
  }

  /**
   * This method is added to exclude certain steps from copy/distribute checking.
   *
   * @since 4.0.0
   */
  public boolean excludeFromCopyDistributeVerification() {
    return false;
  }

  /**
   * Get a list of all the resource dependencies that the step is depending on.
   *
   * @return a list of all the resource dependencies that the step is depending on
   */
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 ); // Lower the initial capacity - unusual
                                                                                // to have more than 1 actually
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );
    return references;
  }

  /**
   * Export resources.
   *
   * @param space
   *          the space
   * @param definitions
   *          the definitions
   * @param resourceNamingInterface
   *          the resource naming interface
   * @param repository
   *          the repository
   * @return the string
   * @throws KettleException
   *           the kettle exception
   * @deprecated use {@link #exportResources(VariableSpace, Map, ResourceNamingInterface, Repository, IMetaStore)}
   */
  @Deprecated
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository ) throws KettleException {
    return null;
  }

  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    return null;
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
    if ( className.endsWith( "Meta" ) ) {
      className = className.substring( 0, className.length() - 4 );
    }
    className += "Dialog";
    return className;
  }

  /**
   * Gets the parent step meta.
   *
   * @return the parent step meta
   */
  public StepMeta getParentStepMeta() {
    return parentStepMeta;
  }

  /**
   * Sets the parent step meta.
   *
   * @param parentStepMeta
   *          the new parent step meta
   */
  public void setParentStepMeta( StepMeta parentStepMeta ) {
    this.parentStepMeta = parentStepMeta;
  }

  // TODO find a way to factor out these methods...
  //

  protected LogChannelInterface log;

  protected ArrayList<KettleAttributeInterface> attributes;

  // Late init to prevent us from logging blank step names, etc.
  /**
   * Gets the log.
   *
   * @return the log
   */
  public LogChannelInterface getLog() {
    if ( log == null ) {
      log = new LogChannel( this );
    }
    return log;
  }

  /**
   * Checks if is basic.
   *
   * @return true, if is basic
   */
  public boolean isBasic() {
    return getLog().isBasic();
  }

  /**
   * Checks if is detailed.
   *
   * @return true, if is detailed
   */
  public boolean isDetailed() {
    return getLog().isDetailed();
  }

  /**
   * Checks if is debug.
   *
   * @return true, if is debug
   */
  public boolean isDebug() {
    return getLog().isDebug();
  }

  /**
   * Checks if is row level.
   *
   * @return true, if is row level
   */
  public boolean isRowLevel() {
    return getLog().isRowLevel();
  }

  /**
   * Log minimal.
   *
   * @param message
   *          the message
   */
  public void logMinimal( String message ) {
    getLog().logMinimal( message );
  }

  /**
   * Log minimal.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logMinimal( String message, Object... arguments ) {
    getLog().logMinimal( message, arguments );
  }

  /**
   * Log basic.
   *
   * @param message
   *          the message
   */
  public void logBasic( String message ) {
    getLog().logBasic( message );
  }

  /**
   * Log basic.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logBasic( String message, Object... arguments ) {
    getLog().logBasic( message, arguments );
  }

  /**
   * Log detailed.
   *
   * @param message
   *          the message
   */
  public void logDetailed( String message ) {
    getLog().logDetailed( message );
  }

  /**
   * Log detailed.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logDetailed( String message, Object... arguments ) {
    getLog().logDetailed( message, arguments );
  }

  /**
   * Log debug.
   *
   * @param message
   *          the message
   */
  public void logDebug( String message ) {
    getLog().logDebug( message );
  }

  /**
   * Log debug.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logDebug( String message, Object... arguments ) {
    getLog().logDebug( message, arguments );
  }

  /**
   * Log rowlevel.
   *
   * @param message
   *          the message
   */
  public void logRowlevel( String message ) {
    getLog().logRowlevel( message );
  }

  /**
   * Log rowlevel.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logRowlevel( String message, Object... arguments ) {
    getLog().logRowlevel( message, arguments );
  }

  /**
   * Log error.
   *
   * @param message
   *          the message
   */
  public void logError( String message ) {
    getLog().logError( message );
  }

  /**
   * Log error.
   *
   * @param message
   *          the message
   * @param e
   *          the e
   */
  public void logError( String message, Throwable e ) {
    getLog().logError( message, e );
  }

  /**
   * Log error.
   *
   * @param message
   *          the message
   * @param arguments
   *          the arguments
   */
  public void logError( String message, Object... arguments ) {
    getLog().logError( message, arguments );
  }

  /**
   * Gets the log channel id.
   *
   * @return the log channel id
   */
  public String getLogChannelId() {
    return null;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return null;
  }

  /**
   * Gets the object copy.
   *
   * @return the object copy
   */
  public String getObjectCopy() {
    return null;
  }

  /**
   * Gets the object id.
   *
   * @return the object id
   */
  public ObjectId getObjectId() {
    return null;
  }

  /**
   * Gets the object revision.
   *
   * @return the object revision
   */
  public ObjectRevision getObjectRevision() {
    return null;
  }

  /**
   * Gets the object type.
   *
   * @return the object type
   */
  public LoggingObjectType getObjectType() {
    return null;
  }

  /**
   * Gets the parent.
   *
   * @return the parent
   */
  public LoggingObjectInterface getParent() {
    return null;
  }

  /**
   * Gets the repository directory.
   *
   * @return the repository directory
   */
  public RepositoryDirectory getRepositoryDirectory() {
    return null;
  }

  /**
   * Returns the Input/Output metadata for this step. By default, each step produces and accepts optional input.
   */
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {
      ioMeta = new StepIOMeta( true, true, true, false, false, false );
    }
    return ioMeta;
  }

  /**
   * @return The list of optional input streams. It allows the user to select from a list of possible actions like
   *         "New target step"
   */
  public List<StreamInterface> getOptionalStreams() {
    List<StreamInterface> list = new ArrayList<StreamInterface>();
    return list;
  }

  /**
   * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
   *
   * @param stream
   *          The optional stream to handle.
   */
  public void handleStreamSelection( StreamInterface stream ) {
  }

  /**
   * Reset step io meta.
   */
  public void resetStepIoMeta() {
    ioMeta = null;
  }

  /**
   * Change step names into step objects to allow them to be name-changed etc.
   *
   * @param steps
   *          the steps to reference
   */
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
  }

  /**
   * @return Optional interface that allows an external program to inject step metadata in a standardized fasion. This
   *         method will return null if the interface is not available for this step.
   */
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return null;
  }

  /**
   * @return The step metadata itself, not the metadata description.
   * For lists it will have 0 entries in case there are no entries.
   * @throws KettleException
   */
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  /**
   * Find parent entry.
   *
   * @param entries
   *          the entries
   * @param key
   *          the key
   * @return the step injection meta entry
   */
  protected StepInjectionMetaEntry findParentEntry( List<StepInjectionMetaEntry> entries, String key ) {
    for ( StepInjectionMetaEntry look : entries ) {
      if ( look.getKey().equals( key ) ) {
        return look;
      }
      StepInjectionMetaEntry check = findParentEntry( look.getDetails(), key );
      if ( check != null ) {
        return check;
      }
    }
    return null;
  }

  /**
   * Creates the entry.
   *
   * @param attr
   *          the attr
   * @param PKG
   *          the pkg
   * @return the step injection meta entry
   */
  protected StepInjectionMetaEntry createEntry( KettleAttributeInterface attr, Class<?> PKG ) {
    return new StepInjectionMetaEntry( attr.getKey(), attr.getType(), BaseMessages.getString( PKG, attr
      .getDescription() ) );
  }

  /**
   * Describe the metadata attributes that can be injected into this step metadata object.
   */
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries( Class<?> PKG ) {
    List<StepInjectionMetaEntry> entries = new ArrayList<StepInjectionMetaEntry>();

    for ( KettleAttributeInterface attr : attributes ) {
      if ( attr.getParent() == null ) {
        entries.add( createEntry( attr, PKG ) );
      } else {
        StepInjectionMetaEntry entry = createEntry( attr, PKG );
        StepInjectionMetaEntry parentEntry = findParentEntry( entries, attr.getParent().getKey() );
        if ( parentEntry == null ) {
          throw new RuntimeException(
            "An error was detected in the step attributes' definition: the parent was not found for attribute "
              + attr );
        }
        parentEntry.getDetails().add( entry );
      }
    }

    return entries;
  }

  /**
   * Load step attributes.
   *
   * @throws KettleException
   *           the kettle exception
   */
  protected void loadStepAttributes() throws KettleException {
    try ( InputStream inputStream = getClass().getResourceAsStream( STEP_ATTRIBUTES_FILE ) ) {
      if ( inputStream != null ) {
        Document document = XMLHandler.loadXMLFile( inputStream );
        Node attrsNode = XMLHandler.getSubNode( document, "attributes" );
        List<Node> nodes = XMLHandler.getNodes( attrsNode, "attribute" );
        attributes = new ArrayList<KettleAttributeInterface>();
        for ( Node node : nodes ) {
          String key = XMLHandler.getTagAttribute( node, "id" );
          String xmlCode = XMLHandler.getTagValue( node, "xmlcode" );
          String repCode = XMLHandler.getTagValue( node, "repcode" );
          String description = XMLHandler.getTagValue( node, "description" );
          String tooltip = XMLHandler.getTagValue( node, "tooltip" );
          int valueType = ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( node, "valuetype" ) );
          String parentId = XMLHandler.getTagValue( node, "parentid" );

          KettleAttribute attribute = new KettleAttribute( key, xmlCode, repCode, description, tooltip, valueType, findParent( attributes, parentId ) );
          attributes.add( attribute );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load file " + STEP_ATTRIBUTES_FILE, e );
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#findParent(java.util.List, java.lang.String)
   */
  @Override
  public KettleAttributeInterface findParent( List<KettleAttributeInterface> attributes, String parentId ) {
    if ( Utils.isEmpty( parentId ) ) {
      return null;
    }
    for ( KettleAttributeInterface attribute : attributes ) {
      if ( attribute.getKey().equals( parentId ) ) {
        return attribute;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#findAttribute(java.lang.String)
   */
  @Override
  public KettleAttributeInterface findAttribute( String key ) {
    for ( KettleAttributeInterface attribute : attributes ) {
      if ( attribute.getKey().equals( key ) ) {
        return attribute;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#getXmlCode(java.lang.String)
   */
  @Override
  public String getXmlCode( String attributeKey ) {
    return findAttribute( attributeKey ).getXmlCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#getRepCode(java.lang.String)
   */
  @Override
  public String getRepCode( String attributeKey ) {
    KettleAttributeInterface attr = findAttribute( attributeKey );
    return Utils.isEmpty( attr.getRepCode() ) ? attr.getXmlCode() : attr.getRepCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#getDescription(java.lang.String)
   */
  @Override
  public String getDescription( String attributeKey ) {
    return findAttribute( attributeKey ).getDescription();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.step.StepAttributesInterface#getTooltip(java.lang.String)
   */
  @Override
  public String getTooltip( String attributeKey ) {
    return findAttribute( attributeKey ).getTooltip();
  }

  /**
   * @return The supported transformation types that this step supports.
   */
  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, TransformationType.SingleThreaded, };
  }

  /**
   * @return True if the job entry defines one or more references to a repository object.
   */
  public boolean hasRepositoryReferences() {
    return false;
  }

  /**
   * Look up the references after import
   *
   * @param repository
   *          the repository to reference.
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
  }

  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ...
   */
  public String[] getReferencedObjectDescriptions() {
    return null;
  }

  public boolean[] isReferencedObjectEnabled() {
    return null;
  }

  /**
   * @return A description of the active referenced object in a transformation.
   * Null if nothing special needs to be done or if the active metadata isn't different from design.
   */
  public String getActiveReferencedObjectDescription() {
    return null;
  }

  /**
   * Load the referenced object
   *
   * @deprecated use {@link #loadReferencedObject(int, Repository, IMetaStore, VariableSpace)}
   *
   * @param meta
   *          The metadata that references
   * @param index
   *          the object index to load
   * @param rep
   *          the repository
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  @Deprecated
  public Object loadReferencedObject( int index, Repository rep, VariableSpace space ) throws KettleException {
    // Provided for v4 API compatibility
    return null;
  }

  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    // Provided for v4 API compatibility
    return null;
  }

  @Deprecated
  public void readRep( Repository rep, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {
    // provided for API (compile & runtime) compatibility with v4
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    // provided for API (compile & runtime) compatibility with v4
  }

  @Deprecated
  public void readRep( Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
    Map<String, Counter> counters ) throws KettleException {
    // provided for API (compile & runtime) compatibility with v4
  }

  /**
   * @deprecated use {@link #loadXML(Node, List, IMetaStore)}
   * @param stepnode
   * @param databases
   * @throws KettleXMLException
   */
  @Deprecated
  public void loadXML( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    // provided for API (compile & runtime) compatibility with v4
  }

  /**
   * @deprecated use {@link #loadXML(Node, List, IMetaStore)}
   * @param stepnode
   * @param databases
   * @throws KettleXMLException
   */
  @Deprecated
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters ) throws KettleXMLException {
    // provided for API (compile & runtime) compatibility with v4
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    // provided for API (compile & runtime) compatibility with v4
  }

  /**
   * @deprecated use {@link #saveRep(Repository, IMetaStore, ObjectId, ObjectId)
   * @param stepnode
   * @param databases
   * @throws KettleXMLException
   */
  @Deprecated
  public void saveRep( Repository rep, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    // provided for API (compile & runtime) compatibility with v4
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    // provided for API (compile & runtime) compatibility with v4
  }

  /**
   * @deprecated use {@link #check(List, TransMeta, StepMeta, RowMetaInterface, String[], String[], RowMetaInterface, VariableSpace, Repository, IMetaStore)}
   * @param remarks
   * @param transMeta
   * @param stepMeta
   * @param prev
   * @param input
   * @param output
   * @param info
   */
  @Deprecated
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info ) {
  }

  /**
   * @deprecated use {@link #check(List, TransMeta, StepMeta, RowMetaInterface, String[], String[], RowMetaInterface, VariableSpace, Repository, IMetaStore)}
   * @param remarks
   * @param transMeta
   * @param stepMeta
   * @param prev
   * @param input
   * @param output
   * @param info
   * @param repository
   * @param metaStore
   */
  @Deprecated
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) {
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
  }

}
