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

package org.pentaho.di.job.entries.success;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;



/**
 * Job entry type to success a job.
 *
 * @author Samatar
 * @since 12-02-2007
 */
public class JobEntrySuccess extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntrySuccess.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public JobEntrySuccess(String n, String scr)
	{
		super(n, "");
	}

	public JobEntrySuccess()
	{
		this("", "");
	}

    public Object clone()
    {
        JobEntrySuccess je = (JobEntrySuccess) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append(super.getXML());

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntrySuccess.Meta.UnableToLoadFromXML"), e);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
	}

	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
	}


	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param previousResult The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setNrErrors(0);
		result.setResult(true);
		
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