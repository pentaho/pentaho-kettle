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

package org.pentaho.di.job.entries.connectedtorepository;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.core.Const;
import org.w3c.dom.Node;



/**
 * Job entry connected to repositoryb.
 *
 * @author Samatar
 * @since 23-06-2008
 */
public class JobEntryConnectedToRepository extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private boolean isspecificrep;
	private String repname;
	private boolean isspecificuser;
	private String username;

	public JobEntryConnectedToRepository(String n, String scr)
	{
		super(n, "");
		isspecificrep=false;
		repname=null;
		isspecificuser=false;
		username=null;
		setJobEntryType(JobEntryType.CONNECTED_TO_REPOSITORY);
	}

	public JobEntryConnectedToRepository()
	{
		this("", "");
	}
	public void setSpecificRep(boolean isspecificrep)
	{
		this.isspecificrep=isspecificrep;
	}
	
	public String getRepName()
	{
		return repname;
	}
	public void setRepName(String repname)
	{
		this.repname=repname;
	}
	public String getUserName()
	{
		return username;
	}
	public void setUserName(String username)
	{
		this.username=username;
	}
	
	
	public boolean isSpecificRep()
	{
		return isspecificrep;
	}

	public boolean isSpecificUser()
	{
		return isspecificuser;
	}

	public void setSpecificUser(boolean isspecificuser)
	{
		this.isspecificuser=isspecificuser;
	}
	public JobEntryConnectedToRepository(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryConnectedToRepository je = (JobEntryConnectedToRepository) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		retval.append("      ").append(XMLHandler.addTagValue("isspecificrep", isspecificrep));
		retval.append("      ").append(XMLHandler.addTagValue("repname", repname));
		retval.append("      ").append(XMLHandler.addTagValue("isspecificuser", isspecificuser));
		retval.append("      ").append(XMLHandler.addTagValue("username", username));
		
		retval.append(super.getXML());

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			isspecificrep = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "isspecificrep"));
			repname=XMLHandler.getTagValue(entrynode, "repname");
			isspecificuser = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "isspecificuser"));
			username=XMLHandler.getTagValue(entrynode, "username");
			
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("JobEntryConnectedToRepository.Meta.UnableToLoadFromXML"), e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			isspecificrep = rep.getJobEntryAttributeBoolean(id_jobentry, "isspecificrep"); 
			repname = rep.getJobEntryAttributeString(id_jobentry, "repname"); 
			isspecificuser = rep.getJobEntryAttributeBoolean(id_jobentry, "isspecificuser"); 
			username = rep.getJobEntryAttributeString(id_jobentry, "username"); 
			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryConnectedToRepository.Meta.UnableToLoadFromRep")+id_jobentry, dbe);


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
			rep.saveJobEntryAttribute(id_job, getID(), "isspecificrep", isspecificrep);
			rep.saveJobEntryAttribute(id_job, getID(), "repname", repname);
			rep.saveJobEntryAttribute(id_job, getID(), "isspecificuser", isspecificuser);
			rep.saveJobEntryAttribute(id_job, getID(), "username", username);
			

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryConnectedToRepository.Meta.UnableToSaveToRep")+id_job, dbe);
		}
	}


	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param previousResult The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		Result result = previousResult;
		result.setNrErrors(1);
		result.setResult(false);
		LogWriter log = LogWriter.getInstance();
		
		if(rep==null)
		{
			log.logError(toString(), Messages.getString("JobEntryConnectedToRepository.Log.NotConnected"));
			return result;
		}
		if(isspecificrep)
		{
			if(Const.isEmpty(repname))
			{
				log.logError(toString(), Messages.getString("JobEntryConnectedToRepository.Error.NoRep"));
				return result;
			}
			String Reponame=environmentSubstitute(repname);
			if(!Reponame.equals(rep.getName()))
			{
				log.logError(toString(), Messages.getString("JobEntryConnectedToRepository.Error.DiffRep",rep.getName(),Reponame));
				return result;
			}
		}
		if(isspecificuser)
		{
			if(Const.isEmpty(username))
			{
				log.logError(toString(), Messages.getString("JobEntryConnectedToRepository.Error.NoUser"));
				return result;
			}
			String Username=environmentSubstitute(username);
			
			if(!Username.equals(rep.getUserInfo().getLogin()))
			{
				log.logError(toString(), Messages.getString("JobEntryConnectedToRepository.Error.DiffUser",rep.getUserInfo().getLogin(),Username));
				return result;
			}
		}
		
		
		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryConnectedToRepository.Log.Connected",rep.getName(),rep.getUserInfo().getLogin()));
		
		result.setResult(true);
		result.setNrErrors(0);
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {

  }



}