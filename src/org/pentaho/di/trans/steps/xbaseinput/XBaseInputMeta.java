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


package org.pentaho.di.trans.steps.xbaseinput;

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
import org.pentaho.di.core.row.RowMeta;
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

/*
 * Created on 2-jun-2003
 *
 */
 
public class XBaseInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = XBaseInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String 	dbfFileName;
	private int 	rowLimit;
	private boolean rowNrAdded;
	private String  rowNrField;
	
    /** Are we accepting filenames in input rows?  */
    private boolean acceptingFilenames;
    
    /** The field in which the filename is placed */
    private String  acceptingField;

    /** The stepname to accept filenames from */
    private String  acceptingStepName;

    /** The step to accept filenames from */
    private StepMeta acceptingStep;

    /** Flag indicating that we should include the filename in the output */
    private boolean includeFilename;

    /** The name of the field in the output containing the filename */
    private String filenameField;
    
    /** The character set / encoding used in the string or memo fields */
    private String charactersetName;

	public XBaseInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the dbfFileName.
     */
    public String getDbfFileName()
    {
        return dbfFileName;
    }
    
    /**
     * @param dbfFileName The dbfFileName to set.
     */
    public void setDbfFileName(String dbfFileName)
    {
        this.dbfFileName = dbfFileName;
    }
    
    /**
     * @return Returns the rowLimit.
     */
    public int getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(int rowLimit)
    {
        this.rowLimit = rowLimit;
    }
    
    /**
     * @return Returns the rowNrField.
     */
    public String getRowNrField()
    {
        return rowNrField;
    }
    
    /**
     * @param rowNrField The rowNrField to set.
     */
    public void setRowNrField(String rowNrField)
    {
        this.rowNrField = rowNrField;
    }
    
    /**
     * @return Returns the rowNrAdded.
     */
    public boolean isRowNrAdded()
    {
        return rowNrAdded;
    }
    
    /**
     * @param rowNrAdded The rowNrAdded to set.
     */
    public void setRowNrAdded(boolean rowNrAdded)
    {
        this.rowNrAdded = rowNrAdded;
    }


    /**
     * @return Returns the acceptingField.
     */
    public String getAcceptingField()
    {
        return acceptingField;
    }

    /**
     * @param acceptingField The acceptingField to set.
     */
    public void setAcceptingField(String acceptingField)
    {
        this.acceptingField = acceptingField;
    }

    /**
     * @return Returns the acceptingFilenames.
     */
    public boolean isAcceptingFilenames()
    {
        return acceptingFilenames;
    }

    /**
     * @param acceptingFilenames The acceptingFilenames to set.
     */
    public void setAcceptingFilenames(boolean acceptingFilenames)
    {
        this.acceptingFilenames = acceptingFilenames;
    }

    /**
     * @return Returns the acceptingStep.
     */
    public StepMeta getAcceptingStep()
    {
        return acceptingStep;
    }

    /**
     * @param acceptingStep The acceptingStep to set.
     */
    public void setAcceptingStep(StepMeta acceptingStep)
    {
        this.acceptingStep = acceptingStep;
    }

    /**
     * @return Returns the acceptingStepName.
     */
    public String getAcceptingStepName()
    {
        return acceptingStepName;
    }

    /**
     * @param acceptingStepName The acceptingStepName to set.
     */
    public void setAcceptingStepName(String acceptingStepName)
    {
        this.acceptingStepName = acceptingStepName;
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
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		XBaseInputMeta retval = (XBaseInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			dbfFileName        = XMLHandler.getTagValue(stepnode, "file_dbf"); //$NON-NLS-1$
			rowLimit           = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
			rowNrAdded         = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "add_rownr")); //$NON-NLS-1$ //$NON-NLS-2$
			rowNrField         = XMLHandler.getTagValue(stepnode, "field_rownr"); //$NON-NLS-1$
            
            includeFilename    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include"));
            filenameField      = XMLHandler.getTagValue(stepnode, "include_field");
            charactersetName   = XMLHandler.getTagValue(stepnode, "charset_name");
            
            acceptingFilenames = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "accept_filenames")); //$NON-NLS-1$
            acceptingField     = XMLHandler.getTagValue(stepnode, "accept_field"); //$NON-NLS-1$
            acceptingStepName  = XMLHandler.getTagValue(stepnode, "accept_stepname"); //$NON-NLS-1$

		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "XBaseInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		dbfFileName    = null;
		rowLimit    = 0;
		rowNrAdded   = false;
		rowNrField = null;
	}
	
    public String getLookupStepname()
    {
        if (acceptingFilenames &&
            acceptingStep!=null && 
            !Const.isEmpty( acceptingStep.getName() )
           ) 
            return acceptingStep.getName();
        return null;
    }

    public void searchInfoAndTargetSteps(List<StepMeta> steps) {
        acceptingStep = StepMeta.findStep(steps, acceptingStepName);
    }

    public String[] getInfoSteps()
    {
        if (acceptingFilenames && acceptingStep!=null)
        {
            return new String[] { acceptingStep.getName() };
        }
        return null;
    }
    
    public RowMetaInterface getOutputFields(FileInputList files, String name) throws KettleStepException {
    	RowMetaInterface rowMeta = new RowMeta();
    	
        // Take the first file to determine what the layout is...
        //
        XBase xbi=null;
		try
		{
            xbi = new XBase(getLog(), KettleVFS.getInputStream(files.getFile(0)));
            xbi.setDbfFile(files.getFile(0).getName().getURI());
            xbi.open();
			RowMetaInterface add = xbi.getFields();
			for (int i=0;i<add.size();i++)
			{
				ValueMetaInterface v=add.getValueMeta(i);
				v.setOrigin(name);
			}
			rowMeta.addRowMeta( add );
		}
		catch(Exception ke)
	    {
			throw new KettleStepException(BaseMessages.getString(PKG, "XBaseInputMeta.Exception.UnableToReadMetaDataFromXBaseFile"), ke); //$NON-NLS-1$
	    }
        finally
        {
            if (xbi!=null) xbi.close();
        }
	    
	    if (rowNrAdded && rowNrField!=null && rowNrField.length()>0)
	    {
	    	ValueMetaInterface rnr = new ValueMeta(rowNrField, ValueMetaInterface.TYPE_INTEGER);
	    	rnr.setOrigin(name);
	    	rowMeta.addValueMeta(rnr);
	    }
        
        if (includeFilename)
        {
            ValueMetaInterface v = new ValueMeta(filenameField, ValueMetaInterface.TYPE_STRING);
            v.setLength(100, -1);
            v.setOrigin(name);
            rowMeta.addValueMeta(v);
        }
		return rowMeta;
    }
    
    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

    	FileInputList fileList = getTextFileList(space);
        if (fileList.nrOfFiles()==0)
        {
            throw new KettleStepException(BaseMessages.getString(PKG, "XBaseInputMeta.Exception.NoFilesFoundToProcess")); //$NON-NLS-1$
        }

        row.addRowMeta( getOutputFields(fileList, name) );
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    " + XMLHandler.addTagValue("file_dbf",    dbfFileName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("limit",       rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("add_rownr",   rowNrAdded)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    " + XMLHandler.addTagValue("field_rownr", rowNrField)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("    " + XMLHandler.addTagValue("include", includeFilename));
        retval.append("    " + XMLHandler.addTagValue("include_field", filenameField));
        retval.append("    " + XMLHandler.addTagValue("charset_name", charactersetName));

        retval.append("    " + XMLHandler.addTagValue("accept_filenames", acceptingFilenames));
        retval.append("    " + XMLHandler.addTagValue("accept_field", acceptingField));
        retval.append("    " + XMLHandler.addTagValue("accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ));

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try
		{
			dbfFileName              =      rep.getStepAttributeString (id_step, "file_dbf"); //$NON-NLS-1$
			rowLimit              = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
			rowNrAdded             =      rep.getStepAttributeBoolean(id_step, "add_rownr"); //$NON-NLS-1$
			rowNrField           =      rep.getStepAttributeString (id_step, "field_rownr"); //$NON-NLS-1$
            
            includeFilename = rep.getStepAttributeBoolean(id_step, "include");
            filenameField = rep.getStepAttributeString(id_step, "include_field");
            charactersetName = rep.getStepAttributeString(id_step, "charset_name");

            acceptingFilenames = rep.getStepAttributeBoolean(id_step, "accept_filenames");
            acceptingField     = rep.getStepAttributeString (id_step, "accept_field");
            acceptingStepName  = rep.getStepAttributeString (id_step, "accept_stepname");

		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "XBaseInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_dbf",        dbfFileName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "add_rownr",       rowNrAdded); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "field_rownr",     rowNrField); //$NON-NLS-1$
            
            rep.saveStepAttribute(id_transformation, id_step, "include", includeFilename);
            rep.saveStepAttribute(id_transformation, id_step, "include_field", filenameField);
            rep.saveStepAttribute(id_transformation, id_step, "charset_name", charactersetName);

            rep.saveStepAttribute(id_transformation, id_step, "accept_filenames", acceptingFilenames); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "accept_field", acceptingField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "accept_stepname", (acceptingStep!=null?acceptingStep.getName():"") ); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "XBaseInputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info) {

		CheckResult cr;
		
		if (dbfFileName==null)
		{
            if ( isAcceptingFilenames() ) 
            {
        	     if ( Const.isEmpty(getAcceptingStepName()) ) 
           	     {
        	    	 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "XBaseInput.Log.Error.InvalidAcceptingStepName"), stepMeta); //$NON-NLS-1$
        	    	 remarks.add(cr);
                 }
           	
           	     if ( Const.isEmpty(getAcceptingField()) )
           	     {
           	    	cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "XBaseInput.Log.Error.InvalidAcceptingFieldName"), stepMeta); //$NON-NLS-1$
           	    	remarks.add(cr);
                 }
            }
            else
            {		
			    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "XBaseInputMeta.Remark.PleaseSelectFileToUse"), stepMeta); //$NON-NLS-1$
			    remarks.add(cr);
            }
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "XBaseInputMeta.Remark.FileToUseIsSpecified"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);

            XBase xbi = new XBase(getLog(), transMeta.environmentSubstitute(dbfFileName));
            try
            {
                xbi.open();
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "XBaseInputMeta.Remark.FileExistsAndCanBeOpened"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
                
                RowMetaInterface r = xbi.getFields();
            
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, r.size()+BaseMessages.getString(PKG, "XBaseInputMeta.Remark.OutputFieldsCouldBeDetermined"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            catch(KettleException ke)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "XBaseInputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            finally
            {
                xbi.close();
            }
        }
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new XBaseInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new XBaseInputData();
	}

    
    public String[] getFilePaths(VariableSpace space)
    {
        return FileInputList.createFilePathList(space, new String[] { dbfFileName}, new String[] { null }, new String[] { null }, new String[] { "N" });
    }
    
    public FileInputList getTextFileList(VariableSpace space)
    {
        return FileInputList.createFileList(space, new String[] { dbfFileName }, new String[] { null }, new String[] { null }, new String[] { "N" });
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "javadbf.jar", };
    }

	/**
	 * @return the charactersetName
	 */
	public String getCharactersetName() {
		return charactersetName;
	}

	/**
	 * @param charactersetName the charactersetName to set
	 */
	public void setCharactersetName(String charactersetName) {
		this.charactersetName = charactersetName;
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
			if (!acceptingFilenames) {
				// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.dbf
				// To   : /home/matt/test/files/foo/bar.dbf
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(dbfFileName), space);
				
				// If the file doesn't exist, forget about this effort too!
				//
				if (fileObject.exists()) {
					// Convert to an absolute path...
					// 
					dbfFileName = resourceNamingInterface.nameResource(fileObject, space, true);
					
					return dbfFileName;
				}
			}
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}

}
