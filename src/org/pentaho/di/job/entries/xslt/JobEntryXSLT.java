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

package org.pentaho.di.job.entries.xslt;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
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
 * This defines a 'xslt' job entry.
 *
 * @author Samatar Hassan
 * @since 02-03-2007
 *
 */
public class JobEntryXSLT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryXSLT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static String FACTORY_JAXP="JAXP";
	public static String FACTORY_SAXON="SAXON";
	
	
	private String xmlfilename;
	private String xslfilename;
	private String outputfilename;
	public int iffileexists;
	private boolean addfiletoresult;
	private String xsltfactory;
	private boolean filenamesfromprevious;

	public JobEntryXSLT(String n)
	{
		super(n, "");
     	xmlfilename=null;
     	xslfilename=null;
		outputfilename=null;
		iffileexists=1;
		addfiletoresult = false;
		filenamesfromprevious=false;
		xsltfactory=FACTORY_JAXP;
		setID(-1L);
	}

	public JobEntryXSLT()
	{
		this("");
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
		retval.append("      ").append(XMLHandler.addTagValue("addfiletoresult",  addfiletoresult));
		retval.append("      ").append(XMLHandler.addTagValue("filenamesfromprevious",  filenamesfromprevious));
		retval.append("      ").append(XMLHandler.addTagValue("xsltfactory", xsltfactory));
		
		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			xmlfilename = XMLHandler.getTagValue(entrynode, "xmlfilename");
			xslfilename = XMLHandler.getTagValue(entrynode, "xslfilename");
			outputfilename = XMLHandler.getTagValue(entrynode, "outputfilename");
			iffileexists = Const.toInt(XMLHandler.getTagValue(entrynode, "iffileexists"), -1);
			addfiletoresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addfiletoresult"));
			filenamesfromprevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "filenamesfromprevious"));
			xsltfactory = XMLHandler.getTagValue(entrynode, "xsltfactory");
			if(xsltfactory==null) xsltfactory=FACTORY_JAXP;

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'xslt' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			xmlfilename = rep.getJobEntryAttributeString(id_jobentry, "xmlfilename");
			xslfilename = rep.getJobEntryAttributeString(id_jobentry, "xslfilename");
			outputfilename = rep.getJobEntryAttributeString(id_jobentry, "outputfilename");
			iffileexists=(int) rep.getJobEntryAttributeInteger(id_jobentry, "iffileexists");
			addfiletoresult=rep.getJobEntryAttributeBoolean(id_jobentry, "addfiletoresult");
			filenamesfromprevious=rep.getJobEntryAttributeBoolean(id_jobentry, "filenamesfromprevious");
			xsltfactory = rep.getJobEntryAttributeString(id_jobentry, "xsltfactory");
			if(xsltfactory==null) xsltfactory=FACTORY_JAXP;
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'xslt' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, ObjectId id_job)
		throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "xmlfilename", xmlfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "xslfilename", xslfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "outputfilename", outputfilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "iffileexists", iffileexists);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "addfiletoresult", addfiletoresult);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filenamesfromprevious", filenamesfromprevious);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "xsltfactory", xsltfactory);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'xslt' to the repository for id_job="+id_job, dbe);
		}
	}

	public String getXSLTFactory()
	{
		return xsltfactory;
	}
	
	public void setXSLTFactory(String xsltfactoryin)
	{
		xsltfactory=xsltfactoryin;
	}
	
    public String getRealxmlfilename()
    {
        return environmentSubstitute(getxmlFilename());
    }

	public String getoutputfilename()
	{
		return environmentSubstitute(getoutputFilename());
	}
    public boolean isFilenamesFromPrevious()
    {
    	return filenamesfromprevious;
    }
    public void setFilenamesFromPrevious(boolean filenamesfromprevious)
    {
    	this.filenamesfromprevious= filenamesfromprevious;
    }

    public String getRealxslfilename()
    {
        return environmentSubstitute(getxslFilename());
    }

	public Result execute(Result previousResult, int nr)  throws KettleException
	{
		Result result = previousResult;
		int NrErrors=0;
		int NrSuccess=0;
		
		List<RowMetaAndData> rows = result.getRows();
		if (isFilenamesFromPrevious())
		{
			if(log.isDetailed())	
				logDetailed(BaseMessages.getString(PKG, "JobEntryXSLT.Log.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));
		}

		if (isFilenamesFromPrevious() && rows!=null) // Copy the input row to the (command line) arguments
		{
			RowMetaAndData resultRow = null;
			for (int iteration=0;iteration<rows.size() && !parentJob.isStopped();iteration++) 
			{
				resultRow = rows.get(iteration);
	
				// Get filenames (xml, xsl, output filename)
				String xmlfilename_previous = resultRow.getString(0,null);
				String xslfilename_previous = resultRow.getString(1,null);
				String ouputfilename_previous = resultRow.getString(2,null);
				
				if (!Const.isEmpty(xmlfilename_previous) && !Const.isEmpty(xslfilename_previous) && !Const.isEmpty(ouputfilename_previous))
				{
					if(processOneXMLFile(xmlfilename_previous,  xslfilename_previous, ouputfilename_previous, result, parentJob))
						NrSuccess++;
					else
						NrErrors++;		
				}else
				{
					// We failed!
					logError( BaseMessages.getString(PKG, "JobEntryXSLT.AllFilesNotNull.Label"));
					NrErrors++;
				}
				
			}
		}else
		{
			String realxmlfilename = getRealxmlfilename();
			String realxslfilename = getRealxslfilename();
			String realoutputfilename = getoutputfilename();
			if (!Const.isEmpty(realxmlfilename) && !Const.isEmpty(realxslfilename) && !Const.isEmpty(realoutputfilename))
			{
				if(processOneXMLFile(realxmlfilename,  realxslfilename, realoutputfilename, result, parentJob))
					NrSuccess++;
				else
			    	NrErrors++;
			}
			else
			{
				// We failed!
				logError( BaseMessages.getString(PKG, "JobEntryXSLT.AllFilesNotNull.Label"));
				NrErrors++;
			}
		}
		

		result.setResult(NrErrors==0);
		result.setNrErrors(NrErrors);
		result.setNrLinesWritten(NrSuccess);
		
		return result;
	}
    private boolean processOneXMLFile(String xmlfilename, String xslfilename, String outputfilename, Result result, Job parentJob)
    {
    	boolean retval=false;
    	FileObject xmlfile = null;
		FileObject xslfile = null;
		FileObject outputfile = null;

		try
		{
			xmlfile = KettleVFS.getFileObject(xmlfilename, this);
			xslfile = KettleVFS.getFileObject(xslfilename, this);
			outputfile = KettleVFS.getFileObject(outputfilename, this);

			if ( xmlfile.exists() && xslfile.exists() )
			{
				if (outputfile.exists() && iffileexists==2)
				{
					//Output file exists
					// User want to fail
					logError(BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists1.Label")
									+ outputfilename + BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists2.Label"));
					return retval;
				}

				else if (outputfile.exists() && iffileexists==1)
				{
					// Do nothing
					if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists1.Label")
							+ outputfilename + BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists2.Label"));
					retval=true;
					return retval;
				}
				else
				{
					 if (outputfile.exists() && iffileexists==0)
						{
							// the output file exists and user want to create new one with unique name
							//Format Date

							// Try to clean filename (without wildcard)
							String wildcard = outputfilename.substring(outputfilename.length()-4,outputfilename.length());
							if(wildcard.substring(0,1).equals("."))
							{
								// Find wildcard
								outputfilename=outputfilename.substring(0,outputfilename.length()-4) +
									"_" + StringUtil.getFormattedDateTimeNow(true) + wildcard;
							}
							else
							{
								// did not find wildcard
								outputfilename=outputfilename + "_" + StringUtil.getFormattedDateTimeNow(true);
							}
						    if(log.isDebug())
						    {
						    	logDebug( BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists1.Label") +
						    	outputfilename +  BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileExists2.Label"));
						    	logDebug(BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileNameChange1.Label") + outputfilename +
						    				BaseMessages.getString(PKG, "JobEntryXSLT.OuputFileNameChange2.Label"));
						    }
						}

					
					// Create transformer factory
					TransformerFactory factory = TransformerFactory.newInstance();	
					
					if (xsltfactory.equals(FACTORY_SAXON))
					{
						// Set the TransformerFactory to the SAXON implementation.
						factory = new net.sf.saxon.TransformerFactoryImpl(); 
					}
					
					if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "JobEntryXSL.Log.TransformerFactoryInfos"),BaseMessages.getString(PKG, "JobEntryXSL.Log.TransformerFactory",factory.getClass().getName()));
			
							
					InputStream xslInputStream = KettleVFS.getInputStream(xslfile);
					InputStream xmlInputStream = KettleVFS.getInputStream(xmlfile);
					OutputStream os = null;
					try {
  					// Use the factory to create a template containing the xsl file
    						Templates template = factory.newTemplates(new StreamSource(	xslInputStream )); 
  
  					// Use the template to create a transformer
  					Transformer xformer = template.newTransformer();
  					
  					if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "JobEntryXSL.Log.TransformerClassInfos"),BaseMessages.getString(PKG, "JobEntryXSL.Log.TransformerClass",xformer.getClass().getName()));
  										
  					
  					// Prepare the input and output files
    				Source source = new StreamSource( xmlInputStream );
    				os=KettleVFS.getOutputStream(outputfile, false);
  					StreamResult resultat = new StreamResult(os);
  
  					// Apply the xsl file to the source file and write the result to the output file
  					xformer.transform(source, resultat);
  					
  					if (isAddFileToResult())
  					{
  						// Add output filename to output files
  	                	ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(outputfilename, this), parentJob.getJobname(), toString());
  	                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
  					}
  					
  
  					// Everything is OK
  					retval=true;
					} finally {
					  try {
					    xslInputStream.close();
					  } catch (IOException ignored) {
					    // ignore IO Exception on close
					  }
					  try {
					    xmlInputStream.close();
					  } catch (IOException ignored) {
					    // ignore IO Exception on close
					  }
					  try {
					   if(os!=null) os.close();
					  } catch (IOException ignored) {
					    // ignore IO Exception on close
					  }
					}
				}
			}
			else
			{

				if(	!xmlfile.exists())
				{
					logError( BaseMessages.getString(PKG, "JobEntryXSLT.FileDoesNotExist1.Label") +
						xmlfilename +  BaseMessages.getString(PKG, "JobEntryXSLT.FileDoesNotExist2.Label"));
				}
				if(!xslfile.exists())
				{
					logError( BaseMessages.getString(PKG, "JobEntryXSLT.FileDoesNotExist1.Label") +
							xmlfilename +  BaseMessages.getString(PKG, "JobEntryXSLT.FileDoesNotExist2.Label"));
				}
			}
		}
		catch ( Exception e )
		{
			logError(BaseMessages.getString(PKG, "JobEntryXSLT.ErrorXLST.Label") +
				BaseMessages.getString(PKG, "JobEntryXSLT.ErrorXLSTXML1.Label") + xmlfilename +
				BaseMessages.getString(PKG, "JobEntryXSLT.ErrorXLSTXML2.Label") +
				BaseMessages.getString(PKG, "JobEntryXSLT.ErrorXLSTXSL1.Label") + xslfilename +
				BaseMessages.getString(PKG, "JobEntryXSLT.ErrorXLSTXSL2.Label") + e.getMessage());
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
    	
    	return retval;
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
	
	public void setAddFileToResult(boolean addfiletoresultin)
	{
		this.addfiletoresult = addfiletoresultin;
	}

	public boolean isAddFileToResult()
	{
		return addfiletoresult;
	}


  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if ( (!Const.isEmpty(xslfilename)) && (!Const.isEmpty(xmlfilename)) ) {
      String realXmlFileName = jobMeta.environmentSubstitute(xmlfilename);
      String realXslFileName = jobMeta.environmentSubstitute(xslfilename);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realXmlFileName, ResourceType.FILE));
      reference.getEntries().add( new ResourceEntry(realXslFileName, ResourceType.FILE));
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
    andValidator().validate(this, "xmlFilename", remarks, ctx);//$NON-NLS-1$
    andValidator().validate(this, "xslFilename", remarks, ctx);//$NON-NLS-1$

    andValidator().validate(this, "outputFilename", remarks, putValidators(notBlankValidator()));//$NON-NLS-1$
  }

}