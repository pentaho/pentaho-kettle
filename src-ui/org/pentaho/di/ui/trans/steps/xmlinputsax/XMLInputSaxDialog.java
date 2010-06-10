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

package org.pentaho.di.ui.trans.steps.xmlinputsax;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.xmlinputsax.XMLInputSaxField;
import org.pentaho.di.trans.steps.xmlinputsax.XMLInputSaxFieldPosition;
import org.pentaho.di.trans.steps.xmlinputsax.XMLInputSaxFieldRetriever;
import org.pentaho.di.trans.steps.xmlinputsax.XMLInputSaxMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class XMLInputSaxDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = XMLInputSaxMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder wTabFolder;

	private FormData fdTabFolder;

	private CTabItem wFileTab, wContentTab, wFieldsTab;

	private Composite wFileComp, wContentComp, wFieldsComp;

	private FormData fdFileComp, fdContentComp, fdFieldsComp;

	private Label wlFilename;

	private Button wbbFilename; // Browse: add file or directory

	private Button wbdFilename; // Delete

	private Button wbeFilename; // Edit

	private Button wbaFilename; // Add or change

	private TextVar wFilename;

	private FormData fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

	private Label wlFilenameList;

	private TableView wFilenameList;

	private FormData fdlFilenameList, fdFilenameList;

	private Label wlFilemask;

	private Text wFilemask;

	private FormData fdlFilemask, fdFilemask;

	private Button wbShowFiles;

	private FormData fdbShowFiles;

	private Label wlInclFilename;

	private Button wInclFilename;

	private FormData fdlInclFilename, fdInclFilename;

	private Label wlInclFilenameField;

	private Text wInclFilenameField;

	private FormData fdlInclFilenameField, fdInclFilenameField;

	private Label wlInclRownum;

	private Button wInclRownum;

	private FormData fdlInclRownum, fdRownum;

	private Label wlInclRownumField;

	private Text wInclRownumField;

	private FormData fdlInclRownumField, fdInclRownumField;

	private Label wlLimit;

	private Text wLimit;

	private FormData fdlLimit, fdLimit;

	private Label wlPosition;

	private TableView wPosition;

	private FormData fdlPosition, fdPosition;

	private TableView wFields;

	private FormData fdFields;

	private TableView wAttributes;

	private FormData fdAttributes;

	private XMLInputSaxMeta input;

	private static final String STRING_PREVIEW_ROWS = BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.PreviewRows.Label"); //$NON-NLS-1$

	public static final int dateLengths[] = new int[] { 23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6 };

	public XMLInputSaxDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (XMLInputSaxMeta) in;
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
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Shell.Text")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.StepName.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top = new FormAttachment(0, margin);
		fdlStepname.right = new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		// ////////////////////////
		// START OF FILE TAB ///
		// ////////////////////////
		wFileTab = new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.File.Label")); //$NON-NLS-1$

		wFileComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename = new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.FileOrDirectory.Label")); //$NON-NLS-1$
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(0, 0);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.System.Button.Browse")); //$NON-NLS-1$
		wbbFilename.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.Brows.ToolTip")); //$NON-NLS-1$
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbaFilename);
		wbaFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.AddFile.Label")); //$NON-NLS-1$
		wbaFilename.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.AddFile.ToolTip")); //$NON-NLS-1$
		fdbaFilename = new FormData();
		fdbaFilename.right = new FormAttachment(wbbFilename, -margin);
		fdbaFilename.top = new FormAttachment(0, 0);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename = new TextVar(transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right = new FormAttachment(wbaFilename, -margin);
		fdFilename.top = new FormAttachment(0, 0);
		wFilename.setLayoutData(fdFilename);

		wlFilemask = new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.FileMaskRegExp.Label")); //$NON-NLS-1$
		props.setLook(wlFilemask);
		fdlFilemask = new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top = new FormAttachment(wFilename, margin);
		fdlFilemask.right = new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask = new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilemask);
		wFilemask.addModifyListener(lsMod);
		fdFilemask = new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top = new FormAttachment(wFilename, margin);
		fdFilemask.right = new FormAttachment(100, 0);
		wFilemask.setLayoutData(fdFilemask);

		// Filename list line
		wlFilenameList = new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.SelectedFiles.Label")); //$NON-NLS-1$
		props.setLook(wlFilenameList);
		fdlFilenameList = new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right = new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbdFilename);
		wbdFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.DeleteEntry.Label")); //$NON-NLS-1$
		wbdFilename.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.DeleteEntry.ToolTip")); //$NON-NLS-1$
		fdbdFilename = new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top = new FormAttachment(wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbeFilename);
		wbeFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.EditEntry.Label")); //$NON-NLS-1$
		wbeFilename.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.EditEntry.ToolTip")); //$NON-NLS-1$
		fdbeFilename = new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top = new FormAttachment(wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.ShowFilenames.Label")); //$NON-NLS-1$
		fdbShowFiles = new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo = new ColumnInfo[2];
		colinfo[0] = new ColumnInfo( BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.FileDirectory.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		colinfo[1] = new ColumnInfo( BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Wildcard.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

		colinfo[0].setUsingVariables(true);
		colinfo[1].setToolTip(BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.FileDirectory.ToolTip")); //$NON-NLS-1$

		wFilenameList = new TableView(transMeta, wFileComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 2,
				lsMod, props);
		props.setLook(wFilenameList);
		fdFilenameList = new FormData();
		fdFilenameList.left = new FormAttachment(middle, 0);
		fdFilenameList.right = new FormAttachment(wbdFilename, -margin);
		fdFilenameList.top = new FormAttachment(wFilemask, margin);
		fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
		wFilenameList.setLayoutData(fdFilenameList);

		fdFileComp = new FormData();
		fdFileComp.left = new FormAttachment(0, 0);
		fdFileComp.top = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom = new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);

		wFileComp.layout();
		wFileTab.setControl(wFileComp);

		// ///////////////////////////////////////////////////////////
		// / END OF FILE TAB
		// ///////////////////////////////////////////////////////////

		// ////////////////////////
		// START OF CONTENT TAB///
		// /
		wContentTab = new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Tab.Content.Label")); //$NON-NLS-1$

		FormLayout contentLayout = new FormLayout();
		contentLayout.marginWidth = 3;
		contentLayout.marginHeight = 3;

		wContentComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		wlInclFilename = new Label(wContentComp, SWT.RIGHT);
		wlInclFilename.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.IncludeFilename.Label")); //$NON-NLS-1$
		props.setLook(wlInclFilename);
		fdlInclFilename = new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top = new FormAttachment(0, 0);
		fdlInclFilename.right = new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclFilename);
		wInclFilename.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.IncludeFilename.ToolTip")); //$NON-NLS-1$
		fdInclFilename = new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top = new FormAttachment(0, 0);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField = new Label(wContentComp, SWT.LEFT);
		wlInclFilenameField.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.FilenameFieldname.Label")); //$NON-NLS-1$
		props.setLook(wlInclFilenameField);
		fdlInclFilenameField = new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top = new FormAttachment(0, 0);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField = new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
		fdInclFilenameField.top = new FormAttachment(0, 0);
		fdInclFilenameField.right = new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclRownum = new Label(wContentComp, SWT.RIGHT);
		wlInclRownum.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.RowNumInOutput.Label")); //$NON-NLS-1$
		props.setLook(wlInclRownum);
		fdlInclRownum = new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right = new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum = new Button(wContentComp, SWT.CHECK);
		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "XMLInputSaxDialog.RowNumInOutput.ToolTip")); //$NON-NLS-1$
		fdRownum = new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField = new Label(wContentComp, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.RowNumInOutputField.Label")); //$NON-NLS-1$
		props.setLook(wlInclRownumField);
		fdlInclRownumField = new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField = new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right = new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);

		wlLimit = new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Limit.Label")); //$NON-NLS-1$
		props.setLook(wlLimit);
		fdlLimit = new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top = new FormAttachment(wInclRownumField, margin);
		fdlLimit.right = new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit = new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit = new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top = new FormAttachment(wInclRownumField, margin);
		fdLimit.right = new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		String positionHelp = BaseMessages.getString(PKG, "XMLInputSaxDialog.Location.ToolTip"); //$NON-NLS-1$

		wlPosition = new Label(wContentComp, SWT.RIGHT);
		wlPosition.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Location.Label")); //$NON-NLS-1$
		wlPosition.setToolTipText(positionHelp);
		props.setLook(wlPosition);
		fdlPosition = new FormData();
		fdlPosition.left = new FormAttachment(0, 0);
		fdlPosition.top = new FormAttachment(wLimit, margin);
		fdlPosition.right = new FormAttachment(middle, -margin);
		wlPosition.setLayoutData(fdlPosition);

		ColumnInfo[] locationColumns = new ColumnInfo[] { new ColumnInfo(BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Elements.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false) //$NON-NLS-1$
		};
		locationColumns[0].setToolTip(positionHelp);

		int nrElements = input.getInputPosition() != null ? input.getInputPosition().length : 0;

		wPosition = new TableView(transMeta, wContentComp, SWT.FULL_SELECTION | SWT.MULTI, locationColumns, nrElements,
				lsMod, props);
		wPosition.addModifyListener(lsMod);
		fdPosition = new FormData();
		fdPosition.left = new FormAttachment(middle, 0);
		fdPosition.top = new FormAttachment(wLimit, margin);
		fdPosition.bottom = new FormAttachment(100, -50);
		fdPosition.right = new FormAttachment(100, 0);
		wPosition.setLayoutData(fdPosition);
		wPosition.setToolTipText(positionHelp);

		fdContentComp = new FormData();
		fdContentComp.left = new FormAttachment(0, 0);
		fdContentComp.top = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom = new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////

		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Tab.Fields.Label")); //$NON-NLS-1$

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;

		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
		props.setLook(wFieldsComp);

		wGet = new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Button.GetFields.Label")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.left = new FormAttachment(50, 0);
		fdGet.bottom = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);

		final int FieldsRows = input.getInputFields().length;

		// Prepare a list of possible formats...
		String dats[] = Const.getDateFormats();
		String nums[] = Const.getNumberFormats();
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x = 0; x < dats.length; x++)
			formats[x] = dats[x];
		for (int x = 0; x < nums.length; x++)
			formats[dats.length + x] = nums[x];

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Name.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Type.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes(), true), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Format.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, formats), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Length.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Precision.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Currency.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Decimal.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Group.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.TrimType.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, XMLInputSaxField.trimTypeDesc, true), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Repeat.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "Y", "N" }, true), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Position.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wFields = new TableView(transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

		final int AttributesRows = input.getDefinitionLength();

		ColumnInfo[] colinfatt = new ColumnInfo[] {
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.Element.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
				new ColumnInfo(
						BaseMessages.getString(PKG, "XMLInputSaxDialog.Column.DefiningAttribute.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wAttributes = new TableView(transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinfatt, AttributesRows,
				lsMod, props);

		fdAttributes = new FormData();
		fdAttributes.left = new FormAttachment(0, 0);
		fdAttributes.top = new FormAttachment(0, 0);
		fdAttributes.right = new FormAttachment(100, 0);
		fdAttributes.bottom = new FormAttachment(15, 0);
		wAttributes.setLayoutData(fdAttributes);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wAttributes,margin*3);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp = new FormData();
		fdFieldsComp.left = new FormAttachment(0, 0);
		fdFieldsComp.top = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom = new FormAttachment(110, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);

		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);

		fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(STRING_PREVIEW_ROWS);

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGet = new Listener()
		{
			public void handleEvent(Event e)
			{
				get();
			}
		};
		lsPreview = new Listener()
		{
			public void handleEvent(Event e)
			{
				preview();
			}
		};
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wLimit.addSelectionListener(lsDef);
		wInclRownumField.addSelectionListener(lsDef);
		wInclFilenameField.addSelectionListener(lsDef);

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() });
				wFilename.setText(""); //$NON-NLS-1$
				wFilemask.setText(""); //$NON-NLS-1$
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
				wFilenameList.optWidth(true);
			}
		};
		wbaFilename.addSelectionListener(selA);
		wFilename.addSelectionListener(selA);

		// Delete files from the list of files...
		wbdFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFilenameList.getSelectionIndices();
				wFilenameList.remove(idx);
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFilenameList.getSelectionIndex();
				if (idx >= 0)
				{
					String string[] = wFilenameList.getItem(idx);
					wFilename.setText(string[0]);
					wFilemask.setText(string[1]);
					wFilenameList.remove(idx);
				}
				wFilenameList.removeEmptyRows();
				wFilenameList.setRowNums();
			}
		});

		// Show the files that are selected at this time...
		wbShowFiles.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					XMLInputSaxMeta tfii = new XMLInputSaxMeta();
					getInfo(tfii);
					String files[] = tfii.getFilePaths(transMeta);
					if (files != null && files.length > 0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(
								shell,
								files,
								BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.FilesRead.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.FilesRead.Message")); //$NON-NLS-1$ //$NON-NLS-2$
						esd.setViewOnly();
						esd.open();
					} else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
						mb.setMessage(BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.NoFilesFound.Message")); //$NON-NLS-1$
						mb.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.NoFilesFound.Title")); //$NON-NLS-1$
						mb.open();
					}
				} catch (KettleException ex)
				{
					new ErrorDialog(
							shell,
							BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Message"), ex); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		// Enable/disable the right fields to allow a filename to be added to
		// each row...
		wInclFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setIncludeFilename();
			}
		});

		// Enable/disable the right fields to allow a row number to be added to
		// each row...
		wInclRownum.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setIncludeRownum();
			}
		});

		// Whenever something changes, set the tooltip to the expanded version
		// of the filename:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText()));
			}
		});

		// Listen to the Browse... button
		wbbFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				if (wFilemask.getText() != null && wFilemask.getText().length() > 0) // A
																						// mask:
																						// a
																						// directory!
				{
					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFilename.getText() != null)
					{
						String fpath = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFilterPath(fpath);
					}

					if (dialog.open() != null)
					{
						String str = dialog.getFilterPath();
						wFilename.setText(str);
					}
				} else
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] { "*.xml;*.XML", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
					if (wFilename.getText() != null)
					{
						String fname = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFileName(fname);
					}

					dialog
							.setFilterNames(new String[] {
									BaseMessages.getString(PKG, "XMLInputSaxDialog.60"), BaseMessages.getString(PKG, "XMLInputSaxDialog.68") }); //$NON-NLS-1$ //$NON-NLS-2$

					if (dialog.open() != null)
					{
						String str = dialog.getFilterPath()
								+ System.getProperty("file.separator") + dialog.getFileName(); //$NON-NLS-1$
						wFilename.setText(str);
					}
				}
			}
		});

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		input.setChanged(changed);
		wFields.optWidth(true);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	public void setMultiple()
	{
		/*
		 * wlFilemask.setEnabled(wMultiple.getSelection());
		 * wFilemask.setEnabled(wMultiple.getSelection());
		 * wlFilename.setText(wMultiple.getSelection()?"Directory":"Filename ");
		 */
	}

	public void setIncludeFilename()
	{
		wlInclFilenameField.setEnabled(wInclFilename.getSelection());
		wInclFilenameField.setEnabled(wInclFilename.getSelection());
	}

	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}

	/**
	 * Read the data from the XMLvInputMeta object and show it in this dialog.
	 * 
	 * @param in
	 *            The XMLvInputMeta object to obtain the data from.
	 */
	public void getData(XMLInputSaxMeta in)
	{
		if (in.getFileName() != null)
		{
			wFilenameList.removeAll();
			for (int i = 0; i < in.getFileName().length; i++)
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] });
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		// wMultiple.setSelection(in.wildcard);
		if (in.getFilenameField() != null)
			wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField() != null)
			wInclRownumField.setText(in.getRowNumberField());
		wLimit.setText("" + in.getRowLimit()); //$NON-NLS-1$

		logDebug("getting fields info..."); //$NON-NLS-1$
		for (int i = 0; i < in.getInputFields().length; i++)
		{
			XMLInputSaxField field = in.getInputFields()[i];

			if (field != null)
			{
				TableItem item = wFields.table.getItem(i);
				String name = field.getName();
				String type = field.getTypeDesc();
				String format = field.getFormat();
				String length = "" + field.getLength(); //$NON-NLS-1$
				String prec = "" + field.getPrecision(); //$NON-NLS-1$
				String curr = field.getCurrencySymbol();
				String group = field.getGroupSymbol();
				String decim = field.getDecimalSymbol();
				String trim = field.getTrimTypeDesc();
				String rep = field.isRepeated() ? "Y" : "N"; //$NON-NLS-1$ //$NON-NLS-2$

				if (name != null)
					item.setText(1, name);
				if (type != null)
					item.setText(2, type);
				if (format != null)
					item.setText(3, format);
				if (length != null && !"-1".equals(length))item.setText(4, length); //$NON-NLS-1$
				if (prec != null && !"-1".equals(prec))item.setText(5, prec); //$NON-NLS-1$
				if (curr != null)
					item.setText(6, curr);
				if (decim != null)
					item.setText(7, decim);
				if (group != null)
					item.setText(8, group);
				if (trim != null)
					item.setText(9, trim);
				if (rep != null)
					item.setText(10, rep);

				item.setText(11, field.getFieldPositionsCode());
			}
		}

		for (int i = 0; i < input.getDefinitionLength(); i++)
		{
			TableItem item = wAttributes.table.getItem(i);
			item.setText(1, input.getDefiningElement(i));
			item.setText(2, input.getDefiningAttribute(i));
		}

		for (int i = 0; i < input.getInputPosition().length; i++)
		{
			TableItem item = wPosition.table.getItem(i);
			if (input.getInputPosition()[i] != null)
				item.setText(1, input.getInputPosition()[i].toString());
		}

		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);

		wPosition.removeEmptyRows();
		wPosition.setRowNums();
		wPosition.optWidth(true);

		setMultiple();
		setIncludeFilename();
		setIncludeRownum();

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		try
		{
			getInfo(input);
		} catch (KettleException e)
		{
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		dispose();
	}

	// get metadata from ui to in
	private void getInfo(XMLInputSaxMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit(Const.toLong(wLimit.getText(), 0L));
		in.setFilenameField(wInclFilenameField.getText());
		in.setRowNumberField(wInclRownumField.getText());

		in.setIncludeFilename(wInclFilename.getSelection());
		in.setIncludeRowNumber(wInclRownum.getSelection());

		int nrFiles = wFilenameList.getItemCount();
		int nrAttributes = wAttributes.nrNonEmpty();
		int nrFields = wFields.nrNonEmpty();
		int nrPositions = wPosition.nrNonEmpty();

		in.allocate(nrFiles, nrFields, nrPositions);

		in.setFileName(wFilenameList.getItems(0));
		in.setFileMask(wFilenameList.getItems(1));

		in.clearDefinition();
		for (int i = 0; i < nrAttributes; i++)
		{
			TableItem item = wAttributes.getNonEmpty(i);
			in.setDefiningAttribute(item.getText(1), item.getText(2));
		}

		for (int i = 0; i < nrFields; i++)
		{
			XMLInputSaxField field = new XMLInputSaxField();

			TableItem item = wFields.getNonEmpty(i);

			field.setName(item.getText(1));
			field.setType(Value.getType(item.getText(2)));
			field.setFormat(item.getText(3));
			field.setLength(Const.toInt(item.getText(4), -1));
			field.setPrecision(Const.toInt(item.getText(5), -1));
			field.setCurrencySymbol(item.getText(6));
			field.setDecimalSymbol(item.getText(7));
			field.setGroupSymbol(item.getText(8));
			field.setTrimType(XMLInputSaxField.getTrimType(item.getText(9)));
			field.setRepeated("Y".equalsIgnoreCase(item.getText(10))); //$NON-NLS-1$
			field.setFieldPosition(item.getText(11));

			in.getInputFields()[i] = field;
		}

		for (int i = 0; i < nrPositions; i++)
		{
			TableItem item = wPosition.getNonEmpty(i);
			String encode = item.getText(1);
			in.getInputPosition()[i] = new XMLInputSaxFieldPosition(encode);
			// System.out.println("Input Position #"+i+" :
			// "+input.getInputPosition());
		}
	}

	//
	private void get()
	{
		try
		{
			XMLInputSaxMeta meta = new XMLInputSaxMeta();
			getInfo(meta);

			// OK, let's try to walk through the complete tree

			List<XMLInputSaxField> fields = new ArrayList<XMLInputSaxField>();

			// Keep the list of positions

			String[] filePaths = meta.getFilePaths(transMeta);
			
			if (meta.getInputPosition().length==0)
			{
				//error
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.NoElements.Message")); //$NON-NLS-1$
				mb.setText(BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.NoElements.Title")); //$NON-NLS-1$
				mb.open();
				return;
			}
			for (int f = 0; f < filePaths.length; f++)
			{
				XMLInputSaxFieldRetriever fieldRetreiver = new XMLInputSaxFieldRetriever(log, filePaths[f], meta);

				fields = fieldRetreiver.getFields();

				// add the values to the grid...
				for (int i = 0; i < fields.size(); i++)
				{
					XMLInputSaxField iF = (XMLInputSaxField) fields.get(i);
					TableItem item = new TableItem(wFields.table, SWT.NONE);
					item.setText(1, iF.getName());
					item.setText(2, iF.getTypeDesc());
					item.setText(11, iF.getFieldPositionsCode(meta.getInputPosition().length));
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		} catch (KettleException e)
		{
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorParsingInputData.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(e.getMessage());
		}
	}

	// Preview the data
	private void preview()
	{
		try
		{
			// Create the XML input step
			XMLInputSaxMeta oneMeta = new XMLInputSaxMeta();
			getInfo(oneMeta);

			TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname
					.getText());

			EnterNumberDialog numberDialog = new EnterNumberDialog(
					shell,
					props.getDefaultPreviewSize(),
					BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.EnterPreviewSize.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.EnterPreviewSize.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			int previewSize = numberDialog.open();
			if (previewSize > 0)
			{
				TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell,
						previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize });
				progressDialog.open();

				if (!progressDialog.isCancelled())
				{
					PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()),
							progressDialog.getPreviewRows(wStepname.getText()));
					prd.open();
				}
			}
		} catch (KettleException e)
		{
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorDisplayingPreviewData.Title"), BaseMessages.getString(PKG, "XMLInputSaxDialog.Dialog.ErrorDisplayingPreviewData.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}

}