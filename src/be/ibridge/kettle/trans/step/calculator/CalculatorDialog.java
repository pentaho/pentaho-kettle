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

package be.ibridge.kettle.trans.step.calculator;

import org.eclipse.swt.SWT;
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
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepMeta;

public class CalculatorDialog extends BaseStepDialog implements StepDialogInterface
{
    private Label        wlStepname;
    private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
    
	private CalculatorMeta input;

	public CalculatorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CalculatorMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText("Calculator");
		
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
		
        wlFields=new Label(shell, SWT.NONE);
        wlFields.setText("Fields: ");
 		props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=input.getCalculation()!=null ? input.getCalculation().length : 1;
        
        final ColumnInfo[] colinf=new ColumnInfo[]
               {
                    new ColumnInfo("New field",     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo("Calculation",   ColumnInfo.COLUMN_TYPE_CCOMBO, CalculatorMetaFunction.calcLongDesc ),
                    new ColumnInfo("Field A",       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo("Field B",       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo("Field C",       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo("Value type",    ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() ),
                    new ColumnInfo("Length",        ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo("Precision",     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo("Remove",        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "N", "Y" } )
               };
        
        colinf[1].setSelectionAdapter(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, CalculatorMetaFunction.calcLongDesc, "Select the calculation type", "Select the calculation type to perform");
                    String string = esd.open();
                    if (string!=null)
                    {
                        TableView tv = (TableView)e.widget;
                        tv.setText(string, e.x, e.y);
                    }
                }
            }
        );

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
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);

        // 
        // Search the fields in the background
        //
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                        Row row = transMeta.getPrevStepFields(stepMeta);
                        
                        colinf[2].setComboValues(row.getFieldNames());
                        colinf[3].setComboValues(row.getFieldNames());
                        colinf[4].setComboValues(row.getFieldNames());
                    }
                    catch(KettleException e)
                    {
                        log.logError(toString(), "Sorry, couldn't find previous step fields...");
                    }
                }
            }
        };
        new Thread(runnable).start();
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

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
		input.setChanged(changed);
	
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
		wStepname.selectAll();
        
        if (input.getCalculation()!=null)
        for (int i=0;i<input.getCalculation().length;i++)
        {
            CalculatorMetaFunction fn = input.getCalculation()[i];
            TableItem item = wFields.table.getItem(i);
            item.setText(1, Const.NVL(fn.getFieldName(), ""));
            item.setText(2, Const.NVL(fn.getCalcTypeLongDesc(), ""));
            item.setText(3, Const.NVL(fn.getFieldA(), ""));
            item.setText(4, Const.NVL(fn.getFieldB(), ""));
            item.setText(5, Const.NVL(fn.getFieldC(), ""));
            item.setText(6, Const.NVL(Value.getTypeDesc(fn.getValueType()), ""));
            if (fn.getValueLength()>=0) item.setText(7, ""+fn.getValueLength());
            if (fn.getValuePrecision()>=0) item.setText(8, ""+fn.getValuePrecision());
            item.setText(9, fn.isRemovedFromResult()?"Y":"N");
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
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
		
        input.allocate(wFields.nrNonEmpty());
        
        for (int i=0;i<wFields.nrNonEmpty();i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            
            String fieldName       = item.getText(1);
            int    calcType        = CalculatorMetaFunction.getCalcFunctionType(item.getText(2));
            String fieldA          = item.getText(3);
            String fieldB          = item.getText(4);
            String fieldC          = item.getText(5);
            int    valueType       = Value.getType( item.getText(6) );
            int    valueLength     = Const.toInt( item.getText(7), -1 );
            int    valuePrecision  = Const.toInt( item.getText(8), -1 );
            boolean removed        = "Y".equalsIgnoreCase( item.getText(9) );
                        
            input.getCalculation()[i] = new CalculatorMetaFunction(fieldName, calcType, fieldA, fieldB, fieldC, valueType, valueLength, valuePrecision, removed);
        }
        
		dispose();
	}
}
