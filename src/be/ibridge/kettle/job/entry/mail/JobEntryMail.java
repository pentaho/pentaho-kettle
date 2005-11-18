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
 
package be.ibridge.kettle.job.entry.mail;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobEntryResult;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * Describes a Mail Job Entry.
 * 
 * @author Matt
 * Created on 17-06-2003
 *
 */

public class JobEntryMail extends JobEntryBase implements JobEntryInterface
{
	private String server;
	private String destination;
	private String replyto;
	private String subject;
	private boolean include_date;
	private String contact_person;
	private String contact_phone;
	private String comment;

	public JobEntryMail(String n)
	{
		super(n, "");
		setType(JobEntryInterface.TYPE_JOBENTRY_MAIL);
	}

	public JobEntryMail()
	{
		this("");
	}
	
	public JobEntryMail(JobEntryBase jeb)
	{
		super(jeb);
	}

	public String getXML()
	{
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("server",         server);
		retval+="      "+XMLHandler.addTagValue("destination",    destination);
		retval+="      "+XMLHandler.addTagValue("replyto",        replyto);
		retval+="      "+XMLHandler.addTagValue("subject",        subject);
		retval+="      "+XMLHandler.addTagValue("include_date",   include_date);
		retval+="      "+XMLHandler.addTagValue("contact_person", contact_person);
		retval+="      "+XMLHandler.addTagValue("contact_phone",  contact_phone);
		retval+="      "+XMLHandler.addTagValue("comment",        comment);

		return retval;
	}
	
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			setServer       ( XMLHandler.getTagValue(entrynode, "server") );
			setDestination  ( XMLHandler.getTagValue(entrynode, "destination") );
			setReplyAddress ( XMLHandler.getTagValue(entrynode, "replyto") );
			setSubject      ( XMLHandler.getTagValue(entrynode, "subject") );
			setIncludeDate  ( "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_date")) );
			setContactPerson( XMLHandler.getTagValue(entrynode, "concact_person") );
			setContactPhone ( XMLHandler.getTagValue(entrynode, "concact_phone") );
			setComment      ( XMLHandler.getTagValue(entrynode, "comment") );
		}
		catch(KettleException xe)
		{
			throw new KettleXMLException("Unable to load mail job entry from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);

        	// First load the common parts like name & description, then the attributes...
			//
			server          = rep.getJobEntryAttributeString (id_jobentry, "server");
			destination     = rep.getJobEntryAttributeString (id_jobentry, "destination");
			replyto         = rep.getJobEntryAttributeString (id_jobentry, "replyto");
			subject         = rep.getJobEntryAttributeString (id_jobentry, "subject");
			include_date    = rep.getJobEntryAttributeBoolean(id_jobentry, "include_date");
			contact_person  = rep.getJobEntryAttributeString (id_jobentry, "contact_person");
			contact_phone   = rep.getJobEntryAttributeString (id_jobentry, "contact_phone");
			comment         = rep.getJobEntryAttributeString (id_jobentry, "comment");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type mail from the repository with id_jobentry="+id_jobentry, dbe);
		}

	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "server", server);
			rep.saveJobEntryAttribute(id_job, getID(), "destination", destination);
			rep.saveJobEntryAttribute(id_job, getID(), "replyto", replyto);
			rep.saveJobEntryAttribute(id_job, getID(), "subject", subject);
			rep.saveJobEntryAttribute(id_job, getID(), "include_date", include_date);
			rep.saveJobEntryAttribute(id_job, getID(), "contact_person", contact_person);
			rep.saveJobEntryAttribute(id_job, getID(), "contact_phone", contact_phone);
			rep.saveJobEntryAttribute(id_job, getID(), "comment", comment);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save jobentry of type mail to the repository for id_job="+id_job, dbe);
		}
			
	}
	
	public void setServer(String s)
	{
		server=s;
	}
	
	public String getServer()
	{
		return server;
	}

	public void setDestination(String dest)
	{
		destination=dest;
	}
	
	public String getDestination()
	{
		return destination;
	}

	public void setReplyAddress(String reply)
	{
		replyto=reply;
	}
	
	public String getReplyAddress()
	{
		return replyto;
	}

	public void setSubject(String subj)
	{
		subject=subj;
	}
	
	public String getSubject()
	{
		return subject;
	}

	public void setIncludeDate(boolean incl)
	{
		include_date=incl;
	}
	
	public boolean getIncludeDate()
	{
		return include_date;
	}

	public void setContactPerson(String person)
	{
		contact_person=person;
	}
	
	public String getContactPerson()
	{
		return contact_person;
	}

	public void setContactPhone(String phone)
	{
		contact_phone=phone;
	}
	
	public String getContactPhone()
	{
		return contact_phone;
	}

	public void setComment(String comm)
	{
		comment = comm;
	}
	
	public String getComment()
	{
		return comment;
	}	
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		Result result = new Result(nr);
		
		// Send an e-mail...
		// create some properties and get the default Session
		Properties props = new Properties();
		props.put("mail.smtp.host", server);
		boolean debug = log.getLogLevel()>=LogWriter.LOG_LEVEL_DEBUG;
		
		if (debug) props.put("mail.debug", "true");

		Session session = Session.getInstance(props, null);
		session.setDebug(debug);
		
		try 
		{
		    // create a message
		    Message msg = new MimeMessage(session);
		    msg.setFrom(new InternetAddress(replyto));
		    InternetAddress[] address = {new InternetAddress(destination)};
		    msg.setRecipients(Message.RecipientType.TO, address);
		    msg.setSubject(subject);
		    msg.setSentDate(new Date());
		    String messageText = "";

		    if (comment!=null)
		    {
		        messageText+=comment+Const.CR+Const.CR;
		    }

	        messageText+="Job:"+Const.CR;
	        messageText+="-----"+Const.CR;
	        messageText+="Name       : "+parentJob.getJobinfo().getName()+Const.CR;
	        messageText+="Directory  : "+parentJob.getJobinfo().getDirectory()+Const.CR;
	        messageText+="JobEntry   : "+getName()+Const.CR;
	        messageText+=Const.CR;

		    if (include_date) 
		    {
		        Value date = new Value("date", new Date());
		        messageText += "Message date: "+date.toString()+Const.CR+Const.CR;
		    }
		    if (prev_result!=null)
		    {
		        messageText+="Previous result:"+Const.CR;
		        messageText+="-----------------"+Const.CR;
		        messageText+="Job entry nr         : "+prev_result.getEntryNr()+Const.CR;
			    messageText+="Errors               : "+prev_result.getNrErrors()+Const.CR;
			    messageText+="Lines read           : "+prev_result.getNrLinesRead()+Const.CR;
			    messageText+="Lines written        : "+prev_result.getNrLinesWritten()+Const.CR;
			    messageText+="Lines input          : "+prev_result.getNrLinesInput()+Const.CR;
			    messageText+="Lines output         : "+prev_result.getNrLinesOutput()+Const.CR;
			    messageText+="Lines updated        : "+prev_result.getNrLinesUpdated()+Const.CR;
			    messageText+="Script exit status   : "+prev_result.getExitStatus()+Const.CR;
			    messageText+="Result               : "+prev_result.getResult()+Const.CR;
			    messageText+=Const.CR;
		    }
		    
		    // Include the path to this job entry...
		    ArrayList path = parentJob.getJobEntryResults();
		    if (path!=null)
		    {
		        messageText+="Path to this job entry:"+Const.CR;
		        messageText+="------------------------"+Const.CR;
		        for (int i=0;i<path.size();i++)
		        {
		            JobEntryResult jer = (JobEntryResult) path.get(i);
			        messageText+="#"+i+" : "+jer.getThisJobEntry().getName()+Const.CR;
		        }
		    }
		    
		    msg.setText(messageText);
		    
		    Transport.send(msg);
		} 
		catch (MessagingException mex) 
		{
		    log.logError(toString(), "Problem while sending message: "+mex.toString());
			result.setNrErrors(1);

		    Exception ex = mex;
		    do 
		    {
				if (ex instanceof SendFailedException) 
				{
				    SendFailedException sfex = (SendFailedException)ex;
				    
				    Address[] invalid = sfex.getInvalidAddresses();
				    if (invalid != null) 
				    {
				    	log.logError(toString(), "    ** Invalid Addresses");
				    	if (invalid != null) 
				    	{
				    		for (int i = 0; i < invalid.length; i++) 
				    		{
				    			log.logError(toString(), "         " + invalid[i]);
				    			result.setNrErrors(1);
				    		}
				    	}
				    }
				    
				    Address[] validUnsent = sfex.getValidUnsentAddresses();
				    if (validUnsent != null) 
				    {
				    	log.logError(toString(), "    ** ValidUnsent Addresses");
				    	if (validUnsent != null) 
				    	{
				    		for (int i = 0; i < validUnsent.length; i++) 
				    		{
				    			log.logError(toString(), "         "+validUnsent[i]);
				    			result.setNrErrors(1);
				    		}
				    	}
				    }
				    
				    Address[] validSent = sfex.getValidSentAddresses();
				    if (validSent != null) 
				    {
				    	//System.out.println("    ** ValidSent Addresses");
				    	if (validSent != null) 
				    	{
				    		for (int i = 0; i < validSent.length; i++) 
				    		{
				    			log.logError(toString(), "         "+validSent[i]);
				    			result.setNrErrors(1);
				    		}
				    	}
				    }
				}
				if (ex instanceof MessagingException)
				{
				    ex = ((MessagingException)ex).getNextException();
				}
				else
				{
				    ex = null;
				}
		    } while (ex != null);
		}

		if (result.getNrErrors() > 0)
		{
			result.setResult( false );
		}
		else
		{
			result.setResult( true );
		}

		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

}
