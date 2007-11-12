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

package org.pentaho.di.trans.steps.getfilenames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



public class GetFileNamesMeta extends BaseStepMeta implements StepMetaInterface
{
	private static final String NO = "N";

	private static final String YES = "Y";

	/** Array of filenames */
	private String             fileName[];

	/** Wildcard or filemask (regular expression) */
	private String             fileMask[];
    
	/** Array of boolean values as string, indicating if a file is required. */
	private String             fileRequired[];
	
	/** Filter indicating file filter */
	private String filterfiletype;
	
	public GetFileNamesMeta()
	{
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the fileMask.
	 */
	public String[] getFileMask()
	{
		return fileMask;
	}
    
	/**
	 * @return Returns the fileRequired.
	 */
	public String[] getFileRequired() 
	{
		return fileRequired;
	}

	/**
	 * @param fileMask The fileMask to set.
	 */
	public void setFileMask(String[] fileMask)
	{
		this.fileMask = fileMask;
	}
    
	/**
	 * @param fileRequired The fileRequired to set.
	 */
	public void setFileRequired(String[] fileRequired)
	{
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
	
	public void setFilterFileType(int filtertypevalue)
	{
		if (filtertypevalue==0)
		{
			this.filterfiletype= "all_files";
		}
		else if (filtertypevalue==1)
		{
			this.filterfiletype= "only_files";
		}
		else if (filtertypevalue==2)
		{
			this.filterfiletype= "only_folders";
		}
	}
	
	public String getFilterFileType()
	{	
		return filterfiletype;
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		GetFileNamesMeta retval = (GetFileNamesMeta) super.clone();

		int nrfiles = fileName.length;

		retval.allocate(nrfiles);

		return retval;
	}

	public void allocate(int nrfiles)
	{
		fileName = new String[nrfiles];
		fileMask = new String[nrfiles];
		fileRequired = new String[nrfiles];
	}

	public void setDefault()
	{
		int nrfiles = 0;
		filterfiletype="all_files";


		allocate(nrfiles);

		for (int i = 0; i < nrfiles; i++)
		{
			fileName[i] = "filename" + (i + 1);
			fileMask[i] = "";
			fileRequired[i] = NO;
		}
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
        
		// the filename
		ValueMetaInterface filename = new ValueMeta("filename",ValueMeta.TYPE_STRING);
		filename.setLength(500);
		filename.setPrecision(-1);
		filename.setOrigin(name);
		row.addValueMeta(filename);

		// the short filename
		ValueMetaInterface short_filename = new ValueMeta("short_filename",ValueMeta.TYPE_STRING);
		short_filename.setLength(500);
		short_filename.setPrecision(-1);
		short_filename.setOrigin(name);
		row.addValueMeta(short_filename);

		// the path
		ValueMetaInterface path = new ValueMeta("path",ValueMeta.TYPE_STRING);
		path.setLength(500);
		path.setPrecision(-1);
		path.setOrigin(name);
		row.addValueMeta(path);
        
		// the type     
		ValueMetaInterface type = new ValueMeta("type",ValueMeta.TYPE_STRING);
		type.setLength(500);
		type.setPrecision(-1);
		type.setOrigin(name);
		row.addValueMeta(type);
        
		// the exists     
		ValueMetaInterface exists = new ValueMeta("exists",ValueMeta.TYPE_BOOLEAN);
		exists.setOrigin(name);
		row.addValueMeta(exists);
        
		// the ishidden     
		ValueMetaInterface ishidden = new ValueMeta("ishidden",ValueMeta.TYPE_BOOLEAN);
		ishidden.setOrigin(name);
		row.addValueMeta(ishidden);
              
		// the isreadable     
		ValueMetaInterface isreadable = new ValueMeta("isreadable",ValueMeta.TYPE_BOOLEAN);
		isreadable.setOrigin(name);
		row.addValueMeta(isreadable);
        
		// the iswriteable     
		ValueMetaInterface iswriteable = new ValueMeta("iswriteable",ValueMeta.TYPE_BOOLEAN);
		iswriteable.setOrigin(name);
		row.addValueMeta(iswriteable);  
                
		// the lastmodifiedtime     
		ValueMetaInterface lastmodifiedtime = new ValueMeta("lastmodifiedtime",ValueMeta.TYPE_DATE);
		lastmodifiedtime.setOrigin(name);
		row.addValueMeta(lastmodifiedtime);  
        
		// the size     
		ValueMetaInterface size = new ValueMeta("size", ValueMeta.TYPE_INTEGER);
		size.setOrigin(name);
		row.addValueMeta(size);
        
		// the extension     
		ValueMetaInterface extension = new ValueMeta("extension", ValueMeta.TYPE_STRING);
		extension.setOrigin(name);
		row.addValueMeta(extension); 
               
		// the uri     
		ValueMetaInterface uri = new ValueMeta("uri", ValueMeta.TYPE_STRING);
		uri.setOrigin(name);
		row.addValueMeta(uri); 
        
        
		// the rooturi     
		ValueMetaInterface rooturi = new ValueMeta("rooturi", ValueMeta.TYPE_STRING);
		rooturi.setOrigin(name);
		row.addValueMeta(rooturi); 
 
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
			
		retval.append("    <filter>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("filterfiletype",  filterfiletype));
		retval.append("    </filter>").append(Const.CR);
			

		retval.append("    <file>").append(Const.CR);
		
		for (int i = 0; i < fileName.length; i++)
		{
			retval.append("      ").append(XMLHandler.addTagValue("name", fileName[i]));
			retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
		}
		retval.append("    </file>").append(Const.CR);

		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			Node filternode         = XMLHandler.getSubNode(stepnode, "filter");
			Node filterfiletypenode = XMLHandler.getSubNode(filternode, "filterfiletype");
			filterfiletype          = XMLHandler.getNodeValue(filterfiletypenode);
					
			Node filenode = XMLHandler.getSubNode(stepnode, "file");
			int nrfiles   = XMLHandler.countNodes(filenode, "name");
				
			allocate(nrfiles);

			for (int i = 0; i < nrfiles; i++)
			{
				Node filenamenode     = XMLHandler.getSubNodeByNr(filenode, "name", i);
				Node filemasknode     = XMLHandler.getSubNodeByNr(filenode, "filemask", i);
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				fileName[i]           = XMLHandler.getNodeValue(filenamenode);
				fileMask[i]           = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i]       = XMLHandler.getNodeValue(fileRequirednode);
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			int nrfiles = rep.countNrStepAttributes(id_step, "file_name");
			filterfiletype=rep.getStepAttributeString(id_step, "filterfiletype");
						
			allocate(nrfiles);

			for (int i = 0; i < nrfiles; i++)
			{
				fileName[i] = rep.getStepAttributeString(id_step, i, "file_name");
				fileMask[i] = rep.getStepAttributeString(id_step, i, "file_mask");
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
				if(!YES.equalsIgnoreCase(fileRequired[i])) fileRequired[i] = NO;
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{			
			rep.saveStepAttribute(id_transformation, id_step, "filterfiletype", filterfiletype);
			
			for (int i = 0; i < fileName.length; i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name", fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask", fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

	public String[] getFilePaths(VariableSpace space)
	{
		return FileInputList.createFilePathList(space, fileName, fileMask, fileRequired);
	}
    
	public FileInputList getTextFileList(VariableSpace space)
	{
		return FileInputList.createFileList(space, fileName, fileMask, fileRequired);
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length > 0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.NoInputError"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.NoInputOk"), stepinfo);
			remarks.add(cr);
		}

		FileInputList textFileList = getTextFileList(transMeta);
		if (textFileList.nrOfFiles() == 0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesError"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesOk", ""+textFileList.nrOfFiles()), stepinfo);
			remarks.add(cr);
		}
	}

  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
     List<ResourceReference> references = new ArrayList<ResourceReference>(5);
     ResourceReference reference = new ResourceReference(stepInfo);
     references.add(reference);

     String[] textFiles = getFilePaths(transMeta);
     if ( textFiles!=null ) {
       for (int i=0; i<textFiles.length; i++) {
         reference.getEntries().add( new ResourceEntry(textFiles[i], ResourceType.FILE));
       }
     }
     return references;
  }
  
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new GetFileNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GetFileNamesData();
	}	
}