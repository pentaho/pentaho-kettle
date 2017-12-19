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

package org.pentaho.di.trans.steps.singlethreader;

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
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute
 *
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class SingleThreaderMeta extends StepWithMappingMeta implements StepMetaInterface, HasRepositoryInterface {

  private static Class<?>  PKG = SingleThreaderMeta.class; // for i18n purposes, needed by Translator2!!

  private String batchSize;
  private String batchTime;

  private String injectStep;
  private String retrieveStep;

  private boolean passingAllParameters;

  private String[] parameters;
  private String[] parameterValues;

  private IMetaStore metaStore;

  public SingleThreaderMeta() {
    super(); // allocate BaseStepMeta

    setDefault();
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

      batchSize = XMLHandler.getTagValue( stepnode, "batch_size" );
      batchTime = XMLHandler.getTagValue( stepnode, "batch_time" );
      injectStep = XMLHandler.getTagValue( stepnode, "inject_step" );
      retrieveStep = XMLHandler.getTagValue( stepnode, "retrieve_step" );

      Node parametersNode = XMLHandler.getSubNode( stepnode, "parameters" );

      String passAll = XMLHandler.getTagValue( parametersNode, "pass_all_parameters" );
      passingAllParameters = Utils.isEmpty( passAll ) || "Y".equalsIgnoreCase( passAll );

      int nrParameters = XMLHandler.countNodes( parametersNode, "parameter" );

      allocate( nrParameters );

      for ( int i = 0; i < nrParameters; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( parametersNode, "parameter", i );

        parameters[i] = XMLHandler.getTagValue( knode, "name" );
        parameterValues[i] = XMLHandler.getTagValue( knode, "value" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SingleThreaderMeta.Exception.ErrorLoadingTransformationStepFromXML" ), e );
    }
  }

  public void allocate( int nrParameters ) {
    parameters = new String[nrParameters];
    parameterValues = new String[nrParameters];
  }

  public Object clone() {
    SingleThreaderMeta retval = (SingleThreaderMeta) super.clone();
    int nrParameters = parameters.length;
    retval.allocate( nrParameters );
    System.arraycopy( parameters, 0, retval.parameters, 0, nrParameters );
    System.arraycopy( parameterValues, 0, retval.parameterValues, 0, nrParameters );

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

    retval.append( "    " ).append( XMLHandler.addTagValue( "batch_size", batchSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "batch_time", batchTime ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "inject_step", injectStep ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "retrieve_step", retrieveStep ) );

    if ( parameters != null ) {
      retval.append( "      " ).append( XMLHandler.openTag( "parameters" ) );

      retval.append( "        " ).append( XMLHandler.addTagValue( "pass_all_parameters", passingAllParameters ) );

      for ( int i = 0; i < parameters.length; i++ ) {
        // This is a better way of making the XML file than the arguments.
        retval.append( "            " ).append( XMLHandler.openTag( "parameter" ) );

        retval.append( "            " ).append( XMLHandler.addTagValue( "name", parameters[i] ) );
        retval.append( "            " ).append( XMLHandler.addTagValue( "value", parameterValues[i] ) );

        retval.append( "            " ).append( XMLHandler.closeTag( "parameter" ) );
      }
      retval.append( "      " ).append( XMLHandler.closeTag( "parameters" ) );
    }
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

    batchSize = rep.getStepAttributeString( id_step, "batch_size" );
    batchTime = rep.getStepAttributeString( id_step, "batch_time" );
    injectStep = rep.getStepAttributeString( id_step, "inject_step" );
    retrieveStep = rep.getStepAttributeString( id_step, "retrieve_step" );

    // The parameters...
    //
    int parameternr = rep.countNrStepAttributes( id_step, "parameter_name" );
    parameters = new String[parameternr];
    parameterValues = new String[parameternr];

    // Read all parameters ...
    for ( int a = 0; a < parameternr; a++ ) {
      parameters[a] = rep.getStepAttributeString( id_step, a, "parameter_name" );
      parameterValues[a] = rep.getStepAttributeString( id_step, a, "parameter_value" );
    }

    passingAllParameters = rep.getStepAttributeBoolean( id_step, 0, "pass_all_parameters", true );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "specification_method", specificationMethod == null
      ? null : specificationMethod.getCode() );
    rep.saveStepAttribute( id_transformation, id_step, "trans_object_id", transObjectId == null
      ? null : transObjectId.toString() );
    rep.saveStepAttribute( id_transformation, id_step, "filename", fileName );
    rep.saveStepAttribute( id_transformation, id_step, "trans_name", transName );
    rep.saveStepAttribute( id_transformation, id_step, "directory_path", directoryPath );

    rep.saveStepAttribute( id_transformation, id_step, "batch_size", batchSize );
    rep.saveStepAttribute( id_transformation, id_step, "batch_time", batchTime );
    rep.saveStepAttribute( id_transformation, id_step, "inject_step", injectStep );
    rep.saveStepAttribute( id_transformation, id_step, "retrieve_step", retrieveStep );

    // The parameters...
    //
    // Save the parameters...
    if ( parameters != null ) {
      for ( int i = 0; i < parameters.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_name", parameters[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_value", Const
          .NVL( parameterValues[i], "" ) );
      }
    }

    rep.saveStepAttribute( id_transformation, id_step, "pass_all_parameters", passingAllParameters );
  }

  public void setDefault() {
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    batchSize = "100";
    batchTime = "";

    passingAllParameters = true;

    parameters = new String[0];
    parameterValues = new String[0];
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // First load some interesting data...
    //
    // Then see which fields get added to the row.
    //
    TransMeta mappingTransMeta = null;
    try {
      mappingTransMeta = loadSingleThreadedTransMeta( this, repository, space );
    } catch ( KettleException e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "SingleThreaderMeta.Exception.UnableToLoadMappingTransformation" ), e );
    }

    row.clear();

    // Let's keep it simple!
    //
    if ( !Utils.isEmpty( space.environmentSubstitute( retrieveStep ) ) ) {
      RowMetaInterface stepFields = mappingTransMeta.getStepFields( retrieveStep );
      row.addRowMeta( stepFields );
    }
  }


  public static final synchronized TransMeta loadSingleThreadedTransMeta( SingleThreaderMeta mappingMeta,
                                                                          Repository rep, VariableSpace space ) throws KettleException {
    return loadMappingMeta( mappingMeta, rep, null, space );
  }

  public static final synchronized TransMeta loadSingleThreadedTransMeta( SingleThreaderMeta mappingMeta,
                                                                          Repository rep, VariableSpace space, boolean passingAllParameters  ) throws KettleException {
    return loadMappingMeta( mappingMeta, rep, null, space, passingAllParameters );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "SingleThreaderMeta.CheckResult.NotReceivingAnyFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SingleThreaderMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SingleThreaderMeta.CheckResult.StepReceivingFieldsFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SingleThreaderMeta.CheckResult.NoInputReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new SingleThreader( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SingleThreaderData();
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

  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, };
  }

  /**
   * @return the batchSize
   */
  public String getBatchSize() {
    return batchSize;
  }

  /**
   * @param batchSize
   *          the batchSize to set
   */
  public void setBatchSize( String batchSize ) {
    this.batchSize = batchSize;
  }

  /**
   * @return the injectStep
   */
  public String getInjectStep() {
    return injectStep;
  }

  /**
   * @param injectStep
   *          the injectStep to set
   */
  public void setInjectStep( String injectStep ) {
    this.injectStep = injectStep;
  }

  /**
   * @return the retrieveStep
   */
  public String getRetrieveStep() {
    return retrieveStep;
  }

  /**
   * @param retrieveStep
   *          the retrieveStep to set
   */
  public void setRetrieveStep( String retrieveStep ) {
    this.retrieveStep = retrieveStep;
  }

  /**
   * @return the passingAllParameters
   */
  public boolean isPassingAllParameters() {
    return passingAllParameters;
  }

  /**
   * @param passingAllParameters
   *          the passingAllParameters to set
   */
  public void setPassingAllParameters( boolean passingAllParameters ) {
    this.passingAllParameters = passingAllParameters;
  }

  /**
   * @return the parameters
   */
  public String[] getParameters() {
    return parameters;
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters( String[] parameters ) {
    this.parameters = parameters;
  }

  /**
   * @return the parameterValues
   */
  public String[] getParameterValues() {
    return parameterValues;
  }

  /**
   * @param parameterValues
   *          the parameterValues to set
   */
  public void setParameterValues( String[] parameterValues ) {
    this.parameterValues = parameterValues;
  }

  /**
   * @return the batchTime
   */
  public String getBatchTime() {
    return batchTime;
  }

  /**
   * @param batchTime
   *          the batchTime to set
   */
  public void setBatchTime( String batchTime ) {
    this.batchTime = batchTime;
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
    return new String[] { BaseMessages.getString( PKG, "SingleThreaderMeta.ReferencedObject.Description" ), };
  }

  private boolean isTransformationDefined() {
    return !Utils.isEmpty( fileName )
      || transObjectId != null || ( !Utils.isEmpty( this.directoryPath ) && !Utils.isEmpty( transName ) );
  }

  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isTransformationDefined(), };
  }

  /**
   * Load the referenced object
   *
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
    return loadSingleThreadedTransMeta( this, rep, space );
  }

  public Object loadReferencedObject( int index, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException {
    return loadMappingMeta( this, rep, metaStore, space );
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }
}
