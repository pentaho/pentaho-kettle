/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
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
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportLocation;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to
 * execute
 * 
 * @since 22-nov-2005
 * @author Matt
 * 
 */

public class SingleThreaderMeta extends BaseStepMeta implements StepMetaInterface, HasRepositoryInterface {
  private static Class<?>                   PKG = SingleThreaderMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  
  private String                            transName;
  private String                            fileName;
  private String                            directoryPath;
  private ObjectId                          transObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private String                            batchSize;
  private String                            batchTime;
  
  private String                            injectStep;
  private String                            retrieveStep;

  private boolean passingAllParameters;
  
  private String                             parameters[];
  private String                             parameterValues[];

  public SingleThreaderMeta() {
    super(); // allocate BaseStepMeta

    setDefault();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    try {
      String method = XMLHandler.getTagValue(stepnode, "specification_method");
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
      String transId = XMLHandler.getTagValue(stepnode, "trans_object_id");
      transObjectId = Const.isEmpty(transId) ? null : new StringObjectId(transId);

      transName = XMLHandler.getTagValue(stepnode, "trans_name"); //$NON-NLS-1$
      fileName = XMLHandler.getTagValue(stepnode, "filename"); //$NON-NLS-1$
      directoryPath = XMLHandler.getTagValue(stepnode, "directory_path"); //$NON-NLS-1$

      batchSize = XMLHandler.getTagValue(stepnode, "batch_size"); //$NON-NLS-1$
      batchTime = XMLHandler.getTagValue(stepnode, "batch_time"); //$NON-NLS-1$
      injectStep = XMLHandler.getTagValue(stepnode, "inject_step"); //$NON-NLS-1$
      retrieveStep = XMLHandler.getTagValue(stepnode, "retrieve_step"); //$NON-NLS-1$
      
      Node parametersNode = XMLHandler.getSubNode(stepnode, "parameters"); //$NON-NLS-1$

      String passAll = XMLHandler.getTagValue(parametersNode, "pass_all_parameters");
      passingAllParameters = Const.isEmpty(passAll) || "Y".equalsIgnoreCase(passAll);
      
      int nrParameters = XMLHandler.countNodes(parametersNode, "parameter"); //$NON-NLS-1$

      parameters = new String[nrParameters];
      parameterValues = new String[nrParameters];

      for (int i = 0; i < nrParameters; i++) {
        Node knode = XMLHandler.getSubNodeByNr(parametersNode, "parameter", i); //$NON-NLS-1$

        parameters[i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
        parameterValues[i] = XMLHandler.getTagValue(knode, "value"); //$NON-NLS-1$
      }
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "SingleThreaderMeta.Exception.ErrorLoadingTransformationStepFromXML"), e); //$NON-NLS-1$
    }
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append("    ").append(XMLHandler.addTagValue("specification_method", specificationMethod == null ? null : specificationMethod.getCode()));
    retval.append("    ").append(XMLHandler.addTagValue("trans_object_id", transObjectId == null ? null : transObjectId.toString()));
    // Export a little bit of extra information regarding the reference since it doesn't really matter outside the same repository.
    //
    if (repository!=null && transObjectId!=null) {
      try {
        RepositoryObject objectInformation = repository.getObjectInformation(transObjectId, RepositoryObjectType.TRANSFORMATION);
        if (objectInformation!=null) {
          transName = objectInformation.getName();
          directoryPath = objectInformation.getRepositoryDirectory().getPath();
        }
      } catch(KettleException e) {
        // Ignore object reference problems.  It simply means that the reference is no longer valid.
      }
    }
    retval.append("    ").append(XMLHandler.addTagValue("trans_name", transName)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("filename", fileName)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("directory_path", directoryPath)); //$NON-NLS-1$

    retval.append("    ").append(XMLHandler.addTagValue("batch_size", batchSize)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("batch_time", batchTime)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("inject_step", injectStep)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("retrieve_step", retrieveStep)); //$NON-NLS-1$

    if (parameters != null) {
      retval.append("      ").append(XMLHandler.openTag("parameters"));

      retval.append("        ").append(XMLHandler.addTagValue("pass_all_parameters", passingAllParameters));

      for (int i = 0; i < parameters.length; i++) {
        // This is a better way of making the XML file than the arguments.
        retval.append("            ").append(XMLHandler.openTag("parameter"));

        retval.append("            ").append(XMLHandler.addTagValue("name", parameters[i]));
        retval.append("            ").append(XMLHandler.addTagValue("value", parameterValues[i]));

        retval.append("            ").append(XMLHandler.closeTag("parameter"));
      }
      retval.append("      ").append(XMLHandler.closeTag("parameters"));
    }
    return retval.toString();
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    String method = rep.getStepAttributeString(id_step, "specification_method");
    specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
    String transId = rep.getStepAttributeString(id_step, "trans_object_id");
    transObjectId = Const.isEmpty(transId) ? null : new StringObjectId(transId);
    transName = rep.getStepAttributeString(id_step, "trans_name"); //$NON-NLS-1$
    fileName = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
    directoryPath = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$

    batchSize = rep.getStepAttributeString(id_step, "batch_size"); //$NON-NLS-1$
    batchTime = rep.getStepAttributeString(id_step, "batch_time"); //$NON-NLS-1$
    injectStep = rep.getStepAttributeString(id_step, "inject_step"); //$NON-NLS-1$
    retrieveStep = rep.getStepAttributeString(id_step, "retrieve_step"); //$NON-NLS-1$

    // The parameters...
    //
    int parameternr = rep.countNrStepAttributes(id_step, "parameter_name");
    parameters = new String[parameternr];
    parameterValues = new String[parameternr];

    // Read all parameters ...
    for (int a = 0; a < parameternr; a++) {
      parameters[a] = rep.getStepAttributeString(id_step, a, "parameter_name");
      parameterValues[a] = rep.getStepAttributeString(id_step, a, "parameter_value");
    }

    passingAllParameters = rep.getStepAttributeBoolean(id_step, 0, "pass_all_parameters", true);
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    rep.saveStepAttribute(id_transformation, id_step, "specification_method", specificationMethod==null ? null : specificationMethod.getCode());
    rep.saveStepAttribute(id_transformation, id_step, "trans_object_id", transObjectId==null ? null : transObjectId.toString());
    rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "trans_name", transName); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$

    rep.saveStepAttribute(id_transformation, id_step, "batch_size", batchSize); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "batch_time", batchTime); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "inject_step", injectStep); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "retrieve_step", retrieveStep); //$NON-NLS-1$
    
    // The parameters...
    //
    // Save the parameters...
    if (parameters!=null)
    {
      for (int i=0;i<parameters.length;i++)
      {
        rep.saveStepAttribute(id_transformation, id_step, i, "parameter_name", parameters[i]);
        rep.saveStepAttribute(id_transformation, id_step, i, "parameter_value", Const.NVL(parameterValues[i], ""));
      }
    }     
    
    rep.saveStepAttribute(id_transformation, id_step, "pass_all_parameters", passingAllParameters);
  }

  public void setDefault() {
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    batchSize = "100";
    batchTime = "";
    
    passingAllParameters = true;
    
    parameters = new String[0];
    parameterValues = new String[0];
  }

  public void getFields(RowMetaInterface row, String origin, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException {

    // First load some interesting data...
    //
    // Then see which fields get added to the row.
    //
    TransMeta mappingTransMeta = null;
    try {
      mappingTransMeta = loadSingleThreadedTransMeta(this, repository, space);
    } catch (KettleException e) {
      throw new KettleStepException(BaseMessages.getString(PKG, "SingleThreaderMeta.Exception.UnableToLoadMappingTransformation"), e);
    }

    row.clear();

    // Let's keep it simple!
    //
    if (!Const.isEmpty(space.environmentSubstitute(retrieveStep))) {
      RowMetaInterface stepFields = mappingTransMeta.getStepFields(retrieveStep);
      row.addRowMeta(stepFields);
    }
  }

  public synchronized static final TransMeta loadSingleThreadedTransMeta(SingleThreaderMeta mappingMeta, Repository rep, VariableSpace space) throws KettleException {
    TransMeta mappingTransMeta = null;
    
    switch(mappingMeta.getSpecificationMethod()) {
    case FILENAME:
      String realFilename = space.environmentSubstitute(mappingMeta.getFileName());
      try {
        // OK, load the meta-data from file...
        //
        // Don't set internal variables: they belong to the parent thread!
        //
        mappingTransMeta = new TransMeta(realFilename, false); 
        mappingTransMeta.getLogChannel().logDetailed("Loading Mapping from repository", "Mapping transformation was loaded from XML file [" + realFilename + "]");
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(PKG, "SingleThreaderMeta.Exception.UnableToLoadMapping"), e);
      }
      break;
      
    case REPOSITORY_BY_NAME:
      String realTransname = space.environmentSubstitute(mappingMeta.getTransName());
      String realDirectory = space.environmentSubstitute(mappingMeta.getDirectoryPath());
      
      if (!Const.isEmpty(realTransname) && !Const.isEmpty(realDirectory) && rep != null) {
        RepositoryDirectoryInterface repdir = rep.findDirectory(realDirectory);
        if (repdir != null) {
          try {
            // reads the last revision in the repository...
            //
            mappingTransMeta = rep.loadTransformation(realTransname, repdir, null, true, null); 
            mappingTransMeta.getLogChannel().logDetailed("Loading Mapping from repository", "Mapping transformation [" + realTransname + "] was loaded from the repository");
          } catch (Exception e) {
            throw new KettleException("Unable to load transformation [" + realTransname + "]", e);
          }
        } else {
          throw new KettleException(BaseMessages.getString(PKG, "SingleThreaderMeta.Exception.UnableToLoadTransformation", realTransname) + realDirectory); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      break;
      
    case REPOSITORY_BY_REFERENCE:
      // Read the last revision by reference...
      mappingTransMeta = rep.loadTransformation(mappingMeta.getTransObjectId(), null);
      break;
    }
    return mappingTransMeta;
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
    CheckResult cr;
    if (prev == null || prev.size() == 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "SingleThreaderMeta.CheckResult.NotReceivingAnyFields"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SingleThreaderMeta.CheckResult.StepReceivingFields", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
      remarks.add(cr);
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SingleThreaderMeta.CheckResult.StepReceivingFieldsFromOtherSteps"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SingleThreaderMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    }


  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
    return new SingleThreader(stepMeta, stepDataInterface, cnr, tr, trans);
  }

  public StepDataInterface getStepData() {
    return new SingleThreaderData();
  }

  /**
   * @return the directoryPath
   */
  public String getDirectoryPath() {
    return directoryPath;
  }

  /**
   * @param directoryPath
   *          the directoryPath to set
   */
  public void setDirectoryPath(String directoryPath) {
    this.directoryPath = directoryPath;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName
   *          the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the transName
   */
  public String getTransName() {
    return transName;
  }

  /**
   * @param transName
   *          the transName to set
   */
  public void setTransName(String transName) {
    this.transName = transName;
  }

  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
    List<ResourceReference> references = new ArrayList<ResourceReference>(5);
    String realFilename = transMeta.environmentSubstitute(fileName);
    String realTransname = transMeta.environmentSubstitute(transName);
    ResourceReference reference = new ResourceReference(stepInfo);
    references.add(reference);

    if (!Const.isEmpty(realFilename)) {
      // Add the filename to the references, including a reference to this step
      // meta data.
      //
      reference.getEntries().add(new ResourceEntry(realFilename, ResourceType.ACTIONFILE));
    } else if (!Const.isEmpty(realTransname)) {
      // Add the filename to the references, including a reference to this step
      // meta data.
      //
      reference.getEntries().add(new ResourceEntry(realTransname, ResourceType.ACTIONFILE));
      references.add(reference);
    }
    return references;
  }

  @Override
  public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
    try {
      // Try to load the transformation from repository or file.
      // Modify this recursively too...
      // 
      // NOTE: there is no need to clone this step because the caller is
      // responsible for this.
      //
      // First load the mapping metadata...
      //
      TransMeta mappingTransMeta = loadSingleThreadedTransMeta(this, repository, space);

      // Also go down into the mapping transformation and export the files
      // there. (mapping recursively down)
      //
      String proposedNewFilename = mappingTransMeta.exportResources(mappingTransMeta, definitions, resourceNamingInterface, repository);

      // To get a relative path to it, we inject
      // ${Internal.Job.Filename.Directory}
      //
      String newFilename = "${" + Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY + "}/" + proposedNewFilename;

      // Set the correct filename inside the XML.
      //
      mappingTransMeta.setFilename(newFilename);

      // exports always reside in the root directory, in case we want to turn
      // this into a file repository...
      //
      mappingTransMeta.setRepositoryDirectory(new RepositoryDirectory());

      // change it in the job entry
      //
      fileName = newFilename;

      return proposedNewFilename;
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "SingleThreaderMeta.Exception.UnableToLoadTransformation", fileName)); //$NON-NLS-1$
    }
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
  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  /**
   * @return the transObjectId
   */
  public ObjectId getTransObjectId() {
    return transObjectId;
  }

  /**
   * @param transObjectId
   *          the transObjectId to set
   */
  public void setTransObjectId(ObjectId transObjectId) {
    this.transObjectId = transObjectId;
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
  public void setSpecificationMethod(ObjectLocationSpecificationMethod specificationMethod) {
    this.specificationMethod = specificationMethod;
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
   * @param batchSize the batchSize to set
   */
  public void setBatchSize(String batchSize) {
    this.batchSize = batchSize;
  }

  /**
   * @return the injectStep
   */
  public String getInjectStep() {
    return injectStep;
  }

  /**
   * @param injectStep the injectStep to set
   */
  public void setInjectStep(String injectStep) {
    this.injectStep = injectStep;
  }

  /**
   * @return the retrieveStep
   */
  public String getRetrieveStep() {
    return retrieveStep;
  }

  /**
   * @param retrieveStep the retrieveStep to set
   */
  public void setRetrieveStep(String retrieveStep) {
    this.retrieveStep = retrieveStep;
  }

  /**
   * @return the passingAllParameters
   */
  public boolean isPassingAllParameters() {
    return passingAllParameters;
  }

  /**
   * @param passingAllParameters the passingAllParameters to set
   */
  public void setPassingAllParameters(boolean passingAllParameters) {
    this.passingAllParameters = passingAllParameters;
  }

  /**
   * @return the parameters
   */
  public String[] getParameters() {
    return parameters;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

  /**
   * @return the parameterValues
   */
  public String[] getParameterValues() {
    return parameterValues;
  }

  /**
   * @param parameterValues the parameterValues to set
   */
  public void setParameterValues(String[] parameterValues) {
    this.parameterValues = parameterValues;
  }

  /**
   * @return the batchTime
   */
  public String getBatchTime() {
    return batchTime;
  }

  /**
   * @param batchTime the batchTime to set
   */
  public void setBatchTime(String batchTime) {
    this.batchTime = batchTime;
  }
  
  @Override
  public boolean hasRepositoryReferences() {
    return specificationMethod==ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
  }
  
  @Override
  public void lookupRepositoryReferences(Repository repository) throws KettleException {
    // The correct reference is stored in the trans name and directory attributes...
    //
    RepositoryDirectoryInterface repositoryDirectoryInterface = RepositoryImportLocation.getRepositoryImportLocation().findDirectory(directoryPath);
    transObjectId = repository.getTransformationID(transName, repositoryDirectoryInterface);
  }
}
