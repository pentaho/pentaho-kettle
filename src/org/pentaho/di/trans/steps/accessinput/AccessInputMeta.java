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

public class AccessInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
 	 
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
	
	private static final String YES = "Y";
	
    public final static String type_trim_code[] = { "none", "left", "right", "both" };
    
	public AccessInputMeta()
	{
		super(); // allocate BaseStepMeta
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
     * @param resetRowNumber The resetRowNumber to set.
     */
    public void setResetRowNumber(boolean resetRowNumberin)
    {
        this.resetRowNumber = resetRowNumberin;
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
     * @param rowNumberField The tablenameField to set.
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
        retval.append("    ").append(XMLHandler.addTagValue("tablename_field", tablenameField));
        retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("resetrownumber",  resetRowNumber));
         
        
        retval.append("    ").append(XMLHandler.addTagValue("table_name",      TableName));
        
        retval.append("    <file>").append(Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
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
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			TableName    = XMLHandler.getTagValue(stepnode, "table_name");
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
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
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
		
		inputFields = new AccessInputField[nrfields];        
	}
	
	public void setDefault()
	{
		includeFilename  = false;
		filenameField    = "";
		includeTablename = false;
		tablenameField   = "";
		includeRowNumber = false;
		rowNumberField   = "";
		TableName   = "";
		
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
		
		if (includeTablename)
		{
			
		    ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(tablenameField), ValueMeta.TYPE_STRING);
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
	
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	
		try
		{
			includeFilename   = rep.getStepAttributeBoolean(id_step, "include");  
			filenameField     = rep.getStepAttributeString (id_step, "include_field");
			TableName          = rep.getStepAttributeString(id_step, "table_name");
			includeTablename  = rep.getStepAttributeBoolean(id_step, "tablename");
			tablenameField    = rep.getStepAttributeString (id_step, "tablename_field");
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
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
        }
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("AccessInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "include",         includeFilename);
			rep.saveStepAttribute(id_transformation, id_step, "include_field",   filenameField);
			rep.saveStepAttribute(id_transformation, id_step, "tablename",          includeTablename);
			rep.saveStepAttribute(id_transformation, id_step, "tablename_field",    tablenameField);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			rep.saveStepAttribute(id_transformation, id_step, "table_name",      TableName);
			rep.saveStepAttribute(id_transformation, id_step, "reset_rownumber",  resetRowNumber);

		
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    AccessInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "fied_attribut",       field.getColumn());
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
			throw new KettleException(Messages.getString("AccessInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessInputMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessInputMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles(transMeta);
		// String files[] = getFiles();
		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
		
		// Check table
		if (Const.isEmpty(getTableName()))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessInputMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new AccessInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new AccessInputData();
	}

	
}