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

package org.pentaho.di.trans.steps.filterrows;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
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


/*
 * Created on 02-jun-2003
 *
 */

public class FilterRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = FilterRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/**
	 * This is the main condition for the complete filter.
	 * @since version 2.1
	 */
	private Condition condition;

	public FilterRowsMeta()
	{
		super(); // allocate BaseStepMeta
        condition=new Condition();
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
	 * @return Returns the condition.
	 */
	public Condition getCondition()
	{
		return condition;
	}
	
	/**
	 * @param condition The condition to set.
	 */
	public void setCondition(Condition condition)
	{
		this.condition = condition;
	}
	
	public void allocate()
	{
		condition = new Condition();
	}

	public Object clone()
	{
		FilterRowsMeta retval = (FilterRowsMeta)super.clone();

		if (condition!=null)
        {
            retval.condition = (Condition)condition.clone();
        }
        else 
        {
            retval.condition=null;
        }

		return retval;
	}
	
	public String getXML() throws KettleException
	{
        StringBuffer retval = new StringBuffer(200);
        
        List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();
		retval.append(XMLHandler.addTagValue("send_true_to", targetStreams.get(0).getStepname()));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("send_false_to", targetStreams.get(1).getStepname()));		 //$NON-NLS-1$
		retval.append("    <compare>").append(Const.CR); //$NON-NLS-1$
		
		if (condition!=null)
		{
			retval.append(condition.getXML());
		}
		
		retval.append("    </compare>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

			targetStreams.get(0).setSubject( XMLHandler.getTagValue(stepnode, "send_true_to") ); //$NON-NLS-1$
			targetStreams.get(1).setSubject( XMLHandler.getTagValue(stepnode, "send_false_to") ); //$NON-NLS-1$

			Node compare = XMLHandler.getSubNode(stepnode, "compare"); //$NON-NLS-1$
			Node condnode = XMLHandler.getSubNode(compare, "condition"); //$NON-NLS-1$
	
			// The new situation...
			if (condnode!=null)
			{
				condition = new Condition(condnode);
			}
			else // Old style condition: Line1 OR Line2 OR Line3: @deprecated!
			{
				condition = new Condition();
				
				int nrkeys   = XMLHandler.countNodes(compare, "key"); //$NON-NLS-1$
				if (nrkeys==1)
				{
					Node knode = XMLHandler.getSubNodeByNr(compare, "key", 0); //$NON-NLS-1$
					
					String key         = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
					String value       = XMLHandler.getTagValue(knode, "value"); //$NON-NLS-1$
					String field       = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
					String comparator  = XMLHandler.getTagValue(knode, "condition"); //$NON-NLS-1$
	
					condition.setOperator( Condition.OPERATOR_NONE );
					condition.setLeftValuename(key);
					condition.setFunction( Condition.getFunction(comparator) );
					condition.setRightValuename(field);
					condition.setRightExact( new ValueMetaAndData("value", value ) ); //$NON-NLS-1$
				}
				else
				{
					for (int i=0;i<nrkeys;i++)
					{
						Node knode = XMLHandler.getSubNodeByNr(compare, "key", i); //$NON-NLS-1$
						
						String key         = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
						String value       = XMLHandler.getTagValue(knode, "value"); //$NON-NLS-1$
						String field       = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
						String comparator  = XMLHandler.getTagValue(knode, "condition"); //$NON-NLS-1$
						
						Condition subc = new Condition();
						if (i>0) subc.setOperator( Condition.OPERATOR_OR   );
						else     subc.setOperator( Condition.OPERATOR_NONE );
						subc.setLeftValuename(key);
						subc.setFunction( Condition.getFunction(comparator) );
						subc.setRightValuename(field);
						subc.setRightExact( new ValueMetaAndData("value", value ) ); //$NON-NLS-1$
						
						condition.addCondition(subc);
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "FilterRowsMeta.Exception..UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		allocate();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			allocate();

			List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

			targetStreams.get(0).setSubject( rep.getStepAttributeString (id_step, "send_true_to") );  //$NON-NLS-1$
			targetStreams.get(1).setSubject( rep.getStepAttributeString (id_step, "send_false_to") );  //$NON-NLS-1$

			condition = rep.loadConditionFromStepAttribute(id_step, "id_condition");

		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "FilterRowsMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getTargetStreams()) {
			stream.setStepMeta( StepMeta.findStep(steps, (String)stream.getSubject()) );
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			if (condition!=null) 
			{
				List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

				rep.saveConditionStepAttribute(id_transformation, id_step, "id_condition", condition);
				rep.saveStepAttribute(id_transformation, id_step, "send_true_to", targetStreams.get(0).getStepname()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, "send_false_to", targetStreams.get(1).getStepname()); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "FilterRowsMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
        
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
        // Clear the sortedDescending flag on fields used within the condition - otherwise the comparisons will be inverted!!
        String conditionField[] = condition.getUsedFields();
        for (int i=0;i<conditionField.length;i++)
        {
            int idx = rowMeta.indexOfValue(conditionField[i]);
            if (idx>=0)
            {
                ValueMetaInterface valueMeta = rowMeta.getValueMeta(idx);
                valueMeta.setSortedDescending(false);
            }
        }
	}

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

		if (targetStreams.get(0).getStepname()!=null && targetStreams.get(1).getStepname()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.BothTrueAndFalseStepSpecified"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		if (targetStreams.get(0).getStepname()==null && targetStreams.get(1).getStepname()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.NeitherTrueAndFalseStepSpecified"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.PlsSpecifyBothTrueAndFalseStep"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
				
		if ( targetStreams.get(0).getStepname()!=null )
		{
			int trueTargetIdx = Const.indexOfString(targetStreams.get(0).getStepname(), output);
			if ( trueTargetIdx < 0 )
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
						             BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.TargetStepInvalid", "true", targetStreams.get(0).getStepname() ), 
						             stepinfo);
				remarks.add(cr);
			}
		}

		if ( targetStreams.get(1).getStepname()!=null )
		{
			int falseTargetIdx = Const.indexOfString(targetStreams.get(1).getStepname(), output);
			if ( falseTargetIdx < 0 )
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
						             BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.TargetStepInvalid", "false", targetStreams.get(1).getStepname()), 
						             stepinfo);
				remarks.add(cr);
			}
		}
		
		if (condition.isEmpty())
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.NoConditionSpecified"), stepinfo);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.ConditionSpecified"), stepinfo); //$NON-NLS-1$
		}
		remarks.add(cr);		
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.StepReceivingFields",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			boolean first=true;
			error_message = ""; //$NON-NLS-1$
			boolean error_found = false;
			
			// What fields are used in the condition?
			String key[] = condition.getUsedFields();
			for (int i=0;i<key.length;i++)
			{
				ValueMetaInterface v = prev.searchValueMeta(key[i]);
				if (v==null)
				{
					if (first)
					{
						first=false;
						error_message+=BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.FieldsNotFoundFromPreviousStep")+Const.CR; //$NON-NLS-1$
					}
					error_found=true;
					error_message+="\t\t"+key[i]+Const.CR;  //$NON-NLS-1$
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.AllFieldsFoundInInputStream"), stepinfo); //$NON-NLS-1$
			}
			remarks.add(cr);
		}
		else
		{
			error_message=BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FilterRowsMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new FilterRows(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new FilterRowsData();
	}	

	/**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, true, false, false, false, false);
    	
	    	ioMeta.addStream( new Stream(StreamType.TARGET, null, BaseMessages.getString(PKG, "FilterRowsMeta.InfoStream.True.Description"), StreamIcon.TRUE, null) );
	    	ioMeta.addStream( new Stream(StreamType.TARGET, null, BaseMessages.getString(PKG, "FilterRowsMeta.InfoStream.False.Description"), StreamIcon.FALSE, null) );
    	}
    	
    	return ioMeta;
    }
    
    @Override
    public void resetStepIoMeta() {
    }
    
    /**
     * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
     * @param stream The optional stream to handle.
     */
    public void handleStreamSelection(StreamInterface stream) {
      // This step targets another step.
      // Make sure that we don't specify the same step for true and false...
      // If the user requests false, we blank out true and vice versa
      //
      List<StreamInterface> targets = getStepIOMeta().getTargetStreams();
      int index = targets.indexOf(stream);
      if (index==0) {
        // True
        //
        StepMeta falseStep = targets.get(1).getStepMeta();
        if (falseStep!=null && falseStep.equals(stream.getStepMeta())) {
          targets.get(1).setStepMeta(null);
        }
      }
      if (index==1) {
        // False
        //
        StepMeta trueStep = targets.get(0).getStepMeta();
        if (trueStep!=null && trueStep.equals(stream.getStepMeta())) {
          targets.get(0).setStepMeta(null);
        }
      }
    }

    @Override
    public boolean excludeFromCopyDistributeVerification() {
    	return true;
    }
}
