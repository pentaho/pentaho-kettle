/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.trans.steps.ldifinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

public class LDIFInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = LDIFInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
 	 
	/** Flag indicating that we should include the filename in the output */
	private  boolean includeFilename;
	
	/** The name of the field in the output containing the filename */
	private  String  filenameField;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	/** The maximum number or lines to read */
	private  long  rowLimit;

	/** The fields to import... */
	private LDIFInputField inputFields[];
	
	private boolean addtoresultfilename;
	
	private String multiValuedSeparator;
	
	private boolean includeContentType;
	
	private String  contentTypeField;
	
	/** file name from previous fields **/
	private boolean filefield;
	
	private String dynamicFilenameField;
	
	
	public LDIFInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
		
	/**
     * @return Returns the input fields.
     */
    public LDIFInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(LDIFInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }

    /**
     * @return Returns the fileMask.
     */
    public String[] getFileMask()
    {
        return fileMask;
    }
    
    /**
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
    }
    
    /**
     * @return Returns the fileName.
     */
    public String[] getFileName()
    {
        return fileName;
    }
    
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String[] fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @param filefield The filefield to set.
     */
    public void setFileField(boolean filefield)
    {
        this.filefield = filefield;
    }
    
    /**
     * @return Returns the File field.
     */
    public boolean isFileField()
    {
        return filefield;
    }
    /**
     * @return Returns the includeFilename.
     */
    public boolean includeFilename()
    {
        return includeFilename;
    }
    
    /**
     * @param includeFilename The includeFilename to set.
     */
    public void setIncludeFilename(boolean includeFilename)
    {
        this.includeFilename = includeFilename;
    }
    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }
    
    /**
     * @return Returns the includeContentType.
     */
    public boolean includeContentType()
    {
        return includeContentType;
    }
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeContentType(boolean includeContentType)
    {
        this.includeContentType = includeContentType;
    }
    
    /**
     * @param multiValuedSeparator The multi-valued separator filed.
     */
    public void setMultiValuedSeparator(String multiValuedSeparator)
    {
        this.multiValuedSeparator = multiValuedSeparator;
    }
    
    
    /**
     * @return Returns the multi valued separator.
     */
    public String getMultiValuedSeparator()
    {
        return multiValuedSeparator;
    }
    
    
    /**
     * @param addtoresultfilename The addtoresultfilename to set.
     */
    public void setAddToResultFilename(boolean addtoresultfilename)
    {
        this.addtoresultfilename = addtoresultfilename;
    }
    
    
    /**
     * @return Returns the addtoresultfilename.
     */
    public boolean AddToResultFilename()
    {
        return addtoresultfilename;
    }
    
    
    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }

    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    
    /**
     * @return Returns the filenameField.
     */
    public String getFilenameField()
    {
        return filenameField;
    } 
    
    /**
     * @return Returns the dynamic filename field (from previous steps)
     */
    public String getDynamicFilenameField()
    {
        return dynamicFilenameField;
    }   
    
    /**
     * @param dynamicFilenameField The dynamic filename field to set.
     */
    public void setDynamicFilenameField(String dynamicFilenameField)
    {
        this.dynamicFilenameField = dynamicFilenameField;
    }

    /**
     * @param filenameField The filenameField to set.
     */
    public void setFilenameField(String filenameField)
    {
        this.filenameField = filenameField;
    }
    
    
    /**
     * @return Returns the contentTypeField.
     */
    public String getContentTypeField()
    {
        return contentTypeField;
    }
    
    /**
     * @param contentTypeField The contentTypeField to set.
     */
    public void setContentTypeField(String contentTypeField)
    {
        this.contentTypeField = contentTypeField;
    }
        
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	    throws KettleXMLException
{
		readData(stepnode);
	}

	public Object clone()
	{
		LDIFInputMeta retval = (LDIFInputMeta)super.clone();
		
		int nrFiles  = fileName.length;
		int nrFields = inputFields.length;

		retval.allocate(nrFiles, nrFields);

        for (int i = 0; i < nrFiles; i++)
        {
            retval.fileName[i]     = fileName[i];
            retval.fileMask[i]     = fileMask[i];
        }
		
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (LDIFInputField)inputFields[i].clone();
            }
		}
		
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer();
        
        retval.append("    ").append(XMLHandler.addTagValue("filefield",       filefield));
        retval.append("    ").append(XMLHandler.addTagValue("dynamicFilenameField",  dynamicFilenameField));
        retval.append("    "+XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    "+XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    "+XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    "+XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    "+XMLHandler.addTagValue("contenttype",          includeContentType));
        retval.append("    "+XMLHandler.addTagValue("contenttype_field",    contentTypeField));
        retval.append("    "+XMLHandler.addTagValue("addtoresultfilename",      addtoresultfilename));
        retval.append("    "+XMLHandler.addTagValue("multiValuedSeparator",      multiValuedSeparator));
        
        retval.append("    <file>"+Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      "+XMLHandler.addTagValue("filemask", fileMask[i]));
        }
        retval.append("      </file>"+Const.CR);
        
        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<inputFields.length;i++)
        {
            LDIFInputField field = inputFields[i];
            retval.append(field.getXML());
        }
        retval.append("      </fields>"+Const.CR);
        retval.append("    "+XMLHandler.addTagValue("limit", rowLimit));

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			filefield  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filefield"));
			dynamicFilenameField    = XMLHandler.getTagValue(stepnode, "dynamicFilenameField");
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			includeContentType  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "contenttype"));
			contentTypeField    = XMLHandler.getTagValue(stepnode, "contenttype_field");
			addtoresultfilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addtoresultfilename"));
			multiValuedSeparator     = XMLHandler.getTagValue(stepnode, "multiValuedSeparator");
			
			Node filenode   = XMLHandler.getSubNode(stepnode,  "file");
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFiles     = XMLHandler.countNodes(filenode,  "name");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
	
			allocate(nrFiles, nrFields);
			
			for (int i=0;i<nrFiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
			}
			
			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				LDIFInputField field = new LDIFInputField(fnode);
				inputFields[i] = field;
			}
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrfiles, int nrfields)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		
		inputFields = new LDIFInputField[nrfields];        
	}
	
	public void setDefault()
	{
		filefield=false;
		dynamicFilenameField ="";
		includeFilename  = false;
		filenameField    = "";
		includeRowNumber = false;
		rowNumberField   = "";
		includeContentType = false;
		contentTypeField   = "";
		multiValuedSeparator=",";
		addtoresultfilename=false;
		
		int nrFiles  =0;
		int nrFields =0;

		allocate(nrFiles, nrFields);	
		
		for (int i=0;i<nrFiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
		}
		
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new LDIFInputField("field"+(i+1));
		}

		rowLimit=0;
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
			LDIFInputField field = inputFields[i];       
	        
			int type=field.getType();
			if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_STRING;
			ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(field.getName()), type);
			v.setLength(field.getLength(), field.getPrecision());
			v.setOrigin(name);
			r.addValueMeta(v);
	        
		}
		
		if (includeFilename)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(filenameField), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}

		if (includeRowNumber)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		if (includeContentType)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(contentTypeField), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
    throws KettleException
   {
		try
		{
			filefield  = rep.getStepAttributeBoolean(id_step, "filefield");
			dynamicFilenameField  = rep.getStepAttributeString(id_step, "dynamicFilenameField");
			includeFilename   = rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     = rep.getStepAttributeString (id_step, "include_field");
			addtoresultfilename   = rep.getStepAttributeBoolean(id_step, "addtoresultfilename");  
			multiValuedSeparator     = rep.getStepAttributeString (id_step, "multiValuedSeparator");
			
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			includeContentType  = rep.getStepAttributeBoolean(id_step, "contenttype");
			contentTypeField    = rep.getStepAttributeString (id_step, "contenttype_field");
			
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");
	
			int nrFiles       = rep.countNrStepAttributes(id_step, "file_name");
			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFiles, nrFields);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
			}

			for (int i=0;i<nrFields;i++)
			{
			    LDIFInputField field = new LDIFInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setAttribut( rep.getStepAttributeString (id_step, i, "field_attribut") );
				field.setType( ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( LDIFInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

				inputFields[i] = field;
			}
        }
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LDIFInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "filefield",          filefield);
			rep.saveStepAttribute(id_transformation, id_step, "dynamicFilenameField",    dynamicFilenameField);
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "contenttype",      includeContentType);
			rep.saveStepAttribute(id_transformation, id_step, "contenttype_field",    contentTypeField);
			
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			rep.saveStepAttribute(id_transformation, id_step, "addtoresultfilename",         addtoresultfilename);
			rep.saveStepAttribute(id_transformation, id_step, "multiValuedSeparator",           multiValuedSeparator);
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    LDIFInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_attribut",      field.getAttribut());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",          field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",        field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",      field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",       field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",         field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",        field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision",     field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type",     field.getTrimTypeCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",        field.isRepeated());
                
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LDIFInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	public FileInputList getFiles(VariableSpace space)
	{
        String required[] = new String[fileName.length];
        boolean subdirs[] = new boolean[fileName.length];
        for (int i=0;i<required.length;i++)
        {
            required[i]="N";
            subdirs[i]=false;
        }
        return FileInputList.createFileList(space,space.environmentSubstitute(fileName), space.environmentSubstitute(fileMask), required, subdirs);
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LDIFInputMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LDIFInputMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles(transMeta);
		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LDIFInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LDIFInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
	}

	public StepDataInterface getStepData()
	{
		return new LDIFInputData();
	}
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new LDIFInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
    public boolean supportsErrorHandling()
    {
        return true;
    } 
    
	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.

	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			//
			if (!filefield) {
				List<FileObject> newFilenames = new ArrayList<FileObject>();
				
				FileInputList fileList = getFiles(space);
				if (fileList.getFiles().size()>0) {
					for (FileObject fileObject : fileList.getFiles()) {
						// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
						// To   : /home/matt/test/files/foo/bar.csv
						//
						// If the file doesn't exist, forget about this effort too!
						//
						if (fileObject.exists()) {
							// Convert to an absolute path and add it to the list.
							// 
							newFilenames.add(fileObject);
						}
					}
					
					// Still here: set a new list of absolute filenames!
					//
					fileName=new String[newFilenames.size()];
					fileMask=new String[newFilenames.size()];
					// fileRequired=new String[newFilenames.size()];
					for (int i=0;i<newFilenames.size();i++) {
						FileObject fileObject = newFilenames.get(i);
						fileName[i] = resourceNamingInterface.nameResource(fileObject.getName().getBaseName(), fileObject.getParent().getName().getPath(), space.toString(), FileNamingType.DATA_FILE);
						fileMask[i] = null;
						// fileRequired[i] = "Y";
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
}