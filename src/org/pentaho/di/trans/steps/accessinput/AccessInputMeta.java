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

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.accessinput;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


public class AccessInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = AccessInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") };
	public static final String[] RequiredFilesCode = new String[] {"N", "Y"};
	
	
	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
	
	
	/** Wildcard or filemask to exclude (regular expression) */
	private String             excludeFileMask[];
	
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];
	
	/** Array of boolean values as string, indicating if we need to fetch sub folders. */
	private  String  includeSubFolders[];
 	 
	/** Flag indicating that we should include the filename in the output */
	private  boolean includeFilename;
	
	/** Flag indicating that we should include the tablename in the output */
	private  boolean includeTablename;
	
	/** Flag indicating that we should reset RowNum for each file */
	private boolean resetRowNumber;
	
	/** The name of the field in the output containing the table name*/
	private  String  tablenameField;
	
	/** The name of the field in the output containing the filename */
	private  String  filenameField;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	/** The name of the table of the database*/
	private  String  TableName;
	
	/** The maximum number or lines to read */
	private  long  rowLimit;

	/** The fields to import... */
	private AccessInputField inputFields[];
	
	/** file name from previous fields **/
	private boolean filefield;
	
	private boolean isaddresult;
	
	private String dynamicFilenameField;
	
	private static final String YES = "Y";
	
    public final static String type_trim_code[] = { "none", "left", "right", "both" };
    
    /** Prefix that flags system tables */
    public static final String PREFIX_SYSTEM = "MSys";
    
    /** Additional fields  **/
    private String shortFileFieldName;
    private String pathFieldName;
    private String hiddenFieldName;
    private String lastModificationTimeFieldName;
    private String uriNameFieldName;
    private String rootUriNameFieldName;
    private String extensionFieldName;
    private String sizeFieldName;


    
	public AccessInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	/**
	 * @return Returns the extensionFieldName.
	 */
    public String getExtensionField()
    {
    	return extensionFieldName;
    }
    /**
	 * @param field The extensionFieldName to set.
	 */
    public void setExtensionField(String field)
    {
    	extensionFieldName=field;
    }
    /**
	 * @return Returns the sizeFieldName.
	 */
    public String getSizeField()
    {
    	return sizeFieldName;
    }
    /**
	 * @param field The sizeFieldName to set.
	 */
    public void setSizeField(String field)
    {
    	sizeFieldName=field;
    }
	/**
	 * @return Returns the shortFileFieldName.
	 */
    public String getShortFileNameField()
    {
    	return shortFileFieldName;
    }
    /**
	 * @param field The shortFileFieldName to set.
	 */
    public void setShortFileNameField(String field)
    {
    	shortFileFieldName=field;
    }
	
	/**
	 * @return Returns the pathFieldName.
	 */
    public String getPathField()
    {
    	return pathFieldName;
    }
    /**
	 * @param field The pathFieldName to set.
	 */
    public void setPathField(String field)
    {
    	this.pathFieldName=field;
    }
	/**
	 * @return Returns the hiddenFieldName.
	 */
    public String isHiddenField()
    {
    	return hiddenFieldName;
    }
    /**
	 * @param field The hiddenFieldName to set.
	 */
    public void setIsHiddenField(String field)
    {
    	hiddenFieldName=field;
    }
	/**
	 * @return Returns the lastModificationTimeFieldName.
	 */
    public String getLastModificationDateField()
    {
    	return lastModificationTimeFieldName;
    }
    /**
	 * @param field The lastModificationTimeFieldName to set.
	 */
    public void setLastModificationDateField(String field)
    {
    	lastModificationTimeFieldName=field;
    }
    /**
	 * @return Returns the uriNameFieldName.
	 */
    public String getUriField()
    {
    	return uriNameFieldName;
    }
    /**
	 * @param field The uriNameFieldName to set.
	 */
    public void setUriField(String field)
    {
    	uriNameFieldName=field;
    }
    /**
	 * @return Returns the uriNameFieldName.
	 */
    public String getRootUriField()
    {
    	return rootUriNameFieldName;
    }
    /**
	 * @param field The rootUriNameFieldName to set.
	 */
    public void setRootUriField(String field)
    {
    	rootUriNameFieldName=field;
    }
		
		
	/**
     * @return Returns the input fields.
     */
    public AccessInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(AccessInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }

	/**
	 * @return Returns the excludeFileMask.
	 */
	public String[] getExludeFileMask()
	{
		return excludeFileMask;
	}
	/**
	 * @param excludeFileMask The excludeFileMask to set.
	 */
	public void setExcludeFileMask(String[] excludeFileMask)
	{
		this.excludeFileMask = excludeFileMask;
	}
    
    
    /**
     * @return Returns the fileMask.
     */
    public String[] getFileMask()
    {
        return fileMask;
    }
    public String[] getFileRequired() {
		return fileRequired;
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
     * @param fileMask The fileMask to set.
     */
    public void setFileMask(String[] fileMask)
    {
        this.fileMask = fileMask;
    }

	public void setFileRequired(String[] fileRequiredin) {
		for (int i=0;i<fileRequiredin.length;i++)
		{
			this.fileRequired[i] = getRequiredFilesCode(fileRequiredin[i]);
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
	public String[] getIncludeSubFolders() {
		return includeSubFolders;
	}
	public void setIncludeSubFolders(String[] includeSubFoldersin) {
		for (int i=0;i<includeSubFoldersin.length;i++)
		{
			this.includeSubFolders[i] = getRequiredFilesCode(includeSubFoldersin[i]);
		}
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
     * @return Returns the includeFilename.
     */
    public boolean includeFilename()
    {
        return includeFilename;
    }
    
    /**
     * @return Returns the includeTablename.
     */
    public boolean includeTablename()
    {
        return includeTablename;
    }
    
    /**
     * @param includeFilename The includeFilename to set.
     */
    public void setIncludeFilename(boolean includeFilename)
    {
        this.includeFilename = includeFilename;
    }
    
    /**
     * @param includeTablename The includeTablename to set.
     */
    public void setIncludeTablename(boolean includeTablename)
    {
        this.includeTablename = includeTablename;
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
     * @return Returns the tablenameField.
     */
    public String gettablenameField()
    {
        return tablenameField;
    }
    

    /**
     * @return Returns the TableName.
     */
    public String getTableName()
    {
        return TableName;
    } 
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    
    /**
     * @param tablenameField The tablenameField to set.
     */
    public void setTablenameField(String tablenameField)
    {
        this.tablenameField = tablenameField;
    }
    
    /**
     * @param TableName The table name to set.
     */
    public void setTableName(String TableName)
    {
        this.TableName = TableName;
    }
      
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		AccessInputMeta retval = (AccessInputMeta)super.clone();
		int nrFiles  = fileName.length;
		int nrFields = inputFields.length;
		retval.allocate(nrFiles, nrFields);
		for (int i=0;i<nrFiles;i++)
		{
			retval.fileName[i]     = fileName[i];
			retval.fileMask[i]     = fileMask[i];
            retval.excludeFileMask[i] = excludeFileMask[i];
			retval.fileRequired[i] = fileRequired[i];
			retval.includeSubFolders[i] = includeSubFolders[i];
		}
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (AccessInputField)inputFields[i].clone();
            }
		}
		
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        retval.append("    ").append(XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    ").append(XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    ").append(XMLHandler.addTagValue("tablename",       includeTablename));
        retval.append("    ").append(XMLHandler.addTagValue("filename_Field",  dynamicFilenameField));
        retval.append("    ").append(XMLHandler.addTagValue("tablename_field", tablenameField));
        retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("isaddresult",     isaddresult));
        retval.append("    ").append(XMLHandler.addTagValue("filefield",       filefield));
        retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("resetrownumber",  resetRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("table_name",      TableName));
        retval.append("    <file>").append(Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      ").append(XMLHandler.addTagValue("exclude_filemask", excludeFileMask[i]));
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
			retval.append("        ").append(XMLHandler.addTagValue("attribut",      inputFields[i].getColumn()));
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
		retval.append("    ").append(XMLHandler.addTagValue("shortFileFieldName", shortFileFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("pathFieldName", pathFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("hiddenFieldName", hiddenFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("lastModificationTimeFieldName", lastModificationTimeFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("uriNameFieldName", uriNameFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("rootUriNameFieldName", rootUriNameFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("extensionFieldName", extensionFieldName));
		retval.append("    ").append(XMLHandler.addTagValue("sizeFieldName", sizeFieldName));
        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			includeTablename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "tablename"));
			tablenameField    = XMLHandler.getTagValue(stepnode, "tablename_field");
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			
			String addresult=XMLHandler.getTagValue(stepnode, "isaddresult");
			if(Const.isEmpty(addresult))
				isaddresult=true;
			else
				isaddresult  = "Y".equalsIgnoreCase(addresult);
			
			filefield  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filefield"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			TableName    = XMLHandler.getTagValue(stepnode, "table_name");
			dynamicFilenameField    = XMLHandler.getTagValue(stepnode, "filename_Field");
			resetRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "resetrownumber"));
	
			Node filenode   = XMLHandler.getSubNode(stepnode,  "file");
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFiles     = XMLHandler.countNodes(filenode,  "name");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
	
			allocate(nrFiles, nrFields);
			
			for (int i=0;i<nrFiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				Node excludefilemasknode     = XMLHandler.getSubNodeByNr(filenode, "exclude_filemask", i);
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				Node includeSubFoldersnode = XMLHandler.getSubNodeByNr(filenode, "include_subfolders", i);
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
				excludeFileMask[i]    = XMLHandler.getNodeValue(excludefilemasknode);
				fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
				includeSubFolders[i] = XMLHandler.getNodeValue(includeSubFoldersnode);
			}
			
		
			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				inputFields[i] = new AccessInputField();
				
				inputFields[i].setName( XMLHandler.getTagValue(fnode, "name") );
				inputFields[i].setColumn(XMLHandler.getTagValue(fnode, "attribut") );
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
			shortFileFieldName = XMLHandler.getTagValue(stepnode, "shortFileFieldName");
			pathFieldName = XMLHandler.getTagValue(stepnode, "pathFieldName");
			hiddenFieldName = XMLHandler.getTagValue(stepnode, "hiddenFieldName");
			lastModificationTimeFieldName = XMLHandler.getTagValue(stepnode, "lastModificationTimeFieldName");
			uriNameFieldName = XMLHandler.getTagValue(stepnode, "uriNameFieldName");
			rootUriNameFieldName = XMLHandler.getTagValue(stepnode, "rootUriNameFieldName");
			extensionFieldName = XMLHandler.getTagValue(stepnode, "extensionFieldName");
			sizeFieldName = XMLHandler.getTagValue(stepnode, "sizeFieldName");
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
		excludeFileMask = new String[nrfiles];
		fileRequired = new String[nrfiles];
		includeSubFolders = new String[nrfiles];   
		inputFields = new AccessInputField[nrfields];        
	}
	
	public void setDefault()
	{
		shortFileFieldName=null;
	    pathFieldName=null;
	    hiddenFieldName=null;
	    lastModificationTimeFieldName=null;
	    uriNameFieldName=null;
	    rootUriNameFieldName=null;
	    extensionFieldName=null;
	    sizeFieldName=null;
	    
		isaddresult=true;
		filefield=false;
		includeFilename  = false;
		filenameField    = "";
		includeTablename = false;
		tablenameField   = "";
		includeRowNumber = false;
		rowNumberField   = "";
		TableName   = "";
		dynamicFilenameField ="";
		
		int nrFiles  =0;
		int nrFields =0;

		allocate(nrFiles, nrFields);	
		
		for (int i=0;i<nrFiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
			excludeFileMask[i] = "";
			fileRequired[i] = RequiredFilesCode[0];
			includeSubFolders[i] = RequiredFilesCode[0];
		}
		
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new AccessInputField("field"+(i+1));
		}

		rowLimit=0;
	}
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    AccessInputField field = inputFields[i];       
	        
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
		
		if (includeFilename)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(filenameField), ValueMeta.TYPE_STRING);
            v.setLength(250);
            v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		
		if (includeTablename)
		{
			
		    ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(tablenameField), ValueMeta.TYPE_STRING);
            v.setLength(250);
            v.setPrecision(-1);
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
		// Add additional fields

		if(getShortFileNameField()!=null && getShortFileNameField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getShortFileNameField()), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		if(getExtensionField()!=null && getExtensionField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getExtensionField()), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		if(getPathField()!=null && getPathField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getPathField()), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		if(getSizeField()!=null && getSizeField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getSizeField()), ValueMeta.TYPE_INTEGER);
			v.setOrigin(name);
			v.setLength(9);
			r.addValueMeta(v);
		}
		if(isHiddenField()!=null && isHiddenField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(isHiddenField()), ValueMeta.TYPE_BOOLEAN);
			v.setOrigin(name);
			r.addValueMeta(v);
		}

		if(getLastModificationDateField()!=null && getLastModificationDateField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getLastModificationDateField()), ValueMeta.TYPE_DATE);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		if(getUriField()!=null && getUriField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getUriField()), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}

		if(getRootUriField()!=null && getRootUriField().length()>0)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getRootUriField()), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
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
	
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	
		try
		{
			includeFilename   = rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     = rep.getStepAttributeString (id_step, "include_field");
			TableName          = rep.getStepAttributeString(id_step, "table_name");
			includeTablename  = rep.getStepAttributeBoolean(id_step, "tablename");
			dynamicFilenameField  = rep.getStepAttributeString(id_step, "filename_Field");
			tablenameField    = rep.getStepAttributeString (id_step, "tablename_field");
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			
			String addresult  = rep.getStepAttributeString(id_step, "isaddresult");
			if(Const.isEmpty(addresult))
				isaddresult=true;
			else	
				isaddresult  = rep.getStepAttributeBoolean(id_step, "isaddresult");
			
			filefield  = rep.getStepAttributeBoolean(id_step, "filefield");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			resetRowNumber     = rep.getStepAttributeBoolean (id_step, "reset_rownumber");
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");
			int nrFiles       = rep.countNrStepAttributes(id_step, "file_name");
			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFiles, nrFields);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				excludeFileMask[i] = rep.getStepAttributeString(id_step, i, "exclude_file_mask");
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))
                	fileRequired[i] = RequiredFilesCode[0];
                includeSubFolders[i] = rep.getStepAttributeString(id_step, i, "include_subfolders");
                if(!YES.equalsIgnoreCase(includeSubFolders[i]))
                	includeSubFolders[i] = RequiredFilesCode[0];
			}

			for (int i=0;i<nrFields;i++)
			{
			    AccessInputField field = new AccessInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setColumn( rep.getStepAttributeString (id_step, i, "field_attribut") );
				field.setType(ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( AccessInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

				inputFields[i] = field;
			}
			shortFileFieldName = rep.getStepAttributeString(id_step, "shortFileFieldName");
			pathFieldName = rep.getStepAttributeString(id_step, "pathFieldName");
			hiddenFieldName = rep.getStepAttributeString(id_step, "hiddenFieldName");
			lastModificationTimeFieldName = rep.getStepAttributeString(id_step, "lastModificationTimeFieldName");
			rootUriNameFieldName = rep.getStepAttributeString(id_step, "rootUriNameFieldName");
			extensionFieldName = rep.getStepAttributeString(id_step, "extensionFieldName");
			sizeFieldName = rep.getStepAttributeString(id_step, "sizeFieldName");
        }
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AccessInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "tablename",          includeTablename);
			rep.saveStepAttribute(id_transformation, id_step, "tablename_field",    tablenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "isaddresult",     isaddresult);
			rep.saveStepAttribute(id_transformation, id_step, "filefield",          filefield);
			rep.saveStepAttribute(id_transformation, id_step, "filename_Field",    dynamicFilenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			rep.saveStepAttribute(id_transformation, id_step, "table_name",      TableName);
			rep.saveStepAttribute(id_transformation, id_step, "reset_rownumber",  resetRowNumber);

		
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "exclude_file_mask", excludeFileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "include_subfolders", includeSubFolders[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    AccessInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_attribut",       field.getColumn());
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
			rep.saveStepAttribute(id_transformation, id_step, "shortFileFieldName", shortFileFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "pathFieldName", pathFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "hiddenFieldName", hiddenFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "lastModificationTimeFieldName", lastModificationTimeFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "uriNameFieldName", uriNameFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "rootUriNameFieldName", rootUriNameFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "extensionFieldName", extensionFieldName);
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AccessInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	public FileInputList  getFiles(VariableSpace space)
	{
        return FileInputList.createFileList(space, fileName, fileMask, excludeFileMask, fileRequired, includeSubFolderBoolean());   
	}
	private boolean[] includeSubFolderBoolean()
    {
    	int len=fileName.length;
		boolean includeSubFolderBoolean[]= new boolean[len];
		for(int i=0; i<len; i++)
		{
			includeSubFolderBoolean[i]=YES.equalsIgnoreCase(includeSubFolders[i]);
		}
		return includeSubFolderBoolean;
    }
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
	
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles(transMeta);
		// String files[] = getFiles();
		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
		
		// Check table
		if (Const.isEmpty(getTableName()))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AccessInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
	}
	
    public static String getFilename(FileObject fileObject)
    {
        FileName fileName = fileObject.getName();
        String root = fileName.getRootURI();
        if(!root.startsWith("file:")) return fileName.getURI();
        if(root.endsWith(":/"))
            root = root.substring(8, 10);
        else
            root = root.substring(7, root.length() - 1);
        String fileString = root + fileName.getPath();
        if(!"/".equals(Const.FILE_SEPARATOR)) fileString = Const.replace(fileString, "/", Const.FILE_SEPARATOR);
        return fileString;
    }
    
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new AccessInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new AccessInputData();
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
            // Replace the filename ONLY (folder or filename)
            // 
            for (int i=0;i<fileName.length;i++) {
              FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(fileName[i]), space);
              fileName[i] = resourceNamingInterface.nameResource(fileObject, space, Const.isEmpty(fileMask[i]));
            }
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
}