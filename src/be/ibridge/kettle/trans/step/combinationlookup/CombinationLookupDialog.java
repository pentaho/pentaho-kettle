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
 */

package be.ibridge.kettle.trans.step.combinationlookup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
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


public class CombinationLookupDialog extends BaseStepDialog implements StepDialogInterface
{
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

	private Group        gTechGroup;
	private FormData     fdTechGroup;
	
	private Label        wlAutoinc;
	private Button       wAutoinc;
	private GridData     gdlAutoinc, gdAutoinc;

	private Label        wlTableMax;
	private Button       wTableMax;
	private GridData     gdlTableMax, gdTableMax;	

	private Label        wlSeqButton;
	private Button       wSeqButton;
	private GridData     gdlSeqButton, gdSeqButton, gdSeq;			
	private Text         wSeq;     

	private Label        wlReplace;
	private Button       wReplace;
	private FormData     fdlReplace, fdReplace;

	private Label        wlHashcode;
	private Button       wHashcode;
	private FormData     fdlHashcode, fdHashcode;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

	private Label        wlHashfield;
	private Text         wHashfield;
	private FormData     fdlHashfield, fdHashfield;

	private Button       wGet, wCreate;
	private Listener     lsGet, lsCreate;	

	private CombinationLookupMeta input;

	private DatabaseMeta ci;

	public CombinationLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(CombinationLookupMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("CombinationLookupDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		ci = input.getDatabase();

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("CombinationLookupDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		wConnection.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					// We have new content: change ci connection:
					ci = transMeta.findDatabase(wConnection.getText());
					setAutoincUse();
					setSequence();
					input.setChanged();
				}
			}
		);
		
		// Table line...
		wlTable = new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("CombinationLookupDialog.Target.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wConnection, margin * 2);
		wlTable.setLayoutData(fdlTable);

		wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
 		wbTable.setText(Messages.getString("CombinationLookupDialog.BrowseTable.Button"));
		fdbTable = new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

		wTable = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable = new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top = new FormAttachment(wConnection, margin * 2);
		fdTable.right = new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);		
		
		// Commit size ...
		wlCommit=new Label(shell, SWT.RIGHT);
		wlCommit.setText(Messages.getString("CombinationLookupDialog.Commitsize.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit=new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.right= new FormAttachment(middle, -margin);
		fdlCommit.top  = new FormAttachment(wTable, margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit=new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top  = new FormAttachment(wTable, margin);
		fdCommit.right= new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

		//
		// The Lookup fields: usually the (business) key
		//
		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("CombinationLookupDialog.Keyfields.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wCommit, margin);
		fdlKey.right = new FormAttachment(100, 0);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=2;
		int nrKeyRows=(input.getKeyField()!=null?input.getKeyField().length:1);

		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("CombinationLookupDialog.ColumnInfo.DimensionField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("CombinationLookupDialog.ColumnInfo.FieldInStream"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

		wKey=new TableView(shell,
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
						      ciKey,
						      nrKeyRows,
						      lsMod,
							  props
						      );

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("CombinationLookupDialog.OK.Button")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("CombinationLookupDialog.GetFields.Button")); //$NON-NLS-1$
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(Messages.getString("CombinationLookupDialog.SQL.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wGet, wCreate, wCancel }, margin, null);

		// Technical key field:
		wlHashfield=new Label(shell, SWT.RIGHT);
		wlHashfield.setText(Messages.getString("CombinationLookupDialog.Hashfield.Label")); //$NON-NLS-1$
 		props.setLook(wlHashfield);
		fdlHashfield=new FormData();
		fdlHashfield.left  = new FormAttachment(0, 0);
		fdlHashfield.right = new FormAttachment(middle, -margin);
		fdlHashfield.bottom= new FormAttachment(wOK, -2*margin);
		wlHashfield.setLayoutData(fdlHashfield);
		wHashfield=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wHashfield);
		wHashfield.addModifyListener(lsMod);
		fdHashfield=new FormData();
		fdHashfield.left  = new FormAttachment(middle, 0);
		fdHashfield.right = new FormAttachment(100, 0);
		fdHashfield.bottom= new FormAttachment(wOK, -2*margin);
		wHashfield.setLayoutData(fdHashfield);

		// Output the input rows or one (1) log-record?
		wlHashcode=new Label(shell, SWT.RIGHT);
		wlHashcode.setText(Messages.getString("CombinationLookupDialog.Hashcode.Label")); //$NON-NLS-1$
 		props.setLook(wlHashcode);
		fdlHashcode=new FormData();
		fdlHashcode.left  = new FormAttachment(0, 0);
		fdlHashcode.right = new FormAttachment(middle, -margin);
		fdlHashcode.bottom= new FormAttachment(wHashfield, -margin);
		wlHashcode.setLayoutData(fdlHashcode);
		wHashcode=new Button(shell, SWT.CHECK);
 		props.setLook(wHashcode);
		fdHashcode=new FormData();
		fdHashcode.left   = new FormAttachment(middle, 0);
		fdHashcode.right  = new FormAttachment(100, 0);
		fdHashcode.bottom = new FormAttachment(wHashfield, -margin);
		wHashcode.setLayoutData(fdHashcode);
		wHashcode.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					enableFields();					
				}
			}
		);

		// Replace lookup fields in the output stream?
		wlReplace=new Label(shell, SWT.RIGHT);
		wlReplace.setText(Messages.getString("CombinationLookupDialog.Replace.Label")); //$NON-NLS-1$
 		props.setLook(wlReplace);
		fdlReplace=new FormData();
		fdlReplace.left  = new FormAttachment(0, 0);
		fdlReplace.right = new FormAttachment(middle, -margin);
		fdlReplace.bottom= new FormAttachment(wHashcode, -margin);
		wlReplace.setLayoutData(fdlReplace);
		wReplace=new Button(shell, SWT.CHECK);
 		props.setLook(wReplace);
		fdReplace=new FormData();
		fdReplace.left  = new FormAttachment(middle, 0);
		fdReplace.bottom= new FormAttachment(wHashcode, -margin);
		fdReplace.right = new FormAttachment(100, 0);
		wReplace.setLayoutData(fdReplace);
		wReplace.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					enableFields();
				}
			}
		);

		gTechGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
		gTechGroup.setText(Messages.getString("CombinationLookupDialog.TechGroup.Label")); //$NON-NLS-1$;
		GridLayout gridLayout = new GridLayout(3, false);
		gTechGroup.setLayout(gridLayout);
		fdTechGroup=new FormData();
		fdTechGroup.left   = new FormAttachment(middle, 0);
		fdTechGroup.bottom = new FormAttachment(wReplace, -margin);
		fdTechGroup.right  = new FormAttachment(100, 0);
		gTechGroup.setBackground(shell.getBackground()); // the default looks ugly
		gTechGroup.setLayoutData(fdTechGroup);

		// Use maximum of table + 1
		wTableMax=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wTableMax);
 		wTableMax.setSelection(false);
		gdTableMax=new GridData();
		wTableMax.setLayoutData(gdTableMax);
		wTableMax.setToolTipText(Messages.getString("CombinationLookupDialog.TableMaximum.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		wlTableMax=new Label(gTechGroup, SWT.LEFT);
		wlTableMax.setText(Messages.getString("CombinationLookupDialog.TableMaximum.Label")); //$NON-NLS-1$
 		props.setLook(wlTableMax);
		gdlTableMax = new GridData(GridData.FILL_BOTH);
		gdlTableMax.horizontalSpan = 2; gdlTableMax.verticalSpan = 1;
		wlTableMax.setLayoutData(gdlTableMax);
		
		// Sequence Check Button
		wSeqButton=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wSeqButton);
 		wSeqButton.setSelection(false);
		gdSeqButton=new GridData();
		wSeqButton.setLayoutData(gdSeqButton);
		wSeqButton.setToolTipText(Messages.getString("CombinationLookupDialog.Sequence.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$		
		wlSeqButton=new Label(gTechGroup, SWT.LEFT);
		wlSeqButton.setText(Messages.getString("CombinationLookupDialog.Sequence.Label")); //$NON-NLS-1$
 		props.setLook(wlSeqButton); 	
		gdlSeqButton=new GridData();
		wlSeqButton.setLayoutData(gdlSeqButton);

		wSeq=new Text(gTechGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSeq);
		wSeq.addModifyListener(lsMod);
		gdSeq=new GridData(GridData.FILL_HORIZONTAL);
		wSeq.setLayoutData(gdSeq);
		wSeq.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				input.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_SEQUENCE);
				wSeqButton.setSelection(true);
				wAutoinc.setSelection(false);
				wTableMax.setSelection(false);				
			}

			public void focusLost(FocusEvent arg0) {
			} 
		});		
		
		// Use an autoincrement field?
		wAutoinc=new Button(gTechGroup, SWT.RADIO);
 		props.setLook(wAutoinc);
 		wAutoinc.setSelection(false);
		gdAutoinc=new GridData();
		wAutoinc.setLayoutData(gdAutoinc);
		wAutoinc.setToolTipText(Messages.getString("CombinationLookupDialog.AutoincButton.Tooltip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
		wlAutoinc=new Label(gTechGroup, SWT.LEFT);
		wlAutoinc.setText(Messages.getString("CombinationLookupDialog.Autoincrement.Label")); //$NON-NLS-1$
 		props.setLook(wlAutoinc);
		gdlAutoinc=new GridData();
		wlAutoinc.setLayoutData(gdlAutoinc);

		setTableMax();
		setSequence();
		setAutoincUse();
		
		// Technical key field:
		wlTk=new Label(shell, SWT.RIGHT);
		wlTk.setText(Messages.getString("CombinationLookupDialog.TechnicalKey.Label")); //$NON-NLS-1$
 		props.setLook(wlTk);
		fdlTk=new FormData();
		fdlTk.left   = new FormAttachment(0, 0);
		fdlTk.right  = new FormAttachment(middle, -margin);
		fdlTk.bottom = new FormAttachment(gTechGroup, -margin);
		wlTk.setLayoutData(fdlTk);
		wTk=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTk);
		fdTk=new FormData();
		fdTk.left   = new FormAttachment(middle, 0);
		fdTk.bottom = new FormAttachment(gTechGroup, -margin);
		fdTk.right  = new FormAttachment(100, 0);
		wTk.setLayoutData(fdTk);

		fdKey=new FormData();
		fdKey.left  = new FormAttachment(0, 0);
		fdKey.top   = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, 0);
		fdKey.bottom= new FormAttachment(wTk, -margin);
		wKey.setLayoutData(fdKey);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();         } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();        } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { create();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };

		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener( lsDef );

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

	public void enableFields()
	{
		wHashfield.setEnabled(wHashcode.getSelection());
		wHashfield.setVisible(wHashcode.getSelection());
		wlHashfield.setEnabled(wHashcode.getSelection());
	}

	public void setAutoincUse()
	{
		boolean enable = (ci == null) || ci.supportsAutoinc();
		wlAutoinc.setEnabled(enable);
		wAutoinc.setEnabled(enable);
		if ( enable == false && 
			 wAutoinc.getSelection() == true )
		{
			wAutoinc.setSelection(false);
			wSeqButton.setSelection(false);
			wTableMax.setSelection(true);
		}		
	}

	public void setTableMax()
	{
		wlTableMax.setEnabled(true);
		wTableMax.setEnabled(true);
	}
	
	public void setSequence()
	{
		boolean seq = (ci == null) || ci.supportsSequences();
		wSeq.setEnabled(seq);
		wlSeqButton.setEnabled(seq);
		wSeqButton.setEnabled(seq);
		if ( seq == false && 
			 wSeqButton.getSelection() == true ) 
		{
		    wAutoinc.setSelection(false);
			wSeqButton.setSelection(false);
			wTableMax.setSelection(true);
		}		
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		log.logDebug(toString(), Messages.getString("CombinationLookupDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getKeyField()!=null)
		for (i=0;i<input.getKeyField().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyLookup()[i]!=null) item.setText(1, input.getKeyLookup()[i]);
			if (input.getKeyField()[i]!=null)  item.setText(2, input.getKeyField()[i]);			
		}

		wReplace.setSelection( input.replaceFields() );
		wHashcode.setSelection( input.useHash() );
		wHashfield.setEnabled(input.useHash());
		wHashfield.setVisible(input.useHash());
		wlHashfield.setEnabled(input.useHash());
		
		String techKeyCreation = input.getTechKeyCreation(); 
		if ( techKeyCreation == null )  {		    
		    // Determine the creation of the technical key for
			// backwards compatibility. Can probably be removed at
			// version 3.x or so (Sven Boden).
		    DatabaseMeta database = input.getDatabase(); 
		    if ( database == null || ! database.supportsAutoinc() )  
		    {
 			    input.setUseAutoinc(false);			
		    }		
		    wAutoinc.setSelection(input.isUseAutoinc());
		    
		    wSeqButton.setSelection(input.getSequenceFrom() != null && input.getSequenceFrom().length() > 0);
		    if ( input.isUseAutoinc() == false && 
			     (input.getSequenceFrom() == null || input.getSequenceFrom().length() <= 0) ) 
		    {
 			    wTableMax.setSelection(true); 			    
		    }
		    
			if ( database != null && database.supportsSequences() && 
				 input.getSequenceFrom() != null) 
			{
				wSeq.setText(input.getSequenceFrom());
				input.setUseAutoinc(false);
				wTableMax.setSelection(false);
			}
		}
		else
		{
		    // KETTLE post 2.2 version:
			// The "creation" field now determines the behaviour of the
			// key creation.
			if ( CombinationLookupMeta.CREATION_METHOD_AUTOINC.equals(techKeyCreation))  
			{
			    wAutoinc.setSelection(true);
			}
			else if ( ( CombinationLookupMeta.CREATION_METHOD_SEQUENCE.equals(techKeyCreation)) )
			{
				wSeqButton.setSelection(true);
			}
			else // the rest
			{
				wTableMax.setSelection(true);
				input.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_TABLEMAX);
			}
			if ( input.getSequenceFrom() != null )
			{
    	        wSeq.setText(input.getSequenceFrom());
			}
		}
		setAutoincUse();
		setSequence();
		setTableMax();
  		if (input.getTablename()!=null)         wTable.setText( input.getTablename() );
		if (input.getTechnicalKeyField()!=null) wTk.setText(input.getTechnicalKeyField());

		if (input.getDatabase()!=null) wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
		if (input.getHashField()!=null)    wHashfield.setText(input.getHashField());

		wCommit.setText(""+input.getCommitSize()); //$NON-NLS-1$

		wKey.setRowNums();
		wKey.optWidth(true);

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}

	private void ok()
	{
		CombinationLookupMeta oldMetaState = (CombinationLookupMeta)input.clone();
		
		getInfo(input);
		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("CombinationLookupDialog.NoValidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("CombinationLookupDialog.NoValidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		if ( ! input.equals(oldMetaState) )  
		{
			input.setChanged();
		}
		dispose();
	}

	private void getInfo(CombinationLookupMeta in)
	{
		int nrkeys         = wKey.nrNonEmpty();

		in.allocate(nrkeys);

		log.logDebug(toString(), Messages.getString("CombinationLookupDialog.Log.SomeKeysFound",String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			in.getKeyLookup()[i] = item.getText(1);
			in.getKeyField()[i]  = item.getText(2);			
		}

		in.setUseAutoinc( wAutoinc.getSelection() && wAutoinc.isEnabled() );
		in.setReplaceFields( wReplace.getSelection() );
		in.setUseHash( wHashcode.getSelection() );
		in.setHashField( wHashfield.getText() );
		in.setTablename( wTable.getText() );
		in.setTechnicalKeyField( wTk.getText() );
		if ( wAutoinc.getSelection() == true )  
		{
			in.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_AUTOINC);
			in.setUseAutoinc( true );   // for downwards compatibility
			in.setSequenceFrom( null );
		}
		else if ( wSeqButton.getSelection() == true )
		{
			in.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_SEQUENCE);
			in.setUseAutoinc(false);
			in.setSequenceFrom( wSeq.getText() );
		}
		else  // all the rest
		{
			in.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_TABLEMAX);
			in.setUseAutoinc( false );
			in.setSequenceFrom( null );
		}
		
		in.setDatabase( transMeta.findDatabase(wConnection.getText()) );

		in.setCommitSize( Const.toInt(wCommit.getText(), 0) );
	}

	private void getTableName()
	{
		DatabaseMeta inf = null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr >= 0)
			inf = transMeta.getDatabase(connr);

		if (inf != null)
		{
			log.logDebug(toString(), Messages.getString("CombinationLookupDialog.Log.LookingAtConnection", inf.toString()));

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, props, SWT.NONE, inf, transMeta.getDatabases());
			std.setSelectedTable(wTable.getText());
			String tableName = (String) std.open();
			if (tableName != null)
			{
				wTable.setText(tableName);
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("CombinationLookupDialog.ConnectionError2.DialogMessage"));
			mb.setText(Messages.getString("System.Dialog.Error.Title"));
			mb.open();
		}
	}	

	private void get()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wKey.table;
				count=table.getItemCount();
				for (i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, String.valueOf((count+i+1))); //$NON-NLS-1$
					ti.setText(1, v.getName());
					ti.setText(2, v.getName());
					ti.setText(3, "N"); //$NON-NLS-1$
				}
				wKey.removeEmptyRows();
				wKey.setRowNums();
				wKey.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, Messages.getString("CombinationLookupDialog.UnableToGetFieldsError.DialogTitle"), Messages.getString("CombinationLookupDialog.UnableToGetFieldsError.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/** 
	 *  Generate code for create table. Conversions done by database.
	 */
	private void create()
	{
		try
		{
			// Gather info...
			CombinationLookupMeta info = new CombinationLookupMeta();
			getInfo(info);
			String name = stepname;  // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(log, Messages.getString("CombinationLookupDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			Row prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabase(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage(Messages.getString("CombinationLookupDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(Messages.getString("CombinationLookupDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("CombinationLookupDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, Messages.getString("CombinationLookupDialog.UnableToCreateSQL.DialogTitle"), Messages.getString("CombinationLookupDialog.UnableToCreateSQL.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public String toString()
	{
		return this.getClass().getName();
	}
}