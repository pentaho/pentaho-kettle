package org.pentaho.di.repository;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

public class RepositoryExporter {

	private Repository repository;
	private LogChannelInterface log;

	/**
	 * @param repository
	 */
	public RepositoryExporter(Repository repository) {
		this.log = repository.getLog();
		this.repository = repository;
	}
	
    public synchronized void exportAllObjects(ProgressMonitorListener monitor, String xmlFilename, RepositoryDirectoryInterface root, String exportType) throws KettleException
    {
    	OutputStream os = null;
    	OutputStreamWriter writer = null;
    	try
        {
            os = new BufferedOutputStream(KettleVFS.getOutputStream(xmlFilename, false));
            writer = new OutputStreamWriter(os, Const.XML_ENCODING);
            
	        if (monitor!=null) monitor.beginTask("Exporting the repository to XML...", 3);
	        
	        root = ((null == root) ? repository.loadRepositoryDirectoryTree() : root);
	        
	        writer.write(XMLHandler.getXMLHeader()); 
	        writer.write("<repository>"+Const.CR+Const.CR);
	
	        if(exportType.equals("all") || exportType.equals("trans"))
	        {
		        // Dump the transformations...
	        	writer.write("<transformations>"+Const.CR);
	        	exportTransformations(monitor, root, writer);
	        	writer.write("</transformations>"+Const.CR);
	        }
	
	        if(exportType.equals("all") || exportType.equals("jobs"))
	        {
		        // Now dump the jobs...
	        	writer.write("<jobs>"+Const.CR);
		        exportJobs(monitor, root, writer);
		        writer.write("</jobs>"+Const.CR);
	        }
	
	        writer.write("</repository>"+Const.CR+Const.CR);
	        
	        if (monitor!=null) monitor.worked(1);

            if (monitor!=null) monitor.subTask("Saving XML to file ["+xmlFilename+"]");

            if (monitor!=null) monitor.worked(1);

        }
        catch(IOException e)
        {
            System.out.println("Couldn't create file ["+xmlFilename+"]");
        }
        finally
        {
        	try {
	        	if (writer!=null) writer.close();
	        	if (os!=null) os.close();
        	}
        	catch(Exception e) {
                System.out.println("Exception closing XML file writer to ["+xmlFilename+"]");
        	}
        }
        
        if (monitor!=null) monitor.done();
    }

    private void exportJobs(ProgressMonitorListener monitor, RepositoryDirectoryInterface dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        // Loop over all the directory id's
	        ObjectId dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	 
	        if (monitor!=null) monitor.subTask("Exporting the jobs...");
	        
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));d++)
	        {
	          RepositoryDirectoryInterface repdir = dirTree.findDirectory(dirids[d]);
	
	            String jobs[]  = repository.getJobNames(dirids[d], false);
	            for (int i=0;i<jobs.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    JobMeta ji = repository.loadJob(jobs[i], repdir, null, null); // reads last version
	                    System.out.println("Loading/Exporting job ["+repdir.getPath()+" : "+jobs[i]+"]");
	                    if (monitor!=null) monitor.subTask("Exporting job ["+jobs[i]+"]");
	                    
	                    writer.write(ji.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                    log.logError("An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
	                    log.logError("Job ["+jobs[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
	                }
	            }
	        }
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository jobs", e);
    	}
    }
    
    private void exportTransformations(ProgressMonitorListener monitor, RepositoryDirectoryInterface dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        if (monitor!=null) monitor.subTask("Exporting the transformations...");
	
	        // Loop over all the directory id's
	        ObjectId dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	        
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
	        {
	          RepositoryDirectoryInterface repdir = dirTree.findDirectory(dirids[d]);
	
	            System.out.println("Directory ID #"+d+" : "+dirids[d]+" : "+repdir);
	
	            String trans[] = repository.getTransformationNames(dirids[d], false);
	            for (int i=0;i<trans.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    TransMeta ti = repository.loadTransformation(trans[i], repdir, null, true, null); // reads last version
	                    System.out.println("Loading/Exporting transformation ["+repdir.getPath()+" : "+trans[i]+"]  ("+ti.getRepositoryDirectory().getPath()+")");
	                    if (monitor!=null) monitor.subTask("Exporting transformation ["+trans[i]+"]");
	                    
	                    writer.write(ti.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                    log.logError("An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : "+ke.getMessage());
	                    log.logError("Transformation ["+trans[i]+"] from directory ["+repdir+"] was not exported because of a loading error!");
	                }
	            }
	        }
	        if (monitor!=null) monitor.worked(1);
	        
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository transformations", e);
    	}
    }
}
