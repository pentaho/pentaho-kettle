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
 
package org.pentaho.di.job.entries.filesexist;

import java.io.IOException;
import java.util.List;

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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This defines a Files exist job entry.
 * 
 * @author Samatar
 * @since 10-12-2007
 *
 */

public class JobEntryFilesExist extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryFilesExist.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String filename;
	
	public String arguments[];
	
	public JobEntryFilesExist(String n)
	{
		super(n, ""); //$NON-NLS-1$
		filename=null;
		setID(-1L);	
	}

	public JobEntryFilesExist()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryFilesExist je = (JobEntryFilesExist) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("filename",   filename));
		
		 retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
		    if (arguments != null) {
		      for (int i = 0; i < arguments.length; i++) {
		        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
		        retval.append("          ").append(XMLHandler.addTagValue("name", arguments[i]));
		        retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
		      }
		    }
		    retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			filename      = XMLHandler.getTagValue(entrynode, "filename");
			
		    Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

	        // How many field arguments?
	        int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
	        arguments = new String[nrFields];

	        // Read them all...
	        for (int i = 0; i < nrFields; i++) {
	        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

	        arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$

	      }
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryFilesExist.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node", xe.getMessage()));
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			filename = rep.getJobEntryAttributeString(id_jobentry, "filename");
			
			 // How many arguments?
	        int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
	        arguments = new String[argnr];

	        // Read them all...
	        for (int a = 0; a < argnr; a++) 
	        {
	          arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); 
	        }
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFilesExist.ERROR_0002_Cannot_Load_Job_From_Repository",""+id_jobentry, dbe.getMessage()));
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "filename", filename);
			
			   // save the arguments...
		    if (arguments != null) {
		       for (int i = 0; i < arguments.length; i++) {
		          rep.saveJobEntryAttribute(id_job, getObjectId(), i, "name", arguments[i]);
		       }
		    }
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryFilesExist.ERROR_0003_Cannot_Save_Job_Entry",""+id_job, dbe.getMessage()));
		}
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	public String getFilename()
	{
		return filename;
	}
    

	
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );
		int missingfiles=0;
		
		if (arguments != null) 
		{
		      for (int i = 0; i < arguments.length && !parentJob.isStopped(); i++) 
		      {
		    	  FileObject file =null;
		      
		    	  try
		            {
		    		   String realFilefoldername = environmentSubstitute(arguments[i]);
		    		   file = KettleVFS.getFileObject(realFilefoldername, this);
		    		  
		    		    if (file.exists() && file.isReadable())
		    		    {
		    		    	if(log.isDetailed())
		    		    		logDetailed(BaseMessages.getString(PKG, "JobEntryFilesExist.File_Exists", realFilefoldername)); //$NON-NLS-1$
		    		    }
		                else
		                {
		                	missingfiles ++;
		                	result.setNrErrors(missingfiles);
		                	if(log.isDetailed())
		                		logDetailed(BaseMessages.getString(PKG, "JobEntryFilesExist.File_Does_Not_Exist", realFilefoldername)); //$NON-NLS-1$
		                }
		    		  
		            }
		    	  	catch (Exception e)
		            {
		    	  		missingfiles ++;
		                result.setNrErrors(missingfiles);
		                logError(BaseMessages.getString(PKG, "JobEntryFilesExist.ERROR_0004_IO_Exception", e.toString()), e); //$NON-NLS-1$
		            }
		    	  	finally
		    	  	{
		    	  		if (file != null) {try {file.close();file=null;} catch (IOException ex) {};}
		    	  	}
		      }
		        
		}
		
		if(missingfiles==0) 
			result.setResult(true);
		
		return result;
	}    

	public boolean evaluates()
	{
		return true;
	}
    
   @Override
   public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
   }

}
