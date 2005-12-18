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

package be.ibridge.kettle.trans.step.textfileinput;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class TextFileInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab, wFieldsTab;

	private Composite    wFileComp, wContentComp, wFieldsComp;
	private FormData     fdFileComp, fdContentComp, fdFieldsComp;

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

    private Button       wFirst;
    private FormData     fdFirst;
    private Listener     lsFirst;

	private Label        wlFiletype;
	private CCombo       wFiletype;
	private FormData     fdlFiletype, fdFiletype;

	private Label        wlSeparator;
	private Button       wbSeparator;
	private Text         wSeparator;
	private FormData     fdlSeparator, fdbSeparator, fdSeparator;

	private Label        wlEnclosure;
	private Text         wEnclosure;
	private FormData     fdlEnclosure, fdEnclosure;

	private Label        wlHeader;
	private Button       wHeader;
	private FormData     fdlHeader, fdHeader;
	
	private Label        wlFooter;
	private Button       wFooter;
	private FormData     fdlFooter, fdFooter;
	
	private Label        wlZipped;
	private Button       wZipped;
	private FormData     fdlZipped, fdZipped;
	
	private Label        wlNoempty;
	private Button       wNoempty;
	private FormData     fdlNoempty, fdNoempty;

	private Label        wlInclFilename;
	private Button       wInclFilename;
	private FormData     fdlInclFilename, fdInclFilename;

	private Label        wlInclFilenameField;
	private Text         wInclFilenameField;
	private FormData     fdlInclFilenameField, fdInclFilenameField;

	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;

	private Label        wlInclRownumField;
	private Text         wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlFormat;
	private CCombo       wFormat;
	private FormData     fdlFormat, fdFormat;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

	private Label        wlFilter;
	private Button       wFilter;
	private FormData     fdlFilter, fdFilter;

	private Label        wlFilterPos;
	private Text         wFilterPos;
	private FormData     fdlFilterPos, fdFilterPos;

	private Label        wlFilterStr;
	private Text         wFilterStr;
	private FormData     fdlFilterStr, fdFilterStr;
	
	private TableView    wFields;
	private FormData     fdFields;

	private TextFileInputMeta input;

	private NumberFormat nf;
	private DecimalFormat df;
	private DecimalFormatSymbols dfs;
	private SimpleDateFormat daf;
	private DateFormatSymbols dafs;

	// Wizard info...
	private Vector fields;
	
	private PreviewRowsDialog previewdialog;
	private int               previewlimit;
	private Rectangle         previewbounds;
	private int               previewhscroll;
	private int               previewvscroll;
	
	private static final String STRING_PREVIEW_ROWS    = "  &Preview rows   "; 
	private static final String STRING_PREVIEW_REFRESH = " &Preview refresh "; 

	public static final int dateLengths[] = new int[]
		{
			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
		}
		;

	public TextFileInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(TextFileInputMeta)in;

		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();

        daf.setLenient(false); // Don't be too smart, only accept exact dates.

		previewdialog = null;
		previewlimit = -1;
		previewhscroll=-1;
		previewvscroll=-1;
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
		shell.setText("Text file input");
		
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
		wFileTab.setText("File");
		
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
		fdbShowFiles.bottom = new FormAttachment(100, 0);
		wbShowFiles.setLayoutData(fdbShowFiles);

		wFirst=new Button(wFileComp, SWT.PUSH);
		wFirst.setText(" View &file content ");
		fdFirst=new FormData();
		fdFirst.left=new FormAttachment(wbShowFiles, margin*2);
		fdFirst.bottom =new FormAttachment(100, 0);
		wFirst.setLayoutData(fdFirst);

		
		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo("File/Directory",  ColumnInfo.COLUMN_TYPE_TEXT,    "", false);
		colinfo[ 1]=new ColumnInfo("Wildcard",        ColumnInfo.COLUMN_TYPE_TEXT,    "", false );
		
		colinfo[ 1].setToolTip("Enter a regular expression here and a directory in the first column.");
		
		
		wFilenameList = new TableView(wFileComp, 
						      SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
						      colinfo, 
						      2,  
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

		// Filetype line
		wlFiletype=new Label(wContentComp, SWT.RIGHT);
		wlFiletype.setText("Filetype ");
 		props.setLook(wlFiletype);
		fdlFiletype=new FormData();
		fdlFiletype.left = new FormAttachment(0, 0);
		fdlFiletype.top  = new FormAttachment(0, 0);
		fdlFiletype.right= new FormAttachment(middle, -margin);
		wlFiletype.setLayoutData(fdlFiletype);
		wFiletype=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
		wFiletype.setText("Filetype");
 		props.setLook(wFiletype);
		wFiletype.add("CSV");
		wFiletype.add("Fixed");
		wFiletype.select(0);
		wFiletype.addModifyListener(lsMod);
		fdFiletype=new FormData();
		fdFiletype.left = new FormAttachment(middle, 0);
		fdFiletype.top  = new FormAttachment(0, 0);
		fdFiletype.right= new FormAttachment(100, 0);
		wFiletype.setLayoutData(fdFiletype);

		wlSeparator=new Label(wContentComp, SWT.RIGHT);
		wlSeparator.setText("Separator ");
 		props.setLook(wlSeparator);
		fdlSeparator=new FormData();
		fdlSeparator.left = new FormAttachment(0, 0);
		fdlSeparator.top  = new FormAttachment(wFiletype, margin);
		fdlSeparator.right= new FormAttachment(middle, -margin);
		wlSeparator.setLayoutData(fdlSeparator);

		wbSeparator=new Button(wContentComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSeparator);
		wbSeparator.setText("Insert &TAB");
		fdbSeparator=new FormData();
		fdbSeparator.right= new FormAttachment(100, 0);
		fdbSeparator.top  = new FormAttachment(wFiletype, 0);
		wbSeparator.setLayoutData(fdbSeparator);

		wSeparator=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSeparator);
		wSeparator.addModifyListener(lsMod);
		fdSeparator=new FormData();
		fdSeparator.left = new FormAttachment(middle, 0);
		fdSeparator.top  = new FormAttachment(wFiletype, margin);
		fdSeparator.right= new FormAttachment(wbSeparator, -margin);
		wSeparator.setLayoutData(fdSeparator);

		// Enclosure
		wlEnclosure=new Label(wContentComp, SWT.RIGHT);
		wlEnclosure.setText("Enclosure ");
 		props.setLook(wlEnclosure);
		fdlEnclosure=new FormData();
		fdlEnclosure.left = new FormAttachment(0, 0);
		fdlEnclosure.top  = new FormAttachment(wSeparator, margin);
		fdlEnclosure.right= new FormAttachment(middle, -margin);
		wlEnclosure.setLayoutData(fdlEnclosure);
		wEnclosure=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wEnclosure);
		wEnclosure.addModifyListener(lsMod);
		fdEnclosure=new FormData();
		fdEnclosure.left = new FormAttachment(middle, 0);
		fdEnclosure.top  = new FormAttachment(wSeparator, margin);
		fdEnclosure.right= new FormAttachment(100, 0);
		wEnclosure.setLayoutData(fdEnclosure);

		// Header checkbox
		wlHeader=new Label(wContentComp, SWT.RIGHT);
		wlHeader.setText("Header ");
 		props.setLook(wlHeader);
		fdlHeader=new FormData();
		fdlHeader.left = new FormAttachment(0, 0);
		fdlHeader.top  = new FormAttachment(wEnclosure, margin);
		fdlHeader.right= new FormAttachment(middle, -margin);
		wlHeader.setLayoutData(fdlHeader);
		wHeader=new Button(wContentComp, SWT.CHECK);
 		props.setLook(wHeader);
		fdHeader=new FormData();
		fdHeader.left = new FormAttachment(middle, 0);
		fdHeader.top  = new FormAttachment(wEnclosure, margin);
		fdHeader.right= new FormAttachment(100, 0);
		wHeader.setLayoutData(fdHeader);

		wlFooter=new Label(wContentComp, SWT.RIGHT);
		wlFooter.setText("Footer ");
 		props.setLook(wlFooter);
		fdlFooter=new FormData();
		fdlFooter.left = new FormAttachment(0, 0);
		fdlFooter.top  = new FormAttachment(wHeader, margin);
		fdlFooter.right= new FormAttachment(middle, -margin);
		wlFooter.setLayoutData(fdlFooter);
		wFooter=new Button(wContentComp, SWT.CHECK);
 		props.setLook(wFooter);
		fdFooter=new FormData();
		fdFooter.left = new FormAttachment(middle, 0);
		fdFooter.top  = new FormAttachment(wHeader, margin);
		fdFooter.right= new FormAttachment(100, 0);
		wFooter.setLayoutData(fdFooter);

		// Zipped?
		wlZipped=new Label(wContentComp, SWT.RIGHT);
		wlZipped.setText("Zipped ");
 		props.setLook(wlZipped);
		fdlZipped=new FormData();
		fdlZipped.left = new FormAttachment(0, 0);
		fdlZipped.top  = new FormAttachment(wFooter, margin);
		fdlZipped.right= new FormAttachment(middle, -margin);
		wlZipped.setLayoutData(fdlZipped);
		wZipped=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wZipped);
		wZipped.setToolTipText("Only the first entry in the archive is read!");
		fdZipped=new FormData();
		fdZipped.left = new FormAttachment(middle, 0);
		fdZipped.top  = new FormAttachment(wFooter, margin);
		fdZipped.right= new FormAttachment(100, 0);
		wZipped.setLayoutData(fdZipped);

		wlNoempty=new Label(wContentComp, SWT.RIGHT);
		wlNoempty.setText("No empty rows ");
 		props.setLook(wlNoempty);
		fdlNoempty=new FormData();
		fdlNoempty.left = new FormAttachment(0, 0);
		fdlNoempty.top  = new FormAttachment(wZipped, margin);
		fdlNoempty.right= new FormAttachment(middle, -margin);
		wlNoempty.setLayoutData(fdlNoempty);
		wNoempty=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wNoempty);
		wNoempty.setToolTipText("Check this to remove empty lines from the output rows.");
		fdNoempty=new FormData();
		fdNoempty.left = new FormAttachment(middle, 0);
		fdNoempty.top  = new FormAttachment(wZipped, margin);
		fdNoempty.right= new FormAttachment(100, 0);
		wNoempty.setLayoutData(fdNoempty);

		wlInclFilename=new Label(wContentComp, SWT.RIGHT);
		wlInclFilename.setText("Include filename in output? ");
 		props.setLook(wlInclFilename);
		fdlInclFilename=new FormData();
		fdlInclFilename.left = new FormAttachment(0, 0);
		fdlInclFilename.top  = new FormAttachment(wNoempty, margin);
		fdlInclFilename.right= new FormAttachment(middle, -margin);
		wlInclFilename.setLayoutData(fdlInclFilename);
		wInclFilename=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wInclFilename);
		wInclFilename.setToolTipText("Check this to add a field (String) containing the filename.");
		fdInclFilename=new FormData();
		fdInclFilename.left = new FormAttachment(middle, 0);
		fdInclFilename.top  = new FormAttachment(wNoempty, margin);
		wInclFilename.setLayoutData(fdInclFilename);

		wlInclFilenameField=new Label(wContentComp, SWT.LEFT);
		wlInclFilenameField.setText("Filename fieldname ");
 		props.setLook(wlInclFilenameField);
		fdlInclFilenameField=new FormData();
		fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
		fdlInclFilenameField.top  = new FormAttachment(wNoempty, margin);
		wlInclFilenameField.setLayoutData(fdlInclFilenameField);
		wInclFilenameField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclFilenameField);
		wInclFilenameField.addModifyListener(lsMod);
		fdInclFilenameField=new FormData();
		fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
		fdInclFilenameField.top  = new FormAttachment(wNoempty, margin);
		fdInclFilenameField.right= new FormAttachment(100, 0);
		wInclFilenameField.setLayoutData(fdInclFilenameField);

		wlInclRownum=new Label(wContentComp, SWT.RIGHT);
		wlInclRownum.setText("Rownum in output? ");
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText("Check this to add a field (String) containing the filename.");
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wContentComp, SWT.RIGHT);
		wlInclRownumField.setText("Rownum fieldname ");
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);

		wlFormat=new Label(wContentComp, SWT.RIGHT);
		wlFormat.setText("Format ");
 		props.setLook(wlFormat);
		fdlFormat=new FormData();
		fdlFormat.left = new FormAttachment(0, 0);
		fdlFormat.top  = new FormAttachment(wInclRownumField, margin);
		fdlFormat.right= new FormAttachment(middle, -margin);
		wlFormat.setLayoutData(fdlFormat);
		wFormat=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
		wFormat.setText("Format");
 		props.setLook(wFormat);
		wFormat.add("DOS");
		wFormat.add("Unix");
		wFormat.select(0);
		wFormat.addModifyListener(lsMod);
		fdFormat=new FormData();
		fdFormat.left = new FormAttachment(middle, 0);
		fdFormat.top  = new FormAttachment(wInclRownumField, margin);
		fdFormat.right= new FormAttachment(100, 0);
		wFormat.setLayoutData(fdFormat);

		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText("Limit ");
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wFormat, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wFormat, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		// Filter?
		wlFilter=new Label(wContentComp, SWT.RIGHT);
		wlFilter.setText("Filter ");
 		props.setLook(wlFilter);
		fdlFilter=new FormData();
		fdlFilter.left = new FormAttachment(0, 0);
		fdlFilter.top  = new FormAttachment(wLimit, margin);
		fdlFilter.right= new FormAttachment(middle, -margin);
		wlFilter.setLayoutData(fdlFilter);
		wFilter=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wFilter);
		wFilter.setToolTipText("Filter rows that have a value on a certain (character) position."+Const.CR+"The first position has number 0!");
		fdFilter=new FormData();
		fdFilter.left = new FormAttachment(middle, 0);
		fdFilter.top  = new FormAttachment(wLimit, margin);
		wFilter.setLayoutData(fdFilter);
		
		// Filter position...
		wlFilterPos=new Label(wContentComp, SWT.LEFT);
		wlFilterPos.setText("Pos ");
 		props.setLook(wlFilterPos);
		fdlFilterPos=new FormData();
		fdlFilterPos.left = new FormAttachment(wFilter, margin*2);
		fdlFilterPos.top  = new FormAttachment(wLimit, margin);
		wlFilterPos.setLayoutData(fdlFilterPos);
		wFilterPos=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilterPos);
		wFilterPos.addModifyListener(lsMod);
		fdFilterPos=new FormData();
		fdFilterPos.left = new FormAttachment(wlFilterPos, margin*2);
		fdFilterPos.top  = new FormAttachment(wLimit, margin);
		fdFilterPos.right= new FormAttachment(wlFilterPos, margin*2+50);
		wFilterPos.setLayoutData(fdFilterPos);

		// Filter position...
		wlFilterStr=new Label(wContentComp, SWT.LEFT);
		wlFilterStr.setText("Value ");
 		props.setLook(wlFilterStr);
		fdlFilterStr=new FormData();
		fdlFilterStr.left = new FormAttachment(wFilterPos, margin*2);
		fdlFilterStr.top  = new FormAttachment(wLimit, margin);
		wlFilterStr.setLayoutData(fdlFilterStr);
		wFilterStr=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilterStr);
		wFilterStr.addModifyListener(lsMod);
		fdFilterStr=new FormData();
		fdFilterStr.left = new FormAttachment(wlFilterStr, margin);
		fdFilterStr.top  = new FormAttachment(wLimit, margin);
		fdFilterStr.right= new FormAttachment(100, 0);
		wFilterStr.setLayoutData(fdFilterStr);
		
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
 		props.setLook(wFieldsComp);
		
		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(" &Get fields ");
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);

		final int FieldsRows=input.getInputFields().length;
		
		// Prepare a list of possible formats...
		String dats[] = Const.dateFormats;
		String nums[] = Const.numberFormats;
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO,  "", Value.getTypes(), true ),
			 new ColumnInfo("Format",     ColumnInfo.COLUMN_TYPE_CCOMBO,  "", formats),
			 new ColumnInfo("Position",   ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Length",     ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Precision",  ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Currency",   ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Decimal",    ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Group",      ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Null if",    ColumnInfo.COLUMN_TYPE_TEXT,    "", false),
			 new ColumnInfo("Trim type",  ColumnInfo.COLUMN_TYPE_CCOMBO,  "", TextFileInputMeta.trimTypeDesc, true ),
			 new ColumnInfo("Repeat",     ColumnInfo.COLUMN_TYPE_CCOMBO,  "", new String[] { "Y", "N" }, true )
            };
		
		colinf[11].setToolTip("set this field to Y if you want to repeat values when the next are empty");
		
		wFields=new TableView(wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
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
		lsFirst    = new Listener() { public void handleEvent(Event e) { first();   } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wFirst.addListener  (SWT.Selection, lsFirst  );
		wGet.addListener    (SWT.Selection, lsGet    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		// wFilename.addSelectionListener( lsDef );
		wSeparator.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		wInclFilenameField.addSelectionListener( lsDef );

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
					TextFileInputMeta tfii = new TextFileInputMeta();
					getInfo(tfii);
					String files[] = tfii.getFiles();
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
		// Allow the insertion of tabs as separator...
		wbSeparator.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent se) 
				{
					wSeparator.insert("\t");
				}
			}
		);
		// Enable/disable the right fields to allow a filename to be added to each row...
		wInclFilename.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeFilename();
				}
			}
		);
		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
				}
			}
		);
		// Enable/disable the right fields to allow a simple filter to be entered
		wFilter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setFilter();
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
						if (wZipped.getSelection())
						{
							dialog.setFilterExtensions(new String[] {"*.zip", "*.txt;*.csv", "*.csv", "*.txt", "*"});
						}
						else
						{
							dialog.setFilterExtensions(new String[] {"*.txt;*.csv", "*.csv", "*.txt", "*"});
						}
						if (wFilename.getText()!=null)
						{
							String fname = Const.replEnv(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						if (wZipped.getSelection())
						{
							dialog.setFilterNames(new String[] {"Zip archives", "Text and CSV files", "Comma Seperated Values", "Text files", "All files"});
						}
						else
						{
							dialog.setFilterNames(new String[] {"Text and CSV files", "Comma Seperated Values", "Text files", "All files"});
						}
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			}
		);
		
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
	
	public void setMultiple()
	{
		/*
		wlFilemask.setEnabled(wMultiple.getSelection());
		wFilemask.setEnabled(wMultiple.getSelection());
		wlFilename.setText(wMultiple.getSelection()?"Directory":"Filename ");
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

	public void setFilter()
	{
		wlFilterPos.setEnabled(wFilter.getSelection());
		wFilterPos.setEnabled(wFilter.getSelection());
		wlFilterStr.setEnabled(wFilter.getSelection());
		wFilterStr.setEnabled(wFilter.getSelection());
		
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(TextFileInputMeta in)
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
		if (in.getFileType() !=null) wFiletype.setText(in.getFileType());
		if (in.getSeparator()!=null) wSeparator.setText(in.getSeparator());
		if (in.getEnclosure()!=null) wEnclosure.setText(in.getEnclosure());
		wHeader.setSelection(in.hasHeader());
		wFooter.setSelection(in.hasFooter());
		wZipped.setSelection(in.isZipped());
		wNoempty.setSelection(in.noEmptyLines());
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		//wMultiple.setSelection(in.wildcard);
		wFilter.setSelection(in.hasFilter());
		if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		if (in.getFileFormat()   !=null) wFormat.setText(in.getFileFormat());
		wLimit.setText(""+in.getRowLimit());
		
		if (in.getFilterString()!=null) wFilterStr.setText(in.getFilterString());
		wFilterPos.setText(""+in.getFilterPosition());
		
		log.logDebug(toString(), "getting fields info...");
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    TextFileInputField field = in.getInputFields()[i];
		    
			TableItem item = wFields.table.getItem(i);
			item.setText(1, field.getName());
			String type     = field.getTypeDesc();
			String format   = field.getFormat();
			String position = ""+field.getPosition();
			String length   = ""+field.getLength();
			String prec     = ""+field.getPrecision();
			String curr     = field.getCurrencySymbol();
			String group    = field.getGroupSymbol();
			String decim    = field.getDecimalSymbol();
			String def      = field.getNullString();
			String trim     = field.getTrimTypeDesc();
			String rep      = field.isRepeated()?"Y":"N";
			
			if (type    !=null) item.setText( 2, type    );
			if (format  !=null) item.setText( 3, format  );
			if (position!=null && !"-1".equals(position)) item.setText( 4, position);
			if (length  !=null && !"-1".equals(length  )) item.setText( 5, length  );
			if (prec    !=null && !"-1".equals(prec    )) item.setText( 6, prec    );
			if (curr    !=null) item.setText( 7, curr    );
			if (decim   !=null) item.setText( 8, decim   );
			if (group   !=null) item.setText( 9, group   );
			if (def     !=null) item.setText(10, def     );
			if (trim    !=null) item.setText(11, trim    );
			if (rep     !=null) item.setText(12, rep     );
		}
		
		setMultiple();
		setIncludeFilename();
		setIncludeRownum();
		setFilter();		

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
	
	private void getInfo(TextFileInputMeta in)
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setFileType( wFiletype.getText() );
		in.setFileFormat( wFormat.getText() );
		in.setSeparator( wSeparator.getText() );
		in.setEnclosure( wEnclosure.getText() );
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setFilenameField( wInclFilenameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
		
		in.setFilter( wFilter.getSelection() );
		in.setFilterPosition( Const.toInt(wFilterPos.getText(), -1) );
		in.setFilterString( wFilterStr.getText() );
		
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setHeader( wHeader.getSelection() );
		in.setFooter( wFooter.getSelection() );
		in.setZipped( wZipped.getSelection() );
		// in.wildcard= wMultiple.getSelection();
		in.setNoEmptyLines( wNoempty.getSelection() );

		int i;
		//Table table = wFields.table;
		
		int nrfiles    = wFilenameList.getItemCount();
		int nrfields   = wFields.nrNonEmpty();
		in.allocate(nrfiles, nrfields);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );

		for (i=0;i<nrfields;i++)
		{
		    TextFileInputField field = new TextFileInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
			field.setName( item.getText(1) );
			field.setType( Value.getType(item.getText(2)) );
			field.setFormat( item.getText(3) );
			field.setPosition( Const.toInt(item.getText(4), -1) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupSymbol( item.getText(9) );
			field.setNullString( item.getText(10) );
			field.setTrimType( TextFileInputMeta.getTrimType(item.getText(11)) );
			field.setRepeated( "Y".equalsIgnoreCase(item.getText(12)) );		
			
			in.getInputFields()[i] = field;
		}		
	}
	
	private void get()
	{
		if (wFiletype.getText().equalsIgnoreCase("CSV"))
		{
			getCSV();
		}
		else
		{
			getFixed();
		}
	}
	
	// Get the data layout
	private void getCSV()
	{
		TextFileInputMeta meta = new TextFileInputMeta();
		getInfo(meta);
						
		String          files[] = meta.getFiles();
		FileInputStream fileInputStream = null;
		ZipInputStream  zipInputStream = null ;
		InputStream     inputStream  = null;
        String          fileFormat = wFormat.getText();
        
		if (files!=null && files.length>0)
		{
			int clearFields = meta.hasHeader()?SWT.YES:SWT.NO;
			int nrInputFields = meta.getInputFields().length;

			if (meta.hasHeader() && nrInputFields>0)
			{
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
				mb.setMessage("Do you want to clear the field list before starting?");
				mb.setText("Question");
				clearFields = mb.open();
			}

			try
			{
				if (clearFields == SWT.YES)
				{
					wFields.table.removeAll();
				}

				fileInputStream = new FileInputStream(new File(files[0]));
				Table table = wFields.table;
				
				if (meta.isZipped())
				{
					zipInputStream = new ZipInputStream(fileInputStream);
					zipInputStream.getNextEntry();
					inputStream=zipInputStream;
				}
				else
				{
					inputStream=fileInputStream;
				}
	
				if (clearFields == SWT.YES || !meta.hasHeader() || nrInputFields >0)
				{
                    // Scan the header-line, determine fields...
                    String line = null;
                    if (meta.hasHeader() || meta.getInputFields().length == 0)
                    {
                        line = TextFileInput.getLine(log, inputStream, fileFormat);
                        if (line != null)
                        {
                            ArrayList fields = TextFileInput.convertLineToStrings(log, line.toString(), meta);
                            // System.out.println("Found "+fields.size()+" fields in header!");

                            for (int i = 0; i < fields.size(); i++)
                            {
                                String field = (String) fields.get(i);
                                if (field == null || field.length() == 0 || (nrInputFields == 0 && !meta.hasHeader()))
                                {
                                    field = "Field" + (i + 1);
                                } else
                                {
                                    // Trim the field
                                    field = Const.trim(field);
                                    // Replace all spaces & - with underscore _
                                    field = Const.replace(field, " ", "_");
                                    field = Const.replace(field, "-", "_");
                                }

                                TableItem item = new TableItem(table, SWT.NONE);
                                item.setText(1, field);
                                item.setText(2, "String"); // The default type is String...
                                
                                // Copy it...
                                getInfo(meta);
                            }
                        }
                    }

                    // Sample a few lines to determine the correct type of the fields...
                    String shellText = "Nr of lines to sample.  0 means all lines.";
                    String lineText = "Number of sample lines (0=all lines)";
                    EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
                    int samples = end.open();
                    if (samples >= 0)
                    {
                        getInfo(meta);

    			        TextFileCSVImportProgressDialog pd = new TextFileCSVImportProgressDialog(log, props, shell, meta, inputStream, samples, clearFields);
                        String message = pd.open();
                        if (message!=null)
                        {
                            // OK, what's the result of our search?
                            getData(meta);
                            wFields.removeEmptyRows();
                            wFields.setRowNums();
                            wFields.optWidth(true);
    
        					EnterTextDialog etd = new EnterTextDialog(shell, props, "Scan results", "Result:", message, true);
        					etd.setReadOnly();
        					etd.open();
                        }
                    }
				}
				else
				{
                    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("I couldn't read the header-line from the input file! (empty file?)");
					mb.setText("ERROR");
					mb.open(); 
				}
			}
			catch(IOException e)
			{
                new ErrorDialog(shell, props, "I/O Error", "I/O error getting file description:", e);
			}
            catch(KettleException e)
            {
                new ErrorDialog(shell, props, "Error", "Error getting file description:", e);
            }
			finally
			{
				try
				{
					if (meta.isZipped() && zipInputStream!=null)
					{
						zipInputStream.closeEntry();
						zipInputStream.close();
					}
					inputStream.close();
				}
				catch(Exception e)
				{					
				}
			}
		}
		else
		{
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("I couldn't find a valid file to work with.  Please check the files, directories & expression.");
			mb.setText("ERROR");
			mb.open(); 
		}
	}
	
	public static final int guessPrecision(double d)
	{
		// Round numbers
		long frac = Math.round(( d - Math.floor(d) ) * 1E10); // max precision : 10
		int precision = 10;
		
		//  0,34 -->  3400000000 
		//  0 to the right --> precision -1!
		//  0 to the right means frac%10 == 0
				
		while (precision>=0 && (frac%10)==0)
		{
			frac/=10;
			precision--;
		}
		precision++;
				
		return precision;
	}

	public static final int guessIntLength(double d)
	{
		double flr = Math.floor(d);
		int len = 1;

		while (flr>9)
		{
			flr/=10;
			flr = Math.floor(flr);
			len++;
		}
				
		return len;
	}
	
	public static final int guessLength(double d)
	{	
		int intlen = guessIntLength(d);
		int precis = guessPrecision(d);
		int length = 1;
		
		if (precis>0)
		{
			length = intlen + 1 + precis;	
		}
		else
		{
			length = intlen;
		}
		
		return length;
	}

	// Preview the data
	private void preview()
	{
		String debug="Start";
		
		// Read dialog information into info
		TextFileInputMeta info = new TextFileInputMeta();
		debug="gef info";
		getInfo(info);
		debug="gef files";
		String files[] = info.getFiles();
		
		Row previousRow=null;
		int nrRepeats=0;
		long rownumber=1;
		
		if (files!=null && files.length>0)
		{
			int maxNrLines;
			if (previewlimit>=0) 
			{
				maxNrLines = previewlimit;
			} 
			else
			{
				String shellText = "Number of preview rows";
				String lineText  = "How many lines do you want to preview?";
				EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
				maxNrLines = end.open();
			}
			
			boolean stopPreview=false;
			if (maxNrLines>=0 && !stopPreview)
			{
				debug="A";

				// How many repeats?
				for (int i=0;i<info.getInputFields().length;i++) if (info.getInputFields()[i].isRepeated()) nrRepeats++;
				
				try
				{
					int linenr = 0;
					ArrayList rowbuffer = new ArrayList();
					for (int x=0;x<files.length && (linenr<maxNrLines || maxNrLines==0) && !stopPreview;x++)
					{
						debug="B";
						//System.out.println("Opening file: "+files[x]);
						FileInputStream fi = new FileInputStream(new File(files[x]));
						ZipInputStream zi=null ;
						InputStream f=null;
						if (info.isZipped())
						{
							zi = new ZipInputStream(fi);
							zi.getNextEntry();
							f=zi;
						}
						else
						{
							f=fi;
						}
						
						debug="C";
						String line = TextFileInput.getLine(log, f, wFormat.getText());
						if (info.hasHeader()) line = TextFileInput.getLine(log, f, wFormat.getText());
						
						debug="D";

						// Now read maxNrLines lines
						while (line!=null && (linenr<maxNrLines || maxNrLines==0))
						{
							StringBuffer error = new StringBuffer();
							Row r = TextFileInput.convertLineToRow(log, line, info, true, df, dfs, daf, dafs, files[x], rownumber);
							if (r!=null) 
							{
								rownumber++;
								// See if the previous values need to be repeated!
								if (nrRepeats>0)
								{
									if (previousRow==null) // First invocation...
									{
										previousRow=new Row();
										for (int i=0;i<info.getInputFields().length;i++)
										{
											if (info.getInputFields()[i].isRepeated())
											{
												Value value    = r.getValue(i);
												previousRow.addValue(new Value(value)); // Copy the first row
											}
										}
									}
									else
									{
										int repnr=0;
										for (int i=0;i<info.getInputFields().length;i++)
										{
											if (info.getInputFields()[i].isRepeated())
											{
												Value value = r.getValue(i);
												if (value.isNull()) // if it is empty: take the previous value!
												{
													Value prev = previousRow.getValue(repnr);
													r.removeValue(i);
													r.addValue(i, prev);
												}
												else // not empty: change the previousRow entry!
												{
													previousRow.removeValue(repnr);
													previousRow.addValue(repnr, new Value(value));
												}
												repnr++;
											}
										}
									}
								}
		
								// Finally, add the row to the preview buffer!
								if ( !(input.noEmptyLines() && r.isEmpty()) && !r.isIgnored() ) 
								{
									rowbuffer.add(r);
								} 
								else 
								{
									rownumber--;
									linenr--;
								} 
								
								// Get next line...
								line = TextFileInput.getLine(log, f, wFormat.getText());
							}
							else
							{
								MessageBox mb = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_ERROR );
								mb.setMessage("Error previewing file on line "+linenr+" : "+error);
								mb.setText("ERROR");
								int answer = mb.open();
								if (answer == SWT.CANCEL) stopPreview = true;
	
								line = null;
							}
							linenr++;
						}

						debug="E";
						
						if (info.isZipped())
						{
							zi.closeEntry();
							zi.close();
						}
						f.close();
					}
					debug="EA";
					if (previewlimit>=0)
					{
						if (!previewdialog.isDisposed())
						{
							previewdialog.dispose();
							debug="EB";
							previewbounds=previewdialog.getBounds();
							debug="EC";
							previewhscroll = previewdialog.getHScroll();
							debug="ED";
							previewvscroll = previewdialog.getVScroll();
						}
					}
					else
					{
						previewlimit=maxNrLines;
					}

					debug="F";

					wPreview.setText(STRING_PREVIEW_REFRESH);
					previewdialog = new PreviewRowsDialog(shell, SWT.NONE, wStepname.getText(), rowbuffer);
					previewdialog.setBounds(previewbounds);
					previewdialog.setHScroll(previewhscroll);
					previewdialog.setVScroll(previewvscroll);
					if (previewdialog.open()==null) // Close used in dialog itself!
					{
						previewbounds=previewdialog.getBounds();
						previewhscroll = previewdialog.getHScroll();
						previewvscroll = previewdialog.getVScroll();
						previewlimit=-1; // reset limit
						wPreview.setText(STRING_PREVIEW_ROWS);
					}
				}
				catch(Exception e)
				{
					wPreview.setText(STRING_PREVIEW_ROWS);
					previewdialog=null;
					previewlimit=-1;
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage("The following error occured while trying to preview rows: "+Const.CR+e.toString()+Const.CR+"Part: "+debug);
					mb.setText("ERROR");
					mb.open(); 
				}
				finally
				{
				}
			}
			
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("I couldn't find a valid file to work with.  Please check the files, directories & expression.");
			mb.setText("ERROR");
			mb.open(); 
		}
	}

	// Get the first x lines
	private void first()
	{
		TextFileInputMeta info = new TextFileInputMeta();
		getInfo(info);
		String files[] = info.getFiles();
		String debug="Start";
		MessageBox mb;
		FileInputStream        fi = null;
		ZipInputStream         zi = null ;
		BufferedInputStream     f  = null;
		
		if (files!=null && files.length>0)
		{
			String filename = files[0];
			try
			{
				fi = new FileInputStream(new File(filename));
				
                if (input.isZipped())
				{
					zi = new ZipInputStream(fi);
					zi.getNextEntry();
					f=new BufferedInputStream(zi);
				}
				else
				{
					f=new BufferedInputStream(fi);
				}

				String shellText = "Nr of lines to view.  0 means all lines.";
				String lineText = "Number of lines (0=all lines)";
				EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
				int nrlines = end.open();
				if (nrlines>=0)
				{
					String firstlines="";
					int    linenr=0;

					String line = TextFileInput.getLine(log, f, wFormat.getText());
					while(line!=null && (linenr<nrlines || nrlines==0))
					{
						firstlines+=line+Const.CR;
						linenr++;
						line = TextFileInput.getLine(log, f, wFormat.getText());
					}
					EnterTextDialog etd = new EnterTextDialog(shell, props, "File "+filename, (nrlines==0?"All":""+nrlines)+" lines:", firstlines, true);
					etd.setReadOnly();
					etd.open();
				}
			}
			catch(Exception e)
			{
				mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage("Error opening file : "+e.toString()+Const.CR+"--> "+debug);
				mb.setText("ERROR");
				mb.open(); 
			}
			finally
			{
				try
				{
					if (input.isZipped() && zi!=null)
					{
						zi.closeEntry();
						zi.close();
					}
					f.close();
				}
				catch(Exception e)
				{					
				}
			}
		}
		else
		{
			mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("I couldn't find a valid file to work with.  Please check the files, directories & expression.");
			mb.setText("ERROR");
			mb.open(); 
		}
	}
	

	// Get the first x lines
	private ArrayList getFirst(int nrlines)
	{
		TextFileInputMeta info = new TextFileInputMeta();
		getInfo(info);
		String files[] = info.getFiles();
		
        FileInputStream fi = null;
		ZipInputStream  zi = null ;
		InputStream     f  = null;
		
		ArrayList retval = new ArrayList();
		
		if (files!=null && files.length>0)
		{
			String filename = files[0];
			try
			{
				fi = new FileInputStream(new File(filename));
				
				if (info.isZipped())
				{
					zi = new ZipInputStream(fi);
					zi.getNextEntry();
					f=zi;
				}
				else
				{
					f=fi;
				}

				String firstlines="";
				int    linenr=0;
				int    maxnr = nrlines+(info.hasHeader()?1:0);
				
				// System.out.println("info.header? "+info.header+", info.footer? "+info.footer);
				
				String line = TextFileInput.getLine(log, f, wFormat.getText());
				while(line!=null && (linenr<maxnr || nrlines==0))
				{
					if (linenr>0 || (linenr==0 && !info.hasHeader())) retval.add(line);
					firstlines+=line+Const.CR;
					linenr++;
					line = TextFileInput.getLine(log, f, wFormat.getText());
				}
				// Did we grab the footer as well?
				if (info.hasFooter() && ( linenr<maxnr || nrlines==0 ))
				{
					// Remove the last line...
					if (retval.size()>0) retval.remove(retval.size()-1);
				}
			}
			catch(Exception e)
			{
			}
			finally
			{
				try
				{
					if (info.isZipped() && zi!=null)
					{
						zi.closeEntry();
						zi.close();
					}
					f.close();
				}
				catch(Exception e)
				{					
				}
			}
		}
		
		return retval;
	}
	

	
	private void getFixed()
	{
		TextFileInputMeta info = new TextFileInputMeta();
		getInfo(info);
		
		Shell sh = new Shell(shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);

		ArrayList rows = getFirst(25);
		fields = getFields(info, rows);
		
		final TextFileImportWizardPage1 page1 = new TextFileImportWizardPage1("1", props, rows, fields);
		page1.createControl(sh);
		final TextFileImportWizardPage2 page2 = new TextFileImportWizardPage2("2", props, rows, fields);
		page2.createControl(sh);

		Wizard wizard = new Wizard() 
		{
			public boolean performFinish() 
			{
				wFields.clearAll(false);
				
				for (int i=0;i<fields.size();i++)
				{
					TextFileInputField field = (TextFileInputField)fields.get(i);
					if (!field.isIgnored() && field.getLength()>0)
					{
						TableItem item = new TableItem(wFields.table, SWT.NONE);
						item.setText( 1,   field.getName());
						item.setText( 2,""+field.getTypeDesc());
						item.setText( 3,""+field.getFormat());
						item.setText( 4,""+field.getPosition());
						item.setText( 5,""+field.getLength());
						item.setText( 6,""+field.getPrecision());
						item.setText( 7,""+field.getCurrencySymbol());
						item.setText( 8,""+field.getDecimalSymbol());
						item.setText( 9,""+field.getGroupSymbol());
						item.setText(10,""+field.getNullString());
						item.setText(11,""+field.getTrimTypeDesc());
						item.setText(12,   field.isRepeated()?"Y":"N");
					}
					
				}
				int size = wFields.table.getItemCount(); 
				if (size==0)
				{
					new TableItem(wFields.table, SWT.NONE);
				}

				wFields.removeEmptyRows();				
				wFields.setRowNums();
				wFields.optWidth(true);
				
				input.setChanged();
				
				return true;
			}
		};
				
		wizard.addPage(page1);
		wizard.addPage(page2);
				
		WizardDialog wd = new WizardDialog(shell, wizard);
		wd.setMinimumPageSize(700,375);
		wd.open();
	}
	
	private Vector getFields(TextFileInputMeta info, ArrayList rows)
	{
		Vector fields = new Vector();

		int maxsize=0;
		for (int i=0;i<rows.size();i++) 
		{
			int len = ((String)rows.get(i)).length();
			if (len>maxsize) maxsize=len;
		}

		int prevEnd = 0;
		int dummynr = 1;

		for (int i=0;i<info.getInputFields().length;i++)
		{
		    TextFileInputField f = info.getInputFields()[i];
		    
			// See if positions are skipped, if this is the case, add dummy fields...
			if (f.getPosition()!=prevEnd) // gap
			{
				TextFileInputField field = new TextFileInputField("Dummy"+dummynr, prevEnd, f.getPosition()-prevEnd);
				field.setIgnored(true); // don't include in result by default.
				fields.add(field);
				dummynr++;
			}

			TextFileInputField field = new TextFileInputField(f.getName(), f.getPosition(), f.getLength());
			field.setType(f.getType());
			field.setIgnored(false);
			field.setFormat(f.getFormat());
			field.setPrecision(f.getPrecision());
			field.setTrimType(f.getTrimType());
			field.setDecimalSymbol(f.getDecimalSymbol());
			field.setGroupSymbol(f.getGroupSymbol());
			field.setCurrencySymbol(f.getCurrencySymbol());
			field.setRepeated(f.isRepeated());
			field.setNullString(f.getNullString());
			
			fields.add(field);
			
			prevEnd = field.getPosition()+field.getLength();
		}
		
		if (info.getInputFields().length==0)
		{
			System.out.println("No fields found: adding one!");
			
			TextFileInputField field = new TextFileInputField("Field1", 0, maxsize);
			fields.add(field);
		}
		else
		{		    
			// Take the last field and see if it reached until the maximum...
		    TextFileInputField f = info.getInputFields()[info.getInputFields().length-1];

			int pos = f.getPosition();
			int len = f.getLength();
			if (pos+len<maxsize)
			{
				// If not, add an extra trailing field!
				TextFileInputField field = new TextFileInputField("Dummy"+dummynr, pos+len, maxsize-pos-len);
				field.setIgnored(true); // don't include in result by default.
				fields.add(field);
				dummynr++;
			}
		}
		
		quickSort(fields);
		
		// for (int i=0;i<fields.size();i++) System.out.println("field #"+i+" : "+(TextFileInputField)fields.get(i));
	
		return fields;
	}

    
	/** Sort the entire vector, if it is not empty
	 */
	public synchronized void quickSort(Vector elements)
	{
		if (! elements.isEmpty())
		{ 
			this.quickSort(elements, 0, elements.size()-1);
		}
	}


	/**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2003 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
	  private synchronized void quickSort(Vector elements, int lowIndex, int highIndex)
	  { 
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		TextFileInputField pivotValue;  // values are Strings in this demo, change to suit your application
		TextFileInputField lowToHighValue;
		TextFileInputField highToLowValue;
		TextFileInputField parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		/** Choose a pivot, remember it's value
		 *  No special action for the pivot element itself.
		 *  It will be treated just like any other element.
		 */
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (TextFileInputField)elements.elementAt(pivotIndex);

		/** Split the Vector in two parts.
		 *
		 *  The lower part will be lowIndex - newHighIndex,
		 *  containing elements <= pivot Value
		 *
		 *  The higher part will be newLowIndex - highIndex,
		 *  containting elements >= pivot Value
		 * 
		 */
		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{ // loop from low to high to find a candidate for swapping
		  lowToHighValue = (TextFileInputField)elements.elementAt(lowToHighIndex);
		  while (lowToHighIndex < newLowIndex
			& lowToHighValue.compare(pivotValue)<0 )
		  { 
			newHighIndex = lowToHighIndex; // add element to lower part
			lowToHighIndex ++;
			lowToHighValue = (TextFileInputField)elements.elementAt(lowToHighIndex);
		  }

		  // loop from high to low find other candidate for swapping
		  highToLowValue = (TextFileInputField)elements.elementAt(highToLowIndex);
		  while (newHighIndex <= highToLowIndex
			& (highToLowValue.compare(pivotValue)>0)
			)
		  { 
			newLowIndex = highToLowIndex; // add element to higher part
			highToLowIndex --;
			highToLowValue = (TextFileInputField)elements.elementAt(highToLowIndex);
		  }

		  // swap if needed
		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		  { 
			newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		  }
		  else if (lowToHighIndex < highToLowIndex) // not last element yet
		  { 
			compareResult = lowToHighValue.compare(highToLowValue);
			if (compareResult >= 0) // low >= high, swap, even if equal
			{ 
			  parking = lowToHighValue;
			  elements.setElementAt(highToLowValue, lowToHighIndex);
			  elements.setElementAt(parking, highToLowIndex);

			  newLowIndex = highToLowIndex;
			  newHighIndex = lowToHighIndex;

			  lowToHighIndex ++;
			  highToLowIndex --;
			}
		  }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{ 
			this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{ 
			this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
		}
	  }

	
	public String toString()
	{
		return this.getClass().getName();
	}

}
