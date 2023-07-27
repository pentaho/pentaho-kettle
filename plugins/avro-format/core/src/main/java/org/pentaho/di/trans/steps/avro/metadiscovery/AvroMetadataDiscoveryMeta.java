/*******************************************************************************
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 - 2023 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 ******************************************************************************/
package org.pentaho.di.trans.steps.avro.metadiscovery;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.List;

@Step( id = "AvroMetadataDiscovery", image = "AvroMetadataDiscovery.svg",
  i18nPackageName = "com.pentaho.di.trans.steps.avrometadiscovery", name = "AvroMetadataDiscovery.Name",
  description = "AvroMetadataDiscovery.Description",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.MetadataDiscovery", documentationUrl =
  "Products/Avro_Metadata_Discovery" )
@InjectionSupported( localizationPrefix = "AvroMetadataDiscovery.Injection." )
public class AvroMetadataDiscoveryMeta extends BaseStepMeta implements StepMetaInterface {

  public static final Class<?> PKG = AvroMetadataDiscoveryMeta.class;

  private static final String DATA_LOCATION = "dataLocation";

  private static final String DATA_LOCATION_TYPE = "dataLocationType";

  private static final String SCHEMA_LOCATION = "schemaLocation";

  private static final String SCHEMA_LOCATION_TYPE = "schemaLocationType";

  private static final String SOURCE_FORMAT = "sourceFormat";

  private static final String AVRO_PATH_FIELD_NAME = "avroPathFieldName";

  private static final String AVRO_NULLABLE_FIELD_NAME = "nullableFieldName";

  private static final String AVRO_TYPE_FIELD_NAME = "avroTypeFieldName";

  private static final String AVRO_KETTLE_TYPE_NAME = "kettleTypeFieldName";

  private static final String IS_CACHE_SCHEMAS = "isCacheSchemas";

  @Injection( name = "AVRO_PATH_FIELD_NAME" )
  private String avroPathFieldName;

  @Injection( name = "AVRO_NULLABLE_FIELD_NAME" )
  private String nullableFieldName;

  @Injection( name = "AVRO_TYPE_FIELD_NAME" )
  private String avroTypeFieldName;

  @Injection( name = "AVRO_KETTLE_TYPE_NAME" )
  private String kettleTypeFieldName;

  @Injection( name = "DATA_FORMAT" )
  private int format;

  @Injection( name = "DATA_LOCATION" )
  private String dataLocation;

  public static enum SourceFormat {
    AVRO_USE_SCHEMA, DATUM_JSON, DATUM_BINARY, AVRO_ALT_SCHEMA;

    public static final SourceFormat[] values = values();
  }

  public static enum LocationDescriptor {
    FILE_NAME, FIELD_NAME, FIELD_CONTAINING_FILE_NAME;
  }

  @Injection( name = "SCHEMA_LOCATION" )
  private String schemaLocation;

  @Injection( name = "DATA_LOCATION_TYPE" )
  @VisibleForTesting
  public int dataLocationType = LocationDescriptor.FILE_NAME.ordinal();

  @Injection( name = "SCHEMA_LOCATION_TYPE" )
  public int schemaLocationType = LocationDescriptor.FILE_NAME.ordinal();

  private boolean isCacheSchemas;


  @Override
  public void setDefault() {
    setAvroPathFieldName( "avro_path" );
    setAvroTypeFieldName( "avro_type" );
    setNullableFieldName( "nullable_field" );
    setKettleTypeFieldName( "pdi_type" );
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new AvroMetadataDiscovery( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public String getDialogClassName() {
    return AvroMetadataDiscoveryDialog.class.getName();
  }

  @Override
  public StepDataInterface getStepData() {
    return new AvroMetadataDiscoveryData();
  }

  public String getAvroPathFieldName() {
    return avroPathFieldName;
  }

  public void setAvroPathFieldName( String avroPathFieldName ) {
    this.avroPathFieldName = avroPathFieldName;
  }

  public String getNullableFieldName() {
    return nullableFieldName;
  }

  public void setNullableFieldName( String nullableFieldName ) {
    this.nullableFieldName = nullableFieldName;
  }

  public String getKettleTypeFieldName() {
    return kettleTypeFieldName;
  }

  public void setKettleTypeFieldName( String kettleTypeFieldName ) {
    this.kettleTypeFieldName = kettleTypeFieldName;
  }

  public String getAvroTypeFieldName() {
    return avroTypeFieldName;
  }

  public void setAvroTypeFieldName( String avroTypeFieldName ) {
    this.avroTypeFieldName = avroTypeFieldName;
  }

  public void setFormat( int formatIndex ) {
    this.format = formatIndex;
  }

  public int getFormat() {
    return this.format;
  }

  public String getDataLocation() {
    return dataLocation;
  }

  public void setDataLocation( String dataLocation, LocationDescriptor locationDescriptor ) {
    this.dataLocation = dataLocation;
    this.dataLocationType = locationDescriptor.ordinal();
  }

  public String getSchemaLocation() {
    return schemaLocation;
  }

  public void setSchemaLocation( String schemaLocation, LocationDescriptor locationDescriptor ) {
    this.schemaLocation = schemaLocation;
    this.schemaLocationType = locationDescriptor.ordinal();
  }

  public LocationDescriptor getDataLocationType() {
    return LocationDescriptor.values()[ dataLocationType ];
  }

  public LocationDescriptor getSchemaLocationType() {
    return LocationDescriptor.values()[ schemaLocationType ];
  }

  public boolean isCacheSchemas() {
    return isCacheSchemas;
  }

  public void setCacheSchemas( boolean cacheSchemas ) {
    isCacheSchemas = cacheSchemas;
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore )
    throws KettleStepException {

    if ( !Utils.isEmpty( Collections.singletonList( getFormat() ) ) ) {
      ValueMetaInterface sourceFormat = new ValueMetaString( String.valueOf( format ) );
      sourceFormat.setOrigin( name );
      sourceFormat.setName( "Data format" );
      sourceFormat.setComments( BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.SourceFormat" ) );
      inputRowMeta.addValueMeta( sourceFormat );
    }

    if ( !Utils.isEmpty( getDataLocation() ) ) {
      ValueMetaInterface dataLocation = new ValueMetaString( getDataLocation() );
      dataLocation.setOrigin( name );
      dataLocation.setName( "Data location" );
      dataLocation.setComments( BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.DataLocation" ) );
      inputRowMeta.addValueMeta( dataLocation );
    }

    if ( !Utils.isEmpty( Collections.singletonList( getDataLocationType() ) ) ) {
      ValueMetaInterface dataLocationType = new ValueMetaString( String.valueOf( getDataLocationType().ordinal() ) );
      dataLocationType.setOrigin( name );
      dataLocationType.setName( "Data location type" );
      dataLocationType.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.DataLocationType" ) );
      inputRowMeta.addValueMeta( dataLocationType );
    }

    if ( !Utils.isEmpty( getSchemaLocation() ) ) {
      ValueMetaInterface schemaLocation = new ValueMetaString( getSchemaLocation() );
      schemaLocation.setOrigin( name );
      schemaLocation.setName( "Schema location" );
      schemaLocation.setComments( BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.SchemaLocation" ) );
      inputRowMeta.addValueMeta( schemaLocation );
    }

    if ( !Utils.isEmpty( Collections.singletonList( getSchemaLocationType() ) ) ) {
      ValueMetaInterface schemaLocationType =
        new ValueMetaString( String.valueOf( getSchemaLocationType().ordinal() ) );
      schemaLocationType.setOrigin( name );
      schemaLocationType.setName( "Schema location type" );
      schemaLocationType.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.SchemaLocationType" ) );
      inputRowMeta.addValueMeta( schemaLocationType );
    }

    if ( !Utils.isEmpty( getAvroPathFieldName() ) ) {
      ValueMetaInterface avroPathFieldName = new ValueMetaString( getAvroPathFieldName() );
      avroPathFieldName.setOrigin( name );
      avroPathFieldName.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.AvroPathFieldName" ) );
      inputRowMeta.addValueMeta( avroPathFieldName );
    }

    if ( !Utils.isEmpty( getNullableFieldName() ) ) {
      ValueMetaInterface avroNullableFieldName = new ValueMetaBoolean( getNullableFieldName() );
      avroNullableFieldName.setOrigin( name );
      avroNullableFieldName.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.AvroNullableFieldName" ) );
      inputRowMeta.addValueMeta( avroNullableFieldName );
    }

    if ( !Utils.isEmpty( getAvroTypeFieldName() ) ) {
      ValueMetaInterface avroTypeFieldName = new ValueMetaString( getAvroTypeFieldName() );
      avroTypeFieldName.setOrigin( name );
      avroTypeFieldName.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.AvroTypeFieldName" ) );
      inputRowMeta.addValueMeta( avroTypeFieldName );
    }

    if ( !Utils.isEmpty( getKettleTypeFieldName() ) ) {
      ValueMetaInterface avroKettleTypeFieldName = new ValueMetaString( getKettleTypeFieldName() );
      avroKettleTypeFieldName.setOrigin( name );
      avroKettleTypeFieldName.setComments(
        BaseMessages.getString( PKG, "AvroMetadataDiscovery.FieldComment.AvroKettleTypeFieldName" ) );
      inputRowMeta.addValueMeta( avroKettleTypeFieldName );
    }

  }

  /**
   * Method to get the AvroSpec.DataType ID by its logical type if available
   *
   * @param avroType
   * @param logicalType
   * @return String value of Avro Type
   */
  public String setAvroFieldType( String avroType, String logicalType ) {
    String avroFieldType = null;
    for ( AvroSpec.DataType dataType : AvroSpec.DataType.values() ) {
      if ( dataType.getBaseType().equalsIgnoreCase( avroType ) ) {
        // Logical Type will be null for Data Types: NULL, BOOLEAN, INTEGER, LONG, FLOAT, DOUBLE, BYTES, STRING, RECORD,
        // ENUM, ARRAY, MAP, FIXED. Just get the ID.
        if ( logicalType == null ) {
          avroFieldType = String.valueOf( ( dataType.getId() ) );
          break;
          // For Data Types: DECIMAL, DATE, TIME_MILLIS, TIME_MICROS, TIMESTAMP_MILLIS, TIMESTAMP_MICROS, DURATION,
          // DECIMAL_FIXED. Get the ID when the Logical Type matches the one passed in.
        } else if ( dataType.getLogicalType() != null && dataType.getLogicalType().equalsIgnoreCase( logicalType ) ) {
          avroFieldType = String.valueOf( ( dataType.getId() ) );
          break;
        }
      }
    }
    return avroFieldType;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metastore ) throws KettleXMLException {
    try {
      dataLocation =
        XMLHandler.getTagValue( stepnode, DATA_LOCATION ) == null ? XMLHandler.getTagValue( stepnode, "fileName" )
          : XMLHandler.getTagValue( stepnode, DATA_LOCATION );
      format = XMLHandler.getTagValue( stepnode, SOURCE_FORMAT ) == null ? LocationDescriptor.FILE_NAME.ordinal()
        : Integer.parseInt( XMLHandler.getTagValue( stepnode, SOURCE_FORMAT ) );
      dataLocationType =
        XMLHandler.getTagValue( stepnode, DATA_LOCATION_TYPE ) == null ? LocationDescriptor.FILE_NAME.ordinal()
          : Integer.parseInt( XMLHandler.getTagValue( stepnode, DATA_LOCATION_TYPE ) );
      schemaLocation = XMLHandler.getTagValue( stepnode, SCHEMA_LOCATION );
      schemaLocationType =
        XMLHandler.getTagValue( stepnode, SCHEMA_LOCATION_TYPE ) == null ? LocationDescriptor.FILE_NAME.ordinal()
          : Integer.parseInt( XMLHandler.getTagValue( stepnode, SCHEMA_LOCATION_TYPE ) );
      isCacheSchemas = Boolean.TRUE.equals( ValueMetaBase.convertStringToBoolean(
        XMLHandler.getTagValue( stepnode, IS_CACHE_SCHEMAS ) == null ? "false"
          : XMLHandler.getTagValue( stepnode, IS_CACHE_SCHEMAS ) ) );
      avroPathFieldName = XMLHandler.getTagValue( stepnode, AVRO_PATH_FIELD_NAME );
      avroTypeFieldName = XMLHandler.getTagValue( stepnode, AVRO_TYPE_FIELD_NAME );
      nullableFieldName = XMLHandler.getTagValue( stepnode, AVRO_NULLABLE_FIELD_NAME );
      kettleTypeFieldName = XMLHandler.getTagValue( stepnode, AVRO_KETTLE_TYPE_NAME );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer( 800 );
    final String INDENT = "    ";

    retval.append( INDENT ).append( XMLHandler.addTagValue( DATA_LOCATION, getDataLocation() ) );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( getDataLocation() );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SOURCE_FORMAT, getFormat() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( DATA_LOCATION_TYPE, dataLocationType ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SCHEMA_LOCATION, getSchemaLocation() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( SCHEMA_LOCATION_TYPE, schemaLocationType ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( IS_CACHE_SCHEMAS, isCacheSchemas() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( AVRO_PATH_FIELD_NAME, getAvroPathFieldName() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( AVRO_TYPE_FIELD_NAME, getAvroTypeFieldName() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( AVRO_NULLABLE_FIELD_NAME, getNullableFieldName() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( AVRO_KETTLE_TYPE_NAME, getKettleTypeFieldName() ) );

    return retval.toString();
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transformationId, ObjectId stepId )
    throws KettleException {
    try {
      rep.saveStepAttribute( transformationId, stepId, DATA_LOCATION, getDataLocation() );
      rep.saveStepAttribute( transformationId, stepId, SOURCE_FORMAT, getFormat() );
      rep.saveStepAttribute( transformationId, stepId, DATA_LOCATION_TYPE, dataLocationType );
      rep.saveStepAttribute( transformationId, stepId, SCHEMA_LOCATION, getSchemaLocation() );
      rep.saveStepAttribute( transformationId, stepId, SCHEMA_LOCATION_TYPE, schemaLocationType );
      rep.saveStepAttribute( transformationId, stepId, IS_CACHE_SCHEMAS, isCacheSchemas() );
      rep.saveStepAttribute( transformationId, stepId, AVRO_PATH_FIELD_NAME, getAvroPathFieldName() );
      rep.saveStepAttribute( transformationId, stepId, AVRO_TYPE_FIELD_NAME, getAvroTypeFieldName() );
      rep.saveStepAttribute( transformationId, stepId, AVRO_NULLABLE_FIELD_NAME, getNullableFieldName() );
      rep.saveStepAttribute( transformationId, stepId, AVRO_KETTLE_TYPE_NAME, getKettleTypeFieldName() );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step into repository: " + stepId, e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId stepId, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      dataLocation = rep.getStepAttributeString( stepId, DATA_LOCATION );
      format = (int) rep.getStepAttributeInteger( stepId, SOURCE_FORMAT );
      dataLocationType = (int) rep.getStepAttributeInteger( stepId, DATA_LOCATION_TYPE );
      schemaLocation = rep.getStepAttributeString( stepId, SCHEMA_LOCATION );
      schemaLocationType = (int) rep.getStepAttributeInteger( stepId, SCHEMA_LOCATION_TYPE );
      isCacheSchemas = rep.getStepAttributeBoolean( stepId, IS_CACHE_SCHEMAS );
      avroPathFieldName = rep.getStepAttributeString( stepId, AVRO_PATH_FIELD_NAME );
      avroTypeFieldName = rep.getStepAttributeString( stepId, AVRO_TYPE_FIELD_NAME );
      nullableFieldName = rep.getStepAttributeString( stepId, AVRO_NULLABLE_FIELD_NAME );
      kettleTypeFieldName = rep.getStepAttributeString( stepId, AVRO_KETTLE_TYPE_NAME );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to load step from repository", e );
    }
  }


}
