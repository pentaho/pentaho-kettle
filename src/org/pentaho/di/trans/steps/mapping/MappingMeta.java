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
 
package org.pentaho.di.trans.steps.mapping;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute 
 * 
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class MappingMeta extends BaseStepMeta implements StepMetaInterface
{
    private String transName;
    private String fileName;
    private String directoryPath;
    
    // Also specify the required fields: tell which fields to use: make the mapping REALLY generic
    private String inputField[];
    private String inputMapping[];
    private String outputField[];
    private String outputMapping[];

	public MappingMeta()
	{
		super(); // allocate BaseStepMeta
	}

    /**
     * @return Returns the inputField.
     */
    public String[] getInputField()
    {
        return inputField;
    }

    /**
     * @param inputField The inputField to set.
     */
    public void setInputField(String[] inputField)
    {
        this.inputField = inputField;
    }

    /**
     * @return Returns the mappingField.
     */
    public String[] getInputMapping()
    {
        return inputMapping;
    }

    /**
     * @param mappingField The mappingField to set.
     */
    public void setInputMapping(String[] mappingField)
    {
        this.inputMapping = mappingField;
    }

    /**
     * @return Returns the outputField.
     */
    public String[] getOutputField()
    {
        return outputField;
    }

    /**
     * @param outputField The outputField to set.
     */
    public void setOutputField(String[] outputField)
    {
        this.outputField = outputField;
    }

    /**
     * @return Returns the outputRename.
     */
    public String[] getOutputMapping()
    {
        return outputMapping;
    }

    /**
     * @param outputRename The outputRename to set.
     */
    public void setOutputMapping(String[] outputRename)
    {
        this.outputMapping = outputRename;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Hashtable counters)
		throws KettleXMLException
	{
        try
        {
            readData(stepnode);
        }
        catch(KettleException e)
        {
            throw new KettleXMLException(Messages.getString("MappingMeta.Exception.ErrorLoadingTransformationStepFromXML"), e); //$NON-NLS-1$
        }
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleException
	{
        transName      = XMLHandler.getTagValue(stepnode, "trans_name"); //$NON-NLS-1$
        fileName       = XMLHandler.getTagValue(stepnode, "filename"); //$NON-NLS-1$
        directoryPath  = XMLHandler.getTagValue(stepnode, "directory_path"); //$NON-NLS-1$
        
        Node inputNode  = XMLHandler.getSubNode(stepnode, "input"); //$NON-NLS-1$
        Node outputNode = XMLHandler.getSubNode(stepnode, "output"); //$NON-NLS-1$

        int nrInput  = XMLHandler.countNodes(inputNode, "connector"); //$NON-NLS-1$
        int nrOutput = XMLHandler.countNodes(outputNode, "connector"); //$NON-NLS-1$

        allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            Node inputConnector = XMLHandler.getSubNodeByNr(inputNode, "connector", i); //$NON-NLS-1$
            inputField[i]   = XMLHandler.getTagValue(inputConnector, "field"); //$NON-NLS-1$
            inputMapping[i] = XMLHandler.getTagValue(inputConnector, "mapping"); //$NON-NLS-1$
        }
        
        for (int i=0;i<nrOutput;i++)
        {
            Node outputConnector = XMLHandler.getSubNodeByNr(outputNode, "connector", i); //$NON-NLS-1$
            outputField[i]   = XMLHandler.getTagValue(outputConnector, "field"); //$NON-NLS-1$
            outputMapping[i] = XMLHandler.getTagValue(outputConnector, "mapping"); //$NON-NLS-1$
        }
	}
    
    public void allocate(int nrInput, int nrOutput)
    {
        inputField   = new String[nrInput];
        inputMapping = new String[nrInput];
        outputField  = new String[nrOutput];
        outputMapping = new String[nrOutput];
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("trans_name", transName) ); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("filename", fileName )); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("directory_path", directoryPath )); //$NON-NLS-1$
        
        retval.append("  <input>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<inputField.length;i++)
        {
            retval.append("    <connector>"+XMLHandler.addTagValue("field", inputField[i], false)+"  "+XMLHandler.addTagValue("mapping", inputMapping[i], false)+"</connector>"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
        retval.append("    </input>"+Const.CR); //$NON-NLS-1$
        
        retval.append("  <output>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<outputField.length;i++)
        {
            retval.append("    <connector>"+XMLHandler.addTagValue("field", outputField[i], false)+"  "+XMLHandler.addTagValue("mapping", outputMapping[i], false)+"</connector>"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
        retval.append("    </output>"+Const.CR); //$NON-NLS-1$
        
        return retval.toString();
    }

	public void setDefault()
	{
        allocate(0,0);
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface info[]) throws KettleStepException
    {
    	// Change the names of the fields if this is required by the mapping.
    	for (int i=0;i<inputField.length;i++)
		{
			if (inputField[i]!=null && inputField[i].length()>0)
			{
				if (inputMapping[i]!=null && inputMapping[i].length()>0)
				{
					if (!inputField[i].equals(inputMapping[i])) // rename these!
					{
						int idx = r.indexOfValue(inputField[i]);
						if (idx<0)
						{
							throw new KettleStepException(Messages.getString("MappingMeta.Exception.MappingTargetFieldNotPresent",inputField[i])); //$NON-NLS-1$ //$NON-NLS-2$
						}
						r.getValueMeta(idx).setName(inputMapping[i]);
					}
				}
				else
				{
					throw new KettleStepException(Messages.getString("MappingMeta.Exception.MappingTargetFieldNotSpecified",i+"",inputField[i])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			else
			{
				throw new KettleStepException(Messages.getString("MappingMeta.Exception.InputFieldNotSpecified",i+"")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

    	// Then see which fields get added to the row.
    	//
        Repository repository = Repository.getCurrentRepository(); 
        TransMeta mappingTransMeta = null;
        try
        {
            mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository);
        }
        catch(KettleException e)
        {
            throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToLoadMappingTransformation"), e);
        }

        if (mappingTransMeta!=null)
        {
            StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
            if (stepMeta!=null)
            {
            	stepMeta.getStepMetaInterface().getFields(r, name, info);
            	
                // Change the output fields that are specified...
                for (int i=0;i<outputMapping.length;i++)
                {
                    ValueMetaInterface v = r.searchValueMeta(outputMapping[i]);
                    if (v!=null)
                    {
                        v.setName(outputField[i]);
                        v.setOrigin(name);
            		}
            		else
            		{
            			throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindField")+outputMapping[i]); //$NON-NLS-1$
            		}
                }
            }
            else
            {
            	throw new KettleStepException(Messages.getString("MappingMeta.Exception.MappingOutputStepRequired")); //$NON-NLS-1$
            }
        }
        else
        {
            throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToGetFieldsFromMapping")); //$NON-NLS-1$
        }
    }

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Hashtable counters) throws KettleException
	{
        transName        = rep.getStepAttributeString(id_step, "trans_name"); //$NON-NLS-1$
        fileName         = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
        directoryPath    = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$
        
        int nrInput  = rep.countNrStepAttributes(id_step, "input_field"); //$NON-NLS-1$
        int nrOutput = rep.countNrStepAttributes(id_step, "output_field"); //$NON-NLS-1$

        allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            inputField[i]   = rep.getStepAttributeString(id_step, i, "input_field"); //$NON-NLS-1$
            inputMapping[i] = rep.getStepAttributeString(id_step, i, "input_mapping"); //$NON-NLS-1$
        }

        for (int i=0;i<nrOutput;i++)
        {
            outputField[i]   = rep.getStepAttributeString(id_step, i, "output_field"); //$NON-NLS-1$
            outputMapping[i] = rep.getStepAttributeString(id_step, i, "output_mapping"); //$NON-NLS-1$
        }
	}
    
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "trans_name", transName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$
        
        if (inputField!=null)
        for (int i=0;i<inputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "input_field",   inputField[i]); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, i, "input_mapping", inputMapping[i]); //$NON-NLS-1$
        }
        
        if (outputField!=null)
        for (int i=0;i<outputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "output_field",   outputField[i]); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, i, "output_mapping", outputMapping[i]); //$NON-NLS-1$
        }
    }

    public synchronized static final TransMeta loadMappingMeta(String fileName, String transName, String directoryPath, Repository rep) throws KettleException
    {
        TransMeta mappingTransMeta = null;
        
        String realFilename = StringUtil.environmentSubstitute(fileName);
        String realTransname = StringUtil.environmentSubstitute(transName);
        
        if ( !Const.isEmpty(realFilename))
        {
            try
            {
            	// OK, load the meta-data from file...
                mappingTransMeta = new TransMeta( realFilename, false ); // don't set internal variables: they belong to the parent thread!
                LogWriter.getInstance().logDetailed("Loading Mapping from repository", "Mapping transformation was loaded from XML file ["+realFilename+"]");
                // mappingTransMeta.setFilename(fileName);
           }
            catch(Exception e)
            {
                LogWriter.getInstance().logError("Loading Mapping from XML", "Unable to load transformation ["+realFilename+"] : "+e.toString());
                LogWriter.getInstance().logError("Loading Mapping from XML", Const.getStackTracker(e));
            }
        }
        else
        {
            // OK, load the meta-data from the repository...
            if (!Const.isEmpty(realTransname) && directoryPath!=null && rep!=null)
            {
                RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(directoryPath);
                if (repdir!=null)
                {
                    try
                    {
                        mappingTransMeta = new TransMeta(rep, realTransname, repdir);
                        LogWriter.getInstance().logDetailed("Loading Mapping from repository", "Mapping transformation ["+realTransname+"] was loaded from the repository");
                    }
                    catch(Exception e)
                    {
                        LogWriter.getInstance().logError("Loading Mapping from repository", "Unable to load transformation ["+realTransname+"] : "+e.toString());
                        LogWriter.getInstance().logError("Loading Mapping from repository", Const.getStackTracker(e));
                    }
                }
                else
                {
                    throw new KettleException(Messages.getString("MappingMeta.Exception.UnableToLoadTransformation",realTransname)+directoryPath); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        
        return mappingTransMeta;
    }
	

	public void check(List<CheckResult> remarks, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MappingMeta.CheckResult.NotReceivingAnyFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFields",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFieldsFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
        
    	// Change the names of the fields if this is required by the mapping.
    	for (int i=0;i<inputField.length;i++)
		{
			if (inputField[i]!=null && inputField[i].length()>0)
			{
				if (inputMapping[i]!=null && inputMapping[i].length()>0)
				{
					if (!inputField[i].equals(inputMapping[i])) // rename these!
					{
						int idx = prev.indexOfValue(inputField[i]);
						if (idx<0)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingTargetFieldNotPresent",inputField[i]), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
							remarks.add(cr);
						}
					}
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingTargetFieldNotSepecified",i+"",inputField[i]), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.InputFieldNotSpecified",i+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}

    	// Then check the fields that get added to the row.
    	//
        
        Repository repository = Repository.getCurrentRepository(); 
        TransMeta mappingTransMeta = null;
        try
        {
            mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository);
        }
        catch(KettleException e)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.UnableToLoadMappingTransformation")+":"+Const.getStackTracker(e), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }

        if (mappingTransMeta!=null)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.MappingTransformationSpecified"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);

            StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
            
            if (stepMeta!=null)
            {
	            // See which fields are coming out of the mapping output step of the sub-transformation
	            // For these fields we check the existance
	            //
	            RowMetaInterface fields = null;
	            try
	            {
	            	fields = mappingTransMeta.getStepFields(stepMeta);
	
	            	boolean allOK = true;
	                
	                // Check the fields...
	                for (int i=0;i<outputMapping.length;i++)
	                {
	                    ValueMetaInterface v = fields.searchValueMeta(outputMapping[i]);
	                    if (v==null) // Not found!
	                    {
	                        cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.MappingOutFieldSpecifiedCouldNotFound")+outputMapping[i], stepinfo); //$NON-NLS-1$
	                        remarks.add(cr);
	                        allOK=false;
	                    }
	                }
	                
	                if (allOK)
	                {
	                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.AllOutputMappingFieldCouldBeFound"), stepinfo); //$NON-NLS-1$
	                    remarks.add(cr);
	                }
	            }
	            catch(KettleStepException e)
	            {
	                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.UnableToGetStepOutputFields")+stepMeta.getName()+"]", stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
	                remarks.add(cr);
	            }
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoMappingOutputStepSpecified"), stepinfo); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoMappingSpecified"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new MappingDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Mapping(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new MappingData();
	}

    /**
     * @return the directoryPath
     */
    public String getDirectoryPath()
    {
        return directoryPath;
    }

    /**
     * @param directoryPath the directoryPath to set
     */
    public void setDirectoryPath(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }

    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /**
     * @return the transName
     */
    public String getTransName()
    {
        return transName;
    }

    /**
     * @param transName the transName to set
     */
    public void setTransName(String transName)
    {
        this.transName = transName;
    }


}
