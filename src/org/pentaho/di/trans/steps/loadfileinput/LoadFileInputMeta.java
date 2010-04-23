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

package org.pentaho.di.trans.steps.loadfileinput;

import java.util.List;
import java.util.Map;


import org.w3c.dom.Node;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



public class LoadFileInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = LoadFileInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") };
	public static final String[] RequiredFilesCode = new String[] {"N", "Y"};
	
	private static final String NO = "N";
	private static final String YES = "Y";
	
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
	private LoadFileInputField inputFields[];
	
    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;
    
    /**  Dynamic FilenameField    */
    private  String DynamicFilenameField;
    
    /**  Is In fields     */
    private  boolean IsInFields;
    
    /** Flag: add result filename **/
    private boolean addresultfile;
    
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];
	
	/** Flag : do we ignore empty file? */
	private boolean IsIgnoreEmptyFile;
	
	/** Array of boolean values as string, indicating if we need to fetch sub folders. */
	private  String  includeSubFolders[];
	
	
	public LoadFileInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public String[] getFileRequired() {
		return fileRequired;
	}
    
	public void setFileRequired(String[] fileRequired) {
		this.fileRequired = fileRequired;
	}

	
	/** 
	 * @return the add result filesname flag
	 */
	public boolean addResultFile()
	{
		return addresultfile;
	}

	/** 
	 * @return the IsIgnoreEmptyFile flag
	 */
	public boolean isIgnoreEmptyFile()
	{
		return IsIgnoreEmptyFile;
	}
	
	/** 
	 * @param the IsIgnoreEmptyFile to set
	 */
	public void setIgnoreEmptyFile(boolean IsIgnoreEmptyFile)
	{
		this.IsIgnoreEmptyFile= IsIgnoreEmptyFile;
	}
	public void setAddResultFile(boolean addresultfile)
	{
		this.addresultfile=addresultfile;
	}
	
	/**
     * @return Returns the input fields.
     */
    public LoadFileInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(LoadFileInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }
    
    /************************************
     * get and set  FilenameField
    *************************************/
    /**  */
    public String getDynamicFilenameField()
    {
        return DynamicFilenameField;
    }
    
    /**  */ 
    public void setDynamicFilenameField(String DynamicFilenameField)
    {
        this.DynamicFilenameField = DynamicFilenameField;
    }
    
    /************************************
     * get et set  IsInFields
    *************************************/
    /**  */
    public boolean getIsInFields()
    {
        return IsInFields;
    }
    
    /**  */ 
    public void setIsInFields(boolean IsInFields)
    {
        this.IsInFields = IsInFields;
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
     * @return the encoding
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		LoadFileInputMeta retval = (LoadFileInputMeta)super.clone();
		
		int nrFiles  = fileName.length;
		int nrFields = inputFields.length;

		retval.allocate(nrFiles, nrFields);
		
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (LoadFileInputField)inputFields[i].clone();
            }
		}		
		return retval;
	}

    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    "+XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    "+XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    "+XMLHandler.addTagValue("addresultfile",   addresultfile));
        retval.append("    "+XMLHandler.addTagValue("IsIgnoreEmptyFile",   IsIgnoreEmptyFile));
        
        retval.append("    "+XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    "+XMLHandler.addTagValue("encoding",        encoding));
        
        retval.append("    <file>"+Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      "+XMLHandler.addTagValue("filemask", fileMask[i]));
            retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
        	retval.append("      "+XMLHandler.addTagValue("include_subfolders", includeSubFolders[i]));
        }
        retval.append("      </file>"+Const.CR);
        
        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<inputFields.length;i++)
        {
            LoadFileInputField field = inputFields[i];
            retval.append(field.getXML());
        }
        retval.append("      </fields>"+Const.CR);
        

        retval.append("    "+XMLHandler.addTagValue("limit", rowLimit));
        
        retval.append("    "+XMLHandler.addTagValue("IsInFields", IsInFields));
        
        retval.append("    "+XMLHandler.addTagValue("DynamicFilenameField", DynamicFilenameField));

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			
			addresultfile  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addresultfile"));
			IsIgnoreEmptyFile  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "IsIgnoreEmptyFile"));
			
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			encoding          = XMLHandler.getTagValue(stepnode, "encoding");
	
			Node filenode  = XMLHandler.getSubNode(stepnode,   "file");
			Node fields    = XMLHandler.getSubNode(stepnode,   "fields");
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
				LoadFileInputField field = new LoadFileInputField(fnode);
				inputFields[i] = field;
			} 
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
			
			IsInFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "IsInFields"));

			DynamicFilenameField = XMLHandler.getTagValue(stepnode, "DynamicFilenameField");
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "LoadFileInputMeta.Exception.ErrorLoadingXML", e.toString()));
		}
	}
	
	public void allocate(int nrfiles, int nrfields)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		fileRequired = new String[nrfiles];
		includeSubFolders = new String[nrfiles];
		inputFields = new LoadFileInputField[nrfields];
        
	}
	
	public void setDefault()
	{
		encoding= "";
		IsIgnoreEmptyFile=false;
		includeFilename    = false;
		filenameField = "";
		includeRowNumber    = false;
		rowNumberField = "";
		addresultfile=true;
		
		int nrFiles=0;
		int nrFields=0;


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
		    inputFields[i] = new LoadFileInputField("field"+(i+1));
		}

		rowLimit=0;
		
		IsInFields    = false;
		DynamicFilenameField = null;
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		if(!getIsInFields()) r.clear();
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    LoadFileInputField field = inputFields[i];
		    int type=field.getType();
		    
			switch (field.getElementType())
			{
				case LoadFileInputField.ELEMENT_TYPE_FILECONTENT:
					if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_STRING;
					break;
				case LoadFileInputField.ELEMENT_TYPE_FILESIZE:
					if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_INTEGER;
					break;
				default:
					break;
			}
		    
			ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(field.getName()), type);
			v.setLength(field.getLength());
			v.setPrecision(field.getPrecision());
			v.setConversionMask(field.getFormat());
			v.setCurrencySymbol(field.getCurrencySymbol());
			v.setDecimalSymbol(field.getDecimalSymbol());
			v.setGroupingSymbol(field.getGroupSymbol());
			v.setTrimType(field.getTrimType());
			v.setOrigin(name);
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
		if (includeRowNumber)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			includeFilename   =      rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     =      rep.getStepAttributeString (id_step, "include_field");
			
			addresultfile  =      rep.getStepAttributeBoolean(id_step, "addresultfile");
			IsIgnoreEmptyFile  =      rep.getStepAttributeBoolean(id_step, "IsIgnoreEmptyFile");
			
			includeRowNumber  =      rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    =      rep.getStepAttributeString (id_step, "rownum_field");
			rowLimit          =      rep.getStepAttributeInteger(id_step, "limit");
			encoding          =      rep.getStepAttributeString (id_step, "encoding");
	
			int nrFiles     = rep.countNrStepAttributes(id_step, "file_name");
			int nrFields    = rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFiles, nrFields);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))    	fileRequired[i] = NO;
                includeSubFolders[i] = rep.getStepAttributeString(id_step, i, "include_subfolders");
                if(!YES.equalsIgnoreCase(includeSubFolders[i])) includeSubFolders[i] = NO;
			}

			for (int i=0;i<nrFields;i++)
			{
			    LoadFileInputField field = new LoadFileInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setElementType( LoadFileInputField.getElementTypeByCode( rep.getStepAttributeString (id_step, i, "element_type") ));
				field.setType( ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( LoadFileInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

                
				inputFields[i] = field;
			}
			IsInFields        =      rep.getStepAttributeBoolean (id_step, "IsInFields");
			
			DynamicFilenameField          =      rep.getStepAttributeString (id_step, "DynamicFilenameField");
            
  

		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LoadFileInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "addresultfile",   addresultfile);
			rep.saveStepAttribute(id_transformation, id_step, "IsIgnoreEmptyFile" ,   IsIgnoreEmptyFile);
			
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
            rep.saveStepAttribute(id_transformation, id_step, "encoding",        encoding);
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "include_subfolders", includeSubFolders[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    LoadFileInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "element_type",        field.getElementTypeCode());
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
			rep.saveStepAttribute(id_transformation, id_step, "IsInFields",       IsInFields);
			
            rep.saveStepAttribute(id_transformation, id_step, "DynamicFilenameField",        DynamicFilenameField);
			
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LoadFileInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	
  public FileInputList  getFiles(VariableSpace space)
  {
   	return FileInputList.createFileList(space, fileName, fileMask, fileRequired, includeSubFolderBoolean());
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
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...		
		if (input.length<=0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.NoInputExpected"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.NoInput"), stepinfo);
			remarks.add(cr);
		}	
		
		if(getIsInFields())
		{
			 if (Const.isEmpty(getDynamicFilenameField()))
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.NoField"), stepinfo);
				 remarks.add(cr); 
			 }
			 else
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.FieldOk"), stepinfo);
				 remarks.add(cr); 
			 }		 
		}
		else
		{
	        FileInputList fileInputList = getFiles(transMeta);

			if (fileInputList==null || fileInputList.getFiles().size()==0)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.NoFiles"), stepinfo);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LoadFileInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepinfo);
				remarks.add(cr);
			}	
		}	
		
		
	}


	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new LoadFileInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new LoadFileInputData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
