/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.avro.output;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.trans.steps.avro.AvroTypeConverter;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.AliasedFileObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.workarounds.ResolvableResource;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Avro output meta step without Hadoop-dependent classes. Required for read meta in the spark native code.
 *
 * @author Alexander Buloichik@epam.com>
 */
public abstract class AvroOutputMetaBase extends BaseStepMeta implements StepMetaInterface,  ResolvableResource {
  private static final String FILE_NAME = "filename";
  private static final String PRECISION = "precision";
  private static final String SCALE = "scale";
  private static final String NULLABLE = "nullable";
  private static final String DEFAULT = "default";
  private static final String SPACES_8 = "        ";

  private static final Class<?> PKG = AvroOutputMetaBase.class;

  @Injection( name = "FILENAME" ) private String filename;

  @InjectionDeep
  private List<AvroOutputField> outputFields = new ArrayList<>();

  @Injection( name = "OPTIONS_DATE_IN_FILE_NAME" )
  protected boolean dateInFileName = false;

  @Injection( name = "OPTIONS_TIME_IN_FILE_NAME" )
  protected boolean timeInFileName = false;

  @Injection( name = "OPTIONS_DATE_FORMAT" )
  protected String dateTimeFormat = "";
  @Injection( name = "OPTIONS_COMPRESSION" ) protected String compressionType;
  @Injection( name = "SCHEMA_FILENAME" ) protected String schemaFilename;
  @Injection( name = "SCHEMA_NAMESPACE" ) protected String namespace;
  @Injection( name = "SCHEMA_RECORD_NAME" ) protected String recordName;
  @Injection( name = "SCHEMA_DOC_VALUE" ) protected String docValue;
  @Injection( name = "OVERRIDE_OUTPUT" )
  protected boolean overrideOutput;

  @Override
  public void setDefault() {
  }

  public boolean isOverrideOutput() {
    return overrideOutput;
  }

  public void setOverrideOutput( boolean overrideOutput ) {
    this.overrideOutput = overrideOutput;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public List<AvroOutputField> getOutputFields() {
    return outputFields;
  }

  public void setOutputFields( List<AvroOutputField> outputFields ) {
    this.outputFields = outputFields;
  }

  public boolean isDateInFileName() {
    return dateInFileName;
  }

  public void setDateInFileName( boolean dateInFileName ) {
    this.dateInFileName = dateInFileName;
  }

  public boolean isTimeInFileName() {
    return timeInFileName;
  }

  public void setTimeInFileName( boolean timeInFileName ) {
    this.timeInFileName = timeInFileName;
  }

  public String getDateTimeFormat() {
    return dateTimeFormat;
  }

  public void setDateTimeFormat( String dateTimeFormat ) {
    this.dateTimeFormat = dateTimeFormat;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      filename = XMLHandler.getTagValue( stepnode, FILE_NAME );
      // Since we had override set to true in the previous release by default, we need to ensure that if the flag is
      // missing in the transformation xml, we set the override flag to true
      String override = XMLHandler.getTagValue( stepnode, FieldNames.OVERRIDE_OUTPUT );
      if ( override != null && override.length() > 0 ) {
        overrideOutput = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, FieldNames.OVERRIDE_OUTPUT ) );
      } else {
        overrideOutput = true;
      }
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );
      List<AvroOutputField> avroOutputFields = new ArrayList<>();
      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        AvroOutputField outputField = new AvroOutputField();
        outputField.setFormatFieldName( XMLHandler.getTagValue( fnode, "path" ) );
        outputField.setPentahoFieldName( XMLHandler.getTagValue( fnode, "name" ) );
        outputField.setFormatType( AvroTypeConverter.convertToAvroType(  XMLHandler.getTagValue( fnode, "type" ) ) );
        outputField.setPrecision( XMLHandler.getTagValue( fnode, PRECISION ) );
        outputField.setScale( XMLHandler.getTagValue( fnode, SCALE ) );
        outputField.setAllowNull( XMLHandler.getTagValue( fnode, NULLABLE ) );
        outputField.setDefaultValue( XMLHandler.getTagValue( fnode, DEFAULT )  );
        avroOutputFields.add( outputField );
      }
      this.outputFields = avroOutputFields;

      compressionType = XMLHandler.getTagValue( stepnode, FieldNames.COMPRESSION );
      dateTimeFormat = XMLHandler.getTagValue( stepnode, FieldNames.DATE_FORMAT );
      dateInFileName = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, FieldNames.DATE_IN_FILE_NAME ) );
      timeInFileName = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, FieldNames.TIME_IN_FILE_NAME ) );
      schemaFilename = XMLHandler.getTagValue( stepnode, FieldNames.SCHEMA_FILENAME );
      namespace = XMLHandler.getTagValue( stepnode, FieldNames.NAMESPACE );
      docValue = XMLHandler.getTagValue( stepnode, FieldNames.DOC_VALUE );
      recordName = XMLHandler.getTagValue( stepnode, FieldNames.RECORD_NAME );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 800 );
    final String INDENT = "    ";

    retval.append( INDENT ).append( XMLHandler.addTagValue( FILE_NAME, filename ) );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( filename );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.OVERRIDE_OUTPUT, overrideOutput ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < outputFields.size(); i++ ) {
      AvroOutputField field = outputFields.get( i );

      if ( field.getPentahoFieldName() != null && field.getPentahoFieldName().length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( "path", field.getFormatFieldName() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( "name", field.getPentahoFieldName() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( "type", field.getAvroType().getId() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( PRECISION, field.getPrecision() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( SCALE, field.getScale() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( NULLABLE, field.getAllowNull() ) );
        retval.append( SPACES_8 ).append( XMLHandler.addTagValue( DEFAULT, field.getDefaultValue() ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.COMPRESSION, compressionType ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.DATE_FORMAT, dateTimeFormat ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.DATE_IN_FILE_NAME, dateInFileName ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.TIME_IN_FILE_NAME, timeInFileName ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.SCHEMA_FILENAME, schemaFilename ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.NAMESPACE, namespace ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.DOC_VALUE, docValue ) );
    retval.append( INDENT ).append( XMLHandler.addTagValue( FieldNames.RECORD_NAME, recordName ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId stepId, List<DatabaseMeta> databases )
      throws KettleException {
    try {
      filename = rep.getStepAttributeString( stepId, FILE_NAME );
      // Since we had override set to true in the previous release by default, we need to ensure that if the flag is
      // missing in the transformation xml, we set the override flag to true
      String override = rep.getStepAttributeString( stepId, FieldNames.OVERRIDE_OUTPUT );
      if ( override != null && override.length() > 0 ) {
        overrideOutput = rep.getStepAttributeBoolean( stepId, FieldNames.OVERRIDE_OUTPUT );
      } else {
        overrideOutput = true;
      }
      // using the "type" column to get the number of field rows because "type" is guaranteed not to be null.
      int nrfields = rep.countNrStepAttributes( stepId, "type" );

      List<AvroOutputField> avroOutputFields = new ArrayList<>();
      for ( int i = 0; i < nrfields; i++ ) {
        AvroOutputField outputField = new AvroOutputField();

        outputField.setFormatFieldName( rep.getStepAttributeString( stepId, i, "path" ) );
        outputField.setPentahoFieldName( rep.getStepAttributeString( stepId, i, "name" ) );
        outputField.setFormatType( AvroTypeConverter.convertToAvroType( rep.getStepAttributeString( stepId, i, "type" ) ) );
        outputField.setPrecision( rep.getStepAttributeString( stepId, i, PRECISION ) );
        outputField.setScale( rep.getStepAttributeString( stepId, i, SCALE ) );
        outputField.setAllowNull( rep.getStepAttributeString( stepId, i, NULLABLE ) );
        outputField.setDefaultValue( rep.getStepAttributeString( stepId, i, DEFAULT ) );

        avroOutputFields.add( outputField );
      }
      this.outputFields = avroOutputFields;
      compressionType = rep.getStepAttributeString( stepId, FieldNames.COMPRESSION );
      dateTimeFormat = rep.getStepAttributeString( stepId, FieldNames.DATE_FORMAT );
      dateInFileName = rep.getStepAttributeBoolean( stepId, FieldNames.DATE_IN_FILE_NAME );
      timeInFileName = rep.getStepAttributeBoolean( stepId, FieldNames.TIME_IN_FILE_NAME );
      schemaFilename = rep.getStepAttributeString( stepId, FieldNames.SCHEMA_FILENAME );
      namespace = rep.getStepAttributeString( stepId, FieldNames.NAMESPACE );
      docValue = rep.getStepAttributeString( stepId, FieldNames.DOC_VALUE );
      recordName = rep.getStepAttributeString( stepId, FieldNames.RECORD_NAME );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId transformationId, ObjectId stepId )
      throws KettleException {
    try {
      rep.saveStepAttribute( transformationId, stepId, FILE_NAME, filename );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.OVERRIDE_OUTPUT, overrideOutput );

      for ( int i = 0; i < outputFields.size(); i++ ) {
        AvroOutputField field = outputFields.get( i );

        rep.saveStepAttribute( transformationId, stepId, i, "path", field.getFormatFieldName() );
        rep.saveStepAttribute( transformationId, stepId, i, "name", field.getPentahoFieldName() );
        rep.saveStepAttribute( transformationId, stepId, i, "type", field.getAvroType().getId() );
        rep.saveStepAttribute( transformationId, stepId, i, PRECISION, field.getPrecision() );
        rep.saveStepAttribute( transformationId, stepId, i, SCALE, field.getScale() );
        rep.saveStepAttribute( transformationId, stepId, i, NULLABLE, Boolean.toString( field.getAllowNull() ) );
        rep.saveStepAttribute( transformationId, stepId, i, DEFAULT, field.getDefaultValue() );
      }
      super.saveRep( rep, metaStore, transformationId, stepId );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.COMPRESSION, compressionType );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.DATE_FORMAT, dateTimeFormat );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.DATE_IN_FILE_NAME, dateInFileName );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.TIME_IN_FILE_NAME, timeInFileName );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.SCHEMA_FILENAME, schemaFilename );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.NAMESPACE, namespace );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.DOC_VALUE, docValue );
      rep.saveStepAttribute( transformationId, stepId, FieldNames.RECORD_NAME, recordName );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for stepId=" + stepId, e );
    }
  }

  @Override
  public void resolve() {
    if ( filename != null && !filename.isEmpty() ) {
      try {
        String realFileName = getParentStepMeta().getParentTransMeta().environmentSubstitute( filename );
        FileObject fileObject = KettleVFS.getFileObject( realFileName );
        if ( AliasedFileObject.isAliasedFile( fileObject ) ) {
          filename = ( (AliasedFileObject) fileObject ).getAELSafeURIString();
        }
      } catch ( KettleFileException e ) {
        throw new RuntimeException( e );
      }
    }

    if ( schemaFilename != null && !schemaFilename.isEmpty() ) {
      try {
        String realSchemaFilename = getParentStepMeta().getParentTransMeta().environmentSubstitute( schemaFilename );
        FileObject fileObject = KettleVFS.getFileObject( realSchemaFilename );
        if ( AliasedFileObject.isAliasedFile( fileObject ) ) {
          schemaFilename = ( (AliasedFileObject) fileObject ).getAELSafeURIString();
        }
      } catch ( KettleFileException e ) {
        throw new RuntimeException( e );
      }
    }
  }

  public String getSchemaFilename() {
    return schemaFilename;
  }

  public void setSchemaFilename( String schemaFilename ) {
    this.schemaFilename = schemaFilename;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }

  public String getRecordName() {
    return recordName;
  }

  public void setRecordName( String recordName ) {
    this.recordName = recordName;
  }

  public String getDocValue() {
    return docValue;
  }

  public void setDocValue( String docValue ) {
    this.docValue = docValue;
  }

  public String getCompressionType() {
    return StringUtil.isVariable( compressionType ) ? compressionType : getCompressionType( null ).toString();
  }

  public void setCompressionType( String value ) {
    compressionType = StringUtil.isVariable( value ) ? value : parseFromToString( value, CompressionType.values(), CompressionType.NONE ).name();
  }

  public CompressionType getCompressionType( VariableSpace vspace ) {
    return parseReplace( compressionType, vspace, this::findCompressionType, CompressionType.NONE );
  }

  private CompressionType findCompressionType( String str ) {
    try {
      return CompressionType.valueOf( str );
    } catch ( Exception ex ) {
      return parseFromToString( str, CompressionType.values(), CompressionType.NONE );
    }
  }

  public String[] getCompressionTypes() {
    return getStrings( CompressionType.values() );
  }

  public enum CompressionType {
    NONE( getMsg( "AvroOutput.CompressionType.NONE" ) ),
    DEFLATE( getMsg( "AvroOutput.CompressionType.DEFLATE" ) ),
    SNAPPY( getMsg( "AvroOutput.CompressionType.SNAPPY" ) );

    private final String name;

    private CompressionType( String name ) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  protected static <T> String[] getStrings( T[] objects ) {
    String[] names = new String[objects.length];
    int i = 0;
    for ( T obj : objects ) {
      names[i++] = obj.toString();
    }
    return names;
  }

  protected static <T> T parseFromToString( String str, T[] values, T defaultValue ) {
    if ( !Utils.isEmpty( str ) ) {
      for ( T type : values ) {
        if ( str.equalsIgnoreCase( type.toString() ) ) {
          return type;
        }
      }
    }
    return defaultValue;
  }

  private  <T> T parseReplace( String value, VariableSpace vspace, Function<String, T> parser, T defaultValue ) {
    String replaced = vspace != null ? vspace.environmentSubstitute( value ) : value;
    if ( !Utils.isEmpty( replaced ) ) {
      try {
        return parser.apply( replaced );
      } catch ( Exception e ) {
        // ignored
      }
    }
    return defaultValue;
  }

  public String constructOutputFilename( String file ) {
    if ( StringUtils.isEmpty( file ) )
        return file;

    return ( file.endsWith( ".avro" ) )
        ? constructSingleOutputFilename( file )
        : constructDirectoryOutputFilename( file );
  }

  private String appendTimestampsToFile( String name ) {
    if ( dateTimeFormat != null && !dateTimeFormat.isEmpty() ) {
      String dateTimeFormatPattern = getParentStepMeta().getParentTransMeta().environmentSubstitute( dateTimeFormat );
      name += new SimpleDateFormat( dateTimeFormatPattern ).format( new Date() );
    } else {
      if ( dateInFileName ) {
        name += '_' + new SimpleDateFormat( "yyyyMMdd" ).format( new Date() );
      }
      if ( timeInFileName ) {
        name += '_' + new SimpleDateFormat( "HHmmss" ).format( new Date() );
      }
    }

    return name;
  }

  private String constructDirectoryOutputFilename( String file ) {
    if ( file.endsWith( "/" ) ) {
      file = file.substring( 0, file.length() - 1 );
    }

    return appendTimestampsToFile( file ) + "/";
  }

  private String constructSingleOutputFilename( String file ) {
    int endIndex = file.lastIndexOf( '.' );
    String name = endIndex > 0 ? file.substring( 0, endIndex ) : file;
    String extension = endIndex <= 0 ? "" : file.substring( endIndex, file.length() );

    return appendTimestampsToFile( name ) + extension;
  }

  private static String getMsg( String key ) {
    return BaseMessages.getString( PKG, key );
  }

  protected static class FieldNames {
    private FieldNames() {
      throw new IllegalStateException( "Utility class" );
    }

    public static final String COMPRESSION = "compression";
    public static final String SCHEMA_FILENAME = "schemaFilename";
    public static final String OVERRIDE_OUTPUT = "overrideOutput";
    public static final String RECORD_NAME = "recordName";
    public static final String DOC_VALUE = "docValue";
    public static final String NAMESPACE = "namespace";
    public static final String DATE_IN_FILE_NAME = "dateInFileName";
    public static final String TIME_IN_FILE_NAME = "timeInFileName";
    public static final String DATE_FORMAT = "dateTimeFormat";
  }

}
