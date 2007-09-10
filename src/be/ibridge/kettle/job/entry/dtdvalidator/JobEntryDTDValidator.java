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

package be.ibridge.kettle.job.entry.dtdvalidator;


import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;

import java.net.URL;
import java.io.BufferedReader;
import java.lang.StringBuffer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


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
		setType(JobEntryInterface.TYPE_JOBENTRY_DTD_VALIDATOR);
	}

	public JobEntryDTDValidator()
	{
		this("");
	}

	public JobEntryDTDValidator(JobEntryBase jeb)
	{
		super(jeb);
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

	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			xmlfilename = XMLHandler.getTagValue(entrynode, "xmlfilename");
			dtdfilename = XMLHandler.getTagValue(entrynode, "dtdfilename");
			dtdintern = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "dtdintern"));


		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'DTDvalidator' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			xmlfilename = rep.getJobEntryAttributeString(id_jobentry, "xmlfilename");
			dtdfilename = rep.getJobEntryAttributeString(id_jobentry, "dtdfilename");
			dtdintern=rep.getJobEntryAttributeBoolean(id_jobentry, "dtdintern");

		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'DTDvalidator' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "xmlfilename", xmlfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "DTDfilename", dtdfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "dtdintern", dtdintern);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'DTDvalidator' to the repository for id_job="+id_job, dbe);
		}
	}

    public String getRealxmlfilename()
    {
        return StringUtil.environmentSubstitute(xmlfilename);
    }

	

    public String getRealDTDfilename()
    {
        return StringUtil.environmentSubstitute(dtdfilename);
    }

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );

		String realxmlfilename = getRealxmlfilename();
		String realDTDfilename = getRealDTDfilename();

	
		FileObject xmlfile = null;
		FileObject DTDfile = null;
	
		try 

		{
		
			if (xmlfilename!=null && dtdfilename!=null)
			{
				xmlfile = KettleVFS.getFileObject(realxmlfilename);
				DTDfile = KettleVFS.getFileObject(realDTDfilename);
				
				if ( xmlfile.exists() && DTDfile.exists() )
				{	
					
					//URL xmlFile = new URL (KettleVFS.getFilename(xmlfile));
					URL xmlFile = new File(KettleVFS.getFilename(xmlfile)).toURL();
					
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
							log.logBasic(toString(),  Messages.getString("JobEntryDTDValidator.ERRORDTDFound.Label", realxmlfilename));
						}
						else
						{
							log.logBasic(toString(),  Messages.getString("JobEntryDTDValidator.ERRORDTDNotFound.Label", realxmlfilename));
						}
							
					
						
					}
					else
					{
						// DTD in external document
						// If we find an intern declaration, we remove it
						if (xmlStartDTD != -1)
						{
							int EndDTD = xmlStringbuffer.indexOf(">",xmlStartDTD);
							String DocTypeDTD = xmlStringbuffer.substring(xmlStartDTD, EndDTD + 1);
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
						
					if (dtdintern && xmlStartDTD == -1)
					{
						result.setResult( false );
						result.setNrErrors(1);
					}
					else
					{
						DocBuilderFactory.setValidating(true);
						
						// Let's parse now ...
											
						xmlDocDTD = DocBuilder.parse(new ByteArrayInputStream(xmlStringbuffer.toString().getBytes(encoding)));
	
						log.logBasic(Messages.getString("JobEntryDTDValidator.DTDValidatorOK.Subject"),
								Messages.getString("JobEntryDTDValidator.DTDValidatorOK.Label",		
										realxmlfilename));
						
						// Everything is OK
						result.setResult( true );
					}
					
				}
				else
				{

					if(	!xmlfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryDTDValidator.FileDoesNotExist.Label",	realxmlfilename));
					}
					if(!DTDfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryDTDValidator.FileDoesNotExist1.Label") + 
							realDTDfilename +  Messages.getString("JobEntryDTDValidator.FileDoesNotExist2.Label"));
					}
					result.setResult( false );
					result.setNrErrors(1);
				}

			}
			else
			{
				log.logError(toString(),  Messages.getString("JobEntryDTDValidator.AllFilesNotNull.Label"));
				result.setResult( false );
				result.setNrErrors(1);
			}


		
		}
	
		catch ( Exception e )
		{
			log.logError(Messages.getString("JobEntryDTDValidator.ErrorDTDValidator.Subject"),
					Messages.getString("JobEntryDTDValidator.ErrorDTDValidator.Label",		
							realxmlfilename,realDTDfilename,e.getMessage()));
			
			result.setResult( false );
			result.setNrErrors(1);
		}	
		finally
		{
			try 
			{
			    if ( xmlfile != null )
			    	xmlfile.close();
			    
			    if ( DTDfile != null )
			    	DTDfile.close();
				
		    }
			catch ( IOException e ) { }			
		}
		

		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryDTDValidatorDialog(shell,this,jobMeta);
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
}