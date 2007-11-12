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

package org.pentaho.di.job.entries.ping;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines a ping job entry.
 *
 * @author Samatar Hassan
 * @since Mar-2007
 *
 */
public class JobEntryPing extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String hostname;
	private String nbrPackets;

	public JobEntryPing(String n)
	{
		super(n, "");
		hostname=null;
		nbrPackets="2";
		setID(-1L);
		setJobEntryType(JobEntryType.PING);
	}

	public JobEntryPing()
	{
		this("");
	}

	public JobEntryPing(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryPing je = (JobEntryPing) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("hostname",    hostname));
		retval.append("      ").append(XMLHandler.addTagValue("nbr_packets", nbrPackets));

		// TODO: The following line may be removed 3 versions after 2.5.0
		retval.append("      ").append(XMLHandler.addTagValue("nbrpaquets",  nbrPackets));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			String nbrPaquets;

			super.loadXML(entrynode, databases, slaveServers);
			hostname   = XMLHandler.getTagValue(entrynode, "hostname");
			nbrPackets = XMLHandler.getTagValue(entrynode, "nbr_packets");

			// TODO: The following lines may be removed 3 versions after 2.5.0
			nbrPaquets = XMLHandler.getTagValue(entrynode, "nbrpaquets");
			if ( nbrPackets == null && nbrPaquets != null )
			{
				// if only nbrpaquets exists this means that the file was
				// save by a version 2.5.0 ping job entry
				nbrPackets = nbrPaquets;
			}
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'ping' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			String nbrPaquets;

			super.loadRep(rep, id_jobentry, databases, slaveServers);
			hostname   = rep.getJobEntryAttributeString(id_jobentry, "hostname");
			nbrPackets = rep.getJobEntryAttributeString(id_jobentry, "nbr_packets");

			// TODO: The following lines may be removed 3 versions after 2.5.0
			nbrPaquets = rep.getJobEntryAttributeString(id_jobentry, "nbrpaquets");
			if ( nbrPackets == null && nbrPaquets != null )
			{
				// if only nbrpaquets exists this means that the file was
				// save by a version 2.5.0 ping job entry
				nbrPackets = nbrPaquets;
			}
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'ping' exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "hostname",    hostname);
			rep.saveJobEntryAttribute(id_job, getID(), "nbr_packets", nbrPackets);
			// TODO: The following line may be removed 3 versions after 2.5.0
			rep.saveJobEntryAttribute(id_job, getID(), "nbrpaquets",  nbrPackets);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'ping' to the repository for id_job="+id_job, dbe);
		}
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getHostname()
	{
		return hostname;
	}

    public String getRealHostname()
    {
        return environmentSubstitute(getHostname());
    }

	public String getNbrPackets()
	{
		return nbrPackets;
	}

	public String getRealNbrPackets()
	{
		return environmentSubstitute(getNbrPackets());
	}

	public void setNbrPackets(String nbrPackets)
	{
		this.nbrPackets = nbrPackets;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
    {
        LogWriter log = LogWriter.getInstance();
        Result result = previousResult;

        result.setNrErrors(1);
        result.setResult(false);

        String hostname = getRealHostname();

        if (!Const.isEmpty(hostname))
        {
            String ip = hostname;

            try
            {
                String lignePing = "";

                int NbrPaquetsSend = Const.toInt(getRealNbrPackets(), -1);

                if (NbrPaquetsSend < 1 || NbrPaquetsSend > 1000)
                {
                    // Value must be between 1 and 1000
                    NbrPaquetsSend = 2;
                }

                if ( log.isDetailed() )
                {
                    log.logDetailed(toString(), Messages.getString("JobPing.NbrPackets.Label") + NbrPaquetsSend);
                    log.logDetailed(toString(), Messages.getString("JobPing.PingingHost1.Label") + ip + Messages.getString("JobPing.PingingHost2.Label"));
                }

                Process processPing = Runtime.getRuntime().exec("ping " + ip + " -n " + NbrPaquetsSend);

                // Get ping response
                log.logDetailed(toString(), Messages.getString("JobPing.Gettingresponse1.Label") + ip
                        + Messages.getString("JobPing.Gettingresponse2.Label"));

                BufferedReader br = new BufferedReader(new InputStreamReader(processPing.getInputStream()));

                // Read response lines
                while ((lignePing = br.readLine()) != null)
                {
                    log.logDetailed(toString(), lignePing);
                    // We succeed only when 0% lost of data
                }
                if (processPing.exitValue()==0)
                {
                    log.logDetailed(toString(), Messages.getString("JobPing.OK1.Label") + ip + Messages.getString("JobPing.OK2.Label"));
                    result.setNrErrors(0);
                    result.setResult(true);
                }
            }
            catch (IOException ex)
            {
                log.logError(toString(), Messages.getString("JobPing.Error.Label") + ex.getMessage());
            }
        }
        else
        {
            // No host was specified
            log.logError(toString(), Messages.getString("JobPing.SpecifyHost.Label"));
        }

        return result;
    }

	public boolean evaluates()
	{
		return true;
	}

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(hostname)) {
      String realServername = jobMeta.environmentSubstitute(hostname);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realServername, ResourceType.SERVER));
      references.add(reference);
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "hostname", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
  }



}