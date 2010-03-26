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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Document;
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
	private static Class<?> PKG = JobEntryDTDValidator.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
		result.setResult( false );

		String realxmlfilename = getRealxmlfilename();
		String realDTDfilename = getRealDTDfilename();

	
		FileObject xmlfile = null;
		FileObject DTDfile = null;
	
		try 

		{
		
			if (xmlfilename!=null &&  ((dtdfilename!=null && !dtdintern) || (dtdintern))   )
			{
				xmlfile = KettleVFS.getFileObject(realxmlfilename, this);
				
				
				if ( xmlfile.exists())   
					
				{	
					
					//URL xmlFile = new URL (KettleVFS.getFilename(xmlfile));
					URL xmlFile = new File(KettleVFS.getFilename(xmlfile)).toURI().toURL();
					
					// open XML File
					BufferedReader xmlBufferedReader = new BufferedReader(new InputStreamReader(xmlFile.openStream()));
					StringBuffer xmlStringbuffer = new StringBuffer("");
					
					char[] buffertXML = new char[1024];
					int LenXML = -1;
					while ((LenXML = xmlBufferedReader.read(buffertXML)) != -1)
						xmlStringbuffer.append(buffertXML, 0,LenXML);
					
					// Prepare parsing ...
					DocumentBuilderFactory DocBuilderFactory = DocumentBuilderFactory.newInstance();
					Document xmlDocDTD=null; 
					DocumentBuilder DocBuilder = DocBuilderFactory.newDocumentBuilder();
					
					// Let's try to get XML document encoding
					DocBuilderFactory.setValidating(false);
					xmlDocDTD = DocBuilder.parse(new ByteArrayInputStream(xmlStringbuffer.toString().getBytes("UTF-8")));
					
					String encoding = null;
					if (xmlDocDTD.getXmlEncoding() == null) 
					{
						encoding = "UTF-8";
					} 
					else 
					{
						encoding = xmlDocDTD.getXmlEncoding();
					}
					
					int xmlStartDTD = xmlStringbuffer.indexOf("<!DOCTYPE");
					 
					if (dtdintern)
					{
						// DTD find in the XML document
						if (xmlStartDTD != -1)
						{
							if(log.isBasic())
								logBasic( BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFound.Label", realxmlfilename));
						}
						else
						{
							if(log.isBasic())
								logBasic( BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDNotFound.Label", realxmlfilename));
						}
							
					
						
					}
					else
					{
						// DTD in external document
						// If we find an intern declaration, we remove it
						DTDfile = KettleVFS.getFileObject(realDTDfilename, this);
						
						if (DTDfile.exists())
						{
							if (xmlStartDTD != -1)
							{
								int EndDTD = xmlStringbuffer.indexOf(">",xmlStartDTD);
								//String DocTypeDTD = xmlStringbuffer.substring(xmlStartDTD, EndDTD + 1);
								xmlStringbuffer.replace(xmlStartDTD,EndDTD + 1, "");
				
							}
							
							
							String xmlRootnodeDTD = xmlDocDTD.getDocumentElement().getNodeName();
								
							String RefDTD = "<?xml version='"
								+ xmlDocDTD.getXmlVersion() + "' encoding='"
								+ encoding + "'?>\n<!DOCTYPE " + xmlRootnodeDTD
								+ " SYSTEM '" + KettleVFS.getFilename(DTDfile) + "'>\n";
	
							int xmloffsetDTD = xmlStringbuffer.indexOf("<"+ xmlRootnodeDTD);
							xmlStringbuffer.replace(0, xmloffsetDTD,RefDTD);
						}
						else
						{
							log.logError(BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFileNotExists.Subject"), BaseMessages.getString(PKG, "JobEntryDTDValidator.ERRORDTDFileNotExists.Msg",realDTDfilename));
						}
					}
						
					if ((dtdintern && xmlStartDTD == -1 || (!dtdintern && !DTDfile.exists())))
					{
						result.setResult( false );
						result.setNrErrors(1);
					}
					else
					{
						DocBuilderFactory.setValidating(true);
						
						// Let's parse now ...
											
						xmlDocDTD = DocBuilder.parse(new ByteArrayInputStream(xmlStringbuffer.toString().getBytes(encoding)));
						if(log.isDetailed())
							log.logDetailed(BaseMessages.getString(PKG, "JobEntryDTDValidator.DTDValidatorOK.Subject"),
								BaseMessages.getString(PKG, "JobEntryDTDValidator.DTDValidatorOK.Label",		
										realxmlfilename));
						
						// Everything is OK
						result.setResult( true );
					}
					
				}
				else
				{

					if(	!xmlfile.exists())
					{
						logError( BaseMessages.getString(PKG, "JobEntryDTDValidator.FileDoesNotExist.Label",	realxmlfilename));
					}
					
					result.setResult( false );
					result.setNrErrors(1);
				}

			}
			else
			{
				logError( BaseMessages.getString(PKG, "JobEntryDTDValidator.AllFilesNotNull.Label"));
				result.setResult( false );
				result.setNrErrors(1);
			}


		
		}
	
		catch ( Exception e )
		{
			log.logError(BaseMessages.getString(PKG, "JobEntryDTDValidator.ErrorDTDValidator.Subject"),
					BaseMessages.getString(PKG, "JobEntryDTDValidator.ErrorDTDValidator.Label",		
							realxmlfilename,realDTDfilename,e.getMessage()));
			
			result.setResult( false );
			result.setNrErrors(1);
		}	
		finally
		{
			try 
			{
			    if ( xmlfile != null ) {
			    	xmlfile.close();
			    	xmlfile=null;
			    }
			    
			    if ( DTDfile != null ) {
			    	DTDfile.close();
			    	DTDfile=null;
			    }
				
		    }
			catch ( IOException e ) { }			
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