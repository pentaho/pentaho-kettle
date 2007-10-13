 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package be.ibridge.kettle.trans.step.accessinput;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.fileinput.FileInputList;


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
     * @return Returns the RealfilenameField.
     */
    public String getRealFilenameField()
    {
    	return  StringUtil.environmentSubstitute(getFilenameField());
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
    

	public String getRealTableName()
	{
	   	return  StringUtil.environmentSubstitute(getTableName());
	} 
	    
    
    /**
     * @return Returns the RealrowNumberField.
     */
    public String getRealRowNumberField()
    {
    	return  StringUtil.environmentSubstitute(getRowNumberField());
    }   
    
    /**
     * @return Returns the RealtablenameField.
     */
    public String getRealtablenameField()
    {
    	return  StringUtil.environmentSubstitute(gettablenameField());
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
        
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
        StringBuffer retval=new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("include",         includeFilename));
        retval.append("    "+XMLHandler.addTagValue("include_field",   filenameField));
        retval.append("    "+XMLHandler.addTagValue("tablename",         includeTablename));
        retval.append("    "+XMLHandler.addTagValue("tablename_field",   tablenameField));
        retval.append("    "+XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    "+XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    "+XMLHandler.addTagValue("table_name",      TableName));
        
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
            AccessInputField field = inputFields[i];
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
			includeFilename   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
			filenameField     = XMLHandler.getTagValue(stepnode, "include_field");
			includeTablename  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "tablename"));
			tablenameField    = XMLHandler.getTagValue(stepnode, "tablename_field");
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			TableName    = XMLHandler.getTagValue(stepnode, "table_name");
	
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
				AccessInputField field = new AccessInputField(fnode);
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
	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    AccessInputField field = inputFields[i];
		    
			int type=field.getType();
			if (type==Value.VALUE_TYPE_NONE) type=Value.VALUE_TYPE_STRING;
			Value v=new Value(field.getName(), type);
			v.setLength(field.getLength(), field.getPrecision());
			v.setOrigin(name);
			row.addValue(v);
		}
		if (includeFilename)
		{
			Value v = new Value(filenameField, Value.VALUE_TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			row.addValue(v);
		}
		if (includeRowNumber)
		{
			Value v = new Value(rowNumberField, Value.VALUE_TYPE_NUMBER);
			v.setLength(7, 0);
			v.setOrigin(name);
			row.addValue(v);
		}
		return row;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
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
				field.setAttribute( rep.getStepAttributeString (id_step, i, "field_attribut") );
				field.setType( Value.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
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
			
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
			}
			
			for (int i=0;i<inputFields.length;i++)
			{
			    AccessInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "fied_attribut",       field.getAttribute());
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
	

	public FileInputList getFiles()
	{
        String required[] = new String[fileName.length];
        boolean subdirs[] = new boolean[fileName.length];
        for (int i=0;i<required.length;i++)
        {
            required[i]="Y";
            subdirs[i]=false;
        }
        return FileInputList.createFileList(StringUtil.environmentSubstitute(fileName), StringUtil.environmentSubstitute(fileMask), required, subdirs);
	}
	
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessInputMeta.CheckResult.NoInputExpected"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessInputMeta.CheckResult.NoInput"), stepinfo);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles();
		// String files[] = getFiles();
		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AccessInputMeta.CheckResult.NoFiles"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AccessInputMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new AccessInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new AccessInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AccessInputData();
	}
}