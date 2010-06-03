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

/*
 * Created on 24-03-2008
 *
 */
package org.pentaho.di.trans.steps.propertyinput;

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
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
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


public class PropertyInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = PropertyInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") };
	public static final String[] RequiredFilesCode = new String[] {"N", "Y"};
	
	public static final String DEFAULT_ENCODING="UTF-8";
	
	private String encoding;
	
	private String fileType;
	public static final String[] fileTypeDesc = new String[] {BaseMessages.getString(PKG, "PropertyInputMeta.FileType.Property"),BaseMessages.getString(PKG, "PropertyInputMeta.FileType.Ini")};
	public static final String[] fileTypeCode = new String[] {"property", "ini"};
	public static final int FILE_TYPE_PROPERTY = 0;
	public static final int FILE_TYPE_INI = 1;
	
	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
 	 
	/** Flag indicating that we should include the filename in the output */
	private  boolean includeFilename;
	
	
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];
	
	/** Array of boolean values as string, indicating if we need to fetch sub folders. */
	private  String  includeSubFolders[];
	
	/** Flag indicating that we should reset RowNum for each file */
	private boolean resetRowNumber;
	
	/** Flag do variable substitution for value */
	private boolean resolvevaluevariable;
	
	/** The name of the field in the output containing the filename */
	private  String  filenameField;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;

	
	/** The maximum number or lines to read */
	private  long  rowLimit;

	/** The fields to import... */
	private PropertyInputField inputFields[];
	
	/** file name from previous fields **/
	private boolean filefield;
	
	private boolean isaddresult;
	
	private String dynamicFilenameField;
	
	private static final String YES = "Y";
	
    public final static String type_trim_code[] = { "none", "left", "right", "both" };
    
    public final static String column_code[] = { "key", "value" };
    
	/** Flag indicating that a INI file section field should be included in the output */
	private  boolean includeIniSection;
	
	/** The name of the field in the output containing the INI file section*/
	private String iniSectionField;
	
	private String section;
    
	public PropertyInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
		
	/**
     * @return Returns the input fields.
     */
    public PropertyInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(PropertyInputField[] inputFields)
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
    public String[] getFileRequired() {
		return this.fileRequired;
	}

	public void setFileRequired(String[] fileRequiredin) {
		for (int i=0;i<fileRequiredin.length;i++)
		{
			this.fileRequired[i] = getRequiredFilesCode(fileRequiredin[i]);
		}
	}
	public String[] getIncludeSubFolders() {
		return includeSubFolders;
	}

	public void setIncludeSubFolders(String[] includeSubFoldersin) {
		for (int i=0;i<includeSubFoldersin.length;i++)
		{
			this.includeSubFolders[i] = getRequiredFilesCode(includeSubFoldersin[i]);
		}
	}
	 public String getRequiredFilesCode(String tt)
	    {
	   	if(tt==null) return RequiredFilesCode[0]; 
			if(tt.equals(RequiredFilesDesc[1]))
				return RequiredFilesCode[1];
			else
				return RequiredFilesCode[0]; 
	    }
	  public String getRequiredFilesDesc(String tt)
	  {
	 	if(tt==null) return RequiredFilesDesc[0]; 
			if(tt.equals(RequiredFilesCode[1]))
				return RequiredFilesDesc[1];
			else
				return RequiredFilesDesc[0]; 
	  }
    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String[] fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @return Returns the filenameField.
     */
    public String getFilenameField()
    {
        return filenameField;
    } 
    
    /**
     * @return Returns the dynamically defined filename field (to read from previous steps).
     */
    public String getDynamicFilenameField()
    {
        return dynamicFilenameField;
    }   
    
    /**
     * @param dynamicFilenameField the dynamically defined filename field (to read from previous steps)
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
    public static String getFileTypeCode(int i) {
		if (i < 0 || i >= fileTypeCode.length)
			return fileTypeCode[0];
		return fileTypeCode[i];
	}
	public static int getFileTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < fileTypeDesc.length; i++) {
			if (fileTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails, try to match using the code.
		return getFileTypeByCode(tt);
	}
	public static int getFileTypeByCode(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < fileTypeCode.length; i++) {
			if (fileTypeCode[i].equalsIgnoreCase(tt))
				return i;
		}
		return 0;
	}
	public static String getFileTypeDesc(int i) {
		if (i < 0 || i >= fileTypeDesc.length)
			return fileTypeDesc[0];
		return fileTypeDesc[i];
	}
	public void setFileType(String filetype)
	{
		this.fileType=filetype;
	}
	public String getFileType()
	{
		return fileType;
	}
    /**
     * @param includeIniSection The includeIniSection to set.
     */
    public void setIncludeIniSection(boolean includeIniSection)
    {
        this.includeIniSection = includeIniSection;
    }

    
    /**
     * @return Returns the includeIniSection.
     */
    public boolean includeIniSection()
    {
        return includeIniSection;
    }
    /**
     * @param encoding The encoding to set.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    /**
     *  @return Returns encoding.
     */
    public String getEncoding()
    {
        return encoding;
    }
    /**
     * @param iniSectionField The iniSectionField to set.
     */
    public void setINISectionField(String iniSectionField)
    {
        this.iniSectionField = iniSectionField;
    }
    
    /**
     * @return Returns the iniSectionField.
     */
    public String getINISectionField()
    {
        return iniSectionField;
    }
   
    /**
     * @param section The section to set.
     */
    public void setSection(String section)
    {
        this.section = section;
    }
    
    /**
     * @return Returns the section.
     */
    public String getSection()
    {
        return section;
    }
    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
    
    /**
     * @return Returns the File field.
     */
    public boolean isFileField()
    {
        return filefield;
    }
    /**
     * @param filefield The filefield to set.
     */
    public void setFileField(boolean filefield)
    {
        this.filefield = filefield;
    }
    
    /**
     * @return Returns the resetRowNumber.
     */
    public boolean resetRowNumber()
    {
        return resetRowNumber;
    }
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }
    
    /**
     * @param isaddresult The isaddresult to set.
     */
    public void setAddResultFile(boolean isaddresult)
    {
        this.isaddresult = isaddresult;
    }
    
    /**
     *  @return Returns isaddresult.
     */
    public boolean isAddResultFile()
    {
        return isaddresult;
    }
    
    /**
     * @param resetRowNumber The resetRowNumber to set.
     */
    public void setResetRowNumber(boolean resetRowNumber)
    {
        this.resetRowNumber = resetRowNumber;
    }
    
    /**
     * @param resolvevaluevariable The resolvevaluevariable to set.
     */
    public void setResolveValueVariable(boolean resolvevaluevariable)
    {
        this.resolvevaluevariable=resolvevaluevariable;
    }
    
    /**
     *  @return Returns resolvevaluevariable.
     */
    public boolean isResolveValueVariable()
    {
        return resolvevaluevariable;
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
    
      
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		PropertyInputMeta retval = (PropertyInputMeta)super.clone();
		int nrFiles  = fileName.length;
		int nrFields = inputFields.length;
		retval.allocate(nrFiles, nrFields);
		for (int i=0;i<nrFiles;i++)
		{
			retval.fileName[i]     = fileName[i];
			retval.fileMask[i]     = fileMask[i];
			retval.fileRequired[i] = fileRequired[i];
			retval.includeSubFolders[i] = includeSubFolders[i];
		}
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (PropertyInputField)inputFields[i].clone();
            }
		}
		
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        retval.append("    ").append(XMLHandler.addTagValue("file_type",         fileType));
        retval.append("    ").append(XMLHandler.addTagValue("encoding",         encoding));
        retval.append("    ").append(XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    ").append(XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    ").append(XMLHandler.addTagValue("filename_Field",  dynamicFilenameField));
        retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("isaddresult",     isaddresult));
        retval.append("    ").append(XMLHandler.addTagValue("filefield",       filefield));
        retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("resetrownumber",  resetRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("resolvevaluevariable",  resolvevaluevariable));
        retval.append("    ").append(XMLHandler.addTagValue("ini_section",          includeIniSection));
        retval.append("    ").append(XMLHandler.addTagValue("ini_section_field",    iniSectionField));
        retval.append("    ").append(XMLHandler.addTagValue("section",  section));
        retval.append("    <file>").append(Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
			retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubFolders[i]));
        }
        retval.append("    </file>").append(Const.CR);
        
         
        /*
		 * Describe the fields to read
		 */
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<inputFields.length;i++)
		{
			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("name",      inputFields[i].getName()) );
			retval.append("        ").append(XMLHandler.addTagValue("column",      inputFields[i].getColumnCode()));
			retval.append("        ").append(XMLHandler.addTagValue("type",      inputFields[i].getTypeDesc()) );
            retval.append("        ").append(XMLHandler.addTagValue("format", inputFields[i].getFormat()));
            retval.append("        ").append(XMLHandler.addTagValue("length",    inputFields[i].getLength()) );
            retval.append("        ").append(XMLHandler.addTagValue("precision", inputFields[i].getPrecision()));
            retval.append("        ").append(XMLHandler.addTagValue("currency", inputFields[i].getCurrencySymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("decimal", inputFields[i].getDecimalSymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("group", inputFields[i].getGroupSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue("trim_type", inputFields[i].getTrimTypeCode() ) );
			retval.append("        ").append(XMLHandler.addTagValue("repeat",    inputFields[i].isRepeated()) );
			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);
        retval.append("    ").append(XMLHandler.addTagValue("limit", rowLimit));

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			fileType     = XMLHandler.getTagValue(stepnode, "file_type");
			encoding     = XMLHandler.getTagValue(stepnode, "encoding");
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			
			String addresult=XMLHandler.getTagValue(stepnode, "isaddresult");
			if(Const.isEmpty(addresult))
				isaddresult=true;
			else
				isaddresult  = "Y".equalsIgnoreCase(addresult);
			section    = XMLHandler.getTagValue(stepnode, "section");
			iniSectionField    = XMLHandler.getTagValue(stepnode, "ini_section_field");
			includeIniSection  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "ini_section"));
			filefield  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filefield"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			dynamicFilenameField    = XMLHandler.getTagValue(stepnode, "filename_Field");
			resetRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "resetrownumber"));
			resolvevaluevariable  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "resolvevaluevariable"));
			Node filenode   = XMLHandler.getSubNode(stepnode,  "file");
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFiles     = XMLHandler.countNodes(filenode,  "name");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
	
			allocate(nrFiles, nrFields);
			
			for (int i=0;i<nrFiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				Node includeSubFoldersnode = XMLHandler.getSubNodeByNr(filenode, "include_subfolders", i);
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
				includeSubFolders[i] = XMLHandler.getNodeValue(includeSubFoldersnode);
			}
			
		
			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				inputFields[i] = new PropertyInputField();
				
				inputFields[i].setName( XMLHandler.getTagValue(fnode, "name") );
				inputFields[i].setColumn( getColumnByCode(XMLHandler.getTagValue(fnode, "column")) );
				inputFields[i].setType( ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
				inputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				inputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
				String srepeat      = XMLHandler.getTagValue(fnode, "repeat");
				inputFields[i].setTrimType( getTrimTypeByCode(XMLHandler.getTagValue(fnode, "trim_type")) );
				
				if (srepeat!=null) inputFields[i].setRepeated( YES.equalsIgnoreCase(srepeat) ); 
				else               inputFields[i].setRepeated( false );
				
				inputFields[i].setFormat(XMLHandler.getTagValue(fnode, "format"));
				inputFields[i].setCurrencySymbol(XMLHandler.getTagValue(fnode, "currency"));
				inputFields[i].setDecimalSymbol(XMLHandler.getTagValue(fnode, "decimal"));
				inputFields[i].setGroupSymbol(XMLHandler.getTagValue(fnode, "group"));

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
		fileRequired   = new String [nrfiles];
		includeSubFolders   = new String [nrfiles]; 
		inputFields = new PropertyInputField[nrfields];        
	}
	
	public void setDefault()
	{
		fileType=fileTypeCode[0];
		section="";
		encoding=DEFAULT_ENCODING;
		includeIniSection=false;
		iniSectionField="";
		resolvevaluevariable=false;
		isaddresult=true;
		filefield=false;
		includeFilename  = false;
		filenameField    = "";
		includeRowNumber = false;
		rowNumberField   = "";
		dynamicFilenameField ="";
		
		int nrFiles  =0;
		int nrFields =0;

		allocate(nrFiles, nrFields);	
		
		for (int i=0;i<nrFiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
			fileRequired[i] = RequiredFilesCode[0];
			includeSubFolders[i] = RequiredFilesCode[0];
		}
		
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new PropertyInputField("field"+(i+1));
		}

		rowLimit=0;
	}
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    PropertyInputField field = inputFields[i];       
	        
			int type=field.getType();
			if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_STRING;
			ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(field.getName()), type);
			v.setLength(field.getLength());
			v.setPrecision(field.getPrecision());
			v.setOrigin(name);
			v.setConversionMask(field.getFormat());
	        v.setDecimalSymbol(field.getDecimalSymbol());
	        v.setGroupingSymbol(field.getGroupSymbol());
	        v.setCurrencySymbol(field.getCurrencySymbol());
			r.addValueMeta(v);   
		}
		String realFilenameField=space.environmentSubstitute(filenameField);
		if (includeFilename && !Const.isEmpty(realFilenameField))
		{
			ValueMetaInterface v = new ValueMeta(realFilenameField, ValueMeta.TYPE_STRING);
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
			
		String realRowNumberField=space.environmentSubstitute(rowNumberField);
		if (includeRowNumber && !Const.isEmpty(realRowNumberField))
		{
			ValueMetaInterface v = new ValueMeta(realRowNumberField, ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		String realSectionField=space.environmentSubstitute(iniSectionField);
		if (includeIniSection && !Const.isEmpty(realSectionField))
		{
			ValueMetaInterface v = new ValueMeta(realSectionField,  ValueMeta.TYPE_STRING);
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}
	
	  public final static int getTrimTypeByCode(String tt)
		{
			if (tt!=null) 
			{		
			    for (int i=0;i<type_trim_code.length;i++)
			    {
				    if (type_trim_code[i].equalsIgnoreCase(tt)) return i;
			    }
			}
			return 0;
		}
	  public final static int getColumnByCode(String tt)
		{
			if (tt!=null) 
			{		
			    for (int i=0;i<column_code.length;i++)
			    {
				    if (column_code[i].equalsIgnoreCase(tt)) return i;
			    }
			}
			return 0;
		}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			fileType     = rep.getStepAttributeString (id_step, "file_type");
			section     = rep.getStepAttributeString (id_step, "section");
			encoding     = rep.getStepAttributeString (id_step, "encoding");
			includeIniSection  = rep.getStepAttributeBoolean(id_step, "ini_section");
			iniSectionField    = rep.getStepAttributeString (id_step, "ini_section_field");
			includeFilename   = rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     = rep.getStepAttributeString (id_step, "include_field");
			dynamicFilenameField  = rep.getStepAttributeString(id_step, "filename_Field");
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			
			String addresult  = rep.getStepAttributeString(id_step, "isaddresult");
			if(Const.isEmpty(addresult))
				isaddresult=true;
			else	
				isaddresult  = rep.getStepAttributeBoolean(id_step, "isaddresult");
			
			filefield  = rep.getStepAttributeBoolean(id_step, "filefield");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			resetRowNumber     = rep.getStepAttributeBoolean (id_step, "reset_rownumber");
			resolvevaluevariable= rep.getStepAttributeBoolean (id_step, "resolve_value_variable");
				
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");
			int nrFiles       = rep.countNrStepAttributes(id_step, "file_name");
			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFiles, nrFields);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = RequiredFilesCode[0];
                includeSubFolders[i] = rep.getStepAttributeString(id_step, i, "include_subfolders");
                if(!YES.equalsIgnoreCase(includeSubFolders[i]))
                	includeSubFolders[i] = RequiredFilesCode[0];
			}

			for (int i=0;i<nrFields;i++)
			{
			    PropertyInputField field = new PropertyInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setColumn(PropertyInputField.getColumnByCode(rep.getStepAttributeString (id_step, i, "field_column") ) );
				field.setType(ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( PropertyInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

				inputFields[i] = field;
			}
        }
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "PropertyInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_type",   fileType);
			rep.saveStepAttribute(id_transformation, id_step, "section",   section);
			rep.saveStepAttribute(id_transformation, id_step, "encoding",   encoding);
			rep.saveStepAttribute(id_transformation, id_step, "ini_section",          includeIniSection);
			rep.saveStepAttribute(id_transformation, id_step, "ini_section_field",    iniSectionField);
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "isaddresult",     isaddresult);
			rep.saveStepAttribute(id_transformation, id_step, "filefield",          filefield);
			rep.saveStepAttribute(id_transformation, id_step, "filename_Field",    dynamicFilenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			rep.saveStepAttribute(id_transformation, id_step, "reset_rownumber",  resetRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "resolve_value_variable",  resolvevaluevariable);
		
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "include_subfolders", includeSubFolders[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    PropertyInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_column",        field.getColumnCode());
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
			throw new KettleException(BaseMessages.getString(PKG, "PropertyInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	public FileInputList  getFiles(VariableSpace space)
	{
        
        
        String required[] = new String[fileName.length];
        boolean subdirs[] = new boolean[fileName.length]; // boolean arrays are defaulted to false.
        for (int i=0;i<required.length; required[i]="Y", i++); //$NON-NLS-1$
        return FileInputList.createFileList(space, fileName, fileMask, required, subdirs);
        
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
	
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "PropertyInputMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "PropertyInputMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles(transMeta);

		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "PropertyInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "PropertyInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
		
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new PropertyInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new PropertyInputData();
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
			// In case the name of the file comes from previous steps, forget about this!
			//
			if (!filefield) {
	            for (int i=0;i<fileName.length;i++) {
	              FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(fileName[i]), space);
	              String prefix;
	              String path;
	              if (Const.isEmpty(fileMask[i])) {
	                prefix = fileObject.getName().getBaseName(); 
	                path = fileObject.getParent().getName().getPath();
	              } else {
	                prefix = "";
	                path = fileObject.getName().getPath();
	              }
	              
	              fileName[i] = resourceNamingInterface.nameResource(
	                  prefix, path, space.toString(), FileNamingType.DATA_FILE
	                );
	            }
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}

}