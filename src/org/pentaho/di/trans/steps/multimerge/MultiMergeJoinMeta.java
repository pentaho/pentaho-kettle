/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.multimerge;

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
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;

/**
 * @author Biswapesh
 * @since 24-nov-2006 
 */
		
public class MultiMergeJoinMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = MultiMergeJoinMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String [] join_types = {"INNER", "FULL OUTER"}; //$NON-NLS-1$
	public static final boolean [] optionals = {false, true};

	private String joinType;

	/**
	 * comma separated key values for each stream
	 */
    private String   keyFields[];
    
    /**
     * input stream names
     */
    private String inputSteps[];


    /**
     * The supported join types are INNER, LEFT OUTER, RIGHT OUTER and FULL OUTER
     * @return The type of join
     */
    public String getJoinType()
    {
    	return joinType;
    }

    /**
     * Sets the type of join
     * @param joinType The type of join, e.g. INNER/FULL OUTER
     */
    public void setJoinType(String joinType)
    {
    	this.joinType = joinType;
    }

    /**
     * @return Returns the keyFields1.
     */
    public String[] getKeyFields()
    {
        return keyFields;
    }

    /**
     * @param keyFields1 The keyFields1 to set.
     */
    public void setKeyFields(String[] keyFields)
    {
        this.keyFields = keyFields;
    }
    
    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }

    public MultiMergeJoinMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}
    	
	public void allocateKeys(int nrKeys)
	{
        keyFields = new String[nrKeys];
	}

	public Object clone()
	{
		MultiMergeJoinMeta retval = (MultiMergeJoinMeta)super.clone();
        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();

		retval.append(XMLHandler.addTagValue("join_type", getJoinType())); //$NON-NLS-1$
		for (int i = 0; i < infoStreams.size(); i++)
		    retval.append(XMLHandler.addTagValue("step"+i, infoStreams.get(i).getStepname())); //$NON-NLS-1$

		retval.append("    <number_input>"+infoStreams.size()+"</number_input>"+Const.CR);
		retval.append("    <keys>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<keyFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields[i])); //$NON-NLS-1$
        }
        retval.append("    </keys>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{ 
            
            Node keysNode = XMLHandler.getSubNode(stepnode, "keys"); //$NON-NLS-1$
            
		    int nrKeys = XMLHandler.countNodes(keysNode, "key"); //$NON-NLS-1$
            
            allocateKeys(nrKeys);
            
            for (int i=0;i<nrKeys;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysNode, "key", i); //$NON-NLS-1$
                keyFields[i] = XMLHandler.getNodeValue(keynode);
            }

            int nInputStreams = Integer.parseInt(XMLHandler.getTagValue(stepnode, "number_input"));
            
            allocateInputSteps(nInputStreams);
            
            for (int i = 0; i < nInputStreams; i++) {
                getStepIOMeta().addStream(new Stream(StreamType.INFO, null,
                    BaseMessages.getString(PKG, "MultiMergeJoin.InfoStream.Description"), 
                    StreamIcon.INFO, null));
            }
            
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            for (int i = 0; i < infoStreams.size(); i++)
            {
            	String stepName=XMLHandler.getTagValue(stepnode, "step"+i);
			    infoStreams.get(i).setSubject(stepName ); //$NON-NLS-1$
			    inputSteps[i]=stepName;
            }

			joinType = XMLHandler.getTagValue(stepnode, "join_type"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "MultiMergeJoinMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		joinType = join_types[0];
        allocateKeys(0);
	}
    
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
            int nrKeys = rep.countNrStepAttributes(id_step, "keys"); //$NON-NLS
            
			allocateKeys(nrKeys);
            
            for (int i=0;i<nrKeys;i++)
            {
                keyFields[i] = rep.getStepAttributeString(id_step, i, "keys"); //$NON-NLS-1$
            }

            long nInputStreams = rep.getStepAttributeInteger(id_step, "number_imput");
            for (int i = 0; i < nInputStreams; i++) {
                getStepIOMeta().addStream(new Stream(StreamType.INFO, null,
                    BaseMessages.getString(PKG, "MultiMergeJoin.InfoStream.Description"), 
                    StreamIcon.INFO, null));
            }
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            
            for (int i=0; i < infoStreams.size(); i++)
			    infoStreams.get(i).setSubject( rep.getStepAttributeString (id_step, "step"+i) );  //$NON-NLS-1$

			joinType = rep.getStepAttributeString(id_step, "join_type"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "MultiMergeJoinMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
			stream.setStepMeta( StepMeta.findStep(steps, (String)stream.getSubject()) );
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
            for (int i=0;i<keyFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "keys", keyFields[i]); //$NON-NLS-1$
            }

            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();

            rep.saveStepAttribute(id_transformation, id_step, "number_inputs", infoStreams.size());
            for (int i=0; i<infoStreams.size(); i++)
			  rep.saveStepAttribute(id_transformation, id_step, "step"+i, infoStreams.get(i).getStepname()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "join_type", getJoinType()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "MultiMergeJoinMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        /*
         * @todo Need to check for the following:
         *   1) Join type must be one of INNER / LEFT OUTER / RIGHT OUTER / FULL OUTER
         *   2) Number of input streams must be two (for now at least)
         *   3) The field names of input streams must be unique
         */ 
        CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "MultiMergeJoinMeta.CheckResult.StepNotVerified"), stepinfo); //$NON-NLS-1$
        remarks.add(cr);
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just merge in the info fields.
        //
        if (info!=null)
        {
            for (int i=0;i<info.length;i++) 
            {
                if (info[i]!=null)
                {
                    r.mergeRowMeta(info[i]);
                }
            }
        }
		
        for (int i=0;i<r.size();i++) r.getValueMeta(i).setOrigin(name);
        return;
    }
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new MultiMergeJoin(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new MultiMergeJoinData();
	}
    
    public void resetStepIoMeta() {
    	// Don't reset!
    }

	public void setInputSteps(String inputSteps[]) {
		this.inputSteps = inputSteps;
	}

	public String[] getInputSteps() {
		return inputSteps;
	}

	public void allocateInputSteps(int count) 
	{
		inputSteps = new String[count];
		
	}
}
