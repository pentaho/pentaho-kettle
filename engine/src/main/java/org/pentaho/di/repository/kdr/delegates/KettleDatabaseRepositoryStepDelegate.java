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

package org.pentaho.di.repository.kdr.delegates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.trans.step.RowDistributionInterface;
import org.pentaho.di.trans.step.RowDistributionPluginType;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.missing.MissingTrans;

public class KettleDatabaseRepositoryStepDelegate extends KettleDatabaseRepositoryBaseDelegate {
  private static Class<?> PKG = StepMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String STEP_ATTRIBUTE_PREFIX = "_ATTR_" + '\t';

  public KettleDatabaseRepositoryStepDelegate( KettleDatabaseRepository repository ) {
    super( repository );
  }

  public synchronized ObjectId getStepTypeID( String code ) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ), code );
  }

  public ObjectId[] getStepTypeIDs( String[] codes, int amount ) throws KettleException {
    if ( amount != codes.length ) {
      String[] tmp = new String[ amount ];
      System.arraycopy( codes, 0, tmp, 0, amount );
      codes = tmp;
    }
    return repository.connectionDelegate.getIDsWithValues(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ), codes );
  }

  public Map<String, LongObjectId> getStepTypeCodeToIdMap() throws KettleException {
    return repository.connectionDelegate.getValueToIdMap(
        quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
        quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ),
        quote( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ) );
  }

  public synchronized ObjectId getStepID( String name, ObjectId id_transformation ) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP ), quote( KettleDatabaseRepository.FIELD_STEP_ID_STEP ),
      quote( KettleDatabaseRepository.FIELD_STEP_NAME ), name,
      quote( KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION ), id_transformation );
  }

  public synchronized String getStepTypeCode( ObjectId id_database_type ) throws KettleException {
    return repository.connectionDelegate.getStringWithID(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ), id_database_type,
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE ) );
  }

  public RowMetaAndData getStep( ObjectId id_step ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP ), quote( KettleDatabaseRepository.FIELD_STEP_ID_STEP ),
      id_step );
  }

  public RowMetaAndData getStepType( ObjectId id_step_type ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP_TYPE ),
      quote( KettleDatabaseRepository.FIELD_STEP_TYPE_ID_STEP_TYPE ), id_step_type );
  }

  public RowMetaAndData getStepAttribute( ObjectId id_step_attribute ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_STEP_ATTRIBUTE ),
      quote( KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE ), id_step_attribute );
  }

  /**
   * Create a new step by loading the metadata from the specified repository.
   *
   * @param rep
   * @param stepId
   * @param databases
   * @param counters
   * @param partitionSchemas
   * @throws KettleException
   */
  public StepMeta loadStepMeta( ObjectId stepId, List<DatabaseMeta> databases,
    List<PartitionSchema> partitionSchemas ) throws KettleException {
    StepMeta stepMeta = new StepMeta();
    PluginRegistry registry = PluginRegistry.getInstance();

    try {
      RowMetaAndData r = getStep( stepId );
      if ( r != null ) {
        stepMeta.setObjectId( stepId );

        stepMeta.setName( r.getString( KettleDatabaseRepository.FIELD_STEP_NAME, null ) );
        stepMeta.setDescription( r.getString( KettleDatabaseRepository.FIELD_STEP_DESCRIPTION, null ) );

        long id_step_type = r.getInteger( KettleDatabaseRepository.FIELD_STEP_ID_STEP_TYPE, -1L );
        RowMetaAndData steptyperow = getStepType( new LongObjectId( id_step_type ) );

        stepMeta.setStepID( steptyperow.getString( KettleDatabaseRepository.FIELD_STEP_TYPE_CODE, null ) );
        stepMeta.setDistributes( r.getBoolean( KettleDatabaseRepository.FIELD_STEP_DISTRIBUTE, true ) );
        int copies = (int) r.getInteger( KettleDatabaseRepository.FIELD_STEP_COPIES, 1 );
        String copiesString = r.getString( KettleDatabaseRepository.FIELD_STEP_COPIES_STRING, null );
        if ( !Utils.isEmpty( copiesString ) ) {
          stepMeta.setCopiesString( copiesString );
        } else {
          stepMeta.setCopies( copies );
        }

        int x = (int) r.getInteger( KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_X, 0 );
        int y = (int) r.getInteger( KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_Y, 0 );
        stepMeta.setLocation( new Point( x, y ) );
        stepMeta.setDraw( r.getBoolean( KettleDatabaseRepository.FIELD_STEP_GUI_DRAW, false ) );

        // Generate the appropriate class...
        PluginInterface sp = registry.findPluginWithId( StepPluginType.class, stepMeta.getStepID() );
        if ( sp == null ) {
          stepMeta.setStepMetaInterface( new MissingTrans( stepMeta.getName(), stepMeta.getStepID() ) );
        } else {
          stepMeta.setStepMetaInterface( (StepMetaInterface) registry.loadClass( sp ) );
        }
        if ( stepMeta.getStepMetaInterface() != null ) {
          // Read the step info from the repository!
          readRepCompatibleStepMeta(
            stepMeta.getStepMetaInterface(), repository, stepMeta.getObjectId(), databases );
          stepMeta.getStepMetaInterface().readRep(
            repository, repository.metaStore, stepMeta.getObjectId(), databases );
        }

        // Get the partitioning as well...
        //
        stepMeta.setStepPartitioningMeta( loadStepPartitioningMeta( stepMeta.getObjectId() ) );
        stepMeta.getStepPartitioningMeta().setPartitionSchemaAfterLoading( partitionSchemas );

        // Get the cluster schema name
        //
        stepMeta.setClusterSchemaName( repository.getStepAttributeString( stepId, "cluster_schema" ) );

        // Are we using a custom row distribution plugin?
        //
        String rowDistributionCode = repository.getStepAttributeString( stepId, 0, "row_distribution_code" );
        RowDistributionInterface rowDistribution =
          PluginRegistry.getInstance().loadClass(
            RowDistributionPluginType.class, rowDistributionCode, RowDistributionInterface.class );
        stepMeta.setRowDistribution( rowDistribution );

        // Load the attribute groups map
        //
        stepMeta.setAttributesMap( loadStepAttributesMap( stepId ) );

        // Done!
        //
        return stepMeta;
      } else {
        throw new KettleException( BaseMessages.getString(
          PKG, "StepMeta.Exception.StepInfoCouldNotBeFound", String.valueOf( stepId ) ) );
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "StepMeta.Exception.StepCouldNotBeLoaded", String
        .valueOf( stepMeta.getObjectId() ) ), dbe );
    }
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
  private void readRepCompatibleStepMeta( StepMetaInterface stepMetaInterface,
    KettleDatabaseRepository repository, ObjectId objectId, List<DatabaseMeta> databases ) throws KettleException {
    stepMetaInterface.readRep( repository, objectId, databases, null );
  }

  public void saveStepMeta( StepMeta stepMeta, ObjectId transformationId ) throws KettleException {
    try {
      log.logDebug( BaseMessages.getString( PKG, "StepMeta.Log.SaveNewStep" ) );
      // Insert new Step in repository
      stepMeta.setObjectId( insertStep(
        transformationId, stepMeta.getName(), stepMeta.getDescription(), stepMeta.getStepID(), stepMeta
          .isDistributes(), stepMeta.getCopies(), stepMeta.getLocation() == null
          ? -1 : stepMeta.getLocation().x, stepMeta.getLocation() == null ? -1 : stepMeta.getLocation().y,
        stepMeta.isDrawn(), stepMeta.getCopiesString() ) );

      // Save partitioning selection for the step
      //
      repository.stepDelegate.saveStepPartitioningMeta(
        stepMeta.getStepPartitioningMeta(), transformationId, stepMeta.getObjectId() );

      // The id_step is known, as well as the id_transformation
      // This means we can now save the attributes of the step...
      //
      log.logDebug( BaseMessages.getString( PKG, "StepMeta.Log.SaveStepDetails" ) );
      compatibleSaveRep( stepMeta.getStepMetaInterface(), repository, transformationId, stepMeta.getObjectId() );
      stepMeta.getStepMetaInterface().saveRep(
        repository, repository.metaStore, transformationId, stepMeta.getObjectId() );

      // Save the name of the clustering schema that was chosen.
      //
      repository.saveStepAttribute( transformationId, stepMeta.getObjectId(), "cluster_schema", stepMeta
        .getClusterSchema() == null ? "" : stepMeta.getClusterSchema().getName() );

      // Save the row distribution code (plugin ID)
      //
      repository.saveStepAttribute( transformationId, stepMeta.getObjectId(), "row_distribution_code", stepMeta
        .getRowDistribution() == null ? null : stepMeta.getRowDistribution().getCode() );

      // Save the attribute groups map
      //
      saveAttributesMap( transformationId, stepMeta.getObjectId(), stepMeta.getAttributesMap() );
    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "StepMeta.Exception.UnableToSaveStepInfo", String
        .valueOf( transformationId ) ), e );
    }
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleSaveRep( StepMetaInterface stepMetaInterface, KettleDatabaseRepository repository,
    ObjectId id_transformation, ObjectId objectId ) throws KettleException {
    stepMetaInterface.saveRep( repository, id_transformation, objectId );
  }

  public void saveStepErrorMeta( StepErrorMeta meta, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_source_step", meta
      .getSourceStep() != null ? meta.getSourceStep().getName() : "" );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_target_step", meta
      .getTargetStep() != null ? meta.getTargetStep().getName() : "" );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_is_enabled", meta.isEnabled() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_nr_valuename", meta
      .getNrErrorsValuename() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_descriptions_valuename", meta
      .getErrorDescriptionsValuename() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_fields_valuename", meta
      .getErrorFieldsValuename() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_codes_valuename", meta
      .getErrorCodesValuename() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_max_errors", meta
      .getMaxErrors() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_max_pct_errors", meta
      .getMaxPercentErrors() );
    repository.saveStepAttribute( id_transformation, id_step, "step_error_handling_min_pct_rows", meta
      .getMinPercentRows() );
  }

  public StepErrorMeta loadStepErrorMeta( VariableSpace variables, StepMeta stepMeta, List<StepMeta> steps ) throws KettleException {
    StepErrorMeta meta = new StepErrorMeta( variables, stepMeta );

    meta.setTargetStep( StepMeta.findStep( steps, repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_target_step" ) ) );
    meta
      .setEnabled( repository.getStepAttributeBoolean( stepMeta.getObjectId(), "step_error_handling_is_enabled" ) );
    meta.setNrErrorsValuename( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_nr_valuename" ) );
    meta.setErrorDescriptionsValuename( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_descriptions_valuename" ) );
    meta.setErrorFieldsValuename( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_fields_valuename" ) );
    meta.setErrorCodesValuename( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_codes_valuename" ) );
    meta.setMaxErrors( repository
      .getStepAttributeString( stepMeta.getObjectId(), "step_error_handling_max_errors" ) );
    meta.setMaxPercentErrors( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_max_pct_errors" ) );
    meta.setMinPercentRows( repository.getStepAttributeString(
      stepMeta.getObjectId(), "step_error_handling_min_pct_rows" ) );

    return meta;
  }

  public StepPartitioningMeta loadStepPartitioningMeta( ObjectId id_step ) throws KettleException {
    StepPartitioningMeta stepPartitioningMeta = new StepPartitioningMeta();

    stepPartitioningMeta.setPartitionSchemaName( repository
      .getStepAttributeString( id_step, "PARTITIONING_SCHEMA" ) );
    String methodCode = repository.getStepAttributeString( id_step, "PARTITIONING_METHOD" );
    stepPartitioningMeta.setMethod( StepPartitioningMeta.getMethod( methodCode ) );
    if ( stepPartitioningMeta.getPartitioner() != null ) {
      stepPartitioningMeta.getPartitioner().loadRep( repository, id_step );
    }
    stepPartitioningMeta.hasChanged( true );

    return stepPartitioningMeta;
  }

  /**
   * Saves partitioning properties in the repository for the given step.
   *
   * @param meta
   *          the partitioning metadata to store.
   * @param id_transformation
   *          the ID of the transformation
   * @param id_step
   *          the ID of the step
   * @throws KettleDatabaseException
   *           In case anything goes wrong
   *
   */
  public void saveStepPartitioningMeta( StepPartitioningMeta meta, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    repository.saveStepAttribute(
      id_transformation, id_step, "PARTITIONING_SCHEMA", meta.getPartitionSchema() != null ? meta
        .getPartitionSchema().getName() : "" ); // selected schema
    repository.saveStepAttribute( id_transformation, id_step, "PARTITIONING_METHOD", meta.getMethodCode() );
    if ( meta.getPartitioner() != null ) {
      meta.getPartitioner().saveRep( repository, id_transformation, id_step );
    }
  }

  //CHECKSTYLE:LineLength:OFF
  public synchronized ObjectId insertStep( ObjectId id_transformation, String name, String description,
    String steptype, boolean distribute, long copies, long gui_location_x, long gui_location_y,
    boolean gui_draw, String copiesString ) throws KettleException {
    ObjectId id = repository.connectionDelegate.getNextStepID();

    ObjectId id_step_type = getStepTypeID( steptype );

    RowMetaAndData table = new RowMetaAndData();

    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_ID_STEP ), id );
    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION ), id_transformation );
    table.addValue( new ValueMetaString( KettleDatabaseRepository.FIELD_STEP_NAME ), name );
    table.addValue( new ValueMetaString( KettleDatabaseRepository.FIELD_STEP_DESCRIPTION ), description );
    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_ID_STEP_TYPE ), id_step_type );
    table.addValue( new ValueMetaBoolean( KettleDatabaseRepository.FIELD_STEP_DISTRIBUTE ), Boolean.valueOf( distribute ) );
    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_COPIES ), new Long( copies ) );
    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_X ), new Long( gui_location_x ) );
    table.addValue( new ValueMetaInteger( KettleDatabaseRepository.FIELD_STEP_GUI_LOCATION_Y ), new Long( gui_location_y ) );
    table.addValue( new ValueMetaBoolean( KettleDatabaseRepository.FIELD_STEP_GUI_DRAW ), Boolean.valueOf( gui_draw ) );
    table.addValue( new ValueMetaString( KettleDatabaseRepository.FIELD_STEP_COPIES_STRING ), copiesString );

    repository.connectionDelegate.getDatabase().prepareInsert( table.getRowMeta(), KettleDatabaseRepository.TABLE_R_STEP );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public synchronized int getNrSteps( ObjectId id_transformation ) throws KettleException {
    int retval = 0;

    RowMetaAndData par = repository.connectionDelegate.getParameterMetaData( id_transformation );
    String sql =
      "SELECT COUNT(*) FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_STEP ) + " WHERE "
        + quote( KettleDatabaseRepository.FIELD_STEP_ID_TRANSFORMATION ) + " = ? ";
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql, par.getRowMeta(), par.getData() );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0L );
    }

    return retval;
  }

  public synchronized int getNrStepAttributes( ObjectId id_step ) throws KettleException {
    int retval = 0;

    RowMetaAndData par = repository.connectionDelegate.getParameterMetaData( id_step );
    String sql =
      "SELECT COUNT(*) FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_STEP_ATTRIBUTE ) + " WHERE "
        + quote( KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_ID_STEP ) + " = ? ";
    RowMetaAndData r = repository.connectionDelegate.getOneRow( sql, par.getRowMeta(), par.getData() );
    if ( r != null ) {
      retval = (int) r.getInteger( 0, 0L );
    }

    return retval;
  }

  private void saveAttributesMap( ObjectId transformationId, ObjectId stepId,
    Map<String, Map<String, String>> attributesMap ) throws KettleException {
    for ( final String groupName : attributesMap.keySet() ) {
      Map<String, String> attributes = attributesMap.get( groupName );
      for ( final String key : attributes.keySet() ) {
        final String value = attributes.get( key );
        if ( key != null && value != null ) {
          repository.connectionDelegate.insertStepAttribute( transformationId, stepId, 0, STEP_ATTRIBUTE_PREFIX
            + groupName + '\t' + key, 0, value );
        }
      }
    }
  }

  private Map<String, Map<String, String>> loadStepAttributesMap( ObjectId stepId ) throws KettleException {
    Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();

    List<Object[]> attributeRows = repository.connectionDelegate.getStepAttributesBuffer();
    RowMetaInterface rowMeta = repository.connectionDelegate.getStepAttributesRowMeta();
    for ( Object[] attributeRow : attributeRows ) {
      String code = rowMeta.getString( attributeRow, KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_CODE, null );
      if ( code != null && code.startsWith( STEP_ATTRIBUTE_PREFIX ) ) {
        String value =
          rowMeta.getString( attributeRow, KettleDatabaseRepository.FIELD_STEP_ATTRIBUTE_VALUE_STR, null );
        if ( value != null ) {
          code = code.substring( STEP_ATTRIBUTE_PREFIX.length() );
          int tabIndex = code.indexOf( '\t' );
          if ( tabIndex > 0 ) {
            String groupName = code.substring( 0, tabIndex );
            String key = code.substring( tabIndex + 1 );
            Map<String, String> attributes = attributesMap.get( groupName );
            if ( attributes == null ) {
              attributes = new HashMap<String, String>();
              attributesMap.put( groupName, attributes );
            }
            attributes.put( key, value );
          }
        }
      }
    }

    return attributesMap;
  }
}
