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
 * Created on 07-sept-2007
 * 
 */

package org.pentaho.di.trans.steps.getfilesrowscount;

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

public class GetFilesRowsCountMeta extends BaseStepMeta implements StepMetaInterface
{	
	/** Array of filenames */
	private  String  fileName[]; 

	/** Wildcard or filemask (regular expression) */
	private  String  fileMask[];
 	 
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeFilesCount;
	
	/** The name of the field in the output containing the file number*/
	private  String  FilesCountFieldName;
	
	/** The name of the field in the output containing the row number*/
	private String RowsCountFieldName;
	
	/** The row separator type*/
	private String RowSeparator_format;
	
	/** The row separator*/
	private String RowSeparator;
	
	
	public GetFilesRowsCountMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	  /**
     * @return Returns the row separator.
     */
	public String getRowSeparator()
	{
		return RowSeparator;
	}
	
	/**
     * @param RowSeparatorin The RowSeparator to set.
     */
	public void setRowSeparator(String RowSeparatorin)
	{
		this.RowSeparator=RowSeparatorin;
	}
	
	  /**
     * @return Returns the row separator format.
     */
	public String getRowSeparatorFormat()
	{
		return RowSeparator_format;
	}
	
	/**
     * @param RowSeparator_formatin The RowSeparator_format to set.
     */
	public void setRowSeparatorFormat(String RowSeparator_formatin)
	{
		this.RowSeparator_format=RowSeparator_formatin;
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
     * @return Returns the includeCountFiles.
     */
    public boolean includeCountFiles()
    {
        return includeFilesCount;
    }
    
   
    /**
     * @param includeFilesCount The includeFilesCount to set.
     */
    public void setIncludeCountFiles(boolean includeFilesCountin)
    {
        this.includeFilesCount = includeFilesCountin;
    }
    
   

    /**
     * @return Returns the FilesCountFieldName.
     */
    public String getFilesCountFieldName()
    {
        return FilesCountFieldName;
    }
    
   
    /**
     * @return Returns the RowsCountFieldName.
     */
    public String getRowsCountFieldName()
    {
        return RowsCountFieldName;
    }
    
    
    
  
    
    
    /**
     * @param FilesCountFieldName The FilesCountFieldName to set.
     */
    public void setIncludeFilesCountFieldName(String FilesCountFieldNamein)
    {
        this.FilesCountFieldName = FilesCountFieldNamein;
    }
    
    /**
     * @param RowsCountFieldName The RowsCountFieldName to set.
     */
    public void setRowsCountFieldName(String RowsCountFieldNamein)
    {
        this.RowsCountFieldName = RowsCountFieldNamein;
    }
    
    
    
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		GetFilesRowsCountMeta retval = (GetFilesRowsCountMeta)super.clone();
		
		int nrFiles  = fileName.length;

		retval.allocate(nrFiles);
	
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer(300);
        
        retval.append("    ").append(XMLHandler.addTagValue("files_count",   includeFilesCount));
        retval.append("    ").append(XMLHandler.addTagValue("files_count_fieldname",FilesCountFieldName));
        retval.append("    ").append(XMLHandler.addTagValue("rows_count_fieldname",RowsCountFieldName));
        retval.append("    ").append(XMLHandler.addTagValue("rowseparator_format",RowSeparator_format));
        retval.append("    ").append(XMLHandler.addTagValue("row_separator",RowSeparator));
        
        retval.append("    <file>").append(Const.CR);
        for (int i=0;i<fileName.length;i++)
        {
            retval.append("      ").append(XMLHandler.addTagValue("name",     fileName[i]));
            retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
        }
        retval.append("    </file>").append(Const.CR);
        

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{

			includeFilesCount  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "files_count"));
			FilesCountFieldName    = XMLHandler.getTagValue(stepnode, "files_count_fieldname");
			RowSeparator_format    = XMLHandler.getTagValue(stepnode, "rowseparator_format");
			RowSeparator    = XMLHandler.getTagValue(stepnode, "row_separator");
			
			
			Node filenode   = XMLHandler.getSubNode(stepnode,  "file");
			int nrFiles     = XMLHandler.countNodes(filenode,  "name");
			allocate(nrFiles);
			
			for (int i=0;i<nrFiles;i++)
			{
				Node filenamenode = XMLHandler.getSubNodeByNr(filenode, "name", i); 
				Node filemasknode = XMLHandler.getSubNodeByNr(filenode, "filemask", i); 
				fileName[i] = XMLHandler.getNodeValue(filenamenode);
				fileMask[i] = XMLHandler.getNodeValue(filemasknode);
			}
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrfiles)
	{
		fileName   = new String [nrfiles];
		fileMask   = new String [nrfiles];
		        
	}
	
	public void setDefault()
	{
		
		includeFilesCount = false;
		FilesCountFieldName   = "";
		RowsCountFieldName   = "rowscount";
		RowSeparator_format="CR";
		RowSeparator ="";
		int nrFiles  =0;
		
		allocate(nrFiles);	
		
		for (int i=0;i<nrFiles;i++) 
		{
			fileName[i]="filename"+(i+1);
			fileMask[i]="";
		}
		

	}
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		
		
		
		ValueMetaInterface nr_row = new ValueMeta(space.environmentSubstitute(RowsCountFieldName), ValueMeta.TYPE_INTEGER);
		nr_row.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
		nr_row.setOrigin(name);
		r.addValueMeta(nr_row);
		
		if (includeFilesCount)
		{
			ValueMetaInterface nr_files = new ValueMeta(space.environmentSubstitute(FilesCountFieldName), ValueMeta.TYPE_INTEGER);
			nr_files.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			nr_files.setOrigin(name);
			r.addValueMeta(nr_files);
		}
		
		
	}
	
	 
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	
		try
		{
			
			includeFilesCount  = rep.getStepAttributeBoolean(id_step, "files_count");
			FilesCountFieldName    = rep.getStepAttributeString (id_step, "files_count_fieldname");
			RowsCountFieldName    = rep.getStepAttributeString (id_step, "rows_count_fieldname");
			RowSeparator_format    = rep.getStepAttributeString (id_step, "rowseparator_format");
			RowSeparator    = rep.getStepAttributeString (id_step, "row_separator");
			
			
			
			
			int nrFiles       = rep.countNrStepAttributes(id_step, "file_name");
            
			allocate(nrFiles);

			for (int i=0;i<nrFiles;i++)
			{
				fileName[i] =      rep.getStepAttributeString (id_step, i, "file_name"    );
				fileMask[i] =      rep.getStepAttributeString (id_step, i, "file_mask"    );
			}

			
        }
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GetFilesRowsCountMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{

			rep.saveStepAttribute(id_transformation, id_step, "files_count",        includeFilesCount);
			rep.saveStepAttribute(id_transformation, id_step, "files_count_fieldname",  FilesCountFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "rows_count_fieldname",  RowsCountFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "rowseparator_format",  RowSeparator_format);
			rep.saveStepAttribute(id_transformation, id_step, "row_separator",  RowSeparator);
			
			
			
					
			for (int i=0;i<fileName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name",     fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask",     fileMask[i]);
			}
			
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("GetFilesRowsCountMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GetFilesRowsCountMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GetFilesRowsCountMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
        FileInputList fileInputList = getFiles(transMeta);

		if (fileInputList==null || fileInputList.getFiles().size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GetFilesRowsCountMeta.CheckResult.NoFiles"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GetFilesRowsCountMeta.CheckResult.FilesOk", ""+fileInputList.getFiles().size()), stepMeta);
			remarks.add(cr);
		}
		
		if ((RowSeparator_format.equals("CUSTOM")) && (RowSeparator==null))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("GetFilesRowsCountMeta.CheckResult.NoSeparator"), stepMeta);
			remarks.add(cr);
		}		
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("GetFilesRowsCountMeta.CheckResult.SeparatorOk"), stepMeta);
			remarks.add(cr);
		}
		
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new GetFilesRowsCount(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new GetFilesRowsCountData();
	}

	
}