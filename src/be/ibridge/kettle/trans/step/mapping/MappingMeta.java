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
 
package be.ibridge.kettle.trans.step.mapping;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute 
 * 
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class MappingMeta extends BaseStepMeta implements StepMetaInterface
{
    private TransMeta mappingTransMeta;
    
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
     * @return Returns the mappingTransMeta.
     */
    public TransMeta getMappingTransMeta()
    {
        return mappingTransMeta;
    }

    /**
     * @param mappingTransMeta The mappingTransMeta to set.
     */
    public void setMappingTransMeta(TransMeta mappingTransMeta)
    {
        this.mappingTransMeta = mappingTransMeta;
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

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
        try
        {
            readData(stepnode);
        }
        catch(KettleException e)
        {
            throw new KettleXMLException("Error loading transformation step from XML", e);
        }
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleException
	{
        transName      = XMLHandler.getTagValue(stepnode, "trans_name");
        fileName       = XMLHandler.getTagValue(stepnode, "filename");
        directoryPath  = XMLHandler.getTagValue(stepnode, "directory_path");
        
        Node inputNode  = XMLHandler.getSubNode(stepnode, "input");
        Node outputNode = XMLHandler.getSubNode(stepnode, "output");

        int nrInput  = XMLHandler.countNodes(inputNode, "connector");
        int nrOutput = XMLHandler.countNodes(outputNode, "connector");

        allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            Node inputConnector = XMLHandler.getSubNodeByNr(inputNode, "connector", i);
            inputField[i]   = XMLHandler.getTagValue(inputConnector, "field");
            inputMapping[i] = XMLHandler.getTagValue(inputConnector, "mapping");
        }
        
        for (int i=0;i<nrOutput;i++)
        {
            Node outputConnector = XMLHandler.getSubNodeByNr(outputNode, "connector", i);
            outputField[i]   = XMLHandler.getTagValue(outputConnector, "field");
            outputMapping[i] = XMLHandler.getTagValue(outputConnector, "mapping");
        }
        
        loadMappingMeta(null);
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
        
        if (mappingTransMeta!=null)
        {
            retval.append("    "+XMLHandler.addTagValue("trans_name", mappingTransMeta.getName()));
            if (mappingTransMeta.getDirectory()!=null)
            {
                retval.append("    "+XMLHandler.addTagValue("directory_path", mappingTransMeta.getDirectory().getPath()));
            }
            if (mappingTransMeta.getFilename()!=null)
            {
                retval.append("    "+XMLHandler.addTagValue("filename", mappingTransMeta.getFilename()));
            }
        }
        
        retval.append("  <input>"+Const.CR);
        for (int i=0;i<inputField.length;i++)
        {
            retval.append("    <connector>"+XMLHandler.addTagValue("field", inputField[i], false)+"  "+XMLHandler.addTagValue("mapping", inputMapping[i], false)+"</connector>"+Const.CR);
        }
        retval.append("    </input>"+Const.CR);
        
        retval.append("  <output>"+Const.CR);
        for (int i=0;i<outputField.length;i++)
        {
            retval.append("    <connector>"+XMLHandler.addTagValue("field", outputField[i], false)+"  "+XMLHandler.addTagValue("mapping", outputMapping[i], false)+"</connector>"+Const.CR);
        }
        retval.append("    </output>"+Const.CR);
        
        return retval.toString();
    }

	public void setDefault()
	{
	}
    
    public Row getFields(Row r, String name, Row info) throws KettleStepException
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
						int idx = r.searchValueIndex(inputField[i]);
						if (idx<0)
						{
							throw new KettleStepException("Mapping target field ["+inputField[i]+"] is not present in the input rows!");
						}
						r.getValue(idx).setName(inputMapping[i]);
					}
				}
				else
				{
					throw new KettleStepException("Mapping target field #"+i+" is not specified for input ["+inputField[i]+"]!");
				}
			}
			else
			{
				throw new KettleStepException("Input field #"+i+" is not specified!");
			}
		}

    	// Then see which fields get added to the row.
    	// 
        if (mappingTransMeta!=null)
        {
            StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
            if (stepMeta!=null)
            {
            	stepMeta.getStepMetaInterface().getFields(r, name, info);
            	
                // Change the output fields that are specified...
                for (int i=0;i<outputMapping.length;i++)
                {
                    Value v = r.searchValue(outputMapping[i]);
                    if (v!=null)
                    {
                        v.setName(outputField[i]);
                        v.setOrigin(name);
            		}
            		else
            		{
            			throw new KettleStepException("Unable to find field : "+outputMapping[i]);
            		}
                }
                
                return r;
            }
            else
            {
            	throw new KettleStepException("Please use one (1) mapping output step in the mapping (sub-transformation)");
            }
        }
        else
        {
            throw new KettleStepException("Unable to get fields from Mapping: no mapping is selected.");
        }
    }

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
	{
        transName        = rep.getStepAttributeString(id_step, "trans_name");
        fileName         = rep.getStepAttributeString(id_step, "filename");
        directoryPath    = rep.getStepAttributeString(id_step, "directory_path");
        
        int nrInput  = rep.countNrStepAttributes(id_step, "input_field");
        int nrOutput = rep.countNrStepAttributes(id_step, "output_field");

        allocate(nrInput, nrOutput);
        
        for (int i=0;i<nrInput;i++)
        {
            inputField[i]   = rep.getStepAttributeString(id_step, i, "input_field");
            inputMapping[i] = rep.getStepAttributeString(id_step, i, "input_mapping");
        }

        for (int i=0;i<nrOutput;i++)
        {
            outputField[i]   = rep.getStepAttributeString(id_step, i, "output_field");
            outputMapping[i] = rep.getStepAttributeString(id_step, i, "output_mapping");
        }

        loadMappingMeta(rep);
	}
    
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        if (mappingTransMeta!=null)
        {
            if (mappingTransMeta.getFilename()!=null)
            {
                rep.saveStepAttribute(id_transformation, id_step, "filename", mappingTransMeta.getFilename());
            }
            else
            {
                rep.saveStepAttribute(id_transformation, id_step, "trans_name", mappingTransMeta.getName());
                if (mappingTransMeta.getDirectory()!=null)
                {
                    rep.saveStepAttribute(id_transformation, id_step, "directory_path", mappingTransMeta.getDirectory().getPath());
                }
            }
        }
        
        if (inputField!=null)
        for (int i=0;i<inputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "input_field",   inputField[i]);
            rep.saveStepAttribute(id_transformation, id_step, i, "input_mapping", inputMapping[i]);
        }
        
        if (outputField!=null)
        for (int i=0;i<outputField.length;i++)
        {
            rep.saveStepAttribute(id_transformation, id_step, i, "output_field",   outputField[i]);
            rep.saveStepAttribute(id_transformation, id_step, i, "output_mapping", outputMapping[i]);
        }
    }

    private void loadMappingMeta(Repository rep) throws KettleException
    {
        if (fileName!=null && fileName.length()>0)
        {
        	// OK, load the meta-data from file...
            // NOTE: id_transformation is saved, but is useless...
                mappingTransMeta = new TransMeta(fileName);
        }
        else
        {
            // OK, load the meta-data from the repository...
            // NOTE: filename is saved, but is useless in this context...
            if (transName!=null && directoryPath!=null && rep!=null)
            {
                RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(directoryPath);
                if (repdir!=null)
                {
                    mappingTransMeta = new TransMeta(rep, transName, repdir);
                }
                else
                {
                    throw new KettleException("Unable to load transformation ["+transName+"] : can't find directory "+directoryPath);
                }
            }
        }
    }
	

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
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
						int idx = prev.searchValueIndex(inputField[i]);
						if (idx<0)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Mapping target field ["+inputField[i]+"] is not present in the input rows!", stepinfo);
							remarks.add(cr);
						}
					}
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Mapping target field #"+i+" is not specified for input ["+inputField[i]+"]!", stepinfo);
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Input field #"+i+" is not specified!", stepinfo);
				remarks.add(cr);
			}
		}

    	// Then check the fields that get added to the row.
    	// 
        if (mappingTransMeta!=null)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "A mapping (transformation) is specified.", stepinfo);
            remarks.add(cr);

            StepMeta stepMeta = mappingTransMeta.getMappingOutputStep();
            
            if (stepMeta!=null)
            {
	            // See which fields are coming out of the mapping output step of the sub-transformation
	            // For these fields we check the existance
	            //
	            Row fields = null;
	            try
	            {
	            	fields = mappingTransMeta.getStepFields(stepMeta);
	
	            	boolean allOK = true;
	                
	                // Check the fields...
	                for (int i=0;i<outputMapping.length;i++)
	                {
	                    Value v = fields.searchValue(outputMapping[i]);
	                    if (v==null) // Not found!
	                    {
	                        cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Mapping output field specified couldn't be found in the mapping: "+outputMapping[i], stepinfo);
	                        remarks.add(cr);
	                        allOK=false;
	                    }
	                }
	                
	                if (allOK)
	                {
	                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All output mapping fields could be found in the mapping.", stepinfo);
	                    remarks.add(cr);
	                }
	            }
	            catch(KettleStepException e)
	            {
	                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Unable to get the step output fields for mapping output step ["+stepMeta.getName()+"]", stepinfo);
	                remarks.add(cr);
	            }
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No mapping output step was specified in the mapping!", stepinfo);
                remarks.add(cr);
            }
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "There is no mapping specified!", stepinfo);
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


}
