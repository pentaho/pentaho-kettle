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
 
package be.ibridge.kettle.job.entry.deletefiles;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.Const;
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
import be.ibridge.kettle.core.vfs.KettleVFS;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSelectInfo;


/**
 * This defines a 'delete file' job entry. Its main use would be to delete 
 * trigger files, but it will delete any file.
 * 
 * @author Samatar Hassan
 * @since 06-05-2007
 *
 */
public class JobEntryDeleteFiles extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename;
	private boolean ignoreerreurs;
	public  boolean argFromPrevious;
	public boolean deletefolder;
	public  boolean includesubfolders;
	public  String  arguments[];
	public  String  filemasks[];
	

	
	public JobEntryDeleteFiles(String n)
	{
		super(n, "");
		filename=null;
		ignoreerreurs=false;
		argFromPrevious=false;
		arguments=null;
		deletefolder=false;
		includesubfolders=false;	
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
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("ignoreerreurs", ignoreerreurs));
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
		retval.append("      ").append(XMLHandler.addTagValue("deletefolder", deletefolder));
		retval.append("      ").append(XMLHandler.addTagValue("includesubfolders", includesubfolders));
		

		if (arguments!=null)
			for (int i=0;i<arguments.length;i++)
			{
				retval.append("      ").append(XMLHandler.addTagValue("name"+i, arguments[i]));
				retval.append("      ").append(XMLHandler.addTagValue("Filemask"+i, filemasks[i]));
			}



		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			ignoreerreurs = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "ignoreerreurs"));
			argFromPrevious = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "arg_from_previous") );
			deletefolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "deletefolder") );
			includesubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "includesubfolders") );
	
			// How many arguments?
			int argnr = 0;
			while ( XMLHandler.getTagValue(entrynode, "name"+argnr)!=null) 	argnr++;
			arguments = new String[argnr];
			filemasks = new String[argnr];
			
			
			// Read them all...
			for (int a=0;a<argnr;a++) 
			{
				arguments[a]=XMLHandler.getTagValue(entrynode, "name"+a);
				filemasks[a]=XMLHandler.getTagValue(entrynode, "Filemask"+a);

			}


		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'delete file' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			ignoreerreurs = rep.getJobEntryAttributeBoolean(id_jobentry, "ignoreerreurs");
			argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			deletefolder = rep.getJobEntryAttributeBoolean(id_jobentry, "deletefolder");
			includesubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "includesubfolders");
	
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
			throw new KettleException("Unable to load job entry of type 'delete file' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
            rep.saveJobEntryAttribute(id_job, getID(), "ignoreerreurs", ignoreerreurs);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
			rep.saveJobEntryAttribute(id_job, getID(), "deletefolder", deletefolder);
			rep.saveJobEntryAttribute(id_job, getID(), "includesubfolders", includesubfolders);
			
	
			// save the arguments...
			if (arguments!=null)
			{
				for (int i=0;i<arguments.length;i++) 
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "argument", arguments[i]);
					rep.saveJobEntryAttribute(id_job, getID(), i, "filemask", filemasks[i]);
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'delete file' to the repository for id_job="+id_job, dbe);
		}
	}


	
	public Result execute(Result result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

		List rows = result.getRows();
		Row resultRow = null;

		boolean resultat=true ;	
		boolean execResultat;
		

		String args[] = arguments;
		String fmasks[] = filemasks;
		result.setResult( true );

		
		resultat=true;
		
		if (argFromPrevious)
		{
			log.logDetailed(toString(), "Found "+(rows!=null?rows.size():0)+" previous result rows");
		}

			
			if (argFromPrevious && rows!=null) // Copy the input row to the (command line) arguments
			{
				
				for (int iteration=0;iteration<rows.size();iteration++) 
				{
					resultRow = (Row) rows.get(iteration);
					args = new String[resultRow.size()];
					fmasks = new String[resultRow.size()];

					//for (int i=0;i<resultRow.size();i++)
					//{
					args[iteration] = resultRow.getValue(0).getString();
					fmasks[iteration] = resultRow.getValue(1).getString();

					if(resultat)
					{
						// ok we can process this file/folder
	
						log.logDetailed(toString(), "Processing row ["  + args[iteration] + "]..wildcard ["+ fmasks[iteration]+"] ?");

						if(! ProcessFile(args[iteration],fmasks[iteration]))
						{
							resultat=false;
						}
					}
					else
					{
						log.logDetailed(toString(), "Ignoring row ["  + args[iteration] + "]..wildcard ["+ fmasks[iteration]+"] ?");
					}
					
					//}

				}
		
			}
			else if (arguments!=null)
			{
				
				for (int i=0;i<arguments.length;i++)
				{
					if(resultat)
					{
						// ok we can process this file/folder
						log.logDetailed(toString(), "Processing argument ["  + arguments[i] + "].. wildcard ["+ filemasks[i]+"] ?");
						if(!ProcessFile(arguments[i],filemasks[i]))
						{
							resultat=false;
						}
					}
					else
					{
						log.logDetailed(toString(), "Ignoring argument ["  + arguments[i] + "].. wildcard ["+ filemasks[i]+"] ?");
					}

				}

			}
		
		if(!resultat && ignoreerreurs)
		{
			result.setResult( false );
			result.setNrErrors(1);
		}
		


		result.setResult( resultat );

		
		return result;
	}

	private boolean ProcessFile(String filename,String wildcard)
	{
		LogWriter log = LogWriter.getInstance();

		boolean resultatex= false ;
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
					// it's a folder
					log.logDetailed(toString(), "Processing folder ["+realFilefoldername+"]");
					// Delete Files
					int Nr=filefolder.delete(new TextFileSelector(realwilcard));
					log.logDetailed(toString(), "Total deleted subfolders/files = "+Nr);

					resultatex=true;
							
		

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
						resultatex=true;
					}


				}
				

			}

			else
			{

				// File already deleted, no reason to try to delete it
				log.logBasic(toString(), "File or folder ["+realFilefoldername+"] already deleted.");

					resultatex=true;
				

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
	
		
		
            
	return resultatex;
		
	
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
			boolean retour=false;
			try
			{
				
				String extension=info.getFile().getName().getExtension();
				if (extension.equals(fileExtension) ||  Const.isEmpty(fileExtension))
				{
					if (info.getFile().getType() == FileType.FOLDER)
					{
						if (deletefolder && includesubfolders)
						{
							retour= true;
						}
						else
						{
							retour= false;
						}
					}
					else
					{
						retour= true;
					}
				}
				else
				{
					retour= false;
				}
			}
			catch (Exception e) 
			{
				log.logError(toString(), "Error, exception: " + e.getMessage());
					
			}
			return retour;

		}

		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return includesubfolders;
		}
	}

	


	public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) 
	{
        return new JobEntryDeleteFilesDialog(shell,this,jobMeta);
    }

	public boolean isIgnoreErreurs() {
		return ignoreerreurs;
	}

	public void setIgnoreErreurs(boolean failIfFileExists) {
		this.ignoreerreurs = failIfFileExists;
	}
	public void setDeleteFolder(boolean deletefolder) 
	{
		this.deletefolder = deletefolder;
	}
	public void setIncludesubfolders(boolean includesubfolders) 
	{
		this.includesubfolders = includesubfolders;
	}

	
	public boolean evaluates()
	{
		return true;
	}	
}