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

package org.pentaho.di.trans.steps.metainject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * @since 2007-07-05
 * @author matt
 * @version 3.0
 */

public class MetaInjectMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = MetaInjectMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    // description of the transformation to execute...
    //
    private String                            transName;
    private String                            fileName;
    private String                            directoryPath;
    private ObjectId                          transObjectId;
    private ObjectLocationSpecificationMethod specificationMethod;
    
    private String                            sourceStepName;

    private Map<TargetStepAttribute, SourceStepField> targetSourceMapping;
    
    private String                            targetFile;
    private boolean                           noExecution;
	
	public MetaInjectMeta()
	{
		super(); // allocate BaseStepMeta
		specificationMethod=ObjectLocationSpecificationMethod.FILENAME;
		targetSourceMapping = new HashMap<TargetStepAttribute, SourceStepField>();
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}

	public void setDefault() {
	}
	
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(500);
		
	    retval.append("    ").append(XMLHandler.addTagValue("specification_method", specificationMethod == null ? null : specificationMethod.getCode()));
	    retval.append("    ").append(XMLHandler.addTagValue("trans_object_id", transObjectId == null ? null : transObjectId.toString()));
	    retval.append("    ").append(XMLHandler.addTagValue("trans_name", transName)); //$NON-NLS-1$
	    retval.append("    ").append(XMLHandler.addTagValue("filename", fileName)); //$NON-NLS-1$
	    retval.append("    ").append(XMLHandler.addTagValue("directory_path", directoryPath)); //$NON-NLS-1$

	    retval.append("    ").append(XMLHandler.addTagValue("source_step", sourceStepName)); //$NON-NLS-1$
      retval.append("    ").append(XMLHandler.addTagValue("target_file", targetFile)); //$NON-NLS-1$
      retval.append("    ").append(XMLHandler.addTagValue("no_execution", noExecution)); //$NON-NLS-1$

	    retval.append("    ").append(XMLHandler.openTag("mappings"));
	    for (TargetStepAttribute target : targetSourceMapping.keySet()) {
	      retval.append("      ").append(XMLHandler.openTag("mapping"));
	      SourceStepField source = targetSourceMapping.get(target);
	      retval.append("        ").append(XMLHandler.addTagValue("target_step_name", target.getStepname()));
          retval.append("        ").append(XMLHandler.addTagValue("target_attribute_key", target.getAttributeKey()));
          retval.append("        ").append(XMLHandler.addTagValue("target_detail", target.isDetail()));
          retval.append("        ").append(XMLHandler.addTagValue("source_step", source.getStepname()));
          retval.append("        ").append(XMLHandler.addTagValue("source_field", source.getField()));
          retval.append("      ").append(XMLHandler.closeTag("mapping"));
	    }
	    retval.append("    ").append(XMLHandler.closeTag("mappings"));

		return retval.toString();
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

        sourceStepName = XMLHandler.getTagValue(stepnode, "source_step"); //$NON-NLS-1$
        targetFile = XMLHandler.getTagValue(stepnode, "target_file"); //$NON-NLS-1$
        noExecution= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "no_execution")); //$NON-NLS-1$

        Node mappingsNode = XMLHandler.getSubNode(stepnode, "mappings");
        int nrMappings = XMLHandler.countNodes(mappingsNode, "mapping");
        for (int i=0;i<nrMappings;i++) {
          Node mappingNode = XMLHandler.getSubNodeByNr(mappingsNode, "mapping", i);
          String targetStepname = XMLHandler.getTagValue(mappingNode, "target_step_name");
          String targetAttributeKey = XMLHandler.getTagValue(mappingNode, "target_attribute_key");
          boolean targetDetail = "Y".equalsIgnoreCase(XMLHandler.getTagValue(mappingNode, "target_detail"));
          String sourceStepname = XMLHandler.getTagValue(mappingNode, "source_step");
          String sourceField = XMLHandler.getTagValue(mappingNode, "source_field");
          
          TargetStepAttribute target = new TargetStepAttribute(targetStepname, targetAttributeKey, targetDetail);
          SourceStepField source = new SourceStepField(sourceStepname, sourceField);
          targetSourceMapping.put(target, source);
        }
      } catch (Exception e) {
          throw new KettleXMLException("Unable to load step info from XML", e);
      }
    }

	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try {
		    String method = rep.getStepAttributeString(id_step, "specification_method");
		    specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
		    String transId = rep.getStepAttributeString(id_step, "trans_object_id");
		    transObjectId = Const.isEmpty(transId) ? null : new StringObjectId(transId);
		    transName = rep.getStepAttributeString(id_step, "trans_name"); //$NON-NLS-1$
		    fileName = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
		    directoryPath = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$

        sourceStepName = rep.getStepAttributeString(id_step, "source_step"); //$NON-NLS-1$
        targetFile = rep.getStepAttributeString(id_step, "target_file"); //$NON-NLS-1$
        noExecution = rep.getStepAttributeBoolean(id_step, "no_execution"); //$NON-NLS-1$

        int nrMappings = rep.countNrStepAttributes(id_step, "mapping_target_step_name");
        for (int i=0;i<nrMappings;i++) {
          String targetStepname = rep.getStepAttributeString(id_step, i, "mapping_target_step_name");
          String targetAttributeKey = rep.getStepAttributeString(id_step, i, "mapping_target_attribute_key");
          boolean targetDetail = rep.getStepAttributeBoolean(id_step, i, "mapping_target_detail");
          String sourceStepname = rep.getStepAttributeString(id_step, i, "mapping_source_step");
          String sourceField = rep.getStepAttributeString(id_step, i, "mapping_source_field");
          
          TargetStepAttribute target = new TargetStepAttribute(targetStepname, targetAttributeKey, targetDetail);
          SourceStepField source = new SourceStepField(sourceStepname, sourceField);
          targetSourceMapping.put(target, source);
        }
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try {
		    rep.saveStepAttribute(id_transformation, id_step, "specification_method", specificationMethod==null ? null : specificationMethod.getCode());
		    rep.saveStepAttribute(id_transformation, id_step, "trans_object_id", transObjectId==null ? null : transObjectId.toString());
		    rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, id_step, "trans_name", transName); //$NON-NLS-1$
		    rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$

        rep.saveStepAttribute(id_transformation, id_step, "source_step", sourceStepName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "target_file", targetFile); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "no_execution", noExecution); //$NON-NLS-1$

        List<TargetStepAttribute> keySet = new ArrayList<TargetStepAttribute>(targetSourceMapping.keySet());
        for (int i=0;i<keySet.size();i++) {
          TargetStepAttribute target = keySet.get(i);
          SourceStepField source = targetSourceMapping.get(target);
          
          rep.saveStepAttribute(id_transformation, id_step, i, "mapping_target_step_name", target.getStepname());
          rep.saveStepAttribute(id_transformation, id_step, i, "mapping_target_attribute_key", target.getAttributeKey());
          rep.saveStepAttribute(id_transformation, id_step, i, "mapping_target_detail", target.isDetail());
          rep.saveStepAttribute(id_transformation, id_step, i, "mapping_source_step", source.getStepname());
          rep.saveStepAttribute(id_transformation, id_step, i, "mapping_source_field", source.getField());
        }
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		rowMeta.clear(); // No defined output is expected from this step.
	}
	
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new MetaInject(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new MetaInjectData();
	}

	public Map<TargetStepAttribute, SourceStepField> getTargetSourceMapping() {
      return targetSourceMapping;
    }
	
	public void setTargetSourceMapping(Map<TargetStepAttribute, SourceStepField> targetSourceMapping) {
      this.targetSourceMapping = targetSourceMapping;
    }
	
	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
	  // TODO Auto-generated method stub
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
  public void setTransName(String transName) {
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
  public void setFileName(String fileName) {
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
  public void setDirectoryPath(String directoryPath) {
    this.directoryPath = directoryPath;
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
   * @param specificationMethod the specificationMethod to set
   */
  public void setSpecificationMethod(ObjectLocationSpecificationMethod specificationMethod) {
    this.specificationMethod = specificationMethod;
  }
  
  public synchronized static final TransMeta loadTransformationMeta(MetaInjectMeta mappingMeta, Repository rep, VariableSpace space) throws KettleException {
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
        throw new KettleException(BaseMessages.getString(PKG, "MetaInjectMeta.Exception.UnableToLoadTransformationFromFile", realFilename), e);
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
          throw new KettleException(BaseMessages.getString(PKG, "MetaInjectMeta.Exception.UnableToLoadTransformationFromRepository", realTransname, realDirectory)); //$NON-NLS-1$ //$NON-NLS-2$
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
  public void setSourceStepName(String sourceStepName) {
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
  public void setTargetFile(String targetFile) {
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
  public void setNoExecution(boolean noExecution) {
    this.noExecution = noExecution;
  }
 
  /**
   * @return The objects referenced in the step, like a mapping, a transformation, a job, ... 
   */
  public String[] getReferencedObjectDescriptions() {
    return new String[] { BaseMessages.getString(PKG, "MetaInjectMeta.ReferencedObject.Description"), };
  }

  private boolean isTransformationDefined() {
    return !Const.isEmpty(fileName) || transObjectId!=null || (!Const.isEmpty(this.directoryPath) && !Const.isEmpty(transName));
  }

  public boolean[] isReferencedObjectEnabled() {
    return new boolean[] { isTransformationDefined(), };
  }
  
  /**
   * Load the referenced object
   * @param meta The metadata that references 
   * @param index the object index to load
   * @param rep the repository
   * @param space the variable space to use
   * @return the referenced object once loaded
   * @throws KettleException
   */
  public Object loadReferencedObject(int index, Repository rep, VariableSpace space) throws KettleException {
    return loadTransformationMeta(this, rep, space);
  }

}