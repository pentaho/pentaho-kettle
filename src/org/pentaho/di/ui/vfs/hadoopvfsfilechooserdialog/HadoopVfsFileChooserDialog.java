/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 */

package org.pentaho.di.ui.vfs.hadoopvfsfilechooserdialog;

import java.net.Socket;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.GenericFileName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.hdfs.vfs.HDFSFileObject;
import org.pentaho.vfs.ui.IVfsFileChooser;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class HadoopVfsFileChooserDialog 
extends VfsFileChooserDialog implements IVfsFileChooser {
   
    //  for message resolution
    private static Class<?> PKG = HadoopVfsFileChooserDialog.class;
    
    //  for logging    
    private LogChannel log = new LogChannel(this);
    
    private Label        wlFileSystemChoice;
    private Button       wHadoopFSRadioButton;
    private Button       wLocalFSRadioButton;
    
    //  URL label and field
    private Label        wlUrl;
    private Text         wUrl;
    private GridData     fdlUrl, fdUrl;
    
    //  Port label and field
    private Label        wlPort;
    private Text         wPort;
    private GridData     fdlPort, fdPort;
    
    //  UserID label and field
    private Label        wlUserID;
    private Text         wUserID;
    private GridData     fdlUserID, fdUserID;
    
    //  Password label and field
    private Label        wlPassword;
    private Text         wPassword;
    private GridData     fdlPassword, fdPassword;
    
    //  Place holder - for creating a blank widget in a grid layout
    private Label        wPlaceHolderLabel;
    private GridData     fdlPlaceHolderLabel;

    //  Connection button
    private Button       wConnectionButton;
    private GridData     fdConnectionButton;
    
    //  Default root file - used to avoid NPE when rootFile was not provided
    //  and the browser is resolved
    FileObject defaultInitialFile = null;
    
    //  File objects to keep track of when the user selects the radio buttons
    FileObject localRootFile = null;
    String     localOpenFromFolder = null;
    FileObject hadoopRootFile = null;
    String     hadoopOpenFromFolder = null;

    public HadoopVfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
        super(rootFile, initialFile);
	}
	
    public FileObject open(Shell applicationShell, FileObject defaultInitialFile, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode) {
        this.defaultInitialFile = defaultInitialFile;
        return open(applicationShell, fileName, fileFilters, fileFilterNames, fileDialogMode);
    }
    
	@Override
	public FileObject open(Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode) {
	    this.fileDialogMode = fileDialogMode;
	    this.fileFilters = fileFilters;
	    this.fileFilterNames = fileFilterNames;
	    dialog = new Shell(applicationShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	    if (fileDialogMode != VFS_DIALOG_SAVEAS) {
	      dialog.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.openFile")); //$NON-NLS-1$
	    } else {
	      dialog.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.saveAs")); //$NON-NLS-1$
	    }

	    dialog.setLayout(new GridLayout());
        
	    //  Create the Hadoop panel 
        createHadoopPanel(dialog);
	    
	    // create our file chooser tool bar, contains parent folder combo and various controls
	    createToolbarPanel(dialog);
	    
	    // create our vfs browser component
	    createVfsBrowser(dialog);
	    if (fileDialogMode == VFS_DIALOG_SAVEAS) {
	      createFileNamePanel(dialog, fileName);
	    } else {
	      // create file filter panel
	      createFileFilterPanel(dialog);
	    }
	    // create our ok/cancel buttons
	    createButtonPanel(dialog);

        //  if we are not passed an initial file 
	    //  or we are passed a 
        if (initialFile != null) {
            
            //  set the radio buttons based on HDFSFileObject class
            if (initialFile instanceof HDFSFileObject) {
                setHadoopPanelEnabled(true);
            } else {
                setHadoopPanelEnabled(false);
            }

            //  populate the browsers file tree
    	    try {
    	        vfsBrowser.selectTreeItemByFileObject(initialFile != null ? initialFile : rootFile, true);
    	        vfsBrowser.setSelectedFileObject(initialFile);
    	        openFileCombo.setText(initialFile != null ? initialFile.getName().getFriendlyURI() : rootFile.getName().getFriendlyURI());
    	        updateParentFileCombo(initialFile != null ? initialFile : rootFile);
    	    } catch (FileSystemException fse) {
    	        showMessageAndLog(dialog, "HadoopVfsFileChooserDialog.error", "HadoopVfsFileChooserDialog.FileSystem.error", fse.getMessage());
    	    }
        }
        else { //  we have a default situation - set the hadoop radio button on.
            setHadoopPanelEnabled(true);
        }
	    
	    // set the size and show the dialog
	    int height = 800;
	    int width = 700;
	    dialog.setSize(width, height);
	    Rectangle bounds = dialog.getDisplay().getPrimaryMonitor().getClientArea();
	    int x = (bounds.width - width) / 2;
	    int y = (bounds.height - height) / 2;
	    dialog.setLocation(x, y);
	    dialog.open();

	    if (rootFile != null && fileDialogMode == VFS_DIALOG_SAVEAS) {
	      if (!rootFile.getFileSystem().hasCapability(Capability.WRITE_CONTENT)) {
	        MessageBox messageDialog = new MessageBox(applicationShell, SWT.OK);
	        messageDialog.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.warning")); //$NON-NLS-1$
	        messageDialog.setMessage(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.noWriteSupport")); //$NON-NLS-1$
	        messageDialog.open();
	      }
	    }

	    vfsBrowser.fileSystemTree.forceFocus();
	    while (!dialog.isDisposed()) {
	      if (!dialog.getDisplay().readAndDispatch())
	        dialog.getDisplay().sleep();
	    }

	    // we just woke up, we are probably disposed already..
	    if (!dialog.isDisposed()) {
	      dialog.dispose();
	    }
	    if (okPressed) {
	      FileObject returnFile = vfsBrowser.getSelectedFileObject();
	      if (returnFile != null && fileDialogMode == VFS_DIALOG_SAVEAS) {
	        try {
	          if (returnFile.getType().equals(FileType.FILE)) {
	            returnFile = returnFile.getParent();
	          }
	          returnFile = returnFile.resolveFile(enteredFileName);
	        } catch (FileSystemException e) {
	          e.printStackTrace();
	        }
	      }
	      return returnFile;
	    } else {
	      return null;
	    }
	  }
	
	/**
	 * Creates the File System Selection and Connection panel.  These make
	 * up the Hadoop panel which this class provides in addition the base
	 * classes panels.
	 * 
	 * @param dialog
	 */
	private void createHadoopPanel(Shell dialog) {
	    createFileSystemSelectorPanel(dialog);
	    createConnectionPanel(dialog);
	    initializeConnectionPanel(dialog);
	}
	
	private void createFileSystemSelectorPanel(Shell dialog) {
	    
	    //  The file system selection label
	    wlFileSystemChoice = wlUrl=new Label(dialog, SWT.RIGHT);
	    wlFileSystemChoice.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileSystemChoice.Label")); //$NON-NLS-1$

	    //  Our layout
	    Composite fileSystemPanel = new Composite(dialog, SWT.NONE);
	    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
	    fileSystemPanel.setLayoutData(gridData);
	    fileSystemPanel.setLayout(new GridLayout(2, false));
    	   
	    //  Hadoop radio button
	    wHadoopFSRadioButton = new Button(fileSystemPanel, SWT.RADIO);
	    wHadoopFSRadioButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileSystemChoice.Hadoop.Label"));
	    wHadoopFSRadioButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            if (wHadoopFSRadioButton.getSelection()) {
	                try {
	                    hadoopSelected();
	                }
	                catch (KettleException ke) {
	                    log.logError(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileBrowser.KettleFileException"));
	                }
	            }
	        }
	    });

	    //  Local radio button
	    wLocalFSRadioButton = new Button(fileSystemPanel, SWT.RADIO);
	    wLocalFSRadioButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileSystemChoice.Local.Label"));
	    wLocalFSRadioButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            if (wLocalFSRadioButton.getSelection()) {
	                try {
	                    localSelected();
	                }
	                catch (KettleException ke) {
	                    log.logError(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileBrowser.KettleFileException"));
	                }
	            }
	        }
	    });
	    
	    //  based on rootFile class, set the radio buttons 
	    if (rootFile != null) {
	        if (rootFile instanceof HDFSFileObject) {
	            wHadoopFSRadioButton.setSelection(true);
	        }
	        else {
	            wLocalFSRadioButton.setSelection(true);
	        }
	    }
	}
	
	private void createConnectionPanel(final Shell dialog) {
	        
        //  The Connection group
        Group connectionGroup = new Group(dialog, SWT.SHADOW_ETCHED_IN);
        connectionGroup.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionGroup.Label")); //$NON-NLS-1$;
        GridLayout connectionGroupLayout = new GridLayout();
        connectionGroupLayout.marginWidth = 5;
        connectionGroupLayout.marginHeight = 5;
        connectionGroupLayout.verticalSpacing = 5;
        connectionGroupLayout.horizontalSpacing = 5;
        GridData gData = new GridData(SWT.FILL, SWT.FILL, true, false);
        connectionGroup.setLayoutData(gData);
        connectionGroup.setLayout(connectionGroupLayout);
        
        //  The composite we need in the group
        Composite textFieldPanel = new Composite(connectionGroup, SWT.NONE);
	    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
	    textFieldPanel.setLayoutData(gridData);
	    textFieldPanel.setLayout(new GridLayout(5, false));
	       	    
	    //  URL label and text field
	    wlUrl=new Label(textFieldPanel, SWT.RIGHT);
	    wlUrl.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.URL.Label")); //$NON-NLS-1$
	    fdlUrl=new GridData();
	    fdlUrl.widthHint = 75;
	    wlUrl.setLayoutData(fdlUrl);
	    wUrl=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);        
	    fdUrl=new GridData();
	    fdUrl.widthHint = 150;
	    wUrl.setLayoutData(fdUrl);
	    wUrl.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleConnectionButton();
            }
        });

	        
	    //  UserID label and field
	    wlUserID=new Label(textFieldPanel, SWT.RIGHT); 
	    wlUserID.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.UserID.Label")); //$NON-NLS-1$
	    fdlUserID=new GridData();
	    fdlUserID.widthHint = 75;
	    wlUserID.setLayoutData(fdlUserID);
	           
	    wUserID=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    fdUserID=new GridData();
	    fdUserID.widthHint = 150;
        wUserID.setLayoutData(fdUserID);
	    
	    //  Place holder
	    wPlaceHolderLabel = new Label(textFieldPanel, SWT.RIGHT);
	    wPlaceHolderLabel.setText("");
	    fdlPlaceHolderLabel=new GridData();
	    fdlPlaceHolderLabel.widthHint = 75;
	    wlUserID.setLayoutData(fdlPlaceHolderLabel);
	    
	    // Port label and text field
	    wlPort=new Label(textFieldPanel, SWT.RIGHT);
	    wlPort.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Port.Label")); //$NON-NLS-1$
	    fdlPort=new GridData();
	    fdlPort.widthHint = 75;
        wlPort.setLayoutData(fdlPort);
	            
	    wPort=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    fdPort=new GridData();
	    fdPort.widthHint = 150;
        wPort.setLayoutData(fdPort);
        wPort.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleConnectionButton();
            }
        });

	    
	    //  password label and field
	    wlPassword=new Label(textFieldPanel, SWT.RIGHT);
	    wlPassword.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Password.Label")); //$NON-NLS-1$
	    fdlPassword=new GridData();
	    fdlPassword.widthHint = 75;
	    wlPassword.setLayoutData(fdlPassword);
	               
	    wPassword=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    wPassword.setEchoChar('*');
	    fdPassword=new GridData();
	    fdPassword.widthHint = 150;
        wPassword.setLayoutData(fdPassword);
	    
	    //  Connection button
	    wConnectionButton = new Button(textFieldPanel, SWT.CENTER);
	    fdConnectionButton = new GridData();
	    fdConnectionButton.widthHint = 75;
	    wConnectionButton.setLayoutData(fdConnectionButton);
	    
	    wConnectionButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionButton.Label"));
	    wConnectionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              try {
                Socket testHdfsSocket = new Socket(wUrl.getText(), Integer.parseInt(wPort.getText()));
                testHdfsSocket.getOutputStream();
                testHdfsSocket.close();
              } catch (Throwable t) {
                showMessageAndLog(dialog, "HadoopVfsFileChooserDialog.error", "HadoopVfsFileChooserDialog.Connection.error", t.getMessage());
                return;
              }
              openFileCombo.setText(buildHadoopFileSystemUrlString());
              resolveVfsBrowser();
            }
        });
	    
	    //  set the tab order
	    textFieldPanel.setTabList(
	            new Control [] {wUrl, wPort, wUserID, wPassword, wConnectionButton});
	}
		
	/**
	 * Build a URL given Url and Port provided by the user.
	 * @return
	 */
	public String buildHadoopFileSystemUrlString() {
	    String urlString = "hdfs://"+wUrl.getText()+":"+wPort.getText();
	    return urlString;
	}
	
	private String hadoopSelected() 
	    throws KettleException {
	    String urlString = null;
	    
	    //  save local files system information they were browsing
	    setLocalInformation(rootFile, openFileCombo.getText());

	    // set the root as the hadoopRootFile
	    if (hadoopRootFile==null) { 
	        setDefaultFileObjectAsWorking();
	    }
	    else {
	        setRootFile(hadoopRootFile);
	    }
	    	    
        //  Here we set the open from folder combo box
        
        if (Const.isEmpty(hadoopOpenFromFolder)) {
            
            if (!Const.isEmpty(wUrl.getText()) && !Const.isEmpty(wPort.getText())) {
                //  no value was saved but we have some connection information entered 
                openFileCombo.setText(buildHadoopFileSystemUrlString());
                resolveVfsBrowser();
            }
            else {
                //  we don't have a value or connection information
                openFileCombo.setText("");
                vfsBrowser.resetVfsRoot(null);
            }
        }
        else {
            //  a value was saved from some browsing 
            openFileCombo.setText(hadoopOpenFromFolder);
            resolveVfsBrowser();
        } 
        
        setHadoopPanelEnabled(true);
        
        return urlString;
	}
	
	private void localSelected() 
	    throws KettleException {
	    setHadoopInformation(rootFile, openFileCombo.getText());
	    if (localRootFile==null) { 
	        setDefaultFileObjectAsWorking();
	        String localFileUrl = this.initialFile.getName().getFriendlyURI();
	        openFileCombo.setText(localFileUrl);
	    }
	    else {
	        setRootFile(localRootFile);
	        openFileCombo.setText(localOpenFromFolder);
	    }
       	resolveVfsBrowser();
	    setHadoopPanelEnabled(false);
	}
	
	private void setHadoopPanelEnabled(boolean enable) {
	    
	    if (enable) {
	        wUrl.setEnabled(true);
	        wPort.setEnabled(true);
	        wUserID.setEnabled(true);
	        wPassword.setEnabled(true);
	        this.handleConnectionButton();
	    }
	    else {
	        wUrl.setEnabled(false);
            wPort.setEnabled(false);
            wUserID.setEnabled(false);
            wPassword.setEnabled(false);    
	    }
	}

	private void initializeConnectionPanel(Shell dialog) {
	    
	    if (initialFile instanceof HDFSFileObject) {
	        
            setHadoopPanelEnabled(true);
            
            //  populate the server and port fields
            try {
                GenericFileName genericFileName = (GenericFileName)initialFile.getFileSystem().getRoot().getName();
                wUrl.setText(genericFileName.getHostName());
                wPort.setText(String.valueOf(genericFileName.getPort()));
            }
            catch (FileSystemException fse) {
                showMessageAndLog(dialog, "HadoopVfsFileChooserDialog.error", "HadoopVfsFileChooserDialog.FileSystem.error", fse.getMessage());
            }
        } else {
            
            setHadoopPanelEnabled(false);
        }
	    
	    handleConnectionButton();
	}
	
	private void setLocalInformation(FileObject localRootFile, String openFromFolder) {
	    this.localRootFile = localRootFile;
	    localOpenFromFolder = openFromFolder;
	}
	
	private void setHadoopInformation(FileObject hadoopRootFile, String openFromFolder) {
	    this.hadoopRootFile = hadoopRootFile;
	    this.hadoopOpenFromFolder = openFromFolder;
	}
		
	private void setRootFile(FileObject rootFile) {
	    this.rootFile = rootFile;
	}
	
	private void setDefaultFileObjectAsWorking() 
	    throws KettleException {
	    
	    try {
	        rootFile = defaultInitialFile.getFileSystem().getRoot();
	        initialFile = defaultInitialFile;
	    }
	    catch (FileSystemException fse) {
	        throw new KettleException(fse);
	    }
	}
	
	private void showMessageAndLog(Shell dialog, String title, String message, String messageToLog) {
    	  MessageBox box = new MessageBox(dialog);
        box.setText(BaseMessages.getString(PKG, title)); //$NON-NLS-1$
        box.setMessage(BaseMessages.getString(PKG, message));
        log.logError(messageToLog);
        box.open();
	}
	
	private void handleConnectionButton() {
	    if (!Const.isEmpty(wUrl.getText()) && !Const.isEmpty(wPort.getText()) ) {
	        wConnectionButton.setEnabled(true);
	    }
	    else {
	        wConnectionButton.setEnabled(false);
	    }
	}
}
