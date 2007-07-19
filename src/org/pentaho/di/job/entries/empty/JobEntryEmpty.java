package org.pentaho.di.job.entries.empty;

import java.util.List;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

public class JobEntryEmpty extends JobEntryBase implements JobEntryInterface
{

	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) throws KettleException
	{
		return null;
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, Repository rep)
			throws KettleXMLException
	{

	}

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {

  }

}
