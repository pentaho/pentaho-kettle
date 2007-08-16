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
 

package be.ibridge.kettle.trans.step.regexeval;

import java.util.regex.*;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Executes a javascript on the values in the input stream. 
 * Selected calculated values can then be put on the output stream.
 * 
 * @author Matt
 * @since 5-apr-2003
 *
 */
public class RegexEval extends BaseStep implements StepInterface
{
	private RegexEvalMeta meta;
	private RegexEvalData data;
	
		
	public RegexEval(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	


    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(RegexEvalMeta)smi;
		data=(RegexEvalData)sdi;
		
		Row r=getRow();       // Get row from input rowset & set row busy!
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		// Try to get Field index
		int i =r.searchValueIndex(meta.getMatcher());

		
		
		if (first)
		{
			first=false;
			// Check if a matcher is given
			if (meta.getMatcher()!=null)
			{
				// Let's check the Field
				if (i<0)
				{
					// The field is unreachable !
					logError(Messages.getString("RegexEval.Log.ErrorFindingField")+ "[" + meta.getMatcher()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("RegexEval.Exception.CouldnotFindField",meta.getMatcher())); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				// Let's check that Result Field is given
				if (meta.getRealResultfieldname() == null )
				{
					//	Result field is missing !
					logError(Messages.getString("RegexEval.Log.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorResultFieldMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				

				
			}
			else
			{
				// Matcher is missing !
				logError(Messages.getString("RegexEval.Log.ErrorMatcherMissing")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorMatcherMissing")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		

		// Get the field value
		Value value = r.getValue(i);
		String Fieldvalue= value.getString();
		
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
			regularexpression = meta.getRealScript();
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
		
		// add new values to the row.
		Value fn=new Value(meta.getRealResultfieldname(),b); // build a value!
		r.addValue(fn);
		
		if (log.isRowLevel()) logRowlevel(Messages.getString("RegexEval.Log.ReadRow") + " " +  r.toString()); 
		
        putRow(r);       // copy row to output rowset(s);
       
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
