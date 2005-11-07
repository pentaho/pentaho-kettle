/*
 *
 *
 */

package be.ibridge.kettle.repository.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while we are exporting the complete repository to XML...
 * 
 * @author Matt
 * @since  02-jun-2005
 */
public class RepositoryExportProgressDialog
{
	private LogWriter log;
	private Props props;
	private Shell shell;
	private Repository rep;
	private String filename;
	
	public RepositoryExportProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, String filename)
	{
		this.log = log;
		this.props = props;
		this.shell = shell;
		this.rep = rep;
		this.filename = filename;
	}
	
	public boolean open()
	{
		boolean retval=true;
		
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
				    monitor.beginTask("Exporting the repository to XML...", 3);
				    
					String xml = XMLHandler.getXMLHeader();
					xml+="<repository>"+Const.CR+Const.CR;
		
					// Dump the transformations...
					xml+="<transformations>"+Const.CR;
					monitor.subTask("Exporting the transformations...");
					
					// Loop over all the directory id's
					long dirids[] = rep.getDirectoryTree().getDirectoryIDs();
					System.out.println("Going through "+dirids.length+" directories.");
					for (int d=0;d<dirids.length && !monitor.isCanceled();d++)
					{
						RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);
		
						String trans[] = rep.getTransformationNames(dirids[d]);
						for (int i=0;i<trans.length && !monitor.isCanceled();i++)
						{
							TransMeta ti = new TransMeta(rep, trans[i], repdir);
							System.out.println("Loading/Exporting transformation ["+trans[i]+"]");
							monitor.subTask("Exporting transformation ["+trans[i]+"]");
							
							xml+= ti.getXML()+Const.CR;
						}
					}
					xml+="</transformations>"+Const.CR;
					monitor.worked(1);
		
					// Now dump the jobs...
					xml+="<jobs>"+Const.CR;
					monitor.subTask("Exporting the jobs...");
					
					for (int d=0;d<dirids.length && !monitor.isCanceled();d++)
					{
						RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirids[d]);
		
						String jobs[]  = rep.getJobNames(dirids[d]);
						for (int i=0;i<jobs.length && !monitor.isCanceled();i++)
						{
							JobMeta ji = new JobMeta(log, rep, jobs[i], repdir);
							System.out.println("Loading/Exporting job ["+jobs[i]+"]");
							monitor.subTask("Exporting job ["+jobs[i]+"]");
							
							xml+=ji.getXML()+Const.CR;
						}
					}
					xml+="</jobs>"+Const.CR;
		
					xml+="</repository>"+Const.CR+Const.CR;
					
					monitor.worked(1);

					if (!monitor.isCanceled())
					{
						monitor.subTask("Saving XML to file ["+filename+"]");

						File f = new File(filename);
						try
						{
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(xml.getBytes(Const.XML_ENCODING));
							fos.close();
						}
						catch(IOException e)
						{
							System.out.println("Couldn't create file ["+filename+"]");
						}
						monitor.worked(1);
					}
					
					monitor.done();
				}
				catch(KettleException e)
				{
					e.printStackTrace();
					throw new InvocationTargetException(e, "Error creating or upgrading repository:"+Const.CR+e.getMessage()+Const.CR);
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error exporting the repository", "An error occured exporting the repository to XML!", e);
			retval=false;
		}
		catch (InterruptedException e)
		{
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error exporting the repository", "An error occured exporting the repository to XML!", e);
			retval=false;
		}

		return retval;
	}
}
