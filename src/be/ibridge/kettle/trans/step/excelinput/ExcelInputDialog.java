 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
/*
 * Created on 18-mei-2003
 *
 */

package be.ibridge.kettle.trans.step.excelinput;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;

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

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterListDialog;
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.dialog.TransPreviewProgressDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.textfileinput.TextFileInputMeta;


public class ExcelInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wSheetTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wSheetComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdSheetComp, fdContentComp, fdFieldsComp;

	private Label        wlFilename;
	private Button       wbbFilename; // Browse: add file or directory
	private Button       wbvFilename; // Variable
	private Button       wbdFilename; // Delete
	private Button       wbeFilename; // Edit
	private Button       wbaFilename; // Add or change
	private Text         wFilename;
	private FormData     fdlFilename, fdbFilename, fdbvFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

	private Label        wlFilenameList;
	private TableView    wFilenameList;
	private FormData     fdlFilenameList, fdFilenameList;

	private Label        wlFilemask;
	private Text         wFilemask;
	private FormData     fdlFilemask, fdFilemask;

	private Button       wbShowFiles;
	private FormData     fdbShowFiles;

	private Label        wlSheetnameList;
	private TableView    wSheetnameList;
	private FormData     fdlSheetnameList;

	private Button       wbGetSheets;
	private FormData     fdbGetSheets;

	private Label        wlHeader;
	private Button       wHeader;
	private FormData     fdlHeader, fdHeader;
	
	private Label        wlNoempty;
	private Button       wNoempty;
	private FormData     fdlNoempty, fdNoempty;

	private Label        wlStoponempty;
	private Button       wStoponempty;
	private FormData     fdlStoponempty, fdStoponempty;

	private Label        wlInclFilenameField;
	private Text         wInclFilenameField;
	private FormData     fdlInclFilenameField, fdInclFilenameField;

	private Label        wlInclSheetnameField;
	private Text         wInclSheetnameField;
	private FormData     fdlInclSheetnameField, fdInclSheetnameField;

	private Label        wlInclRownumField;
	private Text         wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

	private Button       wbGetFields;

	private TableView    wFields;
	private FormData     fdFields;

	private ExcelInputMeta input;
	
	private static final String STRING_PREVIEW_ROWS    = "  &Preview rows   "; 
	
	public ExcelInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ExcelInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed         = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Excel input");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF FILE TAB   ///
		//////////////////////////
		wFileTab=new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText("Files");
		
		wFileComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);

		// Filename line
		wlFilename=new Label(wFileComp, SWT.RIGHT);
		wlFilename.setText("File or directory ");
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbbFilename);
		wbbFilename.setText("&Browse");
		wbbFilename.setToolTipText("Browse for a file or directory & add to the list");
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(0, 0);
		wbbFilename.setLayoutData(fdbFilename);

		wbvFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbvFilename);
		wbvFilename.setText("&Variable");
		wbvFilename.setToolTipText("Insert a variable in the filename or directory");
		fdbvFilename=new FormData();
		fdbvFilename.right= new FormAttachment(wbbFilename, -margin);
		fdbvFilename.top  = new FormAttachment(0, 0);
		wbvFilename.setLayoutData(fdbvFilename);

		wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbaFilename);
		wbaFilename.setText("&Add");
		wbaFilename.setToolTipText("Add this entry to the list of files & directories.");
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbvFilename, -margin);
		fdbaFilename.top  = new FormAttachment(0, 0);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbaFilename, -margin);
		fdFilename.top  = new FormAttachment(0, 0);
		wFilename.setLayoutData(fdFilename);

		wlFilemask=new Label(wFileComp, SWT.RIGHT);
		wlFilemask.setText("Regular Expression ");
 		props.setLook(wlFilemask);
		fdlFilemask=new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top  = new FormAttachment(wFilename, margin);
		fdlFilemask.right= new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilemask);
		wFilemask.addModifyListener(lsMod);
		fdFilemask=new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top  = new FormAttachment(wFilename, margin);
		fdFilemask.right= new FormAttachment(100, 0);
		wFilemask.setLayoutData(fdFilemask);

		// Filename list line
		wlFilenameList=new Label(wFileComp, SWT.RIGHT);
		wlFilenameList.setText("Selected files: ");
 		props.setLook(wlFilenameList);
		fdlFilenameList=new FormData();
		fdlFilenameList.left = new FormAttachment(0, 0);
		fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
		fdlFilenameList.right= new FormAttachment(middle, -margin);
		wlFilenameList.setLayoutData(fdlFilenameList);

		// Buttons to the right of the screen...
		wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdFilename);
		wbdFilename.setText("&Delete");
		wbdFilename.setToolTipText("Delete the selected entries from the list of files.");
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbeFilename);
		wbeFilename.setText("&Edit");
		wbeFilename.setToolTipText("Edit the selected file and remove from the list.");
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbShowFiles);
		wbShowFiles.setText("&Show filename(s)...");
		fdbShowFiles=new FormData();
		fdbShowFiles.left   = new FormAttachment(middle, 0);
		fdbShowFiles.bottom = new FormAttachment(100, -margin);
		wbShowFiles.setLayoutData(fdbShowFiles);

		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo("File/Directory",  ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinfo[ 1]=new ColumnInfo("Wildcard",        ColumnInfo.COLUMN_TYPE_TEXT,    false );
		
		colinfo[ 1].setToolTip("Enter a regular expression here and a directory in the first column.");
		
		
		wFilenameList = new TableView(wFileComp, 
						      SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
						      colinfo, 
						      input.getFileName().length,  
						      lsMod,
							  props
						      );
 		props.setLook(wFilenameList);
		fdFilenameList=new FormData();
		fdFilenameList.left   = new FormAttachment(middle, 0);
		fdFilenameList.right  = new FormAttachment(wbdFilename, -margin);
		fdFilenameList.top    = new FormAttachment(wFilemask, margin);
		fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
		wFilenameList.setLayoutData(fdFilenameList);

	
		fdFileComp=new FormData();
		fdFileComp.left  = new FormAttachment(0, 0);
		fdFileComp.top   = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom= new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);
	
		wFileComp.layout();
		wFileTab.setControl(wFileComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF FILE TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF SHEET TAB  ///
		//////////////////////////
		wSheetTab=new CTabItem(wTabFolder, SWT.NONE);
		wSheetTab.setText("Sheets");
		
		wSheetComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wSheetComp);

		FormLayout sheetLayout = new FormLayout();
		sheetLayout.marginWidth  = 3;
		sheetLayout.marginHeight = 3;
		wSheetComp.setLayout(sheetLayout);
		
		wbGetSheets=new Button(wSheetComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbGetSheets);
		wbGetSheets.setText("&Get sheetname(s)...");
		fdbGetSheets=new FormData();
		fdbGetSheets.left   = new FormAttachment(middle, 0);
		fdbGetSheets.bottom = new FormAttachment(100, -margin);
		wbGetSheets.setLayoutData(fdbGetSheets);

		wlSheetnameList=new Label(wSheetComp, SWT.RIGHT);
		wlSheetnameList.setText("List of sheets to read ");
 		props.setLook(wlSheetnameList);
		fdlSheetnameList=new FormData();
		fdlSheetnameList.left = new FormAttachment(0, 0);
		fdlSheetnameList.top  = new FormAttachment(wFilename, margin);
		fdlSheetnameList.right= new FormAttachment(middle, -margin);
		wlSheetnameList.setLayoutData(fdlSheetnameList);
		
		ColumnInfo[] shinfo=new ColumnInfo[3];
		shinfo[ 0]=new ColumnInfo("Sheet name",     ColumnInfo.COLUMN_TYPE_TEXT,    false);
		shinfo[ 1]=new ColumnInfo("Start row",      ColumnInfo.COLUMN_TYPE_TEXT,    false );
		shinfo[ 2]=new ColumnInfo("Start column",   ColumnInfo.COLUMN_TYPE_TEXT,    false );
		
		wSheetnameList = new TableView(wSheetComp, 
						      SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER, 
						      shinfo, 
						      input.getSheetName().length,  
						      lsMod,
							  props
						      );
 		props.setLook(wSheetnameList);
		fdFilenameList=new FormData();
		fdFilenameList.left   = new FormAttachment(middle, 0);
		fdFilenameList.right  = new FormAttachment(100, 0);
		fdFilenameList.top    = new FormAttachment(0, 0);
		fdFilenameList.bottom = new FormAttachment(wbGetSheets, -margin);
		wSheetnameList.setLayoutData(fdFilenameList);
		
		fdSheetComp=new FormData();
		fdSheetComp.left  = new FormAttachment(0, 0);
		fdSheetComp.top   = new FormAttachment(0, 0);
		fdSheetComp.right = new FormAttachment(100, 0);
		fdSheetComp.bottom= new FormAttachment(100, 0);
		wSheetComp.setLayoutData(fdSheetComp);
	
		wSheetComp.layout();
		wSheetTab.setControl(wSheetComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF SHEET TAB
		/////////////////////////////////////////////////////////////

		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText("Content");

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		// Header checkbox
		wlHeader=new Label(wContentComp, SWT.RIGHT);
		wlHeader.setText("Header ");
 		props.setLook(wlHeader);
		fdlHeader=new FormData();
		fdlHeader.left = new FormAttachment(0, 0);
		fdlHeader.top  = new FormAttachment(0, 0);
		fdlHeader.right= new FormAttachment(middle, -margin);
		wlHeader.setLayoutData(fdlHeader);
		wHeader=new Button(wContentComp, SWT.CHECK);
 		props.setLook(wHeader);
		fdHeader=new FormData();
		fdHeader.left = new FormAttachment(middle, 0);
		fdHeader.top  = new FormAttachment(0, 0);
		fdHeader.right= new FormAttachment(100, 0);
		wHeader.setLayoutData(fdHeader);
		wHeader.addSelectionListener(new SelectionAdapter() 
	        {
				public void widgetSelected(SelectionEvent arg0)
				{
					enableFields();
				}
			});

		wlNoempty=new Label(wContentComp, SWT.RIGHT);
		wlNoempty.setText("No empty rows ");
 		props.setLook(wlNoempty);
		fdlNoempty=new FormData();
		fdlNoempty.left = new FormAttachment(0, 0);
		fdlNoempty.top  = new FormAttachment(wHeader, margin);
		fdlNoempty.right= new FormAttachment(middle, -margin);
		wlNoempty.setLayoutData(fdlNoempty);
		wNoempty=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wNoempty);
		wNoempty.setToolTipText("Check this to remove empty lines from the output rows.");
		fdNoempty=new FormData();
		fdNoempty.left = new FormAttachment(middle, 0);
		fdNoempty.top  = new FormAttachment(wHeader, margin);
		fdNoempty.right= new FormAttachment(100, 0);
		wNoempty.setLayoutData(fdNoempty);

		wlStoponempty=new Label(wContentComp, SWT.RIGHT);
		wlStoponempty.setText("Stop on empty row ");
 		props.setLook(wlStoponempty);
		fdlStoponempty=new FormData();
		fdlStoponempty.left = new FormAttachment(0, 0);
		fdlStoponempty.top  = new FormAttachment(wNoempty, margin);
		fdlStoponempty.right= new FormAttachment(middle, -margin);
		wlStoponempty.setLayoutData(fdlStoponempty);
		wStoponempty=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wStoponempty);
		wStoponempty.setToolTipText("Stop processing when you reach an empty row.");
		fdStoponempty=new FormData();
		fdStoponempty.left = new FormAttachment(middle, 0);
		fdStoponempty.top  = new FormAttachment(wNoempty, margin);
		fdStoponempty.right= new FormAttachment(100, 0);
		wStoponempty.setLayoutData(fdStoponempty);

		wlInclFilenameField=new Label(wContentComp, SWT.RIGHT);
		wlInclFilenameField.setText("Filename field ");
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left  = new FormAttachment(0, 0);
		fdlInclFilenameField.top   = new FormAttachment(wStoponempty, margin);
		fdlInclFilenameField.right = new FormAttachment(middle, -margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(middle, 0);
		fdInclFilenameField.top  = new FormAttachment(wStoponempty, margin);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclSheetnameField=new Label(wContentComp, SWT.RIGHT);
		wlInclSheetnameField.setText("Sheetname field ");
 		props.setLook(wlInclSheetnameField);
		fdlInclSheetnameField=new FormData();
		fdlInclSheetnameField.left  = new FormAttachment(0, 0);
		fdlInclSheetnameField.top   = new FormAttachment(wInclFilenameField, margin);
		fdlInclSheetnameField.right = new FormAttachment(middle, -margin);
		wlInclSheetnameField.setLayoutData(fdlInclSheetnameField);
		wInclSheetnameField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclSheetnameField);
		wInclSheetnameField.addModifyListener(lsMod);
		fdInclSheetnameField=new FormData();
		fdInclSheetnameField.left = new FormAttachment(middle, 0);
		fdInclSheetnameField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclSheetnameField.right= new FormAttachment(100, 0);
		wInclSheetnameField.setLayoutData(fdInclSheetnameField);
		
		wlInclRownumField=new Label(wContentComp, SWT.RIGHT);
		wlInclRownumField.setText("Row number field ");
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left  = new FormAttachment(0, 0);
		fdlInclRownumField.top   = new FormAttachment(wInclSheetnameField, margin);
		fdlInclRownumField.right = new FormAttachment(middle, -margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(middle, 0);
		fdInclRownumField.top  = new FormAttachment(wInclSheetnameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);

		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText("Limit ");
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wInclRownumField, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wInclRownumField, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
		
		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF CONTENT TAB
		/////////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText("Fields");
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);

		wbGetFields=new Button(wFieldsComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbGetFields);
		wbGetFields.setText("&Get fields from header row...");
		
		setButtonPositions(new Button[] { wbGetFields }, margin, null);

		enableFields();

		final int FieldsCols=6;
		final int FieldsRows=input.getFieldName().length;
		int FieldsWidth =600;
		int FieldsHeight=150;
		
		// Prepare a list of possible formats...
		String dats[] = Const.dateFormats;
		String nums[] = Const.numberFormats;
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[ 0]=new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinf[ 1]=new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO,  Value.getTypes() );
		colinf[ 2]=new ColumnInfo("Length",     ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinf[ 3]=new ColumnInfo("Precision",  ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinf[ 4]=new ColumnInfo("Trim type",  ColumnInfo.COLUMN_TYPE_CCOMBO,  TextFileInputMeta.trimTypeDesc );
		colinf[ 5]=new ColumnInfo("Repeat",     ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] { "Y", "N" } );
		
		colinf[ 5].setToolTip("set this field to Y if you want to repeat values when the next are empty");
		
		wFields=new TableView(wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );
		wFields.setSize(FieldsWidth,FieldsHeight);

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wbGetFields, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
 		props.setLook(wFieldsComp);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(STRING_PREVIEW_ROWS);
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		// wFilename.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		wInclFilenameField.addSelectionListener( lsDef );
		wInclSheetnameField.addSelectionListener( lsDef );

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() } );
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
				if (idx>=0)
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
					ExcelInputMeta eii = new ExcelInputMeta();
					getInfo(eii);
					String files[] = eii.getFiles();
					if (files!=null && files.length>0)
					{
						EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, files, "Files read", "Files read:");
						esd.setViewOnly();
						esd.open();
					}
					else
					{
						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
						mb.setMessage("No files found!  Please check the filename/directory and regular expression options.");
						mb.setText("ERROR");
						mb.open(); 
					}
				}
			}
		);

		// Whenever something changes, set the tooltip to the expanded version of the filename:
		wFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFilename.setToolTipText(Const.replEnv( wFilename.getText() ) );
				}
			}
		);
		
		// Listen to the Variable... button
		wbvFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					Properties sp = System.getProperties();
					Enumeration keys = sp.keys();
					int size = sp.values().size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
					int i=0;
					while (keys.hasMoreElements())
					{
						key[i] = (String)keys.nextElement();
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
						i++;
					}
					
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, str, "Select an Environment Variable", "Select an Environment Variable");
					if (esd.open()!=null)
					{
						int nr = esd.getSelectionNr();
						wFilename.insert("%%"+key[nr]+"%%");
						wFilename.setToolTipText(Const.replEnv( wFilename.getText() ) );
					}
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
					if (wFilemask.getText()!=null && wFilemask.getText().length()>0) // A mask: a directory!
					{
						DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wFilename.getText()!=null)
						{
							String fpath = Const.replEnv(wFilename.getText());
							dialog.setFilterPath( fpath );
						}
						
						if (dialog.open()!=null)
						{
							String str= dialog.getFilterPath();
							wFilename.setText(str);
						}
					}
					else
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*.xls;*.XLS", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = Const.replEnv(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {"Excel 95, 97 or 2000 files", "All files"});
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			}
		);
		
		// Get a list of the sheetnames.
		wbGetSheets.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				getSheets();
			}
		});
		
		wbGetFields.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				getFields();
			}
		});
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		input.setChanged(changed);
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	public void enableFields()
	{
		wbGetFields.setEnabled( wHeader.getSelection());
	}
	
	/**
	 * Read the data from the ExcelInputMeta object and show it in this dialog.
	 * 
	 * @param in The ExcelInputMeta object to obtain the data from.
	 */
	public void getData(ExcelInputMeta in)
	{
		if (in.getFileName() !=null) 
		{
			wFilenameList.removeAll();
			for (int i=0;i<in.getFileName().length;i++) 
			{
				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i] } );
			}
			wFilenameList.removeEmptyRows();
			wFilenameList.setRowNums();
			wFilenameList.optWidth(true);
		}
		wHeader.setSelection(in.startsWithHeader());
		wNoempty.setSelection(in.ignoreEmptyRows());
		wStoponempty.setSelection(in.stopOnEmpty());
		if (in.getFileField()!=null) wInclFilenameField.setText(in.getFileField());
		if (in.getSheetField()!=null) wInclSheetnameField.setText(in.getSheetField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		wLimit.setText(""+in.getRowLimit());
		
		log.logDebug(toString(), "getting fields info...");
		for (int i=0;i<in.getFieldName().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			String field    = in.getFieldName()[i];
			String type     = Value.getTypeDesc(in.getFieldType()[i]);
			String length   = ""+in.getFieldLength()[i];
			String prec     = ""+in.getFieldPrecision()[i];
			String trim     = TextFileInputMeta.getTrimTypeDesc(in.getFieldTrimType()[i]);
			String rep      = in.getFieldRepeat()[i]?"Y":"N";
			
			if (field   !=null) item.setText( 1, field);
			if (type    !=null) item.setText( 2, type    );
			if (length  !=null) item.setText( 3, length  );
			if (prec    !=null) item.setText( 4, prec    );
			if (trim    !=null) item.setText( 5, trim    );
			if (rep     !=null) item.setText( 6, rep     );
		}
		
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);

		log.logDebug(toString(), "getting sheets info...");
		for (int i=0;i<in.getSheetName().length;i++)
		{
			TableItem item = wSheetnameList.table.getItem(i);
			String sheetname    =    in.getSheetName()[i];
			String startrow     = ""+in.getStartRow()[i];
			String startcol     = ""+in.getStartColumn()[i];
			
			if (sheetname!=null) item.setText( 1, sheetname);
			if (startrow!=null)  item.setText( 2, startrow);
			if (startcol!=null)  item.setText( 3, startcol);
		}
		wSheetnameList.removeEmptyRows();
		wSheetnameList.setRowNums();
		wSheetnameList.optWidth(true);

		enableFields();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		getInfo(input);
		dispose();
	}
	
	private void getInfo(ExcelInputMeta in)
	{
		stepname = wStepname.getText(); // return value

		// copy info to Meta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0) );
		in.setFileField( wInclFilenameField.getText() );
		in.setSheetField( wInclSheetnameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
		
		in.setStartsWithHeader( wHeader.getSelection() );
		in.setIgnoreEmptyRows( wNoempty.getSelection() );
		in.setStopOnEmpty( wStoponempty.getSelection() );

		int nrfiles    = wFilenameList.nrNonEmpty();
		int nrsheets   = wSheetnameList.nrNonEmpty();
		int nrfields   = wFields.nrNonEmpty();
		
		in.allocate(nrfiles, nrsheets, nrfields);

		for (int i=0;i<nrfiles;i++)
		{
			TableItem item = wFilenameList.getNonEmpty(i);
			in.getFileName()[i] = item.getText(1);
			in.getFileMask()[i] = item.getText(2);
		}

		for (int i=0;i<nrsheets;i++)
		{
			TableItem item = wSheetnameList.getNonEmpty(i);
			in.getSheetName()[i] = item.getText(1);
			in.getStartRow()[i]  = Const.toInt(item.getText(2),0);
			in.getStartColumn()[i]  = Const.toInt(item.getText(3),0);
		}

		for (int i=0;i<nrfields;i++)
		{
			TableItem item  = wFields.getNonEmpty(i);
			in.getFieldName()[i]     = item.getText(1);
			in.getFieldType()[i]     = Value.getType(item.getText(2));
			String slength  = item.getText(3);
			String sprec    = item.getText(4);
			in.getFieldTrimType()[i]  = TextFileInputMeta.getTrimType(item.getText(5));
			in.getFieldRepeat()[i]    = "Y".equalsIgnoreCase(item.getText(6));		

			in.getFieldLength()[i]    = Const.toInt(slength, -1);
			in.getFieldPrecision()[i] = Const.toInt(sprec, -1);
		}	
		
	}
	
	/**
	 * Preview the data generated by this step.
	 * This generates a transformation using this step & a dummy and previews it.
	 *
	 */
	private void preview()
	{
		// Create the excel reader step...
		ExcelInputMeta eii = new ExcelInputMeta();
		getInfo(eii);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(eii, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props, 500, "Enter preview size", "Enter the number of rows you would like to preview:");
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();
            
            if (!progressDialog.isCancelled())
            {
                Trans trans = progressDialog.getTrans();
                String loggingText = progressDialog.getLoggingText();
                
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                    mb.setMessage("One or more errors occured during preview!  Examine the logfile to see what went wrong.");
                    mb.setText("ERROR");
                    mb.open(); 
                }
                
                PreviewRowsDialog prd =new PreviewRowsDialog(shell, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
                prd.open();
            }
        }
	}
		
	
	/**
	 * Get the names of the sheets from the Excel workbooks and let the user select some or all of them.
	 *
	 */
	public void getSheets()
	{
		ArrayList sheetnames = new ArrayList();
		
		ExcelInputMeta info = new ExcelInputMeta();
		getInfo(info);

		String files[] = info.getFiles();
		
		for (int i=0;i<files.length;i++)
		{
			try
			{
				File file = new File(files[i]);
				Workbook workbook = Workbook.getWorkbook(file);
				
				int nrSheets = workbook.getNumberOfSheets();
				for (int j=0;j<nrSheets;j++)
				{
					Sheet sheet = workbook.getSheet(j);
					String sheetname = sheet.getName();
					
					if (Const.indexOfString(sheetname, sheetnames)<0) sheetnames.add(sheetname);
				}
				
				workbook.close();
			}
			catch(Exception e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("I was unable to read the Excel file ["+files[i]+"]."+Const.CR+"  Please check the files, directories & expression.");
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		
		// Put it in an array:
		String lst[] = (String[])sheetnames.toArray(new String[sheetnames.size()]);
		
		// Let the user select the sheet-names...
		EnterListDialog esd = new EnterListDialog(shell, SWT.NONE, lst);
		String selection[] = esd.open();
		if (selection!=null)
		{
			for (int j=0;j<selection.length;j++)
			{
				wSheetnameList.add(new String[] { selection[j], "" } );
			}
			wSheetnameList.removeEmptyRows();
			wSheetnameList.setRowNums();
			wSheetnameList.optWidth(true);
		}


	}

	/**
	 * Get the list of fields in the Excel workbook and put the result in the fields table view.
	 *
	 */
	public void getFields()
	{
		Row fields = new Row();
		
		ExcelInputMeta info = new ExcelInputMeta();
		getInfo(info);

		String files[] = info.getFiles();
		
		for (int i=0;i<files.length;i++)
		{
			try
			{
				File file = new File(files[i]);
				Workbook workbook = Workbook.getWorkbook(file);
				
				int nrSheets = workbook.getNumberOfSheets();
				for (int j=0;j<nrSheets;j++)
				{
					Sheet sheet = workbook.getSheet(j);
					
					// See if it's a selected sheet:
					int sheetIndex = Const.indexOfString(sheet.getName(), info.getSheetName()); 
					if (sheetIndex>=0)
					{
						// We suppose it's the complete range we're looking for...
						int rownr=info.getStartRow()[sheetIndex];
						int startcol = info.getStartColumn()[sheetIndex];
						
						boolean stop=false;
						for (int colnr=startcol;colnr<256 && !stop;colnr++)
						{
							// System.out.println("Checking out (colnr, rownr) : ("+colnr+", "+rownr+")");
							
							try
							{
								String fieldname = null;
								int    fieldtype = Value.VALUE_TYPE_NONE;
		
								Cell cell = sheet.getCell(colnr, rownr);
								if (!cell.getType().equals( CellType.EMPTY ))
								{
									// We found a field.
									fieldname = cell.getContents();
								}
								
                                // System.out.println("Fieldname = "+fieldname);
                                
								Cell below = sheet.getCell(colnr, rownr+1);
								if (below.getType().equals(CellType.BOOLEAN))
								{
									fieldtype = Value.VALUE_TYPE_BOOLEAN;
								}
								else
								if (below.getType().equals(CellType.DATE))
								{
									fieldtype = Value.VALUE_TYPE_DATE;
								}
								else
								if (below.getType().equals(CellType.LABEL))
								{
									fieldtype = Value.VALUE_TYPE_STRING;
								}
								else
								if (below.getType().equals(CellType.NUMBER))
								{
									fieldtype = Value.VALUE_TYPE_NUMBER;
								}
                                
                                if (fieldname!=null && fieldtype==Value.VALUE_TYPE_NONE)
                                {
                                    fieldtype = Value.VALUE_TYPE_STRING;
                                }
								
								if (fieldname!=null && fieldtype!=Value.VALUE_TYPE_NONE)
								{
									Value field = new Value(fieldname, fieldtype);
									if (fields.searchValueIndex(field.getName())<0) fields.addValue(field);
								}
								else
								{
									if (fieldname==null) stop=true;
								}
							}
							catch(ArrayIndexOutOfBoundsException aioobe)
							{
                                // System.out.println("index out of bounds at column "+colnr+" : "+aioobe.toString());
								stop=true;
							}
						}
					}
				}
				
				workbook.close();
			}
			catch(Exception e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("I was unable to read the Excel file ["+files[i]+"]."+Const.CR+"  Please check the files, directories & expression."+Const.CR+e.toString());
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		
		if (fields.size()>0)
		{
			for (int j=0;j<fields.size();j++)
			{
				Value field = fields.getValue(j);
				wFields.add(new String[] { field.getName(), field.getTypeDesc(), "-1", "-1", "none", "N" } );
			}
	
			wFields.removeEmptyRows();
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage("I was unable to find any fields in the Excel file(s).");
			mb.setText("No success");
			mb.open(); 
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}

}
