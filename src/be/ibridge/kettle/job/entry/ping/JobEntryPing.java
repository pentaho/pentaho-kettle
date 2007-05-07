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
 
package be.ibridge.kettle.job.entry.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

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
		setType(JobEntryInterface.TYPE_JOBENTRY_PING);
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
        StringBuffer retval = new StringBuffer();
        
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("hostname",    hostname));
		retval.append("      ").append(XMLHandler.addTagValue("nbr_packets", nbrPackets));

		// TODO: The following line may be removed 3 versions after 2.5.0
		retval.append("      ").append(XMLHandler.addTagValue("nbrpaquets",  nbrPackets));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			String nbrPaquets; 
			
			super.loadXML(entrynode, databases);
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

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			String nbrPaquets;
			
			super.loadRep(rep, id_jobentry, databases);
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
        return StringUtil.environmentSubstitute(getHostname());
    }
	
	public String getNbrPackets()
	{
		return nbrPackets;
	}

	public String getRealNbrPackets()
	{
		return  StringUtil.environmentSubstitute(getNbrPackets());
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
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryPingDialog(shell,this,jobMeta);
    }
}