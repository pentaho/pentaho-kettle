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

package be.ibridge.kettle.trans.step.tableoutput;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.dialog.DatabaseExplorerDialog;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.dialog.SQLEditor;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class TableOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlTable;
	private Button       wbTable, wbcTable;
	private Text         wTable;
	private FormData     fdlTable, fdbTable, fdbcTable, fdTable;

	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;

	private Label        wlTruncate;
	private Button       wTruncate;
	private FormData     fdlTruncate, fdTruncate;

	private Label        wlIgnore;
	private Button       wIgnore;
	private FormData     fdlIgnore, fdIgnore;
	
	private Label        wlBatch;
	private Button       wBatch;
	private FormData     fdlBatch, fdBatch;
    
    private Label        wlUsePart;
    private Button       wUsePart;
    private FormData     fdlUsePart, fdUsePart;
	
    private Label        wlPartField;
    private Text         wPartField;
    private FormData     fdlPartField, fdPartField;

    private Label        wlPartMonthly;
    private Button       wPartMonthly;
    private FormData     fdlPartMonthly, fdPartMonthly;

    private Label        wlPartDaily;
    private Button       wPartDaily;
    private FormData     fdlPartDaily, fdPartDaily;

    private Label        wlNameInField;
    private Button       wNameInField;
    private FormData     fdlNameInField, fdNameInField;

    private Label        wlNameField;
    private Text         wNameField;
    private FormData     fdlNameField, fdNameField;
	
    private Label        wlNameInTable;
    private Button       wNameInTable;
    private FormData     fdlNameInTable, fdNameInTable;

    
    private TableOutputMeta input;
	
	public TableOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(TableOutputMeta)in;
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
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Table output");
		
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText("Target table ");
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wConnection, margin*2);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText("&Browse...");
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wConnection, margin);
		wbTable.setLayoutData(fdbTable);

        wbcTable=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbcTable);
        wbcTable.setText("&Variable...");
        fdbcTable=new FormData();
        fdbcTable.right= new FormAttachment(wbTable, 0);
        fdbcTable.top  = new FormAttachment(wConnection, margin);
        wbcTable.setLayoutData(fdbcTable);

		wTable=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		fdTable=new FormData();
		fdTable.top  = new FormAttachment(wConnection, margin*2);
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.right= new FormAttachment(wbcTable, -margin);
		wTable.setLayoutData(fdTable);

		// Commit size ...
		wlCommit=new Label(shell, SWT.RIGHT);
		wlCommit.setText("Commit size ");
 		props.setLook(wlCommit);
		fdlCommit=new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.right= new FormAttachment(middle, -margin);
		fdlCommit.top  = new FormAttachment(wbTable, margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit=new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top  = new FormAttachment(wbTable, margin);
		fdCommit.right= new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);

		// Truncate table
		wlTruncate=new Label(shell, SWT.RIGHT);
		wlTruncate.setText("Truncate table ");
 		props.setLook(wlTruncate);
		fdlTruncate=new FormData();
		fdlTruncate.left  = new FormAttachment(0, 0);
		fdlTruncate.top   = new FormAttachment(wCommit, margin);
		fdlTruncate.right = new FormAttachment(middle, -margin);
		wlTruncate.setLayoutData(fdlTruncate);
		wTruncate=new Button(shell, SWT.CHECK);
 		props.setLook(wTruncate);
		fdTruncate=new FormData();
		fdTruncate.left  = new FormAttachment(middle, 0);
		fdTruncate.top   = new FormAttachment(wCommit, margin);
		fdTruncate.right = new FormAttachment(100, 0);
		wTruncate.setLayoutData(fdTruncate);
		SelectionAdapter lsSelMod = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
		wTruncate.addSelectionListener(lsSelMod);

		// Ignore errors
		wlIgnore=new Label(shell, SWT.RIGHT);
		wlIgnore.setText("Ignore insert errors ");
 		props.setLook(wlIgnore);
		fdlIgnore=new FormData();
		fdlIgnore.left  = new FormAttachment(0, 0);
		fdlIgnore.top   = new FormAttachment(wTruncate, margin);
		fdlIgnore.right = new FormAttachment(middle, -margin);
		wlIgnore.setLayoutData(fdlIgnore);
		wIgnore=new Button(shell, SWT.CHECK);
 		props.setLook(wIgnore);
		fdIgnore=new FormData();
		fdIgnore.left  = new FormAttachment(middle, 0);
		fdIgnore.top   = new FormAttachment(wTruncate, margin);
		fdIgnore.right = new FormAttachment(100, 0);
		wIgnore.setLayoutData(fdIgnore);
		wIgnore.addSelectionListener(lsSelMod);

		// UsePart update
		wlBatch=new Label(shell, SWT.RIGHT);
		wlBatch.setText("Use batch update for inserts");
 		props.setLook(wlBatch);
		fdlBatch=new FormData();
		fdlBatch.left  = new FormAttachment(0, 0);
		fdlBatch.top   = new FormAttachment(wIgnore, margin);
		fdlBatch.right = new FormAttachment(middle, -margin);
		wlBatch.setLayoutData(fdlBatch);
		wBatch=new Button(shell, SWT.CHECK);
 		props.setLook(wBatch);
		fdBatch=new FormData();
		fdBatch.left  = new FormAttachment(middle, 0);
		fdBatch.top   = new FormAttachment(wIgnore, margin);
		fdBatch.right = new FormAttachment(100, 0);
		wBatch.setLayoutData(fdBatch);
		wBatch.addSelectionListener(lsSelMod);
		
		wBatch.addSelectionListener(
		    new SelectionAdapter()
	        {
	            public void widgetSelected(SelectionEvent arg0)
	            {
	                setFlags();
	            }
	        }
		);


        
        // Partitioning support
        
        // Use partitioning?
        wlUsePart=new Label(shell, SWT.RIGHT);
        wlUsePart.setText("Partition data over tables");
        wlUsePart.setToolTipText("Moves data into table TABLENAME_YYYYMMDD or TABLENAME_YYYYMM based on the partitioning field"+Const.CR+"For example, SALES_200503, SALES_200504, ...");
        props.setLook(wlUsePart);
        fdlUsePart=new FormData();
        fdlUsePart.left  = new FormAttachment(0, 0);
        fdlUsePart.top   = new FormAttachment(wBatch, margin*5);
        fdlUsePart.right = new FormAttachment(middle, -margin);
        wlUsePart.setLayoutData(fdlUsePart);
        wUsePart=new Button(shell, SWT.CHECK);
        props.setLook(wUsePart);
        fdUsePart=new FormData();
        fdUsePart.left  = new FormAttachment(middle, 0);
        fdUsePart.top   = new FormAttachment(wBatch, margin*5);
        fdUsePart.right = new FormAttachment(100, 0);
        wUsePart.setLayoutData(fdUsePart);
        wUsePart.addSelectionListener(lsSelMod);
        
        wUsePart.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    if (wUsePart.getSelection()) wNameInField.setSelection(false);
                    setFlags();
                }
            }
        );
        
        
        // Partitioning field
        wlPartField=new Label(shell, SWT.RIGHT);
        wlPartField.setText("Partitioning field ");
        props.setLook(wlPartField);
        fdlPartField=new FormData();
        fdlPartField.top  = new FormAttachment(wUsePart, margin);
        fdlPartField.left = new FormAttachment(0, 0);
        fdlPartField.right= new FormAttachment(middle, -margin);
        wlPartField.setLayoutData(fdlPartField);
        wPartField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPartField);
        wPartField.addModifyListener(lsMod);
        fdPartField=new FormData();
        fdPartField.top  = new FormAttachment(wUsePart, margin);
        fdPartField.left = new FormAttachment(middle, 0);
        fdPartField.right= new FormAttachment(100, 0);
        wPartField.setLayoutData(fdPartField);

        // Partition per month
        wlPartMonthly=new Label(shell, SWT.RIGHT);
        wlPartMonthly.setText("Partition data per month");
        wlPartMonthly.setToolTipText("Moves data into table TABLENAME_YYYYMM based on the partitioning field"+Const.CR+"For example, SALES_200503, SALES_200504, ...");
        props.setLook(wlPartMonthly);
        fdlPartMonthly=new FormData();
        fdlPartMonthly.left  = new FormAttachment(0, 0);
        fdlPartMonthly.top   = new FormAttachment(wPartField, margin);
        fdlPartMonthly.right = new FormAttachment(middle, -margin);
        wlPartMonthly.setLayoutData(fdlPartMonthly);
        wPartMonthly=new Button(shell, SWT.RADIO);
        props.setLook(wPartMonthly);
        fdPartMonthly=new FormData();
        fdPartMonthly.left  = new FormAttachment(middle, 0);
        fdPartMonthly.top   = new FormAttachment(wPartField, margin);
        fdPartMonthly.right = new FormAttachment(100, 0);
        wPartMonthly.setLayoutData(fdPartMonthly);
        wPartMonthly.addSelectionListener(lsSelMod);
        
        wPartMonthly.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    wPartMonthly.setSelection(true);
                    wPartDaily.setSelection(false);
                }
            }
        );

        // Partition per month
        wlPartDaily=new Label(shell, SWT.RIGHT);
        wlPartDaily.setText("Partition data per day");
        wlPartDaily.setToolTipText("Moves data into table TABLENAME_YYYYMMDD based on the partitioning field"+Const.CR+"For example, SALES_20050301, SALES_20050301, ...");
        props.setLook(wlPartDaily);
        fdlPartDaily=new FormData();
        fdlPartDaily.left  = new FormAttachment(0, 0);
        fdlPartDaily.top   = new FormAttachment(wPartMonthly, margin);
        fdlPartDaily.right = new FormAttachment(middle, -margin);
        wlPartDaily.setLayoutData(fdlPartDaily);
        wPartDaily=new Button(shell, SWT.RADIO);
        props.setLook(wPartDaily);
        fdPartDaily=new FormData();
        fdPartDaily.left  = new FormAttachment(middle, 0);
        fdPartDaily.top   = new FormAttachment(wPartMonthly, margin);
        fdPartDaily.right = new FormAttachment(100, 0);
        wPartDaily.setLayoutData(fdPartDaily);
        wPartDaily.addSelectionListener(lsSelMod);
        
        wPartDaily.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    wPartDaily.setSelection(true);
                    wPartMonthly.setSelection(false);
                }
            }
        );

        
        // NameInField
        wlNameInField=new Label(shell, SWT.RIGHT);
        wlNameInField.setText("Is the name of the table defined in a field? ");
        props.setLook(wlNameInField);
        fdlNameInField=new FormData();
        fdlNameInField.left  = new FormAttachment(0, 0);
        fdlNameInField.top   = new FormAttachment(wPartDaily, margin*5);
        fdlNameInField.right = new FormAttachment(middle, -margin);
        wlNameInField.setLayoutData(fdlNameInField);
        wNameInField=new Button(shell, SWT.CHECK);
        props.setLook(wNameInField);
        fdNameInField=new FormData();
        fdNameInField.left  = new FormAttachment(middle, 0);
        fdNameInField.top   = new FormAttachment(wPartDaily, margin*5);
        fdNameInField.right = new FormAttachment(100, 0);
        wNameInField.setLayoutData(fdNameInField);
        wNameInField.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent se)
                {
                    if (wNameInField.getSelection()) wUsePart.setSelection(false);
                    setFlags();
                }
            }
        );
        
        // NameField size ...
        wlNameField=new Label(shell, SWT.RIGHT);
        wlNameField.setText("Field that contains name of table: ");
        props.setLook(wlNameField);
        fdlNameField=new FormData();
        fdlNameField.left = new FormAttachment(0, 0);
        fdlNameField.right= new FormAttachment(middle, -margin);
        fdlNameField.top  = new FormAttachment(wNameInField, margin);
        wlNameField.setLayoutData(fdlNameField);
        wNameField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNameField);
        wNameField.addModifyListener(lsMod);
        fdNameField=new FormData();
        fdNameField.left = new FormAttachment(middle, 0);
        fdNameField.top  = new FormAttachment(wNameInField, margin);
        fdNameField.right= new FormAttachment(100, 0);
        wNameField.setLayoutData(fdNameField);

        // NameInTable
        wlNameInTable=new Label(shell, SWT.RIGHT);
        wlNameInTable.setText("Store the tablename field ");
        props.setLook(wlNameInTable);
        fdlNameInTable=new FormData();
        fdlNameInTable.left  = new FormAttachment(0, 0);
        fdlNameInTable.top   = new FormAttachment(wNameField, margin);
        fdlNameInTable.right = new FormAttachment(middle, -margin);
        wlNameInTable.setLayoutData(fdlNameInTable);
        wNameInTable=new Button(shell, SWT.CHECK);
        props.setLook(wNameInTable);
        fdNameInTable=new FormData();
        fdNameInTable.left  = new FormAttachment(middle, 0);
        fdNameInTable.top   = new FormAttachment(wNameField, margin);
        fdNameInTable.right = new FormAttachment(100, 0);
        wNameInTable.setLayoutData(fdNameInTable);
        wNameInTable.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    setFlags();
                }
            }
        );
   
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText("  &SQL  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		
		setButtonPositions(new Button[] { wOK, wCreate, wCancel }, margin, wNameInTable);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { sql(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCreate.addListener(SWT.Selection, lsCreate);
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wCommit.addSelectionListener( lsDef );
		wTable.addSelectionListener( lsDef );
		
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
        
        // Listen to the Variable... button
        wbcTable.addSelectionListener
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
                        wTable.insert("%%"+key[nr]+"%%");
                        wTable.setToolTipText(Const.replEnv( wTable.getText() ) );
                    }
                }
                
            }
        );

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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
	
    public void setFlags()
    {
        // Can't ignore errors when using batch inserts.
        boolean useIgnore          = !wBatch.getSelection(); 
        
        boolean useTruncate        = !( wUsePart.getSelection() || wNameInField.getSelection() );
        boolean useTablename       = !( wNameInField.getSelection() );
        boolean usePartitioning    = wUsePart.getSelection();
        boolean isTableNameInField = wNameInField.getSelection();
        
        wIgnore.setEnabled( useIgnore );
        wlIgnore.setEnabled( useIgnore );

        wUsePart.setSelection(usePartitioning);
        wlPartMonthly.setEnabled(usePartitioning);
        wPartMonthly.setEnabled(usePartitioning);
        wlPartDaily.setEnabled(usePartitioning);
        wPartDaily.setEnabled(usePartitioning);
        wlPartField.setEnabled(usePartitioning);
        wPartField.setEnabled(usePartitioning);
        
        wNameInField.setSelection(isTableNameInField);
        wlNameField.setEnabled(isTableNameInField);
        wNameField.setEnabled(isTableNameInField);
        wlNameInTable.setEnabled(isTableNameInField);
        wNameInTable.setEnabled(isTableNameInField);
        
        wlTable.setEnabled( useTablename );
        wTable.setEnabled( useTablename );
        
        wlTruncate.setEnabled(useTruncate);
        wTruncate.setEnabled(useTruncate);
    }

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getTablename()      != null) wTable.setText(input.getTablename());
		if (input.getDatabase() != null) wConnection.setText(input.getDatabase().getName());
		
        wTruncate.setSelection( input.truncateTable() );
        wIgnore.setSelection(input.ignoreErrors());
		wBatch.setSelection(input.useBatchUpdate());

        wCommit.setText(""+(int)input.getCommitSize());

        wUsePart.setSelection(input.isPartitioningEnabled());
        wPartDaily.setSelection(input.isPartitioningDaily());
        wPartMonthly.setSelection(input.isPartitioningMonthly());
        if (input.getPartitioningField()!=null) wPartField.setText(input.getPartitioningField());
        
        wNameInField.setSelection( input.isTableNameInField());
        if (input.getTableNameField()!=null) wNameField.setText( input.getTableNameField() );
        wNameInTable.setSelection( input.isTableNameInTable());
        
		setFlags();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void getInfo(TableOutputMeta info)
	{
		info.setTablename( wTable.getText() );
		info.setDatabase(  transMeta.findDatabase(wConnection.getText()) );
		info.setCommitSize( Const.toInt( wCommit.getText(), 0 ) );
		info.setTruncateTable( wTruncate.getSelection() );
		info.setIgnoreErrors( wIgnore.getSelection() );
		info.setUseBatchUpdate( wBatch.getSelection() );
        info.setPartitioningEnabled( wUsePart.getSelection() );
        info.setPartitioningField( wPartField.getText() );
        info.setPartitioningDaily( wPartDaily.getSelection() );
        info.setPartitioningMonthly( wPartMonthly.getSelection() );
        info.setTableNameInField( wNameInField.getSelection() );
        info.setTableNameField( wNameField.getText() );
        info.setTableNameInTable( wNameInTable.getSelection() );
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		
		getInfo(input);

		if (input.getDatabase()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
	}
	
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
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
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid database connection first!");
			mb.setText("ERROR");
			mb.open(); 
		}
					
	}
	
	// Generate code for create table...
	// Conversions done by Database
	private void sql()
	{
		try
		{
			TableOutputMeta info = new TableOutputMeta();
			getInfo(info);
			Row prev = transMeta.getPrevStepFields(stepname);
            if (info.isTableNameInField() && !info.isTableNameInTable() && info.getTableNameField().length()>0)
            {
                int idx = prev.searchValueIndex(info.getTableNameField());
                if (idx>=0) prev.removeValue(idx);
            }
			StepMeta stepMeta = transMeta.findStep(stepname);
			
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
