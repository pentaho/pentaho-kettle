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
 
package org.pentaho.di.trans.steps.autodoc;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder.OutputType;
import org.w3c.dom.Node;


/**
 * @since 2009-12-01
 * @author matt
 * @version 4
 */
@Step(
		id="AutoDoc",
		name="AutoDoc.Step.Name",
		description="AutoDoc.Step.Description",
		i18nPackageName="org.pentaho.di.autodoc",
		image="org/pentaho/di/autodoc/autodoc.png",
		categoryDescription="Autodoc.Category.PDI-EE"
	)
public class AutoDocMeta extends BaseStepMeta implements StepMetaInterface, AutoDocOptionsInterface
{
	private static Class<?> PKG = AutoDocMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private String filenameField;
	private String fileTypeField;
	
	private String targetFilename;
	private OutputType outputType;
	private boolean includingName;
	private boolean includingDescription;
	private boolean includingExtendedDescription;
	private boolean includingCreated;
	private boolean includingModified;
	private boolean includingImage;
	private boolean includingLoggingConfiguration;
	private boolean includingLastExecutionResult;
	
	public AutoDocMeta()
	{
		super(); // allocate BaseStepMeta
		
		outputType = OutputType.PDF;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}

	public void setDefault() {
	  outputType = OutputType.PDF;
	  targetFilename = "${Internal.Transformation.Filename.Directory}/kettle-autodoc.pdf";
	  includingName=true;
	  includingDescription=true;
	  includingExtendedDescription=true;
	  includingCreated=true;
	  includingModified=true;
	  includingImage=true;
	  includingLoggingConfiguration=true;
	  includingLastExecutionResult=true;
	}
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filenameField = XMLHandler.getTagValue(stepnode, "filename_field");
      fileTypeField = XMLHandler.getTagValue(stepnode, "file_type_field");
			targetFilename = XMLHandler.getTagValue(stepnode, "target_file");
			includingName = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_name"));
			includingDescription = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_description"));
			includingExtendedDescription = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_extended_description"));
			includingCreated = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_creation"));
			includingModified = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_modification"));
			includingImage = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_image"));
			includingLoggingConfiguration = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_logging_config"));
			includingLastExecutionResult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_last_exec_result"));

			try {
				outputType = KettleReportBuilder.OutputType.valueOf( XMLHandler.getTagValue(stepnode, "output_type") );
			} catch(Exception e) {
				outputType = KettleReportBuilder.OutputType.PDF;
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate() {
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(500);

		retval.append("    ").append(XMLHandler.addTagValue("filename_field", filenameField));
    retval.append("    ").append(XMLHandler.addTagValue("file_type_field", fileTypeField));
		retval.append("    ").append(XMLHandler.addTagValue("target_file", targetFilename));
		retval.append("    ").append(XMLHandler.addTagValue("output_type", outputType.name()));
		retval.append("    ").append(XMLHandler.addTagValue("include_name", includingName));
		retval.append("    ").append(XMLHandler.addTagValue("include_description", includingDescription));
		retval.append("    ").append(XMLHandler.addTagValue("include_extended_description", includingExtendedDescription));
		retval.append("    ").append(XMLHandler.addTagValue("include_creation", includingCreated));
		retval.append("    ").append(XMLHandler.addTagValue("include_modification", includingModified));
		retval.append("    ").append(XMLHandler.addTagValue("include_image", includingImage));
		retval.append("    ").append(XMLHandler.addTagValue("include_logging_config", includingLoggingConfiguration));
		retval.append("    ").append(XMLHandler.addTagValue("include_last_exec_result", includingLastExecutionResult));

		return retval.toString();
	}


	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			filenameField = rep.getStepAttributeString(id_step, "filename_field");
      fileTypeField = rep.getStepAttributeString(id_step, "file_type_field");
			targetFilename = rep.getStepAttributeString(id_step, "target_file");
			try {
				outputType = KettleReportBuilder.OutputType.valueOf( rep.getStepAttributeString(id_step, "output_type") );
			} catch(Exception e) {
				outputType = KettleReportBuilder.OutputType.PDF;
			}
			includingName = rep.getStepAttributeBoolean(id_step, "include_name");
			includingDescription = rep.getStepAttributeBoolean(id_step, "include_description");
			includingExtendedDescription = rep.getStepAttributeBoolean(id_step, "include_extended_description");
			includingCreated = rep.getStepAttributeBoolean(id_step, "include_creation");
			includingModified = rep.getStepAttributeBoolean(id_step, "include_modification");
			includingImage = rep.getStepAttributeBoolean(id_step, "include_image");
			includingLoggingConfiguration = rep.getStepAttributeBoolean(id_step, "include_logging_config");
			includingLastExecutionResult = rep.getStepAttributeBoolean(id_step, "include_last_exec_result");
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "filename_field", filenameField);
      rep.saveStepAttribute(id_transformation, id_step, "file_type_field", filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "target_file", targetFilename);
			rep.saveStepAttribute(id_transformation, id_step, "output_type", outputType.name());
			rep.saveStepAttribute(id_transformation, id_step, "include_name", includingName);
			rep.saveStepAttribute(id_transformation, id_step, "include_description", includingDescription);
			rep.saveStepAttribute(id_transformation, id_step, "include_extended_description", includingExtendedDescription);
			rep.saveStepAttribute(id_transformation, id_step, "include_creation", includingCreated);
			rep.saveStepAttribute(id_transformation, id_step, "include_modification", includingModified);
			rep.saveStepAttribute(id_transformation, id_step, "include_image", includingImage);
			rep.saveStepAttribute(id_transformation, id_step, "include_logging_config", includingLoggingConfiguration);
			rep.saveStepAttribute(id_transformation, id_step, "include_last_exec_result", includingLastExecutionResult);
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		if (outputType==OutputType.METADATA) {
		  
		  // Add a bunch of metadata to the output for each input row
		  //
      ValueMetaInterface valueMeta = new ValueMeta("trans_meta", ValueMetaInterface.TYPE_SERIALIZABLE);
      valueMeta.setOrigin(origin);
      rowMeta.addValueMeta(valueMeta);
		  
      if (includingName) {
		    valueMeta = new ValueMeta("name", ValueMetaInterface.TYPE_STRING);
		    valueMeta.setOrigin(origin);
		    rowMeta.addValueMeta(valueMeta);
		  }
      if (includingDescription) {
        valueMeta = new ValueMeta("description", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingExtendedDescription) {
        valueMeta = new ValueMeta("extended_description", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingCreated) {
        valueMeta = new ValueMeta("created", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingModified) {
        valueMeta = new ValueMeta("modified", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingImage) {
        valueMeta = new ValueMeta("image", ValueMetaInterface.TYPE_BINARY);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingLoggingConfiguration) {
        valueMeta = new ValueMeta("logging", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
      if (includingLastExecutionResult) {
        valueMeta = new ValueMeta("last_result", ValueMetaInterface.TYPE_STRING);
        valueMeta.setOrigin(origin);
        rowMeta.addValueMeta(valueMeta);
      }
		} else {
		  
		  rowMeta.clear(); // Start with a clean slate, eats the input
	    
		  // Generate one report in the output...
		  //
	    ValueMetaInterface valueMeta = new ValueMeta("filename", ValueMetaInterface.TYPE_STRING);
	    valueMeta.setOrigin(origin);
	    rowMeta.addValueMeta(valueMeta);
		}
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AutoDocMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AutoDocMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AutoDocMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AutoDocMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new AutoDoc(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new AutoDocData();
	}

	/**
	 * @return the filenameField
	 */
	public String getFilenameField() {
		return filenameField;
	}

	/**
	 * @param filenameField the filenameField to set
	 */
	public void setFilenameField(String filenameField) {
		this.filenameField = filenameField;
	}

	/**
	 * @return the targetFilename
	 */
	public String getTargetFilename() {
		return targetFilename;
	}

	/**
	 * @param targetFilename the targetFilename to set
	 */
	public void setTargetFilename(String targetFilename) {
		this.targetFilename = targetFilename;
	}

	/**
	 * @return the outputType
	 */
	public OutputType getOutputType() {
		return outputType;
	}

	/**
	 * @param outputType the outputType to set
	 */
	public void setOutputType(OutputType outputType) {
		this.outputType = outputType;
	}

	/**
	 * @return the includingDescription
	 */
	public boolean isIncludingDescription() {
		return includingDescription;
	}

	/**
	 * @param includingDescription the includingDescription to set
	 */
	public void setIncludingDescription(boolean includingDescription) {
		this.includingDescription = includingDescription;
	}

	/**
	 * @return the includingCreated
	 */
	public boolean isIncludingCreated() {
		return includingCreated;
	}

	/**
	 * @param includingCreated the includingCreated to set
	 */
	public void setIncludingCreated(boolean includingCreated) {
		this.includingCreated = includingCreated;
	}

	/**
	 * @return the includingModified
	 */
	public boolean isIncludingModified() {
		return includingModified;
	}

	/**
	 * @param includingModified the includingModified to set
	 */
	public void setIncludingModified(boolean includingModified) {
		this.includingModified = includingModified;
	}

	/**
	 * @return the includingImage
	 */
	public boolean isIncludingImage() {
		return includingImage;
	}

	/**
	 * @param includingImage the includingImage to set
	 */
	public void setIncludingImage(boolean includingImage) {
		this.includingImage = includingImage;
	}

	/**
	 * @return the includingLoggingConfiguration
	 */
	public boolean isIncludingLoggingConfiguration() {
		return includingLoggingConfiguration;
	}

	/**
	 * @param includingLoggingConfiguration the includingLoggingConfiguration to set
	 */
	public void setIncludingLoggingConfiguration(boolean includingLoggingConfiguration) {
		this.includingLoggingConfiguration = includingLoggingConfiguration;
	}

	/**
	 * @return the includingLastExecutionResult
	 */
	public boolean isIncludingLastExecutionResult() {
		return includingLastExecutionResult;
	}

	/**
	 * @param includingLastExecutionResult the includingLastExecutionResult to set
	 */
	public void setIncludingLastExecutionResult(boolean includingLastExecutionResult) {
		this.includingLastExecutionResult = includingLastExecutionResult;
	}

	/**
	 * @return the includingExtendedDescription
	 */
	public boolean isIncludingExtendedDescription() {
		return includingExtendedDescription;
	}

	/**
	 * @param includingExtendedDescription the includingExtendedDescription to set
	 */
	public void setIncludingExtendedDescription(boolean includingExtendedDescription) {
		this.includingExtendedDescription = includingExtendedDescription;
	}

	/**
	 * @return the includingName
	 */
	public boolean isIncludingName() {
		return includingName;
	}

	/**
	 * @param includingName the includingName to set
	 */
	public void setIncludingName(boolean includingName) {
		this.includingName = includingName;
	}

  /**
   * @return the fileTypeField
   */
  public String getFileTypeField() {
    return fileTypeField;
  }

  /**
   * @param fileTypeField the fileTypeField to set
   */
  public void setFileTypeField(String fileTypeField) {
    this.fileTypeField = fileTypeField;
  }	
}