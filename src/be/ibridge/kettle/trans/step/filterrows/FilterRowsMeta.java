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

package be.ibridge.kettle.trans.step.filterrows;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Condition;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
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

/*
 * Created on 02-jun-2003
 *
 */

public class FilterRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	/**
	 * This is the main condition for the complete filter.
	 * @since version 2.1
	 */
	private Condition condition;

	private String sendTrueStepname;  // Which step is getting the 'true' records?
	private StepMeta sendTrueStep;    // The true step itself...

	private String sendFalseStepname;  // Which step is getting the 'false' records?
	private StepMeta sendFalseStep;    // The false step itself...

	public FilterRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
	
	/**
     * @return Returns the sendFalseStepname.
     */
    public String getSendFalseStepname()
    {
		if (sendFalseStep!=null && 
		        sendFalseStep.getName()!=null &&
		        sendFalseStep.getName().length()>0
			   ) 
				return sendFalseStep.getName();
			return null;
   }
 
	/**
     * @return Returns the sendTrueStepname.
     */
    public String getSendTrueStepname()
    {
		if (sendTrueStep!=null && 
		        sendTrueStep.getName()!=null &&
		        sendTrueStep.getName().length()>0
			   ) 
				return sendTrueStep.getName();
			return null;
   }
    

    /**
     * @param sendFalseStepname The sendFalseStepname to set.
     */
    public void setSendFalseStepname(String sendFalseStepname)
    {
        this.sendFalseStepname = sendFalseStepname;
    }
    
    /**
     * @param sendTrueStepname The sendTrueStepname to set.
     */
    public void setSendTrueStepname(String sendTrueStepname)
    {
        this.sendTrueStepname = sendTrueStepname;
    }
    
    /**
     * @return Returns the sendFalseStep.
     */
    public StepMeta getSendFalseStep()
    {
        return sendFalseStep;
    }
    
    /**
     * @return Returns the sendTrueStep.
     */
    public StepMeta getSendTrueStep()
    {
        return sendTrueStep;
    }
    
    /**
     * @param sendFalseStep The sendFalseStep to set.
     */
    public void setSendFalseStep(StepMeta sendFalseStep)
    {
        this.sendFalseStep = sendFalseStep;
    }
	
    /**
     * @param sendTrueStep The sendTrueStep to set.
     */
    public void setSendTrueStep(StepMeta sendTrueStep)
    {
        this.sendTrueStep = sendTrueStep;
    }
	
	public void allocate()
	{
		condition = new Condition();
	}

	public Object clone()
	{
		FilterRowsMeta retval = (FilterRowsMeta)super.clone();

		if (condition!=null) retval.condition = (Condition)condition.clone();
		else retval.condition=null;

		return retval;
	}
	
	public String getXML()
	{
		String retval="";

		retval+=XMLHandler.addTagValue("send_true_to", getSendTrueStepname());		
		retval+=XMLHandler.addTagValue("send_false_to", getSendFalseStepname());		
		retval+="    <compare>"+Const.CR;
		
		if (condition!=null)
		{
			retval+=condition.getXML();
		}
		
		retval+="    </compare>"+Const.CR;

		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
		    
			sendFalseStepname = XMLHandler.getTagValue(stepnode, "send_false_to");
			sendTrueStepname = XMLHandler.getTagValue(stepnode, "send_true_to");

			Node compare = XMLHandler.getSubNode(stepnode, "compare");
			Node condnode = XMLHandler.getSubNode(compare, "condition");
	
			// The new situation...
			if (condnode!=null)
			{
				condition = new Condition(condnode);
			}
			else // Old style condition: Line1 OR Line2 OR Line3: @deprecated!
			{
				condition = new Condition();
				
				int nrkeys   = XMLHandler.countNodes(compare, "key");
				if (nrkeys==1)
				{
					Node knode = XMLHandler.getSubNodeByNr(compare, "key", 0);
					
					String key         = XMLHandler.getTagValue(knode, "name");
					String value       = XMLHandler.getTagValue(knode, "value");
					String field       = XMLHandler.getTagValue(knode, "field");
					String comparator  = XMLHandler.getTagValue(knode, "condition");
	
					condition.setOperator( Condition.OPERATOR_NONE );
					condition.setLeftValuename(key);
					condition.setFunction( Condition.getFunction(comparator) );
					condition.setRightValuename(field);
					condition.setRightExact( new Value("value", value ) );
				}
				else
				{
					for (int i=0;i<nrkeys;i++)
					{
						Node knode = XMLHandler.getSubNodeByNr(compare, "key", i);
						
						String key         = XMLHandler.getTagValue(knode, "name");
						String value       = XMLHandler.getTagValue(knode, "value");
						String field       = XMLHandler.getTagValue(knode, "field");
						String comparator  = XMLHandler.getTagValue(knode, "condition");
						
						Condition subc = new Condition();
						if (i>0) subc.setOperator( Condition.OPERATOR_OR   );
						else     subc.setOperator( Condition.OPERATOR_NONE );
						subc.setLeftValuename(key);
						subc.setFunction( Condition.getFunction(comparator) );
						subc.setRightValuename(field);
						subc.setRightExact( new Value("value", value ) );
						
						condition.addCondition(subc);
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		allocate();
	}


	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			allocate();

			sendTrueStepname  =      rep.getStepAttributeString (id_step, "send_true_to"); 
			sendFalseStepname =      rep.getStepAttributeString (id_step, "send_false_to"); 

			long id_condition = rep.getStepAttributeInteger(id_step, 0, "id_condition");
			if (id_condition>0)
			{
				condition = new Condition(rep, id_condition);
			}
			else
			{
				int nrkeys = rep.countNrStepAttributes(id_step, "compare_name");
				if (nrkeys==1)
				{
					String key        = rep.getStepAttributeString(id_step, 0, "compare_name");
					String comparator = rep.getStepAttributeString(id_step, 0, "compare_condition");
					String value      = rep.getStepAttributeString(id_step, 0, "compare_value");
					String field      = rep.getStepAttributeString(id_step, 0, "compare_field");

					condition = new Condition();
					condition.setOperator( Condition.OPERATOR_NONE );
					condition.setLeftValuename(key);
					condition.setFunction( Condition.getFunction(comparator) );
					condition.setRightValuename(field);
					condition.setRightExact( new Value("value", value ) );
				}
				else
				{
					condition = new Condition();
					
					for (int i=0;i<nrkeys;i++)
					{
						String key        = rep.getStepAttributeString(id_step, i, "compare_name");
						String comparator = rep.getStepAttributeString(id_step, i, "compare_condition");
						String value      = rep.getStepAttributeString(id_step, i, "compare_value");
						String field      = rep.getStepAttributeString(id_step, i, "compare_field");
		
						Condition subc = new Condition();
						if (i>0) subc.setOperator( Condition.OPERATOR_OR   );
						else     subc.setOperator( Condition.OPERATOR_NONE );
						subc.setLeftValuename(key);
						subc.setFunction( Condition.getFunction(comparator) );
						subc.setRightValuename(field);
						subc.setRightExact( new Value("value", value ) );
						
						condition.addCondition(subc);
					}
				}
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
			if (condition!=null) 
			{
				condition.saveRep(rep);
				rep.saveStepAttribute(id_transformation, id_step, "id_condition", condition.getID());
				rep.insertTransStepCondition(id_transformation, id_step, condition.getID());
				rep.saveStepAttribute(id_transformation, id_step, "send_true_to", getSendTrueStepname());
				rep.saveStepAttribute(id_transformation, id_step, "send_false_to", getSendFalseStepname());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	public void searchInfoAndTargetSteps(ArrayList steps)
	{
		sendTrueStep  = TransMeta.findStep(steps, sendTrueStepname);
		sendFalseStep = TransMeta.findStep(steps, sendFalseStepname);
	}

    /**
     * @return true if this step chooses both target steps
     */
	public boolean chosesTargetSteps()
	{
	    return sendTrueStep!=null && sendFalseStep!=null;
	}

	public String[] getTargetSteps()
	{
	    if (chosesTargetSteps())
	    {
	        return new String[] { getSendTrueStepname(), getSendFalseStepname() };
	    }
	    return null;
	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = "";
		
		if (getSendTrueStepname()!=null && getSendFalseStepname()!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Both 'true' and 'false' steps are specified.  I know how to split the incoming stream(s) of data.", stepinfo);
			remarks.add(cr);
		}
		else
		if (getSendTrueStepname()==null && getSendFalseStepname()==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Neither 'true' and 'false' steps are specified.  Only when the condition is true, rows are sent to the next steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Please specify both the 'true' AND 'false' steps.  That way Kettle know for sure where to send the data after computing the condition.", stepinfo);
			remarks.add(cr);
		}
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
			
			boolean first=true;
			error_message = "";
			boolean error_found = false;
			
			// What fields are used in the condition?
			String key[] = condition.getUsedFields();
			for (int i=0;i<key.length;i++)
			{
				Value v = prev.searchValue(key[i]);
				if (v==null)
				{
					if (first)
					{
						first=false;
						error_message+="Fields used in the condition, not found in input from previous steps:"+Const.CR;
					}
					error_found=true;
					error_message+="\t\t"+key[i]+Const.CR; 
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields to compare with are found in the input stream.", stepinfo);
			}
			remarks.add(cr);
		}
		else
		{
			error_message="Couldn't read fields from the previous step."+Const.CR;
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
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
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new FilterRowsDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new FilterRows(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new FilterRowsData();
	}

}
