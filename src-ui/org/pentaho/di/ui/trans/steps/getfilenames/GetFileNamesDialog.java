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
/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/

/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.getfilenames;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.trans.steps.getfilenames.GetFileNamesMeta;
import org.pentaho.di.trans.steps.getfilenames.Messages;

public class GetFileNamesDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder wTabFolder;

	private FormData fdTabFolder;

	private CTabItem wFileTab, wFilterTab;

	private Composite wFileComp, wFilterComp;

	private FormData fdFileComp, fdFilterComp;

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

	private Label wlFilterFileType;

	private CCombo wFilterFileType;

	private FormData fdlFilterFileType, fdFilterFileType;

	private GetFileNamesMeta input;

	private int middle, margin;

	private ModifyListener lsMod;

	public GetFileNamesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (GetFileNamesMeta) in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		lsMod = new ModifyListener()
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
		shell.setText(Messages.getString("GetFileNamesDialog.DialogTitle"));

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
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
		wFileTab.setText(Messages.getString("GetFileNamesDialog.FileTab.TabTitle"));

		wFileComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename = new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText(Messages.getString("GetFileNamesDialog.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(0, 0);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbbFilename);
		wbbFilename.setText(Messages.getString("System.Button.Browse"));
		wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbaFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("GetFileNamesDialog.FilenameAdd.Button"));
		wbaFilename.setToolTipText(Messages.getString("GetFileNamesDialog.FilenameAdd.Tooltip"));
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
		wlFilemask.setText(Messages.getString("GetFileNamesDialog.Filemask.Label"));
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
		fdFilemask.right = new FormAttachment(wFilename, 0, SWT.RIGHT);
		wFilemask.setLayoutData(fdFilemask);

		// Filename list line
		wlFilenameList = new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText(Messages.getString("GetFileNamesDialog.FilenameList.Label"));
		props.setLook(wlFilenameList);
		fdlFilenameList = new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right = new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("GetFileNamesDialog.FilenameDelete.Button"));
		wbdFilename.setToolTipText(Messages.getString("GetFileNamesDialog.FilenameDelete.Tooltip"));
		fdbdFilename = new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top = new FormAttachment(wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("GetFileNamesDialog.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("GetFileNamesDialog.FilenameEdit.Tooltip"));
		fdbeFilename = new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.left = new FormAttachment(wbdFilename, 0, SWT.LEFT);
		fdbeFilename.top = new FormAttachment(wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles = new Button(wFileComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbShowFiles);
		wbShowFiles.setText(Messages.getString("GetFileNamesDialog.ShowFiles.Button"));
		fdbShowFiles = new FormData();
		fdbShowFiles.left = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo = new ColumnInfo[] {
				new ColumnInfo(Messages.getString("GetFileNamesDialog.FileDirColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(Messages.getString("GetFileNamesDialog.WildcardColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false), };

		colinfo[1].setToolTip(Messages.getString("GetFileNamesDialog.RegExpColumn.Column"));

		wFilenameList = new TableView(transMeta, wFileComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo,
				colinfo.length, lsMod, props);
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

		fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// ////////////////////////
		// START OF Filter TAB ///
		// ////////////////////////
		wFilterTab = new CTabItem(wTabFolder, SWT.NONE);
		wFilterTab.setText(Messages.getString("GetFileNamesDialog.FilterTab.TabTitle"));

		wFilterComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFilterComp);

		FormLayout filesettingLayout = new FormLayout();
		filesettingLayout.marginWidth = 3;
		filesettingLayout.marginHeight = 3;
		wFilterComp.setLayout(fileLayout);

		// Filter File Type
		wlFilterFileType = new Label(wFilterComp, SWT.RIGHT);
		wlFilterFileType.setText(Messages.getString("GetFileNamesDialog.FilterTab.FileType.Label"));
		props.setLook(wlFilterFileType);
		fdlFilterFileType = new FormData();
		fdlFilterFileType.left = new FormAttachment(0, 0);
		fdlFilterFileType.right = new FormAttachment(middle, 0);
		fdlFilterFileType.top = new FormAttachment(0, margin);
		wlFilterFileType.setLayoutData(fdlFilterFileType);
		wFilterFileType = new CCombo(wFilterComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wFilterFileType.add(Messages.getString("GetFileNamesDialog.FilterTab.FileType.All.Label"));
		wFilterFileType.add(Messages.getString("GetFileNamesDialog.FilterTab.FileType.OnlyFile.Label"));
		wFilterFileType.add(Messages.getString("GetFileNamesDialog.FilterTab.FileType.OnlyFolder.Label"));
		// wFilterFileType.select(0); // +1: starts at -1

		props.setLook(wFilterFileType);
		fdFilterFileType = new FormData();
		fdFilterFileType.left = new FormAttachment(middle, 0);
		fdFilterFileType.top = new FormAttachment(0, 0);
		fdFilterFileType.right = new FormAttachment(100, 0);
		wFilterFileType.setLayoutData(fdFilterFileType);

		fdFilterComp = new FormData();
		fdFilterComp.left = new FormAttachment(0, 0);
		fdFilterComp.top = new FormAttachment(0, 0);
		fdFilterComp.right = new FormAttachment(100, 0);
		fdFilterComp.bottom = new FormAttachment(100, 0);
		wFilterComp.setLayoutData(fdFilterComp);

		wFilterComp.layout();
		wFilterTab.setControl(wFilterComp);

		// ///////////////////////////////////////////////////////////
		// / END OF FILE Filter TAB
		// ///////////////////////////////////////////////////////////

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(Messages.getString("GetFileNamesDialog.Preview.Button"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel , wPreview }, margin, wTabFolder);

		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
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

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() });
				wFilename.setText("");
				wFilemask.setText("");
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
				GetFileNamesMeta tfii = new GetFileNamesMeta();
				getInfo(tfii);
				String files[] = tfii.getFilePaths(transMeta);
				if (files != null && files.length > 0)
				{
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, files, "Files read",
							"Files read:");
					esd.setViewOnly();
					esd.open();
				} else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					mb.setMessage(Messages.getString("GetFileNamesDialog.NoFilesFound.DialogMessage"));
					mb.setText(Messages.getString("System.Dialog.Error.Title"));
					mb.open();
				}
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
					dialog.setFilterExtensions(new String[] { "*.txt;*.csv", "*.csv", "*.txt", "*" });
					if (wFilename.getText() != null)
					{
						String fname = transMeta.environmentSubstitute(wFilename.getText());
						dialog.setFileName(fname);
					}

					dialog.setFilterNames(new String[] {
							Messages.getString("GetFileNamesDialog.FileType.TextAndCSVFiles"),
							Messages.getString("System.FileType.CSVFiles"),
							Messages.getString("System.FileType.TextFiles"),
							Messages.getString("System.FileType.AllFiles") });

					if (dialog.open() != null)
					{
						String str = dialog.getFilterPath() + System.getProperty("file.separator")
								+ dialog.getFileName();
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
		getData(input);

		setSize();

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this
	 * dialog.
	 * 
	 * @param meta
	 *            The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(GetFileNamesMeta meta)
	{
		final GetFileNamesMeta in = meta;

		if (in.getFileName() != null)
		{
			wFilenameList.removeAll();
			for (int i = 0; i < in.getFileName().length; i++)
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i],
						in.getFileRequired()[i] });
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);

			if (in.getFilterFileType() != null)
			{

				if (in.getFilterFileType().equals("only_files"))
				{
					wFilterFileType.select(1);
				} else if (in.getFilterFileType().equals("only_folders"))
				{
					wFilterFileType.select(2);
				} else
				{
					wFilterFileType.select(0);
				}

			} else
			{
				wFilterFileType.select(0);

			}

		}
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

		getInfo(input);
		dispose();
	}

	private void getInfo(GetFileNamesMeta in)
	{
		stepname = wStepname.getText(); // return value

		int nrfiles = wFilenameList.getItemCount();
		in.allocate(nrfiles);

		in.setFileName(wFilenameList.getItems(0));
		in.setFileMask(wFilenameList.getItems(1));
		in.setFileRequired(wFilenameList.getItems(2));

		in.setFilterFileType(wFilterFileType.getSelectionIndex());

	}

	// Preview the data
	private void preview()
	{
		// Create the XML input step
		GetFileNamesMeta oneMeta = new GetFileNamesMeta();
		getInfo(oneMeta);

		TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname
				.getText());

		EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, Messages.getString("GetFileNamesDialog.PreviewSize.DialogTitle"), Messages.getString("GetFileNamesDialog.PreviewSize.DialogMessage"));
		int previewSize = numberDialog.open();
		if (previewSize > 0)
		{
			TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta,
					new String[] { wStepname.getText() }, new int[] { previewSize });
			progressDialog.open();

			if (!progressDialog.isCancelled())
			{
				Trans trans = progressDialog.getTrans();
				String loggingText = progressDialog.getLoggingText();

				if (trans.getResult() != null && trans.getResult().getNrErrors() > 0)
				{
					EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("GetFileNamesDialog.ErrorInPreview.DialogMessage"), loggingText, true);
					etd.setReadOnly();
					etd.open();
				}
				
				
				PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),progressDialog.getPreviewRowsMeta(wStepname.getText()),
						progressDialog.getPreviewRows(wStepname.getText()), loggingText);
				prd.open();
			}
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
