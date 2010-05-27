/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
/*
 *
 *
 */

package org.pentaho.di.ui.repository.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryImportLocation;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 * Takes care of displaying a dialog that will handle the wait while we are
 * importing a backup file from XML...
 * 
 * @author Matt
 * @since 03-jun-2005
 */
public class RepositoryImportProgressDialog extends Dialog implements ProgressMonitorListener, RepositoryElementReadListener {
  private static Class<?>              PKG           = RepositoryImportProgressDialog.class; 

  private LogChannelInterface          log;
  private Shell                        shell, parent;
  private Display                      display;
  private PropsUI                      props;

  private Repository                   rep;
  private String                       fileDirectory;
  private String[]                     filenames;
  private RepositoryDirectoryInterface baseDirectory;
  private Label                        wLabel;
  private Text                         wLogging;
  private Button                       wClose;

  private boolean                      overwrite     = false;
  private boolean                      askOverwrite  = true;
  private boolean                      makeDirectory = false;
  private boolean                      askDirectory  = true;

  private String                       versionComment;
  private SharedObjects sharedObjects;

  public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String fileDirectory, String[] filenames, RepositoryDirectoryInterface baseDirectory, String versionComment) {
    super(parent, style);

    this.log = new LogChannel("Repository import");
    this.props = PropsUI.getInstance();
    this.parent = parent;
    this.rep = rep;
    this.fileDirectory = fileDirectory;
    this.filenames = filenames;
    this.baseDirectory = baseDirectory;
    this.versionComment = versionComment;
  }

  public void open() {
    display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.Title"));
    shell.setImage(GUIResource.getInstance().getImageSpoon());
    shell.setLayout(formLayout);

    // 
    // The task line...
    // ////////////////////////////////////////////////////////////////

    wLabel = new Label(shell, SWT.LEFT);
    props.setLook(wLabel);

    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment(0, 0);
    fdLabel.top = new FormAttachment(0, 0);
    fdLabel.right = new FormAttachment(100, 0);
    wLabel.setLayoutData(fdLabel);

    //
    // The close button...
    // ////////////////////////////////////////////////////////////////

    // Buttons
    wClose = new Button(shell, SWT.PUSH);
    wClose.setText(BaseMessages.getString(PKG, "System.Button.Close"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose }, Const.MARGIN, (Control) null);

    wClose.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        dispose();
      }
    });

    // 
    // Then the logging...
    // ////////////////////////////////////////////////////////////////

    wLogging = new Text(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    props.setLook(wLabel);

    FormData fdLogging = new FormData();
    fdLogging.left = new FormAttachment(0, 0);
    fdLogging.top = new FormAttachment(wLabel, Const.MARGIN);
    fdLogging.right = new FormAttachment(100, 0);
    fdLogging.bottom = new FormAttachment(wClose, -Const.MARGIN);
    wLogging.setLayoutData(fdLogging);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        dispose();
      }
    });

    BaseStepDialog.setSize(shell, 640, 480, true);

    shell.open();

    display.asyncExec(new Runnable() {
      public void run() {
        importAll();
      }
    });

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  private void addLog(String line) {
    String rest = wLogging.getText();
    wLogging.setText(rest + line + Const.CR);
    wLogging.setSelection(wLogging.getText().length()); // make it scroll
  }

  /**
   * 
   * @param transformationNumber
   *          the transformation number (for logging only)
   * @param transnode
   *          The XML DOM node to read the transformation from
   * @return false if the import should be canceled.
   * @throws KettleException
   *           in case there is an unexpected error
   */
  private boolean importTransformation(Node transnode) throws KettleException {
    //
    // Load transformation from XML into a directory, possibly created!
    //
    TransMeta transMeta = new TransMeta(transnode, null); // ignore shared objects
    replaceSharedObjects(transMeta);
    wLabel.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.ImportTrans.Label", Integer.toString(transformationNumber), transMeta.getName()));

    // What's the directory path?
    String directoryPath = XMLHandler.getTagValue(transnode, "info", "directory");
    // remove the leading root, we don't need it.
    directoryPath = directoryPath.substring(1);

    RepositoryDirectoryInterface targetDirectory = baseDirectory.findDirectory(directoryPath);
    if (targetDirectory == null) {
      if (askDirectory) {
        MessageDialogWithToggle mb = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Message", directoryPath),
            MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No"), BaseMessages.getString(PKG, "System.Button.Cancel") }, 1, BaseMessages.getString(PKG,
                "RepositoryImportDialog.DontAskAgain.Label"), !askDirectory);
        MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        int answer = mb.open();
        makeDirectory = (answer & 0xFF) == 0;
        askDirectory = !mb.getToggleState();

        // Cancel?
        if ((answer & 0xFF) == 1)
          return false;
      }

      if (makeDirectory) {
        RepositoryDirectoryInterface baseDir = rep.loadRepositoryDirectoryTree().findDirectory(baseDirectory.getPath());
        targetDirectory = baseDir.findDirectory(directoryPath);
        if (targetDirectory==null) {
          addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Log", directoryPath, baseDirectory.toString()));
          targetDirectory = rep.createRepositoryDirectory(baseDirectory, directoryPath);
        }
      } else {
        targetDirectory = baseDirectory;
      }
    }

    // OK, we loaded the transformation from XML and all went well...
    // See if the transformation already existed!
    ObjectId existingId = rep.getTransformationID(transMeta.getName(), targetDirectory);
    if (existingId!=null && askOverwrite) {
      MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteTrans.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteTrans.Message", transMeta.getName()),
          MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No") }, 1, BaseMessages.getString(PKG, "RepositoryImportDialog.DontAskAgain.Label"), !askOverwrite);
      MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      int answer = md.open();
      overwrite = (answer & 0xFF) == 0;
      askOverwrite = !md.getToggleState();
    } else {
      shell.getDisplay().update();
    }

    if (existingId==null || overwrite) {
      transMeta.setObjectId(existingId);
      transMeta.setRepositoryDirectory(targetDirectory);
      patchMappingSteps(transMeta);

      try {
        // Keep info on who & when this transformation was created...
        if (transMeta.getCreatedUser() == null || transMeta.getCreatedUser().equals("-")) {
          transMeta.setCreatedDate(new Date());
          transMeta.setCreatedUser(rep.getUserInfo().getLogin());
        } else {
          transMeta.setCreatedDate(transMeta.getCreatedDate());
          transMeta.setCreatedUser(transMeta.getCreatedUser());
        }

        // Keep info on who & when this transformation was changed...
        transMeta.setModifiedDate(new Date());
        transMeta.setModifiedUser(rep.getUserInfo().getLogin());
        rep.save(transMeta, versionComment, this);
        addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.TransSaved.Log", Integer.toString(transformationNumber), transMeta.getName()));
      } catch (Exception e) {
        addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorSavingTrans.Log", Integer.toString(transformationNumber), transMeta.getName(), e.toString()));
        addLog(Const.getStackTracker(e));
      }
    } else {
      addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorSavingTrans2.Log", transMeta.getName()));
    }
    return true;
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

  private boolean importJob(Node jobnode) throws KettleException {
    // Load the job from the XML node.
    //                
    JobMeta jobMeta = new JobMeta(jobnode, rep, false, SpoonFactory.getInstance());
    replaceSharedObjects(jobMeta);
    wLabel.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.ImportJob.Label", Integer.toString(jobNumber), jobMeta.getName()));

    // What's the directory path?
    String directoryPath = Const.NVL(XMLHandler.getTagValue(jobnode, "directory"), Const.FILE_SEPARATOR);

    RepositoryDirectoryInterface targetDirectory = baseDirectory.findDirectory(directoryPath);
    if (targetDirectory == null) {
      if (askDirectory) {
        MessageDialogWithToggle mb = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Message", directoryPath),
            MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No"), BaseMessages.getString(PKG, "System.Button.Cancel") }, 1, BaseMessages.getString(PKG,
                "RepositoryImportDialog.DontAskAgain.Label"), !askDirectory);
        MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
        int answer = mb.open();
        makeDirectory = ((answer & 0xFF) == 0);
        askDirectory = !mb.getToggleState();

        // Cancel?
        if ((answer & 0xFF) == 2)
          return false;
      }

      if (makeDirectory) {
        RepositoryDirectoryInterface baseDir = rep.loadRepositoryDirectoryTree().findDirectory(baseDirectory.getPath());
        targetDirectory = baseDir.findDirectory(directoryPath);
        if (targetDirectory==null) {
          addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.CreateDir.Log", directoryPath, baseDirectory.toString()));
          targetDirectory = rep.createRepositoryDirectory(baseDirectory, directoryPath);
        }
      } else {
        targetDirectory = baseDirectory;
      }
    }

    // OK, we loaded the job from XML and all went well...
    // See if the job already exists!
    ObjectId existintId = rep.getJobId(jobMeta.getName(), targetDirectory);
    if (existintId != null && askOverwrite) {
      MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteJob.Title"), null, BaseMessages.getString(PKG, "RepositoryImportDialog.OverwriteJob.Message", jobMeta.getName()),
          MessageDialog.QUESTION, new String[] { BaseMessages.getString(PKG, "System.Button.Yes"), BaseMessages.getString(PKG, "System.Button.No") }, 1, BaseMessages.getString(PKG, "RepositoryImportDialog.DontAskAgain.Label"), !askOverwrite);
      MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      int answer = md.open();
      overwrite = (answer & 0xFF) == 0;
      askOverwrite = !md.getToggleState();
    } else {
      shell.getDisplay().update();
    }

    if (existintId == null || overwrite) {
      jobMeta.setRepositoryDirectory(targetDirectory);
      jobMeta.setObjectId(existintId);
      patchJobEntries(jobMeta);
      rep.save(jobMeta, versionComment, null);
      addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.JobSaved.Log", Integer.toString(jobNumber), jobMeta.getName()));
    } else {
      addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorSavingJob.Log", jobMeta.getName()));
    }
    return true;
  }

  private void importAll() {
    wLabel.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.ImportXML.Label"));
    try {
      
      loadSharedObjects();

      RepositoryImportLocation.setRepositoryImportLocation(baseDirectory);

      for (int ii = 0; ii < filenames.length; ++ii) {

        final String filename = ((fileDirectory != null) && (fileDirectory.length() > 0)) ? fileDirectory + Const.FILE_SEPARATOR + filenames[ii] : filenames[ii];
        if (log.isBasic())
          log.logBasic("Import objects from XML file [" + filename + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.WhichFile.Log", filename));

        // To where?
        wLabel.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.WhichDir.Label"));
        makeDirectory = true; // don't prompt, just create folders.
        askDirectory = false;
        

        // Read it using SAX...
        //
        try {
          RepositoryExportSaxParser parser = new RepositoryExportSaxParser(filename);          
          parser.parse(this);
        } catch(Exception e) {
          new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorGeneral.Title"), BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorGeneral.Message"), e);
        }        
      }
      addLog(BaseMessages.getString(PKG, "RepositoryImportDialog.ImportFinished.Log"));
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorGeneral.Title"), BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorGeneral.Message"), e);
    } finally {
      RepositoryImportLocation.setRepositoryImportLocation(null); // set to null
                                                                  // when done!
    }
  }

  private int transformationNumber = 1;
  public boolean transformationElementRead(String xml) {
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      Node transformationNode = XMLHandler.getSubNode(doc, RepositoryExportSaxParser.STRING_TRANSFORMATION);
      if (!importTransformation(transformationNode)) {
        return false;
      }
      transformationNumber++;
    } catch (Exception e) {
      // Some unexpected error occurred during transformation import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Title"), BaseMessages.getString(PKG, "RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Message"), e);

      MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
      mb.setMessage(BaseMessages.getString(PKG, "RepositoryImportDialog.DoYouWantToContinue.Message"));
      mb.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.DoYouWantToContinue.Title"));
      int answer = mb.open();
      if (answer == SWT.NO)
        return false;
    }
    return true;
  }

  private int jobNumber = 1;
  public boolean jobElementRead(String xml) {
    try {
      Document doc = XMLHandler.loadXMLString(xml);
      Node jobNode = XMLHandler.getSubNode(doc, RepositoryExportSaxParser.STRING_JOB);
      if (!importJob(jobNode)) {
        return false;
      }
      jobNumber++;
    } catch (Exception e) {
      // Some unexpected error occurred during job import
      // This is usually a problem with a missing plugin or something
      // like that...
      //
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryImportDialog.UnexpectedErrorDuringJobImport.Title"), BaseMessages.getString(PKG, "RepositoryImportDialog.UnexpectedErrorDuringJobImport.Message"), e);

      MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
      mb.setMessage(BaseMessages.getString(PKG, "RepositoryImportDialog.DoYouWantToContinue.Message"));
      mb.setText(BaseMessages.getString(PKG, "RepositoryImportDialog.DoYouWantToContinue.Title"));
      int answer = mb.open();
      if (answer == SWT.NO)
        return false;
    }
    return true;
  }

  public void fatalXmlErrorEncountered(SAXParseException e) {
    new ErrorDialog(shell, 
        BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorInvalidXML.Message"),
        BaseMessages.getString(PKG, "RepositoryImportDialog.ErrorInvalidXML.Title"),
        e
       );
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
}
