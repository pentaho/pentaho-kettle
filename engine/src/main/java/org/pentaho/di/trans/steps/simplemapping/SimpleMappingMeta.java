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

package org.pentaho.di.trans.steps.simplemapping;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportLocation;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingParameters;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInputMeta;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute
 *
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class SimpleMappingMeta extends StepWithMappingMeta implements StepMetaInterface, HasRepositoryInterface {

  private static Class<?> PKG = SimpleMappingMeta.class; // for i18n purposes, needed by Translator2!!

  private MappingIODefinition inputMapping;
  private MappingIODefinition outputMapping;
  private MappingParameters mappingParameters;

  /*
   * This repository object is injected from the outside at runtime or at design time. It comes from either Spoon or
   * Trans
   */
  private Repository repository;

  private IMetaStore metaStore;

  public SimpleMappingMeta() {
    super(); // allocate BaseStepMeta

    inputMapping = new MappingIODefinition();
    outputMapping = new MappingIODefinition();

    mappingParameters = new MappingParameters();
  }

  private void checkObjectLocationSpecificationMethod() {
    if ( specificationMethod == null ) {
      // Backward compatibility
      //
      // Default = Filename
      //
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;

      if ( !Utils.isEmpty( fileName ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
      } else if ( transObjectId != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
      } else if ( !Utils.isEmpty( transName ) ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    }
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      String method = XMLHandler.getTagValue( stepnode, "specification_method" );
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
      String transId = XMLHandler.getTagValue( stepnode, "trans_object_id" );
      transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );

      transName = XMLHandler.getTagValue( stepnode, "trans_name" );
      fileName = XMLHandler.getTagValue( stepnode, "filename" );
      directoryPath = XMLHandler.getTagValue( stepnode, "directory_path" );

      // Backward compatibility check for object specification
      //
      checkObjectLocationSpecificationMethod();

      Node mappingsNode = XMLHandler.getSubNode( stepnode, "mappings" );

      if ( mappingsNode == null ) {
        throw new KettleXMLException( "Unable to find <mappings> element in the step XML" );
      }

      // Read all the input mapping definitions...
      //
      Node inputNode = XMLHandler.getSubNode( mappingsNode, "input" );
      Node mappingNode = XMLHandler.getSubNode( inputNode, MappingIODefinition.XML_TAG );
      if ( mappingNode != null ) {
        inputMapping = new MappingIODefinition( mappingNode );
      } else {
        inputMapping = new MappingIODefinition(); // empty
      }
      Node outputNode = XMLHandler.getSubNode( mappingsNode, "output" );
      mappingNode = XMLHandler.getSubNode( outputNode, MappingIODefinition.XML_TAG );
      if ( mappingNode != null ) {
        outputMapping = new MappingIODefinition( mappingNode );
      } else {
        outputMapping = new MappingIODefinition(); // empty
      }

      // Load the mapping parameters too..
      //
      Node mappingParametersNode = XMLHandler.getSubNode( mappingsNode, MappingParameters.XML_TAG );
      mappingParameters = new MappingParameters( mappingParametersNode );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SimpleMappingMeta.Exception.ErrorLoadingTransformationStepFromXML" ), e );
    }
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append(
      XMLHandler.addTagValue( "specification_method", specificationMethod == null ? null : specificationMethod
        .getCode() ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "trans_object_id", transObjectId == null ? null : transObjectId.toString() ) );
    // Export a little bit of extra information regarding the reference since it doesn't really matter outside the same
    // repository.
    //
    if ( repository != null && transObjectId != null ) {
      try {
        RepositoryObject objectInformation =
          repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
        if ( objectInformation != null ) {
          transName = objectInformation.getName();
          directoryPath = objectInformation.getRepositoryDirectory().getPath();
        }
      } catch ( KettleException e ) {
        // Ignore object reference problems. It simply means that the reference is no longer valid.
      }
    }
    retval.append( "    " ).append( XMLHandler.addTagValue( "trans_name", transName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filename", fileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "directory_path", directoryPath ) );

    retval.append( "    " ).append( XMLHandler.openTag( "mappings" ) ).append( Const.CR );

    retval.append( "      " ).append( XMLHandler.openTag( "input" ) ).append( Const.CR );
    retval.append( inputMapping.getXML() );
    retval.append( "      " ).append( XMLHandler.closeTag( "input" ) ).append( Const.CR );

    retval.append( "      " ).append( XMLHandler.openTag( "output" ) ).append( Const.CR );
    retval.append( outputMapping.getXML() );
    retval.append( "      " ).append( XMLHandler.closeTag( "output" ) ).append( Const.CR );

    // Add the mapping parameters too
    //
    retval.append( "      " ).append( mappingParameters.getXML() ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.closeTag( "mappings" ) ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    String method = rep.getStepAttributeString( id_step, "specification_method" );
    specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method );
    String transId = rep.getStepAttributeString( id_step, "trans_object_id" );
    transObjectId = Utils.isEmpty( transId ) ? null : new StringObjectId( transId );
    transName = rep.getStepAttributeString( id_step, "trans_name" );
    fileName = rep.getStepAttributeString( id_step, "filename" );
    directoryPath = rep.getStepAttributeString( id_step, "directory_path" );

    // Backward compatibility check for object specification
    //
    checkObjectLocationSpecificationMethod();

    inputMapping = new MappingIODefinition( rep, id_step, "input_", 0 );
    outputMapping = new MappingIODefinition( rep, id_step, "output_", 0 );

    mappingParameters = new MappingParameters( rep, id_step );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "specification_method", specificationMethod == null
      ? null : specificationMethod.getCode() );
    rep.saveStepAttribute( id_transformation, id_step, "trans_object_id", transObjectId == null
      ? null : transObjectId.toString() );
    rep.saveStepAttribute( id_transformation, id_step, "filename", fileName );
    rep.saveStepAttribute( id_transformation, id_step, "trans_name", transName );
    rep.saveStepAttribute( id_transformation, id_step, "directory_path", directoryPath );

    inputMapping.saveRep( rep, metaStore, id_transformation, id_step, "input_", 0 );
    outputMapping.saveRep( rep, metaStore, id_transformation, id_step, "output_", 0 );

    // save the mapping parameters too
    //
    mappingParameters.saveRep( rep, metaStore, id_transformation, id_step );

  }

  public void setDefault() {
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;

    MappingIODefinition inputDefinition = new MappingIODefinition( null, null );
    inputDefinition.setMainDataPath( true );
    inputDefinition.setRenamingOnOutput( true );
    inputMapping = inputDefinition;

    MappingIODefinition outputDefinition = new MappingIODefinition( null, null );
    outputDefinition.setMainDataPath( true );
    outputMapping = outputDefinition;
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // First load some interesting data...

    // Then see which fields get added to the row.
    //
    TransMeta mappingTransMeta = null;
    try {
      mappingTransMeta =
        loadMappingMeta( this, repository, metaStore, space, mappingParameters.isInheritingAllVariables() );
    } catch ( KettleException e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "SimpleMappingMeta.Exception.UnableToLoadMappingTransformation" ), e );
    }

    // The field structure may depend on the input parameters as well (think of parameter replacements in MDX queries
    // for instance)
    if ( mappingParameters != null && mappingTransMeta != null ) {

      // Just set the variables in the transformation statically.
      // This just means: set a number of variables or parameter values:
      //
      StepWithMappingMeta.activateParams( mappingTransMeta, mappingTransMeta, space, mappingTransMeta.listParameters(),
        mappingParameters.getVariable(), mappingParameters.getInputField() );
    }

    // Keep track of all the fields that need renaming...
    //
    List<MappingValueRename> inputRenameList = new ArrayList<MappingValueRename>();

    //
    // Before we ask the mapping outputs anything, we should teach the mapping
    // input steps in the sub-transformation about the data coming in...
    //

    RowMetaInterface inputRowMeta;

    // The row metadata, what we pass to the mapping input step
    // definition.getOutputStep(), is "row"
    // However, we do need to re-map some fields...
    //
    inputRowMeta = row.clone();
    if ( !inputRowMeta.isEmpty() ) {
      for ( MappingValueRename valueRename : inputMapping.getValueRenames() ) {
        ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta( valueRename.getSourceValueName() );
        if ( valueMeta == null ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "SimpleMappingMeta.Exception.UnableToFindField", valueRename.getSourceValueName() ) );
        }
        valueMeta.setName( valueRename.getTargetValueName() );
      }
    }

    // What is this mapping input step?
    //
    StepMeta mappingInputStep = mappingTransMeta.findMappingInputStep( null );

    // We're certain it's a MappingInput step...
    //
    MappingInputMeta mappingInputMeta = (MappingInputMeta) mappingInputStep.getStepMetaInterface();

    // Inform the mapping input step about what it's going to receive...
    //
    mappingInputMeta.setInputRowMeta( inputRowMeta );

    // What values are we changing names for: already done!
    //
    mappingInputMeta.setValueRenames( null );

    // Keep a list of the input rename values that need to be changed back at
    // the output
    //
    if ( inputMapping.isRenamingOnOutput() ) {
      SimpleMapping.addInputRenames( inputRenameList, inputMapping.getValueRenames() );
    }

    StepMeta mappingOutputStep = mappingTransMeta.findMappingOutputStep( null );

    // We know it's a mapping output step...
    MappingOutputMeta mappingOutputMeta = (MappingOutputMeta) mappingOutputStep.getStepMetaInterface();

    // Change a few columns.
    mappingOutputMeta.setOutputValueRenames( outputMapping.getValueRenames() );

    // Perhaps we need to change a few input columns back to the original?
    //
    mappingOutputMeta.setInputValueRenames( inputRenameList );

    // Now we know wat's going to come out of there...
    // This is going to be the full row, including all the remapping, etc.
    //
    RowMetaInterface mappingOutputRowMeta = mappingTransMeta.getStepFields( mappingOutputStep );

    row.clear();
    row.addRowMeta( mappingOutputRowMeta );
  }

  public String[] getInfoSteps() {
    return null;
  }

  public String[] getTargetSteps() {
    return null;
  }



  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "SimpleMappingMeta.CheckResult.NotReceivingAnyFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SimpleMappingMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SimpleMappingMeta.CheckResult.StepReceivingFieldsFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SimpleMappingMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new SimpleMapping( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SimpleMappingData();
  }

  /**
   * @return the mappingParameters
   */
  public MappingParameters getMappingParameters() {
    return mappingParameters;
  }

  /**
   * @param mappingParameters
   *          the mappingParameters to set
   */
  public void setMappingParameters( MappingParameters mappingParameters ) {
    this.mappingParameters = mappingParameters;
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    String realFilename = transMeta.environmentSubstitute( fileName );
    String realTransname = transMeta.environmentSubstitute( transName );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    if ( !Utils.isEmpty( realFilename ) ) {
      // Add the filename to the references, including a reference to this step
      // meta data.
      //
      reference.getEntries().add( new ResourceEntry( realFilename, ResourceType.ACTIONFILE ) );
    } else if ( !Utils.isEmpty( realTransname ) ) {
      // Add the filename to the references, including a reference to this step
      // meta data.
      //
      reference.getEntries().add( new ResourceEntry( realTransname, ResourceType.ACTIONFILE ) );
      references.add( reference );
    }
    return references;
  }

  /**
   * @return the repository
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * @param repository
   *          the repository to set
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  @Override
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {
      ioMeta = new StepIOMeta( true, true, false, false, false, false );
    }
    return ioMeta;
  }

  /**
   * Remove the cached {@link StepIOMeta} so it is recreated when it is next accessed.
   */
  public void resetStepIoMeta() {
    ioMeta = null;
  }

  public boolean excludeFromRowLayoutVerification() {
    return false;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
  }

  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, };
  }

  @Override
  public boolean hasRepositoryReferences() {
    return specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
  }

  @Override
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    // The correct reference is stored in the trans name and directory attributes...
    //
    RepositoryDirectoryInterface repositoryDirectoryInterface =
      RepositoryImportLocation.getRepositoryImportLocation().findDirectory( directoryPath );
    transObjectId = repository.getTransformationID( transName, repositoryDirectoryInterface );
  }

  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ...
   */
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString( PKG, "SimpleMappingMeta.ReferencedObject.Description" ), };
  }

  private boolean isMapppingDefined() {
    return !Utils.isEmpty( fileName )
      || transObjectId != null || ( !Utils.isEmpty( this.directoryPath ) && !Utils.isEmpty( transName ) );
  }

  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isMapppingDefined(), };
  }

  @Deprecated
  public Object loadReferencedObject( int index, Repository rep, VariableSpace space ) throws KettleException {
    return loadReferencedObject( index, rep, null, space );
  }

  /**
   * Load the referenced object
   *
   * @param index
   *          the object index to load
   * @param rep
   *          the repository
   * @param metaStore
   *          the MetaStore to use
   * @param space
   *          the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return loadMappingMeta( this, rep, metaStore, space );
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public MappingIODefinition getInputMapping() {
    return inputMapping;
  }

  public void setInputMapping( MappingIODefinition inputMapping ) {
    this.inputMapping = inputMapping;
  }

  public MappingIODefinition getOutputMapping() {
    return outputMapping;
  }

  public void setOutputMapping( MappingIODefinition outputMapping ) {
    this.outputMapping = outputMapping;
  }

}
