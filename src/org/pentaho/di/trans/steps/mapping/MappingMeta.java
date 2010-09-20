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
 
package org.pentaho.di.trans.steps.mapping;

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
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInputMeta;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutputMeta;
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
    
    /*
     * This repository object is injected from the outside at runtime or at design time.
     * It comes from either Spoon or Trans
     */
    private Repository repository;

	public MappingMeta()
	{
		super(); // allocate BaseStepMeta
		
		inputMappings = new ArrayList<MappingIODefinition>();
    	outputMappings = new ArrayList<MappingIODefinition>();
    	mappingParameters = new MappingParameters();
	}
 
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
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
        	int nrOutputMappings = XMLHandler.countNodes(outputNode, MappingIODefinition.XML_TAG); //$NON-NLS-1$
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
	        inputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrInput; i++) {
				Node inputConnector = XMLHandler.getSubNodeByNr(inputNode, "connector", i); //$NON-NLS-1$
				String inputField = XMLHandler.getTagValue(inputConnector, "field"); //$NON-NLS-1$
				String inputMapping = XMLHandler.getTagValue(inputConnector, "mapping"); //$NON-NLS-1$
				inputMappingDefinition.getValueRenames().add( new MappingValueRename(inputField, inputMapping) );
			}

	        MappingIODefinition outputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        outputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrOutput; i++) {
				Node outputConnector = XMLHandler.getSubNodeByNr(outputNode, "connector", i); //$NON-NLS-1$
				String outputField = XMLHandler.getTagValue(outputConnector, "field"); //$NON-NLS-1$
				String outputMapping = XMLHandler.getTagValue(outputConnector, "mapping"); //$NON-NLS-1$
				outputMappingDefinition.getValueRenames().add( new MappingValueRename(outputMapping, outputField) );
			}
	        
	        // Don't forget to add these to the input and output mapping definitions...
	        //
	        inputMappings.add(inputMappingDefinition);
	        outputMappings.add(outputMappingDefinition);
	        
	        // The default is to have no mapping parameters: the concept didn't exist before.
	        mappingParameters = new MappingParameters();
        }
	}


    public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);
        
        retval.append("    ").append(XMLHandler.addTagValue("trans_name", transName) ); //$NON-NLS-1$
        retval.append("    ").append(XMLHandler.addTagValue("filename", fileName )); //$NON-NLS-1$
        retval.append("    ").append(XMLHandler.addTagValue("directory_path", directoryPath )); //$NON-NLS-1$
        
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

        // Add the mapping parameters too
        //
        retval.append("      ").append(mappingParameters.getXML()).append(Const.CR); //$NON-NLS-1$
        
        retval.append("    ").append(XMLHandler.closeTag("mappings")).append(Const.CR); //$NON-NLS-1$ $NON-NLS-2$
        
        return retval.toString();
    }
    
    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
        transName        = rep.getStepAttributeString(id_step, "trans_name"); //$NON-NLS-1$
        fileName         = rep.getStepAttributeString(id_step, "filename"); //$NON-NLS-1$
        directoryPath    = rep.getStepAttributeString(id_step, "directory_path"); //$NON-NLS-1$
        
        int nrInput  = rep.countNrStepAttributes(id_step, "input_field"); //$NON-NLS-1$
        int nrOutput = rep.countNrStepAttributes(id_step, "output_field"); //$NON-NLS-1$

        // Backward compatibility...
        //
        if (nrInput>0 || nrOutput>0)
        {
	        MappingIODefinition inputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        inputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrInput; i++) {
	        	String inputField   = rep.getStepAttributeString(id_step, i, "input_field"); //$NON-NLS-1$
	            String inputMapping = rep.getStepAttributeString(id_step, i, "input_mapping"); //$NON-NLS-1$
				inputMappingDefinition.getValueRenames().add( new MappingValueRename(inputField, inputMapping) );
			}

	        MappingIODefinition outputMappingDefinition = new MappingIODefinition(); // null means: auto-detect
	        outputMappingDefinition.setMainDataPath(true);
	        
	        for (int i = 0; i < nrOutput; i++) {
	        	String outputField   = rep.getStepAttributeString(id_step, i, "output_field"); //$NON-NLS-1$
	            String outputMapping = rep.getStepAttributeString(id_step, i, "output_mapping"); //$NON-NLS-1$
				outputMappingDefinition.getValueRenames().add( new MappingValueRename(outputMapping, outputField) );
			}
	        
	        // Don't forget to add these to the input and output mapping definitions...
	        //
	        inputMappings.add(inputMappingDefinition);
	        outputMappings.add(outputMappingDefinition);
	        
	        // The default is to have no mapping parameters: the concept didn't exist before.
	        mappingParameters = new MappingParameters();
        }
        else
        {
        	nrInput  = rep.countNrStepAttributes(id_step, "input_input_step"); //$NON-NLS-1$
        	nrOutput = rep.countNrStepAttributes(id_step, "output_input_step"); //$NON-NLS-1$
        	
        	for (int i=0;i<nrInput;i++) {
        		inputMappings.add( new MappingIODefinition(rep, id_step, "input_", i) );
        	}

        	for (int i=0;i<nrOutput;i++) {
        		outputMappings.add( new MappingIODefinition(rep, id_step, "output_", i) );
        	}

        	mappingParameters = new MappingParameters(rep, id_step);
        }
        
	}
    
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, "filename", fileName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, "trans_name", transName); //$NON-NLS-1$
        
		// Verify import from repository explorer into different directory...
        //
		if (rep.getImportBaseDirectory()!=null && !rep.getImportBaseDirectory().isRoot()) {
			directoryPath = rep.getImportBaseDirectory().getPath()+directoryPath;
		}

		// Now we can save it with the correct reference...
		//
        rep.saveStepAttribute(id_transformation, id_step, "directory_path", directoryPath); //$NON-NLS-1$

        for (int i=0;i<inputMappings.size();i++)
        {
        	inputMappings.get(i).saveRep(rep, id_transformation, id_step, "input_", i);
        }

        for (int i=0;i<outputMappings.size();i++)
        {
        	outputMappings.get(i).saveRep(rep, id_transformation, id_step, "output_", i);
        }
        
        // save the mapping parameters too
        //
        mappingParameters.saveRep(rep, id_transformation, id_step);

    }


	public void setDefault()
	{
	}
	
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException {
    	// First load some interesting data...
    	
    	// Then see which fields get added to the row.
    	//
        TransMeta mappingTransMeta = null;
        try
        {
            mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository, space);
        }
        catch(KettleException e)
        {
            throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToLoadMappingTransformation"), e);
        }
        
        // Keep track of all the fields that need renaming...
        //
        List<MappingValueRename> inputRenameList = new ArrayList<MappingValueRename>();
        
        /*
         * Before we ask the mapping outputs anything, we should teach the mapping input steps in the sub-transformation
         * about the data coming in...
         */
        for (MappingIODefinition definition : inputMappings) {
        	
        	RowMetaInterface inputRowMeta;
        	
        	if (definition.isMainDataPath() || Const.isEmpty(definition.getInputStepname()) ) {
        		// The row metadata, what we pass to the mapping input step definition.getOutputStep(), is "row"
        		// However, we do need to re-map some fields...
        		// 
        		inputRowMeta = row.clone();
        		for (MappingValueRename valueRename : definition.getValueRenames()) {
        			ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta(valueRename.getSourceValueName());
        			if (valueMeta==null) {
        				throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindField", valueRename.getSourceValueName()));
        			}
        			valueMeta.setName(valueRename.getTargetValueName());
        		}
        	}
        	else {
        		// The row metadata that goes to the info mapping input comes from the specified step
        		// In fact, it's one of the info steps that is going to contain this information...
        		//
        		String[] infoSteps = getInfoSteps();
        		int infoStepIndex = Const.indexOfString(definition.getInputStepname(), infoSteps);
        	    if (infoStepIndex<0) {
        	    	throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindMetadataInfo", definition.getInputStepname()));
        	    }
        	    inputRowMeta = info[infoStepIndex].clone();
        	}
        	
    		// What is this mapping input step?
    		//
    		StepMeta mappingInputStep = mappingTransMeta.findMappingInputStep(definition.getOutputStepname());
    		
    		// We're certain it's a MappingInput step...
    		//
    		MappingInputMeta mappingInputMeta = (MappingInputMeta) mappingInputStep.getStepMetaInterface();

    		// Inform the mapping input step about what it's going to receive...
    		//
    		mappingInputMeta.setInputRowMeta(inputRowMeta);
    		
    		// What values are we changing names for?
    		//
    		mappingInputMeta.setValueRenames(definition.getValueRenames());
    		
    		// Keep a list of the input rename values that need to be changed back at the output
    		// 
    		if (definition.isRenamingOnOutput()) Mapping.addInputRenames(inputRenameList, definition.getValueRenames());
        }
        
        // All the mapping steps now know what they will be receiving.
        // That also means that the sub-transformation / mapping has everything it needs.
        // So that means that the MappingOutput steps know exactly what the output is going to be.
        // That could basically be anything.
        // It also could have absolutely no resemblance to what came in on the input.
        // The relative old approach is therefore no longer suited.
        // 
        // OK, but what we *can* do is have the MappingOutput step rename the appropriate fields.
        // The mapping step will tell this step how it's done.
        //
        // Let's look for the mapping output step that is relevant for this actual call...
        //
        MappingIODefinition mappingOutputDefinition = null;
    	if (nextStep==null) {
    		// This is the main step we read from...
    		// Look up the main step to write to.
    		// This is the output mapping definition with "main path" enabled.
    		//
    		for (MappingIODefinition definition : outputMappings) {
    			if (definition.isMainDataPath() || Const.isEmpty(definition.getOutputStepname())) {
    				// This is the definition to use...
    				//
    				mappingOutputDefinition = definition;
    			}
    		}
    	}
    	else {
    		// Is there an output mapping definition for this step?
    		// If so, we can look up the Mapping output step to see what has changed.
    		//
    		
    		for (MappingIODefinition definition : outputMappings) {
    			if (nextStep.getName().equals(definition.getOutputStepname()) || 
    			    definition.isMainDataPath() || 
    			    Const.isEmpty(definition.getOutputStepname())
    			    ) {
    				mappingOutputDefinition = definition;
    			}
    		}
    	}
    	
    	if (mappingOutputDefinition==null) {
    		throw new KettleStepException(Messages.getString("MappingMeta.Exception.UnableToFindMappingDefinition"));
    	}
    		
		// OK, now find the mapping output step in the mapping...
		// This method in TransMeta takes into account a number of things, such as the step not specified, etc.
		// The method never returns null but throws an exception.
		//
		StepMeta mappingOutputStep = mappingTransMeta.findMappingOutputStep(mappingOutputDefinition.getInputStepname());
		
		// We know it's a mapping output step...
		MappingOutputMeta mappingOutputMeta = (MappingOutputMeta) mappingOutputStep.getStepMetaInterface();

		// Change a few columns.
		mappingOutputMeta.setOutputValueRenames(mappingOutputDefinition.getValueRenames());
		
		// Perhaps we need to change a few input columns back to the original?
		//
		mappingOutputMeta.setInputValueRenames(inputRenameList);
		
		// Now we know wat's going to come out of there...
		// This is going to be the full row, including all the remapping, etc.
		//
		RowMetaInterface mappingOutputRowMeta = mappingTransMeta.getStepFields(mappingOutputStep);
		
		row.clear();
		row.addRowMeta(mappingOutputRowMeta);
    }
    
    @Override
    public String[] getInfoSteps() {

    	List<String> infoSteps = new ArrayList<String>();
    	// The infosteps are those steps that are specified in the input mappings
    	for (MappingIODefinition definition : inputMappings) {
    		if (!definition.isMainDataPath() && !Const.isEmpty(definition.getInputStepname())) {
    			infoSteps.add(definition.getInputStepname());
    		}
    	}
    	if (infoSteps.isEmpty()) return null;

    	return infoSteps.toArray(new String[infoSteps.size()]);
    }
    
    @Override
    public String[] getTargetSteps() {

    	List<String> targetSteps = new ArrayList<String>();
    	// The infosteps are those steps that are specified in the input mappings
    	for (MappingIODefinition definition : outputMappings) {
    		if (!definition.isMainDataPath() && !Const.isEmpty(definition.getOutputStepname())) {
    			targetSteps.add(definition.getOutputStepname());
    		}
    	}
    	if (targetSteps.isEmpty()) return null;

    	return targetSteps.toArray(new String[targetSteps.size()]);
    }

    public synchronized static final TransMeta loadMappingMeta(String fileName, String transName, String directoryPath, Repository rep, VariableSpace space) throws KettleException
    {
        TransMeta mappingTransMeta = null;
        
        String realFilename = space.environmentSubstitute(fileName);
        String realTransname = space.environmentSubstitute(transName);
        
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
                // LogWriter.getInstance().logError("Loading Mapping from XML", "Unable to load transformation ["+realFilename+"] : "+e.toString());
                // LogWriter.getInstance().logError("Loading Mapping from XML", Const.getStackTracker(e));
            	
                throw new KettleException(Messages.getString("MappingMeta.Exception.UnableToLoadMapping"), e);
            }
        }
        else
        {
            // OK, load the meta-data from the repository...
            if (!Const.isEmpty(realTransname) && directoryPath!=null && rep!=null)
            {
                RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(space.environmentSubstitute(directoryPath));
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
	

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("MappingMeta.CheckResult.NotReceivingAnyFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFields",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingMeta.CheckResult.StepReceivingFieldsFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
        
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
	
  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
     List<ResourceReference> references = new ArrayList<ResourceReference>(5);
     String realFilename = transMeta.environmentSubstitute(fileName);
     String realTransname = transMeta.environmentSubstitute(transName);
     ResourceReference reference = new ResourceReference(stepInfo);
     references.add(reference);
     
     if (!Const.isEmpty(realFilename)) {
       // Add the filename to the references, including a reference to this step meta data.
       //
       reference.getEntries().add( new ResourceEntry(realFilename, ResourceType.ACTIONFILE));
     } else if (!Const.isEmpty(realTransname)) {
       // Add the filename to the references, including a reference to this step meta data.
       //
       reference.getEntries().add( new ResourceEntry(realTransname, ResourceType.ACTIONFILE));
       references.add(reference);
     }
     return references;
  }
  
	@Override
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// Try to load the transformation from repository or file.
			// Modify this recursively too...
			// 
			// NOTE: there is no need to clone this step because the caller is
			// responsible for this.
			//
			// First load the mapping metadata...
			//
			TransMeta mappingTransMeta = loadMappingMeta(fileName, transName, directoryPath, repository, space);

			// Also go down into the mapping transformation and export the files
			// there. (mapping recursively down)
			//
			String proposedNewFilename = mappingTransMeta.exportResources(mappingTransMeta, definitions, resourceNamingInterface, repository);

			// To get a relative path to it, we inject
			// ${Internal.Job.Filename.Directory}
			//
			String newFilename = "${" + Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY + "}/" + proposedNewFilename;

			// Set the correct filename inside the XML.
			//
			mappingTransMeta.setFilename(newFilename);

			// change it in the job entry
			//
			fileName = newFilename;

			return proposedNewFilename;
		} catch (Exception e) {
			throw new KettleException(Messages.getString("MappingMeta.Exception.UnableToLoadTransformation", fileName)); //$NON-NLS-1$
		}
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
