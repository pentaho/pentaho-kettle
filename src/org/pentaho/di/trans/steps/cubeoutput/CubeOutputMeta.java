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

package org.pentaho.di.trans.steps.cubeoutput;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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


/*
 * Created on 4-apr-2003
 *
 */
public class CubeOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CubeOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename;
	/** Flag: add the filenames to result filenames */
    private boolean addToResultFilenames;
    
    /** Flag : Do not open new file when transformation start  */ 
    private boolean doNotOpenNewFileInit;

	public CubeOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	/**
	 * @return Returns the filename.
	 */
	public String getFilename()
	{
		return filename;
	}
	
    /**
     * @return Returns the add to result filesname.
     */
    public boolean isAddToResultFiles()
    {
    	return addToResultFilenames;
    }
    
    /**
     * @param addtoresultfilenamesin The addtoresultfilenames to set.
     */
    public void setAddToResultFiles(boolean addtoresultfilenamesin)
    {
        this.addToResultFilenames = addtoresultfilenamesin;
    }
    /**
     * @return Returns the "do not open new file at init" flag.
     */    
    public boolean isDoNotOpenNewFileInit()
    {
    	return doNotOpenNewFileInit;
    }

    /**
     * @param doNotOpenNewFileInit The "do not open new file at init" flag to set.
     */
    public void setDoNotOpenNewFileInit(boolean doNotOpenNewFileInit)
    {
    	this.doNotOpenNewFileInit=doNotOpenNewFileInit;
    }
	public Object clone()
	{
		CubeOutputMeta retval = (CubeOutputMeta)super.clone();

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			filename              = XMLHandler.getTagValue(stepnode, "file", "name"); //$NON-NLS-1$ //$NON-NLS-2$
			addToResultFilenames  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "add_to_result_filenames"));
			doNotOpenNewFileInit  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "do_not_open_newfile_init"));
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "CubeOutputMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		filename             = "file"; //$NON-NLS-1$
		addToResultFilenames = false;
		doNotOpenNewFileInit=false;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <file>").append(Const.CR); //$NON-NLS-1$
		retval.append("      ").append(XMLHandler.addTagValue("name",       filename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("add_to_result_filenames",   addToResultFilenames));
		retval.append("      ").append(XMLHandler.addTagValue("do_not_open_newfile_init",   doNotOpenNewFileInit));
		
		retval.append("    </file>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
	throws KettleException
	{
		try
		{
			filename               =      rep.getStepAttributeString (id_step, "file_name"); //$NON-NLS-1$
			addToResultFilenames   =      rep.getStepAttributeBoolean(id_step, "add_to_result_filenames");
			doNotOpenNewFileInit   =      rep.getStepAttributeBoolean(id_step, "do_not_open_newfile_init");
			
			
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CubeOutputMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_name",   filename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "add_to_result_filenames",    addToResultFilenames);  //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "do_not_open_newfile_init",    doNotOpenNewFileInit);  //$NON-NLS-1$
			
			
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CubeOutputMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		// Check output fields
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CubeOutputMeta.CheckResult.ReceivingFields",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "CubeOutputMeta.CheckResult.FileSpecificationsNotChecked"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new CubeOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CubeOutputData();
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
			// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.data
			// To   : /home/matt/test/files/foo/bar.data
			//
			FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(filename), space);
				
			// If the file doesn't exist, forget about this effort too!
			//
			if (fileObject.exists()) {
				// Convert to an absolute path...
				// 
				filename = resourceNamingInterface.nameResource(fileObject, space, true);
				
				return filename;
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}
}