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
 * Created on 02-jun-2003
 *
 */

package org.pentaho.di.trans.steps.streamlookup;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;



public class StreamLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = StreamLookupMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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

    /** The content of the key and lookup is a single Integer (long) */
    private boolean usingIntegerPair;          

	public StreamLookupMeta()
	{
		super(); // allocate BaseStepMeta
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
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
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
	
	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			String dtype;
			int nrkeys, nrvalues;
			
			String lookupFromStepname = XMLHandler.getTagValue(stepnode, "from"); //$NON-NLS-1$
			StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
			infoStream.setSubject(lookupFromStepname);
            
            inputSorted = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "input_sorted")); //$NON-NLS-1$ //$NON-NLS-2$
            memoryPreservationActive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "preserve_memory")); //$NON-NLS-1$ //$NON-NLS-2$
            usingSortedList = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "sorted_list")); //$NON-NLS-1$ //$NON-NLS-2$
            usingIntegerPair = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "integer_pair")); //$NON-NLS-1$ //$NON-NLS-2$
			
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
				valueDefaultType[i] = ValueMeta.getType(dtype);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "StreamLookupMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
			stream.setStepMeta( StepMeta.findStep(steps, (String)stream.getSubject()) );
		}
	}
	
	public void setDefault()
	{
		int nrkeys, nrvalues;
		
		keystream=null;
		value=null;
		valueDefault=null;
		
        memoryPreservationActive = true;
        usingSortedList = false;
        usingIntegerPair = false;
		
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
			valueDefaultType[i] = ValueMetaInterface.TYPE_NUMBER;
		}
	}
	
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		if (info!=null && info.length==1 && info[0]!=null)
		{
            for (int i=0;i<valueName.length;i++)
			{
				ValueMetaInterface v = info[0].searchValueMeta(value[i]);
				if (v!=null) // Configuration error/missing resources...
				{
					v.setName(valueName[i]);
					v.setOrigin(origin);
					v.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL); // Only normal storage goes into the cache
					row.addValueMeta(v);
				}
				else
				{
					throw new KettleStepException(BaseMessages.getString(PKG, "StreamLookupMeta.Exception.ReturnValueCanNotBeFound",value[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else
		{
			for (int i=0;i<valueName.length;i++)
			{
				ValueMetaInterface v=new ValueMeta(valueName[i], valueDefaultType[i]);
				v.setOrigin(origin);
				row.addValueMeta(v);		
			}
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
        StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
		retval.append("    "+XMLHandler.addTagValue("from", infoStream.getStepname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    "+XMLHandler.addTagValue("input_sorted", inputSorted)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("preserve_memory", memoryPreservationActive)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("sorted_list", usingSortedList)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("integer_pair", usingIntegerPair)); //$NON-NLS-1$ //$NON-NLS-2$

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
			retval.append("        "+XMLHandler.addTagValue("type",    ValueMeta.getTypeDesc(valueDefaultType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </value>"+Const.CR); //$NON-NLS-1$
		}
		retval.append("    </lookup>"+Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			String lookupFromStepname =  rep.getStepAttributeString (id_step, "lookup_from_step"); //$NON-NLS-1$
			StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
			infoStream.setSubject(lookupFromStepname);

            inputSorted = rep.getStepAttributeBoolean(id_step, "input_sorted"); //$NON-NLS-1$
			memoryPreservationActive = rep.getStepAttributeBoolean(id_step, "preserve_memory"); // $NON-NLS-1$
            usingSortedList = rep.getStepAttributeBoolean(id_step, "sorted_list"); // $NON-NLS-1$
            usingIntegerPair = rep.getStepAttributeBoolean(id_step, "integer_pair"); // $NON-NLS-1$
            
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
				valueDefaultType[i] = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "return_value_type")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "StreamLookupMeta.Exception.UnexpecteErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
	        StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
			rep.saveStepAttribute(id_transformation, id_step, "lookup_from_step",  infoStream.getStepname()); //$NON-NLS-1$ //$NON-NLS-2$
            rep.saveStepAttribute(id_transformation, id_step, "input_sorted", inputSorted); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "preserve_memory", memoryPreservationActive); // $NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "sorted_list", usingSortedList); // $NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "integer_pair", usingIntegerPair); // $NON-NLS-1$
            
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
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      ValueMeta.getTypeDesc(valueDefaultType[i])); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "StreamLookupMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String  error_message=""; //$NON-NLS-1$
			boolean error_found=false;
			
			// Starting from selected fields in ...
			// Check the fields from the previous stream! 
			for (int i=0;i< keystream.length;i++)
			{
				int idx = prev.indexOfValue(keystream[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keystream[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.FieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.CouldNotFindFieldsFromPreviousSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}

		if (info!=null && info.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.StepReceivingLookupData",info.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String  error_message=""; //$NON-NLS-1$
			boolean error_found=false;

			// Check the fields from the lookup stream! 
			for (int i=0;i< keylookup.length;i++)
			{
				int idx = info.indexOfValue(keylookup[i]);
				if (idx<0)
				{
					error_message+="\t\t"+keylookup[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}

			// Check the values to retrieve from the lookup stream! 
			for (int i=0;i< value.length;i++)
			{
				int idx = info.indexOfValue(value[i]);
				if (idx<0)
				{
					error_message+="\t\t"+value[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream2")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream2"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundFromInLookupSep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		// See if the source step is filled in!
        StreamInterface infoStream = getStepIOMeta().getInfoStreams().get(0);
		if (infoStream.getStepMeta()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.SourceStepNotSelected"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.SourceStepIsSelected"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
			
			// See if the step exists!
			//
			if (info!=null)
			{	
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.SourceStepExist",infoStream.getStepname()), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.SourceStepDoesNotExist",infoStream.getStepname()), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}
		
		// See if we have input streams leading to this step!
		if (input.length>=2)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.StepReceivingInfoFromInputSteps",input.length+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "StreamLookupMeta.CheckResult.NeedAtLeast2InputStreams",Const.CR,Const.CR), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			remarks.add(cr);
		}
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

    /**
     * @return the usingIntegerPair
     */
    public boolean isUsingIntegerPair()
    {
        return usingIntegerPair;
    }

    /**
     * @param usingIntegerPair the usingIntegerPair to set
     */
    public void setUsingIntegerPair(boolean usingIntegerPair)
    {
        this.usingIntegerPair = usingIntegerPair;
    }
    
    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }
	
	/**
     * Returns the Input/Output metadata for this step.
     * The generator step only produces output, does not accept input!
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, true, false, false, false, false);
    	
	    	StreamInterface stream = new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "StreamLookupMeta.InfoStream.Description"), StreamIcon.INFO, null);
	    	ioMeta.addStream(stream);
    	}
    	
    	return ioMeta;
    }
    
    public void resetStepIoMeta() {
    	// Do nothing, don't reset as there is no need to do this.
    };
}
