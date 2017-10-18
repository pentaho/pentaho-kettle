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

package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.pentaho.di.core.parameters.UnknownParamException;
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
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
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

public class MappingMeta extends StepWithMappingMeta implements StepMetaInterface, HasRepositoryInterface {

  private static Class<?> PKG = MappingMeta.class;
  private List<MappingIODefinition> inputMappings;
  private List<MappingIODefinition> outputMappings;
  private MappingParameters mappingParameters;

  private boolean allowingMultipleInputs;
  private boolean allowingMultipleOutputs;

  /*
   * This repository object is injected from the outside at runtime or at design time. It comes from either Spoon or
   * Trans
   */
  private Repository repository;

  private IMetaStore metaStore;

  public MappingMeta() {
    super(); // allocate BaseStepMeta

    inputMappings = new ArrayList<MappingIODefinition>();
    outputMappings = new ArrayList<MappingIODefinition>();
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
      inputMappings.clear();
      outputMappings.clear();

      if ( mappingsNode != null ) {
        // Read all the input mapping definitions...
        //
        Node inputNode = XMLHandler.getSubNode( mappingsNode, "input" );
        int nrInputMappings = XMLHandler.countNodes( inputNode, MappingIODefinition.XML_TAG );
        for ( int i = 0; i < nrInputMappings; i++ ) {
          Node mappingNode = XMLHandler.getSubNodeByNr( inputNode, MappingIODefinition.XML_TAG, i );
          MappingIODefinition inputMappingDefinition = new MappingIODefinition( mappingNode );
          inputMappings.add( inputMappingDefinition );
        }
        Node outputNode = XMLHandler.getSubNode( mappingsNode, "output" );
        int nrOutputMappings = XMLHandler.countNodes( outputNode, MappingIODefinition.XML_TAG );
        for ( int i = 0; i < nrOutputMappings; i++ ) {
          Node mappingNode = XMLHandler.getSubNodeByNr( outputNode, MappingIODefinition.XML_TAG, i );
          MappingIODefinition outputMappingDefinition = new MappingIODefinition( mappingNode );
          outputMappings.add( outputMappingDefinition );
        }

        // Load the mapping parameters too..
        //
        Node mappingParametersNode = XMLHandler.getSubNode( mappingsNode, MappingParameters.XML_TAG );
        mappingParameters = new MappingParameters( mappingParametersNode );
      } else {
        // backward compatibility...
        //
        Node inputNode = XMLHandler.getSubNode( stepnode, "input" );
        Node outputNode = XMLHandler.getSubNode( stepnode, "output" );

        int nrInput = XMLHandler.countNodes( inputNode, "connector" );
        int nrOutput = XMLHandler.countNodes( outputNode, "connector" );

        // null means: auto-detect
        //
        MappingIODefinition inputMappingDefinition = new MappingIODefinition();
        inputMappingDefinition.setMainDataPath( true );

        for ( int i = 0; i < nrInput; i++ ) {
          Node inputConnector = XMLHandler.getSubNodeByNr( inputNode, "connector", i );
          String inputField = XMLHandler.getTagValue( inputConnector, "field" );
          String inputMapping = XMLHandler.getTagValue( inputConnector, "mapping" );
          inputMappingDefinition.getValueRenames().add( new MappingValueRename( inputField, inputMapping ) );
        }

        // null means: auto-detect
        //
        MappingIODefinition outputMappingDefinition = new MappingIODefinition();
        outputMappingDefinition.setMainDataPath( true );

        for ( int i = 0; i < nrOutput; i++ ) {
          Node outputConnector = XMLHandler.getSubNodeByNr( outputNode, "connector", i );
          String outputField = XMLHandler.getTagValue( outputConnector, "field" );
          String outputMapping = XMLHandler.getTagValue( outputConnector, "mapping" );
          outputMappingDefinition.getValueRenames().add( new MappingValueRename( outputMapping, outputField ) );
        }

        // Don't forget to add these to the input and output mapping
        // definitions...
        //
        inputMappings.add( inputMappingDefinition );
        outputMappings.add( outputMappingDefinition );

        // The default is to have no mapping parameters: the concept didn't
        // exist before.
        //
        mappingParameters = new MappingParameters();

      }

      String multiInput = XMLHandler.getTagValue( stepnode, "allow_multiple_input" );
      allowingMultipleInputs =
        Utils.isEmpty( multiInput ) ? inputMappings.size() > 1 : "Y".equalsIgnoreCase( multiInput );
      String multiOutput = XMLHandler.getTagValue( stepnode, "allow_multiple_output" );
      allowingMultipleOutputs =
        Utils.isEmpty( multiOutput ) ? outputMappings.size() > 1 : "Y".equalsIgnoreCase( multiOutput );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "MappingMeta.Exception.ErrorLoadingTransformationStepFromXML" ), e );
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
    for ( int i = 0; i < inputMappings.size(); i++ ) {
      retval.append( inputMappings.get( i ).getXML() );
    }
    retval.append( "      " ).append( XMLHandler.closeTag( "input" ) ).append( Const.CR );

    retval.append( "      " ).append( XMLHandler.openTag( "output" ) ).append( Const.CR );
    for ( int i = 0; i < outputMappings.size(); i++ ) {
      retval.append( outputMappings.get( i ).getXML() );
    }
    retval.append( "      " ).append( XMLHandler.closeTag( "output" ) ).append( Const.CR );

    // Add the mapping parameters too
    //
    retval.append( "      " ).append( mappingParameters.getXML() ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.closeTag( "mappings" ) ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "allow_multiple_input", allowingMultipleInputs ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "allow_multiple_output", allowingMultipleOutputs ) );

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
    inputMappings.clear();
    outputMappings.clear();

    int nrInput = rep.countNrStepAttributes( id_step, "input_field" );
    int nrOutput = rep.countNrStepAttributes( id_step, "output_field" );

    // Backward compatibility...
    //
    if ( nrInput > 0 || nrOutput > 0 ) {
      MappingIODefinition inputMappingDefinition = new MappingIODefinition();
      inputMappingDefinition.setMainDataPath( true );

      for ( int i = 0; i < nrInput; i++ ) {
        String inputField = rep.getStepAttributeString( id_step, i, "input_field" );
        String inputMapping = rep.getStepAttributeString( id_step, i, "input_mapping" );
        inputMappingDefinition.getValueRenames().add( new MappingValueRename( inputField, inputMapping ) );
      }

      MappingIODefinition outputMappingDefinition = new MappingIODefinition();
      outputMappingDefinition.setMainDataPath( true );

      for ( int i = 0; i < nrOutput; i++ ) {
        String outputField = rep.getStepAttributeString( id_step, i, "output_field" );
        String outputMapping = rep.getStepAttributeString( id_step, i, "output_mapping" );
        outputMappingDefinition.getValueRenames().add( new MappingValueRename( outputMapping, outputField ) );
      }

      // Don't forget to add these to the input and output mapping
      // definitions...
      //
      inputMappings.add( inputMappingDefinition );
      outputMappings.add( outputMappingDefinition );

      // The default is to have no mapping parameters: the concept didn't exist
      // before.
      mappingParameters = new MappingParameters();
    } else {
      nrInput = rep.countNrStepAttributes( id_step, "input_main_path" );
      nrOutput = rep.countNrStepAttributes( id_step, "output_main_path" );

      for ( int i = 0; i < nrInput; i++ ) {
        inputMappings.add( new MappingIODefinition( rep, id_step, "input_", i ) );
      }

      for ( int i = 0; i < nrOutput; i++ ) {
        outputMappings.add( new MappingIODefinition( rep, id_step, "output_", i ) );
      }

      mappingParameters = new MappingParameters( rep, id_step );
    }

    allowingMultipleInputs =
      rep.getStepAttributeBoolean( id_step, 0, "allow_multiple_input", inputMappings.size() > 1 );
    allowingMultipleOutputs =
      rep.getStepAttributeBoolean( id_step, 0, "allow_multiple_output", outputMappings.size() > 1 );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "specification_method", specificationMethod == null
      ? null : specificationMethod.getCode() );
    rep.saveStepAttribute( id_transformation, id_step, "trans_object_id", transObjectId == null
      ? null : transObjectId.toString() );
    rep.saveStepAttribute( id_transformation, id_step, "filename", fileName );
    rep.saveStepAttribute( id_transformation, id_step, "trans_name", transName );
    rep.saveStepAttribute( id_transformation, id_step, "directory_path", directoryPath );

    for ( int i = 0; i < inputMappings.size(); i++ ) {
      inputMappings.get( i ).saveRep( rep, metaStore, id_transformation, id_step, "input_", i );
    }

    for ( int i = 0; i < outputMappings.size(); i++ ) {
      outputMappings.get( i ).saveRep( rep, metaStore, id_transformation, id_step, "output_", i );
    }

    // save the mapping parameters too
    //
    mappingParameters.saveRep( rep, metaStore, id_transformation, id_step );

    rep.saveStepAttribute( id_transformation, id_step, 0, "allow_multiple_input", allowingMultipleInputs );
    rep.saveStepAttribute( id_transformation, id_step, 0, "allow_multiple_output", allowingMultipleOutputs );
  }

  public void setDefault() {
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;

    MappingIODefinition inputDefinition = new MappingIODefinition( null, null );
    inputDefinition.setMainDataPath( true );
    inputDefinition.setRenamingOnOutput( true );
    inputMappings.add( inputDefinition );
    MappingIODefinition outputDefinition = new MappingIODefinition( null, null );
    outputDefinition.setMainDataPath( true );
    outputMappings.add( outputDefinition );

    allowingMultipleInputs = false;
    allowingMultipleOutputs = false;
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // First load some interesting data...

    // Then see which fields get added to the row.
    //
    TransMeta mappingTransMeta = null;
    try {
      mappingTransMeta = loadMappingMeta( this, repository, metaStore, space );
    } catch ( KettleException e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "MappingMeta.Exception.UnableToLoadMappingTransformation" ), e );
    }

    // The field structure may depend on the input parameters as well (think of parameter replacements in MDX queries
    // for instance)
    if ( mappingParameters != null ) {

      // See if we need to pass all variables from the parent or not...
      //
      if ( mappingParameters.isInheritingAllVariables() ) {
        mappingTransMeta.copyVariablesFrom( space );
      }

      // Just set the variables in the transformation statically.
      // This just means: set a number of variables or parameter values:
      //
      List<String> subParams = Arrays.asList( mappingTransMeta.listParameters() );

      for ( int i = 0; i < mappingParameters.getVariable().length; i++ ) {
        String name = mappingParameters.getVariable()[i];
        String value = space.environmentSubstitute( mappingParameters.getInputField()[i] );
        if ( !Utils.isEmpty( name ) && !Utils.isEmpty( value ) ) {
          if ( subParams.contains( name ) ) {
            try {
              mappingTransMeta.setParameterValue( name, value );
            } catch ( UnknownParamException e ) {
              // this is explicitly checked for up front
            }
          }
          mappingTransMeta.setVariable( name, value );

        }
      }
    }

    // Keep track of all the fields that need renaming...
    //
    List<MappingValueRename> inputRenameList = new ArrayList<MappingValueRename>();

    /*
     * Before we ask the mapping outputs anything, we should teach the mapping input steps in the sub-transformation
     * about the data coming in...
     */
    for ( MappingIODefinition definition : inputMappings ) {

      RowMetaInterface inputRowMeta;

      if ( definition.isMainDataPath() || Utils.isEmpty( definition.getInputStepname() ) ) {
        // The row metadata, what we pass to the mapping input step
        // definition.getOutputStep(), is "row"
        // However, we do need to re-map some fields...
        //
        inputRowMeta = row.clone();
        if ( !inputRowMeta.isEmpty() ) {
          for ( MappingValueRename valueRename : definition.getValueRenames() ) {
            ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta( valueRename.getSourceValueName() );
            if ( valueMeta == null ) {
              throw new KettleStepException( BaseMessages.getString(
                PKG, "MappingMeta.Exception.UnableToFindField", valueRename.getSourceValueName() ) );
            }
            valueMeta.setName( valueRename.getTargetValueName() );
          }
        }
      } else {
        // The row metadata that goes to the info mapping input comes from the
        // specified step
        // In fact, it's one of the info steps that is going to contain this
        // information...
        //
        String[] infoSteps = getInfoSteps();
        int infoStepIndex = Const.indexOfString( definition.getInputStepname(), infoSteps );
        if ( infoStepIndex < 0 ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "MappingMeta.Exception.UnableToFindMetadataInfo", definition.getInputStepname() ) );
        }
        if ( info[infoStepIndex] != null ) {
          inputRowMeta = info[infoStepIndex].clone();
        } else {
          inputRowMeta = null;
        }
      }

      // What is this mapping input step?
      //
      StepMeta mappingInputStep = mappingTransMeta.findMappingInputStep( definition.getOutputStepname() );

      // We're certain it's a MappingInput step...
      //
      MappingInputMeta mappingInputMeta = (MappingInputMeta) mappingInputStep.getStepMetaInterface();

      // Inform the mapping input step about what it's going to receive...
      //
      mappingInputMeta.setInputRowMeta( inputRowMeta );

      // What values are we changing names for?
      //
      mappingInputMeta.setValueRenames( definition.getValueRenames() );

      // Keep a list of the input rename values that need to be changed back at
      // the output
      //
      if ( definition.isRenamingOnOutput() ) {
        Mapping.addInputRenames( inputRenameList, definition.getValueRenames() );
      }
    }

    // All the mapping steps now know what they will be receiving.
    // That also means that the sub-transformation / mapping has everything it
    // needs.
    // So that means that the MappingOutput steps know exactly what the output
    // is going to be.
    // That could basically be anything.
    // It also could have absolutely no resemblance to what came in on the
    // input.
    // The relative old approach is therefore no longer suited.
    //
    // OK, but what we *can* do is have the MappingOutput step rename the
    // appropriate fields.
    // The mapping step will tell this step how it's done.
    //
    // Let's look for the mapping output step that is relevant for this actual
    // call...
    //
    MappingIODefinition mappingOutputDefinition = null;
    if ( nextStep == null ) {
      // This is the main step we read from...
      // Look up the main step to write to.
      // This is the output mapping definition with "main path" enabled.
      //
      for ( MappingIODefinition definition : outputMappings ) {
        if ( definition.isMainDataPath() || Utils.isEmpty( definition.getOutputStepname() ) ) {
          // This is the definition to use...
          //
          mappingOutputDefinition = definition;
        }
      }
    } else {
      // Is there an output mapping definition for this step?
      // If so, we can look up the Mapping output step to see what has changed.
      //

      for ( MappingIODefinition definition : outputMappings ) {
        if ( nextStep.getName().equals( definition.getOutputStepname() )
          || definition.isMainDataPath() || Utils.isEmpty( definition.getOutputStepname() ) ) {
          mappingOutputDefinition = definition;
        }
      }
    }

    if ( mappingOutputDefinition == null ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "MappingMeta.Exception.UnableToFindMappingDefinition" ) );
    }

    // OK, now find the mapping output step in the mapping...
    // This method in TransMeta takes into account a number of things, such as
    // the step not specified, etc.
    // The method never returns null but throws an exception.
    //
    StepMeta mappingOutputStep =
      mappingTransMeta.findMappingOutputStep( mappingOutputDefinition.getInputStepname() );

    // We know it's a mapping output step...
    MappingOutputMeta mappingOutputMeta = (MappingOutputMeta) mappingOutputStep.getStepMetaInterface();

    // Change a few columns.
    mappingOutputMeta.setOutputValueRenames( mappingOutputDefinition.getValueRenames() );

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
    String[] infoSteps = getStepIOMeta().getInfoStepnames();
    // Return null instead of empty array to preserve existing behavior
    return infoSteps.length == 0 ? null : infoSteps;
  }

  public String[] getTargetSteps() {

    List<String> targetSteps = new ArrayList<String>();
    // The infosteps are those steps that are specified in the input mappings
    for ( MappingIODefinition definition : outputMappings ) {
      if ( !definition.isMainDataPath() && !Utils.isEmpty( definition.getOutputStepname() ) ) {
        targetSteps.add( definition.getOutputStepname() );
      }
    }
    if ( targetSteps.isEmpty() ) {
      return null;
    }

    return targetSteps.toArray( new String[targetSteps.size()] );
  }


  @Deprecated
  public static final synchronized TransMeta loadMappingMeta( MappingMeta mappingMeta, Repository rep,
                                                              VariableSpace space ) throws KettleException {
    return loadMappingMeta( mappingMeta, rep, null, space );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "MappingMeta.CheckResult.NotReceivingAnyFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MappingMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MappingMeta.CheckResult.StepReceivingFieldsFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MappingMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }

    /*
     * TODO re-enable validation code for mappings...
     *
     * // Change the names of the fields if this is required by the mapping. for (int i=0;i<inputField.length;i++) { if
     * (inputField[i]!=null && inputField[i].length()>0) { if (inputMapping[i]!=null && inputMapping[i].length()>0) { if
     * (!inputField[i].equals(inputMapping[i])) // rename these! { int idx = prev.indexOfValue(inputField[i]); if
     * (idx<0) { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.MappingTargetFieldNotPresent",inputField[i]), stepinfo); remarks.add(cr); } } } else {
     * cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.MappingTargetFieldNotSepecified" ,i+"",inputField[i]), stepinfo);
     * remarks.add(cr); } } else { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
     * BaseMessages.getString(PKG, "MappingMeta.CheckResult.InputFieldNotSpecified",i+""), stepinfo); remarks.add(cr); }
     * }
     *
     * // Then check the fields that get added to the row. //
     *
     * Repository repository = Repository.getCurrentRepository(); TransMeta mappingTransMeta = null; try {
     * mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository); } catch(KettleException e) {
     * cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.UnableToLoadMappingTransformation" )+":"+Const.getStackTracker(e), stepinfo);
     * remarks.add(cr); }
     *
     * if (mappingTransMeta!=null) { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
     * BaseMessages.getString(PKG, "MappingMeta.CheckResult.MappingTransformationSpecified"), stepinfo);
     * remarks.add(cr);
     *
     * StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
     *
     * if (stepMeta!=null) { // See which fields are coming out of the mapping output step of the sub-transformation //
     * For these fields we check the existance // RowMetaInterface fields = null; try { fields =
     * mappingTransMeta.getStepFields(stepMeta);
     *
     * boolean allOK = true;
     *
     * // Check the fields... for (int i=0;i<outputMapping.length;i++) { ValueMetaInterface v =
     * fields.searchValueMeta(outputMapping[i]); if (v==null) // Not found! { cr = new
     * CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.MappingOutFieldSpecifiedCouldNotFound" )+outputMapping[i], stepinfo); remarks.add(cr);
     * allOK=false; } }
     *
     * if (allOK) { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.AllOutputMappingFieldCouldBeFound"), stepinfo); remarks.add(cr); } }
     * catch(KettleStepException e) { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
     * BaseMessages.getString(PKG, "MappingMeta.CheckResult.UnableToGetStepOutputFields" )+stepMeta.getName()+"]",
     * stepinfo); remarks.add(cr); } } else { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
     * BaseMessages.getString(PKG, "MappingMeta.CheckResult.NoMappingOutputStepSpecified"), stepinfo); remarks.add(cr);
     * } } else { cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
     * "MappingMeta.CheckResult.NoMappingSpecified"), stepinfo); remarks.add(cr); }
     */
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Mapping( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new MappingData();
  }

  /**
   * @return the inputMappings
   */
  public List<MappingIODefinition> getInputMappings() {
    return inputMappings;
  }

  /**
   * @param inputMappings
   *          the inputMappings to set
   */
  public void setInputMappings( List<MappingIODefinition> inputMappings ) {
    this.inputMappings = inputMappings;
    resetStepIoMeta();
  }

  /**
   * @return the outputMappings
   */
  public List<MappingIODefinition> getOutputMappings() {
    return outputMappings;
  }

  /**
   * @param outputMappings
   *          the outputMappings to set
   */
  public void setOutputMappings( List<MappingIODefinition> outputMappings ) {
    this.outputMappings = outputMappings;
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

  /**
   * @return the specificationMethod
   */
  public ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  /**
   * @param specificationMethod
   *          the specificationMethod to set
   */
  public void setSpecificationMethod( ObjectLocationSpecificationMethod specificationMethod ) {
    this.specificationMethod = specificationMethod;
  }

  @Override
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {
      // TODO Create a dynamic StepIOMeta so that we can more easily manipulate the info streams?
      ioMeta = new StepIOMeta( true, true, true, false, true, false );
      for ( MappingIODefinition def : inputMappings ) {
        if ( isInfoMapping( def ) ) {
          Stream stream =
            new Stream( StreamType.INFO, def.getInputStep(), BaseMessages.getString(
              PKG, "MappingMeta.InfoStream.Description" ), StreamIcon.INFO, null );
          ioMeta.addStream( stream );
        }
      }
    }
    return ioMeta;
  }

  private boolean isInfoMapping( MappingIODefinition def ) {
    return !def.isMainDataPath() && !Utils.isEmpty( def.getInputStepname() );
  }

  /**
   * Remove the cached {@link StepIOMeta} so it is recreated when it is next accessed.
   */
  public void resetStepIoMeta() {
    ioMeta = null;
  }

  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    // Assign all StepMeta references for Input Mappings that are INFO inputs
    for ( MappingIODefinition def : inputMappings ) {
      if ( isInfoMapping( def ) ) {
        def.setInputStep( StepMeta.findStep( steps, def.getInputStepname() ) );
      }
    }
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
   * @return the allowingMultipleInputs
   */
  public boolean isAllowingMultipleInputs() {
    return allowingMultipleInputs;
  }

  /**
   * @param allowingMultipleInputs
   *          the allowingMultipleInputs to set
   */
  public void setAllowingMultipleInputs( boolean allowingMultipleInputs ) {
    this.allowingMultipleInputs = allowingMultipleInputs;
  }

  /**
   * @return the allowingMultipleOutputs
   */
  public boolean isAllowingMultipleOutputs() {
    return allowingMultipleOutputs;
  }

  /**
   * @param allowingMultipleOutputs
   *          the allowingMultipleOutputs to set
   */
  public void setAllowingMultipleOutputs( boolean allowingMultipleOutputs ) {
    this.allowingMultipleOutputs = allowingMultipleOutputs;
  }

  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ...
   */
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString( PKG, "MappingMeta.ReferencedObject.Description" ), };
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

}
