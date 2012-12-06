/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class EnterSearchDialog
{
	private static Class<?> PKG = DatabaseDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static final PropsUI props = PropsUI.getInstance();
    private Shell parentShell;
    private Shell shell;
    
    private boolean retval;
    private Display display;
    
    private boolean searchingSteps;
    private boolean searchingDatabases;
    private boolean searchingNotes;
    private String  filterString;
    
    private Label   wlStep;
    private Button  wStep;

    private Label   wlDB;
    private Button  wDB;
    private Label   wlNote;
    private Button  wNote;

    private Label   wlFilter;
    private Text    wFilter;

    public EnterSearchDialog(Shell parentShell)
    {
        this.parentShell = parentShell;
        this.display = parentShell.getDisplay();
        
        retval=true;
        
        searchingSteps     = true;
        searchingDatabases = true;
        searchingNotes     = true;
    }

    public boolean open()
    {
        shell = new Shell(parentShell,  SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());
        shell.setText(BaseMessages.getString(PKG, "EnterSearchDialog.Shell.Title"));
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
        // Search Steps?...
        wlStep=new Label(shell, SWT.RIGHT);
        wlStep.setText(BaseMessages.getString(PKG, "EnterSearchDialog.Step.Label"));
        props.setLook(wlStep);
        FormData fdlStep=new FormData();
        fdlStep.left = new FormAttachment(0, 0);
        fdlStep.top  = new FormAttachment(0, 0);
        fdlStep.right= new FormAttachment(middle, -margin);
        wlStep.setLayoutData(fdlStep);
        
        wStep=new Button(shell, SWT.CHECK );
        props.setLook(wStep);
        wStep.setToolTipText(BaseMessages.getString(PKG, "EnterSearchDialog.Step.Tooltip"));
        FormData fdStep=new FormData();
        fdStep.left = new FormAttachment(middle, 0);
        fdStep.top  = new FormAttachment(0, 0);
        fdStep.right= new FormAttachment(100, 0);
        wStep.setLayoutData(fdStep);

        // Search databases...
        wlDB=new Label(shell, SWT.RIGHT);
        wlDB.setText(BaseMessages.getString(PKG, "EnterSearchDialog.DB.Label"));
        props.setLook(wlDB);
        FormData fdlDB=new FormData();
        fdlDB.left = new FormAttachment(0, 0);
        fdlDB.top  = new FormAttachment(wStep, margin);
        fdlDB.right= new FormAttachment(middle, -margin);
        wlDB.setLayoutData(fdlDB);
        wDB=new Button(shell, SWT.CHECK );
        props.setLook(wDB);
        wDB.setToolTipText(BaseMessages.getString(PKG, "EnterSearchDialog.DB.Tooltip"));
        FormData fdDB=new FormData();
        fdDB.left = new FormAttachment(middle, 0);
        fdDB.top  = new FormAttachment(wStep, margin);
        fdDB.right= new FormAttachment(100, 0);
        wDB.setLayoutData(fdDB);

        // Search notes...
        wlNote=new Label(shell, SWT.RIGHT);
        wlNote.setText(BaseMessages.getString(PKG, "EnterSearchDialog.Note.Label"));
        props.setLook(wlNote);
        FormData fdlNote=new FormData();
        fdlNote.left = new FormAttachment(0, 0);
        fdlNote.top  = new FormAttachment(wDB, margin);
        fdlNote.right= new FormAttachment(middle, -margin);
        wlNote.setLayoutData(fdlNote);
        wNote=new Button(shell, SWT.CHECK );
        props.setLook(wNote);
        wNote.setToolTipText(BaseMessages.getString(PKG, "EnterSearchDialog.Note.Tooltip"));
        FormData fdNote=new FormData();
        fdNote.left = new FormAttachment(middle, 0);
        fdNote.top  = new FormAttachment(wDB, margin);
        fdNote.right= new FormAttachment(100, 0);
        wNote.setLayoutData(fdNote);

        // Filter line
        wlFilter=new Label(shell, SWT.RIGHT);
        wlFilter.setText(BaseMessages.getString(PKG, "EnterSearchDialog.FilterSelection.Label")); //Select filter 
        props.setLook(wlFilter);
        FormData fdlFilter=new FormData();
        fdlFilter.left = new FormAttachment(0, 0);
        fdlFilter.right= new FormAttachment(middle, -margin);
        fdlFilter.top  = new FormAttachment(wNote, 3*margin);
        wlFilter.setLayoutData(fdlFilter);
        wFilter=new Text(shell, SWT.SINGLE | SWT.BORDER);
        props.setLook(wFilter);
        FormData fdFilter=new FormData();
        fdFilter.left = new FormAttachment(middle, 0);
        fdFilter.top  = new FormAttachment(wNote, 3*margin);
        fdFilter.right= new FormAttachment(100, 0);
        wFilter.setLayoutData(fdFilter);

        Button wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(); } } );
        
        Button wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cancel(); } } );
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, Const.MARGIN, wFilter);
        
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
        SelectionAdapter lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wFilter.addSelectionListener( lsDef );
        
        getData();
        
		BaseStepDialog.setSize(shell);

        shell.open();
        
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }

        return retval;
    }
    
    private void getData()
    {
        wStep.setSelection(searchingSteps);
        wDB.setSelection(searchingDatabases);
        wNote.setSelection(searchingNotes);
        wFilter.setText(Const.NVL(filterString, ""));

        wFilter.setFocus();
    }
    
    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);

        shell.dispose();
    }

    public void cancel()
    {
        retval=false;
        dispose();
    }

    public void ok()
    {
        retval=true;
        searchingSteps = wStep.getSelection();
        searchingDatabases = wDB.getSelection();
        searchingNotes = wNote.getSelection();
        filterString = wFilter.getText();
        
        dispose();
    }
    
    public boolean isSearchingSteps()
    {
        return searchingSteps;
    }

    public boolean isSearchingDatabases()
    {
        return searchingDatabases;
    }

    public boolean isSearchingNotes()
    {
        return searchingNotes;
    }

    /**
     * @return Returns the filterString.
     */
    public String getFilterString()
    {
        return filterString;
    }

    /**
     * @param filterString The filterString to set.
     */
    public void setFilterString(String filterString)
    {
        this.filterString = filterString;
    }

}
