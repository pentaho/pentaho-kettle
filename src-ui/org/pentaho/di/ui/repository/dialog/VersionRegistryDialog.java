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

 

package org.pentaho.di.ui.repository.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectVersion;
import org.pentaho.di.repository.RepositoryVersionRegistry;
import org.pentaho.di.repository.SimpleObjectVersion;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Allows you to manage the version registry.
 * 
 * @author Matt
 */
public class VersionRegistryDialog extends Dialog
{
	private static Class<?> PKG = VersionRegistryDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private TableView    wVersions;
	
	private Button wClose, wUpdate, wDelete, wRefresh;

	private Shell         shell;
	private PropsUI       props;
	private String        title;
    
    private VariableSpace variables;

	private RepositoryVersionRegistry versionRegistry;

	private List<ObjectVersion>	versions;
	
    /**
     * 
     * @param parent
     * @param style
     * @param buf
     */
	public VersionRegistryDialog(Shell parent, RepositoryVersionRegistry versionRegistry)
	{
		super(parent, SWT.NONE);
		this.versionRegistry = versionRegistry;
		props=PropsUI.getInstance();
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		if (title==null) title = BaseMessages.getString(PKG, "VersionRegistryDialog.Title");

		shell.setLayout(formLayout);
		shell.setImage(GUIResource.getInstance().getImageTransGraph());
		shell.setText(title);
		
		int margin = Const.MARGIN;

        getVersions();
		
		int FieldsRows=versions.size();
		
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "VersionRegistryDialog.Column.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
			new ColumnInfo(BaseMessages.getString(PKG, "VersionRegistryDialog.Column.Description"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
			new ColumnInfo(BaseMessages.getString(PKG, "VersionRegistryDialog.Column.PlannedReleaseDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
			new ColumnInfo(BaseMessages.getString(PKG, "VersionRegistryDialog.Column.PreviousVersion"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
		};
		
		wVersions=new TableView(variables, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      null,
							  props
						      );

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(BaseMessages.getString("System.Button.OK"));
		wClose.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { close(); } }  );
		wUpdate=new Button(shell, SWT.PUSH);
		wUpdate.setText(BaseMessages.getString(PKG, "VersionRegistryDialog.Button.Update"));
		wUpdate.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { update(); } });
		wDelete=new Button(shell, SWT.PUSH);
		wDelete.setText(BaseMessages.getString(PKG, "VersionRegistryDialog.Button.Delete"));
		wDelete.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { delete(); } });
		wRefresh=new Button(shell, SWT.PUSH);
		wRefresh.setText(BaseMessages.getString(PKG, "VersionRegistryDialog.Button.Refresh"));
		wRefresh.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { refresh(); } });

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose, wUpdate, wDelete, wRefresh, }, margin, null);

		FormData fdVersions = new FormData();
		fdVersions.left   = new FormAttachment(0, 0);
		fdVersions.top    = new FormAttachment(0, 0);
		fdVersions.right  = new FormAttachment(100, 0);
		fdVersions.bottom = new FormAttachment(wClose, -margin);
		wVersions.setLayoutData(fdVersions);


		// Add listeners
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	private void getVersions() {
		try {
			versions = versionRegistry.getVersions();
		} catch(Exception e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotGetVersions.Title"), BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotGetVersions.Message"), e);
			versions = new ArrayList<ObjectVersion>(); // empty, start from scratch.
		}
	}

	protected void update() {
		try {
			int nr = wVersions.nrNonEmpty();
			for (int i=0;i<nr;i++) {
				TableItem item = wVersions.getNonEmpty(i);
				int colnr=1;
				String label = item.getText(colnr++);
				String description = item.getText(colnr++);
				Date plannedReleaseDate = XMLHandler.stringToDate(item.getText(colnr++));
				String previousVersion = item.getText(colnr++);
				
				ObjectVersion version = new SimpleObjectVersion(label, description, previousVersion, plannedReleaseDate);
				
				if (findVersion(label)==null) {
					// This is a new version entry...
					//
					versionRegistry.addVersion(version);
				} else {
					// Update
					//
					versionRegistry.updateVersion(version);
				}
				
				refresh();
			}
		} catch(Exception e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotUpdateVersions.Title"), BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotUpdateVersions.Message"), e);
		}
	}

	protected void delete() {
		try {
			
			int[] indices = wVersions.getSelectionIndices();
			for (int index : indices) {
				String label = wVersions.table.getItem(index).getText(1);
				versionRegistry.removeVersion(label);
			}
			refresh();
		} catch(Exception e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotUpdateVersions.Title"), BaseMessages.getString(PKG, "VersionRegistryDialog.Exception.CouldNotUpdateVersions.Message"), e);
		}
	}

	private ObjectVersion findVersion(String label) {
		for (ObjectVersion version : versions) {
			if (version.getLabel().equals(label)) {
				return version;
			}
		}
		return null;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public RepositoryVersionRegistry getVersionRegistry() {
		return versionRegistry;
	}
	
	/**
	 * Copy information from the input buffer to the dialog fields.
	 */ 
	private void getData()
	{
		for (int i=0;i<versions.size();i++)
		{
			ObjectVersion version = versions.get(i);
			TableItem item = new TableItem(wVersions.table, SWT.NONE);
			int colnr=1;
			item.setText(colnr++, version.getLabel());
			item.setText(colnr++, Const.NVL(version.getDescription(), ""));
			item.setText(colnr++, version.getPlannedReleaseDate()==null ? "" : XMLHandler.date2string(version.getPlannedReleaseDate()));
			item.setText(colnr++, Const.NVL(version.getPreviousVersion(), ""));
		}
		wVersions.removeEmptyRows();
		wVersions.setRowNums();
		wVersions.optWidth(true);
	}
	
	private void refresh()
	{
		getVersions();
		wVersions.clearAll();
		getData();
	}	
	
	private void close()
	{
		dispose();
	}	
}
