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
 * Created on 2-jul-2003
 *
 */

package be.ibridge.kettle.trans.step.dimensionlookup;

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
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class DimensionLookupDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wTableTab, wKeyTab, wFieldsTab;

	private FormData     fdTableComp, fdKeyComp, fdFieldsComp;

	private CCombo       wConnection;

	private Label        wlTable;
	private Button       wbTable;
	private Text         wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;

	private Label        wlTk;
	private Text         wTk;
	private FormData     fdlTk, fdTk;

	private Label        wlTkRename;
	private Text         wTkRename;
	private FormData     fdlTkRename, fdTkRename;

	private Label        wlAutoinc;
	private Button       wAutoinc;
	private FormData     fdlAutoinc, fdAutoinc;

	private Label        wlSeq;
	private Text         wSeq;
	private FormData     fdlSeq, fdSeq;

	private Label        wlVersion;
	private Text         wVersion;
	private FormData     fdlVersion, fdVersion;

	private Label        wlDatefield;
	private Text         wDatefield;
	private FormData     fdlDatefield, fdDatefield;

	private Label        wlFromdate;
	private Text         wFromdate;
	private FormData     fdlFromdate, fdFromdate;

	private Label        wlMinyear;
	private Text         wMinyear;
	private FormData     fdlMinyear, fdMinyear;

	private Label        wlTodate;
	private Text         wTodate;
	private FormData     fdlTodate, fdTodate;

	private Label        wlMaxyear;
	private Text         wMaxyear;
	private FormData     fdlMaxyear, fdMaxyear;

	private Label        wlUpdate;
	private Button       wUpdate;
	private FormData     fdlUpdate, fdUpdate;
	
	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

	private Label        wlUpIns;
	private TableView    wUpIns;
	private FormData     fdlUpIns, fdUpIns;

	private Button wGet, wCreate;
	private Listener lsGet, lsCreate;

	private DimensionLookupMeta input;
	private boolean backupUpdate, backupAutoInc;
	
	private DatabaseMeta ci;

	public DimensionLookupDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DimensionLookupMeta)in;
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
		backupChanged = input.hasChanged();
		backupUpdate = input.isUpdate();
		backupAutoInc = input.isAutoIncrement();
		ci = input.getDatabaseMeta();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Dimension Lookup / Update");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		int width  = Const.RIGHT;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText("Step name ");
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
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
		// START OF TABLE TAB///
		///
		wTableTab=new CTabItem(wTabFolder, SWT.NONE);
		wTableTab.setText("Table");
		
		Composite wTableComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wTableComp);

		FormLayout tableLayout = new FormLayout();
		tableLayout.marginWidth  = 3;
		tableLayout.marginHeight = 3;
		wTableComp.setLayout(tableLayout);

		// Connection line
		wConnection = addConnectionLine(wTableComp, null, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		wConnection.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				// We have new content: change ci connection:
				ci = transMeta.findDatabase(wConnection.getText());
				setAutoinc();
				setSequence();
			}
		});

		// Table line...
		wlTable=new Label(wTableComp, SWT.RIGHT);
		wlTable.setText("Target table ");
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wConnection, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(wTableComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText("&Browse...");
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wConnection, margin);
		fdTable.right= new FormAttachment(wbTable, 0);
		wTable.setLayoutData(fdTable);

		// Commit size ...
		wlCommit=new Label(wTableComp, SWT.RIGHT);
		wlCommit.setText("Commit size ");
 		props.setLook(wlCommit);
		fdlCommit=new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.right= new FormAttachment(middle, -margin);
		fdlCommit.top  = new FormAttachment(wTable, margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit=new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top  = new FormAttachment(wTable, margin);
		fdCommit.right= new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);
		
		// Technical key field:
		wlTk=new Label(wTableComp, SWT.RIGHT);
		wlTk.setText("Technical key field ");
 		props.setLook(wlTk);
		fdlTk=new FormData();
		fdlTk.left = new FormAttachment(0, 0);
		fdlTk.right= new FormAttachment(middle, -margin);
		fdlTk.top  = new FormAttachment(wCommit, margin);
		wlTk.setLayoutData(fdlTk);
		wTk=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTk);
		wTk.addModifyListener(lsMod);
		fdTk=new FormData();
		fdTk.left = new FormAttachment(middle, 0);
		fdTk.top  = new FormAttachment(wCommit, margin);
		fdTk.right= new FormAttachment(50+middle/2, 0);
		wTk.setLayoutData(fdTk);
		
		wlTkRename=new Label(wTableComp, SWT.RIGHT);
		wlTkRename.setText("New name ");
 		props.setLook(wlTkRename);
		fdlTkRename=new FormData();
		fdlTkRename.left = new FormAttachment(50+middle/2, margin);
		fdlTkRename.top  = new FormAttachment(wCommit, margin);
		wlTkRename.setLayoutData(fdlTkRename);
		wTkRename=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTkRename);
		wTkRename.addModifyListener(lsMod);
		fdTkRename=new FormData();
		fdTkRename.left = new FormAttachment(wlTkRename, margin);
		fdTkRename.top  = new FormAttachment(wCommit, margin);
		fdTkRename.right= new FormAttachment(100, 0);
		wTkRename.setLayoutData(fdTkRename);

		// Use an autoincrement field?
		wlAutoinc=new Label(wTableComp, SWT.RIGHT);
		wlAutoinc.setText("use auto increment field?");
 		props.setLook(wlAutoinc);
		fdlAutoinc=new FormData();
		fdlAutoinc.left = new FormAttachment(0, 0);
		fdlAutoinc.right= new FormAttachment(middle, -margin);
		fdlAutoinc.top  = new FormAttachment(wTk, margin);
		wlAutoinc.setLayoutData(fdlAutoinc);
		wAutoinc=new Button(wTableComp, SWT.CHECK);
 		props.setLook(wAutoinc);
		fdAutoinc=new FormData();
		fdAutoinc.left = new FormAttachment(middle, 0);
		fdAutoinc.top  = new FormAttachment(wTk, margin);
		fdAutoinc.right= new FormAttachment(100, 0);
		wAutoinc.setLayoutData(fdAutoinc);
		wAutoinc.setToolTipText("If this field is disabled, get the next value from the indicated sequence."+Const.CR+"If no sequence is supplied, Kettle will generate the appropriate keys");
		setAutoinc();
		// Clicking on update changes the options in the update combo boxes!		
		wAutoinc.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setAutoIncrement( !input.isAutoIncrement() );
					input.setChanged();
		
					setAutoinc();
				}
			}
		);


		// Sequence key field:
		wlSeq=new Label(wTableComp, SWT.RIGHT);
		wlSeq.setText("Optional Sequence ");
 		props.setLook(wlSeq);
		fdlSeq=new FormData();
		fdlSeq.left = new FormAttachment(0, 0);
		fdlSeq.right= new FormAttachment(middle, -margin);
		fdlSeq.top  = new FormAttachment(wlAutoinc, margin);
		wlSeq.setLayoutData(fdlSeq);
		wSeq=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSeq);
		wSeq.addModifyListener(lsMod);
		fdSeq=new FormData();
		fdSeq.left = new FormAttachment(middle, 0);
		fdSeq.top  = new FormAttachment(wlAutoinc, margin);
		fdSeq.right= new FormAttachment(100, 0);
		wSeq.setLayoutData(fdSeq);
		setSequence();

		// Version key field:
		wlVersion=new Label(wTableComp, SWT.RIGHT);
		wlVersion.setText("Version field ");
 		props.setLook(wlVersion);
		fdlVersion=new FormData();
		fdlVersion.left = new FormAttachment(0, 0);
		fdlVersion.right= new FormAttachment(middle, -margin);
		fdlVersion.top  = new FormAttachment(wSeq, margin);
		wlVersion.setLayoutData(fdlVersion);
		wVersion=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wVersion);
		wVersion.addModifyListener(lsMod);
		fdVersion=new FormData();
		fdVersion.left = new FormAttachment(middle, 0);
		fdVersion.top  = new FormAttachment(wSeq, margin);
		fdVersion.right= new FormAttachment(100, 0);
		wVersion.setLayoutData(fdVersion);

		// Datefield line
		wlDatefield=new Label(wTableComp, SWT.RIGHT);
		wlDatefield.setText("Stream Datefield ");
 		props.setLook(wlDatefield);
		fdlDatefield=new FormData();
		fdlDatefield.left = new FormAttachment(0, 0);
		fdlDatefield.right= new FormAttachment(middle, -margin);
		fdlDatefield.top  = new FormAttachment(wVersion, margin);
		wlDatefield.setLayoutData(fdlDatefield);
		wDatefield=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDatefield);
		wDatefield.addModifyListener(lsMod);
		fdDatefield=new FormData();
		fdDatefield.left = new FormAttachment(middle, 0);
		fdDatefield.top  = new FormAttachment(wVersion, margin);
		fdDatefield.right= new FormAttachment(100, 0);
		wDatefield.setLayoutData(fdDatefield);

		// Fromdate line
		//
		//  0 [wlFromdate] middle [wFromdate] (100-middle)/3 [wlMinyear] 2*(100-middle)/3 [wMinyear] 100%
		//
		wlFromdate=new Label(wTableComp, SWT.RIGHT);
		wlFromdate.setText("Date range start field ");
 		props.setLook(wlFromdate);
		fdlFromdate=new FormData();
		fdlFromdate.left = new FormAttachment(0, 0);
		fdlFromdate.right= new FormAttachment(middle, -margin);
		fdlFromdate.top  = new FormAttachment(wDatefield, margin);
		wlFromdate.setLayoutData(fdlFromdate);
		wFromdate=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFromdate);
		wFromdate.addModifyListener(lsMod);
		fdFromdate=new FormData();
		fdFromdate.left = new FormAttachment(middle, 0);
		fdFromdate.right= new FormAttachment(middle+(100-middle)/3, -margin);
		fdFromdate.top  = new FormAttachment(wDatefield, margin);
		wFromdate.setLayoutData(fdFromdate);

		// Minyear line
		wlMinyear=new Label(wTableComp, SWT.RIGHT);
		wlMinyear.setText("Min. year ");
 		props.setLook(wlMinyear);
		fdlMinyear=new FormData();
		fdlMinyear.left  = new FormAttachment(wFromdate, margin);
		fdlMinyear.right = new FormAttachment(middle+2*(100-middle)/3, -margin);
		fdlMinyear.top   = new FormAttachment(wDatefield, margin);
		wlMinyear.setLayoutData(fdlMinyear);
		wMinyear=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinyear);
		wMinyear.addModifyListener(lsMod);
		fdMinyear=new FormData();
		fdMinyear.left = new FormAttachment(wlMinyear, margin);
		fdMinyear.right= new FormAttachment(100, 0);
		fdMinyear.top  = new FormAttachment(wDatefield, margin);
		wMinyear.setLayoutData(fdMinyear);
		wMinyear.setToolTipText("Use date: yyyy/01/01 00:00:00.000 as the date range minimum.");

		// Todate line
		wlTodate=new Label(wTableComp, SWT.RIGHT);
		wlTodate.setText("Table daterange end ");
 		props.setLook(wlTodate);
		fdlTodate=new FormData();
		fdlTodate.left = new FormAttachment(0, 0);
		fdlTodate.right= new FormAttachment(middle, -margin);
		fdlTodate.top  = new FormAttachment(wFromdate, margin);
		wlTodate.setLayoutData(fdlTodate);
		wTodate=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTodate);
		wTodate.addModifyListener(lsMod);
		fdTodate=new FormData();
		fdTodate.left = new FormAttachment(middle, 0);
		fdTodate.right= new FormAttachment(middle+(100-middle)/3, -margin);
		fdTodate.top  = new FormAttachment(wFromdate, margin);
		wTodate.setLayoutData(fdTodate);

		// Maxyear line
		wlMaxyear=new Label(wTableComp, SWT.RIGHT);
		wlMaxyear.setText("Max. year ");
 		props.setLook(wlMaxyear);
		fdlMaxyear=new FormData();
		fdlMaxyear.left  = new FormAttachment(wTodate, margin);
		fdlMaxyear.right = new FormAttachment(middle+2*(100-middle)/3, -margin);
		fdlMaxyear.top   = new FormAttachment(wFromdate, margin);
		wlMaxyear.setLayoutData(fdlMaxyear);
		wMaxyear=new Text(wTableComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxyear);
		wMaxyear.addModifyListener(lsMod);
		fdMaxyear=new FormData();
		fdMaxyear.left = new FormAttachment(wlMaxyear, margin);
		fdMaxyear.right= new FormAttachment(100, 0);
		fdMaxyear.top  = new FormAttachment(wFromdate, margin);
		wMaxyear.setLayoutData(fdMaxyear);
		wMaxyear.setToolTipText("Use date: yyyy/12/31 23:59:59.999 as the date range minimum.");

		// Update the dimension?
		wlUpdate=new Label(wTableComp, SWT.RIGHT);
		wlUpdate.setText("Update dim?");
 		props.setLook(wlUpdate);
		fdlUpdate=new FormData();
		fdlUpdate.left = new FormAttachment(0, 0);
		fdlUpdate.right= new FormAttachment(middle, -margin);
		fdlUpdate.top  = new FormAttachment(wMaxyear, margin);
		wlUpdate.setLayoutData(fdlUpdate);
		wUpdate=new Button(wTableComp, SWT.CHECK);
 		props.setLook(wUpdate);
		fdUpdate=new FormData();
		fdUpdate.left = new FormAttachment(middle, 0);
		fdUpdate.right= new FormAttachment(0, middle+width);
		fdUpdate.top  = new FormAttachment(wMaxyear, margin);
		wUpdate.setLayoutData(fdUpdate);
		
		fdTableComp=new FormData();
		fdTableComp.left  = new FormAttachment(0, 0);
		fdTableComp.top   = new FormAttachment(0, 0);
		fdTableComp.right = new FormAttachment(100, 0);
		fdTableComp.bottom= new FormAttachment(100, 0);
		wTableComp.setLayoutData(fdTableComp);
	
		wTableComp.layout();
		wTableTab.setControl(wTableComp);

		/////////////////////////////////////////////////////////////
		/// END OF TABLE TAB
		/////////////////////////////////////////////////////////////


		//////////////////////////
		// START OF KEY TAB    ///
		///
		wKeyTab=new CTabItem(wTabFolder, SWT.NONE);
		wKeyTab.setText("Keys");

		FormLayout keyLayout = new FormLayout ();
		keyLayout.marginWidth  = 3;
		keyLayout.marginHeight = 3;
		
		Composite wKeyComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wKeyComp);
		wKeyComp.setLayout(keyLayout);


		// The Lookup fields: usualy the key
		//
		wlKey=new Label(wKeyComp, SWT.NONE);
		wlKey.setText("Key fields (to look up row in dimension): ");
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(0, margin);
		fdlKey.right = new FormAttachment(100, 0);
		wlKey.setLayoutData(fdlKey);
		
		int nrKeyCols=2;
		int nrKeyRows=(input.getKeyStream()!=null?input.getKeyStream().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo("Dimension field",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciKey[1]=new ColumnInfo("Field in stream",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
		wKey=new TableView(wKeyComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(100, 0);
		wKey.setLayoutData(fdKey);

		fdKeyComp = new FormData();
		fdKeyComp.left  = new FormAttachment(0, 0);
		fdKeyComp.top   = new FormAttachment(0, 0);
		fdKeyComp.right = new FormAttachment(100, 0);
		fdKeyComp.bottom= new FormAttachment(100, 0);
		wKeyComp.setLayoutData(fdKeyComp);

		wKeyComp.layout();
		wKeyTab.setControl(wKeyComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF KEY TAB
		/////////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText("Fields");
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);

		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(" &Get Fields ");

		setButtonPositions(new Button[] { wGet }, margin, null);
		
		// THE UPDATE/INSERT TABLE
		wlUpIns=new Label(wFieldsComp, SWT.NONE);
		wlUpIns.setText("Lookup/Update fields ");
 		props.setLook(wlUpIns);
		fdlUpIns=new FormData();
		fdlUpIns.left  = new FormAttachment(0, 0);
		fdlUpIns.top   = new FormAttachment(0, margin);
		wlUpIns.setLayoutData(fdlUpIns);
		
		int UpInsCols=3;
		int UpInsRows= (input.getFieldStream()!=null?input.getFieldStream().length:1);
		
		final ColumnInfo[] ciUpIns=new ColumnInfo[UpInsCols];
		ciUpIns[0]=new ColumnInfo("Dimension field",              ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciUpIns[1]=new ColumnInfo("Stream field",                 ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciUpIns[2]=new ColumnInfo("Type of dimension update",     ColumnInfo.COLUMN_TYPE_CCOMBO, input.isUpdate()?DimensionLookupMeta.typeDesc:DimensionLookupMeta.typeDescLookup );
		
		wUpIns=new TableView(wFieldsComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciUpIns, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		fdUpIns=new FormData();
		fdUpIns.left  = new FormAttachment(0, 0);
		fdUpIns.top   = new FormAttachment(wlUpIns, margin);
		fdUpIns.right = new FormAttachment(100, 0);
		fdUpIns.bottom= new FormAttachment(wGet, -margin);
		wUpIns.setLayoutData(fdUpIns);
		setUpdate();
		
		// Clicking on update changes the options in the update combo boxes!		
		wUpdate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setUpdate(!input.isUpdate());
					input.setChanged();
		
					setUpdate();
				}
			}
		);

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
		


		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(" &SQL ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wCreate, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { create(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
		wCommit.addSelectionListener( lsDef );
		wTk.addSelectionListener( lsDef );
		wTkRename.addSelectionListener( lsDef );
		wSeq.addSelectionListener( lsDef );
		wVersion.addSelectionListener( lsDef );
		wDatefield.addSelectionListener( lsDef );
		wFromdate.addSelectionListener( lsDef );
		wMinyear.addSelectionListener( lsDef );
		wTodate.addSelectionListener( lsDef );
		wMaxyear.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);

		wTabFolder.setSelection(0);
		
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
	
	public void setAutoinc()
	{
		boolean enable= ci==null || ci.supportsAutoinc();
		wlAutoinc.setEnabled(enable);
		wAutoinc.setEnabled(enable);
	}

	public void setSequence()
	{
		boolean seq = ci==null || ci.supportsSequences();
		wlSeq.setEnabled(seq);
		wSeq.setEnabled(seq);
	}
	
	public void setUpdate()
	{
		ColumnInfo colinf =new ColumnInfo("Type",      ColumnInfo.COLUMN_TYPE_CCOMBO, 
			  input.isUpdate()?
				 DimensionLookupMeta.typeDesc:
				 DimensionLookupMeta.typeDescLookup 
		);
		wUpIns.setColumnInfo(2, colinf);

		if (input.isUpdate())
		{
			wUpIns.setColumnText(2, "Stream field to compare with");
			wUpIns.setColumnText(3, "Type of dimension update");
			wUpIns.setColumnToolTip(2, "Insert: Kimball Type II"+Const.CR+"Punch Through: Kimball Type I"+Const.CR+"Update: Correct error in last version");
		}
		else
		{
			wUpIns.setColumnText(2, "New name of output field");
			wUpIns.setColumnText(3, "Type of return field");
			wUpIns.setColumnToolTip(2, "Select the type of the value to return.");
		}
		wUpIns.optWidth(true);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		log.logDebug(toString(), "getting key info...");
		
		if (input.getKeyStream()!=null)
		for (i=0;i<input.getKeyStream().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyLookup()[i]!=null) item.setText(1, input.getKeyLookup()[i]);
			if (input.getKeyStream()[i]!=null) item.setText(2, input.getKeyStream()[i]);
		}
		
		if (input.getFieldStream()!=null)
		for (i=0;i<input.getFieldStream().length;i++)
		{
			TableItem item = wUpIns.table.getItem(i);
			if (input.getFieldLookup()[i]!=null) item.setText(1, input.getFieldLookup()[i]);
			if (input.getFieldStream()[i]!=null) item.setText(2, input.getFieldStream()[i]);
			item.setText(3, DimensionLookupMeta.getUpdateType(input.isUpdate(), input.getFieldUpdate()[i]) );
		}
		
		wUpdate.setSelection( input.isUpdate() );
		
		if (input.getTableName()!=null)       wTable.setText( input.getTableName() );
		if (input.getKeyField()!=null)        wTk.setText(input.getKeyField());
		if (input.getKeyRename()!=null)       wTkRename.setText(input.getKeyRename());
		
		wAutoinc.setSelection( input.isAutoIncrement() );
		
		if (input.getVersionField()!=null)    wVersion.setText(input.getVersionField());
		if (input.getSequenceName()!=null)        wSeq.setText(input.getSequenceName());
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getDateField()!=null)    wDatefield.setText(input.getDateField());
		if (input.getDateFrom()!=null)     wFromdate.setText(input.getDateFrom());
		if (input.getDateTo()!=null)       wTodate.setText(input.getDateTo());
		
		wCommit.setText(""+input.getCommitSize());
		
		wMinyear.setText(""+input.getMinYear());
		wMaxyear.setText(""+input.getMaxYear());

		wUpIns.removeEmptyRows();
		wUpIns.setRowNums();
		wUpIns.optWidth(true);
		wKey.removeEmptyRows();
		wKey.setRowNums();
		wKey.optWidth(true);

		ci = transMeta.findDatabase(wConnection.getText());
		setAutoinc();
		setSequence();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		input.setUpdate( backupUpdate );
		input.setAutoIncrement( backupAutoInc );
		dispose();
	}
	
	private void ok()
	{
		getInfo(input);

		stepname = wStepname.getText(); // return value

		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
	}
	
	private void getInfo(DimensionLookupMeta in)
	{
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wUpIns.nrNonEmpty();
		
		in.allocate(nrkeys, nrfields);

		log.logDebug(toString(), "Found "+nrkeys+" keys");
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			in.getKeyLookup()[i] = item.getText(1);
			in.getKeyStream()[i] = item.getText(2);
		}

		log.logDebug(toString(), "Found "+nrfields+" fields");
		for (int i=0;i<nrfields;i++)
		{
			TableItem item        = wUpIns.getNonEmpty(i);
			in.getFieldLookup()[i]  = item.getText(1);
			in.getFieldStream()[i]  = item.getText(2);
			in.getFieldUpdate()[i]  = DimensionLookupMeta.getUpdateType(in.isUpdate(), item.getText(3));
		}
		
		in.setTableName( wTable.getText() ); 
		in.setKeyField( wTk.getText() );
		in.setKeyRename( wTkRename.getText() );
		in.setAutoIncrement( wAutoinc.getSelection() );
		
		if (in.getKeyRename()!=null && in.getKeyRename().equalsIgnoreCase(in.getKeyField()))
			in.setKeyRename( null ); // Don't waste space&time if it's the same
		
		in.setVersionField( wVersion.getText() );
		in.setSequenceName( wSeq.getText() );
		in.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		in.setDateField( wDatefield.getText() );
		in.setDateFrom( wFromdate.getText() );
		in.setDateTo( wTodate.getText() );
		
		in.setUpdate( wUpdate.getSelection() );

		in.setCommitSize( Const.toInt(wCommit.getText(), 0) );
		in.setMinYear( Const.toInt(wMinyear.getText(), Const.MIN_YEAR) );
		in.setMaxYear( Const.toInt(wMaxyear.getText(), Const.MAX_YEAR) );
	}

	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		DatabaseMeta inf = transMeta.getDatabase(connr);
					
		log.logDebug(toString(), "Looking at connection: "+inf.toString());
	
		DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, inf, transMeta.getDatabases());
		std.setSelectedTable(wTable.getText());
		String tableName = (String)std.open();
		if (tableName != null)
		{
			wTable.setText(tableName);
		}
	}

	private void get()
	{
		if (input.isUpdate()) getUpdate();
		else getLookup();
	}
	
	private void getUpdate()
	{
		try
		{
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wUpIns.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					int idx = wKey.indexOfString(v.getName(), 2);
					if (idx<0)
					{
						TableItem ti = new TableItem(table, SWT.NONE);
						ti.setText(1, v.getName());
						ti.setText(2, v.getName());
						ti.setText(3, "Insert");
					}
				}
				wUpIns.removeEmptyRows();
				wUpIns.setRowNums();
				wUpIns.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}
	}

	private void getLookup()
	{
		DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
		if (ci!=null)
		{
			Database db = new Database(ci);
			try
			{
				db.connect();
				Row r = db.getTableFields(wTable.getText());
				if (r!=null)
				{
					Table table=wUpIns.table;
					for (int i=0;i<r.size();i++)
					{
						Value v = r.getValue(i);
						int idx = wKey.indexOfString(v.getName(), 2);
						if (idx<0 && 
							!v.getName().equalsIgnoreCase(wTk.getText()) &&
							!v.getName().equalsIgnoreCase(wVersion.getText()) &&
							!v.getName().equalsIgnoreCase(wFromdate.getText()) &&
							!v.getName().equalsIgnoreCase(wTodate.getText())
							)
						{
							TableItem ti = new TableItem(table, SWT.NONE);
							ti.setText(1, v.getName());
							ti.setText(2, v.getName());
							ti.setText(3, v.getTypeDesc());
						}
					}
					wUpIns.removeEmptyRows();
					wUpIns.setRowNums();
					wUpIns.optWidth(true);
				}
			}
			catch(KettleException e)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setText("ERROR!");
				mb.setMessage("An error occurred: "+Const.CR+e.getMessage());
				mb.open(); 
			}
			finally
			{
				db.disconnect();
			}
		}
	}
	
	// Generate code for create table...
	// Conversions done by Database
	// For Sybase ASE: don't keep everything in lowercase!
	private void create()
	{
		try
		{
			DimensionLookupMeta info = new DimensionLookupMeta();
			getInfo(info);
			
			String name = stepname;  // new name might not yet be linked to other steps! 
			StepMeta stepinfo = new StepMeta(log, "CombinationLookup", name, info);
			Row prev = transMeta.getPrevStepFields(stepname);
	
			SQLStatement sql = info.getSQLStatements(transMeta, stepinfo, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage("No SQL needs to be executed to make this step function properly.");
					mb.setText("OK");
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText("ERROR");
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Couldn't build SQL", "Unable to build the SQL statement because of an error", ke);
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}
