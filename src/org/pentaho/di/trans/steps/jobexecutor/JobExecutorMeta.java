/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.jobexecutor;

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
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
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
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;

/**
 * Meta-data for the Job executor step.
 * 
 * @since 29-AUG-2011
 * @author Matt
 * 
 */

public class JobExecutorMeta extends BaseStepMeta implements StepMetaInterface, HasRepositoryInterface {
  private static Class<?>                   PKG = JobExecutorMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$
  private String                            jobName;
  private String                            fileName;
  private String                            directoryPath;
  private ObjectId                          jobObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  /** The number of input rows that are sent as result rows to the job in one go, defaults to "1" */
  private String groupSize;
  
  /** Optional name of a field to group rows together that are sent together to the job as result rows (empty default) */
  private String groupField;
  
  /** Optional time in ms that is spent waiting and accumulating rows before they are given to the job as result rows (empty default, "0") */
  private String groupTime;
  
  private JobExecutorParameters                 parameters;

  private String   executionResultTargetStep;
  private StepMeta executionResultTargetStepMeta;

  /** The optional name of the output field that will contain the execution time of the job in (Integer in ms) */ 
  private String executionTimeField;

  /** The optional name of the output field that will contain the execution result (Boolean) */
  private String executionResultField;

  /** The optional name of the output field that will contain the number of errors (Integer) */
  private String executionNrErrorsField;

  /** The optional name of the output field that will contain the number of rows read (Integer) */
  private String executionLinesReadField;

  /** The optional name of the output field that will contain the number of rows written (Integer) */
  private String executionLinesWrittenField;
  
  /** The optional name of the output field that will contain the number of rows input (Integer) */
  private String executionLinesInputField;
  
  /** The optional name of the output field that will contain the number of rows output (Integer) */
  private String executionLinesOutputField;
  
  /** The optional name of the output field that will contain the number of rows rejected (Integer) */
  private String executionLinesRejectedField;
  
  /** The optional name of the output field that will contain the number of rows updated (Integer) */
  private String executionLinesUpdatedField;

  /** The optional name of the output field that will contain the number of rows deleted (Integer) */
  private String executionLinesDeletedField;

  /** The optional name of the output field that will contain the number of files retrieved (Integer) */
  private String executionFilesRetrievedField;

  /** The optional name of the output field that will contain the exit status of the last executed shell script (Integer) */
  private String executionExitStatusField;

  /** The optional name of the output field that will contain the log text of the job execution (String) */
  private String executionLogTextField;

  /** The optional name of the output field that will contain the log channel ID of the job execution (String) */
  private String executionLogChannelIdField;

  /** The optional step to send the result rows to */
  private String   resultRowsTargetStep;
  private StepMeta resultRowsTargetStepMeta;
  private String[] resultRowsField;
  private int[]    resultRowsType;
  private int[]    resultRowsLength;
  private int[]    resultRowsPrecision;
  
  /** The optional step to send the result files to */
  private String  resultFilesTargetStep;
  private StepMeta resultFilesTargetStepMeta;
  private String  resultFilesFileNameField;
  
  /**
   * This repository object is injected from the outside at runtime or at design
   * time. It comes from either Spoon or Trans
   */
  private Repository                        repository;

  public JobExecutorMeta() {
    super(); // allocate BaseStepMeta

    parameters = new JobExecutorParameters();
    resultRowsField = new String[0];
  }


  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append("    ").append(XMLHandler.addTagValue("specification_method", specificationMethod == null ? null : specificationMethod.getCode()));
    retval.append("    ").append(XMLHandler.addTagValue("job_object_id", jobObjectId == null ? null : jobObjectId.toString()));
    // Export a little bit of extra information regarding the reference since it doesn't really matter outside the same repository.
    //
    if (repository!=null && jobObjectId!=null) {
      try {
        RepositoryObject objectInformation = repository.getObjectInformation(jobObjectId, RepositoryObjectType.TRANSFORMATION);
        if (objectInformation!=null) {
          jobName = objectInformation.getName();
          directoryPath = objectInformation.getRepositoryDirectory().getPath();
        }
      } catch(KettleException e) {
        // Ignore object reference problems.  It simply means that the reference is no longer valid.
      }
    }
    retval.append("    ").append(XMLHandler.addTagValue("job_name", jobName)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("filename", fileName)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("directory_path", directoryPath)); //$NON-NLS-1$

    retval.append("    ").append(XMLHandler.addTagValue("group_size", groupSize)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("group_field", groupField)); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue("group_time", groupTime)); //$NON-NLS-1$

    // Add the mapping parameters too
    //
    retval.append("      ").append(parameters.getXML()).append(Const.CR); //$NON-NLS-1$

    // The output side...
    //
    retval.append("    ").append(XMLHandler.addTagValue("execution_result_target_step", executionResultTargetStepMeta==null ? null : executionResultTargetStepMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_time_field", executionTimeField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_result_field", executionResultField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_errors_field", executionNrErrorsField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_read_field", executionLinesReadField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_written_field", executionLinesWrittenField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_input_field", executionLinesInputField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_output_field", executionLinesOutputField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_rejected_field", executionLinesRejectedField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_updated_field", executionLinesUpdatedField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_lines_deleted_field", executionLinesDeletedField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_files_retrieved_field", executionFilesRetrievedField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_exit_status_field", executionExitStatusField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_log_text_field", executionLogTextField)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("execution_log_channelid_field", executionLogChannelIdField)); //$NON-NLS-1$ //$NON-NLS-2$
    
    retval.append("    ").append(XMLHandler.addTagValue("result_rows_target_step", resultRowsTargetStepMeta==null ? null : resultRowsTargetStepMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i=0;i<resultRowsField.length;i++) {
      retval.append("      ").append(XMLHandler.openTag("result_rows_field"));
      retval.append(XMLHandler.addTagValue("name", resultRowsField[i], false)); //$NON-NLS-1$
      retval.append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(resultRowsType[i]), false)); //$NON-NLS-1$
      retval.append(XMLHandler.addTagValue("length", resultRowsLength[i], false)); //$NON-NLS-1$
      retval.append(XMLHandler.addTagValue("precision", resultRowsPrecision[i], false)); //$NON-NLS-1$
      retval.append(XMLHandler.closeTag("result_rows_field")).append(Const.CR);
    }
      
    retval.append("    ").append(XMLHandler.addTagValue("result_files_target_step", resultFilesTargetStepMeta==null ? null : resultFilesTargetStepMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("result_files_file_name_field", resultFilesFileNameField)); //$NON-NLS-1$ //$NON-NLS-2$
    
    return retval.toString();
  }
  
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    try {
      String method = XMLHandler.getTagValue(stepnode, "specification_method");
      specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
      String jobId = XMLHandler.getTagValue(stepnode, "job_object_id");
      jobObjectId = Const.isEmpty(jobId) ? null : new StringObjectId(jobId);

      jobName = XMLHandler.getTagValue(stepnode, "job_name"); //$NON-NLS-1$
      fileName = XMLHandler.getTagValue(stepnode, "filename"); //$NON-NLS-1$
      directoryPath = XMLHandler.getTagValue(stepnode, "directory_path"); //$NON-NLS-1$

      groupSize = XMLHandler.getTagValue(stepnode, "group_size"); //$NON-NLS-1$
      groupField = XMLHandler.getTagValue(stepnode, "group_field"); //$NON-NLS-1$
      groupTime = XMLHandler.getTagValue(stepnode, "group_time"); //$NON-NLS-1$

      // Load the mapping parameters too..
      //
      Node mappingParametersNode = XMLHandler.getSubNode(stepnode, JobExecutorParameters.XML_TAG);
      parameters = new JobExecutorParameters(mappingParametersNode);

      // The output side...
      //
      executionResultTargetStep = XMLHandler.getTagValue(stepnode, "execution_result_target_step"); //$NON-NLS-1$
      executionTimeField = XMLHandler.getTagValue(stepnode, "execution_time_field"); //$NON-NLS-1$
      executionResultField = XMLHandler.getTagValue(stepnode, "execution_result_field"); //$NON-NLS-1$
      executionNrErrorsField = XMLHandler.getTagValue(stepnode, "execution_errors_field"); //$NON-NLS-1$
      executionLinesReadField = XMLHandler.getTagValue(stepnode, "execution_lines_read_field"); //$NON-NLS-1$
      executionLinesWrittenField = XMLHandler.getTagValue(stepnode, "execution_lines_written_field"); //$NON-NLS-1$
      executionLinesInputField = XMLHandler.getTagValue(stepnode, "execution_lines_input_field"); //$NON-NLS-1$
      executionLinesOutputField = XMLHandler.getTagValue(stepnode, "execution_lines_output_field"); //$NON-NLS-1$
      executionLinesRejectedField = XMLHandler.getTagValue(stepnode, "execution_lines_rejected_field"); //$NON-NLS-1$
      executionLinesUpdatedField = XMLHandler.getTagValue(stepnode, "execution_lines_updated_field"); //$NON-NLS-1$
      executionLinesDeletedField = XMLHandler.getTagValue(stepnode, "execution_lines_deleted_field"); //$NON-NLS-1$
      executionFilesRetrievedField = XMLHandler.getTagValue(stepnode, "execution_files_retrieved_field"); //$NON-NLS-1$
      executionExitStatusField = XMLHandler.getTagValue(stepnode, "execution_exit_status_field"); //$NON-NLS-1$
      executionLogTextField = XMLHandler.getTagValue(stepnode, "execution_log_text_field"); //$NON-NLS-1$
      executionLogChannelIdField = XMLHandler.getTagValue(stepnode, "execution_log_channelid_field"); //$NON-NLS-1$
      
      resultRowsTargetStep = XMLHandler.getTagValue(stepnode, "result_rows_target_step"); //$NON-NLS-1$

      int nrFields = XMLHandler.countNodes(stepnode, "result_rows_field");
      resultRowsField     = new String[nrFields];
      resultRowsType      = new int[nrFields];
      resultRowsLength    = new int[nrFields];
      resultRowsPrecision = new int[nrFields];
                                    
      for (int i=0;i<nrFields;i++) {
        
        Node fieldNode = XMLHandler.getSubNodeByNr(stepnode, "result_rows_field", i);

        resultRowsField[i] = XMLHandler.getTagValue(fieldNode, "name"); //$NON-NLS-1$
        resultRowsType[i] = ValueMeta.getType(XMLHandler.getTagValue(fieldNode, "type")); //$NON-NLS-1$
        resultRowsLength[i] = Const.toInt(XMLHandler.getTagValue(fieldNode, "length"), -1); //$NON-NLS-1$
        resultRowsPrecision[i] = Const.toInt(XMLHandler.getTagValue(fieldNode, "precision"), -1); //$NON-NLS-1$
      }
        
      resultFilesTargetStep = XMLHandler.getTagValue(stepnode, "result_files_target_step"); //$NON-NLS-1$
      resultFilesFileNameField = XMLHandler.getTagValue(stepnode, "result_files_file_name_field"); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobExecutorMeta.Exception.ErrorLoadingJobExecutorDetailsFromXML"), e); //$NON-NLS-1$
    }
  }


  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    String method = rep.getStepAttributeString(id_step, "specification_method");
    specificationMethod = ObjectLocationSpecificationMethod.getSpecificationMethodByCode(method);
    String jobId = rep.getStepAttributeString(id_step, "job_object_id");
    jobObjectId = Const.isEmpty(jobId) ? null : new StringObjectId(jobId);
    jobName = rep.getStepAttributeString(id_step, "job_name"); //$NON-NLS-1$
    fileName = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
    directoryPath = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$

    groupSize = rep.getStepAttributeString(id_step, "group_size"); //$NON-NLS-1$
    groupField = rep.getStepAttributeString(id_step, "group_field"); //$NON-NLS-1$
    groupTime = rep.getStepAttributeString(id_step, "group_time"); //$NON-NLS-1$

    parameters = new JobExecutorParameters(rep, id_step);
    
    executionResultTargetStep = rep.getStepAttributeString(id_step, "execution_result_target_step"); //$NON-NLS-1$
    executionTimeField = rep.getStepAttributeString(id_step, "execution_time_field"); //$NON-NLS-1$
    executionNrErrorsField = rep.getStepAttributeString(id_step, "execution_result_field"); //$NON-NLS-1$
    executionLinesReadField = rep.getStepAttributeString(id_step, "execution_errors_field"); //$NON-NLS-1$
    executionLinesWrittenField = rep.getStepAttributeString(id_step, "execution_lines_written_field"); //$NON-NLS-1$
    executionLinesInputField = rep.getStepAttributeString(id_step, "execution_lines_input_field"); //$NON-NLS-1$
    executionLinesOutputField = rep.getStepAttributeString(id_step, "execution_lines_output_field"); //$NON-NLS-1$
    executionLinesRejectedField = rep.getStepAttributeString(id_step, "execution_lines_rejected_field"); //$NON-NLS-1$
    executionLinesUpdatedField = rep.getStepAttributeString(id_step, "execution_lines_updated_field"); //$NON-NLS-1$
    executionLinesDeletedField = rep.getStepAttributeString(id_step, "execution_lines_deleted_field"); //$NON-NLS-1$
    executionFilesRetrievedField = rep.getStepAttributeString(id_step, "execution_files_retrieved_field"); //$NON-NLS-1$
    executionExitStatusField = rep.getStepAttributeString(id_step, "execution_exit_status_field"); //$NON-NLS-1$
    executionLogTextField = rep.getStepAttributeString(id_step, "execution_log_text_field"); //$NON-NLS-1$
    executionLogChannelIdField = rep.getStepAttributeString(id_step, "execution_log_channelid_field"); //$NON-NLS-1$

    resultRowsTargetStep = rep.getStepAttributeString(id_step, "result_rows_target_step"); //$NON-NLS-1$
    int nrFields = rep.countNrStepAttributes(id_step, "result_rows_field");
    resultRowsField     = new String[nrFields];
    resultRowsType      = new int[nrFields];
    resultRowsLength    = new int[nrFields];
    resultRowsPrecision = new int[nrFields];
    
    for (int i=0;i<nrFields;i++) {
      resultRowsField[i] = rep.getStepAttributeString(id_step, i, "result_rows_field");
      resultRowsType[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "result_rows_type"));
      resultRowsLength[i] = (int)rep.getStepAttributeInteger(id_step, i, "result_rows_length");
      resultRowsPrecision[i] = (int)rep.getStepAttributeInteger(id_step, i, "result_rows_precision");
    }
    
    resultFilesTargetStep = rep.getStepAttributeString(id_step, "result_files_target_step"); //$NON-NLS-1$
    resultFilesFileNameField = rep.getStepAttributeString(id_step, "result_files_file_name_field"); //$NON-NLS-1$
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    rep.saveStepAttribute(id_transformation, id_step, "specification_method", specificationMethod==null ? null : specificationMethod.getCode());
    rep.saveStepAttribute(id_transformation, id_step, "job_object_id", jobObjectId==null ? null : jobObjectId.toString());
    rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "job_name", jobName); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$

    rep.saveStepAttribute(id_transformation, id_step, "group_size", groupSize); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "group_field", groupField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "group_time", groupTime); //$NON-NLS-1$

    // save the mapping parameters too
    //
    parameters.saveRep(rep, id_transformation, id_step);
    
    // The output side...
    //
    rep.saveStepAttribute(id_transformation, id_step, "execution_result_target_step", executionResultTargetStepMeta==null ? null : executionResultTargetStepMeta.getName()); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_time_field", executionTimeField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_result_field", executionResultField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_errors_field", executionNrErrorsField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_read_field", executionLinesReadField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_written_field", executionLinesWrittenField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_input_field", executionLinesInputField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_output_field", executionLinesOutputField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_rejected_field", executionLinesRejectedField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_updated_field", executionLinesUpdatedField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_lines_deleted_field", executionLinesDeletedField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_files_retrieved_field", executionFilesRetrievedField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_exit_status_field", executionExitStatusField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_log_text_field", executionLogTextField); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "execution_log_channelid_field", executionLogChannelIdField); //$NON-NLS-1$

    rep.saveStepAttribute(id_transformation, id_step, "result_rows_target_step", resultRowsTargetStepMeta==null ? null : resultRowsTargetStepMeta.getName()); //$NON-NLS-1$

    for (int i=0;i<resultRowsField.length;i++) {
      rep.saveStepAttribute(id_transformation, id_step, i, "result_rows_field_name", resultRowsField[i]);
      rep.saveStepAttribute(id_transformation, id_step, i, "result_rows_field_type", ValueMeta.getTypeDesc(resultRowsType[i]));
      rep.saveStepAttribute(id_transformation, id_step, i, "result_rows_field_length", resultRowsLength[i]);
      rep.saveStepAttribute(id_transformation, id_step, i, "result_rows_field_precision", resultRowsPrecision[i]);
    }

    rep.saveStepAttribute(id_transformation, id_step, "result_files_target_step", resultFilesTargetStepMeta==null ? null : resultFilesTargetStepMeta.getName()); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, "result_files_file_name_field", resultFilesFileNameField); //$NON-NLS-1$
  }

  public void setDefault() {
    specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    parameters = new JobExecutorParameters();
    parameters.setInheritingAllVariables(true);
    
    groupSize = "1";
    groupField = "";
    groupTime = "";
    
    executionTimeField = "ExecutionTime"; //$NON-NLS-1$ 
    executionResultField = "ExecutionResult"; //$NON-NLS-1$ 
    executionNrErrorsField = "ExecutionNrErrors"; //$NON-NLS-1$ 
    executionLinesReadField = "ExecutionLinesRead"; //$NON-NLS-1$
    executionLinesWrittenField = "ExecutionLinesWritten"; //$NON-NLS-1$
    executionLinesInputField = "ExecutionLinesInput"; //$NON-NLS-1$ 
    executionLinesOutputField = "ExecutionLinesOutput"; //$NON-NLS-1$
    executionLinesRejectedField = "ExecutionLinesRejected"; //$NON-NLS-1$
    executionLinesUpdatedField = "ExecutionLinesUpdated"; //$NON-NLS-1$
    executionLinesDeletedField = "ExecutionLinesDeleted"; //$NON-NLS-1$
    executionFilesRetrievedField = "ExecutionFilesRetrieved"; //$NON-NLS-1$
    executionExitStatusField = "ExecutionExitStatus"; //$NON-NLS-1$
    executionLogTextField = "ExecutionLogText"; //$NON-NLS-1$
    executionLogChannelIdField = "ExecutionLogChannelId"; //$NON-NLS-1$
    
    resultFilesFileNameField = "FileName";
  }

  public void getFields(RowMetaInterface row, String origin, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException {
    
    row.clear();

    if (nextStep!=null && resultRowsTargetStepMeta!=null && nextStep.equals(resultRowsTargetStepMeta)) {
      for (int i=0;i<resultRowsField.length;i++) {
        ValueMetaInterface value = new ValueMeta(resultRowsField[i], resultRowsType[i], resultRowsLength[i], resultRowsPrecision[i]);
        row.addValueMeta(value);
      }
    } else if (nextStep!=null && resultFilesTargetStepMeta!=null && nextStep.equals(resultFilesTargetStepMeta)) {
      if (!Const.isEmpty(resultFilesFileNameField)) {
        ValueMetaInterface value = new ValueMeta("filename", ValueMeta.TYPE_STRING, 255, 0);
        row.addValueMeta(value);
      }
    } else if (nextStep!=null && executionResultTargetStepMeta!=null && nextStep.equals(executionResultTargetStepMeta)) {
      if (!Const.isEmpty(executionTimeField)) {
        ValueMetaInterface value = new ValueMeta(executionTimeField, ValueMeta.TYPE_INTEGER, 15, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionResultField)) {
        ValueMetaInterface value = new ValueMeta(executionResultField, ValueMeta.TYPE_BOOLEAN);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionNrErrorsField)) {
        ValueMetaInterface value = new ValueMeta(executionNrErrorsField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesReadField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesReadField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesWrittenField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesWrittenField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesInputField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesInputField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesOutputField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesOutputField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesRejectedField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesRejectedField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesUpdatedField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesUpdatedField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLinesDeletedField)) {
        ValueMetaInterface value = new ValueMeta(executionLinesDeletedField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionFilesRetrievedField)) {
        ValueMetaInterface value = new ValueMeta(executionFilesRetrievedField, ValueMeta.TYPE_INTEGER, 9, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionExitStatusField)) {
        ValueMetaInterface value = new ValueMeta(executionExitStatusField, ValueMeta.TYPE_INTEGER, 3, 0);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLogTextField)) {
        ValueMetaInterface value = new ValueMeta(executionLogTextField, ValueMeta.TYPE_STRING);
        value.setLargeTextField(true);
        row.addValueMeta(value);
      }
      if (!Const.isEmpty(executionLogChannelIdField)) {
        ValueMetaInterface value = new ValueMeta(executionLogChannelIdField, ValueMeta.TYPE_STRING, 50, 0);
        row.addValueMeta(value);
      }
    }
  }

  public String[] getInfoSteps() {
    String[] infoSteps = getStepIOMeta().getInfoStepnames();
    // Return null instead of empty array to preserve existing behavior
    return infoSteps.length == 0 ? null : infoSteps;
  }

  public String[] getTargetSteps() {

    List<String> targetSteps = new ArrayList<String>();

    if (!Const.isEmpty(resultFilesTargetStep)) {
      targetSteps.add(resultFilesTargetStep);
    }
    if (!Const.isEmpty(resultRowsTargetStep)) {
      targetSteps.add(resultRowsTargetStep);
    }
    
    if (targetSteps.isEmpty())
      return null;

    return targetSteps.toArray(new String[targetSteps.size()]);
  }

  public synchronized static final JobMeta loadMappingMeta(JobExecutorMeta executorMeta, Repository rep, VariableSpace space) throws KettleException {
    JobMeta mappingJobMeta = null;
    
    switch(executorMeta.getSpecificationMethod()) {
    case FILENAME:
      String realFilename = space.environmentSubstitute(executorMeta.getFileName());
      try {
        // OK, load the meta-data from file...
        //
        // Don't set internal variables: they belong to the parent thread!
        //
        mappingJobMeta = new JobMeta(realFilename, rep); 
        LogChannel.GENERAL.logDetailed("Loading job from repository", "Job was loaded from XML file [" + realFilename + "]");
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(PKG, "JobExecutorMeta.Exception.UnableToLoadJob"), e);
      }
      break;
      
    case REPOSITORY_BY_NAME:
      String realJobname = space.environmentSubstitute(executorMeta.getJobName());
      String realDirectory = space.environmentSubstitute(executorMeta.getDirectoryPath());
      
      if (!Const.isEmpty(realJobname) && !Const.isEmpty(realDirectory) && rep != null) {
        RepositoryDirectoryInterface repdir = rep.findDirectory(realDirectory);
        if (repdir != null) {
          try {
            // reads the last revision in the repository...
            //
            mappingJobMeta = rep.loadJob(realJobname, repdir, null, null); 
            LogChannel.GENERAL.logDetailed("Loading job from repository", "Executor job [" + realJobname + "] was loaded from the repository");
          } catch (Exception e) {
            throw new KettleException("Unable to load job [" + realJobname + "]", e);
          }
        } else {
          throw new KettleException(BaseMessages.getString(PKG, "JobExecutorMeta.Exception.UnableToLoadJob", realJobname) + realDirectory); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      break;
      
    case REPOSITORY_BY_REFERENCE:
      // Read the last revision by reference...
      mappingJobMeta = rep.loadJob(executorMeta.getJobObjectId(), null);
      break;
    }
    
    mappingJobMeta.copyVariablesFrom(space);
    
    return mappingJobMeta;
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
    CheckResult cr;
    if (prev == null || prev.size() == 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "JobExecutorMeta.CheckResult.NotReceivingAnyFields"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JobExecutorMeta.CheckResult.StepReceivingFields", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
      remarks.add(cr);
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JobExecutorMeta.CheckResult.StepReceivingFieldsFromOtherSteps"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JobExecutorMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    }
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
    return new JobExecutor(stepMeta, stepDataInterface, cnr, tr, trans);
  }

  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
    List<ResourceReference> references = new ArrayList<ResourceReference>(5);
    String realFilename = transMeta.environmentSubstitute(fileName);
    String realTransname = transMeta.environmentSubstitute(jobName);
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
      // First load the executor job metadata...
      //
      JobMeta executorJobMeta = loadMappingMeta(this, repository, space);

      // Also go down into the mapping transformation and export the files
      // there. (mapping recursively down)
      //
      String proposedNewFilename = executorJobMeta.exportResources(executorJobMeta, definitions, resourceNamingInterface, repository);

      // To get a relative path to it, we inject
      // ${Internal.Transformation.Filename.Directory}
      //
      String newFilename = "${" + Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY + "}/" + proposedNewFilename;

      // Set the correct filename inside the XML.
      //
      executorJobMeta.setFilename(newFilename);

      // exports always reside in the root directory, in case we want to turn
      // this into a file repository...
      //
      executorJobMeta.setRepositoryDirectory(new RepositoryDirectory());

      // change it in the job entry
      //
      fileName = newFilename;

      return proposedNewFilename;
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "JobExecutorMeta.Exception.UnableToLoadJob", fileName)); //$NON-NLS-1$
    }
  }

  public StepDataInterface getStepData() {
    return new JobExecutorData();
  }

  @Override
  public StepIOMetaInterface getStepIOMeta() {
    if (ioMeta == null) {

      ioMeta = new StepIOMeta(true, true, true, false, true, false);
      
      ioMeta.addStream(new Stream(StreamType.TARGET, executionResultTargetStepMeta, BaseMessages.getString(PKG,
      "JobExecutorMeta.ResultStream.Description"), StreamIcon.TARGET, null));
      ioMeta.addStream(new Stream(StreamType.TARGET, resultRowsTargetStepMeta, BaseMessages.getString(PKG,
      "JobExecutorMeta.ResultRowsStream.Description"), StreamIcon.TARGET, null));
      ioMeta.addStream(new Stream(StreamType.TARGET, resultFilesTargetStepMeta, BaseMessages.getString(PKG,
      "JobExecutorMeta.ResultFilesStream.Description"), StreamIcon.TARGET, null));
    }
    return ioMeta;
  }
  
  /**
   * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
   * @param stream The optional stream to handle.
   */
  public void handleStreamSelection(StreamInterface stream) {
    // This step targets another step.
    // Make sure that we don't specify the same step for more than 1 target...
    //
    List<StreamInterface> targets = getStepIOMeta().getTargetStreams();
    int index = targets.indexOf(stream);
    StepMeta step = targets.get(index).getStepMeta();
    switch(index) {
    case 0: setExecutionResultTargetStepMeta(step); break; 
    case 1: setResultRowsTargetStepMeta(step); break; 
    case 2: setResultFilesTargetStepMeta(step); break; 
    }
    
  }

  
  /**
   * Remove the cached {@link StepIOMeta} so it is recreated when it is next accessed.
   */
  public void resetStepIoMeta() {
  }

  @Override
  public void searchInfoAndTargetSteps(List<StepMeta> steps) {
    executionResultTargetStepMeta = StepMeta.findStep(steps, executionResultTargetStep);
    resultRowsTargetStepMeta = StepMeta.findStep(steps, resultRowsTargetStep);
    resultFilesTargetStepMeta = StepMeta.findStep(steps, resultFilesTargetStep);
  }
  
  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, };
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
    jobObjectId = repository.getTransformationID(jobName, repositoryDirectoryInterface);
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
   * @return the mappingParameters
   */
  public JobExecutorParameters getMappingParameters() {
    return parameters;
  }

  /**
   * @param mappingParameters
   *          the mappingParameters to set
   */
  public void setMappingParameters(JobExecutorParameters mappingParameters) {
    this.parameters = mappingParameters;
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

  /**
   * @return the jobName
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * @param jobName the jobName to set
   */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  /**
   * @return the jobObjectId
   */
  public ObjectId getJobObjectId() {
    return jobObjectId;
  }

  /**
   * @param jobObjectId the jobObjectId to set
   */
  public void setJobObjectId(ObjectId jobObjectId) {
    this.jobObjectId = jobObjectId;
  }

  /**
   * @return the parameters
   */
  public JobExecutorParameters getParameters() {
    return parameters;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(JobExecutorParameters parameters) {
    this.parameters = parameters;
  }

  /**
   * @return the executionTimeField
   */
  public String getExecutionTimeField() {
    return executionTimeField;
  }

  /**
   * @param executionTimeField the executionTimeField to set
   */
  public void setExecutionTimeField(String executionTimeField) {
    this.executionTimeField = executionTimeField;
  }

  /**
   * @return the executionResultField
   */
  public String getExecutionResultField() {
    return executionResultField;
  }

  /**
   * @param executionResultField the executionResultField to set
   */
  public void setExecutionResultField(String executionResultField) {
    this.executionResultField = executionResultField;
  }

  /**
   * @return the executionNrErrorsField
   */
  public String getExecutionNrErrorsField() {
    return executionNrErrorsField;
  }

  /**
   * @param executionNrErrorsField the executionNrErrorsField to set
   */
  public void setExecutionNrErrorsField(String executionNrErrorsField) {
    this.executionNrErrorsField = executionNrErrorsField;
  }

  /**
   * @return the executionLinesReadField
   */
  public String getExecutionLinesReadField() {
    return executionLinesReadField;
  }

  /**
   * @param executionLinesReadField the executionLinesReadField to set
   */
  public void setExecutionLinesReadField(String executionLinesReadField) {
    this.executionLinesReadField = executionLinesReadField;
  }

  /**
   * @return the executionLinesWrittenField
   */
  public String getExecutionLinesWrittenField() {
    return executionLinesWrittenField;
  }

  /**
   * @param executionLinesWrittenField the executionLinesWrittenField to set
   */
  public void setExecutionLinesWrittenField(String executionLinesWrittenField) {
    this.executionLinesWrittenField = executionLinesWrittenField;
  }

  /**
   * @return the executionLinesInputField
   */
  public String getExecutionLinesInputField() {
    return executionLinesInputField;
  }

  /**
   * @param executionLinesInputField the executionLinesInputField to set
   */
  public void setExecutionLinesInputField(String executionLinesInputField) {
    this.executionLinesInputField = executionLinesInputField;
  }

  /**
   * @return the executionLinesOutputField
   */
  public String getExecutionLinesOutputField() {
    return executionLinesOutputField;
  }

  /**
   * @param executionLinesOutputField the executionLinesOutputField to set
   */
  public void setExecutionLinesOutputField(String executionLinesOutputField) {
    this.executionLinesOutputField = executionLinesOutputField;
  }

  /**
   * @return the executionLinesRejectedField
   */
  public String getExecutionLinesRejectedField() {
    return executionLinesRejectedField;
  }

  /**
   * @param executionLinesRejectedField the executionLinesRejectedField to set
   */
  public void setExecutionLinesRejectedField(String executionLinesRejectedField) {
    this.executionLinesRejectedField = executionLinesRejectedField;
  }

  /**
   * @return the executionLinesUpdatedField
   */
  public String getExecutionLinesUpdatedField() {
    return executionLinesUpdatedField;
  }

  /**
   * @param executionLinesUpdatedField the executionLinesUpdatedField to set
   */
  public void setExecutionLinesUpdatedField(String executionLinesUpdatedField) {
    this.executionLinesUpdatedField = executionLinesUpdatedField;
  }

  /**
   * @return the executionLinesDeletedField
   */
  public String getExecutionLinesDeletedField() {
    return executionLinesDeletedField;
  }

  /**
   * @param executionLinesDeletedField the executionLinesDeletedField to set
   */
  public void setExecutionLinesDeletedField(String executionLinesDeletedField) {
    this.executionLinesDeletedField = executionLinesDeletedField;
  }

  /**
   * @return the executionFilesRetrievedField
   */
  public String getExecutionFilesRetrievedField() {
    return executionFilesRetrievedField;
  }

  /**
   * @param executionFilesRetrievedField the executionFilesRetrievedField to set
   */
  public void setExecutionFilesRetrievedField(String executionFilesRetrievedField) {
    this.executionFilesRetrievedField = executionFilesRetrievedField;
  }

  /**
   * @return the executionExitStatusField
   */
  public String getExecutionExitStatusField() {
    return executionExitStatusField;
  }

  /**
   * @param executionExitStatusField the executionExitStatusField to set
   */
  public void setExecutionExitStatusField(String executionExitStatusField) {
    this.executionExitStatusField = executionExitStatusField;
  }

  /**
   * @return the executionLogTextField
   */
  public String getExecutionLogTextField() {
    return executionLogTextField;
  }

  /**
   * @param executionLogTextField the executionLogTextField to set
   */
  public void setExecutionLogTextField(String executionLogTextField) {
    this.executionLogTextField = executionLogTextField;
  }

  /**
   * @return the executionLogChannelIdField
   */
  public String getExecutionLogChannelIdField() {
    return executionLogChannelIdField;
  }

  /**
   * @param executionLogChannelIdField the executionLogChannelIdField to set
   */
  public void setExecutionLogChannelIdField(String executionLogChannelIdField) {
    this.executionLogChannelIdField = executionLogChannelIdField;
  }

  /**
   * @return the resultRowsTargetStep
   */
  public String getResultRowsTargetStep() {
    return resultRowsTargetStep;
  }

  /**
   * @param resultRowsTargetStep the resultRowsTargetStep to set
   */
  public void setResultRowsTargetStep(String resultRowsTargetStep) {
    this.resultRowsTargetStep = resultRowsTargetStep;
  }

  /**
   * @return the resultRowsField
   */
  public String[] getResultRowsField() {
    return resultRowsField;
  }

  /**
   * @param resultRowsField the resultRowsField to set
   */
  public void setResultRowsField(String[] resultRowsField) {
    this.resultRowsField = resultRowsField;
  }

  /**
   * @return the resultRowsType
   */
  public int[] getResultRowsType() {
    return resultRowsType;
  }

  /**
   * @param resultRowsType the resultRowsType to set
   */
  public void setResultRowsType(int[] resultRowsType) {
    this.resultRowsType = resultRowsType;
  }

  /**
   * @return the resultRowsLength
   */
  public int[] getResultRowsLength() {
    return resultRowsLength;
  }

  /**
   * @param resultRowsLength the resultRowsLength to set
   */
  public void setResultRowsLength(int[] resultRowsLength) {
    this.resultRowsLength = resultRowsLength;
  }

  /**
   * @return the resultRowsPrecision
   */
  public int[] getResultRowsPrecision() {
    return resultRowsPrecision;
  }

  /**
   * @param resultRowsPrecision the resultRowsPrecision to set
   */
  public void setResultRowsPrecision(int[] resultRowsPrecision) {
    this.resultRowsPrecision = resultRowsPrecision;
  }

  /**
   * @return the resultFilesTargetStep
   */
  public String getResultFilesTargetStep() {
    return resultFilesTargetStep;
  }

  /**
   * @param resultFilesTargetStep the resultFilesTargetStep to set
   */
  public void setResultFilesTargetStep(String resultFilesTargetStep) {
    this.resultFilesTargetStep = resultFilesTargetStep;
  }

  /**
   * @return the resultRowsTargetStepMeta
   */
  public StepMeta getResultRowsTargetStepMeta() {
    return resultRowsTargetStepMeta;
  }

  /**
   * @param resultRowsTargetStepMeta the resultRowsTargetStepMeta to set
   */
  public void setResultRowsTargetStepMeta(StepMeta resultRowsTargetStepMeta) {
    this.resultRowsTargetStepMeta = resultRowsTargetStepMeta;
  }

  /**
   * @return the resultFilesTargetStepMeta
   */
  public StepMeta getResultFilesTargetStepMeta() {
    return resultFilesTargetStepMeta;
  }

  /**
   * @param resultFilesTargetStepMeta the resultFilesTargetStepMeta to set
   */
  public void setResultFilesTargetStepMeta(StepMeta resultFilesTargetStepMeta) {
    this.resultFilesTargetStepMeta = resultFilesTargetStepMeta;
  }

  /**
   * @return the groupSize
   */
  public String getGroupSize() {
    return groupSize;
  }

  /**
   * @param groupSize the groupSize to set
   */
  public void setGroupSize(String groupSize) {
    this.groupSize = groupSize;
  }

  /**
   * @return the groupField
   */
  public String getGroupField() {
    return groupField;
  }

  /**
   * @param groupField the groupField to set
   */
  public void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  /**
   * @return the groupTime
   */
  public String getGroupTime() {
    return groupTime;
  }

  /**
   * @param groupTime the groupTime to set
   */
  public void setGroupTime(String groupTime) {
    this.groupTime = groupTime;
  }
  
  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }


  /**
   * @return the executionResultTargetStep
   */
  public String getExecutionResultTargetStep() {
    return executionResultTargetStep;
  }


  /**
   * @param executionResultTargetStep the executionResultTargetStep to set
   */
  public void setExecutionResultTargetStep(String executionResultTargetStep) {
    this.executionResultTargetStep = executionResultTargetStep;
  }


  /**
   * @return the executionResultTargetStepMeta
   */
  public StepMeta getExecutionResultTargetStepMeta() {
    return executionResultTargetStepMeta;
  }


  /**
   * @param executionResultTargetStepMeta the executionResultTargetStepMeta to set
   */
  public void setExecutionResultTargetStepMeta(StepMeta executionResultTargetStepMeta) {
    this.executionResultTargetStepMeta = executionResultTargetStepMeta;
  }


  /**
   * @return the resultFilesFileNameField
   */
  public String getResultFilesFileNameField() {
    return resultFilesFileNameField;
  }


  /**
   * @param resultFilesFileNameField the resultFilesFileNameField to set
   */
  public void setResultFilesFileNameField(String resultFilesFileNameField) {
    this.resultFilesFileNameField = resultFilesFileNameField;
  }
  
  
}
