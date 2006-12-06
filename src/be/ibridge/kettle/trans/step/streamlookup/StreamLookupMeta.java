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
 * Created on 02-jun-2003
 *
 */

package be.ibridge.kettle.trans.step.streamlookup;

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
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


public class StreamLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	/**fields in input  streams with which we look up values*/
	private String keystream[];         
	
	/**fields in lookup stream  with which we look up values*/
	private String keylookup[];        
	
	/**return these field values from lookup*/
	private String value[];              
	
	/**rename to this after lookup*/
	private String valueName[];           
	
	/**default value in case not found...*/
	private String valueDefault[];            
	
	/**type of default value*/
	private int    valueDefaultType[];         
	
	/**Indicate that the input is considered sorted!*/
	private boolean inputSorted;          

    /**Indicate that we need to preserve memory by serializing objects */
    private boolean memoryPreservationActive;          

    /**Indicate that we want to use a sorted list vs. a hashtable */
    private boolean usingSortedList;          

	/**Which step is providing the lookup data?*/
	private StepMeta lookupFromStep;
	
	/**Which step is providing the lookup data?*/
	private String   lookupFromStepname;
	
	public StreamLookupMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
	 * @return Returns the lookupFromStep.
	 */
	public StepMeta getLookupFromStep()
	{
		return lookupFromStep;
	}
	
	/**
	 * @param lookupFromStep The lookupFromStep to set.
	 */
	public void setLookupFromStep(StepMeta lookupFromStep)
	{
		this.lookupFromStep = lookupFromStep;
	}

		
    /**
     * @return Returns the inputSorted.
     */
    public boolean isInputSorted()
    {
        return inputSorted;
    }
    
    /**
     * @param inputSorted The inputSorted to set.
     */
    public void setInputSorted(boolean inputSorted)
    {
        this.inputSorted = inputSorted;
    }
    
    /**
     * @return Returns the keylookup.
     */
    public String[] getKeylookup()
    {
        return keylookup;
    }
    
    /**
     * @param keylookup The keylookup to set.
     */
    public void setKeylookup(String[] keylookup)
    {
        this.keylookup = keylookup;
    }
    
    /**
     * @return Returns the keystream.
     */
    public String[] getKeystream()
    {
        return keystream;
    }
    
    /**
     * @param keystream The keystream to set.
     */
    public void setKeystream(String[] keystream)
    {
        this.keystream = keystream;
    }
    
    /**
     * @return Returns the value.
     */
    public String[] getValue()
    {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String[] value)
    {
        this.value = value;
    }
    
    /**
     * @return Returns the valueDefault.
     */
    public String[] getValueDefault()
    {
        return valueDefault;
    }
    
    /**
     * @param valueDefault The valueDefault to set.
     */
    public void setValueDefault(String[] valueDefault)
    {
        this.valueDefault = valueDefault;
    }
    
    /**
     * @return Returns the valueDefaultType.
     */
    public int[] getValueDefaultType()
    {
        return valueDefaultType;
    }
    
    /**
     * @param valueDefaultType The valueDefaultType to set.
     */
    public void setValueDefaultType(int[] valueDefaultType)
    {
        this.valueDefaultType = valueDefaultType;
    }
    
    /**
     * @return Returns the valueName.
     */
    public String[] getValueName()
    {
        return valueName;
    }
    
    /**
     * @param valueName The valueName to set.
     */
    public void setValueName(String[] valueName)
    {
        this.valueName = valueName;
    }
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}
	
	public void allocate(int nrkeys, int nrvalues)
	{
		keystream    	= new String[nrkeys];
		keylookup    	= new String[nrkeys];
		
		value        	= new String[nrvalues];
		valueName    	= new String[nrvalues];
		valueDefault    = new String[nrvalues];
		valueDefaultType= new int   [nrvalues];
	}

	public Object clone()
	{
		StreamLookupMeta retval = (StreamLookupMeta)super.clone();

		int nrkeys   = keystream.length;
		int nrvalues = value.length;

		retval.allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			retval.keystream[i] = keystream[i];
			retval.keylookup[i] = keylookup[i];
		}

		for (int i=0;i<nrvalues;i++)
		{
			retval.value[i]            = value[i];
			retval.valueName[i]    	   = valueName[i];
			retval.valueDefault[i]     = valueDefault[i]; 
			retval.valueDefaultType[i] = valueDefaultType[i];
		}
		
		return retval;
	}
	
	/**
	 * @return the informational source steps, if any. Null is the default: none.
	 */
	public String[] getInfoSteps()
	{
	    if (getLookupStepname()!=null) return new String[] { getLookupStepname() };
	    return null;
	}
    
    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
        if (infoSteps!=null && infoSteps.length>0)
        {
            lookupFromStep = infoSteps[0];
        }
    }

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			String dtype;
			int nrkeys, nrvalues;
			
			lookupFromStepname = XMLHandler.getTagValue(stepnode, "from"); //$NON-NLS-1$
			lookupFromStep = null;
            
            inputSorted = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "input_sorted")); //$NON-NLS-1$ //$NON-NLS-2$
            memoryPreservationActive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "preserve_memory")); //$NON-NLS-1$ //$NON-NLS-2$
            usingSortedList = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "sorted_list")); //$NON-NLS-1$ //$NON-NLS-2$
			
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
			nrkeys   = XMLHandler.countNodes(lookup, "key"); //$NON-NLS-1$
			nrvalues = XMLHandler.countNodes(lookup, "value"); //$NON-NLS-1$
	
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i); //$NON-NLS-1$
				
				keystream[i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				keylookup[i] = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i); //$NON-NLS-1$
				
				value[i]        = XMLHandler.getTagValue(vnode, "name"); //$NON-NLS-1$
				valueName[i]    = XMLHandler.getTagValue(vnode, "rename"); //$NON-NLS-1$
				if (valueName[i]==null) valueName[i]=value[i]; // default: same name to return!
				valueDefault[i]     = XMLHandler.getTagValue(vnode, "default"); //$NON-NLS-1$
				dtype           = XMLHandler.getTagValue(vnode, "type"); //$NON-NLS-1$
				valueDefaultType[i] = Value.getType(dtype);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("StreamLookupMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		int nrkeys, nrvalues;
		
		keystream=null;
		value=null;
		valueDefault=null;
		
		lookupFromStepname = null;
		lookupFromStep = null;
        
        memoryPreservationActive = true;
        usingSortedList = true;
		
		nrkeys   = 0;
		nrvalues = 0;

		allocate(nrkeys, nrvalues);

		for (int i=0;i<nrkeys;i++)
		{
			keystream[i] = "key"+i; //$NON-NLS-1$
			keylookup[i] = "keyfield"+i; //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			value[i]        = "value"+i; //$NON-NLS-1$
			valueName[i]    = "valuename"+i; //$NON-NLS-1$
			valueDefault[i]     = "default"+i; //$NON-NLS-1$
			valueDefaultType[i] = Value.VALUE_TYPE_NUMBER;
		}
	}
	
	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		if (info!=null && info.size()!=0)
		{
			for (int i=0;i<valueName.length;i++)
			{
				Value v = info.searchValue(value[i]);
				if (v!=null) // Configuration error/missing resources...
				{
					v.setName(valueName[i]);
					v.setOrigin(name);
					row.addValue(v);
				}
				else
				{
					throw new KettleStepException(Messages.getString("StreamLookupMeta.Exception.ReturnValueCanNotBeFound",value[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else
		{
			for (int i=0;i<valueName.length;i++)
			{
				Value v=new Value(valueName[i], valueDefaultType[i]);
				v.setOrigin(name);
				row.addValue(v);		
			}
		}

		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("from", lookupFromStep!=null?lookupFromStep.getName():"")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    "+XMLHandler.addTagValue("input_sorted", inputSorted)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("preserve_memory", memoryPreservationActive)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("sorted_list", usingSortedList)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <lookup>"+Const.CR); //$NON-NLS-1$
		for (int i=0;i<keystream.length;i++)
		{
			retval.append("      <key>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",  keystream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("field", keylookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </key>"+Const.CR); //$NON-NLS-1$
		}
		
		for (int i=0;i<value.length;i++)
		{
			retval.append("      <value>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",    value[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("rename",  valueName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("default", valueDefault[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("type",    Value.getTypeDesc(valueDefaultType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </value>"+Const.CR); //$NON-NLS-1$
		}
		retval.append("    </lookup>"+Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			lookupFromStepname =  rep.getStepAttributeString (id_step, "lookup_from_step"); //$NON-NLS-1$
			lookupFromStep = null;
            inputSorted = rep.getStepAttributeBoolean(id_step, "input_sorted"); //$NON-NLS-1$
			memoryPreservationActive = rep.getStepAttributeBoolean(id_step, "preserve_memory"); // $NON-NLS-1$
            usingSortedList = rep.getStepAttributeBoolean(id_step, "sorted_list"); // $NON-NLS-1$
            
			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name"); //$NON-NLS-1$
			int nrvalues = rep.countNrStepAttributes(id_step, "return_value_name"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keystream[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name"); //$NON-NLS-1$
				keylookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				value[i]        =                rep.getStepAttributeString(id_step, i, "return_value_name"); //$NON-NLS-1$
				valueName[i]    =                rep.getStepAttributeString(id_step, i, "return_value_rename"); //$NON-NLS-1$
				valueDefault[i]     =                rep.getStepAttributeString(id_step, i, "return_value_default"); //$NON-NLS-1$
				valueDefaultType[i] = Value.getType( rep.getStepAttributeString(id_step, i, "return_value_type")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("StreamLookupMeta.Exception.UnexpecteErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "lookup_from_step",  lookupFromStep!=null?lookupFromStep.getName():""); //$NON-NLS-1$ //$NON-NLS-2$
            rep.saveStepAttribute(id_transformation, id_step, "input_sorted", inputSorted); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "preserve_memory", memoryPreservationActive); // $NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "sorted_list", usingSortedList); // $NON-NLS-1$
            
            for (int i=0;i<keystream.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      keystream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     keylookup[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<value.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_name",      value[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_rename",    valueName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_default",   valueDefault[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      Value.getTypeDesc(valueDefaultType[i])); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("StreamLookupMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}


	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String  error_message=""; //$NON-NLS-1$
			boolean error_found=false;
			
			// Starting from selected fields in ...
			// Check the fields from the previous stream! 
			for (int i=0;i< keystream.length;i++)
			{
				int idx = prev.searchValueIndex(keystream[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keystream[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=Messages.getString("StreamLookupMeta.CheckResult.FieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("StreamLookupMeta.CheckResult.CouldNotFindFieldsFromPreviousSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}

		if (info!=null && info.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.StepReceivingLookupData",info.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String  error_message=""; //$NON-NLS-1$
			boolean error_found=false;

			// Check the fields from the lookup stream! 
			for (int i=0;i< keylookup.length;i++)
			{
				int idx = info.searchValueIndex(keylookup[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keylookup[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=Messages.getString("StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}

			// Check the values to retrieve from the lookup stream! 
			for (int i=0;i< value.length;i++)
			{
				int idx = info.searchValueIndex(value[i]);
				if (idx<0)
				{
					error_message+="\t\t"+value[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=Messages.getString("StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream2")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream2"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("StreamLookupMeta.CheckResult.FieldsNotFoundFromInLookupSep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		// See if the source step is filled in!
		if (lookupFromStep==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("StreamLookupMeta.CheckResult.SourceStepNotSelected"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.SourceStepIsSelected"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
			
			// See if the step exists!
			//
			if (info!=null)
			{	
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.SourceStepExist",lookupFromStep+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("StreamLookupMeta.CheckResult.SourceStepDoesNotExist",lookupFromStep+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>=2)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("StreamLookupMeta.CheckResult.StepReceivingInfoFromInputSteps",input.length+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("StreamLookupMeta.CheckResult.NeedAtLeast2InputStreams",Const.CR,Const.CR), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			remarks.add(cr);
		}
	}
	
	public StepMeta getLookupStep()
	{
		return lookupFromStep;
	}
	
	public String getLookupStepname()
	{
		if (lookupFromStep!=null && 
			lookupFromStep.getName()!=null &&
			lookupFromStep.getName().length()>0
		   ) 
			return lookupFromStep.getName();
		return null;
	}

	public void searchInfoAndTargetSteps(ArrayList steps)
	{
		lookupFromStep = TransMeta.findStep(steps, lookupFromStepname);
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new StreamLookupDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new StreamLookup(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new StreamLookupData();
	}

    public boolean isMemoryPreservationActive()
    {
        return memoryPreservationActive;
    }

    public void setMemoryPreservationActive(boolean memoryPreservationActive)
    {
        this.memoryPreservationActive = memoryPreservationActive;
    }

    public boolean isUsingSortedList()
    {
        return usingSortedList;
    }

    public void setUsingSortedList(boolean usingSortedList)
    {
        this.usingSortedList = usingSortedList;
    }

}
