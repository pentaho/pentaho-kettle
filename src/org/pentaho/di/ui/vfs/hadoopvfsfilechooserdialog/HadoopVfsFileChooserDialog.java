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
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.eclipse.swt.SWT;
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
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.vfs.ui.IVfsFileChooser;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class HadoopVfsFileChooserDialog 
extends VfsFileChooserDialog implements IVfsFileChooser {
   
    //  for message resolution
    private static Class PKG = HadoopVfsFileChooserDialog.class;
    
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
    
    //  File objects to keep track of when the user selects the radio buttons
    FileObject localRootFile = null;
    FileObject localInitialFile = null;
    FileObject hadoopRootFile = null;
    FileObject hadoopInitialFile = null;

    public HadoopVfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
        super(rootFile, initialFile);
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

	    //  based on the initial file's protocol...
	    if (initialFile.getName().getFriendlyURI().startsWith("hdfs:")) {
	        setHadoopPanelEnabled(true);
	    } else {
	        setHadoopPanelEnabled(false);
	    }

	    try {
	        vfsBrowser.selectTreeItemByFileObject(initialFile != null ? initialFile : rootFile, true);
	        vfsBrowser.setSelectedFileObject(initialFile);
	        openFileCombo.setText(initialFile != null ? initialFile.getName().getFriendlyURI() : rootFile.getName().getFriendlyURI());
	        updateParentFileCombo(initialFile != null ? initialFile : rootFile);
	    } catch (FileSystemException e) {
	        MessageBox box = new MessageBox(applicationShell);
	        box.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.error")); //$NON-NLS-1$
	        box.setMessage(e.getMessage());
	        box.open();
	    }
	    
	    // set the size and show the dialog
	    int height = 400;
	    int width = 600;
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
	    
	    //  Select the hadoop radio button
	    //  based on the file system of the initialFile
	    if (rootFile != null) {
	        if (rootFile.getName().getFriendlyURI().startsWith("file:")) {
	            wLocalFSRadioButton.setSelection(true);
	        }
	        else {
	            wHadoopFSRadioButton.setSelection(true);
	        }
	    }
	    
	}
	
	private void createConnectionPanel(Shell dialog) {
	        
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

        //wUrl.setSize(200, 20);
        
	    fdUrl=new GridData();
	    fdUrl.widthHint = 150;
	    wUrl.setLayoutData(fdUrl);
	        
	    //  UserID label and field
	    wlUserID=new Label(textFieldPanel, SWT.RIGHT); 
	    wlUserID.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.UserID.Label")); //$NON-NLS-1$
	    fdlUserID=new GridData();
	    fdlUserID.widthHint = 75;
	    wlUserID.setLayoutData(fdlUserID);
	           
	    wUserID=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    //wlUserID.setSize(200, 20);
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
	    //wPort.setSize(200, 20);
	    fdPort=new GridData();
	    fdPort.widthHint = 150;
        wPort.setLayoutData(fdPort);
	    
	    //  password label and field
	    wlPassword=new Label(textFieldPanel, SWT.RIGHT);
	    wlPassword.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Password.Label")); //$NON-NLS-1$
	    fdlPassword=new GridData();
	    fdlPassword.widthHint = 75;
	    wlPassword.setLayoutData(fdlPassword);
	               
	    wPassword=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    //wPassword.setSize(200, 20);
	    wPassword.setEchoChar('*');
	    fdPassword=new GridData();
	    fdPassword.widthHint = 150;
        wPassword.setLayoutData(fdPassword);
	    
	    //  Connection button
	    wConnectionButton = new Button(textFieldPanel, SWT.RIGHT);
	    wConnectionButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionButton.Label"));
	    wConnectionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
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
	    
	    setLocalFileObjects(rootFile, initialFile);
	    
	    //  if our hadoop files are null
	    if (hadoopRootFile==null || hadoopInitialFile == null) {
	        
	        //  we do this to avoid an NPR when the VfsBrowser is null 
	        setDefaultLocalFileObjectsAsWorking();
	    }
	    else {
	        setWorkingFileObjects(hadoopRootFile, hadoopInitialFile);
	    }
        setHadoopPanelEnabled(true);
	            
        if (!Const.isEmpty(wUrl.getText()) && !Const.isEmpty(wPort.getText())) {
            urlString = buildHadoopFileSystemUrlString();
            openFileCombo.setText(urlString);
            resolveVfsBrowser();
        }
        else {
            openFileCombo.setText("");
            vfsBrowser.resetVfsRoot(null);
        }
        
        return urlString;
	}
	
	private void localSelected() 
	    throws KettleException {
	    setHadoopFileObjects(rootFile, initialFile);
	    if (localRootFile==null || localRootFile == null) {
	        setDefaultLocalFileObjectsAsWorking();
	    }
	    else {
       	    setWorkingFileObjects(localRootFile, localInitialFile);
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
	        wConnectionButton.setEnabled(true);
	    }
	    else {
	        wUrl.setEnabled(false);
            wPort.setEnabled(false);
            wUserID.setEnabled(false);
            wPassword.setEnabled(false);
            wConnectionButton.setEnabled(false);         
	    }
	}
	
	private void setLocalFileObjects(FileObject rootFile, FileObject initialFile) {
	    localRootFile = rootFile;
	    localInitialFile = initialFile;
	}
	
	private void setHadoopFileObjects(FileObject rootFile, FileObject initialFile) {
	    hadoopRootFile = rootFile;
	    hadoopInitialFile = initialFile;
	}
		
	private void setWorkingFileObjects(FileObject rootFile, FileObject initialFile) {
	    this.rootFile = rootFile;
	    this.initialFile = initialFile;
	}
	
	private void setDefaultLocalFileObjectsAsWorking() 
	    throws KettleException {
	    
	    try {
	        //initialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
	        initialFile = KettleVFS.getFileObject("file:///C:/");
	        rootFile = initialFile.getFileSystem().getRoot();
	    }
	    catch (FileSystemException fse) {
	        throw new KettleException(fse);
	    }
	}
}
