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

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
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
	
	public String getTypeCode()
	{
		return JobEntryInterface.typeCode[type];
	}
	
	public static final String getTypeCode(int type)
	{
		return JobEntryInterface.typeCode[type];
	}
	
	public String getTypeDesc()
	{
		return JobEntryInterface.typeDesc[type];
	}
	
	public static final String getTypeDesc(int type)
	{
		return JobEntryInterface.typeDesc[type];
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
        StringBuffer retval = new StringBuffer();
        //Tom modify these for performance
//		retval.append("      "+XMLHandler.addTagValue("name",              getName()));
		retval.append("      ").append(XMLHandler.addTagValue("name",              getName()));
//		retval.append("      "+XMLHandler.addTagValue("description",       getDescription()));
		retval.append("      ").append(XMLHandler.addTagValue("description",       getDescription()));
//		retval.append("      "+XMLHandler.addTagValue("type",              getTypeDesc()));
        if (type!=JobEntryInterface.TYPE_JOBENTRY_NONE)
            retval.append("      ").append(XMLHandler.addTagValue("type",              getTypeCode()));
	
		return retval.toString();
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
			setID( rep.insertJobEntry(id_job, getName(), getDescription(), getTypeCode()) );
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
	 * Is a reset of the number of errors required before execution.
	 * 
	 * @return 
	 */
	public boolean resetErrorsBeforeExecution()
	{
	    return true;
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
    
    public ArrayList getSQLStatements(Repository repository) throws KettleException
    {
        return new ArrayList();
    }
    
    public String getFilename()
    {
        return null;
    }
    
    public String getRealFilename()
    {
        return null;
    }
}
