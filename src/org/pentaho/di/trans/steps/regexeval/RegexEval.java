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
 

package org.pentaho.di.trans.steps.regexeval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Validate Field with regular expression.
 * 
 * @author Matt
 * @since 15-08-2007
 *
 */

public class RegexEval extends BaseStep implements StepInterface
{
	private RegexEvalMeta meta;
	private RegexEvalData data;
	
	
	public RegexEval(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}



    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(RegexEvalMeta)smi;
		data=(RegexEvalData)sdi;
		
		Object[] row = getRow();
		
		if (row==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first) // we just got started
		{
			first=false;
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// Check if a Field (matcher) is given
			if (meta.getMatcher()!=null)
			{
				 // Cache the position of the Field
				if (data.indexOfFieldToEvaluate<0)
				{
					data.indexOfFieldToEvaluate = getInputRowMeta().indexOfValue(meta.getMatcher());
					
					if (data.indexOfFieldToEvaluate<0)
		            {                    
		                // The field is unreachable !
						logError(Messages.getString("RegexEval.Log.ErrorFindingField")+ "[" + meta.getMatcher()+"]"); 
						throw new KettleStepException(Messages.getString("RegexEval.Exception.CouldnotFindField",meta.getMatcher())); 
					}
		                
					
					 // Let's check that Result Field is given
					if (environmentSubstitute(meta.getResultfieldname()) == null )
					{
						//	Result field is missing !
						logError(Messages.getString("RegexEval.Log.ErrorResultFieldMissing")); 
						throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorResultFieldMissing"));
					}
		                
				}
			}
			else
			{
				// Matcher is missing !
				log.logError("Error",Messages.getString("RegexEval.Log.ErrorMatcherMissing"));
				throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorMatcherMissing")); 
			}
			
		}
		
		// Get the Field value
		String Fieldvalue= getInputRowMeta().getString(row,data.indexOfFieldToEvaluate);
		
		// Endebbed options
		String options="";
		
		if (meta.caseinsensitive())
		{
			options = options + "(?i)";
		}
		if (meta.comment())
		{
			options = options + "(?x)";
		}
		if (meta.dotall())
		{
			options = options + "(?s)";
		}
		if (meta.multiline())
		{
			options = options + "(?m)";
		}
		if (meta.unicode())
		{
			options = options + "(?u)";
		}
		if (meta.unix())
		{
			options = options + "(?d)";
		}
	
		// Regular expression
		String regularexpression= meta.getScript();
		if (meta.useVar())
		{
			regularexpression = environmentSubstitute(meta.getScript());
		}
		if (log.isDetailed()) logDetailed(Messages.getString("RegexEval.Log.Regexp") + " " + options+regularexpression); 
		
		// Regex compilation
		Pattern p;
		
		if (meta.canoeq())
		{
			p= Pattern.compile(options+regularexpression,Pattern.CANON_EQ);
		}
		else
		{
			p= Pattern.compile(options+regularexpression);	
		}
		
		// Search engine
		Matcher m = p.matcher(Fieldvalue);
		
		// Start search
		boolean b = m.matches();
		
		// Add result field to input stream
		Object[] outputRowData2 =RowDataUtil.addValueData(row, getInputRowMeta().size(),b);
		
		if (log.isRowLevel()) logRowlevel(Messages.getString("RegexEval.Log.ReadRow") + " " +  getInputRowMeta().getString(row)); 
		
		putRow(data.outputRowMeta, outputRowData2);  // copy row to output rowset(s);
       
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RegexEvalMeta)smi;
		data=(RegexEvalData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}


	//
	// Run is were the action happens!
	public void run()
	{
		
		try
		{
			logBasic(Messages.getString("RegexEval.Log.StartingToRun")); //$NON-NLS-1$
		
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			

			
			logError(Messages.getString("RegexEval.Log.UnexpectedeError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Messages.getString("RegexEval.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
