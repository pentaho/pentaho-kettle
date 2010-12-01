 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 
/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.accessoutput;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.accessoutput.AccessOutputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.healthmarketscience.jackcess.Database;


public class AccessOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = AccessOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlFilename;
    private Button       wbbFilename; // Browse: add file or directory
    private TextVar      wFilename;
    private FormData     fdlFilename, fdbFilename, fdFilename;

    private Label        wlCreateFile;
    private Button       wCreateFile;
    private FormData     fdlCreateFile, fdCreateFile;
    
    private Label        wlTablename;
    private TextVar      wTablename;
    private Button       wbbTablename;
    private FormData     fdlTablename, fdTablename, fdbTablename;

    /*
	private Label        wlTruncate;
	private Button       wTruncate;
	private FormData     fdlTruncate, fdTruncate;
    */
    
    private Label        wlCreateTable;
    private Button       wCreateTable;
    private FormData     fdlCreateTable, fdCreateTable;

    private Label        wlCommitSize;
    private Text         wCommitSize;
    private FormData     fdlCommitSize, fdCommitSize;
    
	private Label        wlAddToResult;
	private Button       wAddToResult;
	private FormData     fdlAddToResult, fdAddToResult;
	
	private Label        wlDoNotOpenNewFileInit;
	private Button       wDoNotOpenNewFileInit;
	private FormData     fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;

    private AccessOutputMeta input;
	
	public AccessOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(AccessOutputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
        SelectionAdapter lsSelMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		backupChanged = input.hasChanged();
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "AccessOutputDialog.DialogTitle"));
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, 0);
		fdlStepname.top  = new FormAttachment(0, 0);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, margin);
		fdStepname.top  = new FormAttachment(0, 0);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

        // Filename line
        wlFilename=new Label(shell, SWT.RIGHT);
        wlFilename.setText(BaseMessages.getString(PKG, "AccessOutputDialog.Filename.Label"));
        props.setLook(wlFilename);
        fdlFilename=new FormData();
        fdlFilename.left = new FormAttachment(0, 0);
        fdlFilename.top  = new FormAttachment(wStepname, margin);
        fdlFilename.right= new FormAttachment(middle, 0);
        wlFilename.setLayoutData(fdlFilename);

        wbbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
        fdbFilename=new FormData();
        fdbFilename.right= new FormAttachment(100, 0);
        fdbFilename.top  = new FormAttachment(wStepname, margin);
        wbbFilename.setLayoutData(fdbFilename);

        wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wFilename.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.Filename.Tooltip"));
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        fdFilename=new FormData();
        fdFilename.left = new FormAttachment(middle, margin);
        fdFilename.right= new FormAttachment(wbbFilename, -margin);
        fdFilename.top  = new FormAttachment(wStepname, margin);
        wFilename.setLayoutData(fdFilename);
        
		// Open new File at Init
		wlDoNotOpenNewFileInit=new Label(shell, SWT.RIGHT);
		wlDoNotOpenNewFileInit.setText(BaseMessages.getString(PKG, "AccessOutputDialog.DoNotOpenNewFileInit.Label"));
 		props.setLook(wlDoNotOpenNewFileInit);
		fdlDoNotOpenNewFileInit=new FormData();
		fdlDoNotOpenNewFileInit.left = new FormAttachment(0, 0);
		fdlDoNotOpenNewFileInit.top  = new FormAttachment(wFilename, margin);
		fdlDoNotOpenNewFileInit.right= new FormAttachment(middle, -margin);
		wlDoNotOpenNewFileInit.setLayoutData(fdlDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit=new Button(shell, SWT.CHECK );
		wDoNotOpenNewFileInit.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.DoNotOpenNewFileInit.Tooltip"));
 		props.setLook(wDoNotOpenNewFileInit);
		fdDoNotOpenNewFileInit=new FormData();
		fdDoNotOpenNewFileInit.left = new FormAttachment(middle, margin);
		fdDoNotOpenNewFileInit.top  = new FormAttachment(wFilename, margin);
		fdDoNotOpenNewFileInit.right= new FormAttachment(100, 0);
		wDoNotOpenNewFileInit.setLayoutData(fdDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		

        // Create file?
        wlCreateFile=new Label(shell, SWT.RIGHT);
        wlCreateFile.setText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateFile.Label"));
        wlCreateFile.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateFile.Tooltip"));
        props.setLook(wlCreateFile);
        fdlCreateFile=new FormData();
        fdlCreateFile.left  = new FormAttachment(0, 0);
        fdlCreateFile.top   = new FormAttachment(wDoNotOpenNewFileInit, margin);
        fdlCreateFile.right = new FormAttachment(middle, 0);
        wlCreateFile.setLayoutData(fdlCreateFile);
        wCreateFile=new Button(shell, SWT.CHECK);
        wCreateFile.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateFile.Tooltip"));
        props.setLook(wCreateFile);
        fdCreateFile=new FormData();
        fdCreateFile.left  = new FormAttachment(middle, margin);
        fdCreateFile.top   = new FormAttachment(wDoNotOpenNewFileInit, margin);
        fdCreateFile.right = new FormAttachment(100, 0);
        wCreateFile.setLayoutData(fdCreateFile);
        wCreateFile.addSelectionListener(lsSelMod);

		// Table line...
        wbbTablename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbTablename);
        wbbTablename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        fdbTablename=new FormData();
        fdbTablename.right= new FormAttachment(100, 0);
        fdbTablename.top  = new FormAttachment(wCreateFile, margin);
        wbbTablename.setLayoutData(fdbTablename);

        wlTablename=new Label(shell, SWT.RIGHT);
        wlTablename.setText(BaseMessages.getString(PKG, "AccessOutputDialog.TargetTable.Label"));
        props.setLook(wlTablename);
        fdlTablename=new FormData();
        fdlTablename.left = new FormAttachment(0, 0);
        fdlTablename.top  = new FormAttachment(wCreateFile, margin);
        fdlTablename.right= new FormAttachment(middle, 0);
        wlTablename.setLayoutData(fdlTablename);

        wTablename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTablename.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.TargetTable.Tooltip"));
 		props.setLook(wTablename);
		fdTablename=new FormData();
        fdTablename.left = new FormAttachment(middle, margin);
        fdTablename.right= new FormAttachment(wbbTablename, -margin);
        fdTablename.top  = new FormAttachment(wCreateFile, margin);
		wTablename.setLayoutData(fdTablename);

		// Create table?
		wlCreateTable=new Label(shell, SWT.RIGHT);
		wlCreateTable.setText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateTable.Label"));
        wlCreateTable.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateTable.Tooltip"));
 		props.setLook(wlCreateTable);
		fdlCreateTable=new FormData();
		fdlCreateTable.left  = new FormAttachment(0, 0);
		fdlCreateTable.top   = new FormAttachment(wTablename, margin);
		fdlCreateTable.right = new FormAttachment(middle, 0);
		wlCreateTable.setLayoutData(fdlCreateTable);
		wCreateTable=new Button(shell, SWT.CHECK);
        wCreateTable.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.CreateTable.Tooltip"));
 		props.setLook(wCreateTable);
		fdCreateTable=new FormData();
		fdCreateTable.left  = new FormAttachment(middle, margin);
		fdCreateTable.top   = new FormAttachment(wTablename, margin);
		fdCreateTable.right = new FormAttachment(100, 0);
		wCreateTable.setLayoutData(fdCreateTable);
		wCreateTable.addSelectionListener(lsSelMod);
        
        // The commit size...
        wlCommitSize=new Label(shell, SWT.RIGHT);
        wlCommitSize.setText(BaseMessages.getString(PKG, "AccessOutputDialog.CommitSize.Label"));
        props.setLook(wlCommitSize);
        fdlCommitSize=new FormData();
        fdlCommitSize.left = new FormAttachment(0, 0);
        fdlCommitSize.top  = new FormAttachment(wCreateTable, margin);
        fdlCommitSize.right= new FormAttachment(middle, 0);
        wlCommitSize.setLayoutData(fdlCommitSize);

        wCommitSize=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wCommitSize.setToolTipText(BaseMessages.getString(PKG, "AccessOutputDialog.CommitSize.Tooltip"));
        props.setLook(wCommitSize);
        fdCommitSize=new FormData();
        fdCommitSize.left = new FormAttachment(middle, margin);
        fdCommitSize.right= new FormAttachment(100, 0);
        fdCommitSize.top  = new FormAttachment(wCreateTable, margin);
        wCommitSize.setLayoutData(fdCommitSize);
        

		// Add File to the result files name
		wlAddToResult=new Label(shell, SWT.RIGHT);
		wlAddToResult.setText(BaseMessages.getString(PKG, "AccessOutputMeta.AddFileToResult.Label"));
		props.setLook(wlAddToResult);
		fdlAddToResult=new FormData();
		fdlAddToResult.left  = new FormAttachment(0, 0);
		fdlAddToResult.top   = new FormAttachment(wCommitSize, 2*margin);
		fdlAddToResult.right = new FormAttachment(middle, -margin);
		wlAddToResult.setLayoutData(fdlAddToResult);
		wAddToResult=new Button(shell, SWT.CHECK);
		wAddToResult.setToolTipText(BaseMessages.getString(PKG, "AccessOutputMeta.AddFileToResult.Tooltip"));
 		props.setLook(wAddToResult);
		fdAddToResult=new FormData();
		fdAddToResult.left  = new FormAttachment(middle, margin);
		fdAddToResult.top   = new FormAttachment(wCommitSize, 2*margin);
		fdAddToResult.right = new FormAttachment(100, 0);
		wAddToResult.setLayoutData(fdAddToResult);
		SelectionAdapter lsSelR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wAddToResult.addSelectionListener(lsSelR);


		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wAddToResult);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wTablename.addSelectionListener( lsDef );
		
		wbbTablename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);
        
        // Listen to the Browse... button
        wbbFilename.addSelectionListener
        (
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*.mdb;*.MDB;*.accdb;*.ACCDB", "*"});

                    if (!Const.isEmpty(wFilename.getText()))
                    {
                        String fname = transMeta.environmentSubstitute(wFilename.getText());
                        dialog.setFileName( fname );
                    }
                    
                    dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "AccessOutputDialog.FileType.AccessFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles")});
                    
                    if (dialog.open()!=null)
                    {
                        String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
                        wFilename.setText(str);
                    }
                }
            }
        );

        
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(backupChanged);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        if (input.getFilename()  != null) wFilename.setText(input.getFilename());
		if (input.getTablename() != null) wTablename.setText(input.getTablename());
		
        wCreateFile.setSelection( input.isFileCreated() );
        wCreateTable.setSelection(input.isFileCreated() );
        if (input.getCommitSize()>0) wCommitSize.setText( Integer.toString( input.getCommitSize() ) );
        wAddToResult.setSelection(input.isAddToResultFiles());
        wDoNotOpenNewFileInit.setSelection(input.isDoNotOpenNewFileInit());
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void getInfo(AccessOutputMeta info)
	{
        info.setFilename( wFilename.getText() );
		info.setTablename( wTablename.getText() );
        info.setFileCreated( wCreateFile.getSelection() );
		info.setTableCreated( wCreateTable.getSelection() );
        info.setCommitSize( Const.toInt(wCommitSize.getText(), -1) );
        info.setAddToResultFiles( wAddToResult.getSelection() );
        input.setDoNotOpenNewFileInit(wDoNotOpenNewFileInit.getSelection() );
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		
		stepname = wStepname.getText(); // return value
		
		getInfo(input);
		
		dispose();
	}
	
	private void getTableName()
	{
        AccessOutputMeta meta = new AccessOutputMeta();
        getInfo(meta);
        
        Database database = null;
		// New class: SelectTableDialog
        try
        {
            String realFilename = transMeta.environmentSubstitute(meta.getFilename());
            FileObject fileObject = KettleVFS.getFileObject(realFilename, transMeta);
            File file = FileUtils.toFile(fileObject.getURL());
            
            if (!file.exists() || !file.isFile())
            {
                throw new KettleException(BaseMessages.getString(PKG, "AccessOutputMeta.Exception.FileDoesNotExist", realFilename));
            }

            database = Database.open(file);
            Set<String> set= database.getTableNames();
            String[] tablenames = set.toArray(new String[set.size()]);
            EnterSelectionDialog dialog = new EnterSelectionDialog(shell, tablenames, BaseMessages.getString(PKG, "AccessOutputDialog.Dialog.SelectATable.Title"), BaseMessages.getString(PKG, "AccessOutputDialog.Dialog.SelectATable.Message"));
            String tablename = dialog.open();
            if (tablename!=null)
            {
                wTablename.setText(tablename);
            }
        }
        catch(Throwable e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "AccessOutputDialog.UnableToGetListOfTables.Title"), BaseMessages.getString(PKG, "AccessOutputDialog.UnableToGetListOfTables.Message"), e);
        }
        finally
        {
            // Don't forget to close the bugger.
            try
            {
                if (database!=null) database.close();
            }
            catch(Exception e)
            {
                
            }
        }
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
