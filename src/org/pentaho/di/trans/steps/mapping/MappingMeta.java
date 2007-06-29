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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.shared.SharedObjectInterface;
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

    private List<MappingIODefinition> inputMappings;
    private List<MappingIODefinition> outputMappings;
    private MappingParameters         mappingParameters;

	public MappingMeta()
	{
		super(); // allocate BaseStepMeta
	}
 
    public void loadXML(Node stepnode, List<? extends SharedObjectInterface> databases, Hashtable counters) throws KettleXMLException
	{
    	setDefault();
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
        
        Node mappingsNode  = XMLHandler.getSubNode(stepnode, "mappings"); //$NON-NLS-1$
                
        if (mappingsNode!=null)
        {
        	// Read all the input mapping definitions...
        	Node inputNode  = XMLHandler.getSubNode(mappingsNode, "input"); //$NON-NLS-1$
        	int nrInputMappings = XMLHandler.countNodes(inputNode, MappingIODefinition.XML_TAG); //$NON-NLS-1$
        	for (int i=0;i<nrInputMappings;i++) {
        		Node mappingNode = XMLHandler.getSubNodeByNr(inputNode, MappingIODefinition.XML_TAG, i);
        		MappingIODefinition inputMappingDefinition = new MappingIODefinition(mappingNode);
        		inputMappings.add(inputMappingDefinition);
        	}
        	Node outputNode  = XMLHandler.getSubNode(mappingsNode, "output"); //$NON-NLS-1$
        	int nrOutputMappings = XMLHandler.countNodes(inputNode, MappingIODefinition.XML_TAG); //$NON-NLS-1$
        	for (int i=0;i<nrOutputMappings;i++) {
        		Node mappingNode = XMLHandler.getSubNodeByNr(outputNode, MappingIODefinition.XML_TAG, i);
        		MappingIODefinition outputMappingDefinition = new MappingIODefinition(mappingNode);
        		outputMappings.add(outputMappingDefinition);
        	}
        	
        	// Load the mapping parameters too..
        	Node mappingParametersNode = XMLHandler.getSubNode(mappingsNode, MappingParameters.XML_TAG);
        	mappingParameters = new MappingParameters(mappingParametersNode);
        }
        else
        {
        	// backward compatibility...
        	//
            Node inputNode  = XMLHandler.getSubNode(stepnode, "input"); //$NON-NLS-1$
            Node outputNode = XMLHandler.getSubNode(stepnode, "output"); //$NON-NLS-1$
            
	        int nrInput  = XMLHandler.countNodes(inputNode, "connector"); //$NON-NLS-1$
	        int nrOutput = XMLHandler.countNodes(outputNode, "connector"); //$NON-NLS-1$
	        
	        MappingIODefinition inputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        
	        String inputField[] = new String[nrInput];
	        String inputMapping[] = new String[nrInput];
	        
	        for (int i=0;i<nrInput;i++)
	        {
	            Node inputConnector = XMLHandler.getSubNodeByNr(inputNode, "connector", i); //$NON-NLS-1$
	            inputField[i]   = XMLHandler.getTagValue(inputConnector, "field"); //$NON-NLS-1$
	            inputMapping[i] = XMLHandler.getTagValue(inputConnector, "mapping"); //$NON-NLS-1$
	        }
	        inputMappingDefinition.setParentField(inputField);
	        inputMappingDefinition.setMappingField(inputMapping);

	        MappingIODefinition outputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        
	        String outputField[] = new String[nrOutput];
	        String outputMapping[] = new String[nrOutput];
	        
	        for (int i=0;i<nrOutput;i++)
	        {
	            Node outputConnector = XMLHandler.getSubNodeByNr(outputNode, "connector", i); //$NON-NLS-1$
	            outputField[i]   = XMLHandler.getTagValue(outputConnector, "field"); //$NON-NLS-1$
	            outputMapping[i] = XMLHandler.getTagValue(outputConnector, "mapping"); //$NON-NLS-1$
	        }
	        
	        outputMappingDefinition.setMappingField(outputMapping);
	        outputMappingDefinition.setParentField(outputField);
	        
	        // Don't forget to add these to the input and output mapping definitions...
	        //
	        inputMappings.add(inputMappingDefinition);
	        outputMappings.add(outputMappingDefinition);
	        
	        // The default is to have no mapping parameters: the concept didn't exist before.
	        mappingParameters = new MappingParameters();
        }
	}
    
    public void allocate(int nrInput, int nrOutput)
    {
    	inputMappings = new ArrayList<MappingIODefinition>(nrInput);
    	outputMappings = new ArrayList<MappingIODefinition>(nrOutput);
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("    "+XMLHandler.addTagValue("trans_name", transName) ); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("filename", fileName )); //$NON-NLS-1$
        retval.append("    "+XMLHandler.addTagValue("directory_path", directoryPath )); //$NON-NLS-1$
        
        retval.append("    ").append(XMLHandler.openTag("mappings")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        retval.append("      ").append(XMLHandler.openTag("input")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        for (int i=0;i<inputMappings.size();i++)
        {
            retval.append(inputMappings.get(i).getXML());
        }
        retval.append("      ").append(XMLHandler.closeTag("input")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        retval.append("      ").append(XMLHandler.openTag("output")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        for (int i=0;i<outputMappings.size();i++)
        {
            retval.append(outputMappings.get(i).getXML());
        }
        retval.append("      ").append(XMLHandler.closeTag("output")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$

        retval.append("    ").append(XMLHandler.closeTag("mappings")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        
        return retval.toString();
    }

	public void setDefault()
	{
        allocate(0,0);
	}
    
    public void getFields(RowMetaInterface r, String origin, RowMetaInterface info[]) throws KettleStepException
    {
    	/*
    	 * TODO re-enable this.
    	 *      The problem for now is: we need to select a primary output and multiple targeted outputs...
    	 *      This getFields should only deal with the targetted one.
    	 *      We run into a certain little problem here.
    	 *      What we need to do is pass the "next step" into the equation/interface so that we know who's asking.
    	 *      If nobody is asking here, it's the main step.
    	 *      If one of the next steps is asking, it should be a certain other response.
    	 *      
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
        */
    }

    public void readRep(Repository rep, long id_step, List<? extends SharedObjectInterface> databases, Hashtable counters) throws KettleException
	{
    	/*
    	 * TODO re-enable repository support
    	 * 
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
        */
	}
    
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
    	/*
    	 * TODO re-enable repository support
    	 * 
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
        */
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
	

    public void check(List<CheckResult> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
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
        
		/*
		 * TODO re-enable validation code for mappings...
		 * 
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
        */
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

	/**
	 * @return the inputMappings
	 */
	public List<MappingIODefinition> getInputMappings() {
		return inputMappings;
	}

	/**
	 * @param inputMappings the inputMappings to set
	 */
	public void setInputMappings(List<MappingIODefinition> inputMappings) {
		this.inputMappings = inputMappings;
	}

	/**
	 * @return the outputMappings
	 */
	public List<MappingIODefinition> getOutputMappings() {
		return outputMappings;
	}

	/**
	 * @param outputMappings the outputMappings to set
	 */
	public void setOutputMappings(List<MappingIODefinition> outputMappings) {
		this.outputMappings = outputMappings;
	}

	/**
	 * @return the mappingParameters
	 */
	public MappingParameters getMappingParameters() {
		return mappingParameters;
	}

	/**
	 * @param mappingParameters the mappingParameters to set
	 */
	public void setMappingParameters(MappingParameters mappingParameters) {
		this.mappingParameters = mappingParameters;
	}
}
