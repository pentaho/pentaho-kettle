/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.metainject;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.CurrentDirectoryResolver;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.HasRepositoryDirectories;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.ISubTransAwareMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaChangeListenerInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Step(
    id = "MetaInject",
    i18nPackageName = "org.pentaho.di.trans.steps.metainject",
    image = "org/pentaho/di/trans/steps/metainject/img/GenericTransform.svg",
    name = "i18n:org.pentaho.di.trans.step:BaseStep.TypeLongDesc.MetaInject",
    description = "i18n:org.pentaho.di.trans.step:BaseStep.TypeTooltipDesc.MetaInject",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Flow",
    documentationUrl = "Products/ETL_metadata_injection" )
@InjectionSupported( localizationPrefix = "MetaInject.Injection.", groups = { "SOURCE_OUTPUT_FIELDS",
  "MAPPING_FIELDS" } )
public class MetaInjectMeta extends BaseStepMeta implements StepMetaInterface, StepMetaChangeListenerInterface,
  HasRepositoryDirectories, ISubTransAwareMeta {

  private static Class<?> PKG = MetaInjectMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String MAPPINGS = "mappings";
  private static final String MAPPING = "mapping";

  private static final String SPECIFICATION_METHOD = "specification_method";
  private static final String TRANS_OBJECT_ID = "trans_object_id";
  private static final String TRANS_NAME = "trans_name";
  private static final String FILENAME = "filename";
  private static final String DIRECTORY_PATH = "directory_path";
  private static final String TARGET_FILE = "target_file";
  private static final String NO_EXECUTION = "no_execution";
  private static final String SOURCE_STEP = "source_step";

  private static final String STREAM_SOURCE_STEP = "stream_source_step";
  private static final String STREAM_TARGET_STEP = "stream_target_step";
  private static final String TARGET_STEP_NAME = "target_step_name";
  private static final String TARGET_ATTRIBUTE_KEY = "target_attribute_key";
  private static final String TARGET_DETAIL = "target_detail";
  private static final String SOURCE_FIELD = "source_field";
  private static final String SOURCE_OUTPUT_FIELDS = "source_output_fields";
  private static final String SOURCE_OUTPUT_FIELD = "source_output_field";
  private static final String SOURCE_OUTPUT_FIELD_NAME = "source_output_field_name";
  private static final String SOURCE_OUTPUT_FIELD_TYPE = "source_output_field_type";
  private static final String SOURCE_OUTPUT_FIELD_LENGTH = "source_output_field_length";
  private static final String SOURCE_OUTPUT_FIELD_PRECISION = "source_output_field_precision";

  private static final String MAPPING_SOURCE_FIELD = "mapping_source_field";
  private static final String MAPPING_SOURCE_STEP = "mapping_source_step";
  private static final String MAPPING_TARGET_DETAIL = "mapping_target_detail";
  private static final String MAPPING_TARGET_ATTRIBUTE_KEY = "mapping_target_attribute_key";
  private static final String MAPPING_TARGET_STEP_NAME = "mapping_target_step_name";

  private static final String GROUP_AND_NAME_DELIMITER = ".";

  // description of the transformation to execute...
  //
  @Injection( name = "TRANS_NAME" )
  private String transName;

  @Injection( name = "FILE_NAME" )
  private String fileName;

  @Injection( name = "DIRECTORY_PATH" )
  private String directoryPath;

  private ObjectId transObjectId;

  @Injection( name = "TRANS_SEPECIFICATION_METHOD" )
  private ObjectLocationSpecificationMethod specificationMethod;

  @Injection( name = "SOURCE_STEP_NAME" )
  private String sourceStepName;

  @InjectionDeep
  private List<MetaInjectOutputField> sourceOutputFields;

  private Map<TargetStepAttribute, SourceStepField> targetSourceMapping;

  @InjectionDeep
  private List<MetaInjectMapping> metaInjectMapping;

  @Injection( name = "TARGET_FILE" )
  private String targetFile;

  @Injection( name = "NO_EXECUTION" )
  private boolean noExecution;

  @Injection( name = "STREAMING_SOURCE_STEP" )
  private String streamSourceStepname;

  private StepMeta streamSourceStep;

  @Injection( name = "STREAMING_TARGET_STEP" )
  private String streamTargetStepname;

  public MetaInjectMeta() {
    super(); // allocate BaseStepMeta
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    targetSourceMapping = new HashMap<TargetStepAttribute, SourceStepField>();
    sourceOutputFields = new ArrayList<MetaInjectOutputField>();
  }

  // TODO: deep copy
  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public void setDefault() {
  }

  @Override
  public String getXML() {
    actualizeMetaInjectMapping();
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( SPECIFICATION_METHOD, specificationMethod == null ? null
      : specificationMethod.getCode() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( TRANS_OBJECT_ID, transObjectId == null ? null
      : transObjectId.toString() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( TRANS_NAME, transName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( FILENAME, fileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( DIRECTORY_PATH, directoryPath ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( SOURCE_STEP, sourceStepName ) );
    retval.append( "    " ).append( XMLHandler.openTag( SOURCE_OUTPUT_FIELDS ) );
    for ( MetaInjectOutputField field : sourceOutputFields ) {
      retval.append( "      " ).append( XMLHandler.openTag( SOURCE_OUTPUT_FIELD ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_OUTPUT_FIELD_NAME, field.getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_OUTPUT_FIELD_TYPE, field
        .getTypeDescription() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_OUTPUT_FIELD_LENGTH, field.getLength() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_OUTPUT_FIELD_PRECISION, field
        .getPrecision() ) );
      retval.append( "      " ).append( XMLHandler.closeTag( SOURCE_OUTPUT_FIELD ) );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( SOURCE_OUTPUT_FIELDS ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( TARGET_FILE, targetFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( NO_EXECUTION, noExecution ) );

    if ( ( streamSourceStepname == null ) && ( streamSourceStep != null ) ) {
      streamSourceStepname = streamSourceStep.getName();
    }
    retval.append( "    " ).append( XMLHandler.addTagValue( STREAM_SOURCE_STEP, streamSourceStepname ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( STREAM_TARGET_STEP, streamTargetStepname ) );

    retval.append( "    " ).append( XMLHandler.openTag( MAPPINGS ) );
    for ( TargetStepAttribute target : targetSourceMapping.keySet() ) {
      retval.append( "      " ).append( XMLHandler.openTag( MAPPING ) );
      SourceStepField source = targetSourceMapping.get( target );
      retval.append( "        " ).append( XMLHandler.addTagValue( TARGET_STEP_NAME, target.getStepname() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( TARGET_ATTRIBUTE_KEY, target.getAttributeKey() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( TARGET_DETAIL, target.isDetail() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_STEP, source.getStepname() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( SOURCE_FIELD, source.getField() ) );
      retval.append( "      " ).append( XMLHandler.closeTag( MAPPING ) );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( MAPPINGS ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {

      String method = XMLHandler.getTagValue( stepnode, SPECIFICATION_METHOD );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
      String transId = XMLHandler.getTagValue( stepnode, TRANS_OBJECT_ID );
      transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );

      transName = XMLHandler.getTagValue( stepnode, TRANS_NAME );
      fileName = XMLHandler.getTagValue( stepnode, FILENAME );
      directoryPath = XMLHandler.getTagValue( stepnode, DIRECTORY_PATH );

      sourceStepName = XMLHandler.getTagValue( stepnode, SOURCE_STEP );
      Node outputFieldsNode = XMLHandler.getSubNode( stepnode, SOURCE_OUTPUT_FIELDS );
      List<Node> outputFieldNodes = XMLHandler.getNodes( outputFieldsNode, SOURCE_OUTPUT_FIELD );
      sourceOutputFields = new ArrayList<MetaInjectOutputField>();
      for ( Node outputFieldNode : outputFieldNodes ) {
        String name = XMLHandler.getTagValue( outputFieldNode, SOURCE_OUTPUT_FIELD_NAME );
        String typeName = XMLHandler.getTagValue( outputFieldNode, SOURCE_OUTPUT_FIELD_TYPE );
        int length = Const.toInt( XMLHandler.getTagValue( outputFieldNode, SOURCE_OUTPUT_FIELD_LENGTH ), -1 );
        int precision = Const.toInt( XMLHandler.getTagValue( outputFieldNode, SOURCE_OUTPUT_FIELD_PRECISION ), -1 );
        int type = ValueMetaFactory.getIdForValueMeta( typeName );
        sourceOutputFields.add( new MetaInjectOutputField( name, type, length, precision ) );
      }

      targetFile = XMLHandler.getTagValue( stepnode, TARGET_FILE );
      noExecution = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, NO_EXECUTION ) );

      streamSourceStepname = XMLHandler.getTagValue( stepnode, STREAM_SOURCE_STEP );
      streamTargetStepname = XMLHandler.getTagValue( stepnode, STREAM_TARGET_STEP );

      Node mappingsNode = XMLHandler.getSubNode( stepnode, MAPPINGS );
      int nrMappings = XMLHandler.countNodes( mappingsNode, MAPPING );
      for ( int i = 0; i < nrMappings; i++ ) {
        Node mappingNode = XMLHandler.getSubNodeByNr( mappingsNode, MAPPING, i );
        String targetStepname = XMLHandler.getTagValue( mappingNode, TARGET_STEP_NAME );
        String targetAttributeKey = XMLHandler.getTagValue( mappingNode, TARGET_ATTRIBUTE_KEY );
        boolean targetDetail = "Y".equalsIgnoreCase( XMLHandler.getTagValue( mappingNode, TARGET_DETAIL ) );
        String sourceStepname = XMLHandler.getTagValue( mappingNode, SOURCE_STEP );
        String sourceField = XMLHandler.getTagValue( mappingNode, SOURCE_FIELD );

        TargetStepAttribute target = new TargetStepAttribute( targetStepname, targetAttributeKey, targetDetail );
        SourceStepField source = new SourceStepField( sourceStepname, sourceField );
        targetSourceMapping.put( target, source );
      }
      MetaInjectMigration.migrate( targetSourceMapping );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      String method = rep.getStepAttributeString( id_step, SPECIFICATION_METHOD );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
      String transId = rep.getStepAttributeString( id_step, TRANS_OBJECT_ID );
      transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );
      transName = rep.getStepAttributeString( id_step, TRANS_NAME );
      fileName = rep.getStepAttributeString( id_step, FILENAME );
      directoryPath = rep.getStepAttributeString( id_step, DIRECTORY_PATH );

      sourceStepName = rep.getStepAttributeString( id_step, SOURCE_STEP );
      streamSourceStepname = rep.getStepAttributeString( id_step, STREAM_SOURCE_STEP );
      streamTargetStepname = rep.getStepAttributeString( id_step, STREAM_TARGET_STEP );
      sourceOutputFields = new ArrayList<MetaInjectOutputField>();
      int nrSourceOutputFields = rep.countNrStepAttributes( id_step, SOURCE_OUTPUT_FIELD_NAME );
      for ( int i = 0; i < nrSourceOutputFields; i++ ) {
        String name = rep.getStepAttributeString( id_step, i, SOURCE_OUTPUT_FIELD_NAME );
        String typeName = rep.getStepAttributeString( id_step, i, SOURCE_OUTPUT_FIELD_TYPE );
        int length = (int) rep.getStepAttributeInteger( id_step, i, SOURCE_OUTPUT_FIELD_LENGTH );
        int precision = (int) rep.getStepAttributeInteger( id_step, i, SOURCE_OUTPUT_FIELD_PRECISION );
        int type = ValueMetaFactory.getIdForValueMeta( typeName );
        sourceOutputFields.add( new MetaInjectOutputField( name, type, length, precision ) );
      }
      targetFile = rep.getStepAttributeString( id_step, TARGET_FILE );
      noExecution = rep.getStepAttributeBoolean( id_step, NO_EXECUTION );

      int nrMappings = rep.countNrStepAttributes( id_step, MAPPING_TARGET_STEP_NAME );
      for ( int i = 0; i < nrMappings; i++ ) {
        String targetStepname = rep.getStepAttributeString( id_step, i, MAPPING_TARGET_STEP_NAME );
        String targetAttributeKey = rep.getStepAttributeString( id_step, i, MAPPING_TARGET_ATTRIBUTE_KEY );
        boolean targetDetail = rep.getStepAttributeBoolean( id_step, i, MAPPING_TARGET_DETAIL );
        String sourceStepname = rep.getStepAttributeString( id_step, i, MAPPING_SOURCE_STEP );
        String sourceField = rep.getStepAttributeString( id_step, i, MAPPING_SOURCE_FIELD );

        TargetStepAttribute target = new TargetStepAttribute( targetStepname, targetAttributeKey, targetDetail );
        SourceStepField source = new SourceStepField( sourceStepname, sourceField );
        targetSourceMapping.put( target, source );
      }

      MetaInjectMigration.migrate( targetSourceMapping );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, SPECIFICATION_METHOD, specificationMethod == null ? null
        : specificationMethod.getCode() );
      rep.saveStepAttribute( id_transformation, id_step, TRANS_OBJECT_ID, transObjectId == null ? null : transObjectId
        .toString() );
      rep.saveStepAttribute( id_transformation, id_step, FILENAME, fileName );
      rep.saveStepAttribute( id_transformation, id_step, TRANS_NAME, transName );
      rep.saveStepAttribute( id_transformation, id_step, DIRECTORY_PATH, directoryPath );

      rep.saveStepAttribute( id_transformation, id_step, SOURCE_STEP, sourceStepName );
      rep.saveStepAttribute( id_transformation, id_step, STREAM_SOURCE_STEP, streamSourceStepname );
      rep.saveStepAttribute( id_transformation, id_step, STREAM_TARGET_STEP, streamTargetStepname );
      MetaInjectOutputField aField = null;
      for ( int i = 0; i < sourceOutputFields.size(); i++ ) {
        aField = sourceOutputFields.get( i );
        rep.saveStepAttribute( id_transformation, id_step, i, SOURCE_OUTPUT_FIELD_NAME, aField.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, SOURCE_OUTPUT_FIELD_TYPE, aField.getTypeDescription() );
        rep.saveStepAttribute( id_transformation, id_step, i, SOURCE_OUTPUT_FIELD_LENGTH, aField.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, SOURCE_OUTPUT_FIELD_PRECISION, aField.getPrecision() );
      }

      rep.saveStepAttribute( id_transformation, id_step, TARGET_FILE, targetFile );
      rep.saveStepAttribute( id_transformation, id_step, NO_EXECUTION, noExecution );

      List<TargetStepAttribute> keySet = new ArrayList<TargetStepAttribute>( targetSourceMapping.keySet() );
      for ( int i = 0; i < keySet.size(); i++ ) {
        TargetStepAttribute target = keySet.get( i );
        SourceStepField source = targetSourceMapping.get( target );

        rep.saveStepAttribute( id_transformation, id_step, i, MAPPING_TARGET_STEP_NAME, target.getStepname() );
        rep.saveStepAttribute( id_transformation, id_step, i, MAPPING_TARGET_ATTRIBUTE_KEY, target.getAttributeKey() );
        rep.saveStepAttribute( id_transformation, id_step, i, MAPPING_TARGET_DETAIL, target.isDetail() );
        rep.saveStepAttribute( id_transformation, id_step, i, MAPPING_SOURCE_STEP, source.getStepname() );
        rep.saveStepAttribute( id_transformation, id_step, i, MAPPING_SOURCE_FIELD, source.getField() );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    rowMeta.clear(); // No defined output is expected from this step.
    if ( !Utils.isEmpty( sourceStepName ) ) {
      for ( MetaInjectOutputField field : sourceOutputFields ) {
        try {
          rowMeta.addValueMeta( field.createValueMeta() );
        } catch ( KettlePluginException e ) {
          throw new KettleStepException( "Error creating value meta for output field '" + field.getName() + "'", e );
        }
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new MetaInject( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MetaInjectData();
  }

  public Map<TargetStepAttribute, SourceStepField> getTargetSourceMapping() {
    return targetSourceMapping;
  }

  public void setTargetSourceMapping( Map<TargetStepAttribute, SourceStepField> targetSourceMapping ) {
    this.targetSourceMapping = targetSourceMapping;
  }

  /**
   * @return the transName
   */
  public String getTransName() {
    return transName;
  }

  /**
   * @param transName the transName to set
   */
  public void setTransName( String transName ) {
    this.transName = transName;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return the directoryPath
   */
  public String getDirectoryPath() {
    return directoryPath;
  }

  /**
   * @param directoryPath the directoryPath to set
   */
  public void setDirectoryPath( String directoryPath ) {
    this.directoryPath = directoryPath;
  }

  @Override
  public String[] getDirectories() {
    return new String[]{ directoryPath };
  }

  @Override
  public void setDirectories( String[] directories ) {
    this.directoryPath = directories[0];
  }

  /**
   * @return the transObjectId
   */
  public ObjectId getTransObjectId() {
    return transObjectId;
  }

  /**
   * @param transObjectId the transObjectId to set
   */
  public void setTransObjectId( ObjectId transObjectId ) {
    this.transObjectId = transObjectId;
  }

  @Injection( name = "TRANS_OBJECT_ID" )
  public void setTransStringObjectId( String transStringObjectId ) {
    this.transObjectId = new StringObjectId( transStringObjectId );
  }

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  @Override
  public ObjectLocationSpecificationMethod[] getSpecificationMethods() {
    return new ObjectLocationSpecificationMethod[] { specificationMethod };
  }

  /**
   * @param specificationMethod the specificationMethod to set
   */
  public void setSpecificationMethod( ObjectLocationSpecificationMethod specificationMethod ) {
    this.specificationMethod = specificationMethod;
  }

  @Override
  public TransMeta fetchTransMeta( StepMetaInterface stepMeta, Repository rep, IMetaStore metastore, VariableSpace space ) throws KettleException {
    return ( stepMeta != null && stepMeta instanceof MetaInjectMeta )
        ? loadTransformationMeta( (MetaInjectMeta) stepMeta, rep, metastore, space ) : null;

  }

  @Deprecated
  public static final synchronized TransMeta loadTransformationMeta( MetaInjectMeta mappingMeta, Repository rep,
                                                                     VariableSpace space ) throws KettleException {
    return loadTransformationMeta( mappingMeta, rep, null, space );
  }

  public static final synchronized TransMeta loadTransformationMeta( MetaInjectMeta injectMeta, Repository rep,
                                                                     IMetaStore metaStore, VariableSpace space )
    throws KettleException {
    TransMeta mappingTransMeta = null;

    CurrentDirectoryResolver resolver = new CurrentDirectoryResolver();
    VariableSpace tmpSpace =
      resolver.resolveCurrentDirectory( injectMeta.getSpecificationMethod(), space, rep, injectMeta
        .getParentStepMeta(), injectMeta.getFileName() );

    switch ( injectMeta.getSpecificationMethod() ) {
      case FILENAME:
        String realFilename = tmpSpace.environmentSubstitute( injectMeta.getFileName() );
        try {
          // OK, load the meta-data from file...
          //
          // Don't set internal variables: they belong to the parent thread!
          //
          if ( rep != null ) {
            // need to try to load from the repository
            realFilename = resolver.normalizeSlashes( realFilename );
            try {
              String dirStr = realFilename.substring( 0, realFilename.lastIndexOf( "/" ) );
              String tmpFilename = realFilename.substring( realFilename.lastIndexOf( "/" ) + 1 );
              RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
              mappingTransMeta = rep.loadTransformation( tmpFilename, dir, null, true, null );
            } catch ( KettleException ke ) {
              // try without extension
              if ( realFilename.endsWith( Const.STRING_TRANS_DEFAULT_EXT ) ) {
                try {
                  String tmpFilename =
                    realFilename.substring( realFilename.lastIndexOf( "/" ) + 1, realFilename.indexOf( "."
                      + Const.STRING_TRANS_DEFAULT_EXT ) );
                  String dirStr = realFilename.substring( 0, realFilename.lastIndexOf( "/" ) );
                  RepositoryDirectoryInterface dir = rep.findDirectory( dirStr );
                  mappingTransMeta = rep.loadTransformation( tmpFilename, dir, null, true, null );
                } catch ( KettleException ke2 ) {
                  // fall back to try loading from file system (transMeta is going to be null)
                }
              }
            }
          }
          if ( mappingTransMeta == null ) {
            mappingTransMeta = new TransMeta( realFilename, metaStore, rep, false, tmpSpace, null );
            mappingTransMeta.getLogChannel().logDetailed( "Loading Mapping from repository",
              "Mapping transformation was loaded from XML file [" + realFilename + "]" );
          }
        } catch ( Exception e ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "MetaInjectMeta.Exception.UnableToLoadTransformationFromFile", realFilename ), e );
        }
        break;

      case REPOSITORY_BY_NAME:
        String realTransname = tmpSpace.environmentSubstitute( injectMeta.getTransName() );
        String realDirectory = tmpSpace.environmentSubstitute( injectMeta.getDirectoryPath() );
        if ( rep != null ) {
          if ( !Utils.isEmpty( realTransname ) && !Utils.isEmpty( realDirectory ) && rep != null ) {
            RepositoryDirectoryInterface repdir = rep.findDirectory( realDirectory );
            if ( repdir != null ) {
              try {
                // reads the last revision in the repository...
                //
                // TODO: FIXME: see if we need to pass external MetaStore references to the repository?
                //
                mappingTransMeta = rep.loadTransformation( realTransname, repdir, null, true, null );

                mappingTransMeta.getLogChannel().logDetailed( "Loading Mapping from repository",
                  "Mapping transformation [" + realTransname + "] was loaded from the repository" );
              } catch ( Exception e ) {
                throw new KettleException( "Unable to load transformation [" + realTransname + "]", e );
              }
            } else {
              throw new KettleException( BaseMessages.getString( PKG,
                "MetaInjectMeta.Exception.UnableToLoadTransformationFromRepository", realTransname, realDirectory ) );
            }
          }
        } else {
          try {
            mappingTransMeta =
              new TransMeta( realDirectory + "/" + realTransname, metaStore, rep, true, tmpSpace, null );
          } catch ( KettleException ke ) {
            try {
              // add .ktr extension and try again
              mappingTransMeta =
                new TransMeta( realDirectory + "/" + realTransname + "." + Const.STRING_TRANS_DEFAULT_EXT, metaStore,
                  rep, true, tmpSpace, null );
            } catch ( KettleException ke2 ) {
              throw new KettleException( BaseMessages.getString( PKG, "StepWithMappingMeta.Exception.UnableToLoadTrans",
                realTransname ) + realDirectory );
            }
          }
        }
        break;
      case REPOSITORY_BY_REFERENCE:
        // Read the last revision by reference...
        mappingTransMeta = rep.loadTransformation( injectMeta.getTransObjectId(), null );
        break;
      default:
        break;
    }

    // Pass some important information to the mapping transformation metadata:
    //
    mappingTransMeta.copyVariablesFrom( space );
    mappingTransMeta.setRepository( rep );
    mappingTransMeta.setFilename( mappingTransMeta.getFilename() );

    return mappingTransMeta;
  }

  /**
   * package-local visibility for testing purposes
   */
  TransMeta loadTransformationMeta( Repository rep, VariableSpace space ) throws KettleException {
    return MetaInjectMeta.loadTransformationMeta( this, rep, null, space );
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    String realFilename = transMeta.environmentSubstitute( fileName );
    String realTransname = transMeta.environmentSubstitute( transName );
    String realDirectoryPath = transMeta.environmentSubstitute( directoryPath );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    if ( !Utils.isEmpty( realFilename ) ) {
      // Add the filename to the references, including a reference to this step
      // meta data.
      //
      reference.getEntries().add( new ResourceEntry( realFilename, ResourceType.ACTIONFILE ) );
    } else if ( !Utils.isEmpty( realTransname ) ) {
      // Add the trans name (including full repository path) to dependencies
      String realTransformation = realDirectoryPath + "/" + realTransname;
      reference.getEntries().add( new ResourceEntry( realTransformation, ResourceType.ACTIONFILE ) );
    }
    return references;
  }

  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
                                 ResourceNamingInterface resourceNamingInterface, Repository repository,
                                 IMetaStore metaStore )
    throws KettleException {
    try {
      // Try to load the transformation from repository or file.
      // Modify this recursively too...
      //
      // NOTE: there is no need to clone this step because the caller is
      // responsible for this.
      //
      // First load the executor transformation metadata...
      //
      TransMeta executorTransMeta = loadTransformationMeta( repository, space );

      // Also go down into the mapping transformation and export the files
      // there. (mapping recursively down)
      //
      String proposedNewFilename =
        executorTransMeta.exportResources( executorTransMeta, definitions, resourceNamingInterface, repository,
          metaStore );

      // To get a relative path to it, we inject
      // ${Internal.Entry.Current.Directory}
      //
      String newFilename =
        "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + proposedNewFilename;

      // Set the correct filename inside the XML.
      //
      executorTransMeta.setFilename( newFilename );

      // exports always reside in the root directory, in case we want to turn
      // this into a file repository...
      //
      executorTransMeta.setRepositoryDirectory( new RepositoryDirectory() );

      // change it in the entry
      //
      fileName = newFilename;

      setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

      return proposedNewFilename;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MetaInjectMeta.Exception.UnableToLoadTrans",
        fileName ) );
    }
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  @Override
  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  /**
   * @return the sourceStepName
   */
  public String getSourceStepName() {
    return sourceStepName;
  }

  /**
   * @param sourceStepName the sourceStepName to set
   */
  public void setSourceStepName( String sourceStepName ) {
    this.sourceStepName = sourceStepName;
  }

  /**
   * @return the targetFile
   */
  public String getTargetFile() {
    return targetFile;
  }

  /**
   * @param targetFile the targetFile to set
   */
  public void setTargetFile( String targetFile ) {
    this.targetFile = targetFile;
  }

  /**
   * @return the noExecution
   */
  public boolean isNoExecution() {
    return noExecution;
  }

  /**
   * @param noExecution the noExecution to set
   */
  public void setNoExecution( boolean noExecution ) {
    this.noExecution = noExecution;
  }

  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ...
   */
  @Override
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString( PKG, "MetaInjectMeta.ReferencedObject.Description" ), };
  }

  private boolean isTransformationDefined() {
    return !Utils.isEmpty( fileName ) || transObjectId != null || ( !Utils.isEmpty( this.directoryPath ) && !Const
      .isEmpty( transName ) );
  }

  @Override
  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isTransformationDefined(), };
  }

  @Override
  public String getActiveReferencedObjectDescription() {
    return BaseMessages.getString( PKG, "MetaInjectMeta.ReferencedObjectAfterInjection.Description" );
  }

  @Override
  @Deprecated
  public Object loadReferencedObject( int index, Repository rep, VariableSpace space ) throws KettleException {
    return loadReferencedObject( index, rep, null, space );
  }

  /**
   * Load the referenced object
   *
   * @param index     the object index to load
   * @param rep       the repository
   * @param metaStore metaStore
   * @param space     the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  @Override
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space )
    throws KettleException {
    return loadTransformationMeta( this, rep, metaStore, space );
  }

  public String getStreamSourceStepname() {
    return streamSourceStepname;
  }

  public void setStreamSourceStepname( String streamSourceStepname ) {
    this.streamSourceStepname = streamSourceStepname;
  }

  public StepMeta getStreamSourceStep() {
    return streamSourceStep;
  }

  public void setStreamSourceStep( StepMeta streamSourceStep ) {
    this.streamSourceStep = streamSourceStep;
  }

  public String getStreamTargetStepname() {
    return streamTargetStepname;
  }

  public void setStreamTargetStepname( String streamTargetStepname ) {
    this.streamTargetStepname = streamTargetStepname;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    streamSourceStep = StepMeta.findStep( steps, streamSourceStepname );
  }

  public List<MetaInjectOutputField> getSourceOutputFields() {
    return sourceOutputFields;
  }

  public void setSourceOutputFields( List<MetaInjectOutputField> sourceOutputFields ) {
    this.sourceOutputFields = sourceOutputFields;
  }

  public List<MetaInjectMapping> getMetaInjectMapping() {
    return metaInjectMapping;
  }

  public void setMetaInjectMapping( List<MetaInjectMapping> metaInjectMapping ) {
    this.metaInjectMapping = metaInjectMapping;
  }

  public void actualizeMetaInjectMapping() {
    if ( metaInjectMapping == null || metaInjectMapping.isEmpty() ) {
      return;
    }
    Map<TargetStepAttribute, SourceStepField> targetToSourceMap = convertToMap( metaInjectMapping );
    setTargetSourceMapping( targetToSourceMap );
  }

  /**
   * package-local visibility for testing purposes
   */
  static Map<TargetStepAttribute, SourceStepField> convertToMap( List<MetaInjectMapping> metaInjectMapping ) {
    Map<TargetStepAttribute, SourceStepField> targetToSourceMap = new HashMap<TargetStepAttribute, SourceStepField>();
    for ( MetaInjectMapping mappingEntry : metaInjectMapping ) {
      if ( !isMappingEntryFilled( mappingEntry ) ) {
        continue;
      }
      TargetStepAttribute targetStepAttribute = createTargetStepAttribute( mappingEntry );
      SourceStepField sourceStepField = createSourceStepField( mappingEntry );
      targetToSourceMap.put( targetStepAttribute, sourceStepField );
    }
    return targetToSourceMap;
  }

  private static TargetStepAttribute createTargetStepAttribute( MetaInjectMapping mappingEntry ) {
    String targetFieldName = mappingEntry.getTargetField();
    if ( targetFieldName.contains( GROUP_AND_NAME_DELIMITER ) ) {
      String[] targetFieldGroupAndName = targetFieldName.split( "\\" + GROUP_AND_NAME_DELIMITER );
      return new TargetStepAttribute( mappingEntry.getTargetStep(), targetFieldGroupAndName[ 1 ], true );
    }
    return new TargetStepAttribute( mappingEntry.getTargetStep(), mappingEntry.getTargetField(), false );
  }

  private static boolean isMappingEntryFilled( MetaInjectMapping mappingEntry ) {
    if ( mappingEntry.getSourceStep() == null || mappingEntry.getSourceField() == null || mappingEntry
      .getTargetStep() == null || mappingEntry.getTargetField() == null ) {
      return false;
    }
    return true;
  }

  private static SourceStepField createSourceStepField( MetaInjectMapping mappingEntry ) {
    return new SourceStepField( mappingEntry.getSourceStep(), mappingEntry.getSourceField() );
  }

  @Override
  public void onStepChange( TransMeta transMeta, StepMeta oldMeta, StepMeta newMeta ) {
    for ( int i = 0; i < transMeta.nrTransHops(); i++ ) {
      TransHopMeta hopMeta = transMeta.getTransHop( i );
      if ( hopMeta.getFromStep().equals( oldMeta ) ) {
        StepMeta toStepMeta = hopMeta.getToStep();
        if ( ( toStepMeta.getStepMetaInterface() instanceof MetaInjectMeta ) && ( toStepMeta.equals( this
          .getParentStepMeta() ) ) ) {
          MetaInjectMeta toMeta = (MetaInjectMeta) toStepMeta.getStepMetaInterface();
          Map<TargetStepAttribute, SourceStepField> sourceMapping = toMeta.getTargetSourceMapping();
          for ( Entry<TargetStepAttribute, SourceStepField> entry : sourceMapping.entrySet() ) {
            SourceStepField value = entry.getValue();
            if ( value.getStepname() != null && value.getStepname().equals( oldMeta.getName() ) ) {
              value.setStepname( newMeta.getName() );
            }
          }
        }
      }
    }
  }
}
