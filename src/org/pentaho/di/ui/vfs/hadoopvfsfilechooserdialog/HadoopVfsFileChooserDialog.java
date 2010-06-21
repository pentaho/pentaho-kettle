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
 * @author Michael D'Amour
 */

package org.pentaho.di.ui.vfs.hadoopvfsfilechooserdialog;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;

import org.pentaho.vfs.ui.IVfsFileChooser;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class HadoopVfsFileChooserDialog 
extends VfsFileChooserDialog implements IVfsFileChooser {
   
    private static Class PKG = HadoopVfsFileChooserDialog.class;
    
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
    
    //  Username label and field
    private Label        wlUsername;
    private Text         wUsername;
    private GridData     fdlUsername, fdUsername;
    
    //  Username label and field
    private Label        wlPassword;
    private Text         wPassword;
    private GridData     fdlPassword, fdPassword;

    //  Connection button
    private Button       wConnectionButton;
    
    public HadoopVfsFileChooserDialog(FileObject rootFile, FileObject initialFile) {
		
        //  
        super(rootFile, initialFile);
	}
	
	@Override
	public FileObject open(Shell applicationShell, String fileName, String[] fileFilters, String[] fileFilterNames, int fileDialogMode) {
	    System.out.println("HadoopVfsFileChooserDialog.open() has been invoked.");
	    this.fileDialogMode = fileDialogMode;
	    this.fileFilters = fileFilters;
	    this.fileFilterNames = fileFilterNames;
	    dialog = new Shell(applicationShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	    if (fileDialogMode != VFS_DIALOG_SAVEAS) {
	      dialog.setText(BaseMessages.getString("VfsFileChooserDialog.openFile")); //$NON-NLS-1$
	    } else {
	      dialog.setText(BaseMessages.getString("VfsFileChooserDialog.saveAs")); //$NON-NLS-1$
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

	    // set the initial file selection
	    try {
	      vfsBrowser.selectTreeItemByFileObject(initialFile != null ? initialFile : rootFile, true);
	      // vfsBrowser.setSelectedFileObject(initialFile);
	      openFileCombo.setText(initialFile != null ? initialFile.getName().getFriendlyURI() : rootFile.getName().getFriendlyURI());
	      updateParentFileCombo(initialFile != null ? initialFile : rootFile);
	    } catch (FileSystemException e) {
	      MessageBox box = new MessageBox(applicationShell);
	      box.setText(BaseMessages.getString("VfsFileChooserDialog.error")); //$NON-NLS-1$
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
	        messageDialog.setText(BaseMessages.getString("VfsFileChooserDialog.warning")); //$NON-NLS-1$
	        messageDialog.setMessage(BaseMessages.getString("VfsFileChooserDialog.noWriteSupport")); //$NON-NLS-1$
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
	 * Creates the File System Secltion and Connection panel.  These make
	 * up the Haddop panel whcih this class provides in addition the base
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
	            System.out.println("Hadoop file system has been selected.");
	        }
	    });

	    //  Local radio button
	    wLocalFSRadioButton = new Button(fileSystemPanel, SWT.RADIO);
	    wLocalFSRadioButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.FileSystemChoice.Local.Label"));
	    wLocalFSRadioButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	            System.out.println("Local file system has been selected.");
	        }
	    });
	    
	    //  Select the hadoop radio button
	    wHadoopFSRadioButton.setSelection(true);
	    
	}
	
	private void createConnectionPanel(Shell dialog) {
	        
        //  The Connection group
        Group connectionGroup = new Group(dialog, SWT.SHADOW_ETCHED_IN);
        connectionGroup.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionGroup.Label")); //$NON-NLS-1$;
        GridLayout connectionGroupLayout = new GridLayout();
        connectionGroupLayout.marginWidth = 3;
        connectionGroupLayout.marginHeight = 3;
        connectionGroupLayout.verticalSpacing = 3;
        connectionGroupLayout.horizontalSpacing = 3;
        connectionGroup.setLayout(connectionGroupLayout);
        
        //  The composite we need in the group
        Composite textFieldPanel = new Composite(connectionGroup, SWT.NONE);
	    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
	    textFieldPanel.setLayoutData(gridData);
	    textFieldPanel.setLayout(new GridLayout(4, false));
	       	    
	    //  URL label and text field
	    wlUrl=new Label(textFieldPanel, SWT.RIGHT);
	    wlUrl.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.URL.Label")); //$NON-NLS-1$
	    fdlUrl=new GridData();
	    wlUrl.setLayoutData(fdlUrl);
	        
	    wUrl=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);

	    //  Compute size of text fields - do this here as we need a Text
	    //  and just created one.
        int columns = 30;
        GC gc = new GC (wUrl);
        FontMetrics fm = gc.getFontMetrics ();
        int width = columns * fm.getAverageCharWidth ();
        int height = fm.getHeight ();
        gc.dispose ();
        wUrl.setSize (wUrl.computeSize (width, height));  

	    fdUrl=new GridData();
	    wUrl.setLayoutData(fdUrl);
	        
	    //  Username label and field
	    wlUsername=new Label(textFieldPanel, SWT.RIGHT); 
	    wlUsername.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Username.Label")); //$NON-NLS-1$
	    fdlUsername=new GridData();
	    wlUsername.setLayoutData(fdlUsername);
	           
	    wUsername=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    wlUsername.setSize (wUsername.computeSize (width, height));
	    fdUsername=new GridData();
	    wUsername.setLayoutData(fdUsername);
	    
	    // Port label and text field
	    wlPort=new Label(textFieldPanel, SWT.RIGHT);
	    wlPort.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Port.Label")); //$NON-NLS-1$
	    fdlPort=new GridData();
	    wlPort.setLayoutData(fdlPort);
	            
	    wPort=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    wPort.setSize (wPort.computeSize (width, height));
	    fdPort=new GridData();
	    wPort.setLayoutData(fdPort);
	    
	    //  password label and field
	    wlPassword=new Label(textFieldPanel, SWT.RIGHT);
	    wlPassword.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.Password.Label")); //$NON-NLS-1$
	    fdlPassword=new GridData();
	    wlPassword.setLayoutData(fdlPassword);
	               
	    wPassword=new Text(textFieldPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    wPassword.setSize (wPassword.computeSize (width, height));
	    wPassword.setEchoChar('*');
	    fdPassword=new GridData();
	    wPassword.setLayoutData(fdPassword);
	    
	    //  Connection button
	    wConnectionButton = new Button(textFieldPanel, SWT.RIGHT);
	    wConnectionButton.setText(BaseMessages.getString(PKG, "HadoopVfsFileChooserDialog.ConnectionButton.Label"));
	    wConnectionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                System.out.println("The connection button has been pressed.");
            }
        });
	
	}
}
