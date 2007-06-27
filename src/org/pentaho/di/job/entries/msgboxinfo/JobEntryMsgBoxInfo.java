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
 
package org.pentaho.di.job.entries.msgboxinfo;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * Job entry type to display a message box.
 * It uses a piece of javascript to do this.
 * 
 * @author Samatar
 * @since 12-02-2007
 */

public class JobEntryMsgBoxInfo extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String bodymessage;
	private String titremessage;

	public JobEntryMsgBoxInfo(String n, String scr)
	{
		super(n, "");
		bodymessage=null;
		titremessage=null;
		setType(JobEntryInterface.TYPE_JOBENTRY_MSGBOX_INFO);
	}

	public JobEntryMsgBoxInfo()
	{
		this("", "");
	}
	
	public JobEntryMsgBoxInfo(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryMsgBoxInfo je = (JobEntryMsgBoxInfo) super.clone();
        return je;
    }
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
	
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("bodymessage",      bodymessage));
		retval.append("      ").append(XMLHandler.addTagValue("titremessage",     titremessage));


		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			bodymessage = XMLHandler.getTagValue(entrynode, "bodymessage");
			titremessage = XMLHandler.getTagValue(entrynode, "titremessage");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'Msgbox Info' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

			bodymessage = rep.getJobEntryAttributeString(id_jobentry, "bodymessage");
			titremessage = rep.getJobEntryAttributeString(id_jobentry, "titremessage");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'Msgbox Info' from the repository with id_jobentry="+id_jobentry, dbe);
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
			
			rep.saveJobEntryAttribute(id_job, getID(), "bodymessage", bodymessage);
			rep.saveJobEntryAttribute(id_job, getID(), "titremessage", titremessage);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'Msgbox Info' to the repository for id_job="+id_job, dbe);
		}
	}


	/**
	 * Display the Message Box.
	 */
	public boolean evaluate(Result result)
	{
		LogWriter log = LogWriter.getInstance();
		
			
		try
		{
			// Try to display MSGBOX
	
			Display display = new Display();
			Shell shell = new Shell(display);
			MessageBox mb = new MessageBox(shell, SWT.OK |SWT.CANCEL | SWT.ICON_INFORMATION );
			// Set the Body Message
			mb.setMessage(getRealBodyMessage()+Const.CR);
			// Set the title Message
			mb.setText(getRealTitleMessage());
			//mb.open();


			if (mb.open() == SWT.OK)
					
				return true;
			else
				return false;

			
					
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(toString(), "Couldn't display message box: "+e.toString());
			return false;
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
	
	public boolean resetErrorsBeforeExecution()
	{
		// we should be able to evaluate the errors in
		// the previous jobentry.
	    return false;
	}
	
	public boolean evaluates()
	{
		return true;
	}
	
	public boolean isUnconditional()
	{
		return false;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryMsgBoxInfoDialog(shell,this);
    }
    
	public String getRealTitleMessage()
	{
		return environmentSubstitute(getTitleMessage());
	}

	public String getRealBodyMessage()
	{
		return environmentSubstitute(getBodyMessage());
	}

	public String getTitleMessage()
	{
		if (titremessage == null)
		{
			titremessage="";
		}
		return titremessage;
	}
	public String getBodyMessage()
	{
		if (bodymessage == null)
		{
			bodymessage="";
		}
		return bodymessage;
	
		
	}

	public void setBodyMessage(String s)
	{
		
		bodymessage=s;
	
	}

	public void setTitleMessage(String s)
	{
		
		titremessage=s;
	
	}	

	
}