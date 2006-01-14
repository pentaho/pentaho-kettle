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

package be.ibridge.kettle.trans.step.sql;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class ExecSQLDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlSQL;
	private Text         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlEachRow;
	private Button       wEachRow;
	private FormData     fdlEachRow, fdEachRow;
    
    private Label        wlInsertField;
    private Text         wInsertField;
    private FormData     fdlInsertField, fdInsertField;
    
    private Label        wlUpdateField;
    private Text         wUpdateField;
    private FormData     fdlUpdateField, fdUpdateField;
    
    private Label        wlDeleteField;
    private Text         wDeleteField;
    private FormData     fdlDeleteField, fdDeleteField;
    
    private Label        wlReadField;
    private Text         wReadField;
    private FormData     fdlReadField, fdReadField;
    
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;

	private ExecSQLMeta input;

	public ExecSQLDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ExecSQLMeta)in;
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
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Execute SQL statements");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

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
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// Table line...
		wlSQL=new Label(shell, SWT.LEFT);
		wlSQL.setText("SQL script to execute. (statements separated by ; ) Question marks will be replaced by arguments. ");
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wSQL=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wlSQL, margin  );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(100, -250      );
		wSQL.setLayoutData(fdSQL);
		
		// Execute for each row?
		wlEachRow=new Label(shell, SWT.RIGHT);
		wlEachRow.setText("Execute for each row? ");
 		props.setLook(wlEachRow);
		fdlEachRow=new FormData();
		fdlEachRow.left = new FormAttachment(0, 0);
		fdlEachRow.right= new FormAttachment(middle, -margin);
		fdlEachRow.top  = new FormAttachment(wSQL, margin);
		wlEachRow.setLayoutData(fdlEachRow);
		wEachRow=new Button(shell, SWT.CHECK);
 		props.setLook(wEachRow);
		fdEachRow=new FormData();
		fdEachRow.left = new FormAttachment(middle, 0);
		fdEachRow.top  = new FormAttachment(wSQL, margin);
		fdEachRow.right= new FormAttachment(100, 0);
		wEachRow.setLayoutData(fdEachRow);
        
        wlFields=new Label(shell, SWT.NONE);
        wlFields.setText("Parameters : ");
 		props.setLook(        wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wEachRow, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=input.getArguments().length;
        
        ColumnInfo[] colinf=new ColumnInfo[]
               {
                new ColumnInfo("Field name to be used as argument",       ColumnInfo.COLUMN_TYPE_TEXT,   false)
               };
        
        wFields=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              lsMod,
                              props
                              );

        fdFields=new FormData();
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.top   = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(middle, 0);
        fdFields.bottom= new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        
        // insert field
        wlInsertField=new Label(shell, SWT.RIGHT);
        wlInsertField.setText("Field to contain insert stats");
 		props.setLook(        wlInsertField);
        fdlInsertField=new FormData();
        fdlInsertField.left = new FormAttachment(wFields, margin);
        fdlInsertField.right= new FormAttachment(middle*2, -margin);
        fdlInsertField.top  = new FormAttachment(wEachRow, margin);
        wlInsertField.setLayoutData(fdlInsertField);
        wInsertField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wInsertField);
        wInsertField.addModifyListener(lsMod);
        fdInsertField=new FormData();
        fdInsertField.left = new FormAttachment(middle*2, 0);
        fdInsertField.top  = new FormAttachment(wEachRow, margin);
        fdInsertField.right= new FormAttachment(100, 0);
        wInsertField.setLayoutData(fdInsertField);
        
        // Update field
        wlUpdateField=new Label(shell, SWT.RIGHT);
        wlUpdateField.setText("Field to contain Update stats");
 		props.setLook(        wlUpdateField);
        fdlUpdateField=new FormData();
        fdlUpdateField.left = new FormAttachment(wFields, margin);
        fdlUpdateField.right= new FormAttachment(middle*2, -margin);
        fdlUpdateField.top  = new FormAttachment(wInsertField, margin);
        wlUpdateField.setLayoutData(fdlUpdateField);
        wUpdateField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wUpdateField);
        wUpdateField.addModifyListener(lsMod);
        fdUpdateField=new FormData();
        fdUpdateField.left = new FormAttachment(middle*2, 0);
        fdUpdateField.top  = new FormAttachment(wInsertField, margin);
        fdUpdateField.right= new FormAttachment(100, 0);
        wUpdateField.setLayoutData(fdUpdateField);
        
        // Delete field
        wlDeleteField=new Label(shell, SWT.RIGHT);
        wlDeleteField.setText("Field to contain Delete stats");
 		props.setLook(        wlDeleteField);
        fdlDeleteField=new FormData();
        fdlDeleteField.left = new FormAttachment(wFields, margin);
        fdlDeleteField.right= new FormAttachment(middle*2, -margin);
        fdlDeleteField.top  = new FormAttachment(wUpdateField, margin);
        wlDeleteField.setLayoutData(fdlDeleteField);
        wDeleteField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wDeleteField);
        wDeleteField.addModifyListener(lsMod);
        fdDeleteField=new FormData();
        fdDeleteField.left = new FormAttachment(middle*2, 0);
        fdDeleteField.top  = new FormAttachment(wUpdateField, margin);
        fdDeleteField.right= new FormAttachment(100, 0);
        wDeleteField.setLayoutData(fdDeleteField);
        
        // Read field
        wlReadField=new Label(shell, SWT.RIGHT);
        wlReadField.setText("Field to contain Read stats");
 		props.setLook(        wlReadField);
        fdlReadField=new FormData();
        fdlReadField.left = new FormAttachment(wFields, 0);
        fdlReadField.right= new FormAttachment(middle*2, -margin);
        fdlReadField.top  = new FormAttachment(wDeleteField, margin);
        wlReadField.setLayoutData(fdlReadField);
        wReadField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wReadField);
        wReadField.addModifyListener(lsMod);
        fdReadField=new FormData();
        fdReadField.left = new FormAttachment(middle*2, 0);
        fdReadField.top  = new FormAttachment(wDeleteField, margin);
        fdReadField.right= new FormAttachment(100, 0);
        wReadField.setLayoutData(fdReadField);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
        wGet=new Button(shell, SWT.PUSH);
        wGet.setText("  &Get fields  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsGet      = new Listener() { public void handleEvent(Event e) { get(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
        wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener   (SWT.Selection, lsGet);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wEachRow.addSelectionListener( lsDef );

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}


    /**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getSql() != null) wSQL.setText(input.getSql());
		if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
		wEachRow.setSelection(input.isExecutedEachInputRow());
        
        if (input.getUpdateField()!=null) wUpdateField.setText(input.getUpdateField());
        if (input.getInsertField()!=null) wInsertField.setText(input.getInsertField());
        if (input.getDeleteField()!=null) wDeleteField.setText(input.getDeleteField());
        if (input.getReadField()  !=null) wReadField  .setText(input.getReadField());
		
		for (int i=0;i<input.getArguments().length;i++)
        {
            TableItem item = wFields.table.getItem(i);
            if (input.getArguments()[i]!=null) item.setText(1, input.getArguments()[i]);
        }

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
		stepname = wStepname.getText(); // return value
		// copy info to TextFileInputMeta class (input)
		input.setSql( wSQL.getText() );
		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
		input.setExecutedEachInputRow( wEachRow.getSelection() );
        
        input.setInsertField(wInsertField.getText());
        input.setUpdateField(wUpdateField.getText());
        input.setDeleteField(wDeleteField.getText());
        input.setReadField  (wReadField  .getText());
		
        int nrargs = wFields.nrNonEmpty();
        input.allocate(nrargs);

        log.logDebug(toString(), "Found "+nrargs+" arguments");
        for (int i=0;i<nrargs;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            input.getArguments()[i] = item.getText(1);
        }
        
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
		}
		
		dispose();
    }
    
    
    private void get()
    {
        try
        {
            int i, count;
            Row r = transMeta.getPrevStepFields(stepname);
            if (r!=null)
            {
                Table table=wFields.table;
                count=table.getItemCount();
                for (i=0;i<r.size();i++)
                {
                    Value v = r.getValue(i);
                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, ""+(count+i+1));
                    ti.setText(1, v.getName());
                    ti.setText(2, "IN");
                    ti.setText(3, v.getTypeDesc());
                }
                wFields.removeEmptyRows();
                wFields.setRowNums();
                wFields.optWidth(true);
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
        }

    }
}
