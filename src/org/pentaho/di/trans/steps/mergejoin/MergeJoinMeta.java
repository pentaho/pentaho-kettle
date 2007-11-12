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

package org.pentaho.di.trans.steps.mergejoin;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * @author Biswapesh
 * @since 24-nov-2006 
 */

public class MergeJoinMeta extends BaseStepMeta implements StepMetaInterface
{
	public static final String [] join_types = {"INNER", "LEFT OUTER", "RIGHT OUTER", "FULL OUTER"}; //$NON-NLS-1$
	public static final boolean [] one_optionals = {false, false, true, true};
	public static final boolean [] two_optionals = {false, true, false, true};

	private String joinType;

	private String stepName1;
	private StepMeta stepMeta1;

	private String stepName2;
	private StepMeta stepMeta2;

    private String   keyFields1[];
    private String   keyFields2[];

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
    public String[] getKeyFields1()
    {
        return keyFields1;
    }

    /**
     * @param keyFields1 The keyFields1 to set.
     */
    public void setKeyFields1(String[] keyFields1)
    {
        this.keyFields1 = keyFields1;
    }

	/**
     * @return Returns the keyFields2.
     */
    public String[] getKeyFields2()
    {
        return keyFields2;
    }

    /**
     * @param keyFields2 The keyFields2 to set.
     */
    public void setKeyFields2(String[] keyFields2)
    {
        this.keyFields2 = keyFields2;
    }
    
    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }

    public MergeJoinMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
     * @return Returns the stepName1.
     */
    public String getStepName1()
    {
		if (stepMeta1!=null && 
		        stepMeta1.getName()!=null &&
		        stepMeta1.getName().length()>0
			   ) 
				return stepMeta1.getName();
			return null;
   }
 
	/**
     * @return Returns the stepName2.
     */
    public String getStepName2()
    {
		if (stepMeta2!=null && 
		        stepMeta2.getName()!=null &&
		        stepMeta2.getName().length()>0
			   ) 
				return stepMeta2.getName();
			return null;
   }
    

    /**
     * @param stepName1 The stepName1 to set.
     */
    public void setStepName1(String stepName1)
    {
        this.stepName1 = stepName1;
    }
    
    /**
     * @param stepName2 The stepName2 to set.
     */
    public void setStepName2(String stepName2)
    {
        this.stepName2 = stepName2;
    }
    
    /**
     * @return Returns the stepMeta1.
     */
    public StepMeta getStepMeta1()
    {
        return stepMeta1;
    }
    
    /**
     * @return Returns the stepMeta2
     */
    public StepMeta getStepMeta2()
    {
        return stepMeta2;
    }
    
    /**
     * @param stepMeta1 The stepMeta1 to set.
     */
    public void setStepMeta1(StepMeta stepMeta1)
    {
        this.stepMeta1 = stepMeta1;
    }
	
    /**
     * @param stepMeta2 The stepMeta2 to set.
     */
    public void setStepMeta2(StepMeta stepMeta2)
    {
        this.stepMeta2 = stepMeta2;
    }
	
	public void allocate(int nrKeys1, int nrKeys2)
	{
        keyFields1 = new String[nrKeys1];
        keyFields2 = new String[nrKeys2];
	}

	public Object clone()
	{
		MergeJoinMeta retval = (MergeJoinMeta)super.clone();
        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append(XMLHandler.addTagValue("join_type", getJoinType())); //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("step1", getStepName1())); //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("step2", getStepName2())); //$NON-NLS-1$

		retval.append("    <keys_1>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<keyFields1.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields1[i])); //$NON-NLS-1$
        }
        retval.append("    </keys_1>"+Const.CR); //$NON-NLS-1$
        
        retval.append("    <keys_2>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<keyFields2.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields2[i])); //$NON-NLS-1$
        }
        retval.append("    </keys_2>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
            
            Node keysNode1 = XMLHandler.getSubNode(stepnode, "keys_1"); //$NON-NLS-1$
            Node keysNode2 = XMLHandler.getSubNode(stepnode, "keys_2"); //$NON-NLS-1$
            
		    int nrKeys1 = XMLHandler.countNodes(keysNode1, "key"); //$NON-NLS-1$
            int nrKeys2 = XMLHandler.countNodes(keysNode2, "key"); //$NON-NLS-1$
            
            allocate(nrKeys1, nrKeys2);
            
            for (int i=0;i<nrKeys1;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysNode1, "key", i); //$NON-NLS-1$
                keyFields1[i] = XMLHandler.getNodeValue(keynode);
            }
            
            for (int i=0;i<nrKeys2;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysNode2, "key", i); //$NON-NLS-1$
                keyFields2[i] = XMLHandler.getNodeValue(keynode);
            }
            
			stepName1 = XMLHandler.getTagValue(stepnode, "step1"); //$NON-NLS-1$
			stepName2 = XMLHandler.getTagValue(stepnode, "step2"); //$NON-NLS-1$
			joinType = XMLHandler.getTagValue(stepnode, "join_type"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("MergeJoinMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		joinType = join_types[0];
        allocate(0,0);
	}
    
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
            int nrKeys1 = rep.countNrStepAttributes(id_step, "keys_1"); //$NON-NLS-1$
            int nrKeys2 = rep.countNrStepAttributes(id_step, "keys_2"); //$NON-NLS-1$
            
			allocate(nrKeys1, nrKeys2);
            
            for (int i=0;i<nrKeys1;i++)
            {
                keyFields1[i] = rep.getStepAttributeString(id_step, i, "keys_1"); //$NON-NLS-1$
            }
            for (int i=0;i<nrKeys2;i++)
            {
                keyFields2[i] = rep.getStepAttributeString(id_step, i, "keys_2"); //$NON-NLS-1$
            }

			stepName1 = rep.getStepAttributeString (id_step, "step1");  //$NON-NLS-1$
			stepName2 = rep.getStepAttributeString (id_step, "step2");  //$NON-NLS-1$
			joinType = rep.getStepAttributeString(id_step, "join_type"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeJoinMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            for (int i=0;i<keyFields1.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "keys_1", keyFields1[i]); //$NON-NLS-1$
            }

            for (int i=0;i<keyFields2.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "keys_2", keyFields2[i]); //$NON-NLS-1$
            }

			rep.saveStepAttribute(id_transformation, id_step, "step1", getStepName1()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "step2", getStepName2()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "join_type", getJoinType()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeJoinMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		stepMeta1  = StepMeta.findStep(steps, stepName1);
		stepMeta2 = StepMeta.findStep(steps, stepName2);
	}
    
    public String[] getInfoSteps()
    {
        List<String> list = new ArrayList<String>();
        if (stepMeta1!=null) list.add(stepMeta1.getName());
        if (stepMeta2!=null) list.add(stepMeta2.getName());
        if (list.isEmpty()) return null;
        return list.toArray(new String[list.size()]);
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        /*
         * @todo Need to check for the following:
         *   1) Join type must be one of INNER / LEFT OUTER / RIGHT OUTER / FULL OUTER
         *   2) Number of input streams must be two (for now at least)
         *   3) The field names of input streams must be unique
         */ 
        CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("MergeJoinMeta.CheckResult.StepNotVerified"), stepinfo); //$NON-NLS-1$
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
		return new MergeJoin(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new MergeJoinData();
	}

}
