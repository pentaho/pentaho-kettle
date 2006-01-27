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
 
package be.ibridge.kettle.job.entry;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.eval.JobEntryEval;
import be.ibridge.kettle.job.entry.eval.JobEntryEvalDialog;
import be.ibridge.kettle.job.entry.fileexists.JobEntryFileExists;
import be.ibridge.kettle.job.entry.fileexists.JobEntryFileExistsDialog;
import be.ibridge.kettle.job.entry.ftp.JobEntryFTP;
import be.ibridge.kettle.job.entry.ftp.JobEntryFTPDialog;
import be.ibridge.kettle.job.entry.http.JobEntryHTTP;
import be.ibridge.kettle.job.entry.http.JobEntryHTTPDialog;
import be.ibridge.kettle.job.entry.job.JobEntryJob;
import be.ibridge.kettle.job.entry.job.JobEntryJobDialog;
import be.ibridge.kettle.job.entry.mail.JobEntryMail;
import be.ibridge.kettle.job.entry.mail.JobEntryMailDialog;
import be.ibridge.kettle.job.entry.sftp.JobEntrySFTP;
import be.ibridge.kettle.job.entry.sftp.JobEntrySFTPDialog;
import be.ibridge.kettle.job.entry.shell.JobEntryShell;
import be.ibridge.kettle.job.entry.shell.JobEntryShellDialog;
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
import be.ibridge.kettle.job.entry.sql.JobEntrySQL;
import be.ibridge.kettle.job.entry.sql.JobEntrySQLDialog;
import be.ibridge.kettle.job.entry.tableexists.JobEntryTableExists;
import be.ibridge.kettle.job.entry.tableexists.JobEntryTableExistsDialog;
import be.ibridge.kettle.job.entry.trans.JobEntryTrans;
import be.ibridge.kettle.job.entry.trans.JobEntryTransDialog;
import be.ibridge.kettle.repository.Repository;


/**
 * Base class for the different types of job-entries...
 * 
 * @author Matt
 * Created on 18-jun-04
 *
 * 
 */

public class JobEntryBase implements Cloneable
{
	private String  name;
	private String  description;
	private boolean changed;
	private int     type;
	private long id;
	
	public JobEntryBase()
	{
		name=null;
		description=null;
	}
	
	public JobEntryBase(String name, String description)
	{
		setName(name);
		setDescription(description);
		setID(-1L);
	}
	
	public JobEntryBase(JobEntryBase jeb)
	{
		setName(jeb.getName());
		setDescription(jeb.getDescription());
		setType(jeb.getType());
		setID(jeb.getID());
	}

	public void clear()
	{
		name = null;
		description = null;
		changed = false;
	}

	public void setID(long id)
	{
		this.id=id;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getTypeDesc()
	{
		return JobEntryInterface.type_desc[type];
	}
	
	public static final String getTypeDesc(int type)
	{
		return JobEntryInterface.type_desc[type];
	}

	public static final int getType(String desc)
	{
		String d[] = JobEntryInterface.type_desc;
		for (int i=1;i< d.length;i++)
		{
			if (d[i].equalsIgnoreCase(desc)) return i;
		}
		d=JobEntryInterface.type_desc_long;
		for (int i=1;i< d.length;i++)
		{
			if (d[i].equalsIgnoreCase(desc)) return i;
		}
		return JobEntryInterface.TYPE_JOBENTRY_NONE;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDescription(String Description)
	{
		this.description = Description;
	}

	public String getDescription()
	{
		return description;
	}

	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed=ch;
	}

	public boolean hasChanged()
	{
		return changed;
	}
	
	public boolean isStart()
	{
		return false;
	}

	public boolean isDummy()
	{
		return false;
	}

	public boolean isEvaluation()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_EVALUATION;
	}

	public boolean isJob()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_JOB;
	}

	public boolean isMail()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_MAIL;
	}

	public boolean isShell()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_MAIL;
	}

	public boolean isSpecial()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_SPECIAL;
	}
	
	public boolean isTransformation()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION;
	}

	public boolean isFTP()
	{
		return getType()==JobEntryInterface.TYPE_JOBENTRY_FTP;
	}
    
    public boolean isSFTP()
    {
        return getType()==JobEntryInterface.TYPE_JOBENTRY_SFTP;
    }

    public boolean isHTTP()
    {
        return getType()==JobEntryInterface.TYPE_JOBENTRY_HTTP;
    }

	public String getXML()
	{
		String retval = "";
		retval+="      "+XMLHandler.addTagValue("name",              getName());
		retval+="      "+XMLHandler.addTagValue("description",       getDescription());
		retval+="      "+XMLHandler.addTagValue("type",              getTypeDesc());
	
		return retval;
	}	

	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			setName( XMLHandler.getTagValue(entrynode, "name") );
			setDescription( XMLHandler.getTagValue(entrynode, "description") );
			String stype = XMLHandler.getTagValue(entrynode, "type");
			setType(JobEntryCopy.getType(stype));
			
			System.out.println("Loaded job entry ["+name+"] of type ["+stype+"]");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load base info for job entry", e);
		}
	}
    
    public void parseRepositoryObjects(Repository rep) throws KettleException
    {
    }
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			setID( rep.insertJobEntry(id_job, getName(), getDescription(), getTypeDesc()) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry base information to the repository for id_job="+id_job, dbe);
		}
	}
	
	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			Row r = rep.getJobEntry(id_jobentry);
			if( r!=null)
			{
				setName( r.searchValue("NAME").getString() );
				
				setDescription( r.searchValue("DESCRIPTION").getString() );
				int id_jobentry_type = (int) r.searchValue("ID_JOBENTRY_TYPE").getInteger();
				Row jetrow = rep.getJobEntryType(id_jobentry_type);
				if (jetrow!=null)
				{
					type = JobEntryCopy.getType( jetrow.searchValue("CODE").getString() );
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load base job entry information from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	/**
	 * Allocate the appropriate class for the given type.
	 * @param type The type of job entry to allocate
	 * @return The appropriate JobEntryInterface class
	 */
	public static final JobEntryInterface newJobEntryInterface(int type)
		throws KettleException
	{
		JobEntryInterface jei = null;
		switch(type)
		{
		case JobEntryInterface.TYPE_JOBENTRY_EVALUATION     : jei = new JobEntryEval(); break;
		case JobEntryInterface.TYPE_JOBENTRY_JOB            : jei = new JobEntryJob(); break;
		case JobEntryInterface.TYPE_JOBENTRY_MAIL           : jei = new JobEntryMail(); break;
		case JobEntryInterface.TYPE_JOBENTRY_SHELL          : jei = new JobEntryShell(); break;
		case JobEntryInterface.TYPE_JOBENTRY_SPECIAL        : jei = new JobEntrySpecial(); break;
		case JobEntryInterface.TYPE_JOBENTRY_SQL            : jei = new JobEntrySQL(); break;
		case JobEntryInterface.TYPE_JOBENTRY_TABLE_EXISTS   : jei = new JobEntryTableExists(); break;
		case JobEntryInterface.TYPE_JOBENTRY_FILE_EXISTS    : jei = new JobEntryFileExists(); break;
		case JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION : jei = new JobEntryTrans(); break;
		case JobEntryInterface.TYPE_JOBENTRY_FTP            : jei = new JobEntryFTP(); break;
        case JobEntryInterface.TYPE_JOBENTRY_SFTP           : jei = new JobEntrySFTP(); break;
        case JobEntryInterface.TYPE_JOBENTRY_HTTP           : jei = new JobEntryHTTP(); break;
		default: 
			throw new KettleException("Unknown job entry type : "+type);
		}
		
		return jei;
	}

	/**
	 * Allocate the appropriate class for the given type.
	 * @param type The type of job entry to allocate
	 * @return The appropriate JobEntryInterface class
	 */
	public static final JobEntryInterface newJobEntryInterface(int type, Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		JobEntryInterface jei = newJobEntryInterface(type);
		jei.loadRep(rep, id_jobentry, databases);
		return jei;
	}
	
	/**
	 * Start the appropriate dialog for a given class interface
	 * @param parent The parent Shell
	 * @param jei JobEntryInterface to build the dialog for.
	 * @param rep The repository to connect to.
	 * @param jobinfo The job we're working with.
	 * @return A new dialog for the job entry.
	 */
	public static final JobEntryDialogInterface newJobEntryDialog(Shell parent, JobEntryInterface jei, Repository rep, JobMeta jobinfo) throws KettleException
	{
		if (jei==null) return null;
		
		JobEntryDialogInterface d = null;
		switch(jei.getType())
		{
		case JobEntryInterface.TYPE_JOBENTRY_EVALUATION     : d = new JobEntryEvalDialog(parent, (JobEntryEval)jei); break;
		case JobEntryInterface.TYPE_JOBENTRY_JOB            : d = new JobEntryJobDialog(parent, (JobEntryJob)jei, rep); break;
		case JobEntryInterface.TYPE_JOBENTRY_MAIL           : d = new JobEntryMailDialog(parent, (JobEntryMail)jei, rep); break;
		case JobEntryInterface.TYPE_JOBENTRY_SHELL          : d = new JobEntryShellDialog(parent, (JobEntryShell)jei, rep); break;
		case JobEntryInterface.TYPE_JOBENTRY_SQL            : d = new JobEntrySQLDialog(parent, (JobEntrySQL)jei, rep, jobinfo); break;
		case JobEntryInterface.TYPE_JOBENTRY_FTP            : d = new JobEntryFTPDialog(parent, (JobEntryFTP)jei, rep, jobinfo); break;
		case JobEntryInterface.TYPE_JOBENTRY_TABLE_EXISTS   : d = new JobEntryTableExistsDialog(parent, (JobEntryTableExists)jei, rep, jobinfo); break;
		case JobEntryInterface.TYPE_JOBENTRY_FILE_EXISTS    : d = new JobEntryFileExistsDialog(parent, (JobEntryFileExists)jei, rep, jobinfo); break;
		case JobEntryInterface.TYPE_JOBENTRY_TRANSFORMATION : d = new JobEntryTransDialog(parent, (JobEntryTrans)jei, rep); break;
        case JobEntryInterface.TYPE_JOBENTRY_SFTP           : d = new JobEntrySFTPDialog(parent, (JobEntrySFTP)jei, rep, jobinfo); break;
        case JobEntryInterface.TYPE_JOBENTRY_HTTP           : d = new JobEntryHTTPDialog(parent, (JobEntryHTTP)jei, rep, jobinfo); break;
		default:
            throw new KettleException("Unable to find dialog for job interface: "+jei.getName());
		}
		return d;
	}

	public Object clone()
	{
		JobEntryBase je;
		try
		{
			je = (JobEntryBase)super.clone();
		}
		catch(CloneNotSupportedException cnse)
		{
			return null;
		}
		return je;
	}

	public String toString()
	{
		return name; 
	}
	
	/**
	 * check whether or not this job entry evaluates.
	 * @return true if the job entry evaluates
	 */
	public boolean evaluates()
	{
		return false;
	}
	
	public boolean isUnconditional()
	{
		return true;
	}
}
