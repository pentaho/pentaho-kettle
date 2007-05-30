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

package be.ibridge.kettle.job.entry.xsdvalidator;


import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.transform.Source;
import org.xml.sax.SAXException; 
import javax.xml.transform.stream.StreamSource;

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
 * This defines a 'xsdvalidator' job entry. 
 * 
 * @author Samatar Hassan
 * @since 30-04-2007
 *
 */
public class JobEntryXSDValidator extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String xmlfilename;
	private String xsdfilename;



	public JobEntryXSDValidator(String n)
	{
		super(n, "");
     	xmlfilename=null;
     	xsdfilename=null;

		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_XSD_VALIDATOR);
	}

	public JobEntryXSDValidator()
	{
		this("");
	}

	public JobEntryXSDValidator(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryXSDValidator je = (JobEntryXSDValidator)super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("xmlfilename", xmlfilename));
		retval.append("      ").append(XMLHandler.addTagValue("xsdfilename", xsdfilename));

		return retval.toString();
	}

	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			xmlfilename = XMLHandler.getTagValue(entrynode, "xmlfilename");
			xsdfilename = XMLHandler.getTagValue(entrynode, "xsdfilename");


		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'xsdvalidator' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			xmlfilename = rep.getJobEntryAttributeString(id_jobentry, "xmlfilename");
			xsdfilename = rep.getJobEntryAttributeString(id_jobentry, "xsdfilename");

		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'xsdvalidator' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "xmlfilename", xmlfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "xsdfilename", xsdfilename);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'xsdvalidator' to the repository for id_job="+id_job, dbe);
		}
	}

    public String getRealxmlfilename()
    {
        return StringUtil.environmentSubstitute(getxmlFilename());
    }

	

    public String getRealxsdfilename()
    {
        return StringUtil.environmentSubstitute(getxsdFilename());
    }

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );

		String realxmlfilename = getRealxmlfilename();
		String realxsdfilename = getRealxsdfilename();

	
		FileObject xmlfile = null;
		FileObject xsdfile = null;
	
		try 

		{
		
			if (xmlfilename!=null && xsdfilename!=null)
			{
				xmlfile = KettleVFS.getFileObject(realxmlfilename);
				xsdfile = KettleVFS.getFileObject(realxsdfilename);
				
				if ( xmlfile.exists() && xsdfile.exists() )
				{	
					
					SchemaFactory factorytXSDValidator_1 = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

					// Get XSD File
					File XSDFile = new File(KettleVFS.getFilename(xsdfile));
					Schema SchematXSD = factorytXSDValidator_1.newSchema(XSDFile);
					
					Validator XSDValidator = SchematXSD.newValidator();
					
					// Get XMLD File
					File xmlfiletXSDValidator_1 = new File(	KettleVFS.getFilename(xmlfile));
					
					Source sourcetXSDValidator_1 = new StreamSource(xmlfiletXSDValidator_1);
					
	
					XSDValidator.validate(sourcetXSDValidator_1);
						

					// Everything is OK
					result.setResult( true );
					
				}
				else
				{

					if(	!xmlfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryXSDValidator.FileDoesNotExist1.Label") + 
							realxmlfilename +  Messages.getString("JobEntryXSDValidator.FileDoesNotExist2.Label"));
					}
					if(!xsdfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryXSDValidator.FileDoesNotExist1.Label") + 
							realxsdfilename +  Messages.getString("JobEntryXSDValidator.FileDoesNotExist2.Label"));
					}
					result.setResult( false );
					result.setNrErrors(1);
				}

			}
			else
			{
				log.logError(toString(),  Messages.getString("JobEntryXSDValidator.AllFilesNotNull.Label"));
				result.setResult( false );
				result.setNrErrors(1);
			}


		
		}
	
		catch (SAXException ex) {
			log.logError(toString(),"Error :" + ex.getMessage());
		}
		catch ( Exception e )
		{

			log.logError(toString(), Messages.getString("JobEntryXSDValidator.ErrorXSDValidator.Label") + 
				Messages.getString("JobEntryXSDValidator.ErrorXML1.Label") + realxmlfilename +  
				Messages.getString("JobEntryXSDValidator.ErrorXML2.Label") + 
				Messages.getString("JobEntryXSDValidator.ErrorXSD1.Label") + realxsdfilename + 
				Messages.getString("JobEntryXSDValidator.ErrorXSD2.Label") + e.getMessage());
			result.setResult( false );
			result.setNrErrors(1);
		}	
		finally
		{
			try 
			{
			    if ( xmlfile != null )
			    	xmlfile.close();
			    
			    if ( xsdfile != null )
			    	xsdfile.close();
				
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
        return new JobEntryXSDValidatorDialog(shell,this,jobMeta);
    }

	public void setxmlFilename(String filename)
	{
		this.xmlfilename = filename;
	}

	public String getxmlFilename()
	{
		return xmlfilename;
	}


	public void setxsdFilename(String filename)
	{
		this.xsdfilename = filename;
	}

	public String getxsdFilename()
	{
		return xsdfilename;
	}
}