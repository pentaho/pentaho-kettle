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

package be.ibridge.kettle.trans.step.mergerows;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;


public class MergeRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlReference;
	private CCombo       wReference;
	private FormData     fdlReference, fdReference;

	private Label        wlCompare;
	private CCombo       wCompare;
	private FormData     fdlCompare, fdCompare;
    
    private Label        wlFlagfield;
    private Text         wFlagfield;
    private FormData     fdlFlagfield, fdFlagfield;
    
    private Label        wlKeys;
    private TableView    wKeys;
    private Button       wbKeys;
    private FormData     fdlKeys, fdKeys, fdbKeys;

    private Label        wlValues;
    private TableView    wValues;
    private Button       wbValues;
    private FormData     fdlValues, fdValues, fdbValues;

	private MergeRowsMeta input;
	
	public MergeRowsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MergeRowsMeta)in;
     }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);

		
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
			
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Filter rows");
		
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

        // Get the previous steps...
        String previousSteps[] = transMeta.getPrevStepNames(stepname);
        
		// Send 'True' data to...
		wlReference=new Label(shell, SWT.RIGHT);
		wlReference.setText("Reference rows origin:");
 		props.setLook(wlReference);
		fdlReference=new FormData();
		fdlReference.left = new FormAttachment(0, 0);
		fdlReference.right= new FormAttachment(middle, -margin);
		fdlReference.top  = new FormAttachment(wStepname, margin);
		wlReference.setLayoutData(fdlReference);
		wReference=new CCombo(shell, SWT.BORDER );
 		props.setLook(wReference);

		if (previousSteps!=null)
		{
			wReference.setItems( previousSteps );
		}
		
		wReference.addModifyListener(lsMod);
		fdReference=new FormData();
		fdReference.left = new FormAttachment(middle, 0);
		fdReference.top  = new FormAttachment(wStepname, margin);
		fdReference.right= new FormAttachment(100, 0);
		wReference.setLayoutData(fdReference);

		// Send 'False' data to...
		wlCompare=new Label(shell, SWT.RIGHT);
		wlCompare.setText("Compare rows origin:");
 		props.setLook(wlCompare);
		fdlCompare=new FormData();
		fdlCompare.left = new FormAttachment(0, 0);
		fdlCompare.right= new FormAttachment(middle, -margin);
		fdlCompare.top  = new FormAttachment(wReference, margin);
		wlCompare.setLayoutData(fdlCompare);
		wCompare=new CCombo(shell, SWT.BORDER );
 		props.setLook(wCompare);

        if (previousSteps!=null)
        {
            wCompare.setItems( previousSteps );
        }	
        
		wCompare.addModifyListener(lsMod);
		fdCompare=new FormData();
        fdCompare.top  = new FormAttachment(wReference, margin);
		fdCompare.left = new FormAttachment(middle, 0);
		fdCompare.right= new FormAttachment(100, 0);
		wCompare.setLayoutData(fdCompare);

        
        // Stepname line
        wlFlagfield=new Label(shell, SWT.RIGHT);
        wlFlagfield.setText("Flag fieldname ");
        props.setLook(wlFlagfield);
        fdlFlagfield=new FormData();
        fdlFlagfield.left = new FormAttachment(0, 0);
        fdlFlagfield.right= new FormAttachment(middle, -margin);
        fdlFlagfield.top  = new FormAttachment(wCompare, margin);
        wlFlagfield.setLayoutData(fdlFlagfield);
        wFlagfield=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFlagfield);
        wFlagfield.addModifyListener(lsMod);
        fdFlagfield=new FormData();
        fdFlagfield.top  = new FormAttachment(wCompare, margin);
        fdFlagfield.left = new FormAttachment(middle, 0);
        fdFlagfield.right= new FormAttachment(100, 0);
        wFlagfield.setLayoutData(fdFlagfield);

        
        // THE KEYS TO MATCH...
        wlKeys=new Label(shell, SWT.NONE);
        wlKeys.setText("Keys to match :");
        props.setLook(wlKeys);
        fdlKeys=new FormData();
        fdlKeys.left  = new FormAttachment(0, 0);
        fdlKeys.top   = new FormAttachment(wFlagfield, margin);
        wlKeys.setLayoutData(fdlKeys);
        
        int nrKeyRows= (input.getKeyFields()!=null?input.getKeyFields().length:1);
        
        ColumnInfo[] ciKeys=new ColumnInfo[] {
            new ColumnInfo("Key field", ColumnInfo.COLUMN_TYPE_TEXT, false),
        };
            
        wKeys=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciKeys, 
                              nrKeyRows,  
                              lsMod,
                              props
                              );

        fdKeys = new FormData();
        fdKeys.top    = new FormAttachment(wlKeys, margin);
        fdKeys.left   = new FormAttachment(0,   0);
        fdKeys.bottom = new FormAttachment(100, -70);
        fdKeys.right  = new FormAttachment(50, -margin);
        wKeys.setLayoutData(fdKeys);

        wbKeys=new Button(shell, SWT.PUSH);
        wbKeys.setText(" Get &key fields ");
        fdbKeys = new FormData();
        fdbKeys.top   = new FormAttachment(wKeys, margin);
        fdbKeys.left  = new FormAttachment(0, 0);
        fdbKeys.right = new FormAttachment(50, -margin);
        wbKeys.setLayoutData(fdbKeys);
        wbKeys.addSelectionListener(new SelectionAdapter()
            {
            
                public void widgetSelected(SelectionEvent e)
                {
                    getKeys();
                }
            }
        );


        // VALUES TO COMPARE
        wlValues=new Label(shell, SWT.NONE);
        wlValues.setText("Values to compare :");
        props.setLook(wlValues);
        fdlValues=new FormData();
        fdlValues.left  = new FormAttachment(50, 0);
        fdlValues.top   = new FormAttachment(wFlagfield, margin);
        wlValues.setLayoutData(fdlValues);
        
        int nrValueRows= (input.getValueFields()!=null?input.getValueFields().length:1);
        
        ColumnInfo[] ciValues=new ColumnInfo[] {
            new ColumnInfo("Value field", ColumnInfo.COLUMN_TYPE_TEXT, false),
        };
            
        wValues=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
                              ciValues, 
                              nrValueRows,  
                              lsMod,
                              props
                              );

        fdValues = new FormData();
        fdValues.top    = new FormAttachment(wlValues, margin);
        fdValues.left   = new FormAttachment(50,  0);
        fdValues.bottom = new FormAttachment(100, -70);
        fdValues.right  = new FormAttachment(100, 0);
        wValues.setLayoutData(fdValues);

        
        wbValues=new Button(shell, SWT.PUSH);
        wbValues.setText(" Get &value fields ");
        fdbValues = new FormData();
        fdbValues.top   = new FormAttachment(wValues, margin);
        fdbValues.left  = new FormAttachment(50,  0);
        fdbValues.right = new FormAttachment(100, 0);
        wbValues.setLayoutData(fdbValues);
        wbValues.addSelectionListener(new SelectionAdapter()
                {
                
                    public void widgetSelected(SelectionEvent e)
                    {
                        getValues();
                    }
                }
            );
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wbKeys);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getReferenceStepName() != null) wReference.setText(input.getReferenceStepName());
		if (input.getCompareStepName() != null) wCompare.setText(input.getCompareStepName());
        if (input.getFlagField() !=null ) wFlagfield.setText(input.getFlagField() ); 
        
        for (int i=0;i<input.getKeyFields().length;i++)
        {
            TableItem item = wKeys.table.getItem(i);
            if (input.getKeyFields()[i]!=null) item.setText(1, input.getKeyFields()[i]);
        }
        for (int i=0;i<input.getValueFields().length;i++)
        {
            TableItem item = wValues.table.getItem(i);
            if (input.getValueFields()[i]!=null) item.setText(1, input.getValueFields()[i]);
        }
        
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
		input.setReferenceStepMeta( transMeta.findStep( wReference.getText() ) );
		input.setCompareStepMeta( transMeta.findStep( wCompare.getText() ) );
        input.setFlagField( wFlagfield.getText());

        int nrKeys   = wKeys.nrNonEmpty();
        int nrValues = wValues.nrNonEmpty();

        input.allocate(nrKeys, nrValues );
        
        for (int i=0;i<nrKeys;i++)
        {
            TableItem item = wKeys.getNonEmpty(i);
            input.getKeyFields()[i] = item.getText(1);
        }

        for (int i=0;i<nrValues;i++)
        {
            TableItem item = wValues.getNonEmpty(i);
            input.getValueFields()[i] = item.getText(1);
        }

		stepname = wStepname.getText(); // return value
		
		dispose();
	}
    
    private void getKeys()
    {
        try
        {
            StepMeta stepMeta = transMeta.findStep(input.getReferenceStepName());
            if (stepMeta!=null)
            {
                Row prev = transMeta.getStepFields(stepMeta);
                if (prev!=null)
                {
                    for (int i=0;i<prev.size();i++)
                    {
                        TableItem item = new TableItem(wKeys.table, SWT.NONE);
                        item.setText(1, prev.getValue(i).getName());
                    }
                    wKeys.removeEmptyRows();
                    wKeys.setRowNums();
                    wKeys.optWidth(true);
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error getting fields", "Unable to get the fields because of an error: ", e);
        }
    }
    
    private void getValues()
    {
        try
        {
            StepMeta stepMeta = transMeta.findStep(input.getReferenceStepName());
            if (stepMeta!=null)
            {
                Row prev = transMeta.getStepFields(stepMeta);
                if (prev!=null)
                {
                    for (int i=0;i<prev.size();i++)
                    {
                        TableItem item = new TableItem(wValues.table, SWT.NONE);
                        item.setText(1, prev.getValue(i).getName());
                    }
                    wValues.removeEmptyRows();
                    wValues.setRowNums();
                    wValues.optWidth(true);
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, props, "Error getting fields", "Unable to get the fields because of an error: ", e);
        }
    }
}
