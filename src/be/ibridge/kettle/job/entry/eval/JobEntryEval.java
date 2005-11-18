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
 
package be.ibridge.kettle.job.entry.eval;
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * Job entry type to evaluate the result of a previous job entry.
 * It uses a piece of javascript to do this.
 * 
 * @author Matt
 * @since 5-11-2003
 */

public class JobEntryEval extends JobEntryBase implements JobEntryInterface
{
	private String script;

	public JobEntryEval(String n, String scr)
	{
		super(n, "");
		script=scr;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_EVALUATION);
	}

	public JobEntryEval()
	{
		this("", "");
	}
	
	public JobEntryEval(JobEntryBase jeb)
	{
		super(jeb);
	}
	
	public String getXML()
	{
		String retval ="";
	
		retval+=super.getXML();
		retval+="      "+XMLHandler.addTagValue("script",      script);

		return retval;
	}
	
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			script = XMLHandler.getTagValue(entrynode, "script");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load evaluation job entry from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

			script = rep.getJobEntryAttributeString(id_jobentry, "script");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load evaluation job entry from the repository with id_jobentry="+id_jobentry, dbe);
		}
	}
	
	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "script", script);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save job entry of type transMeta to the repository for id_job="+id_job, dbe);
		}
	}

	public void setScript(String s)
	{
		script = s;
	}
	
	public String getScript()
	{
		return script;
	}

	/**
	 * Evaluate the result of the execution of previous job entry.
	 * @param result The result to evaulate.
	 * @return The boolean result of the evaluation script.
	 */
	public boolean evaluate(Result result)
	{
		LogWriter log = LogWriter.getInstance();
		Context cx;
		Scriptable scope;
		String debug="start";
		
		cx = Context.enter();

		debug="try";
		try
		{
			scope = cx.initStandardObjects(null);
			
			debug="Long";
			Long errors          = new Long(result.getNrErrors());
			Long lines_input     = new Long(result.getNrLinesInput());
			Long lines_output    = new Long(result.getNrLinesOutput());
			Long lines_updated   = new Long(result.getNrLinesUpdated());
			Long lines_read      = new Long(result.getNrLinesRead());
			Long lines_written   = new Long(result.getNrLinesWritten());
			Long exit_status     = new Long(result.getExitStatus());
			Long files_retrieved = new Long(result.getNrFilesRetrieved());
			Long nr              = new Long(result.getEntryNr());
			
			debug="scope.put";
			scope.put("errors", scope, errors);
			scope.put("lines_input", scope, lines_input);
			scope.put("lines_output", scope, lines_output);
			scope.put("lines_updated", scope, lines_updated);
			scope.put("lines_read", scope, lines_read);
			scope.put("lines_written", scope, lines_written);
			scope.put("files_retrieved", scope, files_retrieved);
			scope.put("exit_status", scope, exit_status);
			scope.put("nr", scope, nr);
            scope.put("is_windows", scope, new Boolean(Const.isWindows()));
            
			Object array[] = null;
			if (result.rows!=null)
			{
				array=result.rows.toArray(); 
			}
			
			scope.put("rows", scope, array);
	
			try
			{
				debug="cx.evaluateString()";
				Object res = cx.evaluateString(scope, this.script, "<cmd>", 1, null);
				debug="toBoolean";
				boolean retval = Context.toBoolean(res);
				// System.out.println(result.toString()+" + ["+this.script+"] --> "+retval);
				return retval;
			}
			catch(Exception e)
			{
				log.logError(toString(), "Couldn't compile javascript: "+e.toString());
				return false;
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), "Error evaluating expression in ["+debug+"] : "+e.toString());
			return false;
		}
		finally 
		{
			Context.exit();
		}
	}
	
	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param prev_result The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		prev_result.setResult( evaluate(prev_result) );
		
		return prev_result;
	}
	
	public boolean evaluates()
	{
		return true;
	}
	
	public boolean isUnconditional()
	{
		return false;
	}

}
