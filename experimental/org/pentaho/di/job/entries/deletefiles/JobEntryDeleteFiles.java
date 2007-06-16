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
 
package org.pentaho.di.job.entries.deletefiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;


/**
 * This defines a 'delete files' job entry.
 * 
 * @author Samatar Hassan
 * @since 06-05-2007
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private boolean ignoreErrors;
	public  boolean argFromPrevious;
	public  boolean deleteFolder;
	public  boolean includeSubfolders;
	public  String  arguments[];
	public  String  filemasks[];
	
	public JobEntryDeleteFiles(String n)
	{
		super(n, "");
		ignoreErrors=false;
		argFromPrevious=false;
		arguments=null;
		deleteFolder=false;
		includeSubfolders=false;	
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_DELETE_FILES);
	}

	public JobEntryDeleteFiles()
	{
		this("");
	}

	public JobEntryDeleteFiles(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryDeleteFiles je = (JobEntryDeleteFiles) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("ignore_errors",      ignoreErrors));
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous",  argFromPrevious));
		retval.append("      ").append(XMLHandler.addTagValue("delete_folder",      deleteFolder));
		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includeSubfolders));
		
		retval.append("      <fields>").append(Const.CR);
		if (arguments!=null)
		{
			for (int i=0;i<arguments.length;i++)
			{
				retval.append("        <field>").append(Const.CR);
				retval.append("          ").append(XMLHandler.addTagValue("name",     arguments[i]));
				retval.append("          ").append(XMLHandler.addTagValue("filemask", filemasks[i]));
				retval.append("        </field>").append(Const.CR);
			}
		}
		retval.append("      </fields>").append(Const.CR);
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			ignoreErrors      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "ignore_errors"));
			argFromPrevious   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous") );
			deleteFolder      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "delete_folder") );
			includeSubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders") );
			
			Node fields = XMLHandler.getSubNode(entrynode, "fields");
			
            // How many field arguments?
			int nrFields = XMLHandler.countNodes(fields, "field");			
			arguments = new String[nrFields];
			filemasks = new String[nrFields];
			
			// Read them all...
			for (int i = 0; i < nrFields; i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				arguments[i] = XMLHandler.getTagValue(fnode, "name");
				filemasks[i] = XMLHandler.getTagValue(fnode, "filemask");
		    }
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'delete files' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			ignoreErrors      = rep.getJobEntryAttributeBoolean(id_jobentry, "ignore_errors");
			argFromPrevious   = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			deleteFolder      = rep.getJobEntryAttributeBoolean(id_jobentry, "delete_folder");
			includeSubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");
	
			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name");
			arguments = new String[argnr];
			filemasks = new String[argnr];
			
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				arguments[a]= rep.getJobEntryAttributeString(id_jobentry, a, "name");
				filemasks[a]= rep.getJobEntryAttributeString(id_jobentry, a, "filemask");
			}
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'delete files' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
            rep.saveJobEntryAttribute(id_job, getID(), "ignore_errors",      ignoreErrors);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous",  argFromPrevious);
			rep.saveJobEntryAttribute(id_job, getID(), "delete_folder",      deleteFolder);
			rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includeSubfolders);
			
			// save the arguments...
			if (arguments!=null)
			{
				for (int i=0;i<arguments.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "name",     arguments[i]);
					rep.saveJobEntryAttribute(id_job, getID(), i, "filemask", filemasks[i]);
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'delete files' to the repository for id_job="+id_job, dbe);
		}
	}

	public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException
	{
		LogWriter log = LogWriter.getInstance();

		List rows = result.getRows();
		RowMetaAndData resultRow = null;

		boolean rcode=true ;	

		String args[] = arguments;
		String fmasks[] = filemasks;
		result.setResult( true );
		
		rcode=true;
		
		if (argFromPrevious)
		{
		    log.logDetailed(toString(), "Found "+(rows!=null?rows.size():0)+" previous result rows");
		}

		if (argFromPrevious && rows!=null) // Copy the input row to the (command line) arguments
		{
			for (int iteration=0;iteration<rows.size();iteration++) 
			{
				resultRow = (RowMetaAndData) rows.get(iteration);
				args = new String[resultRow.size()];
				fmasks = new String[resultRow.size()];

				args[iteration] = resultRow.getString(0, null);
				fmasks[iteration] = resultRow.getString(1, null);

				if(rcode)
				{
					// ok we can process this file/folder
					log.logDetailed(toString(), "Processing row ["  + args[iteration] + "]..wildcard ["+ fmasks[iteration]+"] ?");

					if(! ProcessFile(args[iteration],fmasks[iteration]))
					{
						rcode=false;
					}
				}
				else
				{
					log.logDetailed(toString(), "Ignoring row ["  + args[iteration] + "]..wildcard ["+ fmasks[iteration]+"] ?");
				}
			}
		}
		else if (arguments!=null)
		{
			for (int i=0;i<arguments.length;i++)
			{
				if(rcode)
				{
					// ok we can process this file/folder
					log.logDetailed(toString(), "Processing argument ["  + arguments[i] + "].. wildcard ["+ filemasks[i]+"] ?");
					if(!ProcessFile(arguments[i],filemasks[i]))
					{
						rcode=false;
					}
				}
				else
				{
					log.logDetailed(toString(), "Ignoring argument ["  + arguments[i] + "].. wildcard ["+ filemasks[i]+"] ?");
				}

			}
		}		
		if(!rcode && ignoreErrors)
		{
			result.setResult( false );
			result.setNrErrors(1);
		}

		result.setResult( rcode );
		
		return result;
	}

	private boolean ProcessFile(String filename,String wildcard)
	{
		LogWriter log = LogWriter.getInstance();

		boolean rcode = false ;
		FileObject filefolder = null;
		String realFilefoldername = StringUtil.environmentSubstitute(filename);
		String realwilcard = StringUtil.environmentSubstitute(wildcard);

		try
		{
			filefolder = KettleVFS.getFileObject(realFilefoldername);

			if ( filefolder.exists() )
			{
				// the file or folder exists
				if (filefolder.getType() == FileType.FOLDER)
				{
					// It's a folder
					if ( log.isDetailed())
					    log.logDetailed(toString(), "Processing folder ["+realFilefoldername+"]");
					// Delete Files
					int Nr=filefolder.delete(new TextFileSelector(realwilcard));
					
					if ( log.isDetailed())
					    log.logDetailed(toString(), "Total deleted subfolders/files = "+Nr);
					rcode=true;
				}
				else
				{
					// It's a file
					log.logDetailed(toString(), "Processing file ["+realFilefoldername+"]");	
					boolean deleted = filefolder.delete();
					if ( !deleted )
					{
						log.logError(toString(), "Could not delete file ["+realFilefoldername+"].");
					}
					else
					{
						log.logBasic(toString(), "File ["+filename+"] deleted!");
						rcode=true;
					}
				}
			}
			else
			{
				// File already deleted, no reason to try to delete it
				log.logBasic(toString(), "File or folder ["+realFilefoldername+"] already deleted.");
				rcode=true;
			}
		}		
		catch (IOException e) 
		{
			log.logError(toString(), "Could not process ["+realFilefoldername+"], exception: " + e.getMessage());					
		}
		finally 
		{
			if ( filefolder != null )
			{
				try  
				{
					filefolder.close();
				}
				catch ( IOException ex ) {};
			}
		}

	    return rcode;
	}

	private class TextFileSelector implements FileSelector 
	{
		LogWriter log = LogWriter.getInstance();
		String fileExtension;
		
		public TextFileSelector(String extension) 
		{
			if ( !Const.isEmpty(extension))
			{
				fileExtension=extension.replace('.',' ').replace('*',' ').replace('$',' ').trim();
			}
		}
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean rcode=false;
			try
			{
				String extension=info.getFile().getName().getExtension();
				if (extension.equals(fileExtension) ||  Const.isEmpty(fileExtension))
				{
					if (info.getFile().getType() == FileType.FOLDER)
					{
						if (deleteFolder && includeSubfolders)
						{
							rcode= true;
						}
						else
						{
							rcode= false;
						}
					}
					else
					{
						rcode= true;
					}
				}
				else
				{
					rcode= false;
				}
			}
			catch (Exception e) 
			{
				log.logError(toString(), "Error exception: " + e.getMessage());				
			}
			return rcode;
		}

		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return includeSubfolders;
		}
	}

	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
	{
        return new JobEntryDeleteFilesDialog(shell,this,jobMeta);
    }

	public boolean isIgnoreErrors() {
		return ignoreErrors;
	}

	public void setIgnoreErrors(boolean ignoreErrors) {
		this.ignoreErrors = ignoreErrors;
	}
	
	public void setDeleteFolder(boolean deleteFolder) 
	{
		this.deleteFolder = deleteFolder;
	}
	public void setIncludeSubfolders(boolean includeSubfolders) 
	{
		this.includeSubfolders = includeSubfolders;
	}
	
	public boolean evaluates()
	{
		return true;
	}	
}