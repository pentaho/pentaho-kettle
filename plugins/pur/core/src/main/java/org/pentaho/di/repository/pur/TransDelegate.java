/*!
 * Copyright 2010 - 2023 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransDependency;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.RowDistributionInterface;
import org.pentaho.di.trans.step.RowDistributionPluginType;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.ui.repository.pur.services.IConnectionAclService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

public class TransDelegate extends AbstractDelegate implements ITransformer, ISharedObjectsTransformer,
    java.io.Serializable {

  private static final long serialVersionUID = 3766852226384368923L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final String PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS = "step_error_handling_min_pct_rows";

  private static final String PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS = "step_error_handling_max_pct_errors";

  private static final String PROP_STEP_ERROR_HANDLING_MAX_ERRORS = "step_error_handling_max_errors";

  private static final String PROP_STEP_ERROR_HANDLING_CODES_VALUENAME = "step_error_handling_codes_valuename";

  private static final String PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME = "step_error_handling_fields_valuename";

  static final String PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME = "step_error_handling_descriptions_valuename";

  private static final String PROP_STEP_ERROR_HANDLING_NR_VALUENAME = "step_error_handling_nr_valuename";

  private static final String PROP_STEP_ERROR_HANDLING_IS_ENABLED = "step_error_handling_is_enabled";

  private static final String PROP_STEP_ERROR_HANDLING_TARGET_STEP = "step_error_handling_target_step";

  private static final String PROP_LOG_SIZE_LIMIT = "LOG_SIZE_LIMIT";

  private static final String PROP_LOG_INTERVAL = "LOG_INTERVAL";

  private static final String PROP_TRANSFORMATION_TYPE = "TRANSFORMATION_TYPE";

  public static final String PROP_TRANS_DATA_SERVICE_NAME = "DATA_SERVICE_NAME";

  private static final String PROP_STEP_PERFORMANCE_LOG_TABLE = "STEP_PERFORMANCE_LOG_TABLE";

  private static final String PROP_STEP_PERFORMANCE_CAPTURING_DELAY = "STEP_PERFORMANCE_CAPTURING_DELAY";

  private static final String PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT = "STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT";

  private static final String PROP_CAPTURE_STEP_PERFORMANCE = "CAPTURE_STEP_PERFORMANCE";

  private static final String PROP_SHARED_FILE = "SHARED_FILE";

  private static final String PROP_USING_THREAD_PRIORITIES = "USING_THREAD_PRIORITIES";

  static final String PROP_FEEDBACK_SIZE = "FEEDBACK_SIZE";

  private static final String PROP_FEEDBACK_SHOWN = "FEEDBACK_SHOWN";

  static final String PROP_UNIQUE_CONNECTIONS = "UNIQUE_CONNECTIONS";

  private static final String PROP_ID_DIRECTORY = "ID_DIRECTORY";

  static final String PROP_SIZE_ROWSET = "SIZE_ROWSET";

  private static final String PROP_MODIFIED_DATE = "MODIFIED_DATE";

  private static final String PROP_MODIFIED_USER = "MODIFIED_USER";

  private static final String PROP_CREATED_DATE = "CREATED_DATE";

  private static final String PROP_CREATED_USER = "CREATED_USER";

  static final String PROP_DIFF_MAXDATE = "DIFF_MAXDATE";

  static final String PROP_OFFSET_MAXDATE = "OFFSET_MAXDATE";

  private static final String PROP_FIELD_NAME_MAXDATE = "FIELD_NAME_MAXDATE";

  private static final String PROP_TABLE_NAME_MAXDATE = "TABLE_NAME_MAXDATE";

  private static final String PROP_ID_DATABASE_MAXDATE = "ID_DATABASE_MAXDATE";

  static final String PROP_USE_LOGFIELD = "USE_LOGFIELD";

  static final String PROP_USE_BATCHID = "USE_BATCHID";

  private static final String PROP_TABLE_NAME_LOG = "TABLE_NAME_LOG";

  private static final String PROP_DATABASE_LOG = "DATABASE_LOG";

  private static final String PROP_STEP_REJECTED = "STEP_REJECTED";

  private static final String PROP_STEP_UPDATE = "STEP_UPDATE";

  private static final String PROP_STEP_OUTPUT = "STEP_OUTPUT";

  private static final String PROP_STEP_INPUT = "STEP_INPUT";

  private static final String PROP_STEP_WRITE = "STEP_WRITE";

  private static final String PROP_STEP_READ = "STEP_READ";

  static final String PROP_TRANS_STATUS = "TRANS_STATUS";

  private static final String PROP_TRANS_VERSION = "TRANS_VERSION";

  private static final String PROP_EXTENDED_DESCRIPTION = "EXTENDED_DESCRIPTION";

  static final String PROP_NR_PARAMETERS = "NR_PARAMETERS";

  static final String NODE_PARAMETERS = "parameters";

  static final String PROP_NR_HOPS = "NR_HOPS";

  static final String PROP_NR_NOTES = "NR_NOTES";

  static final String NODE_NOTES = "notes";

  static final String NODE_HOPS = "hops";

  private static final String PROP_STEP_ERROR_HANDLING_SOURCE_STEP = "step_error_handling_source_step";

  private static final String NODE_PARTITIONER_CUSTOM = "partitionerCustom";

  private static final String PROP_PARTITIONING_SCHEMA = "PARTITIONING_SCHEMA";

  private static final String PROP_PARTITIONING_METHOD = "PARTITIONING_METHOD";

  private static final String PROP_CLUSTER_SCHEMA = "cluster_schema";

  private static final String NODE_STEP_CUSTOM = "custom";

  private static Class<?> PKG = TransDelegate.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  static final String PROP_STEP_DISTRIBUTE = "STEP_DISTRIBUTE";

  static final String PROP_STEP_ROW_DISTRIBUTION = "STEP_ROW_DISTRIBUTION";

  static final String PROP_STEP_GUI_DRAW = "STEP_GUI_DRAW";

  static final String PROP_STEP_GUI_LOCATION_Y = "STEP_GUI_LOCATION_Y";

  static final String PROP_STEP_GUI_LOCATION_X = "STEP_GUI_LOCATION_X";

  static final String PROP_STEP_COPIES = "STEP_COPIES";

  static final String PROP_STEP_COPIES_STRING = "STEP_COPIES_STRING";

  static final String PROP_STEP_TYPE = "STEP_TYPE";

  static final String NODE_TRANS = "transformation";

  static final String NODE_TRANS_PRIVATE_DATABASES = "transPrivateDatabases";

  static final String NODE_TRANS_DEPENDENCIES = "transDepedencies";

  static final String PROP_TRANS_DEPENDENCY_DB_NAME = "TRANS_DEPENDENCY_DB_NAME";

  static final String PROP_TRANS_DEPENDENCY_TABLE = "TRANS_DEPENDENCY_TABLE";

  static final String PROP_TRANS_DEPENDENCY_FIELD = "TRANS_DEPENDENCY_FIELD";

  static final String PROP_TRANS_PRIVATE_DATABASE_NAMES = "PROP_TRANS_PRIVATE_DATABASE_NAMES";

  static final String TRANS_PRIVATE_DATABASE_DELIMITER = "\t";

  private static final String EXT_STEP = ".kst";

  static final String NODE_STEPS = "steps";

  private static final String PROP_XML = "XML";

  private static final String NOTE_PREFIX = "__NOTE__#";

  private static final String TRANS_HOP_FROM = "TRANS_HOP_FROM";

  private static final String TRANS_HOP_TO = "TRANS_HOP_TO";

  private static final String TRANS_HOP_ENABLED = "TRANS_HOP_ENABLED";

  private static final String TRANS_HOP_PREFIX = "__TRANS_HOP__#";

  private static final String TRANS_PARAM_PREFIX = "__TRANS_PARAM__#";

  private static final String PARAM_KEY = "PARAM_KEY";

  private static final String PARAM_DESC = "PARAM_DESC";

  private static final String PARAM_DEFAULT = "PARAM_DEFAULT";

  private final Repository repo;

  private final IConnectionAclService unifiedRepositoryConnectionAclService;

  public TransDelegate( final Repository repo, final IUnifiedRepository pur ) {
    super();
    this.repo = repo;
    this.unifiedRepositoryConnectionAclService = new UnifiedRepositoryConnectionAclService( pur );
  }

  public RepositoryElementInterface dataNodeToElement( final DataNode rootNode ) throws KettleException {
    TransMeta transMeta = new TransMeta();
    dataNodeToElement( rootNode, transMeta );
    return transMeta;
  }

  public void dataNodeToElement( final DataNode rootNode, final RepositoryElementInterface element )
    throws KettleException {
    TransMeta transMeta = (TransMeta) element;

    Set<String> privateDatabases = null;
    // read the private databases
    DataNode privateDbsNode = rootNode.getNode( NODE_TRANS_PRIVATE_DATABASES );
    // if we have node than we use one of two new formats. The older format that took
    // too long to save, uses a separate node for each database name, the new format
    // puts all the database names in the PROP_TRANS_PRIVATE_DATABASE_NAMES property.
    // BACKLOG-6635
    if ( privateDbsNode != null ) {
      privateDatabases = new HashSet<String>();
      if ( privateDbsNode.hasProperty( PROP_TRANS_PRIVATE_DATABASE_NAMES ) ) {
        for ( String privateDatabaseName : getString( privateDbsNode, PROP_TRANS_PRIVATE_DATABASE_NAMES ).split(
            TRANS_PRIVATE_DATABASE_DELIMITER ) ) {
          if ( !privateDatabaseName.isEmpty() ) {
            privateDatabases.add( privateDatabaseName );
          }
        }
      } else {
        for ( DataNode privateDatabase : privateDbsNode.getNodes() ) {
          privateDatabases.add( privateDatabase.getName() );
        }
      }
    }
    transMeta.setPrivateDatabases( privateDatabases );

    // read the steps...
    //
    DataNode stepsNode = rootNode.getNode( NODE_STEPS );
    for ( DataNode stepNode : stepsNode.getNodes() ) {

      StepMeta stepMeta = new StepMeta( new StringObjectId( stepNode.getId().toString() ) );
      stepMeta.setParentTransMeta( transMeta ); // for tracing, retain hierarchy

      // Read the basics
      //
      stepMeta.setName( getString( stepNode, PROP_NAME ) );
      if ( stepNode.hasProperty( PROP_DESCRIPTION ) ) {
        stepMeta.setDescription( getString( stepNode, PROP_DESCRIPTION ) );
      }
      stepMeta.setDistributes( stepNode.getProperty( PROP_STEP_DISTRIBUTE ).getBoolean() );
      DataProperty rowDistributionProperty = stepNode.getProperty( PROP_STEP_ROW_DISTRIBUTION );
      String rowDistributionCode = rowDistributionProperty == null ? null : rowDistributionProperty.getString();
      RowDistributionInterface rowDistribution =
          PluginRegistry.getInstance().loadClass( RowDistributionPluginType.class, rowDistributionCode,
              RowDistributionInterface.class );
      stepMeta.setRowDistribution( rowDistribution );
      stepMeta.setDraw( stepNode.getProperty( PROP_STEP_GUI_DRAW ).getBoolean() );
      int copies = (int) stepNode.getProperty( PROP_STEP_COPIES ).getLong();
      String copiesString =
          stepNode.getProperty( PROP_STEP_COPIES_STRING ) != null ? stepNode.getProperty( PROP_STEP_COPIES_STRING )
              .getString() : StringUtils.EMPTY;
      if ( !Utils.isEmpty( copiesString ) ) {
        stepMeta.setCopiesString( copiesString );
      } else {
        stepMeta.setCopies( copies ); // for backward compatibility
      }

      int x = (int) stepNode.getProperty( PROP_STEP_GUI_LOCATION_X ).getLong();
      int y = (int) stepNode.getProperty( PROP_STEP_GUI_LOCATION_Y ).getLong();
      stepMeta.setLocation( x, y );

      // Load the group attributes map
      //
      AttributesMapUtil.loadAttributesMap( stepNode, stepMeta );

      String stepType = getString( stepNode, PROP_STEP_TYPE );

      // Create a new StepMetaInterface object...
      //
      PluginRegistry registry = PluginRegistry.getInstance();
      PluginInterface stepPlugin = registry.findPluginWithId( StepPluginType.class, stepType );

      StepMetaInterface stepMetaInterface = null;
      if ( stepPlugin != null ) {
        stepMetaInterface = (StepMetaInterface) registry.loadClass( stepPlugin );
        stepType = stepPlugin.getIds()[0]; // revert to the default in case we loaded an alternate version
      } else {
        stepMeta.setStepMetaInterface( (StepMetaInterface) new MissingTrans( stepMeta.getName(), stepType ) );
        transMeta.addMissingTrans( (MissingTrans) stepMeta.getStepMetaInterface() );
      }

      stepMeta.setStepID( stepType );

      // Read the metadata from the repository too...
      //
      RepositoryProxy proxy = new RepositoryProxy( stepNode.getNode( NODE_STEP_CUSTOM ) );
      if ( !stepMeta.isMissing() ) {
        readRepCompatibleStepMeta( stepMetaInterface, proxy, null, transMeta.getDatabases() );
        stepMetaInterface.readRep( proxy, transMeta.getMetaStore(), null, transMeta.getDatabases() );
        stepMeta.setStepMetaInterface( stepMetaInterface );
      }

      // Get the partitioning as well...
      StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta();
      if ( stepNode.hasProperty( PROP_PARTITIONING_SCHEMA ) ) {
        String partSchemaId = stepNode.getProperty( PROP_PARTITIONING_SCHEMA ).getRef().getId().toString();
        String schemaName = repo.loadPartitionSchema( new StringObjectId( partSchemaId ), null ).getName();

        stepPartitioningMeta.setPartitionSchemaName( schemaName );
        String methodCode = getString( stepNode, PROP_PARTITIONING_METHOD );
        stepPartitioningMeta.setMethod( StepPartitioningMeta.getMethod( methodCode ) );
        if ( stepPartitioningMeta.getPartitioner() != null ) {
          proxy = new RepositoryProxy( stepNode.getNode( NODE_PARTITIONER_CUSTOM ) );
          stepPartitioningMeta.getPartitioner().loadRep( proxy, null );
        }
        stepPartitioningMeta.hasChanged( true );
      }
      stepMeta.setStepPartitioningMeta( stepPartitioningMeta );

      stepMeta.getStepPartitioningMeta().setPartitionSchemaAfterLoading( transMeta.getPartitionSchemas() );
      // Get the cluster schema name
      String clusterSchemaName = getString( stepNode, PROP_CLUSTER_SCHEMA );
      stepMeta.setClusterSchemaName( clusterSchemaName );
      if ( clusterSchemaName != null && transMeta.getClusterSchemas() != null ) {
        // Get the cluster schema from the given name
        for ( ClusterSchema clusterSchema : transMeta.getClusterSchemas() ) {
          if ( clusterSchema.getName().equals( clusterSchemaName ) ) {
            stepMeta.setClusterSchema( clusterSchema );
            break;
          }
        }
      }

      transMeta.addStep( stepMeta );

    }

    for ( DataNode stepNode : stepsNode.getNodes() ) {

      ObjectId stepObjectId = new StringObjectId( stepNode.getId().toString() );
      StepMeta stepMeta = StepMeta.findStep( transMeta.getSteps(), stepObjectId );

      // Also load the step error handling metadata
      //
      if ( stepNode.hasProperty( PROP_STEP_ERROR_HANDLING_SOURCE_STEP ) ) {
        StepErrorMeta meta = new StepErrorMeta( transMeta, stepMeta );
        meta.setTargetStep( StepMeta.findStep( transMeta.getSteps(), stepNode.getProperty(
            PROP_STEP_ERROR_HANDLING_TARGET_STEP ).getString() ) );
        meta.setEnabled( stepNode.getProperty( PROP_STEP_ERROR_HANDLING_IS_ENABLED ).getBoolean() );
        meta.setNrErrorsValuename( getString( stepNode, PROP_STEP_ERROR_HANDLING_NR_VALUENAME ) );
        meta.setErrorDescriptionsValuename( getString( stepNode, PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME ) );
        meta.setErrorFieldsValuename( getString( stepNode, PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME ) );
        meta.setErrorCodesValuename( getString( stepNode, PROP_STEP_ERROR_HANDLING_CODES_VALUENAME ) );
        meta.setMaxErrors( getString( stepNode, PROP_STEP_ERROR_HANDLING_MAX_ERRORS ) );
        meta.setMaxPercentErrors( getString( stepNode, PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS ) );
        meta.setMinPercentRows( getString( stepNode, PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS ) );
        meta.getSourceStep().setStepErrorMeta( meta ); // a bit of a trick, I know.
      }
    }

    // Have all StreamValueLookups, etc. reference the correct source steps...
    //
    for ( int i = 0; i < transMeta.nrSteps(); i++ ) {
      StepMeta stepMeta = transMeta.getStep( i );
      StepMetaInterface sii = stepMeta.getStepMetaInterface();
      if ( sii != null ) {
        sii.searchInfoAndTargetSteps( transMeta.getSteps() );
      }
    }

    // Read the notes...
    //
    DataNode notesNode = rootNode.getNode( NODE_NOTES );
    int nrNotes = (int) notesNode.getProperty( PROP_NR_NOTES ).getLong();
    for ( DataNode noteNode : notesNode.getNodes() ) {
      String xml = getString( noteNode, PROP_XML );
      transMeta
          .addNote( new NotePadMeta( XMLHandler.getSubNode( XMLHandler.loadXMLString( xml ), NotePadMeta.XML_TAG ) ) );
    }
    if ( transMeta.nrNotes() != nrNotes ) {
      throw new KettleException( "The number of notes read [" + transMeta.nrNotes()
          + "] was not the number we expected [" + nrNotes + "]" );
    }

    // Read the hops...
    //
    DataNode hopsNode = rootNode.getNode( NODE_HOPS );
    int nrHops = (int) hopsNode.getProperty( PROP_NR_HOPS ).getLong();
    for ( DataNode hopNode : hopsNode.getNodes() ) {
      String stepFromName = getString( hopNode, TRANS_HOP_FROM );
      String stepToName = getString( hopNode, TRANS_HOP_TO );
      boolean enabled = true;
      if ( hopNode.hasProperty( TRANS_HOP_ENABLED ) ) {
        enabled = hopNode.getProperty( TRANS_HOP_ENABLED ).getBoolean();
      }

      StepMeta stepFrom = StepMeta.findStep( transMeta.getSteps(), stepFromName );
      StepMeta stepTo = StepMeta.findStep( transMeta.getSteps(), stepToName );

      // Make sure to only accept valid hops PDI-5519
      //
      if ( stepFrom != null && stepTo != null ) {
        transMeta.addTransHop( new TransHopMeta( stepFrom, stepTo, enabled ) );
      }

    }
    if ( transMeta.nrTransHops() != nrHops ) {
      throw new KettleException( "The number of hops read [" + transMeta.nrTransHops()
          + "] was not the number we expected [" + nrHops + "]" );
    }

    // Load the details at the end, to make sure we reference the databases correctly, etc.
    //
    loadTransformationDetails( rootNode, transMeta );
    loadDependencies( rootNode, transMeta );

    transMeta.eraseParameters();

    DataNode paramsNode = rootNode.getNode( NODE_PARAMETERS );

    int count = (int) paramsNode.getProperty( PROP_NR_PARAMETERS ).getLong();
    for ( int idx = 0; idx < count; idx++ ) {
      DataNode paramNode = paramsNode.getNode( TRANS_PARAM_PREFIX + idx );
      String key = getString( paramNode, PARAM_KEY );
      String def = getString( paramNode, PARAM_DEFAULT );
      String desc = getString( paramNode, PARAM_DESC );
      transMeta.addParameterDefinition( key, def, desc );
    }

    transMeta.activateParameters();
  }

  /**
   * Compatible loading of metadata for v4 style plugins using deprecated methods.
   * 
   * @param stepMetaInterface
   * @param repository
   * @param objectId
   * @param databases
   * @throws KettleException
   */
  @SuppressWarnings( "deprecation" )
  private void readRepCompatibleStepMeta( StepMetaInterface stepMetaInterface, Repository repository,
      ObjectId objectId, List<DatabaseMeta> databases ) throws KettleException {
    stepMetaInterface.readRep( repository, objectId, databases, null );
  }

  protected void loadTransformationDetails( final DataNode rootNode, final TransMeta transMeta ) throws KettleException {
    transMeta.setExtendedDescription( getString( rootNode, PROP_EXTENDED_DESCRIPTION ) );
    transMeta.setTransversion( getString( rootNode, PROP_TRANS_VERSION ) );
    transMeta.setTransstatus( (int) rootNode.getProperty( PROP_TRANS_STATUS ).getLong() );

    if ( rootNode.hasProperty( PROP_STEP_READ ) ) {
      transMeta.getTransLogTable().setStepRead(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_READ ) ) );
    }
    if ( rootNode.hasProperty( PROP_STEP_WRITE ) ) {
      transMeta.getTransLogTable().setStepWritten(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_WRITE ) ) );
    }
    if ( rootNode.hasProperty( PROP_STEP_INPUT ) ) {
      transMeta.getTransLogTable().setStepInput(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_INPUT ) ) );
    }
    if ( rootNode.hasProperty( PROP_STEP_OUTPUT ) ) {
      transMeta.getTransLogTable().setStepOutput(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_OUTPUT ) ) );
    }
    if ( rootNode.hasProperty( PROP_STEP_UPDATE ) ) {
      transMeta.getTransLogTable().setStepUpdate(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_UPDATE ) ) );
    }
    if ( rootNode.hasProperty( PROP_STEP_REJECTED ) ) {
      transMeta.getTransLogTable().setStepRejected(
          StepMeta.findStep( transMeta.getSteps(), getString( rootNode, PROP_STEP_REJECTED ) ) );
    }

    if ( rootNode.hasProperty( PROP_DATABASE_LOG ) ) {
      String id = rootNode.getProperty( PROP_DATABASE_LOG ).getRef().getId().toString();
      DatabaseMeta conn = DatabaseMeta.findDatabase( transMeta.getDatabases(), new StringObjectId( id ) );
      transMeta.getTransLogTable().setConnectionName( conn.getName() );
    }
    transMeta.getTransLogTable().setTableName( getString( rootNode, PROP_TABLE_NAME_LOG ) );
    transMeta.getTransLogTable().setBatchIdUsed( rootNode.getProperty( PROP_USE_BATCHID ).getBoolean() );
    transMeta.getTransLogTable().setLogFieldUsed( rootNode.getProperty( PROP_USE_LOGFIELD ).getBoolean() );

    if ( rootNode.hasProperty( PROP_ID_DATABASE_MAXDATE ) ) {
      String id = rootNode.getProperty( PROP_ID_DATABASE_MAXDATE ).getRef().getId().toString();
      transMeta.setMaxDateConnection( DatabaseMeta.findDatabase( transMeta.getDatabases(), new StringObjectId( id ) ) );
    }
    transMeta.setMaxDateTable( getString( rootNode, PROP_TABLE_NAME_MAXDATE ) );
    transMeta.setMaxDateField( getString( rootNode, PROP_FIELD_NAME_MAXDATE ) );
    transMeta.setMaxDateOffset( rootNode.getProperty( PROP_OFFSET_MAXDATE ).getDouble() );
    transMeta.setMaxDateDifference( rootNode.getProperty( PROP_DIFF_MAXDATE ).getDouble() );

    transMeta.setCreatedUser( getString( rootNode, PROP_CREATED_USER ) );
    transMeta.setCreatedDate( getDate( rootNode, PROP_CREATED_DATE ) );

    transMeta.setModifiedUser( getString( rootNode, PROP_MODIFIED_USER ) );
    transMeta.setModifiedDate( getDate( rootNode, PROP_MODIFIED_DATE ) );

    // Optional:
    transMeta.setSizeRowset( Const.ROWS_IN_ROWSET );
    long val_size_rowset = rootNode.getProperty( PROP_SIZE_ROWSET ).getLong();
    if ( val_size_rowset > 0 ) {
      transMeta.setSizeRowset( (int) val_size_rowset );
    }

    if ( rootNode.hasProperty( PROP_ID_DIRECTORY ) ) {
      String id_directory = getString( rootNode, PROP_ID_DIRECTORY );
      if ( log.isDetailed() ) {
        log.logDetailed( toString(), PROP_ID_DIRECTORY + "=" + id_directory ); //$NON-NLS-1$
      }
      // Set right directory...
      transMeta.setRepositoryDirectory( repo.findDirectory( new StringObjectId( id_directory ) ) ); // always reload the
                                                                                                    // folder structure
    }

    transMeta.setUsingUniqueConnections( rootNode.getProperty( PROP_UNIQUE_CONNECTIONS ).getBoolean() );
    boolean feedbackShown = true;
    if ( rootNode.hasProperty( PROP_FEEDBACK_SHOWN ) ) {
      feedbackShown = rootNode.getProperty( PROP_FEEDBACK_SHOWN ).getBoolean();
    }
    transMeta.setFeedbackShown( feedbackShown );
    transMeta.setFeedbackSize( (int) rootNode.getProperty( PROP_FEEDBACK_SIZE ).getLong() );
    boolean usingThreadPriorityManagement = true;
    if ( rootNode.hasProperty( PROP_USING_THREAD_PRIORITIES ) ) {
      usingThreadPriorityManagement = rootNode.getProperty( PROP_USING_THREAD_PRIORITIES ).getBoolean();
    }
    transMeta.setUsingThreadPriorityManagment( usingThreadPriorityManagement );
    transMeta.setSharedObjectsFile( getString( rootNode, PROP_SHARED_FILE ) );
    String transTypeCode = getString( rootNode, PROP_TRANSFORMATION_TYPE );
    transMeta.setTransformationType( TransformationType.getTransformationTypeByCode( transTypeCode ) );

    // Performance monitoring for steps...
    //
    boolean capturingStepPerformanceSnapShots = true;
    if ( rootNode.hasProperty( PROP_CAPTURE_STEP_PERFORMANCE ) ) {
      capturingStepPerformanceSnapShots = rootNode.getProperty( PROP_CAPTURE_STEP_PERFORMANCE ).getBoolean();
    }
    transMeta.setCapturingStepPerformanceSnapShots( capturingStepPerformanceSnapShots );
    transMeta.setStepPerformanceCapturingDelay( getLong( rootNode, PROP_STEP_PERFORMANCE_CAPTURING_DELAY ) );
    transMeta.setStepPerformanceCapturingSizeLimit( getString( rootNode, PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT ) );
    transMeta.getPerformanceLogTable().setTableName( getString( rootNode, PROP_STEP_PERFORMANCE_LOG_TABLE ) );
    transMeta.getTransLogTable().setLogSizeLimit( getString( rootNode, PROP_LOG_SIZE_LIMIT ) );

    // Load the logging tables too..
    //
    RepositoryAttributeInterface attributeInterface = new PurRepositoryAttribute( rootNode, transMeta.getDatabases() );
    for ( LogTableInterface logTable : transMeta.getLogTables() ) {
      logTable.loadFromRepository( attributeInterface );
    }

    AttributesMapUtil.loadAttributesMap( rootNode, transMeta );
  }

  public DataNode elementToDataNode( final RepositoryElementInterface element ) throws KettleException {
    TransMeta transMeta = (TransMeta) element;

    DataNode rootNode = new DataNode( NODE_TRANS );

    if ( transMeta.getPrivateDatabases() != null ) {
      // save all private transformations database name http://jira.pentaho.com/browse/PPP-3405
      String privateDatabaseNames = StringUtils.join( transMeta.getPrivateDatabases(), TRANS_PRIVATE_DATABASE_DELIMITER );
      DataNode privateDatabaseNode = rootNode.addNode( NODE_TRANS_PRIVATE_DATABASES );
      privateDatabaseNode.setProperty( PROP_TRANS_PRIVATE_DATABASE_NAMES, privateDatabaseNames );
    }

    DataNode stepsNode = rootNode.addNode( NODE_STEPS );

    // Also save all the steps in the transformation!
    //
    int stepNr = 0;
    for ( StepMeta step : transMeta.getSteps() ) {
      stepNr++;
      DataNode stepNode = stepsNode.addNode( sanitizeNodeName( step.getName() ) + "_" + stepNr + EXT_STEP ); //$NON-NLS-1$

      // Store the main data
      //
      stepNode.setProperty( PROP_NAME, step.getName() );
      stepNode.setProperty( PROP_DESCRIPTION, step.getDescription() );
      stepNode.setProperty( PROP_STEP_TYPE, step.getStepID() );
      stepNode.setProperty( PROP_STEP_DISTRIBUTE, step.isDistributes() );
      stepNode.setProperty( PROP_STEP_ROW_DISTRIBUTION, step.getRowDistribution() == null ? null : step
          .getRowDistribution().getCode() );
      stepNode.setProperty( PROP_STEP_COPIES, step.getCopies() );
      stepNode.setProperty( PROP_STEP_COPIES_STRING, step.getCopiesString() );
      stepNode.setProperty( PROP_STEP_GUI_LOCATION_X, step.getLocation().x );
      stepNode.setProperty( PROP_STEP_GUI_LOCATION_Y, step.getLocation().y );
      stepNode.setProperty( PROP_STEP_GUI_DRAW, step.isDrawn() );

      // Also save the step group attributes map
      //
      AttributesMapUtil.saveAttributesMap( stepNode, step );

      // Save the step metadata using the repository save method, NOT XML
      // That is because we want to keep the links to databases, conditions, etc by ID, not name.
      //
      StepMetaInterface stepMetaInterface = step.getStepMetaInterface();
      DataNode stepCustomNode = new DataNode( NODE_STEP_CUSTOM );
      Repository proxy = new RepositoryProxy( stepCustomNode );
      compatibleSaveRep( stepMetaInterface, proxy, null, null );
      stepMetaInterface.saveRep( proxy, MetaStoreConst.getDefaultMetastore(), null, null );
      stepNode.addNode( stepCustomNode );

      // Save the partitioning information by reference as well...
      //
      StepPartitioningMeta partitioningMeta = step.getStepPartitioningMeta();
      if ( partitioningMeta != null && partitioningMeta.getPartitionSchema() != null
          && partitioningMeta.isPartitioned() ) {
        DataNodeRef ref = new DataNodeRef( partitioningMeta.getPartitionSchema().getObjectId().getId() );
        stepNode.setProperty( PROP_PARTITIONING_SCHEMA, ref );
        stepNode.setProperty( PROP_PARTITIONING_METHOD, partitioningMeta.getMethodCode() ); // method of partitioning
        if ( partitioningMeta.getPartitioner() != null ) {
          DataNode partitionerCustomNode = new DataNode( NODE_PARTITIONER_CUSTOM );
          proxy = new RepositoryProxy( partitionerCustomNode );
          partitioningMeta.getPartitioner().saveRep( proxy, null, null );
          stepNode.addNode( partitionerCustomNode );
        }
      }

      // Save the clustering information as well...
      //
      stepNode.setProperty( PROP_CLUSTER_SCHEMA, step.getClusterSchema() == null ? "" : step.getClusterSchema() //$NON-NLS-1$
          .getName() );

      // Save the error hop metadata
      //
      StepErrorMeta stepErrorMeta = step.getStepErrorMeta();
      if ( stepErrorMeta != null ) {
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_SOURCE_STEP, stepErrorMeta.getSourceStep() != null
            ? stepErrorMeta.getSourceStep().getName() : "" ); //$NON-NLS-1$
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_TARGET_STEP, stepErrorMeta.getTargetStep() != null
            ? stepErrorMeta.getTargetStep().getName() : "" ); //$NON-NLS-1$
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_IS_ENABLED, stepErrorMeta.isEnabled() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_NR_VALUENAME, stepErrorMeta.getNrErrorsValuename() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_DESCRIPTIONS_VALUENAME, stepErrorMeta
            .getErrorDescriptionsValuename() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_FIELDS_VALUENAME, stepErrorMeta.getErrorFieldsValuename() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_CODES_VALUENAME, stepErrorMeta.getErrorCodesValuename() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_MAX_ERRORS, stepErrorMeta.getMaxErrors() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_MAX_PCT_ERRORS, stepErrorMeta.getMaxPercentErrors() );
        stepNode.setProperty( PROP_STEP_ERROR_HANDLING_MIN_PCT_ROWS, stepErrorMeta.getMinPercentRows() );
      }

    }

    // Save the notes
    //
    DataNode notesNode = rootNode.addNode( NODE_NOTES );
    notesNode.setProperty( PROP_NR_NOTES, transMeta.nrNotes() );
    for ( int i = 0; i < transMeta.nrNotes(); i++ ) {
      NotePadMeta note = transMeta.getNote( i );
      DataNode noteNode = notesNode.addNode( NOTE_PREFIX + i );

      noteNode.setProperty( PROP_XML, note.getXML() );
    }

    // Finally, save the hops
    //
    DataNode hopsNode = rootNode.addNode( NODE_HOPS );
    hopsNode.setProperty( PROP_NR_HOPS, transMeta.nrTransHops() );
    for ( int i = 0; i < transMeta.nrTransHops(); i++ ) {
      TransHopMeta hop = transMeta.getTransHop( i );
      DataNode hopNode = hopsNode.addNode( TRANS_HOP_PREFIX + i );
      hopNode.setProperty( TRANS_HOP_FROM, hop.getFromStep().getName() );
      hopNode.setProperty( TRANS_HOP_TO, hop.getToStep().getName() );
      hopNode.setProperty( TRANS_HOP_ENABLED, hop.isEnabled() );
    }

    // Parameters
    //
    String[] paramKeys = transMeta.listParameters();
    DataNode paramsNode = rootNode.addNode( NODE_PARAMETERS );
    paramsNode.setProperty( PROP_NR_PARAMETERS, paramKeys == null ? 0 : paramKeys.length );

    for ( int idx = 0; idx < paramKeys.length; idx++ ) {
      DataNode paramNode = paramsNode.addNode( TRANS_PARAM_PREFIX + idx );
      String key = paramKeys[idx];
      String description = transMeta.getParameterDescription( paramKeys[idx] );
      String defaultValue = transMeta.getParameterDefault( paramKeys[idx] );

      paramNode.setProperty( PARAM_KEY, key != null ? key : "" ); //$NON-NLS-1$
      paramNode.setProperty( PARAM_DEFAULT, defaultValue != null ? defaultValue : "" ); //$NON-NLS-1$
      paramNode.setProperty( PARAM_DESC, description != null ? description : "" ); //$NON-NLS-1$
    }

    // Let's not forget to save the details of the transformation itself.
    // This includes logging information, parameters, etc.
    //
    saveTransformationDetails( rootNode, transMeta );

    saveDependencies( rootNode, transMeta );

    return rootNode;
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleSaveRep( StepMetaInterface stepMetaInterface, Repository repository,
      ObjectId id_transformation, ObjectId objectId ) throws KettleException {
    stepMetaInterface.saveRep( repository, id_transformation, objectId );
  }

  /**
   * Save transformation dependencies
   * @param rootNode
   * @param transMeta
   */
  private void saveDependencies( final DataNode rootNode, final TransMeta transMeta ) {
    DataNode dependenciesNodeRoot = new DataNode( NODE_TRANS_DEPENDENCIES );
    rootNode.addNode( dependenciesNodeRoot );
    for ( TransDependency transDependency : transMeta.getDependencies() ) {
      DataNode dependencyNode = new DataNode(
        sanitizeNodeName( String.join( "_", transDependency.getDatabase().getName(),
          transDependency.getTablename(), transDependency.getFieldname() ) ) );
      dependencyNode.setProperty( PROP_TRANS_DEPENDENCY_DB_NAME, transDependency.getDatabase().getName() );
      dependencyNode.setProperty( PROP_TRANS_DEPENDENCY_TABLE, transDependency.getTablename() );
      dependencyNode.setProperty( PROP_TRANS_DEPENDENCY_FIELD, transDependency.getFieldname() );
      dependenciesNodeRoot.addNode( dependencyNode );
    }
  }

  private void loadDependencies( final DataNode rootNode, final TransMeta transMeta ) {
    if ( rootNode.hasNode( NODE_TRANS_DEPENDENCIES ) ) {
      DataNode dependenciesNode = rootNode.getNode( NODE_TRANS_DEPENDENCIES );
      for ( DataNode dependencyNode : dependenciesNode.getNodes() ) {
        TransDependency transDependency = new TransDependency();
        transDependency.setTablename( getString( dependencyNode, PROP_TRANS_DEPENDENCY_TABLE ) );
        transDependency.setFieldname( getString( dependencyNode, PROP_TRANS_DEPENDENCY_FIELD ) );
        String dbName = getString( dependencyNode, PROP_TRANS_DEPENDENCY_DB_NAME );
        transDependency.setDatabase( transMeta.findDatabase( dbName ) );
        transMeta.addDependency( transDependency );
      }
    }
  }

  private void saveTransformationDetails( final DataNode rootNode, final TransMeta transMeta ) throws KettleException {

    rootNode.setProperty( PROP_EXTENDED_DESCRIPTION, transMeta.getExtendedDescription() );
    rootNode.setProperty( PROP_TRANS_VERSION, transMeta.getTransversion() );
    rootNode.setProperty( PROP_TRANS_STATUS, transMeta.getTransstatus() < 0 ? -1L : transMeta.getTransstatus() );

    rootNode.setProperty( PROP_STEP_READ, transMeta.getTransLogTable().getStepnameRead() );
    rootNode.setProperty( PROP_STEP_WRITE, transMeta.getTransLogTable().getStepnameWritten() );
    rootNode.setProperty( PROP_STEP_INPUT, transMeta.getTransLogTable().getStepnameInput() );
    rootNode.setProperty( PROP_STEP_OUTPUT, transMeta.getTransLogTable().getStepnameOutput() );
    rootNode.setProperty( PROP_STEP_UPDATE, transMeta.getTransLogTable().getStepnameUpdated() );
    rootNode.setProperty( PROP_STEP_REJECTED, transMeta.getTransLogTable().getStepnameRejected() );

    if ( transMeta.getTransLogTable().getDatabaseMeta() != null ) {
      DataNodeRef ref = new DataNodeRef( transMeta.getTransLogTable().getDatabaseMeta().getObjectId().getId() );
      rootNode.setProperty( PROP_DATABASE_LOG, ref );
    }

    rootNode.setProperty( PROP_TABLE_NAME_LOG, transMeta.getTransLogTable().getTableName() );

    rootNode.setProperty( PROP_USE_BATCHID, Boolean.valueOf( transMeta.getTransLogTable().isBatchIdUsed() ) );
    rootNode.setProperty( PROP_USE_LOGFIELD, Boolean.valueOf( transMeta.getTransLogTable().isLogFieldUsed() ) );

    if ( transMeta.getMaxDateConnection() != null ) {
      DataNodeRef ref = new DataNodeRef( transMeta.getMaxDateConnection().getObjectId().getId() );
      rootNode.setProperty( PROP_ID_DATABASE_MAXDATE, ref );
    }

    rootNode.setProperty( PROP_TABLE_NAME_MAXDATE, transMeta.getMaxDateTable() );
    rootNode.setProperty( PROP_FIELD_NAME_MAXDATE, transMeta.getMaxDateField() );
    rootNode.setProperty( PROP_OFFSET_MAXDATE, new Double( transMeta.getMaxDateOffset() ) );
    rootNode.setProperty( PROP_DIFF_MAXDATE, new Double( transMeta.getMaxDateDifference() ) );

    rootNode.setProperty( PROP_CREATED_USER, transMeta.getCreatedUser() );
    rootNode.setProperty( PROP_CREATED_DATE, transMeta.getCreatedDate() );

    rootNode.setProperty( PROP_MODIFIED_USER, transMeta.getModifiedUser() );
    rootNode.setProperty( PROP_MODIFIED_DATE, transMeta.getModifiedDate() );

    rootNode.setProperty( PROP_SIZE_ROWSET, transMeta.getSizeRowset() );

    rootNode.setProperty( PROP_UNIQUE_CONNECTIONS, transMeta.isUsingUniqueConnections() );
    rootNode.setProperty( PROP_FEEDBACK_SHOWN, transMeta.isFeedbackShown() );
    rootNode.setProperty( PROP_FEEDBACK_SIZE, transMeta.getFeedbackSize() );
    rootNode.setProperty( PROP_USING_THREAD_PRIORITIES, transMeta.isUsingThreadPriorityManagment() );
    rootNode.setProperty( PROP_SHARED_FILE, transMeta.getSharedObjectsFile() );

    rootNode.setProperty( PROP_CAPTURE_STEP_PERFORMANCE, transMeta.isCapturingStepPerformanceSnapShots() );
    rootNode.setProperty( PROP_STEP_PERFORMANCE_CAPTURING_DELAY, transMeta.getStepPerformanceCapturingDelay() );
    rootNode.setProperty( PROP_STEP_PERFORMANCE_CAPTURING_SIZE_LIMIT, transMeta.getStepPerformanceCapturingSizeLimit() );
    rootNode.setProperty( PROP_STEP_PERFORMANCE_LOG_TABLE, transMeta.getPerformanceLogTable().getTableName() );

    rootNode.setProperty( PROP_LOG_SIZE_LIMIT, transMeta.getTransLogTable().getLogSizeLimit() );
    rootNode.setProperty( PROP_LOG_INTERVAL, transMeta.getTransLogTable().getLogInterval() );

    rootNode.setProperty( PROP_TRANSFORMATION_TYPE, transMeta.getTransformationType().getCode() );

    // Save the logging tables too..
    //
    RepositoryAttributeInterface attributeInterface = new PurRepositoryAttribute( rootNode, transMeta.getDatabases() );
    for ( LogTableInterface logTable : transMeta.getLogTables() ) {
      logTable.saveToRepository( attributeInterface );
    }

    // Save the transformation attribute groups map
    //
    AttributesMapUtil.saveAttributesMap( rootNode, transMeta );
  }

  @SuppressWarnings( "unchecked" )
  public SharedObjects loadSharedObjects( final RepositoryElementInterface element,
      final Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType )
    throws KettleException {
    TransMeta transMeta = (TransMeta) element;
    transMeta.setSharedObjects( transMeta.readSharedObjects() );

    // Repository objects take priority so let's overwrite them...
    //
    readDatabases( transMeta, true, (List<DatabaseMeta>) sharedObjectsByType.get( RepositoryObjectType.DATABASE ) );
    readPartitionSchemas( transMeta, true, (List<PartitionSchema>) sharedObjectsByType
        .get( RepositoryObjectType.PARTITION_SCHEMA ) );
    readSlaves( transMeta, true, (List<SlaveServer>) sharedObjectsByType.get( RepositoryObjectType.SLAVE_SERVER ) );
    readClusters( transMeta, true, (List<ClusterSchema>) sharedObjectsByType.get( RepositoryObjectType.CLUSTER_SCHEMA ) );

    return transMeta.getSharedObjects();
  }

  /**
   * Insert all the databases from the repository into the TransMeta object, overwriting optionally
   * 
   * @param TransMeta
   *          The transformation to load into.
   * @param overWriteShared
   *          if an object with the same name exists, overwrite
   * @throws KettleException
   */
  protected void readDatabases( TransMeta transMeta, boolean overWriteShared, List<DatabaseMeta> databaseMetas ) {
    for ( DatabaseMeta databaseMeta : databaseMetas ) {
      if ( overWriteShared || transMeta.findDatabase( databaseMeta.getName() ) == null ) {
        if ( databaseMeta.getName() != null ) {
          databaseMeta.shareVariablesWith( transMeta );
          transMeta.addOrReplaceDatabase( databaseMeta );
          if ( !overWriteShared ) {
            databaseMeta.setChanged( false );
          }
        }
      }
    }
    transMeta.clearChangedDatabases();
  }

  /**
   * Add clusters in the repository to this transformation if they are not yet present.
   * 
   * @param TransMeta
   *          The transformation to load into.
   * @param overWriteShared
   *          if an object with the same name exists, overwrite
   */
  protected void readClusters( TransMeta transMeta, boolean overWriteShared, List<ClusterSchema> clusterSchemas ) {
    for ( ClusterSchema clusterSchema : clusterSchemas ) {
      if ( overWriteShared || transMeta.findClusterSchema( clusterSchema.getName() ) == null ) {
        if ( !Utils.isEmpty( clusterSchema.getName() ) ) {
          clusterSchema.shareVariablesWith( transMeta );
          transMeta.addOrReplaceClusterSchema( clusterSchema );
          if ( !overWriteShared ) {
            clusterSchema.setChanged( false );
          }
        }
      }
    }
  }

  /**
   * Add the partitions in the repository to this transformation if they are not yet present.
   * 
   * @param TransMeta
   *          The transformation to load into.
   * @param overWriteShared
   *          if an object with the same name exists, overwrite
   */
  protected void readPartitionSchemas( TransMeta transMeta, boolean overWriteShared,
      List<PartitionSchema> partitionSchemas ) {
    for ( PartitionSchema partitionSchema : partitionSchemas ) {
      if ( overWriteShared || transMeta.findPartitionSchema( partitionSchema.getName() ) == null ) {
        if ( !Utils.isEmpty( partitionSchema.getName() ) ) {
          transMeta.addOrReplacePartitionSchema( partitionSchema );
          if ( !overWriteShared ) {
            partitionSchema.setChanged( false );
          }
        }
      }
    }
  }

  /**
   * Add the slave servers in the repository to this transformation if they are not yet present.
   * 
   * @param TransMeta
   *          The transformation to load into.
   * @param overWriteShared
   *          if an object with the same name exists, overwrite
   */
  protected void readSlaves( TransMeta transMeta, boolean overWriteShared, List<SlaveServer> slaveServers ) {
    for ( SlaveServer slaveServer : slaveServers ) {
      if ( overWriteShared || transMeta.findSlaveServer( slaveServer.getName() ) == null ) {
        if ( !Utils.isEmpty( slaveServer.getName() ) ) {
          slaveServer.shareVariablesWith( transMeta );
          transMeta.addOrReplaceSlaveServer( slaveServer );
          if ( !overWriteShared ) {
            slaveServer.setChanged( false );
          }
        }
      }
    }
  }

  public void saveSharedObjects( final RepositoryElementInterface element, final String versionComment )
    throws KettleException {
    TransMeta transMeta = (TransMeta) element;
    // First store the databases and other depending objects in the transformation.
    //

    // Only store if the database has actually changed or doesn't have an object ID (imported)
    //
    for ( DatabaseMeta databaseMeta : transMeta.getDatabases() ) {
      if ( databaseMeta.hasChanged() || databaseMeta.getObjectId() == null ) {
        if ( databaseMeta.getObjectId() == null
            || unifiedRepositoryConnectionAclService.hasAccess( databaseMeta.getObjectId(),
                RepositoryFilePermission.WRITE ) ) {
          repo.save( databaseMeta, versionComment, null );
        } else {
          log.logError( BaseMessages.getString( PKG, "PurRepository.ERROR_0004_DATABASE_UPDATE_ACCESS_DENIED",
              databaseMeta.getName() ) );
        }
      }
    }

    // Store the slave servers...
    //
    for ( SlaveServer slaveServer : transMeta.getSlaveServers() ) {
      if ( slaveServer.hasChanged() || slaveServer.getObjectId() == null ) {
        repo.save( slaveServer, versionComment, null );
      }
    }

    // Store the cluster schemas
    //
    for ( ClusterSchema clusterSchema : transMeta.getClusterSchemas() ) {
      if ( clusterSchema.hasChanged() || clusterSchema.getObjectId() == null ) {
        repo.save( clusterSchema, versionComment, null );
      }
    }

    // Save the partition schemas
    //
    for ( PartitionSchema partitionSchema : transMeta.getPartitionSchemas() ) {
      if ( partitionSchema.hasChanged() || partitionSchema.getObjectId() == null ) {
        repo.save( partitionSchema, versionComment, null );
      }
    }

  }

}
