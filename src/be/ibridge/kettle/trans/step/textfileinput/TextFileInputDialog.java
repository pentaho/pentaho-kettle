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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import be.ibridge.kettle.core.dialog.EnterNumberDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.EnterTextDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.dialog.TransPreviewProgressDialog;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class TextFileInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wFileTab, wContentTab;
    private CTabItem     wFilterTab;
    private CTabItem     wFieldsTab;

	private Composite    wFileComp, wContentComp;
    private Composite    wFilterComp;
    private Composite    wFieldsComp;
    
	private FormData     fdFileComp, fdContentComp;
    private FormData     fdFilterComp;
    private FormData     fdFieldsComp;

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

    private Button       wFirstHeader;
    private FormData     fdFirstHeader;
    private Listener     lsFirstHeader;

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

    private Label        wlEscape;
    private Text         wEscape;
    private FormData     fdlEscape, fdEscape;

	private Label        wlHeader;
	private Button       wHeader;
	private FormData     fdlHeader, fdHeader;

    private Label        wlNrHeader;
    private Text         wNrHeader;
    private FormData     fdlNrHeader, fdNrHeader;

	private Label        wlFooter;
	private Button       wFooter;
	private FormData     fdlFooter, fdFooter;

    private Label        wlNrFooter;
    private Text         wNrFooter;
    private FormData     fdlNrFooter, fdNrFooter;

    private Label        wlWraps;
    private Button       wWraps;
    private FormData     fdlWraps, fdWraps;

    private Label        wlNrWraps;
    private Text         wNrWraps;
    private FormData     fdlNrWraps, fdNrWraps;

    private Label        wlLayoutPaged;
    private Button       wLayoutPaged;
    private FormData     fdlLayoutPaged, fdLayoutPaged;

    private Label        wlNrLinesPerPage;
    private Text         wNrLinesPerPage;
    private FormData     fdlNrLinesPerPage, fdNrLinesPerPage;

    private Label        wlNrLinesDocHeader;
    private Text         wNrLinesDocHeader;
    private FormData     fdlNrLinesDocHeader, fdNrLinesDocHeader;

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

    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;

	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

    // ERROR HANDLING...
    private Label        wlErrorIgnored;
    private Button       wErrorIgnored;
    private FormData     fdlErrorIgnored, fdErrorIgnored;

    private Label        wlErrorCount;
    private Text         wErrorCount;
    private FormData     fdlErrorCount, fdErrorCount;

    private Label        wlErrorFields;
    private Text         wErrorFields;
    private FormData     fdlErrorFields, fdErrorFields;

    private Label        wlErrorText;
    private Text         wErrorText;
    private FormData     fdlErrorText, fdErrorText;

    
    private TableView    wFilter;
    private FormData     fdFilter;
    
	private TableView    wFields;
	private FormData     fdFields;

	private TextFileInputMeta input;

	// Wizard info...
	private Vector fields;
    
    private int middle, margin;
    private ModifyListener lsMod;
		
	private static final String STRING_PREVIEW_ROWS    = "  &Preview rows   "; 
	
	public static final int dateLengths[] = new int[]
		{
			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
		}
		;
    
    private boolean gotEncodings = false;

	public TextFileInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(TextFileInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		lsMod = new ModifyListener() 
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
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;

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
		wFirst.setText(" Show &file content ");
		fdFirst=new FormData();
		fdFirst.left=new FormAttachment(wbShowFiles, margin*2);
		fdFirst.bottom =new FormAttachment(100, 0);
		wFirst.setLayoutData(fdFirst);

        wFirstHeader=new Button(wFileComp, SWT.PUSH);
        wFirstHeader.setText(" Show &content from first data line");
        fdFirstHeader=new FormData();
        fdFirstHeader.left=new FormAttachment(wFirst, margin*2);
        fdFirstHeader.bottom =new FormAttachment(100, 0);
        wFirstHeader.setLayoutData(fdFirstHeader);

		
		ColumnInfo[] colinfo=new ColumnInfo[2];
		colinfo[ 0]=new ColumnInfo("File/Directory",  ColumnInfo.COLUMN_TYPE_TEXT,    false);
		colinfo[ 1]=new ColumnInfo("Wildcard",        ColumnInfo.COLUMN_TYPE_TEXT,    false );
		
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

		addContentTab();
		addFiltersTabs();
        addFieldsTabs();

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
		lsOK          = new Listener() { public void handleEvent(Event e) { ok();           } };
		lsFirst       = new Listener() { public void handleEvent(Event e) { first(false);   } };
        lsFirstHeader = new Listener() { public void handleEvent(Event e) { first(true);    } };
		lsGet         = new Listener() { public void handleEvent(Event e) { get();          } };
		lsPreview     = new Listener() { public void handleEvent(Event e) { preview();      } };
		lsCancel      = new Listener() { public void handleEvent(Event e) { cancel();       } };
		
		wOK.addListener           (SWT.Selection, lsOK          );
		wFirst.addListener        (SWT.Selection, lsFirst       );
        wFirstHeader.addListener  (SWT.Selection, lsFirstHeader );
		wGet.addListener          (SWT.Selection, lsGet         );
		wPreview.addListener      (SWT.Selection, lsPreview     );
		wCancel.addListener       (SWT.Selection, lsCancel      );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		// wFilename.addSelectionListener( lsDef );
		wSeparator.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		wInclFilenameField.addSelectionListener( lsDef );
        wNrHeader.addSelectionListener( lsDef );
        wNrFooter.addSelectionListener( lsDef );
        wNrWraps.addSelectionListener( lsDef );

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
        
        SelectionAdapter lsFlags = new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                setFlags();
            }
        };
        
		// Enable/disable the right fields...
        wInclFilename.addSelectionListener( lsFlags );
        wInclRownum.addSelectionListener( lsFlags );
        wErrorIgnored.addSelectionListener(lsFlags);
        wHeader.addSelectionListener(lsFlags);
        wFooter.addSelectionListener(lsFlags);
        wWraps.addSelectionListener(lsFlags);
        wLayoutPaged.addSelectionListener(lsFlags);


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
                    System.out.println("sp.keySet().size()="+sp.keySet().size());
                    ArrayList keys = new ArrayList( sp.keySet() );
                    Collections.sort(keys);
                    
					int size = keys.size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
                    
					for (int i=0;i<keys.size();i++)
					{
						key[i] = (String)keys.get(i);
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
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
		getData(input);

        setSize();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void addContentTab()
    {
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

        // Escape
        wlEscape=new Label(wContentComp, SWT.RIGHT);
        wlEscape.setText("Escape ");
        props.setLook(wlEscape);
        fdlEscape=new FormData();
        fdlEscape.left = new FormAttachment(0, 0);
        fdlEscape.top  = new FormAttachment(wEnclosure, margin);
        fdlEscape.right= new FormAttachment(middle, -margin);
        wlEscape.setLayoutData(fdlEscape);
        wEscape=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wEscape);
        wEscape.addModifyListener(lsMod);
        fdEscape=new FormData();
        fdEscape.left = new FormAttachment(middle, 0);
        fdEscape.top  = new FormAttachment(wEnclosure, margin);
        fdEscape.right= new FormAttachment(100, 0);
        wEscape.setLayoutData(fdEscape);

        // Header checkbox
        wlHeader=new Label(wContentComp, SWT.RIGHT);
        wlHeader.setText("Header ");
        props.setLook(wlHeader);
        fdlHeader=new FormData();
        fdlHeader.left = new FormAttachment(0, 0);
        fdlHeader.top  = new FormAttachment(wEscape, margin);
        fdlHeader.right= new FormAttachment(middle, -margin);
        wlHeader.setLayoutData(fdlHeader);
        wHeader=new Button(wContentComp, SWT.CHECK);
        props.setLook(wHeader);
        fdHeader=new FormData();
        fdHeader.left = new FormAttachment(middle, 0);
        fdHeader.top  = new FormAttachment(wEscape, margin);
        wHeader.setLayoutData(fdHeader);

        // NrHeader
        wlNrHeader=new Label(wContentComp, SWT.RIGHT);
        wlNrHeader.setText("Number of header lines");
        props.setLook(wlNrHeader);
        fdlNrHeader=new FormData();
        fdlNrHeader.left = new FormAttachment(wHeader, margin);
        fdlNrHeader.top  = new FormAttachment(wEscape, margin);
        wlNrHeader.setLayoutData(fdlNrHeader);
        wNrHeader=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNrHeader.setTextLimit(3);
        props.setLook(wNrHeader);
        wNrHeader.addModifyListener(lsMod);
        fdNrHeader=new FormData();
        fdNrHeader.left = new FormAttachment(wlNrHeader, margin);
        fdNrHeader.top  = new FormAttachment(wEscape, margin);
        wNrHeader.setLayoutData(fdNrHeader);
        
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
        wFooter.setLayoutData(fdFooter);

        // NrFooter
        wlNrFooter=new Label(wContentComp, SWT.RIGHT);
        wlNrFooter.setText("Number of footer lines");
        props.setLook(wlNrFooter);
        fdlNrFooter=new FormData();
        fdlNrFooter.left = new FormAttachment(wFooter, margin);
        fdlNrFooter.top  = new FormAttachment(wHeader, margin);
        wlNrFooter.setLayoutData(fdlNrFooter);
        wNrFooter=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNrFooter.setTextLimit(3);
        props.setLook(wNrFooter);
        wNrFooter.addModifyListener(lsMod);
        fdNrFooter=new FormData();
        fdNrFooter.left = new FormAttachment(wlNrFooter, margin);
        fdNrFooter.top  = new FormAttachment(wHeader, margin);
        wNrFooter.setLayoutData(fdNrFooter);

        // Wraps
        wlWraps=new Label(wContentComp, SWT.RIGHT);
        wlWraps.setText("Wrapped lines?");
        props.setLook(wlWraps);
        fdlWraps=new FormData();
        fdlWraps.left = new FormAttachment(0, 0);
        fdlWraps.top  = new FormAttachment(wFooter, margin);
        fdlWraps.right= new FormAttachment(middle, -margin);
        wlWraps.setLayoutData(fdlWraps);
        wWraps=new Button(wContentComp, SWT.CHECK);
        props.setLook(wWraps);
        fdWraps=new FormData();
        fdWraps.left = new FormAttachment(middle, 0);
        fdWraps.top  = new FormAttachment(wFooter, margin);
        wWraps.setLayoutData(fdWraps);

        // NrWraps
        wlNrWraps=new Label(wContentComp, SWT.RIGHT);
        wlNrWraps.setText("Number of times wrapped");
        props.setLook(wlNrWraps);
        fdlNrWraps=new FormData();
        fdlNrWraps.left = new FormAttachment(wWraps, margin);
        fdlNrWraps.top  = new FormAttachment(wFooter, margin);
        wlNrWraps.setLayoutData(fdlNrWraps);
        wNrWraps=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNrWraps.setTextLimit(3);
        props.setLook(wNrWraps);
        wNrWraps.addModifyListener(lsMod);
        fdNrWraps=new FormData();
        fdNrWraps.left = new FormAttachment(wlNrWraps, margin);
        fdNrWraps.top  = new FormAttachment(wFooter, margin);
        wNrWraps.setLayoutData(fdNrWraps);

        // Pages
        wlLayoutPaged=new Label(wContentComp, SWT.RIGHT);
        wlLayoutPaged.setText("Paged layout (printout)?");
        props.setLook(wlLayoutPaged);
        fdlLayoutPaged=new FormData();
        fdlLayoutPaged.left = new FormAttachment(0, 0);
        fdlLayoutPaged.top  = new FormAttachment(wWraps, margin);
        fdlLayoutPaged.right= new FormAttachment(middle, -margin);
        wlLayoutPaged.setLayoutData(fdlLayoutPaged);
        wLayoutPaged=new Button(wContentComp, SWT.CHECK);
        props.setLook(wLayoutPaged);
        fdLayoutPaged=new FormData();
        fdLayoutPaged.left = new FormAttachment(middle, 0);
        fdLayoutPaged.top  = new FormAttachment(wWraps, margin);
        wLayoutPaged.setLayoutData(fdLayoutPaged);

        // Nr of lines per page
        wlNrLinesPerPage=new Label(wContentComp, SWT.RIGHT);
        wlNrLinesPerPage.setText("Number lines per page");
        props.setLook(wlNrLinesPerPage);
        fdlNrLinesPerPage=new FormData();
        fdlNrLinesPerPage.left = new FormAttachment(wLayoutPaged, margin);
        fdlNrLinesPerPage.top  = new FormAttachment(wWraps, margin);
        wlNrLinesPerPage.setLayoutData(fdlNrLinesPerPage);
        wNrLinesPerPage=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNrLinesPerPage.setTextLimit(3);
        props.setLook(wNrLinesPerPage);
        wNrLinesPerPage.addModifyListener(lsMod);
        fdNrLinesPerPage=new FormData();
        fdNrLinesPerPage.left = new FormAttachment(wlNrLinesPerPage, margin);
        fdNrLinesPerPage.top  = new FormAttachment(wWraps, margin);
        wNrLinesPerPage.setLayoutData(fdNrLinesPerPage);

        // NrPages
        wlNrLinesDocHeader=new Label(wContentComp, SWT.RIGHT);
        wlNrLinesDocHeader.setText("Document header lines ");
        props.setLook(wlNrLinesDocHeader);
        fdlNrLinesDocHeader=new FormData();
        fdlNrLinesDocHeader.left = new FormAttachment(wNrLinesPerPage, margin);
        fdlNrLinesDocHeader.top  = new FormAttachment(wWraps, margin);
        wlNrLinesDocHeader.setLayoutData(fdlNrLinesDocHeader);
        wNrLinesDocHeader=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNrLinesDocHeader.setTextLimit(3);
        props.setLook(wNrLinesDocHeader);
        wNrLinesDocHeader.addModifyListener(lsMod);
        fdNrLinesDocHeader=new FormData();
        fdNrLinesDocHeader.left = new FormAttachment(wlNrLinesDocHeader, margin);
        fdNrLinesDocHeader.top  = new FormAttachment(wWraps, margin);
        wNrLinesDocHeader.setLayoutData(fdNrLinesDocHeader);

        // Zipped?
        wlZipped=new Label(wContentComp, SWT.RIGHT);
        wlZipped.setText("Zipped ");
        props.setLook(wlZipped);
        fdlZipped=new FormData();
        fdlZipped.left = new FormAttachment(0, 0);
        fdlZipped.top  = new FormAttachment(wNrLinesDocHeader, margin);
        fdlZipped.right= new FormAttachment(middle, -margin);
        wlZipped.setLayoutData(fdlZipped);
        wZipped=new Button(wContentComp, SWT.CHECK );
        props.setLook(wZipped);
        wZipped.setToolTipText("Only the first entry in the archive is read!");
        fdZipped=new FormData();
        fdZipped.left = new FormAttachment(middle, 0);
        fdZipped.top  = new FormAttachment(wNrLinesDocHeader, margin);
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

        wlEncoding=new Label(wContentComp, SWT.RIGHT);
        wlEncoding.setText("Encoding ");
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wFormat, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wFormat, margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        wlLimit=new Label(wContentComp, SWT.RIGHT);
        wlLimit.setText("Limit ");
        props.setLook(wlLimit);
        fdlLimit=new FormData();
        fdlLimit.left = new FormAttachment(0, 0);
        fdlLimit.top  = new FormAttachment(wEncoding, margin);
        fdlLimit.right= new FormAttachment(middle, -margin);
        wlLimit.setLayoutData(fdlLimit);
        wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLimit);
        wLimit.addModifyListener(lsMod);
        fdLimit=new FormData();
        fdLimit.left = new FormAttachment(middle, 0);
        fdLimit.top  = new FormAttachment(wEncoding, margin);
        fdLimit.right= new FormAttachment(100, 0);
        wLimit.setLayoutData(fdLimit);

        
        // ERROR HANDLING...
        // ErrorIgnored?
        wlErrorIgnored = new Label(wContentComp, SWT.RIGHT);
        wlErrorIgnored.setText("Ignore errors? ");
        props.setLook(wlErrorIgnored);
        fdlErrorIgnored = new FormData();
        fdlErrorIgnored.left = new FormAttachment(0, 0);
        fdlErrorIgnored.top = new FormAttachment(wLimit, margin);
        fdlErrorIgnored.right = new FormAttachment(middle, -margin);
        wlErrorIgnored.setLayoutData(fdlErrorIgnored);
        wErrorIgnored = new Button(wContentComp, SWT.CHECK);
        props.setLook(wErrorIgnored);
        wErrorIgnored.setToolTipText("Ignore parsing errors that occur, optionally log information about the errors.");
        fdErrorIgnored = new FormData();
        fdErrorIgnored.left = new FormAttachment(middle, 0);
        fdErrorIgnored.top = new FormAttachment(wLimit, margin);
        wErrorIgnored.setLayoutData(fdErrorIgnored);

        wlErrorCount=new Label(wContentComp, SWT.RIGHT);
        wlErrorCount.setText("Error count fieldname ");
        props.setLook(wlErrorCount);
        fdlErrorCount=new FormData();
        fdlErrorCount.left = new FormAttachment(0, 0);
        fdlErrorCount.top  = new FormAttachment(wErrorIgnored, margin);
        fdlErrorCount.right= new FormAttachment(middle, -margin);
        wlErrorCount.setLayoutData(fdlErrorCount);
        wErrorCount=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wErrorCount);
        wErrorCount.addModifyListener(lsMod);
        fdErrorCount=new FormData();
        fdErrorCount.left = new FormAttachment(middle, 0);
        fdErrorCount.top  = new FormAttachment(wErrorIgnored, margin);
        fdErrorCount.right= new FormAttachment(100, 0);
        wErrorCount.setLayoutData(fdErrorCount);

        wlErrorFields=new Label(wContentComp, SWT.RIGHT);
        wlErrorFields.setText("Error fields fieldname");
        props.setLook(wlErrorFields);
        fdlErrorFields=new FormData();
        fdlErrorFields.left = new FormAttachment(0, 0);
        fdlErrorFields.top  = new FormAttachment(wErrorCount, margin);
        fdlErrorFields.right= new FormAttachment(middle, -margin);
        wlErrorFields.setLayoutData(fdlErrorFields);
        wErrorFields=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wErrorFields);
        wErrorFields.addModifyListener(lsMod);
        fdErrorFields=new FormData();
        fdErrorFields.left = new FormAttachment(middle, 0);
        fdErrorFields.top  = new FormAttachment(wErrorCount, margin);
        fdErrorFields.right= new FormAttachment(100, 0);
        wErrorFields.setLayoutData(fdErrorFields);

        wlErrorText=new Label(wContentComp, SWT.RIGHT);
        wlErrorText.setText("Error text fieldname");
        props.setLook(wlErrorText);
        fdlErrorText=new FormData();
        fdlErrorText.left = new FormAttachment(0, 0);
        fdlErrorText.top  = new FormAttachment(wErrorFields, margin);
        fdlErrorText.right= new FormAttachment(middle, -margin);
        wlErrorText.setLayoutData(fdlErrorText);
        wErrorText=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wErrorText);
        wErrorText.addModifyListener(lsMod);
        fdErrorText=new FormData();
        fdErrorText.left = new FormAttachment(middle, 0);
        fdErrorText.top  = new FormAttachment(wErrorFields, margin);
        fdErrorText.right= new FormAttachment(100, 0);
        wErrorText.setLayoutData(fdErrorText);
        
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

    }

    private void addFiltersTabs()
    {
        // Filters tab...
        //
        wFilterTab = new CTabItem(wTabFolder, SWT.NONE);
        wFilterTab.setText("Filters");
        
        FormLayout FilterLayout = new FormLayout ();
        FilterLayout.marginWidth  = Const.FORM_MARGIN;
        FilterLayout.marginHeight = Const.FORM_MARGIN;
        
        wFilterComp = new Composite(wTabFolder, SWT.NONE);
        wFilterComp.setLayout(FilterLayout);
        props.setLook(wFilterComp);
        
        final int FilterRows=input.getFilter().length;
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
             new ColumnInfo("Filter string",      ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Filter position",    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Stop on filter",     ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] { "N", "Y" } )
            };
        
        colinf[2].setToolTip("set this field to Y if you want to stop processing when the filter is encountered");
        
        wFilter=new TableView(wFilterComp, 
                              SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FilterRows,  
                              lsMod,
                              props
                              );

        fdFilter=new FormData();
        fdFilter.left  = new FormAttachment(0, 0);
        fdFilter.top   = new FormAttachment(0, 0);
        fdFilter.right = new FormAttachment(100, 0);
        fdFilter.bottom= new FormAttachment(100, 0);
        wFilter.setLayoutData(fdFilter);

        fdFilterComp=new FormData();
        fdFilterComp.left  = new FormAttachment(0, 0);
        fdFilterComp.top   = new FormAttachment(0, 0);
        fdFilterComp.right = new FormAttachment(100, 0);
        fdFilterComp.bottom= new FormAttachment(100, 0);
        wFilterComp.setLayoutData(fdFilterComp);
        
        wFilterComp.layout();
        wFilterTab.setControl(wFilterComp);
    }

    
    private void addFieldsTabs()
    {
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
        String formats[] = Const.getConversionFormats();
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
             new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO,  Value.getTypes(), true ),
             new ColumnInfo("Format",     ColumnInfo.COLUMN_TYPE_CCOMBO,  formats),
             new ColumnInfo("Position",   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Length",     ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Precision",  ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Currency",   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Decimal",    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Group",      ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Null if",    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Trim type",  ColumnInfo.COLUMN_TYPE_CCOMBO,  TextFileInputMeta.trimTypeDesc, true ),
             new ColumnInfo("Repeat",     ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] { "Y", "N" }, true )
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
    }

    public void setFlags()
	{
        wlInclFilenameField.setEnabled(wInclFilename.getSelection());
        wInclFilenameField.setEnabled(wInclFilename.getSelection());

        wlInclRownumField.setEnabled(wInclRownum.getSelection());
        wInclRownumField.setEnabled(wInclRownum.getSelection());

        wlErrorCount.setEnabled( wErrorIgnored.getSelection() );
        wErrorCount.setEnabled( wErrorIgnored.getSelection() );
        wlErrorFields.setEnabled( wErrorIgnored.getSelection() );
        wErrorFields.setEnabled( wErrorIgnored.getSelection() );
        wlErrorText.setEnabled( wErrorIgnored.getSelection() );
        wErrorText.setEnabled( wErrorIgnored.getSelection() );
        
        wlNrHeader.setEnabled( wHeader.getSelection() );
        wNrHeader.setEnabled( wHeader.getSelection() );
        wlNrFooter.setEnabled( wFooter.getSelection() );
        wNrFooter.setEnabled( wFooter.getSelection() );
        wlNrWraps.setEnabled( wWraps.getSelection() );
        wNrWraps.setEnabled( wWraps.getSelection() );

        wlNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
        wNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
        wlNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
        wNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
    }

	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param meta The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(TextFileInputMeta meta)
	{
        final TextFileInputMeta in = meta;
        
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
        if (in.getEscapeCharacter()!=null) wEscape.setText(in.getEscapeCharacter());
		wHeader.setSelection(in.hasHeader());
        wNrHeader.setText( ""+in.getNrHeaderLines() );
		wFooter.setSelection(in.hasFooter());
        wNrFooter.setText( ""+in.getNrFooterLines() );
        wWraps.setSelection(in.isLineWrapped());
        wNrWraps.setText( ""+in.getNrWraps() );
        wLayoutPaged.setSelection(in.isLayoutPaged());
        wNrLinesPerPage.setText( ""+in.getNrLinesPerPage() );
        wNrLinesDocHeader.setText( ""+in.getNrLinesDocHeader() );
		wZipped.setSelection(in.isZipped());
		wNoempty.setSelection(in.noEmptyLines());
		wInclFilename.setSelection(in.includeFilename());
		wInclRownum.setSelection(in.includeRowNumber());
		
        if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		if (in.getFileFormat()   !=null) wFormat.setText(in.getFileFormat());
		wLimit.setText(""+in.getRowLimit());
		
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
		
        if ( in.getEncoding()!=null ) wEncoding.setText( in.getEncoding() );
        
        // Error handling fields...
        wErrorIgnored.setSelection( in.isErrorIgnored() );
        if (in.getErrorCountField()!=null) wErrorCount.setText( in.getErrorCountField() );
        if (in.getErrorFieldsField()!=null) wErrorFields.setText( in.getErrorFieldsField() );
        if (in.getErrorTextField()!=null) wErrorText.setText( in.getErrorTextField() );
        
        for (int i=0;i<in.getFilter().length;i++)
        {
            TableItem item = wFilter.table.getItem(i);
            
            TextFileFilter filter = in.getFilter()[i];
            if (filter.getFilterString()  !=null) item.setText(1, filter.getFilterString());
            if (filter.getFilterPosition()>=0   ) item.setText(2, ""+filter.getFilterPosition());
            item.setText(3, filter.isFilterLastLine()?"Y":"N");
        }
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

        wFilter.removeEmptyRows();
        wFilter.setRowNums();
        wFilter.optWidth(true);

        setFlags();
        
		wStepname.selectAll();
	}
	
	private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            ArrayList values = new ArrayList(Charset.availableCharsets().values());
            for (int i=0;i<values.size();i++)
            {
                Charset charSet = (Charset)values.get(i);
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
        }
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
        in.setEscapeCharacter( wEscape.getText() );
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setFilenameField( wInclFilenameField.getText() );
		in.setRowNumberField( wInclRownumField.getText() );
				
		in.setIncludeFilename( wInclFilename.getSelection() );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setHeader( wHeader.getSelection() );
        in.setNrHeaderLines( Const.toInt( wNrHeader.getText(), 1) );
		in.setFooter( wFooter.getSelection() );
        in.setNrFooterLines( Const.toInt( wNrFooter.getText(), 1) );
        in.setLineWrapped( wWraps.getSelection() );
        in.setNrWraps( Const.toInt( wNrWraps.getText(), 1) );
        in.setLayoutPaged( wLayoutPaged.getSelection() );
        in.setNrLinesPerPage( Const.toInt( wNrLinesPerPage.getText(), 80) );
        in.setNrLinesDocHeader( Const.toInt( wNrLinesDocHeader.getText(), 0) );
		in.setZipped( wZipped.getSelection() );

		in.setNoEmptyLines( wNoempty.getSelection() );

        String encoding = wEncoding.getText();
        if (encoding.length()>0) 
        {
            in.setEncoding(encoding);
        }
        
		int nrfiles    = wFilenameList.getItemCount();
		int nrfields   = wFields.nrNonEmpty();
        int nrfilters  = wFilter.nrNonEmpty();
		in.allocate(nrfiles, nrfields, nrfilters);

		in.setFileName( wFilenameList.getItems(0) );
		in.setFileMask( wFilenameList.getItems(1) );

		for (int i=0;i<nrfields;i++)
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
        
        for (int i=0;i<nrfilters;i++)
        {
            TableItem item = wFilter.getNonEmpty(i);
            TextFileFilter filter = new TextFileFilter();
            in.getFilter()[i] = filter;
            
            filter.setFilterString( item.getText(1) );
            filter.setFilterPosition( Const.toInt(item.getText(2), -1) );
            filter.setFilterLastLine( "Y".equalsIgnoreCase( item.getText(3) ) );
        }
        // Error handling fields...
        in.setErrorIgnored( wErrorIgnored.getSelection() );
        in.setErrorCountField( wErrorCount.getText() );
        in.setErrorFieldsField( wErrorFields.getText() );
        in.setErrorTextField( wErrorText.getText() );
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
                
                InputStreamReader reader;
                if (meta.getEncoding()!=null && meta.getEncoding().length()>0)
                {
                    reader = new InputStreamReader(inputStream, meta.getEncoding());
                }
                else
                {
                    reader = new InputStreamReader(inputStream);
                }
	
				if (clearFields == SWT.YES || !meta.hasHeader() || nrInputFields >0)
				{
                    // Scan the header-line, determine fields...
                    String line = null;
                    if (meta.hasHeader() || meta.getInputFields().length == 0)
                    {
                        line = TextFileInput.getLine(log, reader, fileFormat);
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

    			        TextFileCSVImportProgressDialog pd = new TextFileCSVImportProgressDialog(log, props, shell, meta, reader, samples, clearFields);
                        String message = pd.open();
                        if (message!=null)
                        {
                            // OK, what's the result of our search?
                            getData(meta);
                            wFields.removeEmptyRows();
                            wFields.setRowNums();
                            wFields.optWidth(true);
    
        					EnterTextDialog etd = new EnterTextDialog(shell, "Scan results", "Result:", message, true);
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
        // Create the XML input step
        TextFileInputMeta oneMeta = new TextFileInputMeta();
        getInfo(oneMeta);

        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(oneMeta, wStepname.getText());
        
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

	// Get the first x lines
	private void first(boolean skipHeaders)
	{
		TextFileInputMeta info = new TextFileInputMeta();
		getInfo(info);

        try
        {
    		if (info.getFiles()!=null && info.getFiles().length>0)
    		{
    			String shellText = "Nr of lines to view.  0 means all lines.";
    			String lineText = "Number of lines (0=all lines)";
    			EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
    			int nrLines = end.open();
    			if (nrLines>=0)
    			{
                    ArrayList linesList = getFirst(nrLines, skipHeaders);
                    if (linesList!=null && linesList.size()>0)
                    {
                        String firstlines="";
                        for (int i=0;i<linesList.size();i++)
                        {
                            firstlines+=(String)linesList.get(i)+Const.CR;
                        }
                        EnterTextDialog etd = new EnterTextDialog(shell, "Content of first file", (nrLines==0?"All":""+nrLines)+" lines:", firstlines, true);
                        etd.setReadOnly();
                        etd.open();
                    }
                    else
                    {
                        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                        mb.setMessage("I couldn't read any lines from the specified files.");
                        mb.setText("Sorry");
                        mb.open(); 
    				}
    			}
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                mb.setMessage("I couldn't find a valid file to work with.  Please check the files, directories & expressions.");
                mb.setText("ERROR");
                mb.open(); 
            }
        }
		catch(KettleException e)
		{
            new ErrorDialog(shell, props, "Error", "Error getting data from file", e);
		}
	}
	

	// Get the first x lines
	private ArrayList getFirst(int nrlines, boolean skipHeaders) throws KettleException
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
                
                InputStreamReader reader;
                if (info.getEncoding()!=null && info.getEncoding().length()>0)
                {
                    reader = new InputStreamReader(f, info.getEncoding());
                }
                else
                {
                    reader = new InputStreamReader(f);
                }


				String firstlines="";
				int    linenr=0;
				int    maxnr = nrlines+(info.hasHeader()?info.getNrHeaderLines():0);
				
                if (skipHeaders)
                {
                    // Skip the header lines first if more then one, it helps us position
                    if (info.isLayoutPaged() && info.getNrLinesDocHeader()>0)
                    {
                        int skipped = 0;
                        String line = TextFileInput.getLine(log, reader, wFormat.getText());
                        while (line!=null && skipped<info.getNrLinesDocHeader()-1)
                        {
                            skipped++;
                            line = TextFileInput.getLine(log, reader, wFormat.getText());
                        }
                    }
                    
                    // Skip the header lines first if more then one, it helps us position
                    if (info.hasHeader() && info.getNrHeaderLines()>0)
                    {
                        int skipped = 0;
                        String line = TextFileInput.getLine(log, reader, wFormat.getText());
                        while (line!=null && skipped<info.getNrHeaderLines()-1)
                        {
                            skipped++;
                            line = TextFileInput.getLine(log, reader, wFormat.getText());
                        }
                    }
                }
                
				String line = TextFileInput.getLine(log, reader, wFormat.getText());
				while(line!=null && (linenr<maxnr || nrlines==0))
				{
					retval.add(line);
					firstlines+=line+Const.CR;
					linenr++;
					line = TextFileInput.getLine(log, reader, wFormat.getText());
				}
			}
			catch(Exception e)
			{
                throw new KettleException("Error getting first "+nrlines+" from file "+filename, e);
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

        try
        {
    		ArrayList rows = getFirst(50, false);
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
        catch(Exception e)
        {
            new ErrorDialog(shell, props, "Error showing fixed wizard", "An unexpected error occured showing the fixed field width wizard", e);
        }
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
