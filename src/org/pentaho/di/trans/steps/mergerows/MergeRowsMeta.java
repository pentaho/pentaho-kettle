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

package org.pentaho.di.trans.steps.mergerows;

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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
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

/*
 * Created on 02-jun-2003
 *
 */

public class MergeRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = MergeRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String flagField;

    private String   keyFields[];
    private String   valueFields[];

	/**
     * @return Returns the keyFields.
     */
    public String[] getKeyFields()
    {
        return keyFields;
    }

    /**
     * @param keyFields The keyFields to set.
     */
    public void setKeyFields(String[] keyFields)
    {
        this.keyFields = keyFields;
    }

    /**
     * @return Returns the valueFields.
     */
    public String[] getValueFields()
    {
        return valueFields;
    }

    /**
     * @param valueFields The valueFields to set.
     */
    public void setValueFields(String[] valueFields)
    {
        this.valueFields = valueFields;
    }

    public MergeRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

    /**
     * @return Returns the flagField.
     */
    public String getFlagField()
    {
        return flagField;
    }

    /**
     * @param flagField The flagField to set.
     */
    public void setFlagField(String flagField)
    {
        this.flagField = flagField;
    }

	public void allocate(int nrKeys, int nrValues)
	{
        keyFields = new String[nrKeys];
        valueFields = new String[nrValues];
	}

	public Object clone()
	{
		MergeRowsMeta retval = (MergeRowsMeta)super.clone();

        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

        retval.append("    <keys>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<keyFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
        }
        retval.append("    </keys>"+Const.CR); //$NON-NLS-1$
        
        retval.append("    <values>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<valueFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("value", valueFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
        }
        retval.append("    </values>"+Const.CR); //$NON-NLS-1$

        retval.append(XMLHandler.addTagValue("flag_field", flagField));         //$NON-NLS-1$

        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
		retval.append(XMLHandler.addTagValue("reference", infoStreams.get(0).getStepname()));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("compare", infoStreams.get(1).getStepname()));		 //$NON-NLS-1$
		retval.append("    <compare>"+Const.CR); //$NON-NLS-1$
				
		retval.append("    </compare>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
            
            Node keysnode   = XMLHandler.getSubNode(stepnode, "keys"); //$NON-NLS-1$
            Node valuesnode = XMLHandler.getSubNode(stepnode, "values"); //$NON-NLS-1$
            
		    int nrKeys   = XMLHandler.countNodes(keysnode, "key"); //$NON-NLS-1$
            int nrValues = XMLHandler.countNodes(valuesnode, "value"); //$NON-NLS-1$
            
            allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysnode, "key", i); //$NON-NLS-1$
                keyFields[i] = XMLHandler.getNodeValue(keynode);
            }
            
            for (int i=0;i<nrValues;i++) 
            {
                Node valuenode = XMLHandler.getSubNodeByNr(valuesnode, "value", i); //$NON-NLS-1$
                valueFields[i] = XMLHandler.getNodeValue(valuenode);
            }
            
            flagField = XMLHandler.getTagValue(stepnode, "flag_field"); //$NON-NLS-1$
            
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface referenceStream = infoStreams.get(0);
            StreamInterface compareStream = infoStreams.get(1);

			compareStream.setSubject( XMLHandler.getTagValue(stepnode, "compare") ); //$NON-NLS-1$
			referenceStream.setSubject( XMLHandler.getTagValue(stepnode, "reference") ); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "MergeRowsMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
        flagField = "flagfield";
        allocate(0,0);
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
            int nrKeys = rep.countNrStepAttributes(id_step, "key_field"); //$NON-NLS-1$
            int nrValues = rep.countNrStepAttributes(id_step, "value_field"); //$NON-NLS-1$
            
			allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++)
            {
                keyFields[i] = rep.getStepAttributeString(id_step, i, "key_field"); //$NON-NLS-1$
            }
            for (int i=0;i<nrValues;i++)
            {
                valueFields[i] = rep.getStepAttributeString(id_step, i, "value_field"); //$NON-NLS-1$
            }

            flagField  =   rep.getStepAttributeString (id_step, "flag_field");  //$NON-NLS-1$

            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface referenceStream = infoStreams.get(0);
            StreamInterface compareStream = infoStreams.get(1);

			referenceStream.setSubject( rep.getStepAttributeString (id_step, "reference") );  //$NON-NLS-1$
			compareStream.setSubject( rep.getStepAttributeString (id_step, "compare") );  //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "MergeRowsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
			stream.setStepMeta( StepMeta.findStep(steps, (String)stream.getSubject()) );
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
            for (int i=0;i<keyFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "key_field", keyFields[i]); //$NON-NLS-1$
            }

            for (int i=0;i<valueFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "value_field", valueFields[i]); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "flag_field", flagField); //$NON-NLS-1$

            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface referenceStream = infoStreams.get(0);
            StreamInterface compareStream = infoStreams.get(1);

			rep.saveStepAttribute(id_transformation, id_step, "reference", referenceStream.getStepname()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "compare", compareStream.getStepname()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "MergeRowsMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just merge in the info fields.
        //
        if (info!=null)
        {
            boolean found=false;
            for (int i=0;i<info.length && !found;i++) 
            {
                if (info[i]!=null)
                {
                    r.mergeRowMeta(info[i]);
                    found=true;
                }
            }
        }
        
        if (Const.isEmpty(flagField)) throw new KettleStepException(BaseMessages.getString(PKG, "MergeRowsMeta.Exception.FlagFieldNotSpecified"));
        ValueMetaInterface flagFieldValue = new ValueMeta(flagField, ValueMetaInterface.TYPE_STRING);
        flagFieldValue.setOrigin(name);
        r.addValueMeta(flagFieldValue);

    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
        StreamInterface referenceStream = infoStreams.get(0);
        StreamInterface compareStream = infoStreams.get(1);

		if (referenceStream.getStepname()!=null && compareStream.getStepname()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MergeRowsMeta.CheckResult.SourceStepsOK"), stepinfo);
			remarks.add(cr);
		}
		else
		if (referenceStream.getStepname()==null && compareStream.getStepname()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "MergeRowsMeta.CheckResult.SourceStepsMissing"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MergeRowsMeta.CheckResult.OneSourceStepMissing"), stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new MergeRows(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new MergeRowsData();
	}

	/**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, true, false, false, false, false);
    	
	    	ioMeta.addStream( new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "MergeRowsMeta.InfoStream.FirstStream.Description"), StreamIcon.INFO, null) );
	    	ioMeta.addStream( new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "MergeRowsMeta.InfoStream.SecondStream.Description"), StreamIcon.INFO, null) );
    	}
    	
    	return ioMeta;
    }

    public void resetStepIoMeta() {
    }
    
    public TransformationType[] getSupportedTransformationTypes() {
      return new TransformationType[] { TransformationType.Normal, };
    }

}
