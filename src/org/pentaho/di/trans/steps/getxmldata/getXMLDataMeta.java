/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.trans.steps.getxmldata;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.fileinput.FileInputList;

/**
 * Store run-time data on the XML xpath step.
 */
public class getXMLDataMeta extends BaseStepMeta implements StepMetaInterface
{	
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

    /** The number or lines to skip before starting to read*/
    private  String  loopxpath;

	/** The fields to import... */
	private getXMLDataField inputFields[];
	
    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;
    
    /**  Is In fields     */
    private  String XmlField;
    
    /**  Is In fields     */
    private  boolean IsInFields;
    
    /**  Is a File     */
    private  boolean IsAFile;
    
    /** Flag: add result filename **/
    private boolean addresultfile;

    /** Flag: set Namespace aware **/
    private boolean namespaceaware;
    
    /** Flag: set XML Validating **/
    private boolean validating;
    
	/** Array of boolean values as string, indicating if a file is required. */
	private  String  fileRequired[];
	
	public getXMLDataMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/** 
	 * @return the add result filesname flag
	 */
	public boolean addResultFile()
	{
		return addresultfile;
	}
	
	/** 
	 * @return the namespaceware flag
	 */
	public boolean isNamespaceAware()
	{
		return namespaceaware;
	}
	
	/** 
	 * @return the validating flag
	 */
	public boolean isValidating()
	{
		return validating;
	}
	
	/** 
	 * @param the validating to set
	 */
	void setValidating(boolean validating)
	{
		this.validating= validating;
	}
	
	/** 
	 * @param the namespaceaware to set
	 */
	void setNamespaceAwre(boolean namespaceaware)
	{
		this.namespaceaware= namespaceaware;
	}
	
	public void setAddResultFile(boolean addresultfile)
	{
		this.addresultfile=addresultfile;
	}
	
	/**
     * @return Returns the input fields.
     */
    public getXMLDataField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(getXMLDataField[] inputFields)
    {
        this.inputFields = inputFields;
    }
    
    /**
     * Get XML field.
     */
    public String getXMLField()
    {
        return XmlField;
    }
    
    /**
     * Set XML field.
     */ 
    public void setXMLField(String XmlField)
    {
        this.XmlField = XmlField;
    }
    
    /**  
     * Get the IsInFields.
     */
    public boolean getIsInFields()
    {
        return IsInFields;
    }
    
    /**  
     * Set the IsInFields.
     */
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
    
	public String[] getFileRequired() {
		return fileRequired;
	}
    
	public void setFileRequired(String[] fileRequired) {
		this.fileRequired = fileRequired;
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
     * @return Returns the LoopXPath
     */
    public String getLoopXPath()
    {
        return loopxpath;
    }

    /**
     * @param loopxpath The loopxpath to set.
     */
    public void setLoopXPath(String loopxpath)
    {
        this.loopxpath = loopxpath;
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
    
    public boolean getIsAFile()
    {
    	return IsAFile;
    }    
    
    public void setIsAFile(boolean IsAFile)
    {
    	this.IsAFile=IsAFile;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
  	    throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		getXMLDataMeta retval = (getXMLDataMeta)super.clone();
		
		int nrFiles  = fileName.length;
		int nrFields = inputFields.length;

		retval.allocate(nrFiles, nrFields);
		
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (getXMLDataField)inputFields[i].clone();
            }
		}		
		return retval;
	}

    public String getXML()
    {
        StringBuffer retval=new StringBuffer(400);
        
        retval.append("    ").append(XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    ").append(XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("addresultfile",   addresultfile));
        retval.append("    ").append(XMLHandler.addTagValue("namespaceaware",  namespaceaware));
        retval.append("    ").append(XMLHandler.addTagValue("validating",      validating));
        
        retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("encoding",        encoding));
        
        retval.append("    <file>").append(Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
            retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
        }
        retval.append("    </file>").append(Const.CR);
        
        retval.append("    <fields>").append(Const.CR);
        for (int i=0;i<inputFields.length;i++)
        {
            getXMLDataField field = inputFields[i];
            retval.append(field.getXML());
        }
        retval.append("    </fields>").append(Const.CR);
        
        retval.append("    ").append(XMLHandler.addTagValue("limit", rowLimit));
        retval.append("    ").append(XMLHandler.addTagValue("loopxpath", loopxpath));
        
        retval.append("    ").append(XMLHandler.addTagValue("IsInFields", IsInFields));
        retval.append("    ").append(XMLHandler.addTagValue("IsAFile", IsAFile));
        
        retval.append("    ").append(XMLHandler.addTagValue("XmlField", XmlField));

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			
			addresultfile     = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addresultfile"));
			namespaceaware    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "namespaceaware"));
			validating        = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "validating"));
			
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			encoding          = XMLHandler.getTagValue(stepnode, "encoding");
	
			Node filenode     = XMLHandler.getSubNode(stepnode,   "file");
			Node fields       = XMLHandler.getSubNode(stepnode,   "fields");
			int nrFiles       = XMLHandler.countNodes(filenode,  "name");
			int nrFields      = XMLHandler.countNodes(fields,    "field");
	
			allocate(nrFiles, nrFields);
			
			for (int i=0;i<nrFiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i] = XMLHandler.getNodeValue(fileRequirednode);
			}
			
			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				getXMLDataField field = new getXMLDataField(fnode);
				inputFields[i] = field;
			} 
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
            // Do we skip rows before starting to read
			loopxpath = XMLHandler.getTagValue(stepnode, "loopxpath");
			
			IsInFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "IsInFields"));
			IsAFile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "IsAFile"));
			
			XmlField = XMLHandler.getTagValue(stepnode, "XmlField");
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("getXMLDataMeta.Exception.ErrorLoadingXML", e.toString()));
		}
	}
	
	public void allocate(int nrfiles, int nrfields)
	{
		fileName    = new String [nrfiles];
		fileMask    = new String [nrfiles];
		fileRequired = new String[nrfiles];
		inputFields = new getXMLDataField[nrfields];       
	}
	
	public void setDefault()
	{
		includeFilename = false;
		filenameField = "";
		includeRowNumber = false;
		rowNumberField = "";
		IsAFile = false;
		addresultfile = true;
		namespaceaware = false;
		validating = false;
		
		int nrFiles=0;
		int nrFields=0;
		loopxpath= "";

		allocate(nrFiles, nrFields);	
		
		for (int i=0;i<nrFiles;i++) 
		{
			fileName[i] = "filename"+(i+1);
			fileMask[i] = "";
			fileRequired[i] = NO;
		}
		
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new getXMLDataField("field"+(i+1));
		}

		rowLimit = 0;
		
		IsInFields = false;
		XmlField = "";
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
			getXMLDataField field = inputFields[i];       
	        
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
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	    throws KettleException
	{
	
		try
		{
			includeFilename   =   rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     =   rep.getStepAttributeString (id_step, "include_field");
			
			addresultfile     =   rep.getStepAttributeBoolean(id_step, "addresultfile");
			namespaceaware    =   rep.getStepAttributeBoolean(id_step, "namespaceaware");
			validating        =   rep.getStepAttributeBoolean(id_step, "validating");
			
			includeRowNumber  =   rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    =   rep.getStepAttributeString (id_step, "rownum_field");
			rowLimit          =   rep.getStepAttributeInteger(id_step, "limit");
			loopxpath      	  =   rep.getStepAttributeString(id_step, "loopxpath");
			encoding          =   rep.getStepAttributeString (id_step, "encoding");
	
			int nrFiles       =   rep.countNrStepAttributes(id_step, "file_name");
			int nrFields      =   rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFiles, nrFields);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
                if(!YES.equalsIgnoreCase(fileRequired[i]))    	fileRequired[i] = NO;
			}

			for (int i=0;i<nrFields;i++)
			{
			    getXMLDataField field = new getXMLDataField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setXPath( rep.getStepAttributeString (id_step, i, "field_xpath") );
				field.setElementType( getXMLDataField.getElementTypeByCode( rep.getStepAttributeString (id_step, i, "element_type") ));
				field.setType( ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( getXMLDataField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );
                
				inputFields[i] = field;
			}
			IsInFields = rep.getStepAttributeBoolean (id_step, "IsInFields");
			IsAFile    = rep.getStepAttributeBoolean (id_step, "IsAFile");
			
			XmlField   = rep.getStepAttributeString (id_step, "XmlField");
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("getXMLDataMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "addresultfile",   addresultfile);
			rep.saveStepAttribute(id_transformation, id_step, "namespaceaware",   namespaceaware);
			rep.saveStepAttribute(id_transformation, id_step, "validating",   validating);
			
			
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
            rep.saveStepAttribute(id_transformation, id_step, "loopxpath",       loopxpath);
            rep.saveStepAttribute(id_transformation, id_step, "encoding",        encoding);
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    getXMLDataField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_xpath",         field.getXPath());
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
			rep.saveStepAttribute(id_transformation, id_step, "IsAFile",       IsAFile);
			
            rep.saveStepAttribute(id_transformation, id_step, "XmlField",        XmlField);
			
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("getXMLDataMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}

	public FileInputList getFiles(VariableSpace space)
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
		if (input.length<=0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("getXMLDataMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("getXMLDataMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
		//	control Xpath	
		if (getLoopXPath()== null || Const.isEmpty(getLoopXPath()))
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("getXMLDataMeta.CheckResult.NoLoopXpath"), stepMeta);
			remarks.add(cr);
		}
		if(getInputFields().length<=0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("getXMLDataMeta.CheckResult.NoInputField"), stepMeta);
			remarks.add(cr);
		}		
		
		if(getIsInFields())
		{
			 if (Const.isEmpty(getXMLField()))
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("getXMLDataMeta.CheckResult.NoField"), stepMeta);
				 remarks.add(cr); 
			 }
			 else
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("getXMLDataMeta.CheckResult.FieldOk"), stepMeta);
				 remarks.add(cr); 
			 }		 
		}
		else
		{
	        FileInputList fileInputList = getFiles(transMeta);
			// String files[] = getFiles();
			if (fileInputList==null || fileInputList.getFiles().size()==0)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("getXMLDataMeta.CheckResult.NoFiles"), stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("getXMLDataMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
				remarks.add(cr);
			}	
		}	
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new getXMLData(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new getXMLDataData();
	}

    public boolean supportsErrorHandling()
    {
        return true;
    } 
}