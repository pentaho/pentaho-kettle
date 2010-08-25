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

package org.pentaho.di.job.entries.dtdvalidator;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;


/**
 * This defines a 'dtdvalidator' job entry. 
 * 
 * @author Samatar Hassan
 * @since 30-04-2007
 *
 */

public class JobEntryDTDValidator extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String xmlfilename;
	private String dtdfilename;
	private boolean dtdintern;



	public JobEntryDTDValidator(String n)
	{
		super(n, "");
     	xmlfilename=null;
     	dtdfilename=null;
     	dtdintern=false;

		setID(-1L);
	}

	public JobEntryDTDValidator()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryDTDValidator je = (JobEntryDTDValidator)super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("xmlfilename", xmlfilename));
		retval.append("      ").append(XMLHandler.addTagValue("dtdfilename", dtdfilename));
		retval.append("      ").append(XMLHandler.addTagValue("dtdintern",  dtdintern));
		

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
	
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			xmlfilename = XMLHandler.getTagValue(entrynode, "xmlfilename");
			dtdfilename = XMLHandler.getTagValue(entrynode, "dtdfilename");
			dtdintern = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "dtdintern"));


		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'DTDvalidator' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			xmlfilename = rep.getJobEntryAttributeString(id_jobentry, "xmlfilename");
			dtdfilename = rep.getJobEntryAttributeString(id_jobentry, "dtdfilename");
			dtdintern=rep.getJobEntryAttributeBoolean(id_jobentry, "dtdintern");

		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'DTDvalidator' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "xmlfilename", xmlfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "DTDfilename", dtdfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "dtdintern", dtdintern);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'DTDvalidator' to the repository for id_job="+id_job, dbe);
		}
	}

    public String getRealxmlfilename()
    {
        return environmentSubstitute(xmlfilename);
    }

	

    public String getRealDTDfilename()
    {
        return environmentSubstitute(dtdfilename);
    }

	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( true );

		String realxmlfilename = getRealxmlfilename();
		String realDTDfilename = getRealDTDfilename();
	
		// Define a new DTD validator instance
		DTDValidator validator = new DTDValidator(log);
		// Set XML filename
		validator.setXMLFilename(realxmlfilename);
		if(dtdintern) {
			// The DTD is intern to XML file
			validator.setInternDTD(true);
		} else {
			// The DTD is extern
			// set the DTD filename
			validator.setDTDFilename(realDTDfilename);
		}
		// Validate the XML file and return the status
		boolean status = validator.validate();
		if(!status) {
			// The XML file is invalid!
			log.logError(validator.getErrorMessage());
			result.setResult(false);
			result.setNrErrors(validator.getNrErrors());
			result.setLogText(validator.getErrorMessage());
		}
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

	public void setxmlFilename(String filename)
	{
		this.xmlfilename = filename;
	}

	public String getxmlFilename()
	{
		return xmlfilename;
	}


	public void setdtdFilename(String filename)
	{
		this.dtdfilename = filename;
	}

	public String getdtdFilename()
	{
		return dtdfilename;
	}
	
	public boolean getDTDIntern()
	{
		return dtdintern;
	}
	
	public void setDTDIntern(boolean dtdinternin)
	{
		this.dtdintern=dtdinternin;
	}
	
	public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
	    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
	    if ( (!Const.isEmpty(dtdfilename)) && (!Const.isEmpty(xmlfilename)) ) {
	      String realXmlFileName = jobMeta.environmentSubstitute(xmlfilename);
	      String realXsdFileName = jobMeta.environmentSubstitute(dtdfilename);
	      ResourceReference reference = new ResourceReference(this);
	      reference.getEntries().add( new ResourceEntry(realXmlFileName, ResourceType.FILE));
	      reference.getEntries().add( new ResourceEntry(realXsdFileName, ResourceType.FILE));
	      references.add(reference);
	    }
	    return references;
	  }

	  @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    ValidatorContext ctx = new ValidatorContext();
	    putVariableSpace(ctx, getVariables());
	    putValidators(ctx, notBlankValidator(), fileExistsValidator());
	    andValidator().validate(this, "dtdfilename", remarks, ctx);//$NON-NLS-1$
	    andValidator().validate(this, "xmlFilename", remarks, ctx);//$NON-NLS-1$
	  }
}