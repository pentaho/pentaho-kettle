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


package org.pentaho.di.trans.steps.cubeinput;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
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
 * Created on 2-jun-2003
 *
 */
public class CubeInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CubeInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename;
	private int rowLimit;
	private boolean addfilenameresult;

	public CubeInputMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
	 * @return Returns the filename.
	 */
	public String getFilename()
	{
		return filename;
	}
	
	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	/**
	 * @param rowLimit The rowLimit to set.
	 */
	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}
	
	/**
	 * @return Returns the rowLimit.
	 */
	public int getRowLimit()
	{
		return rowLimit;
	}
	
	/**
	 * @return Returns the addfilenameresult.
	 */
	public boolean isAddResultFile()
	{
		return addfilenameresult;
	}
	
	/**
	 * @param addfilenameresult The addfilenameresult to set.
	 */
	public void setAddResultFile(boolean addfilenameresult)
	{
		this.addfilenameresult=addfilenameresult;
	}
	
	public Object clone()
	{
		CubeInputMeta retval = (CubeInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			filename  = XMLHandler.getTagValue(stepnode, "file", "name"); //$NON-NLS-1$ //$NON-NLS-2$
			rowLimit  = Const.toInt( XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
			addfilenameresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addfilenameresult"));
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		filename = "file"; //$NON-NLS-1$
		rowLimit = 0;
		addfilenameresult=false;
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
		throws KettleStepException
	{
		GZIPInputStream fis = null;
		DataInputStream dis = null;
		try
		{
			InputStream is = KettleVFS.getInputStream(space.environmentSubstitute(filename), space);
			fis = new GZIPInputStream(is);
			dis = new DataInputStream(fis);
	
			RowMetaInterface add = new RowMeta(dis);		
			for (int i=0;i<add.size();i++)
			{
				add.getValueMeta(i).setOrigin(name);
			}
			r.mergeRowMeta(add);
		}
		catch(KettleFileException kfe)
		{
			throw new KettleStepException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.UnableToReadMetaData"), kfe); //$NON-NLS-1$
		}
		catch(IOException e)
		{
			throw new KettleStepException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.ErrorOpeningOrReadingCubeFile"), e); //$NON-NLS-1$
		}
		finally
		{
			try
			{
				if (fis!=null) fis.close();
				if (dis!=null) dis.close();
			}
			catch(IOException ioe)
			{
				throw new KettleStepException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.UnableToCloseCubeFile"), ioe); //$NON-NLS-1$
			}
		}
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <file>").append(Const.CR); //$NON-NLS-1$
		retval.append("      ").append(XMLHandler.addTagValue("name", filename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    </file>").append(Const.CR); //$NON-NLS-1$
		retval.append("    ").append(XMLHandler.addTagValue("limit",  rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("addfilenameresult", addfilenameresult));
		

		return retval.toString();
	}
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleException
	{
		try
		{
			filename         =      rep.getStepAttributeString (id_step, "file_name"); //$NON-NLS-1$
			rowLimit         = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
			addfilenameresult = rep.getStepAttributeBoolean(id_step, "addfilenameresult");
			
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_name",   filename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "limit",       rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "addfilenameresult", addfilenameresult);
			
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CubeInputMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "CubeInputMeta.CheckResult.FileSpecificationsNotChecked"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CubeInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CubeInputData();
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
