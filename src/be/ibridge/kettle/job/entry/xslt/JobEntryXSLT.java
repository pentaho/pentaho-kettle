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

package be.ibridge.kettle.job.entry.xslt;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
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
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;


/**
 * This defines a 'xslt' job entry. 
 * 
 * @author Samatar Hassan
 * @since 02-03-2007
 *
 */
public class JobEntryXSLT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String xmlfilename;
	private String xslfilename;
	private String outputfilename;
	public int iffileexists;


	public JobEntryXSLT(String n)
	{
		super(n, "");
     	xmlfilename=null;
     	xslfilename=null;
		outputfilename=null;
		iffileexists=1;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_XSLT);
	}

	public JobEntryXSLT()
	{
		this("");
	}

	public JobEntryXSLT(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryXSLT je = (JobEntryXSLT)super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("xmlfilename", xmlfilename));
		retval.append("      ").append(XMLHandler.addTagValue("xslfilename", xslfilename));
		retval.append("      ").append(XMLHandler.addTagValue("outputfilename", outputfilename));
		retval.append("      ").append(XMLHandler.addTagValue("iffileexists",  iffileexists));
	

		return retval.toString();
	}

	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			xmlfilename = XMLHandler.getTagValue(entrynode, "xmlfilename");
			xslfilename = XMLHandler.getTagValue(entrynode, "xslfilename");
			outputfilename = XMLHandler.getTagValue(entrynode, "outputfilename");
			iffileexists = Const.toInt(XMLHandler.getTagValue(entrynode, "iffileexists"), -1);

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'xslt' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			xmlfilename = rep.getJobEntryAttributeString(id_jobentry, "xmlfilename");
			xslfilename = rep.getJobEntryAttributeString(id_jobentry, "xslfilename");
			outputfilename = rep.getJobEntryAttributeString(id_jobentry, "outputfilename");
			iffileexists=(int) rep.getJobEntryAttributeInteger(id_jobentry, "iffileexists");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'xslt' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "xmlfilename", xmlfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "xslfilename", xslfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "outputfilename", outputfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "iffileexists", iffileexists);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'xslt' to the repository for id_job="+id_job, dbe);
		}
	}

    public String getRealxmlfilename()
    {
        return StringUtil.environmentSubstitute(getxmlFilename());
    }

	public String getRealoutputfilename()
	{
		return StringUtil.environmentSubstitute(getoutputFilename());
	}


    public String getRealxslfilename()
    {
        return StringUtil.environmentSubstitute(getxslFilename());
    }

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );

		String realxmlfilename = getRealxmlfilename();
		String realxslfilename = getRealxslfilename();
		String realoutputfilename = getRealoutputfilename();

	
		FileObject xmlfile = null;
		FileObject xslfile = null;
		FileObject outputfile = null;

		try 

		{
		
			if (xmlfilename!=null && xslfilename!=null && outputfilename!=null)
			{
				xmlfile = KettleVFS.getFileObject(realxmlfilename);
				xslfile = KettleVFS.getFileObject(realxslfilename);
				outputfile = KettleVFS.getFileObject(realoutputfilename);

				if ( xmlfile.exists() && xslfile.exists() )
				{	
					if (outputfile.exists() && iffileexists==2) 
					{
						//Output file exists
						// User want to fail
						log.logError(toString(), Messages.getString("JobEntryXSLT.OuputFileExists1.Label") 
										+ realoutputfilename + Messages.getString("JobEntryXSLT.OuputFileExists2.Label"));
						result.setResult( false );
						result.setNrErrors(1);


					}
				
					else if (outputfile.exists() && iffileexists==1)
					{
						// Do nothing
						log.logDebug(toString(), Messages.getString("JobEntryXSLT.OuputFileExists1.Label")
								+ realoutputfilename + Messages.getString("JobEntryXSLT.OuputFileExists2.Label"));
						result.setResult( true );
					}
					else
					{
								
	
						 if (outputfile.exists() && iffileexists==0)
							{
								// the zip file exists and user want to create new one with unique name
								//Format Date
		
								DateFormat dateFormat = new SimpleDateFormat("hhmmss_MMddyyyy");
								// Try to clean filename (without wildcard)
								String wildcard = realoutputfilename.substring(realoutputfilename.length()-4,realoutputfilename.length());
								if(wildcard.substring(0,1).equals("."))
								{
									// Find wildcard			
									realoutputfilename=realoutputfilename.substring(0,realoutputfilename.length()-4) + 
										"_" + dateFormat.format(new Date()) + wildcard;
								}
								else
								{
									// did not find wilcard
									realoutputfilename=realoutputfilename + "_" + dateFormat.format(new Date());
								}
							    log.logDebug(toString(),  Messages.getString("JobEntryXSLT.OuputFileExists1.Label") + 
										realoutputfilename +  Messages.getString("JobEntryXSLT.OuputFileExists2.Label"));
								log.logDebug(toString(), Messages.getString("JobEntryXSLT.OuputFileNameChange1.Label") + realoutputfilename + 
								Messages.getString("JobEntryXSLT.OuputFileNameChange2.Label"));

							
							}

						//String xmlSystemXML = new File(realxmlfilename).toURL().toExternalForm(  );
						//String xsltSystemXSL = new File(realxslfilename).toURL().toExternalForm(  );
								
						// Create transformer factory
						TransformerFactory factory = TransformerFactory.newInstance();
		    
						// Use the factory to create a template containing the xsl file
						Templates template = factory.newTemplates(new StreamSource(	KettleVFS.getInputStream(xslfile))); 
		    
						// Use the template to create a transformer
						Transformer xformer = template.newTransformer();
		    
						// Prepare the input and output files
						Source source = new StreamSource(KettleVFS.getInputStream(xmlfile));
						StreamResult resultat = new StreamResult(KettleVFS.getOutputStream(outputfile, false));

						// Apply the xsl file to the source file and write the result to the output file
						xformer.transform(source, resultat);

						// Everything is OK
						result.setResult( true );
					}
				}
				else
				{

					if(	!xmlfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryXSLT.FileDoesNotExist1.Label") + 
							realxmlfilename +  Messages.getString("JobEntryXSLT.FileDoesNotExist2.Label"));
					}
					if(!xslfile.exists())
					{
						log.logError(toString(),  Messages.getString("JobEntryXSLT.FileDoesNotExist1.Label") + 
							realxslfilename +  Messages.getString("JobEntryXSLT.FileDoesNotExist2.Label"));
					}
					result.setResult( false );
					result.setNrErrors(1);
				}

			}
			else
			{
				log.logError(toString(),  Messages.getString("JobEntryXSLT.AllFilesNotNull.Label"));
				result.setResult( false );
				result.setNrErrors(1);
			}


		
		}
	

		catch ( Exception e )
		{

			log.logError(toString(), Messages.getString("JobEntryXSLT.ErrorXLST.Label") + 
				Messages.getString("JobEntryXSLT.ErrorXLSTXML1.Label") + realxmlfilename +  
				Messages.getString("JobEntryXSLT.ErrorXLSTXML2.Label") + 
				Messages.getString("JobEntryXSLT.ErrorXLSTXSL1.Label") + realxslfilename + 
				Messages.getString("JobEntryXSLT.ErrorXLSTXSL2.Label") + e.getMessage());
			result.setResult( false );
			result.setNrErrors(1);
		}	
		finally
		{
			try 
			{
			    if ( xmlfile != null )
			    	xmlfile.close();
			    
			    if ( xslfile != null )
			    	xslfile.close();	
				if ( outputfile != null )
					outputfile.close();	
				
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
        return new JobEntryXSLTDialog(shell,this,jobMeta);
    }

	public void setxmlFilename(String filename)
	{
		this.xmlfilename = filename;
	}

	public String getxmlFilename()
	{
		return xmlfilename;
	}

	public String getoutputFilename()
	{
		return outputfilename;
	}


	public void setoutputFilename(String outputfilename)
	{
		this.outputfilename = outputfilename;
	}

	public void setxslFilename(String filename)
	{
		this.xslfilename = filename;
	}

	public String getxslFilename()
	{
		return xslfilename;
	}
}