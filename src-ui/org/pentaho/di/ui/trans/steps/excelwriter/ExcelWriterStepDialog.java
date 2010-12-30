//
// Excel Writer plugin for Pentaho PDI a.k.a. Kettle
// 
// Copyright (C) 2010 Slawomir Chodnicki
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package org.pentaho.di.ui.trans.steps.excelwriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelwriter.ExcelWriterStepField;
import org.pentaho.di.trans.steps.excelwriter.ExcelWriterStepMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class ExcelWriterStepDialog extends BaseStepDialog implements StepDialogInterface {
	private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n

	private CTabFolder wTabFolder;
	private FormData fdTabFolder;

	private CTabItem wFileTab, wContentTab;

	private FormData fdFileComp, fdContentComp;

	private Label wlFilename;
	private Button wbFilename;
	private TextVar wFilename;
	private FormData fdlFilename, fdbFilename, fdFilename;

	private Label wlExtension;
	private CCombo wExtension;
	private FormData fdlExtension, fdExtension;

	private Label wlAddStepnr;
	private Button wAddStepnr;
	private FormData fdlAddStepnr, fdAddStepnr;

	private Label wlAddDate;
	private Button wAddDate;
	private FormData fdlAddDate, fdAddDate;

	private Label wlAddTime;
	private Button wAddTime;
	private FormData fdlAddTime, fdAddTime;

	private Label wlProtectSheet;
	private Button wProtectSheet;
	private FormData fdlProtectSheet, fdProtectSheet;

	private Button wbShowFiles;
	private FormData fdbShowFiles;

	private Label wlHeader;
	private Button wHeader;
	private FormData fdlHeader, fdHeader;

	private Label wlFooter;
	private Button wFooter;
	private FormData fdlFooter, fdFooter;

	private Label wlSplitEvery;
	private Text wSplitEvery;
	private FormData fdlSplitEvery, fdSplitEvery;

	private Label wlTemplate;
	private Button wTemplate;
	private FormData fdlTemplate, fdTemplate;

	private Label wlTemplateFilename;
	private Button wbTemplateFilename;
	private TextVar wTemplateFilename;
	private FormData fdlTemplateFilename, fdbTemplateFilename, fdTemplateFilename;

	private Label wlPassword;
	private TextVar wPassword;
	private FormData fdlPassword, fdPassword;

	private Label wlSheetname;
	private TextVar wSheetname;
	private FormData fdlSheetname, fdSheetname;

	private TableView wFields;
	private FormData fdFields;

	private ExcelWriterStepMeta input;

	private Button wMinWidth;
	private Listener lsMinWidth;

	private Label wlAddToResult;
	private Button wAddToResult;
	private FormData fdlAddToResult, fdAddToResult;

	//	private Label wlAppend;
	//	private Button wAppend;
	//	private FormData fdlAppend, fdAppend;

	private Label wlDoNotOpenNewFileInit;
	private Button wDoNotOpenNewFileInit;
	private FormData fdlDoNotOpenNewFileInit, fdDoNotOpenNewFileInit;

	private Label wlSpecifyFormat;
	private Button wSpecifyFormat;
	private FormData fdlSpecifyFormat, fdSpecifyFormat;

	private Label wlDateTimeFormat;
	private CCombo wDateTimeFormat;
	private FormData fdlDateTimeFormat, fdDateTimeFormat;

	private Label wlAutoSize;
	private Button wAutoSize;
	private FormData fdlAutoSize, fdAutoSize;

//	private Label wlNullIsBlank;
//	private Button wNullIsBlank;
//	private FormData fdlNullIsBlank, fdNullIsBlank;

	private Group wTemplateGroup;
	private FormData fdTemplateGroup;

	private ColumnInfo[] colinf;

	private Map<String, Integer> inputFields;

	private Label wlIfFileExists;

	private FormData fdlIfFileExists;

	private CCombo wIfFileExists;

	private Label wlIfSheetExists;

	private CCombo wIfSheetExists;

	private Label wlTemplateSheetname;

	private TextVar wTemplateSheetname;

	private Label wlStartingCell;

	private TextVar wStartingCell;

	private Label wlRowWritingMethod;

	private CCombo wRowWritingMethod;

	private Label wlTemplateSheet;

	private Button wTemplateSheet;

	private Button wAppendLines;

	private Text wSkipRows;

	private Text wEmptyRows;

	private Button wOmitHeader;

	private TextVar wProtectedBy;

	private Button wMakeActiveSheet;
	private Label wlForceFormulaRecalculation;
	private FormData fdlForceFormulaRecalculation;
	private Button wForceFormulaRecalculation;
	private FormData fdForceFormulaRecalculation;
	private Label wlLeaveExistingStylesUnchanged;
	private FormData fdlLeaveExistingStylesUnchanged;
	private Button wLeaveExistingStylesUnchanged;
	private FormData fdLeaveExistingStylesUnchanged;

	public ExcelWriterStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (ExcelWriterStepMeta) in;
		inputFields = new HashMap<String, Integer>();
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.DialogTitle"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		// START OF FILE TAB///
		// /
		wFileTab = new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.FileTab.TabTitle"));

		Composite wFileComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		Group fileGroup = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(fileGroup);
		fileGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.fileGroup.Label"));

		FormLayout fileGroupgroupLayout = new FormLayout();
		fileGroupgroupLayout.marginWidth = 10;
		fileGroupgroupLayout.marginHeight = 10;
		fileGroup.setLayout(fileGroupgroupLayout);

		// Filename line
		wlFilename = new Label(fileGroup, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(0, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(fileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(0, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(transMeta, fileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		wFilename.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Filename.Tooltip"));
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top = new FormAttachment(0, margin);
		fdFilename.right = new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Extension line
		wlExtension = new Label(fileGroup, SWT.RIGHT);
		wlExtension.setText(BaseMessages.getString(PKG, "System.Label.Extension"));
		props.setLook(wlExtension);
		fdlExtension = new FormData();
		fdlExtension.left = new FormAttachment(0, 0);
		fdlExtension.top = new FormAttachment(wFilename, margin);
		fdlExtension.right = new FormAttachment(middle, -margin);
		wlExtension.setLayoutData(fdlExtension);
		wExtension = new CCombo(fileGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		String xlsLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLS.Label");
		String xlsxLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLSX.Label");
		wExtension.setItems(new String[] { xlsLabel, xlsxLabel });
		wExtension.setData(xlsLabel, "xls");
		wExtension.setData(xlsxLabel, "xlsx");

		props.setLook(wExtension);
		wExtension.addModifyListener(lsMod);

		wExtension.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				enableExtension();
			}
		});
		
		wExtension.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Extension.Tooltip"));

		fdExtension = new FormData();
		fdExtension.left = new FormAttachment(middle, 0);
		fdExtension.top = new FormAttachment(wFilename, margin);
		fdExtension.right = new FormAttachment(wbFilename, -margin);
		wExtension.setLayoutData(fdExtension);

		// split every x rows
		wlSplitEvery = new Label(fileGroup, SWT.RIGHT);
		wlSplitEvery.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.SplitEvery.Label"));
		props.setLook(wlSplitEvery);
		fdlSplitEvery = new FormData();
		fdlSplitEvery.left = new FormAttachment(0, 0);
		fdlSplitEvery.top = new FormAttachment(wExtension, margin);
		fdlSplitEvery.right = new FormAttachment(middle, -margin);
		wlSplitEvery.setLayoutData(fdlSplitEvery);
		wSplitEvery = new Text(fileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSplitEvery);
		wSplitEvery.addModifyListener(lsMod);
		wSplitEvery.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.SplitEvery.Tooltip"));
		fdSplitEvery = new FormData();
		fdSplitEvery.left = new FormAttachment(middle, 0);
		fdSplitEvery.top = new FormAttachment(wExtension, margin);
		fdSplitEvery.right = new FormAttachment(100, 0);
		wSplitEvery.setLayoutData(fdSplitEvery);

		// Create multi-part file?
		wlAddStepnr = new Label(fileGroup, SWT.RIGHT);
		wlAddStepnr.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AddStepnr.Label"));
		props.setLook(wlAddStepnr);
		fdlAddStepnr = new FormData();
		fdlAddStepnr.left = new FormAttachment(0, 0);
		fdlAddStepnr.top = new FormAttachment(wSplitEvery, margin);
		fdlAddStepnr.right = new FormAttachment(middle, -margin);
		wlAddStepnr.setLayoutData(fdlAddStepnr);
		wAddStepnr = new Button(fileGroup, SWT.CHECK);
		props.setLook(wAddStepnr);
		fdAddStepnr = new FormData();
		fdAddStepnr.left = new FormAttachment(middle, 0);
		fdAddStepnr.top = new FormAttachment(wSplitEvery, margin);
		fdAddStepnr.right = new FormAttachment(100, 0);
		wAddStepnr.setLayoutData(fdAddStepnr);
		wAddStepnr.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});

		// Create multi-part file?
		wlAddDate = new Label(fileGroup, SWT.RIGHT);
		wlAddDate.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AddDate.Label"));
		props.setLook(wlAddDate);
		fdlAddDate = new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top = new FormAttachment(wAddStepnr, margin);
		fdlAddDate.right = new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate = new Button(fileGroup, SWT.CHECK);
		props.setLook(wAddDate);
		fdAddDate = new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top = new FormAttachment(wAddStepnr, margin);
		fdAddDate.right = new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});
		// Create multi-part file?
		wlAddTime = new Label(fileGroup, SWT.RIGHT);
		wlAddTime.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AddTime.Label"));
		props.setLook(wlAddTime);
		fdlAddTime = new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top = new FormAttachment(wAddDate, margin);
		fdlAddTime.right = new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime = new Button(fileGroup, SWT.CHECK);
		props.setLook(wAddTime);
		fdAddTime = new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top = new FormAttachment(wAddDate, margin);
		fdAddTime.right = new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});
		// Specify date time format?
		wlSpecifyFormat = new Label(fileGroup, SWT.RIGHT);
		wlSpecifyFormat.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.SpecifyFormat.Label"));
		props.setLook(wlSpecifyFormat);
		fdlSpecifyFormat = new FormData();
		fdlSpecifyFormat.left = new FormAttachment(0, 0);
		fdlSpecifyFormat.top = new FormAttachment(wAddTime, margin);
		fdlSpecifyFormat.right = new FormAttachment(middle, -margin);
		wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
		wSpecifyFormat = new Button(fileGroup, SWT.CHECK);
		props.setLook(wSpecifyFormat);
		wSpecifyFormat.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.SpecifyFormat.Tooltip"));
		fdSpecifyFormat = new FormData();
		fdSpecifyFormat.left = new FormAttachment(middle, 0);
		fdSpecifyFormat.top = new FormAttachment(wAddTime, margin);
		fdSpecifyFormat.right = new FormAttachment(100, 0);
		wSpecifyFormat.setLayoutData(fdSpecifyFormat);
		wSpecifyFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				setDateTimeFormat();
			}
		});

		// Prepare a list of possible DateTimeFormats...
		String dats[] = Const.getDateFormats();

		// DateTimeFormat
		wlDateTimeFormat = new Label(fileGroup, SWT.RIGHT);
		wlDateTimeFormat.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.DateTimeFormat.Label"));
		props.setLook(wlDateTimeFormat);
		fdlDateTimeFormat = new FormData();
		fdlDateTimeFormat.left = new FormAttachment(0, 0);
		fdlDateTimeFormat.top = new FormAttachment(wSpecifyFormat, margin);
		fdlDateTimeFormat.right = new FormAttachment(middle, -margin);
		wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
		wDateTimeFormat = new CCombo(fileGroup, SWT.BORDER | SWT.READ_ONLY);
		wDateTimeFormat.setEditable(true);
		props.setLook(wDateTimeFormat);
		wDateTimeFormat.addModifyListener(lsMod);
		fdDateTimeFormat = new FormData();
		fdDateTimeFormat.left = new FormAttachment(middle, 0);
		fdDateTimeFormat.top = new FormAttachment(wSpecifyFormat, margin);
		fdDateTimeFormat.right = new FormAttachment(100, 0);
		wDateTimeFormat.setLayoutData(fdDateTimeFormat);
		for (int x = 0; x < dats.length; x++)
			wDateTimeFormat.add(dats[x]);

		wbShowFiles = new Button(fileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbShowFiles);
		wbShowFiles.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ShowFiles.Button"));
		fdbShowFiles = new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.top = new FormAttachment(wDateTimeFormat, margin * 3);
		wbShowFiles.setLayoutData(fdbShowFiles);
		wbShowFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExcelWriterStepMeta tfoi = new ExcelWriterStepMeta();
				getInfo(tfoi);
				String files[] = tfoi.getFiles(transMeta);
				if (files != null && files.length > 0) {
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, BaseMessages.getString(PKG, "ExcelWriterDialog.SelectOutputFiles.DialogTitle"), BaseMessages.getString(PKG,
							"ExcelWriterDialog.SelectOutputFiles.DialogMessage"));
					esd.setViewOnly();
					esd.open();
				} else {
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setMessage(BaseMessages.getString(PKG, "ExcelWriterDialog.NoFilesFound.DialogMessage"));
					mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
					mb.open();
				}
			}
		});

		// If output file exists line
		wlIfFileExists = new Label(fileGroup, SWT.RIGHT);
		wlIfFileExists.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.top = new FormAttachment(wbShowFiles, 2 * margin, margin);
		fdlIfFileExists.right = new FormAttachment(middle, -margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		// wIfFileExists=new TextVar(transMeta,wFileComp, SWT.SINGLE | SWT.LEFT |
		// SWT.BORDER);
		wIfFileExists = new CCombo(fileGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		String createNewLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.IfFileExists.CreateNew.Label");
		String reuseLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.IfFileExists.Reuse.Label");
		wIfFileExists.setItems(new String[] { createNewLabel, reuseLabel });
		wIfFileExists.setData(createNewLabel, ExcelWriterStepMeta.IF_FILE_EXISTS_CREATE_NEW);
		wIfFileExists.setData(reuseLabel, ExcelWriterStepMeta.IF_FILE_EXISTS_REUSE);

		props.setLook(wIfFileExists);
		wIfFileExists.addModifyListener(lsMod);
		wIfFileExists.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.IfFileExists.Tooltip"));


		FormData fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wbShowFiles, 2 * margin, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		// Open new File at Init
		wlDoNotOpenNewFileInit = new Label(fileGroup, SWT.RIGHT);
		wlDoNotOpenNewFileInit.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.DoNotOpenNewFileInit.Label"));
		props.setLook(wlDoNotOpenNewFileInit);
		fdlDoNotOpenNewFileInit = new FormData();
		fdlDoNotOpenNewFileInit.left = new FormAttachment(0, 0);
		fdlDoNotOpenNewFileInit.top = new FormAttachment(wIfFileExists, 2 * margin, margin);
		fdlDoNotOpenNewFileInit.right = new FormAttachment(middle, -margin);
		wlDoNotOpenNewFileInit.setLayoutData(fdlDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit = new Button(fileGroup, SWT.CHECK);
		wDoNotOpenNewFileInit.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.DoNotOpenNewFileInit.Tooltip"));
		props.setLook(wDoNotOpenNewFileInit);
		fdDoNotOpenNewFileInit = new FormData();
		fdDoNotOpenNewFileInit.left = new FormAttachment(middle, 0);
		fdDoNotOpenNewFileInit.top = new FormAttachment(wIfFileExists, 2 * margin, margin);
		fdDoNotOpenNewFileInit.right = new FormAttachment(100, 0);
		wDoNotOpenNewFileInit.setLayoutData(fdDoNotOpenNewFileInit);
		wDoNotOpenNewFileInit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});

		// Add File to the result files name
		wlAddToResult = new Label(fileGroup, SWT.RIGHT);
		wlAddToResult.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AddFileToResult.Label"));
		props.setLook(wlAddToResult);
		fdlAddToResult = new FormData();
		fdlAddToResult.left = new FormAttachment(0, 0);
		fdlAddToResult.top = new FormAttachment(wDoNotOpenNewFileInit);
		fdlAddToResult.right = new FormAttachment(middle, -margin);
		wlAddToResult.setLayoutData(fdlAddToResult);
		wAddToResult = new Button(fileGroup, SWT.CHECK);
		wAddToResult.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.AddFileToResult.Tooltip"));
		props.setLook(wAddToResult);
		fdAddToResult = new FormData();
		fdAddToResult.left = new FormAttachment(middle, 0);
		fdAddToResult.top = new FormAttachment(wDoNotOpenNewFileInit);
		fdAddToResult.right = new FormAttachment(100, 0);
		wAddToResult.setLayoutData(fdAddToResult);
		SelectionAdapter lsSelR = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				input.setChanged();
			}
		};
		wAddToResult.addSelectionListener(lsSelR);

		FormData fsFileGroup = new FormData();
		fsFileGroup.left = new FormAttachment(0, margin);
		fsFileGroup.top = new FormAttachment(0, margin);
		fsFileGroup.right = new FormAttachment(100, -margin);
		fileGroup.setLayoutData(fsFileGroup);

		// END OF FILE GROUP

		Group sheetGroup = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(sheetGroup);
		sheetGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.sheetGroup.Label"));

		FormLayout sheetGroupLayout = new FormLayout();
		sheetGroupLayout.marginWidth = 10;
		sheetGroupLayout.marginHeight = 10;
		sheetGroup.setLayout(sheetGroupLayout);

		// Sheet name line
		wlSheetname = new Label(sheetGroup, SWT.RIGHT);
		wlSheetname.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Sheetname.Label"));
		props.setLook(wlSheetname);
		fdlSheetname = new FormData();
		fdlSheetname.left = new FormAttachment(0, 0);
		fdlSheetname.top = new FormAttachment(0, margin);
		fdlSheetname.right = new FormAttachment(middle, -margin);
		wlSheetname.setLayoutData(fdlSheetname);
		wSheetname = new TextVar(transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSheetname.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Sheetname.Tooltip"));
		props.setLook(wSheetname);
		wSheetname.addModifyListener(lsMod);
		fdSheetname = new FormData();
		fdSheetname.left = new FormAttachment(middle, 0);
		fdSheetname.top = new FormAttachment(0, margin);
		fdSheetname.right = new FormAttachment(100, 0);
		wSheetname.setLayoutData(fdSheetname);
		
		// Make sheet active Sheet Line
		Label wlMakeActiveSheet = new Label(sheetGroup, SWT.RIGHT);
		wlMakeActiveSheet.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.MakeActiveSheet.Label"));
		props.setLook(wlMakeActiveSheet);
		FormData fdlMakeActiveSheet = new FormData();
		fdlMakeActiveSheet.left = new FormAttachment(0, 0);
		fdlMakeActiveSheet.top = new FormAttachment(wSheetname, margin);
		fdlMakeActiveSheet.right = new FormAttachment(middle, -margin);
		wlMakeActiveSheet.setLayoutData(fdlMakeActiveSheet);
		wMakeActiveSheet = new Button(sheetGroup, SWT.CHECK);
		wMakeActiveSheet.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.MakeActiveSheet.Tooltip"));
		props.setLook(wMakeActiveSheet);
		FormData fdMakeActiveSheet = new FormData();
		fdMakeActiveSheet.left = new FormAttachment(middle, 0);
		fdMakeActiveSheet.top = new FormAttachment(wSheetname, margin);
		fdMakeActiveSheet.right = new FormAttachment(100, 0);
		wMakeActiveSheet.setLayoutData(fdMakeActiveSheet);
		wMakeActiveSheet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});
		

		// If output sheet exists line
		wlIfSheetExists = new Label(sheetGroup, SWT.RIGHT);
		wlIfSheetExists.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.IfSheetExists.Label"));
		props.setLook(wlIfSheetExists);
		FormData fdlIfSheetExists = new FormData();
		fdlIfSheetExists.left = new FormAttachment(0, 0);
		fdlIfSheetExists.top = new FormAttachment(wMakeActiveSheet, margin);
		fdlIfSheetExists.right = new FormAttachment(middle, -margin);
		wlIfSheetExists.setLayoutData(fdlIfSheetExists);
		wIfSheetExists = new CCombo(sheetGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		String replaceSheetNewLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.IfSheetExists.CreateNew.Label");
		String reuseSheetLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.IfSheetExists.Reuse.Label");
		wIfSheetExists.setItems(new String[] { replaceSheetNewLabel, reuseSheetLabel });
		wIfSheetExists.setData(replaceSheetNewLabel, ExcelWriterStepMeta.IF_SHEET_EXISTS_CREATE_NEW);
		wIfSheetExists.setData(reuseSheetLabel, ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE);

		props.setLook(wIfSheetExists);
		wIfSheetExists.addModifyListener(lsMod);
		wIfSheetExists.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.IfSheetExists.Tooltip"));

		//		wIfSheetExists.addSelectionListener(new SelectionAdapter() {
		//			public void widgetSelected(SelectionEvent e) {
		//				input.setChanged();
		//				EnableIfSheetExists();
		//			}
		//		});

		FormData fdIfSheetExists = new FormData();
		fdIfSheetExists.left = new FormAttachment(middle, 0);
		fdIfSheetExists.top = new FormAttachment(wMakeActiveSheet, margin);
		fdIfSheetExists.right = new FormAttachment(100, 0);
		wIfSheetExists.setLayoutData(fdIfSheetExists);

		// Protect Sheet?
		wlProtectSheet = new Label(sheetGroup, SWT.RIGHT);
		wlProtectSheet.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ProtectSheet.Label"));
		props.setLook(wlProtectSheet);
		fdlProtectSheet = new FormData();
		fdlProtectSheet.left = new FormAttachment(0, 0);
		fdlProtectSheet.top = new FormAttachment(wIfSheetExists, margin);
		fdlProtectSheet.right = new FormAttachment(middle, -margin);
		wlProtectSheet.setLayoutData(fdlProtectSheet);
		wProtectSheet = new Button(sheetGroup, SWT.CHECK);
		wProtectSheet.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.ProtectSheet.Tooltip"));
		props.setLook(wProtectSheet);
		fdProtectSheet = new FormData();
		fdProtectSheet.left = new FormAttachment(middle, 0);
		fdProtectSheet.top = new FormAttachment(wIfSheetExists, margin);
		fdProtectSheet.right = new FormAttachment(100, 0);
		wProtectSheet.setLayoutData(fdProtectSheet);
		wProtectSheet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				enablePassword();
			}
		});

		// Protected by line
		Label wlProtectedBy = new Label(sheetGroup, SWT.RIGHT);
		wlProtectedBy.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ProtectedBy.Label"));
		props.setLook(wlProtectedBy);
		FormData fdlProtectedBy = new FormData();
		fdlProtectedBy.left = new FormAttachment(0, 0);
		fdlProtectedBy.top = new FormAttachment(wProtectSheet, margin);
		fdlProtectedBy.right = new FormAttachment(middle, -margin);
		wlProtectedBy.setLayoutData(fdlProtectedBy);
		wProtectedBy = new TextVar(transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wProtectedBy.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.ProtectedBy.Tooltip"));
		props.setLook(wProtectedBy);

		wProtectedBy.addModifyListener(lsMod);
		FormData fdProtectedBy = new FormData();
		fdProtectedBy.left = new FormAttachment(middle, 0);
		fdProtectedBy.top = new FormAttachment(wProtectSheet, margin);
		fdProtectedBy.right = new FormAttachment(100, 0);
		wProtectedBy.setLayoutData(fdProtectedBy);

		// Password line
		wlPassword = new Label(sheetGroup, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Password.Label"));
		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top = new FormAttachment(wProtectedBy, margin);
		fdlPassword.right = new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword = new TextVar(transMeta, sheetGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.PASSWORD);
		wPassword.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Password.Tooltip"));
		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		wPassword.addModifyListener(lsMod);
		fdPassword = new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top = new FormAttachment(wProtectedBy, margin);
		fdPassword.right = new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

		FormData fsSheetGroup = new FormData();
		fsSheetGroup.left = new FormAttachment(0, margin);
		fsSheetGroup.top = new FormAttachment(fileGroup, margin);
		fsSheetGroup.right = new FormAttachment(100, -margin);
		sheetGroup.setLayoutData(fsSheetGroup);

		// END OF SHEET GROUP

		// ///////////////////////////////
		// START OF Template Group GROUP //
		// ///////////////////////////////

		wTemplateGroup = new Group(wFileComp, SWT.SHADOW_NONE);
		props.setLook(wTemplateGroup);
		wTemplateGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.TemplateGroup.Label"));

		FormLayout TemplateGroupgroupLayout = new FormLayout();
		TemplateGroupgroupLayout.marginWidth = 10;
		TemplateGroupgroupLayout.marginHeight = 10;
		wTemplateGroup.setLayout(TemplateGroupgroupLayout);

		// Use template
		wlTemplate = new Label(wTemplateGroup, SWT.RIGHT);
		wlTemplate.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Template.Label"));
		props.setLook(wlTemplate);
		fdlTemplate = new FormData();
		fdlTemplate.left = new FormAttachment(0, 0);
		fdlTemplate.top = new FormAttachment(0, margin);
		fdlTemplate.right = new FormAttachment(middle, -margin);
		wlTemplate.setLayoutData(fdlTemplate);
		wTemplate = new Button(wTemplateGroup, SWT.CHECK);
		props.setLook(wTemplate);
		fdTemplate = new FormData();
		fdTemplate.left = new FormAttachment(middle, 0);
		fdTemplate.top = new FormAttachment(0, margin);
		fdTemplate.right = new FormAttachment(100, 0);
		wTemplate.setLayoutData(fdTemplate);
		wTemplate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				enableTemplate();
			}
		});
		wTemplate.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Template.Tooltip"));

		// TemplateFilename line
		wlTemplateFilename = new Label(wTemplateGroup, SWT.RIGHT);
		wlTemplateFilename.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.TemplateFilename.Label"));
		props.setLook(wlTemplateFilename);
		fdlTemplateFilename = new FormData();
		fdlTemplateFilename.left = new FormAttachment(0, 0);
		fdlTemplateFilename.top = new FormAttachment(wTemplate, margin);
		fdlTemplateFilename.right = new FormAttachment(middle, -margin);
		wlTemplateFilename.setLayoutData(fdlTemplateFilename);

		wbTemplateFilename = new Button(wTemplateGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbTemplateFilename);
		wbTemplateFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbTemplateFilename = new FormData();
		fdbTemplateFilename.right = new FormAttachment(100, 0);
		fdbTemplateFilename.top = new FormAttachment(wTemplate, 0);
		wbTemplateFilename.setLayoutData(fdbTemplateFilename);

		wTemplateFilename = new TextVar(transMeta, wTemplateGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTemplateFilename);
		wTemplateFilename.addModifyListener(lsMod);
		fdTemplateFilename = new FormData();
		fdTemplateFilename.left = new FormAttachment(middle, 0);
		fdTemplateFilename.top = new FormAttachment(wTemplate, margin);
		fdTemplateFilename.right = new FormAttachment(wbTemplateFilename, -margin);
		wTemplateFilename.setLayoutData(fdTemplateFilename);

		// Use template sheet
		wlTemplateSheet = new Label(wTemplateGroup, SWT.RIGHT);
		wlTemplateSheet.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.TemplateSheet.Label"));
		props.setLook(wlTemplateSheet);
		FormData fdlTemplateSheet = new FormData();
		fdlTemplateSheet.left = new FormAttachment(0, 0);
		fdlTemplateSheet.top = new FormAttachment(wTemplateFilename, margin);
		fdlTemplateSheet.right = new FormAttachment(middle, -margin);
		wlTemplateSheet.setLayoutData(fdlTemplateSheet);
		wTemplateSheet = new Button(wTemplateGroup, SWT.CHECK);
		wTemplateSheet.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.TemplateSheet.Tooltip"));
		
		props.setLook(wTemplateSheet);
		FormData fdTemplateSheet = new FormData();
		fdTemplateSheet.left = new FormAttachment(middle, 0);
		fdTemplateSheet.top = new FormAttachment(wTemplateFilename, margin);
		fdTemplateSheet.right = new FormAttachment(100, 0);
		wTemplateSheet.setLayoutData(fdTemplateSheet);
		wTemplateSheet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				enableTemplateSheet();
			}
		});

		// TemplateSheetname line
		wlTemplateSheetname = new Label(wTemplateGroup, SWT.RIGHT);
		wlTemplateSheetname.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.TemplateSheetname.Label"));
		props.setLook(wlTemplateSheetname);
		FormData fdlTemplateSheetname = new FormData();
		fdlTemplateSheetname.left = new FormAttachment(0, 0);
		fdlTemplateSheetname.top = new FormAttachment(wTemplateSheet, margin);
		fdlTemplateSheetname.right = new FormAttachment(middle, -margin);
		wlTemplateSheetname.setLayoutData(fdlTemplateSheetname);

		wTemplateSheetname = new TextVar(transMeta, wTemplateGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTemplateSheetname);
		wTemplateSheetname.addModifyListener(lsMod);
		FormData fdTemplateSheetname = new FormData();
		fdTemplateSheetname.left = new FormAttachment(middle, 0);
		fdTemplateSheetname.top = new FormAttachment(wTemplateSheet, margin);
		fdTemplateSheetname.right = new FormAttachment(wbTemplateFilename, -margin);
		wTemplateSheetname.setLayoutData(fdTemplateSheetname);

		fdTemplateGroup = new FormData();
		fdTemplateGroup.left = new FormAttachment(0, margin);
		fdTemplateGroup.top = new FormAttachment(sheetGroup, margin);
		fdTemplateGroup.right = new FormAttachment(100, -margin);
		wTemplateGroup.setLayoutData(fdTemplateGroup);

		// ///////////////////////////////////////////////////////////
		// / END OF Write to existing Group GROUP
		// ///////////////////////////////////////////////////////////		

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
		wContentTab.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ContentTab.TabTitle"));

		FormLayout contentLayout = new FormLayout();
		contentLayout.marginWidth = 3;
		contentLayout.marginHeight = 3;

		Composite wContentComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		Group wContentGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wContentGroup);
		wContentGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ContentGroup.Label"));

		FormLayout ContentGroupgroupLayout = new FormLayout();
		ContentGroupgroupLayout.marginWidth = 10;
		ContentGroupgroupLayout.marginHeight = 10;
		wContentGroup.setLayout(ContentGroupgroupLayout);

		// starting cell
		wlStartingCell = new Label(wContentGroup, SWT.RIGHT);
		wlStartingCell.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.StartingCell.Label"));
		props.setLook(wlStartingCell);
		FormData fdlStartingCell = new FormData();
		fdlStartingCell.left = new FormAttachment(0, 0);
		fdlStartingCell.top = new FormAttachment(wIfSheetExists, margin);
		fdlStartingCell.right = new FormAttachment(middle, -margin);
		wlStartingCell.setLayoutData(fdlStartingCell);
		wStartingCell = new TextVar(transMeta, wContentGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStartingCell.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.StartingCell.Tooltip"));
		props.setLook(wStartingCell);
		wStartingCell.addModifyListener(lsMod);
		FormData fdStartingCell = new FormData();
		fdStartingCell.left = new FormAttachment(middle, 0);
		fdStartingCell.top = new FormAttachment(wIfSheetExists, margin);
		fdStartingCell.right = new FormAttachment(100, 0);
		wStartingCell.setLayoutData(fdStartingCell);

		// row writing method line
		wlRowWritingMethod = new Label(wContentGroup, SWT.RIGHT);
		wlRowWritingMethod.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.RowWritingMethod.Label"));
		props.setLook(wlRowWritingMethod);
		FormData fdlRowWritingMethod = new FormData();
		fdlRowWritingMethod.left = new FormAttachment(0, 0);
		fdlRowWritingMethod.top = new FormAttachment(wStartingCell, margin);
		fdlRowWritingMethod.right = new FormAttachment(middle, -margin);
		wlRowWritingMethod.setLayoutData(fdlRowWritingMethod);
		wRowWritingMethod = new CCombo(wContentGroup, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);

		String overwriteLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.RowWritingMethod.Overwrite.Label");
		String pushDownLabel = BaseMessages.getString(PKG, "ExcelWriterDialog.RowWritingMethod.PushDown.Label");
		wRowWritingMethod.setItems(new String[] { overwriteLabel, pushDownLabel });
		wRowWritingMethod.setData(overwriteLabel, ExcelWriterStepMeta.ROW_WRITE_OVERWRITE);
		wRowWritingMethod.setData(pushDownLabel, ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN);
		wRowWritingMethod.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.RowWritingMethod.Tooltip"));

		props.setLook(wRowWritingMethod);
		wRowWritingMethod.addModifyListener(lsMod);

		//		wRowWritingMethod.addSelectionListener(new SelectionAdapter() {
		//			public void widgetSelected(SelectionEvent e) {
		//				input.setChanged();
		//				EnableRowWritingMethod();
		//			}
		//		});

		FormData fdRowWritingMethod = new FormData();
		fdRowWritingMethod.left = new FormAttachment(middle, 0);
		fdRowWritingMethod.top = new FormAttachment(wStartingCell, margin);
		fdRowWritingMethod.right = new FormAttachment(100, 0);
		wRowWritingMethod.setLayoutData(fdRowWritingMethod);

		wlHeader = new Label(wContentGroup, SWT.RIGHT);
		wlHeader.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Header.Label"));
		props.setLook(wlHeader);
		fdlHeader = new FormData();
		fdlHeader.left = new FormAttachment(0, 0);
		fdlHeader.top = new FormAttachment(wRowWritingMethod, margin);
		fdlHeader.right = new FormAttachment(middle, -margin);
		wlHeader.setLayoutData(fdlHeader);
		wHeader = new Button(wContentGroup, SWT.CHECK);
		props.setLook(wHeader);
		fdHeader = new FormData();
		fdHeader.left = new FormAttachment(middle, 0);
		fdHeader.top = new FormAttachment(wRowWritingMethod, margin);
		fdHeader.right = new FormAttachment(100, 0);
		wHeader.setLayoutData(fdHeader);
		wHeader.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Header.Tooltip"));
		wHeader.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
				enableHeader();
			}
		});

		wlFooter = new Label(wContentGroup, SWT.RIGHT);
		wlFooter.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.Footer.Label"));
		props.setLook(wlFooter);
		fdlFooter = new FormData();
		fdlFooter.left = new FormAttachment(0, 0);
		fdlFooter.top = new FormAttachment(wHeader, margin);
		fdlFooter.right = new FormAttachment(middle, -margin);
		wlFooter.setLayoutData(fdlFooter);
		wFooter = new Button(wContentGroup, SWT.CHECK);
		props.setLook(wFooter);
		fdFooter = new FormData();
		fdFooter.left = new FormAttachment(middle, 0);
		fdFooter.top = new FormAttachment(wHeader, margin);
		fdFooter.right = new FormAttachment(100, 0);
		wFooter.setLayoutData(fdFooter);
		wFooter.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.Footer.Tooltip"));
		wFooter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});

		// auto size columns?
		wlAutoSize = new Label(wContentGroup, SWT.RIGHT);
		wlAutoSize.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AutoSize.Label"));
		props.setLook(wlAutoSize);
		fdlAutoSize = new FormData();
		fdlAutoSize.left = new FormAttachment(0, 0);
		fdlAutoSize.top = new FormAttachment(wFooter, margin);
		fdlAutoSize.right = new FormAttachment(middle, -margin);
		wlAutoSize.setLayoutData(fdlAutoSize);
		wAutoSize = new Button(wContentGroup, SWT.CHECK);
		props.setLook(wAutoSize);
		wAutoSize.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.AutoSize.Tooltip"));
		fdAutoSize = new FormData();
		fdAutoSize.left = new FormAttachment(middle, 0);
		fdAutoSize.top = new FormAttachment(wFooter, margin);
		fdAutoSize.right = new FormAttachment(100, 0);
		wAutoSize.setLayoutData(fdAutoSize);
		wAutoSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});
		
		// force formula recalculation?
		wlForceFormulaRecalculation = new Label(wContentGroup, SWT.RIGHT);
		wlForceFormulaRecalculation.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.ForceFormulaRecalculation.Label"));
		props.setLook(wlForceFormulaRecalculation);
		fdlForceFormulaRecalculation = new FormData();
		fdlForceFormulaRecalculation.left = new FormAttachment(0, 0);
		fdlForceFormulaRecalculation.top = new FormAttachment(wAutoSize, margin);
		fdlForceFormulaRecalculation.right = new FormAttachment(middle, -margin);
		wlForceFormulaRecalculation.setLayoutData(fdlForceFormulaRecalculation);
		wForceFormulaRecalculation = new Button(wContentGroup, SWT.CHECK);
		props.setLook(wForceFormulaRecalculation);
		wForceFormulaRecalculation.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.ForceFormulaRecalculation.Tooltip"));
		fdForceFormulaRecalculation = new FormData();
		fdForceFormulaRecalculation.left = new FormAttachment(middle, 0);
		fdForceFormulaRecalculation.top = new FormAttachment(wAutoSize, margin);
		fdForceFormulaRecalculation.right = new FormAttachment(100, 0);
		wForceFormulaRecalculation.setLayoutData(fdForceFormulaRecalculation);
		wForceFormulaRecalculation.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});		
		
		// leave existing styles alone? 
		wlLeaveExistingStylesUnchanged = new Label(wContentGroup, SWT.RIGHT);
		wlLeaveExistingStylesUnchanged.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.LeaveExistingStylesUnchanged.Label"));
		props.setLook(wlLeaveExistingStylesUnchanged);
		fdlLeaveExistingStylesUnchanged = new FormData();
		fdlLeaveExistingStylesUnchanged.left = new FormAttachment(0, 0);
		fdlLeaveExistingStylesUnchanged.top = new FormAttachment(wForceFormulaRecalculation, margin);
		fdlLeaveExistingStylesUnchanged.right = new FormAttachment(middle, -margin);
		wlLeaveExistingStylesUnchanged.setLayoutData(fdlLeaveExistingStylesUnchanged);
		wLeaveExistingStylesUnchanged = new Button(wContentGroup, SWT.CHECK);
		props.setLook(wLeaveExistingStylesUnchanged);
		wLeaveExistingStylesUnchanged.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.LeaveExistingStylesUnchanged.Tooltip"));
		fdLeaveExistingStylesUnchanged = new FormData();
		fdLeaveExistingStylesUnchanged.left = new FormAttachment(middle, 0);
		fdLeaveExistingStylesUnchanged.top = new FormAttachment(wForceFormulaRecalculation, margin);
		fdLeaveExistingStylesUnchanged.right = new FormAttachment(100, 0);
		wLeaveExistingStylesUnchanged.setLayoutData(fdLeaveExistingStylesUnchanged);
		wLeaveExistingStylesUnchanged.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});				
		
		FormData fdContentGroup = new FormData();
		fdContentGroup.left = new FormAttachment(0, margin);
		fdContentGroup.top = new FormAttachment(0, margin);
		fdContentGroup.right = new FormAttachment(100, -margin);
		wContentGroup.setLayoutData(fdContentGroup);

		/// END OF CONTENT GROUP

		Group writeToExistingGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(writeToExistingGroup);
		writeToExistingGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.writeToExistingGroup.Label"));

		FormLayout writeToExistingGroupgroupLayout = new FormLayout();
		writeToExistingGroupgroupLayout.marginWidth = 10;
		writeToExistingGroupgroupLayout.marginHeight = 10;
		writeToExistingGroup.setLayout(writeToExistingGroupgroupLayout);

		// Use AppendLines
		Label wlAppendLines = new Label(writeToExistingGroup, SWT.RIGHT);
		wlAppendLines.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.AppendLines.Label"));
		props.setLook(wlAppendLines);
		FormData fdlAppendLines = new FormData();
		fdlAppendLines.left = new FormAttachment(0, 0);
		fdlAppendLines.top = new FormAttachment(0, margin);
		fdlAppendLines.right = new FormAttachment(middle, -margin);
		wlAppendLines.setLayoutData(fdlAppendLines);
		wAppendLines = new Button(writeToExistingGroup, SWT.CHECK);
		wAppendLines.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.AppendLines.Tooltip"));
		props.setLook(wAppendLines);
		FormData fdAppendLines = new FormData();
		fdAppendLines.left = new FormAttachment(middle, 0);
		fdAppendLines.top = new FormAttachment(0, margin);
		fdAppendLines.right = new FormAttachment(100, 0);
		wAppendLines.setLayoutData(fdAppendLines);
		//wAppendLines.addSelectionListener(lsMod);
		wAppendLines.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				input.setChanged();
				enableAppend();
			}
		});

		// SkipRows line
		Label wlSkipRows = new Label(writeToExistingGroup, SWT.RIGHT);
		wlSkipRows.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.SkipRows.Label"));
		props.setLook(wlSkipRows);
		FormData fdlSkipRows = new FormData();
		fdlSkipRows.left = new FormAttachment(0, 0);
		fdlSkipRows.top = new FormAttachment(wAppendLines, margin);
		fdlSkipRows.right = new FormAttachment(middle, -margin);
		wlSkipRows.setLayoutData(fdlSkipRows);

		wSkipRows = new Text(writeToExistingGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSkipRows.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.SkipRows.Tooltip"));
		props.setLook(wSkipRows);
		wSkipRows.addModifyListener(lsMod);
		FormData fdSkipRows = new FormData();
		fdSkipRows.left = new FormAttachment(middle, 0);
		fdSkipRows.top = new FormAttachment(wAppendLines, margin);
		fdSkipRows.right = new FormAttachment(100, 0);
		wSkipRows.setLayoutData(fdSkipRows);

		// EmptyRows line
		Label wlEmptyRows = new Label(writeToExistingGroup, SWT.RIGHT);
		wlEmptyRows.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.EmptyRows.Label"));
		props.setLook(wlEmptyRows);
		FormData fdlEmptyRows = new FormData();
		fdlEmptyRows.left = new FormAttachment(0, 0);
		fdlEmptyRows.top = new FormAttachment(wSkipRows, margin);
		fdlEmptyRows.right = new FormAttachment(middle, -margin);
		wlEmptyRows.setLayoutData(fdlEmptyRows);

		wEmptyRows = new Text(writeToExistingGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wEmptyRows.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.EmptyRows.Tooltip"));
		props.setLook(wEmptyRows);
		wEmptyRows.addModifyListener(lsMod);
		FormData fdEmptyRows = new FormData();
		fdEmptyRows.left = new FormAttachment(middle, 0);
		fdEmptyRows.top = new FormAttachment(wSkipRows, margin);
		fdEmptyRows.right = new FormAttachment(100, 0);
		wEmptyRows.setLayoutData(fdEmptyRows);

		// Use AppendLines
		Label wlOmitHeader = new Label(writeToExistingGroup, SWT.RIGHT);
		wlOmitHeader.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.OmitHeader.Label"));
		props.setLook(wlOmitHeader);
		FormData fdlOmitHeader = new FormData();
		fdlOmitHeader.left = new FormAttachment(0, 0);
		fdlOmitHeader.top = new FormAttachment(wEmptyRows, margin);
		fdlOmitHeader.right = new FormAttachment(middle, -margin);
		wlOmitHeader.setLayoutData(fdlOmitHeader);
		wOmitHeader = new Button(writeToExistingGroup, SWT.CHECK);
		wOmitHeader.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.OmitHeader.Tooltip"));
		props.setLook(wOmitHeader);
		FormData fdOmitHeader = new FormData();
		fdOmitHeader.left = new FormAttachment(middle, 0);
		fdOmitHeader.top = new FormAttachment(wEmptyRows, margin);
		fdOmitHeader.right = new FormAttachment(100, 0);
		wOmitHeader.setLayoutData(fdOmitHeader);
		wOmitHeader.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				input.setChanged();
			}
		});	

		FormData fdWriteToExistingGroup = new FormData();
		fdWriteToExistingGroup.left = new FormAttachment(0, margin);
		fdWriteToExistingGroup.top = new FormAttachment(wContentGroup, margin);
		fdWriteToExistingGroup.right = new FormAttachment(100, -margin);
		writeToExistingGroup.setLayoutData(fdWriteToExistingGroup);

		// ///////////////////////////////////////////////////////////
		// / END OF Write to existing Group GROUP
		// ///////////////////////////////////////////////////////////		

		Group fieldGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(fieldGroup);
		fieldGroup.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.fieldGroup.Label"));

		FormLayout fieldGroupgroupLayout = new FormLayout();
		fieldGroupgroupLayout.marginWidth = 10;
		fieldGroupgroupLayout.marginHeight = 10;
		fieldGroup.setLayout(fieldGroupgroupLayout);

		wGet = new Button(fieldGroup, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wGet.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.GetFields"));

		wMinWidth = new Button(fieldGroup, SWT.PUSH);
		wMinWidth.setText(BaseMessages.getString(PKG, "ExcelWriterDialog.MinWidth.Button"));
		wMinWidth.setToolTipText(BaseMessages.getString(PKG, "ExcelWriterDialog.MinWidth.Tooltip"));

		setButtonPositions(new Button[] { wGet, wMinWidth }, margin, null);

		final int FieldsRows = input.getOutputFields().length;

		// Prepare a list of possible formats, filtering reserved internal formats away
		String[] formats = BuiltinFormats.getAll();

		List<String> allFormats = Arrays.asList(BuiltinFormats.getAll());
		List<String> nonReservedFormats = new ArrayList<String>(allFormats.size());

		for (String format : allFormats) {
			if (!format.startsWith("reserved")) {
				nonReservedFormats.add(format);
			}
		}
		
		Collections.sort(nonReservedFormats);
		formats = nonReservedFormats.toArray(new String[0]);

		colinf = new ColumnInfo[] { new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.NameColumn.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.TypeColumn.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.FormatColumn.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, formats),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.UseStyleCell.Column"), ColumnInfo.COLUMN_TYPE_TEXT),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.TitleColumn.Column"), ColumnInfo.COLUMN_TYPE_TEXT),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.UseTitleStyleCell.Column"), ColumnInfo.COLUMN_TYPE_TEXT),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.FormulaField.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "N", "Y" }, true),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.HyperLinkField.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.CommentField.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(BaseMessages.getString(PKG, "ExcelWriterDialog.CommentAuthor.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false) };

		wFields = new TableView(transMeta, fieldGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);
		
		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);
		wFields.addModifyListener(lsMod);
		// Search the fields in the background

		final Runnable runnable = new Runnable() {
			public void run() {
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta != null) {
					try {
						RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

						// Remember these fields...
						for (int i = 0; i < row.size(); i++) {
							inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
						}
						setComboBoxes();
					} catch (KettleException e) {
						logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
					}
				}
			}
		};
		new Thread(runnable).start();

		FormData fdFieldGroup = new FormData();
		fdFieldGroup.left = new FormAttachment(0, margin);
		fdFieldGroup.top = new FormAttachment(writeToExistingGroup, margin);
		fdFieldGroup.bottom = new FormAttachment(100, 0);
		fdFieldGroup.right = new FormAttachment(100, -margin);
		fieldGroup.setLayoutData(fdFieldGroup);

		fdContentComp = new FormData();
		fdContentComp.left = new FormAttachment(0, 0);
		fdContentComp.top = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom = new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsMinWidth = new Listener() {
			public void handleEvent(Event e) {
				setMinimalWidth();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wMinWidth.addListener(SWT.Selection, lsMinWidth);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wFilename.addSelectionListener(lsDef);
		wTemplateFilename.addSelectionListener(lsDef);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText())+"\n\n"+BaseMessages.getString(PKG, "ExcelWriterDialog.Filename.Tooltip"));
			}
		});
		wTemplateFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wTemplateFilename.setToolTipText(transMeta.environmentSubstitute(wTemplateFilename.getText()));
			}
		});
		
		wSheetname.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wSheetname.setToolTipText(transMeta.environmentSubstitute(wSheetname.getText())+"\n\n"+BaseMessages.getString(PKG, "ExcelWriterDialog.Sheetname.Tooltip"));
			}
		});
		
		wTemplateSheetname.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wTemplateSheetname.setToolTipText(transMeta.environmentSubstitute(wTemplateSheetname.getText()));
			}
		});

		wStartingCell.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wStartingCell.setToolTipText(transMeta.environmentSubstitute(wStartingCell.getText())+"\n\n"+BaseMessages.getString(PKG, "ExcelWriterDialog.StartingCell.Tooltip"));
			}
		});

		wPassword.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wPassword.setToolTipText(transMeta.environmentSubstitute(wPassword.getText())+"\n\n"+BaseMessages.getString(PKG, "ExcelWriterDialog.Password.Tooltip"));
			}
		});
		
		wProtectedBy.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				wProtectedBy.setToolTipText(transMeta.environmentSubstitute(wProtectedBy.getText())+"\n\n"+BaseMessages.getString(PKG, "ExcelWriterDialog.ProtectedBy.Tooltip"));
			}
		});

		wbFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.xls", "*.xlsx", "*.*" });
				if (wFilename.getText() != null) {
					dialog.setFileName(transMeta.environmentSubstitute(wFilename.getText()));
				}
				dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLS.Label"), BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLSX.Label"),
						BaseMessages.getString(PKG, "System.FileType.AllFiles") });
				if (dialog.open() != null) {
					wFilename.setText(dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName());
				}
			}
		});

		wbTemplateFilename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.xls", "*.xlsx", "*.*" });
				if (wTemplateFilename.getText() != null) {
					dialog.setFileName(transMeta.environmentSubstitute(wTemplateFilename.getText()));
				}
				dialog.setFilterNames(new String[] { BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLS.Label"), BaseMessages.getString(PKG, "ExcelWriterDialog.FormatXLSX.Label"),
						BaseMessages.getString(PKG, "System.FileType.AllFiles") });
				if (dialog.open() != null) {
					wTemplateFilename.setText(dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName());
				}
			}
		});

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize(shell, 600, 600, true);

		getData();
		setDateTimeFormat();
		enableExtension();
		enableAppend();
		enableHeader();
		enableTemplateSheet();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	private void enableAppend() {
		wSplitEvery.setEnabled(!wAppendLines.getSelection());
	}

	private void enableHeader() {
		wOmitHeader.setEnabled(wHeader.getSelection());
	}
	
	
	private void setDateTimeFormat() {
		if (wSpecifyFormat.getSelection()) {
			wAddDate.setSelection(false);
			wAddTime.setSelection(false);
		}

		wDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wlDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wlAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wAddTime.setEnabled(!wSpecifyFormat.getSelection());
		wlAddTime.setEnabled(!wSpecifyFormat.getSelection());

	}

	protected void setComboBoxes() {
		// Something was changed in the row.
		//
		final Map<String, Integer> fields = new HashMap<String, Integer>();

		// Add the currentMeta fields...
		fields.putAll(inputFields);

		Set<String> keySet = fields.keySet();
		List<String> entries = new ArrayList<String>(keySet);

		String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

		Const.sortStrings(fieldNames);
		colinf[0].setComboValues(fieldNames);
		colinf[7].setComboValues(fieldNames);
		colinf[8].setComboValues(fieldNames);
		colinf[9].setComboValues(fieldNames);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		if (input.getFileName() != null)
			wFilename.setText(input.getFileName());
		wDoNotOpenNewFileInit.setSelection(input.isDoNotOpenNewFileInit());
		if (input.getExtension() != null) {

			if (input.getExtension().equals("xlsx")) {
				wExtension.select(1);
			} else {
				wExtension.select(0);
			}

		}

		wSplitEvery.setText("" + input.getSplitEvery());
		wEmptyRows.setText("" + input.getAppendEmpty());
		wSkipRows.setText("" + input.getAppendOffset());
		wAppendLines.setSelection(input.isAppendLines());
		wHeader.setSelection(input.isHeaderEnabled());
		wFooter.setSelection(input.isFooterEnabled());
		wOmitHeader.setSelection(input.isAppendOmitHeader());
		wForceFormulaRecalculation.setSelection(input.isForceFormulaRecalculation());
		wLeaveExistingStylesUnchanged.setSelection(input.isLeaveExistingStylesUnchanged());

		if (input.getStartingCell() != null){
			wStartingCell.setText(input.getStartingCell());	
		}
		
		wAddDate.setSelection(input.isDateInFilename());
		wAddTime.setSelection(input.isTimeInFilename());

		if (input.getDateTimeFormat() != null)
			wDateTimeFormat.setText(input.getDateTimeFormat());
		wSpecifyFormat.setSelection(input.isSpecifyFormat());

		wAddToResult.setSelection(input.isAddToResultFiles());
		wAutoSize.setSelection(input.isAutoSizeColums());
		wIfFileExists.select(ExcelWriterStepMeta.IF_FILE_EXISTS_REUSE.equals(input.getIfFileExists())?1:0);
		wIfSheetExists.select(ExcelWriterStepMeta.IF_SHEET_EXISTS_REUSE.equals(input.getIfSheetExists())?1:0);
		wRowWritingMethod.select(ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN.equals(input.getRowWritingMethod())?1:0);

		wAddStepnr.setSelection(input.isStepNrInFilename());
		wMakeActiveSheet.setSelection(input.isMakeSheetActive());
		wTemplate.setSelection(input.isTemplateEnabled());
		wTemplateSheet.setSelection(input.isTemplateSheetEnabled());
		
		if (input.getTemplateFileName() != null)
			wTemplateFilename.setText(input.getTemplateFileName());
		
		if (input.getTemplateSheetName() != null)
			wTemplateSheetname.setText(input.getTemplateSheetName());
		
		if (input.getSheetname() != null) {
			wSheetname.setText(input.getSheetname());
		} else {
			wSheetname.setText("Sheet1");
		}
		wProtectSheet.setSelection(input.isSheetProtected());

		enablePassword();
		enableTemplate();

		if (input.getPassword() != null)
			wPassword.setText(input.getPassword());
		if (input.getProtectedBy() != null){
			wProtectedBy.setText(input.getProtectedBy());
		}

		logDebug("getting fields info...");

		for (int i = 0; i < input.getOutputFields().length; i++) {
			ExcelWriterStepField field = input.getOutputFields()[i];

			TableItem item = wFields.table.getItem(i);
			if (field.getName() != null)
				item.setText(1, field.getName());
			item.setText(2, field.getTypeDesc());

			if (field.getFormat() != null)
				item.setText(3, field.getFormat());
			if (field.getStyleCell() != null)
				item.setText(4, field.getStyleCell());
			if (field.getTitle() != null)
				item.setText(5, field.getTitle());
			if (field.getTitleStyleCell() != null)
				item.setText(6, field.getTitleStyleCell());
			if (field.isFormula()) {
				item.setText(7, "Y");
			} else {
				item.setText(7, "N");
			}

			if (field.getHyperlinkField() != null)
				item.setText(8, field.getHyperlinkField());
			if (field.getCommentField() != null)
				item.setText(9, field.getCommentField());
			if (field.getCommentAuthorField() != null)
				item.setText(10, field.getCommentAuthorField());

		}

		wFields.optWidth(true);
		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;

		input.setChanged(backupChanged);

		dispose();
	}

	private void getInfo(ExcelWriterStepMeta tfoi) {
		tfoi.setFileName(wFilename.getText());
		tfoi.setDoNotOpenNewFileInit(wDoNotOpenNewFileInit.getSelection());
		tfoi.setAppendOmitHeader(wOmitHeader.getSelection());
		tfoi.setExtension((String) wExtension.getData(wExtension.getText()));
		tfoi.setSplitEvery(Const.toInt(wSplitEvery.getText(), 0));
		tfoi.setAppendOffset(Const.toInt(wSkipRows.getText(), 0));
		tfoi.setAppendEmpty(Const.toInt(wEmptyRows.getText(), 0));
		tfoi.setAppendLines(wAppendLines.getSelection());
		tfoi.setHeaderEnabled(wHeader.getSelection());
		tfoi.setFooterEnabled(wFooter.getSelection());
		tfoi.setStartingCell(wStartingCell.getText());
		tfoi.setStepNrInFilename(wAddStepnr.getSelection());
		tfoi.setDateInFilename(wAddDate.getSelection());
		tfoi.setTimeInFilename(wAddTime.getSelection());
		tfoi.setIfFileExists((String)wIfFileExists.getData(wIfFileExists.getText()));
		tfoi.setIfSheetExists((String)wIfSheetExists.getData(wIfSheetExists.getText()));
		tfoi.setRowWritingMethod((String)wRowWritingMethod.getData(wRowWritingMethod.getText()));
		tfoi.setForceFormulaRecalculation(wForceFormulaRecalculation.getSelection());
		tfoi.setLeaveExistingStylesUnchanged(wLeaveExistingStylesUnchanged.getSelection());
		
		tfoi.setDateTimeFormat(wDateTimeFormat.getText());
		tfoi.setSpecifyFormat(wSpecifyFormat.getSelection());
		tfoi.setAutoSizeColums(wAutoSize.getSelection());

		tfoi.setAddToResultFiles(wAddToResult.getSelection());
		
		tfoi.setMakeSheetActive(wMakeActiveSheet.getSelection());
		tfoi.setProtectSheet(wProtectSheet.getSelection());
		tfoi.setProtectedBy(wProtectedBy.getText());
		tfoi.setPassword(wPassword.getText());

		tfoi.setTemplateEnabled(wTemplate.getSelection());
		tfoi.setTemplateSheetEnabled(wTemplateSheet.getSelection());
		tfoi.setTemplateFileName(wTemplateFilename.getText());
		tfoi.setTemplateSheetName(wTemplateSheetname.getText());
		
		if (wSheetname.getText() != null) {
			tfoi.setSheetname(wSheetname.getText());
		} else {
			tfoi.setSheetname("Sheet 1");
		}

		int nrfields = wFields.nrNonEmpty();

		tfoi.allocate(nrfields);

		for (int i = 0; i < nrfields; i++) {
			ExcelWriterStepField field = new ExcelWriterStepField();

			TableItem item = wFields.getNonEmpty(i);
			field.setName(item.getText(1));
			field.setType(item.getText(2));
			field.setFormat(item.getText(3));
			field.setStyleCell(item.getText(4));
			field.setTitle(item.getText(5));
			field.setTitleStyleCell(item.getText(6));
			field.setFormula(item.getText(7).equalsIgnoreCase("Y"));
			field.setHyperlinkField(item.getText(8));
			field.setCommentField(item.getText(9));
			field.setCommentAuthorField(item.getText(10));

			tfoi.getOutputFields()[i] = field;
		}
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))
			return;

		stepname = wStepname.getText(); // return value

		getInfo(input);

		dispose();
	}

	private void enablePassword() {
		wPassword.setEnabled(wProtectSheet.getSelection());
		wProtectedBy.setEnabled(wProtectSheet.getSelection());
	}

	private void enableTemplate() {
		wbTemplateFilename.setEnabled(wTemplate.getSelection());
		wTemplateFilename.setEnabled(wTemplate.getSelection());
	}

	private void enableTemplateSheet() {
		wTemplateSheetname.setEnabled(wTemplateSheet.getSelection());
	}

	private void enableExtension() {
		wProtectSheet.setEnabled(wExtension.getSelectionIndex() == 0);
		if (wExtension.getSelectionIndex() == 0) {
			wPassword.setEnabled(wProtectSheet.getSelection());
			wProtectedBy.setEnabled(wProtectSheet.getSelection());
		} else {
			wPassword.setEnabled(false);
			wProtectedBy.setEnabled(false);
		}
	}

	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null) {
				TableItemInsertListener listener = new TableItemInsertListener() {
					public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
						if (v.isNumber()) {
							if (v.getLength() > 0) {
								int le = v.getLength();
								int pr = v.getPrecision();

								if (v.getPrecision() <= 0) {
									pr = 0;
								}

								String mask = "";
								for (int m = 0; m < le - pr; m++) {
									mask += "0";
								}
								if (pr > 0)
									mask += ".";
								for (int m = 0; m < pr; m++) {
									mask += "0";
								}
								tableItem.setText(3, mask);
							}
						}
						return true;
					}
				};
				BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1, 5 }, new int[] { 2 }, 0, 0, listener);
			}
		} catch (KettleException ke) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
		}

	}

	/**
	 * Sets the output width to minimal width...
	 * 
	 */
	public void setMinimalWidth() {
		int nrNonEmptyFields = wFields.nrNonEmpty();
		for (int i = 0; i < nrNonEmptyFields; i++) {
			TableItem item = wFields.getNonEmpty(i);

			int type = ValueMeta.getType(item.getText(2));
			switch (type) {
			case ValueMetaInterface.TYPE_STRING:
				item.setText(3, "");
				break;
			case ValueMetaInterface.TYPE_INTEGER:
				item.setText(3, "0");
				break;
			case ValueMetaInterface.TYPE_NUMBER:
				item.setText(3, "0.#####");
				break;
			case ValueMetaInterface.TYPE_DATE:
				break;
			default:
				break;
			}
		}
		wFields.optWidth(true);
	}
}
