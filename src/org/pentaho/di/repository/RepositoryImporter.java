package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

public class RepositoryImporter implements IRepositoryImporter {
  private static Class<?>              PKG           = RepositoryImporter.class; 

  private Repository rep;
  private LogChannelInterface log;

  private SharedObjects sharedObjects;
  private RepositoryDirectoryInterface baseDirectory;
  
  private boolean overwrite;
  private boolean askOverwrite  = true;

  private String versionComment;

  private boolean continueOnError;

  private String transDirOverride = null;
  private String jobDirOverride = null;
  
  public RepositoryImporter(Repository repository) {
      this.log = new LogChannel("Repository import"); //$NON-NLS-1$
      this.rep = repository;
  }
  
  public synchronized void importAll(RepositoryImportFeedbackInterface feedback, String fileDirectory, String[] filenames, RepositoryDirectoryInterface baseDirectory, boolean overwrite, boolean continueOnError, String versionComment) {
    this.baseDirectory = baseDirectory;
    this.overwrite = overwrite;
    this.continueOnError = continueOnError;
    this.versionComment = versionComment;

    feedback.setLabel(BaseMessages.getString(PKG, "RepositoryImporter.ImportXML.Label"));
    try {
      
      loadSharedObjects();

      RepositoryImportLocation.setRepositoryImportLocation(baseDirectory);

      for (int ii = 0; ii < filenames.length; ++ii) {

        final String filename = ((fileDirectory != null) && (fileDirectory.length() > 0)) ? fileDirectory + Const.FILE_SEPARATOR + filenames[ii] : filenames[ii];
        if (log.isBasic())
          log.logBasic("Import objects from XML file [" + filename + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.WhichFile.Log", filename));

        // To where?
        feedback.setLabel(BaseMessages.getString(PKG, "RepositoryImporter.WhichDir.Label"));

        // Read it using SAX...
        //
        try {
          RepositoryExportSaxParser parser = new RepositoryExportSaxParser(filename, feedback);          
          parser.parse(this);
        } catch(Exception e) {
          feedback.showError(BaseMessages.getString(PKG, "RepositoryImporter.ErrorGeneral.Title"), 
              BaseMessages.getString(PKG, "RepositoryImporter.ErrorGeneral.Message"), e);
        }        
      }
      feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.ImportFinished.Log"));
    } catch (KettleException e) {
      feedback.showError(BaseMessages.getString(PKG, "RepositoryImporter.ErrorGeneral.Title"), 
          BaseMessages.getString(PKG, "RepositoryImporter.ErrorGeneral.Message"), e);
    } finally {
      RepositoryImportLocation.setRepositoryImportLocation(null); // set to null
                                                                  // when done!
    }
  }

  /**
   * Load the shared objects up front, replace them in the xforms/jobs loaded from XML.
   * We do this for performance reasons.
   * 
   * @throws KettleException
   */
  private void loadSharedObjects() throws KettleException {
    sharedObjects = new SharedObjects();
    
    for (ObjectId id : rep.getDatabaseIDs(false)) sharedObjects.storeObject(rep.loadDatabaseMeta(id, null));
    List<SlaveServer> slaveServers = new ArrayList<SlaveServer>();
    for (ObjectId id : rep.getSlaveIDs(false)) {
      SlaveServer slaveServer = rep.loadSlaveServer(id, null);
      sharedObjects.storeObject(slaveServer);
      slaveServers.add(slaveServer);
    }
    for (ObjectId id : rep.getClusterIDs(false)) sharedObjects.storeObject(rep.loadClusterSchema(id, slaveServers, null));
    for (ObjectId id : rep.getPartitionSchemaIDs(false)) sharedObjects.storeObject(rep.loadPartitionSchema(id, null));
  }

  public void addLog(String line) {
    log.logBasic(line);
  }

  public void setLabel(String labelText) {
    log.logBasic(labelText);
  }

  public boolean transOverwritePrompt(TransMeta transMeta) {
    return overwrite;
  }
  
  public boolean jobOverwritePrompt(JobMeta jobMeta) {
    return overwrite;
  }

  public void updateDisplay() {
  }
  
  public void showError(String title, String message, Exception e) {
    log.logError(message, e);
  }

  private void replaceSharedObjects(TransMeta transMeta) {
    for (SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values()) {
      // Database...
      //
      if (sharedObject instanceof DatabaseMeta) {
        DatabaseMeta databaseMeta = (DatabaseMeta) sharedObject;
        int index = transMeta.indexOfDatabase(databaseMeta);
        if (index<0) {
          transMeta.addDatabase(databaseMeta);
        } else {
          DatabaseMeta existing = transMeta.getDatabase(index);
          existing.replaceMeta(databaseMeta);
          existing.setObjectId(databaseMeta.getObjectId());
        }
      }

      // Partition Schema...
      //
      if (sharedObject instanceof SlaveServer) {
        SlaveServer slaveServer = (SlaveServer) sharedObject;
        
        int index = transMeta.getSlaveServers().indexOf(slaveServer);
        if (index<0) {
          transMeta.getSlaveServers().add(slaveServer);
        } else {
          SlaveServer existing = transMeta.getSlaveServers().get(index);
          existing.replaceMeta(slaveServer);
          existing.setObjectId(slaveServer.getObjectId());
        }
      }

      // Cluster Schema...
      //
      if (sharedObject instanceof ClusterSchema) {
        ClusterSchema clusterSchema = (ClusterSchema) sharedObject;
        
        int index = transMeta.getClusterSchemas().indexOf(clusterSchema);
        if (index<0) {
          transMeta.getClusterSchemas().add(clusterSchema);
        } else {
          ClusterSchema existing = transMeta.getClusterSchemas().get(index);
          existing.replaceMeta(clusterSchema);
          existing.setObjectId(clusterSchema.getObjectId());
        }
      }

      // Partition Schema...
      //
      if (sharedObject instanceof PartitionSchema) {
        PartitionSchema partitionSchema = (PartitionSchema) sharedObject;
        
        int index = transMeta.getPartitionSchemas().indexOf(partitionSchema);
        if (index<0) {
          transMeta.getPartitionSchemas().add(partitionSchema);
        } else {
          PartitionSchema existing = transMeta.getPartitionSchemas().get(index);
          existing.replaceMeta(partitionSchema);
          existing.setObjectId(partitionSchema.getObjectId());
        }
      }

    }
  }
  
  private void replaceSharedObjects(JobMeta transMeta) {
    for (SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values()) {
      // Database...
      //
      if (sharedObject instanceof DatabaseMeta) {
        DatabaseMeta databaseMeta = (DatabaseMeta) sharedObject;
        int index = transMeta.indexOfDatabase(databaseMeta);
        if (index<0) {
          transMeta.addDatabase(databaseMeta);
        } else {
          DatabaseMeta existing = transMeta.getDatabase(index);
          existing.replaceMeta(databaseMeta);
          existing.setObjectId(databaseMeta.getObjectId());
        }
      }

      // Partition Schema...
      //
      if (sharedObject instanceof SlaveServer) {
        SlaveServer slaveServer = (SlaveServer) sharedObject;
        
        int index = transMeta.getSlaveServers().indexOf(slaveServer);
        if (index<0) {
          transMeta.getSlaveServers().add(slaveServer);
        } else {
          SlaveServer existing = transMeta.getSlaveServers().get(index);
          existing.replaceMeta(slaveServer);
          existing.setObjectId(slaveServer.getObjectId());
        }
      }
    }
  }


  private void patchMappingSteps(TransMeta transMeta) {
    for (StepMeta stepMeta : transMeta.getSteps()) {
      if (stepMeta.isMapping()) {
        MappingMeta mappingMeta = (MappingMeta) stepMeta.getStepMetaInterface();
        if (mappingMeta.getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME) {
          if (transDirOverride != null) {
            mappingMeta.setDirectoryPath(transDirOverride);
            continue;
          }
          String newPath = baseDirectory.getPath();
          String extraPath = mappingMeta.getDirectoryPath();
          if (newPath.endsWith("/") && extraPath.startsWith("/")) {
            newPath=newPath.substring(0,newPath.length()-1);
          } else if (!newPath.endsWith("/") && !extraPath.startsWith("/")) {
            newPath+="/";
          } else if (extraPath.equals("/")) {
            extraPath="";
          }
          mappingMeta.setDirectoryPath(newPath+extraPath);
        }
      }
    }
  }

  private void patchJobEntries(JobMeta jobMeta) {
    for (JobEntryCopy copy : jobMeta.getJobCopies()) {
      if (copy.isTransformation()) {
        JobEntryTrans entry = (JobEntryTrans) copy.getEntry();
        if (entry.getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME) {
          if (transDirOverride != null) {
            entry.setDirectory(transDirOverride);
            continue;
          }
          String newPath = baseDirectory.getPath();
          String extraPath = Const.NVL(entry.getDirectory(), "/");
          if (newPath.endsWith("/") && extraPath.startsWith("/")) {
            newPath=newPath.substring(0,newPath.length()-1);
          } else if (!newPath.endsWith("/") && !extraPath.startsWith("/")) {
            newPath+="/";
          } else if (extraPath.equals("/")) {
            extraPath="";
          }
          entry.setDirectory(newPath+extraPath);
        }
      }
      if (copy.isJob()) {
        JobEntryJob entry = (JobEntryJob) copy.getEntry();
        if (entry.getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME) {
          if (jobDirOverride != null) {
            entry.setDirectory(jobDirOverride);
            continue;
          }
          String newPath = baseDirectory.getPath();
          String extraPath = Const.NVL(entry.getDirectory(), "/");
          if (newPath.endsWith("/") && extraPath.startsWith("/")) {
            newPath=newPath.substring(0,newPath.length()-1);
          } else if (!newPath.endsWith("/") && !extraPath.startsWith("/")) {
            newPath+="/";
          } else if (extraPath.equals("/")) {
            extraPath="";
          }
          entry.setDirectory(newPath+extraPath);
        }
      }
    }
  }

  /**
   * 
   * @param transnode
   *          The XML DOM node to read the transformation from
   * @return false if the import should be canceled.
   * @throws KettleException
   *           in case there is an unexpected error
   */
  private boolean importTransformation(Node transnode, RepositoryImportFeedbackInterface feedback) throws KettleException {
    //
    // Load transformation from XML into a directory, possibly created!
    //
    TransMeta transMeta = new TransMeta(transnode, null); // ignore shared objects
    replaceSharedObjects(transMeta);
    feedback.setLabel(BaseMessages.getString(PKG, "RepositoryImporter.ImportTrans.Label", Integer.toString(transformationNumber), transMeta.getName()));

    // What's the directory path?
    String directoryPath = XMLHandler.getTagValue(transnode, "info", "directory");
    // remove the leading root, we don't need it.
    directoryPath = directoryPath.substring(1);

    RepositoryDirectoryInterface targetDirectory = baseDirectory.findDirectory(directoryPath);
    if (targetDirectory == null) {
        targetDirectory = baseDirectory.findDirectory(directoryPath);
        if (targetDirectory==null) {
          feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.CreateDir.Log", directoryPath, baseDirectory.toString()));
          targetDirectory = rep.createRepositoryDirectory(baseDirectory, directoryPath);
        }
    }

    // OK, we loaded the transformation from XML and all went well...
    // See if the transformation already existed!
    ObjectId existingId = rep.getTransformationID(transMeta.getName(), targetDirectory);
    if (existingId!=null && askOverwrite) {
      overwrite = transOverwritePrompt(transMeta);
    } else {
      updateDisplay();
    }

    if (existingId==null || overwrite) {
      transMeta.setObjectId(existingId);
      transMeta.setRepositoryDirectory(targetDirectory);
      patchMappingSteps(transMeta);

      try {
        // Keep info on who & when this transformation was created...
        if (transMeta.getCreatedUser() == null || transMeta.getCreatedUser().equals("-")) {
          transMeta.setCreatedDate(new Date());
          if (rep.getUserInfo() != null) {
            transMeta.setCreatedUser(rep.getUserInfo().getLogin());
          } else {
            transMeta.setCreatedUser(null);
          }
        }
        rep.save(transMeta, versionComment, this);
        feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.TransSaved.Log", Integer.toString(transformationNumber), transMeta.getName()));
      } catch (Exception e) {
        feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.ErrorSavingTrans.Log", Integer.toString(transformationNumber), transMeta.getName(), e.toString()));
        feedback.addLog(Const.getStackTracker(e));
      }
    } else {
      feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.ErrorSavingTrans2.Log", transMeta.getName()));
    }
    return true;
  }

  private boolean importJob(Node jobnode, RepositoryImportFeedbackInterface feedback) throws KettleException {
    // Load the job from the XML node.
    //                
    JobMeta jobMeta = new JobMeta(jobnode, rep, false, SpoonFactory.getInstance());
    replaceSharedObjects(jobMeta);
    feedback.setLabel(BaseMessages.getString(PKG, "RepositoryImporter.ImportJob.Label", Integer.toString(jobNumber), jobMeta.getName()));

    // What's the directory path?
    String directoryPath = Const.NVL(XMLHandler.getTagValue(jobnode, "directory"), Const.FILE_SEPARATOR);

    RepositoryDirectoryInterface targetDirectory = baseDirectory.findDirectory(directoryPath);
    if (targetDirectory == null) {
      targetDirectory = baseDirectory.findDirectory(directoryPath);
      if (targetDirectory==null) {
        feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.CreateDir.Log", directoryPath, baseDirectory.toString()));
        targetDirectory = rep.createRepositoryDirectory(baseDirectory, directoryPath);
      }
    }

    // OK, we loaded the job from XML and all went well...
    // See if the job already exists!
    ObjectId existintId = rep.getJobId(jobMeta.getName(), targetDirectory);
    if (existintId != null && askOverwrite) {
      overwrite = feedback.jobOverwritePrompt(jobMeta);
    } else {
      updateDisplay();
    }

    if (existintId == null || overwrite) {
      jobMeta.setRepositoryDirectory(targetDirectory);
      jobMeta.setObjectId(existintId);
      patchJobEntries(jobMeta);
      rep.save(jobMeta, versionComment, null);
      feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.JobSaved.Log", Integer.toString(jobNumber), jobMeta.getName()));
    } else {
      feedback.addLog(BaseMessages.getString(PKG, "RepositoryImporter.ErrorSavingJob.Log", jobMeta.getName()));
    }
    return true;
  }


  private int transformationNumber = 1;
  public boolean transformationElementRead(String xml, RepositoryImportFeedbackInterface feedback) {
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      Node transformationNode = XMLHandler.getSubNode(doc, RepositoryExportSaxParser.STRING_TRANSFORMATION);
      if (!importTransformation(transformationNode, feedback)) {
        return false;
      }
      transformationNumber++;
    } catch (Exception e) {
      // Some unexpected error occurred during transformation import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      feedback.showError(BaseMessages.getString(PKG, "RepositoryImporter.UnexpectedErrorDuringTransformationImport.Title"), 
          BaseMessages.getString(PKG, "RepositoryImporter.UnexpectedErrorDuringTransformationImport.Message"), e);

      if (!feedback.askContinueOnErrorQuestion(BaseMessages.getString(PKG, "RepositoryImporter.DoYouWantToContinue.Title"), 
          BaseMessages.getString(PKG, "RepositoryImporter.DoYouWantToContinue.Message"))) {
        return false;
      }
    }
    return true;
  }

  private int jobNumber = 1;
  public boolean jobElementRead(String xml, RepositoryImportFeedbackInterface feedback) {
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      Node jobNode = XMLHandler.getSubNode(doc, RepositoryExportSaxParser.STRING_JOB);
      if (!importJob(jobNode, feedback)) {
        return false;
      }
      jobNumber++;
    } catch (Exception e) {
      // Some unexpected error occurred during job import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      showError(BaseMessages.getString(PKG, "RepositoryImporter.UnexpectedErrorDuringJobImport.Title"), 
          BaseMessages.getString(PKG, "RepositoryImporter.UnexpectedErrorDuringJobImport.Message"), e);
      
      if (!feedback.askContinueOnErrorQuestion(BaseMessages.getString(PKG, "RepositoryImporter.DoYouWantToContinue.Title"), 
          BaseMessages.getString(PKG, "RepositoryImporter.DoYouWantToContinue.Message"))) {
        return false;
      }
    }
    return true;
  }

  public void fatalXmlErrorEncountered(SAXParseException e) {
    showError(
        BaseMessages.getString(PKG, "RepositoryImporter.ErrorInvalidXML.Message"),
        BaseMessages.getString(PKG, "RepositoryImporter.ErrorInvalidXML.Title"),
        e
       );
  }

  public boolean askContinueOnErrorQuestion(String title, String message) {
    return continueOnError;
  }
  
  public void beginTask(String message, int nrWorks) {
    addLog(message);
  }

  public void done() {
  }

  public boolean isCanceled() {
    return false;
  }

  public void setTaskName(String taskName) {
    addLog(taskName);
  }

  public void subTask(String message) {
    addLog(message);
  }

  public void worked(int nrWorks) {
  }
  
  public String getTransDirOverride() {
    return transDirOverride;
  }

  public void setTransDirOverride(String transDirOverride) {
    this.transDirOverride = transDirOverride;
  }

  public String getJobDirOverride() {
    return jobDirOverride;
  }

  public void setJobDirOverride(String jobDirOverride) {
    this.jobDirOverride = jobDirOverride;
  }
}
