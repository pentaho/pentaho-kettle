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
 

package be.ibridge.kettle.trans.step.scriptvalues;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
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
public class ScriptValues extends BaseStep implements StepInterface
{
	private ScriptValuesMeta meta;
	private ScriptValuesData data;
	
	public ScriptValues(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private void determineUsedFields(Row row)
	{
		int nr=0;
		
		// Count the occurences of the values.
		// Perhaps we find values in comments, but we take no risk!
		for (int i=0;i<row.size();i++)
		{
			String valname = row.getValue(i).getName().toUpperCase();
			if (meta.getScript().toUpperCase().indexOf(valname)>=0)
			{
				nr++;
			}
		}
		
		// Allocate fields_used
		data.fields_used = new int[nr];

		nr = 0;
		// Count the occurences of the values.
		// Perhaps we find values in comments, but we take no risk!
		for (int i=0;i<row.size();i++)
		{
			// Values are case-insensitive in JavaScript.
			//
			String valname = row.getValue(i).getName();
			if (meta.getScript().indexOf(valname)>=0)
			{
				if (log.isDetailed()) logDetailed(Messages.getString("ScriptValues.Log.UsedValueName",String.valueOf(i),valname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				data.fields_used[nr]=i;
				nr++;
			}
		}
		
		if (log.isDetailed()) logDetailed(Messages.getString("ScriptValues.Log.UsingValuesFromInputStream",String.valueOf(data.fields_used.length))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	
	private synchronized boolean addValues(Row row)
	{
		if (first)
		{
			// Determine the indexes of the fields used!
			determineUsedFields(row);
			
			debug=Messages.getString("ScriptValues.DebugMessage.FirstRow"); //$NON-NLS-1$
			data.cx = Context.enter();
			data.scope = data.cx.initStandardObjects(null);
			
			first = false;
			Scriptable jsvalue = Context.toObject(this, data.scope);
			data.scope.put("_step_", data.scope, jsvalue); //$NON-NLS-1$
			
			//StringReader in = new StringReader(info.script);
			
			try
			{
				debug=Messages.getString("ScriptValues.DebugMessage.CompileReader"); //$NON-NLS-1$
				data.script=data.cx.compileString(meta.getScript(), "script", 1, null); //$NON-NLS-1$
				// script=cx.compileReader(scope, in, "script", 1, null);
			}
			catch(Exception e)
			{
				logError(Messages.getString("ScriptValues.Log.CouldNotCompileJavascript")+e.toString()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				return false;
			}
		}

		debug=Messages.getString("ScriptValues.DebugMessage.ToObjectRow"); //$NON-NLS-1$
		Scriptable jsrow = Context.toObject(row, data.scope);
		data.scope.put("row", data.scope, jsrow); //$NON-NLS-1$

		debug=Messages.getString("ScriptValues.DebugMessage.ToObjectRowValues"); //$NON-NLS-1$
		for (int i=0;i<data.fields_used.length;i++)
		{
			Value val = row.getValue(data.fields_used[i]); 
			debug=Messages.getString("ScriptValues.DebugMessage.ToObjectRowValue")+i; //$NON-NLS-1$
			Scriptable jsarg = Context.toObject(val, data.scope);
			debug=Messages.getString("ScriptValues.DebugMessage.ToObjectRowValue2")+i+" name==null?"+(val.getName()==null)+" row="+row; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			data.scope.put(val.getName(), data.scope, jsarg);
		}
		
		debug=Messages.getString("ScriptValues.DebugMessage.ToObjectValueClass"); //$NON-NLS-1$
		Scriptable jsval = Context.toObject(Value.class, data.scope);
		data.scope.put("Value", data.scope, jsval); //$NON-NLS-1$

		try
		{
			//ScriptableObject.defineClass(scope, Value.class);

			debug=Messages.getString("ScriptValues.DebugMessage.Exec"); //$NON-NLS-1$
			//result = cx.evaluateString(scope, info.script, "<cmd>", 1, null);
			data.script.exec(data.cx, data.scope);
			
			//Object id[] = scope.getIds();

			StringBuffer message = new StringBuffer();
			for (int i=0;i<meta.getName().length;i++)
			{
				Value res = new Value();
				if (meta.getValue(data.scope, i, res, message)) 
				{
					logError(message.toString());
					setErrors(1);
					stopAll();
					return false;
				}
					
				row.addValue(res);  // This means the row in rowset gets an extra field!
			}
		}
		catch(JavaScriptException jse)
		{
            logError(Messages.getString("ScriptValues.Log.JavascriptError")+jse.toString()); //$NON-NLS-1$
            logError(Messages.getString("ScriptValues.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(jse)); //$NON-NLS-1$
            setErrors(1);
			stopAll();
			return false;
		}
		catch(Exception e)
		{
			logError(Messages.getString("ScriptValues.Log.JavascriptError")+e.toString()); //$NON-NLS-1$
            logError(Messages.getString("ScriptValues.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			return false;
		}
		
		return true;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(ScriptValuesMeta)smi;
		data=(ScriptValuesData)sdi;

		Row r=getRow();       // Get row from input rowset & set row busy!

		if (r==null)  // no more input to be expected...
		{
			debug=Messages.getString("ScriptValues.DebugMessage.End"); //$NON-NLS-1$
			if (data.cx!=null) Context.exit();
			setOutputDone();
			return false;
		}
		    
		// add new values to the row.
		if (!addValues(r))
		{
			debug=Messages.getString("ScriptValues.DebugMessage.NoMoreNewValues"); //$NON-NLS-1$
			if (data.cx!=null) Context.exit();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		putRow(r);       // copy row to output rowset(s);
			
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("ScriptValues.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ScriptValuesMeta)smi;
		data=(ScriptValuesData)sdi;
		
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
			logBasic(Messages.getString("ScriptValues.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("ScriptValues.Log.UnexpectedeError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Messages.getString("ScriptValues.Log.ErrorStackTrace")+Const.CR+Const.getStackTracker(e)); //$NON-NLS-1$
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
