/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.avro.input;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.trans.steps.avro.AvroTypeConverter;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.AliasedFileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.workarounds.ResolvableResource;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class AvroInputMetaBase extends BaseStepMeta implements StepMetaInterface, ResolvableResource {

  public static final Class<?> PKG = AvroInputMetaBase.class;

  public static enum LocationDescriptor {
    FILE_NAME, FIELD_NAME, FIELD_CONTAINING_FILE_NAME;
  }

  public static enum SourceFormat {
    AVRO_USE_SCHEMA, DATUM_JSON, DATUM_BINARY, AVRO_ALT_SCHEMA;

    public static final SourceFormat[] values = values();
  }

  @Injection( name = "DATA_LOCATION" )
  private String dataLocation;

  @Injection( name = "DATA_LOCATION_TYPE" )
  @VisibleForTesting
  public int dataLocationType = LocationDescriptor.FILE_NAME.ordinal();

  @Injection( name = "SCHEMA_LOCATION" )
  private String schemaLocation;

  @Injection( name = "SCHEMA_LOCATION_TYPE" )
  private int schemaLocationType = LocationDescriptor.FILE_NAME.ordinal();

  private boolean isCacheSchemas;

  @Injection( name = "ALLOW_NULL_FOR_MISSING_FIELDS" )
  private boolean allowNullForMissingFields;

  @Injection( name = "PASS_THROUGH_FIELDS" )
  public boolean passingThruFields;

  @Injection( name = "DATA_FORMAT" )
  private int format;

  @InjectionDeep
  public AvroInputField[] inputFields = new AvroInputField[ 0 ];

  @InjectionDeep
  private List<AvroLookupField> lookupFields = new ArrayList<>();

  public List<AvroLookupField> getLookupFields() {
    return lookupFields;
  }

  public void setLookupFields( List<AvroLookupField> lookupFields ) {
    this.lookupFields = lookupFields;
  }


  public String getDataLocation() {
    return dataLocation;
  }

  public void setDataLocation( String dataLocation, LocationDescriptor locationDescriptor ) {
    this.dataLocation = dataLocation;
    this.dataLocationType = locationDescriptor.ordinal();
  }

  public LocationDescriptor getDataLocationType() {
    return LocationDescriptor.values()[ dataLocationType ];
  }

  public void setFormat( int formatIndex ) {
    this.format = formatIndex;
  }

  public int getFormat() {
    return this.format;
  }

  public String getSchemaLocation() {
    return schemaLocation;
  }

  public void setSchemaLocation( String schemaLocation, LocationDescriptor locationDescriptor ) {
    this.schemaLocation = schemaLocation;
    this.schemaLocationType = locationDescriptor.ordinal();
  }

  @Override
  public void setDefault() {
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

  public boolean isAllowNullForMissingFields() {
    return allowNullForMissingFields;
  }

  public void setAllowNullForMissingFields( boolean allowNullForMissingFields ) {
    this.allowNullForMissingFields = allowNullForMissingFields;
  }


  /**
   * TODO: remove from base
   */
  public String getEncoding() {
    return null;
  }

  public AvroInputField[] getInputFields() {
    return this.inputFields;
  }

  public void setInputFields( AvroInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  public void setInputFields( List<AvroInputField> inputFields ) {
    this.inputFields = new AvroInputField[ inputFields.size() ];
    this.inputFields = inputFields.toArray( this.inputFields );
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, metaStore );
  }

  private void readData( Node stepnode, IMetaStore metastore ) throws KettleXMLException {
    try {
      String passFileds = XMLHandler.getTagValue( stepnode, "passing_through_fields" ) == null ? "false" : XMLHandler.getTagValue( stepnode, "passing_through_fields" );
      passingThruFields = ValueMetaBase.convertStringToBoolean( passFileds );

      dataLocation =
        XMLHandler.getTagValue( stepnode, "dataLocation" ) == null ? XMLHandler.getTagValue( stepnode, "fileName" )
          : XMLHandler.getTagValue( stepnode, "dataLocation" );
      format = XMLHandler.getTagValue( stepnode, "sourceFormat" ) == null ? LocationDescriptor.FILE_NAME.ordinal() : Integer.parseInt( XMLHandler.getTagValue( stepnode, "sourceFormat" ) );
      dataLocationType = XMLHandler.getTagValue( stepnode, "dataLocationType" ) == null ? LocationDescriptor.FILE_NAME.ordinal() : Integer.parseInt( XMLHandler.getTagValue( stepnode, "dataLocationType" ) );
      schemaLocation = XMLHandler.getTagValue( stepnode, "schemaLocation" );
      schemaLocationType = XMLHandler.getTagValue( stepnode, "schemaLocationType" ) == null ? LocationDescriptor.FILE_NAME.ordinal() : Integer.parseInt( XMLHandler.getTagValue( stepnode, "schemaLocationType" ) );
      isCacheSchemas = ValueMetaBase.convertStringToBoolean( XMLHandler.getTagValue( stepnode, "isCacheSchemas" ) == null ? "false" : XMLHandler.getTagValue( stepnode, "isCacheSchemas" ) );
      allowNullForMissingFields = ValueMetaBase.convertStringToBoolean( XMLHandler.getTagValue( stepnode, "allowNullForMissingFields" ) == null ? "false" : XMLHandler.getTagValue( stepnode, "allowNullForMissingFields" ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );
      this.inputFields = new AvroInputField[ nrfields ];
      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        AvroInputField inputField = new AvroInputField();
        inputField.setFormatFieldName( XMLHandler.getTagValue( fnode, "path" ) );
        inputField.setPentahoFieldName( XMLHandler.getTagValue( fnode, "name" ) );
        inputField.setPentahoType( XMLHandler.getTagValue( fnode, "type" ) );
        String avroType = XMLHandler.getTagValue( fnode, "avro_type" );
        if ( avroType != null && !avroType.equalsIgnoreCase( "null" ) ) {
          inputField.setAvroType( avroType );
        } else {
          inputField.setAvroType( AvroTypeConverter.convertToAvroType( inputField.getPentahoType() ) );
        }
        String stringFormat = XMLHandler.getTagValue( fnode, "format" );
        inputField.setStringFormat( stringFormat == null ? "" : stringFormat );
        String indexedValues = XMLHandler.getTagValue( fnode, "indexed_vals" );
        if ( indexedValues != null && indexedValues.length() > 0 ) {
          inputField.setIndexedValues( indexedValues );
        }
        this.inputFields[ i ] = inputField;
      }

      fields = XMLHandler.getSubNode( stepnode, "lookupFields" );
      nrfields = XMLHandler.countNodes( fields, "lookupField" );
      this.lookupFields = new ArrayList<AvroLookupField>();
      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "lookupField", i );
        AvroLookupField lookupField = new AvroLookupField();
        lookupField.setFieldName( XMLHandler.getTagValue( fnode, "fieldName" ) );
        lookupField.setVariableName( XMLHandler.getTagValue( fnode, "variableName" ) );
        lookupField.setDefaultValue( XMLHandler.getTagValue( fnode, "defaultValue" ) );
        this.lookupFields.add( lookupField );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer( 800 );
    final String INDENT = "    ";

    retval.append( INDENT ).append( XMLHandler.addTagValue( "passing_through_fields", passingThruFields ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "dataLocation", getDataLocation() ) );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( getDataLocation() );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "sourceFormat", getFormat() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "dataLocationType", dataLocationType ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "schemaLocation", getSchemaLocation() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "schemaLocationType", schemaLocationType ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "isCacheSchemas", isCacheSchemas() ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( "allowNullForMissingFields", isAllowNullForMissingFields() ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      AvroInputField field = inputFields[ i ];

      if ( field.getPentahoFieldName() != null && field.getPentahoFieldName().length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "path", field.getAvroFieldName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", field.getPentahoFieldName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "type", field.getTypeDesc() ) );
        AvroSpec.DataType avroDataType = field.getAvroType();
        if ( avroDataType != null && !avroDataType.equals( AvroSpec.DataType.NULL ) ) {
          retval.append( "        " ).append( XMLHandler.addTagValue( "avro_type", avroDataType.getName() ) );
        } else {
          retval.append( "        " ).append( XMLHandler.addTagValue( "avro_type", AvroTypeConverter.convertToAvroType( field.getTypeDesc() ) ) );
        }
        if ( field.getStringFormat() != null ) {
          retval.append( "        " ).append( XMLHandler.addTagValue( "format", field.getStringFormat() ) );
        }
        String indexedValues = field.getIndexedValues();
        if ( indexedValues != null && indexedValues.length() > 0 ) {
          retval.append( "        " ).append( XMLHandler.addTagValue( "indexed_vals", indexedValues ) );
        }
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    <lookupFields>" ).append( Const.CR );
    for ( int i = 0; i < lookupFields.size(); i++ ) {
      AvroLookupField field = lookupFields.get( i );

      if ( field.getFieldName() != null && field.getFieldName().length() != 0 ) {
        retval.append( "      <lookupField>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "fieldName", field.getFieldName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "variableName", field.getVariableName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "defaultValue", field.getDefaultValue() ) );
        retval.append( "      </lookupField>" ).append( Const.CR );
      }
    }
    retval.append( "    </lookupFields>" ).append( Const.CR );


    return retval.toString();
  }

  @Override
  public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      passingThruFields = rep.getStepAttributeBoolean( id_step, "passing_through_fields" );
      dataLocation = rep.getStepAttributeString( id_step, "dataLocation" );
      format = (int) rep.getStepAttributeInteger( id_step, "sourceFormat" );
      dataLocationType = (int) rep.getStepAttributeInteger( id_step, "dataLocationType" );
      schemaLocation = rep.getStepAttributeString( id_step, "schemaLocation" );
      schemaLocationType = (int) rep.getStepAttributeInteger( id_step, "schemaLocationType" );
      isCacheSchemas = rep.getStepAttributeBoolean( id_step, "isCacheSchemas" );
      allowNullForMissingFields = rep.getStepAttributeBoolean( id_step, "allowNullForMissingFields" );


      // using the "type" column to get the number of field rows because "type" is guaranteed not to be null.
      int nrfields = rep.countNrStepAttributes( id_step, "type" );
      this.inputFields = new AvroInputField[ nrfields ];
      for ( int i = 0; i < nrfields; i++ ) {
        AvroInputField inputField = new AvroInputField();
        inputField.setFormatFieldName( rep.getStepAttributeString( id_step, i, "path" ) );
        inputField.setPentahoFieldName( rep.getStepAttributeString( id_step, i, "name" ) );
        inputField.setPentahoType( rep.getStepAttributeString( id_step, i, "type" ) );
        String avroType = rep.getStepAttributeString( id_step, i, "avro_type" );
        if ( avroType != null && !avroType.equalsIgnoreCase( "null" ) ) {
          inputField.setAvroType( avroType );
        } else {
          inputField.setAvroType( AvroTypeConverter.convertToAvroType( inputField.getPentahoType() ) );
        }
        String stringFormat = rep.getStepAttributeString( id_step, i, "format" );
        inputField.setStringFormat( stringFormat == null ? "" : stringFormat );
        String indexedValues = rep.getStepAttributeString( id_step, i, "indexed_vals" );
        if ( indexedValues != null && indexedValues.length() > 0 ) {
          inputField.setIndexedValues( indexedValues );
        }
        this.inputFields[ i ] = inputField;
      }

      nrfields = rep.countNrStepAttributes( id_step, "fieldName" );
      this.lookupFields = new ArrayList<>();
      for ( int i = 0; i < nrfields; i++ ) {
        AvroLookupField lookupField = new AvroLookupField();
        lookupField.setFieldName( rep.getStepAttributeString( id_step, i, "fieldName" ) );
        lookupField.setVariableName( rep.getStepAttributeString( id_step, i, "variableName" ) );
        lookupField.setDefaultValue( rep.getStepAttributeString( id_step, i, "defaultValue" ) );
        this.lookupFields.add( lookupField );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {

    try {
      rep.saveStepAttribute( id_transformation, id_step, "passing_through_fields", passingThruFields );
      rep.saveStepAttribute( id_transformation, id_step, "dataLocation", getDataLocation() );
      rep.saveStepAttribute( id_transformation, id_step, "sourceFormat", getFormat() );
      rep.saveStepAttribute( id_transformation, id_step, "dataLocationType", dataLocationType );
      rep.saveStepAttribute( id_transformation, id_step, "schemaLocation", getSchemaLocation() );
      rep.saveStepAttribute( id_transformation, id_step, "schemaLocationType", schemaLocationType );
      rep.saveStepAttribute( id_transformation, id_step, "isCacheSchemas", isCacheSchemas() );
      rep.saveStepAttribute( id_transformation, id_step, "allowNullForMissingFields", isAllowNullForMissingFields() );

      for ( int i = 0; i < inputFields.length; i++ ) {
        AvroInputField field = inputFields[ i ];

        rep.saveStepAttribute( id_transformation, id_step, i, "path", field.getAvroFieldName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "name", field.getPentahoFieldName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "type", field.getTypeDesc() );
        AvroSpec.DataType avroDataType = field.getAvroType();
        if ( avroDataType != null && !avroDataType.equals( AvroSpec.DataType.NULL ) ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "avro_type", avroDataType.getName() );
        } else {
          rep.saveStepAttribute( id_transformation, id_step, i, "avro_type", AvroTypeConverter.convertToAvroType( field.getTypeDesc() ) );
        }
        if ( field.getStringFormat() != null ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "format", field.getStringFormat() );
        }
        String indexedValues = field.getIndexedValues();
        if ( indexedValues != null && indexedValues.length() > 0 ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "indexed_vals", indexedValues );
        }
      }

      for ( int i = 0; i < lookupFields.size(); i++ ) {
        AvroLookupField field = lookupFields.get( i );

        rep.saveStepAttribute( id_transformation, id_step, i, "fieldName", field.getFieldName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "variableName", field.getVariableName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "defaultValue", field.getDefaultValue() );

      }

      super.saveRep( rep, metaStore, id_transformation, id_step );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws
    KettleStepException {
    try {
      if ( !passingThruFields ) {
        // all incoming fields are not transmitted !
        rowMeta.clear();
      } else {
        if ( info != null ) {
          boolean found = false;
          for ( int i = 0; i < info.length && !found; i++ ) {
            if ( info[i] != null ) {
              rowMeta.mergeRowMeta( info[i], origin );
              found = true;
            }
          }
        }
      }
      for ( int i = 0; i < inputFields.length; i++ ) {
        AvroInputField field = inputFields[ i ];
        String value = space.environmentSubstitute( field.getPentahoFieldName() );
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( value, field.getPentahoType() );
        v.setOrigin( origin );
        rowMeta.addValueMeta( v );
      }
    } catch ( KettlePluginException e ) {
      throw new KettleStepException( "Unable to create value type", e );
    }
  }


  @Override
  public void resolve() {
    if ( dataLocation != null && !dataLocation.isEmpty() ) {
      try {
        String realFileName = getParentStepMeta().getParentTransMeta().environmentSubstitute( dataLocation );
        FileObject fileObject = KettleVFS.getFileObject( realFileName );
        if ( AliasedFileObject.isAliasedFile( fileObject ) ) {
          dataLocation = ( (AliasedFileObject) fileObject ).getAELSafeURIString();
        }
      } catch ( KettleFileException e ) {
        throw new RuntimeException( e );
      }
    }

    if ( schemaLocation != null && !schemaLocation.isEmpty() ) {
      try {
        String realSchemaFilename = getParentStepMeta().getParentTransMeta().environmentSubstitute( schemaLocation );
        FileObject fileObject = KettleVFS.getFileObject( realSchemaFilename );
        if ( AliasedFileObject.isAliasedFile( fileObject ) ) {
          schemaLocation = ( (AliasedFileObject) fileObject ).getAELSafeURIString();
        }
      } catch ( KettleFileException e ) {
        throw new RuntimeException( e );
      }
    }
  }

  @Injection( name = "AVRO_FILENAME"  )
  @Deprecated
  public void setDataFileName( String fileName ) {
    setDataLocation( fileName, LocationDescriptor.FILE_NAME );
    setFormat( SourceFormat.AVRO_USE_SCHEMA.ordinal() );
  }

  @Injection( name = "DATABASE_STREAM_NAME"  )
  @Deprecated
  public void setDataFieldName( String fieldName ) {
    setDataLocation( fieldName, LocationDescriptor.FIELD_NAME );
    setFormat( SourceFormat.AVRO_USE_SCHEMA.ordinal() );
  }

  @Injection( name = "SCHEMA_FILENAME"  )
  @Deprecated
  public void setSchemaFileName( String fileName ) {
    setSchemaLocation( fileName, LocationDescriptor.FILE_NAME );
  }
}
