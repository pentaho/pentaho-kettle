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

package org.pentaho.di.trans.steps.numberrange;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/**
 * Configuration for the NumberRangePlugin
 * 
 * @author ronny.roeller@fredhopper.com
 *
 */

public class NumberRangeMeta extends BaseStepMeta implements
	 		StepMetaInterface 
 {
	 
	 	private String inputField;
	 	private String outputField;
	 	private String fallBackValue;
	 
	 	private List<NumberRangeRule> rules;
	 
	 	public NumberRangeMeta() 
		{
					super();
				}
	 	
	 	public void emptyRules() 
		{
					rules = new LinkedList<NumberRangeRule>();
					}
	  
	 	public NumberRangeMeta(Node stepnode,List<DatabaseMeta> databases, Map<String, Counter> counters) 
	 	throws KettleXMLException 
			{
		 		loadXML(stepnode, databases, counters);
		 	}
	 
	 	public NumberRangeMeta(Repository rep, ObjectId id_step,
	 			List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	   {
	 		readRep(rep, id_step, databases, counters);
	 	}
	 	
	 	public String getXML() 
		{
			StringBuffer retval = new StringBuffer();
	
			retval.append("    ").append(XMLHandler.addTagValue("inputField", inputField));
			retval.append("    ").append(XMLHandler.addTagValue("outputField", outputField));
			retval.append("    ").append(XMLHandler.addTagValue("fallBackValue", getFallBackValue()));
	
			retval.append("    <rules>").append(Const.CR); //$NON-NLS-1$
			for (NumberRangeRule rule : rules) 
			{
				retval.append("      <rule>").append(Const.CR); //$NON-NLS-1$
				retval.append("        ").append(XMLHandler.addTagValue("lower_bound", rule.getLowerBound())); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("        ").append(XMLHandler.addTagValue("upper_bound", rule.getUpperBound())); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("        ").append(XMLHandler.addTagValue("value", rule.getValue())); //$NON-NLS-1$ //$NON-NLS-2$
				retval.append("      </rule>").append(Const.CR); //$NON-NLS-1$
			}
			retval.append("    </rules>").append(Const.CR); //$NON-NLS-1$
	
			return retval.toString();
		}
	 	public void getFields(RowMetaInterface row, String name,
	 						RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	 						throws KettleStepException 
	 	{
				ValueMetaInterface mcValue = new ValueMeta(outputField,
						ValueMetaInterface.TYPE_STRING);
				mcValue.setOrigin(name);
				mcValue.setLength(255);
				row.addValueMeta(mcValue);
	 	}


	 	public Object clone() 
		{
			Object retval = super.clone();
			return retval;
		}
	 

	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException 
	{
		try 
		{
			inputField = XMLHandler.getTagValue(stepnode, "inputField");
			outputField = XMLHandler.getTagValue(stepnode, "outputField");
	 
	 		emptyRules();
	 		String fallBackValue = XMLHandler.getTagValue(stepnode,	"fallBackValue");
	 		setFallBackValue(fallBackValue);
	 
	 		Node fields = XMLHandler.getSubNode(stepnode, "rules"); //$NON-NLS-1$
			int count = XMLHandler.countNodes(fields, "rule"); //$NON-NLS-1$
			for (int i = 0; i < count; i++) 
			{
				
				Node fnode = XMLHandler.getSubNodeByNr(fields, "rule", i); //$NON-NLS-1$
	 
	 			String lowerBoundStr = XMLHandler.getTagValue(fnode,	"lower_bound"); //$NON-NLS-1$
				String upperBoundStr = XMLHandler.getTagValue(fnode,	"upper_bound"); //$NON-NLS-1$
				String value = XMLHandler.getTagValue(fnode, "value"); //$NON-NLS-1$
	 
	
 				double lowerBound = Double.parseDouble(lowerBoundStr);
 				double upperBound = Double.parseDouble(upperBoundStr);
 				addRule(lowerBound, upperBound, value);
			}
	 				
		 }
		 catch (Exception e) 
	    {
		 	throw new KettleXMLException("Unable to read step info from XML node", e);
		}
	}
	 
	 	public void setDefault() 
		{
			emptyRules();
			setFallBackValue("unknown");
			addRule(-Double.MAX_VALUE, 5, "Less than 5");
			addRule(5, 10, "5-10");
			addRule(10, Double.MAX_VALUE, "More than 10");
			inputField = "";
			outputField = "range";
		}
	 
	 	public void readRep(Repository rep, ObjectId id_step,
		List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException 
		{
	 		try 
	 		{
	 			inputField = rep.getStepAttributeString(id_step, "inputField");
	 			outputField = rep.getStepAttributeString(id_step, "outputField");
	 
	 			emptyRules();
	 			String fallBackValue = rep.getStepAttributeString(id_step,	"fallBackValue");
	 			setFallBackValue(fallBackValue);
	 			int nrfields = rep.countNrStepAttributes(id_step, "lower_bound"); //$NON-NLS-1$
	 			for (int i = 0; i < nrfields; i++) 
	 			{
	 				String lowerBoundStr = rep.getStepAttributeString(id_step, i,	"lower_bound"); //$NON-NLS-1$
	 				String upperBoundStr = rep.getStepAttributeString(id_step, i,	"upper_bound"); //$NON-NLS-1$
	 				String value = rep.getStepAttributeString(id_step, i, "value"); //$NON-NLS-1$
	 

	 				double lowerBound = Double.parseDouble(lowerBoundStr);
	 				double upperBound = Double.parseDouble(upperBoundStr);
	 
	 				addRule(lowerBound, upperBound, value);

	 			} 
	 	  }
		 catch (Exception dbe) 
		 {
		 			throw new KettleException("error reading step with id_step="+ id_step + " from the repository", dbe);
		 } 
	}

	 
	 	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
						throws KettleException 
		{
	 		try 
	 		{
	 			rep.saveStepAttribute(id_transformation, id_step, "inputField",	inputField);
	 			rep.saveStepAttribute(id_transformation, id_step, "outputField",outputField);
	 			rep.saveStepAttribute(id_transformation, id_step, "fallBackValue",	getFallBackValue());
	 
	 			int i = 0;
	 			for (NumberRangeRule rule : rules) 
	 			{
	 				rep.saveStepAttribute(id_transformation, id_step, i,"lower_bound", String.valueOf(rule.getLowerBound())); //$NON-NLS-1$
	 				rep.saveStepAttribute(id_transformation, id_step, i,"upper_bound", String.valueOf(rule.getUpperBound())); //$NON-NLS-1$
	 				rep.saveStepAttribute(id_transformation, id_step, i, "value",	String.valueOf(rule.getValue())); //$NON-NLS-1$
	 				i++;
	 			}
	 		} 
	 catch (KettleDatabaseException dbe) 
 	{
	 			throw new KettleException(	"Unable to save step information to the repository, id_step="+	 id_step, dbe);
	 }
 }
	 

	 	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
																  			StepMeta stepinfo, RowMetaInterface prev, String[] input,
																							String[] output, RowMetaInterface info) 
	 	{
	 		CheckResult cr;
	 		if (prev == null || prev.size() == 0) 
	 		{
	 			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
	 					"Not receiving any fields from previous steps!", stepinfo);
	 			remarks.add(cr);
	 		} 
	 		else 
	 		{
	 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
	 					"Step is connected to previous one, receiving " +
	 							 prev.size() + " fields", stepinfo);
	 			remarks.add(cr);
	 		}
	 
	 		// See if we have input streams leading to this step!
	 		if (input.length > 0) 
	 		{
	 			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
	 					"Step is receiving info from other steps.", stepinfo);
	 			remarks.add(cr);
	 		} 
	 		else 
	 		{
	 			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
	 					"No input received from other steps!", stepinfo);
	 			remarks.add(cr);
	 		}
	 	}
	 
	 	public StepInterface getStep(StepMeta stepMeta,
						StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
																 			Trans disp) 
		 {
			 	return new NumberRange(stepMeta, stepDataInterface, cnr,
						   				transMeta, disp);
	 	}
	 
	 	public StepDataInterface getStepData() 
		{
			return new NumberRangeData();
		}
	 
	 	public String getInputField() 
		{
			return inputField;
		}
	 
	 	public String getOutputField() 
		{
			return outputField;
		}
	 	
	 	public void setOutputField(String outputField) 
	 	{
			this.outputField = outputField;
		}
	 
	 	public List<NumberRangeRule> getRules() 
	 	{
	 		return rules;
	 	}
	 
	 	public String getFallBackValue() 
		{
			return fallBackValue;
		}
	 
	 	public void setInputField(String inputField) 
		{
			this.inputField = inputField;
		}
	 
	 	public void setFallBackValue(String fallBackValue) 
		{
			this.fallBackValue = fallBackValue;
		}
	 
	 	public void addRule(double lowerBound, double upperBound, String value) 
		{
			NumberRangeRule rule = new NumberRangeRule(lowerBound, upperBound, value);
			rules.add(rule);
		}
	    public boolean supportsErrorHandling()
	    {
	        return true;
	    }
	 }