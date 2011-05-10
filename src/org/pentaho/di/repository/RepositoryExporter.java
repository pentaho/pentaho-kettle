/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.repository;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;

public class RepositoryExporter implements IRepositoryExporter {

	private Repository repository;
	private LogChannelInterface log;
	private ImportRules importRules;

	/**
	 * @param repository
	 */
	public RepositoryExporter(Repository repository) {
		this.log = repository.getLog();
		this.repository = repository;
		this.importRules = new ImportRules();
	}
	
	@Override
	public void setImportRulesToValidate(ImportRules importRules) {
	  this.importRules = importRules;
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
	        boolean continueOnError=true;
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));d++)
	        {
	          RepositoryDirectoryInterface repdir = dirTree.findDirectory(dirids[d]);
	
	            String jobs[]  = repository.getJobNames(dirids[d], false);
	            for (int i=0;i<jobs.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    JobMeta jobMeta = repository.loadJob(jobs[i], repdir, null, null); // reads last version
	                    
	                    // Pass the repository along in order for us to do correct exports to XML of object references
	                    //
	                    jobMeta.setRepository(repository);
	                    
	                    System.out.println("Loading/Exporting job ["+repdir.getPath()+" : "+jobs[i]+"]");
	                    if (monitor!=null) monitor.subTask("Exporting job ["+jobs[i]+"]");
	                    
	                    // Check file repository export
	                    //
	                    convertFromFileRepository(jobMeta);
	                    
                      try {
                        RepositoryImporter.validateImportedElement(importRules, jobMeta);
                      } catch(KettleException ve) {
                        continueOnError=false;
                        throw(ve);
                      }

	                    writer.write(jobMeta.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                  if (continueOnError) {
	                    log.logError("An error occurred reading job ["+jobs[i]+"] from directory ["+repdir+"] : ", ke);
	                  } else {
	                    throw ke;
	                  }
	                }
	            }
	        }
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository jobs", e);
    	}
    }
    
    private void convertFromFileRepository(JobMeta jobMeta) {

      if (repository instanceof KettleFileRepository) {
        
        KettleFileRepository fileRep = (KettleFileRepository)repository;
        
        // The id of the job is the filename.
        // Setting the filename also sets internal variables needed to load the trans/job referenced.
        //
        String jobMetaFilename = fileRep.calcFilename(jobMeta.getObjectId());
        jobMeta.setFilename(jobMetaFilename);
        
        for (JobEntryCopy copy : jobMeta.getJobCopies()) {
          JobEntryInterface entry = copy.getEntry();
          if (entry instanceof JobEntryTrans) {
            // convert to a named based reference.
            //
            JobEntryTrans trans = (JobEntryTrans) entry;
            if (trans.getSpecificationMethod()==ObjectLocationSpecificationMethod.FILENAME) {
              try {
                TransMeta meta = trans.getTransMeta(repository, jobMeta);
                FileObject fileObject = KettleVFS.getFileObject(meta.getFilename());
                trans.setSpecificationMethod(ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME);
                trans.setFileName(null);
                trans.setTransname(meta.getName());
                trans.setDirectory(Const.NVL(calcRepositoryDirectory(fileRep, fileObject), "/"));
              } catch(Exception e) {
                log.logError("Unable to load transformation specified in job entry '"+trans.getName()+"'", e);
                // Ignore this error, just leave everything the way it is.
              }
            }
          }

          if (entry instanceof JobEntryJob) {
            // convert to a named based reference.
            //
            JobEntryJob job = (JobEntryJob) entry;
            if (job.getSpecificationMethod()==ObjectLocationSpecificationMethod.FILENAME) {
              try {
                JobMeta meta = job.getJobMeta(repository, jobMeta);
                FileObject fileObject = KettleVFS.getFileObject(meta.getFilename());
                job.setSpecificationMethod(ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME);
                job.setFileName(null);
                job.setJobName(meta.getName());
                job.setDirectory(Const.NVL(calcRepositoryDirectory(fileRep, fileObject), "/"));
              } catch(Exception e) {
                log.logError("Unable to load job specified in job entry '"+job.getName()+"'", e);
                // Ignore this error, just leave everything the way it is.
              }
            }
          }
        }
      }
    }
    
    private void convertFromFileRepository(TransMeta transMeta) {

      if (repository instanceof KettleFileRepository) {
        
        KettleFileRepository fileRep = (KettleFileRepository)repository;
        
        // The id of the transformation is the relative filename.
        // Setting the filename also sets internal variables needed to load the trans/job referenced.
        //
        String transMetaFilename = fileRep.calcFilename(transMeta.getObjectId());
        transMeta.setFilename(transMetaFilename);
        
        for (StepMeta stepMeta : transMeta.getSteps()) {
          if (stepMeta.isMapping()) {
            MappingMeta mappingMeta = (MappingMeta) stepMeta.getStepMetaInterface();

            // convert to a named based reference.
            //
            if (mappingMeta.getSpecificationMethod()==ObjectLocationSpecificationMethod.FILENAME) {
              try {
                TransMeta meta = MappingMeta.loadMappingMeta(mappingMeta, repository, transMeta);
                FileObject fileObject = KettleVFS.getFileObject(meta.getFilename());
                mappingMeta.setSpecificationMethod(ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME);
                mappingMeta.setFileName(null);
                mappingMeta.setTransName(meta.getName());
                mappingMeta.setDirectoryPath(Const.NVL(calcRepositoryDirectory(fileRep, fileObject), "/"));
              } catch(Exception e) {
                log.logError("Unable to load transformation specified in map '"+mappingMeta.getName()+"'", e);
                // Ignore this error, just leave everything the way it is.
              }
            }
          }
        }
      }
    }


    private String calcRepositoryDirectory(KettleFileRepository fileRep, FileObject fileObject) throws FileSystemException{
      String path = fileObject.getParent().getName().getPath(); 
      String baseDirectory = fileRep.getRepositoryMeta().getBaseDirectory();
      // Double check!
      //
      if (path.startsWith(baseDirectory)) {
        return path.substring(baseDirectory.length());
      } else {
        return path;
      }
    }

    private void exportTransformations(ProgressMonitorListener monitor, RepositoryDirectoryInterface dirTree, OutputStreamWriter writer) throws KettleException
    {
    	try {
	        if (monitor!=null) monitor.subTask("Exporting the transformations...");
	
	        // Loop over all the directory id's
	        ObjectId dirids[] = dirTree.getDirectoryIDs();
	        System.out.println("Going through "+dirids.length+" directories in directory ["+dirTree.getPath()+"]");
	        boolean continueOnError=true;
	        for (int d=0;d<dirids.length && (monitor==null || (monitor!=null && !monitor.isCanceled()) );d++)
	        {
	          RepositoryDirectoryInterface repdir = dirTree.findDirectory(dirids[d]);
	
	            System.out.println("Directory ID #"+d+" : "+dirids[d]+" : "+repdir);
	
	            String trans[] = repository.getTransformationNames(dirids[d], false);
	            for (int i=0;i<trans.length && (monitor==null || (monitor!=null && !monitor.isCanceled()));i++)
	            {
	                try
	                {
	                    TransMeta transMeta = repository.loadTransformation(trans[i], repdir, null, true, null); // reads last version
	                    transMeta.setRepository(repository);
	                    System.out.println("Loading/Exporting transformation ["+repdir.getPath()+" : "+trans[i]+"]  ("+transMeta.getRepositoryDirectory().getPath()+")");
	                    if (monitor!=null) monitor.subTask("Exporting transformation ["+trans[i]+"]");
	                    
	                    convertFromFileRepository(transMeta);

	                    try {
	                      RepositoryImporter.validateImportedElement(importRules, transMeta);
	                    } catch(KettleException ve) {
	                      continueOnError=false;
	                      throw(ve);
	                    }
	                    
	                    writer.write(transMeta.getXML()+Const.CR);
	                }
	                catch(KettleException ke)
	                {
	                  // In case of a loading error of some kind, simply continue.
	                  // In case of a validation error against the rules, bail out.
	                  //
	                  if (continueOnError) {
	                    log.logError("An error occurred reading transformation ["+trans[i]+"] from directory ["+repdir+"] : ", ke);
	                  } else {
	                    throw ke;
	                  }
	                }
	            }
	        }
	        if (monitor!=null) monitor.worked(1);
	        
    	} catch(Exception e) {
    		throw new KettleException("Error while exporting repository transformations", e);
    	}
    }
}
