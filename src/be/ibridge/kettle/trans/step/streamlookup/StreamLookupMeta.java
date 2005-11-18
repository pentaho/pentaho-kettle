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

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			String dtype;
			int nrkeys, nrvalues;
			
			lookupFromStepname = XMLHandler.getTagValue(stepnode, "from");
			lookupFromStep = null;
            
            inputSorted = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "input_sorted"));
			
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup");
			nrkeys   = XMLHandler.countNodes(lookup, "key");
			nrvalues = XMLHandler.countNodes(lookup, "value");
	
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i);
				
				keystream[i] = XMLHandler.getTagValue(knode, "name");
				keylookup[i] = XMLHandler.getTagValue(knode, "field");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i);
				
				value[i]        = XMLHandler.getTagValue(vnode, "name");
				valueName[i]    = XMLHandler.getTagValue(vnode, "rename");
				if (valueName[i]==null) valueName[i]=value[i]; // default: same name to return!
				valueDefault[i]     = XMLHandler.getTagValue(vnode, "default");
				dtype           = XMLHandler.getTagValue(vnode, "type");
				valueDefaultType[i] = Value.getType(dtype);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
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
		
		nrkeys   = 0;
		nrvalues = 0;

		allocate(nrkeys, nrvalues);

		for (int i=0;i<nrkeys;i++)
		{
			keystream[i] = "key"+i;
			keylookup[i] = "keyfield"+i;
		}

		for (int i=0;i<nrvalues;i++)
		{
			value[i]        = "value"+i;
			valueName[i]    = "valuename"+i;
			valueDefault[i]     = "default"+i;
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
					throw new KettleStepException("Return value "+value[i]+" can't be found in the input row.");
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
		String retval="";
		int i;
		
		retval+="    "+XMLHandler.addTagValue("from", lookupFromStep!=null?lookupFromStep.getName():"");
        retval+="    "+XMLHandler.addTagValue("input_sorted", inputSorted);

		retval+="    <lookup>"+Const.CR;
		for (i=0;i<keystream.length;i++)
		{
			retval+="      <key>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",  keystream[i]);
			retval+="        "+XMLHandler.addTagValue("field", keylookup[i]);
			retval+="      </key>"+Const.CR;
		}
		
		for (i=0;i<value.length;i++)
		{
			retval+="      <value>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",    value[i]);
			retval+="        "+XMLHandler.addTagValue("rename",  valueName[i]);
			retval+="        "+XMLHandler.addTagValue("default", valueDefault[i]);
			retval+="        "+XMLHandler.addTagValue("type",    Value.getTypeDesc(valueDefaultType[i]));
			retval+="      </value>"+Const.CR;
		}
		retval+="    </lookup>"+Const.CR;
		
		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			lookupFromStepname =  rep.getStepAttributeString (id_step, "lookup_from_step");
			lookupFromStep = null;
            inputSorted = rep.getStepAttributeBoolean(id_step, "input_sorted");
			
			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name");
			int nrvalues = rep.countNrStepAttributes(id_step, "return_value_name");
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keystream[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name");
				keylookup[i] = rep.getStepAttributeString(id_step, i, "lookup_key_field");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				value[i]        =                rep.getStepAttributeString(id_step, i, "return_value_name");
				valueName[i]    =                rep.getStepAttributeString(id_step, i, "return_value_rename");
				valueDefault[i]     =                rep.getStepAttributeString(id_step, i, "return_value_default");
				valueDefaultType[i] = Value.getType( rep.getStepAttributeString(id_step, i, "return_value_type"));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "lookup_from_step",  lookupFromStep!=null?lookupFromStep.getName():"");
            rep.saveStepAttribute(id_transformation, id_step, "input_sorted", inputSorted);
            
            for (int i=0;i<keystream.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      keystream[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     keylookup[i]);
			}
	
			for (int i=0;i<value.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_name",      value[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_rename",    valueName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_default",   valueDefault[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      Value.getTypeDesc(valueDefaultType[i]));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}


	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
			remarks.add(cr);
			
			String  error_message="";
			boolean error_found=false;
			
			// Starting from selected fields in ...
			// Check the fields from the previous stream! 
			for (int i=0;i< keystream.length;i++)
			{
				int idx = prev.searchValueIndex(keystream[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keystream[i]+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message="Fields that were not found in input stream:"+Const.CR+Const.CR+error_message;

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields are found in the input stream.", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Couldn't find fields from previous steps, check the hops...!", stepMeta);
			remarks.add(cr);
		}

		if (info!=null && info.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving lookup data to other step, receiving "+info.size()+" fields", stepMeta);
			remarks.add(cr);
			
			String  error_message="";
			boolean error_found=false;

			// Check the fields from the lookup stream! 
			for (int i=0;i< keylookup.length;i++)
			{
				int idx = info.searchValueIndex(keylookup[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keylookup[i]+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message="Fields that were not found in lookup stream:"+Const.CR+Const.CR+error_message;

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields are found in the lookup stream.", stepMeta);
				remarks.add(cr);
			}

			// Check the values to retrieve from the lookup stream! 
			for (int i=0;i< value.length;i++)
			{
				int idx = info.searchValueIndex(value[i]);
				if (idx<0)
				{
					error_message+="\t\t"+value[i]+Const.CR;
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message="Fields to retrieve that were not found in lookup stream:"+Const.CR+Const.CR+error_message;

				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields to retrieve are found in the input lookup stream.", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Couldn't find fields from lookup steps, check the hops...!", stepMeta);
			remarks.add(cr);
		}
		
		// See if the source step is filled in!
		if (lookupFromStep==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Source step is not selected!", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Source step is selected.", stepMeta);
			remarks.add(cr);
			
			// See if the step exists!
			//
			if (info!=null)
			{	
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Source step ["+lookupFromStep+"] exist!", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Source step ["+lookupFromStep+"] doesn't exist!", stepMeta);
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>=2)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from "+input.length+" input steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "We need at least 2 input streams:"+Const.CR+"  1 (or more) input stream for the data"+Const.CR+" 1 input stream for the lookup data", stepMeta);
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

}
