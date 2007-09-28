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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import be.ibridge.kettle.core.LocalVariables;
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
    
	private CalculatorMeta currentMeta;
	private CalculatorMeta originalMeta;
    
    private Map      inputFields;
    private ColumnInfo[] colinf;

	public CalculatorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		
		// The order here is important... currentMeta is looked at for changes
		currentMeta=(CalculatorMeta)in;
		originalMeta=(CalculatorMeta)currentMeta.clone();
        inputFields =new HashMap();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
        setShellImage(shell, currentMeta);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				currentMeta.setChanged();
			}
		};
		changed = currentMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("CalculatorDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
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
        wlFields.setText(Messages.getString("CalculatorDialog.Fields.Label"));
 		props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=currentMeta.getCalculation()!=null ? currentMeta.getCalculation().length : 1;
        
        colinf=new ColumnInfo[]
               {
                    new ColumnInfo(Messages.getString("CalculatorDialog.NewFieldColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.CalculationColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.FieldAColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.FieldBColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.FieldCColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.ValueTypeColumn.Column"),    ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() ),
                    new ColumnInfo(Messages.getString("CalculatorDialog.LengthColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.PrecisionColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(Messages.getString("CalculatorDialog.RemoveColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { Messages.getString("System.Combo.No"), Messages.getString("System.Combo.Yes") } )
               };
        
        colinf[1].setSelectionAdapter(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, CalculatorMetaFunction.calcLongDesc, Messages.getString("CalculatorDialog.SelectCalculationType.Title"), Messages.getString("CalculatorDialog.SelectCalculationType.Message"));
                    String string = esd.open();
                    if (string!=null)
                    {
                        TableView tv = (TableView)e.widget;
                        tv.setText(string, e.x, e.y);
                        currentMeta.setChanged();
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
        final Thread parentThread = Thread.currentThread();
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                //  This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                        Row row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            inputFields.put(row.getValue(i).getName(), new Integer(i));
                        }
                        
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                        log.logError(toString(), Messages.getString("CalculatorDialog.Log.UnableToFindInput"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
        wFields.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    // Now set the combo's
                    shell.getDisplay().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            setComboBoxes();
                        }
                    
                    });
                    
                }
            }
        );
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

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
		currentMeta.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map fields = new HashMap();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        shell.getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    // Add the newly create fields.
                    //
                    int nrNonEmptyFields = wFields.nrNonEmpty();
                    for (int i=0;i<nrNonEmptyFields;i++)
                    {
                        TableItem item = wFields.getNonEmpty(i);
                        fields.put(item.getText(1), new Integer(1000000+i));  // The number is just to debug the origin of the fieldname
                    }
                }
            }
        );
        
        Set keySet = fields.keySet();
        ArrayList entries = new ArrayList(keySet);
        
        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[2].setComboValues(fieldNames);
        colinf[3].setComboValues(fieldNames);
        colinf[4].setComboValues(fieldNames);
    }

    /**
	 * Copy information from the meta-data currentMeta to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        
        if (currentMeta.getCalculation()!=null)
        for (int i=0;i<currentMeta.getCalculation().length;i++)
        {
            CalculatorMetaFunction fn = currentMeta.getCalculation()[i];
            TableItem item = wFields.table.getItem(i);
            item.setText(1, Const.NVL(fn.getFieldName(), ""));
            item.setText(2, Const.NVL(fn.getCalcTypeLongDesc(), ""));
            item.setText(3, Const.NVL(fn.getFieldA(), ""));
            item.setText(4, Const.NVL(fn.getFieldB(), ""));
            item.setText(5, Const.NVL(fn.getFieldC(), ""));
            item.setText(6, Const.NVL(Value.getTypeDesc(fn.getValueType()), ""));
            if (fn.getValueLength()>=0) item.setText(7, ""+fn.getValueLength());
            if (fn.getValuePrecision()>=0) item.setText(8, ""+fn.getValuePrecision());
            item.setText(9, fn.isRemovedFromResult()?Messages.getString("System.Combo.Yes"):Messages.getString("System.Combo.No"));
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		currentMeta.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		
        currentMeta.allocate(wFields.nrNonEmpty());
        
        int nrNonEmptyFields = wFields.nrNonEmpty();
        for (int i=0;i<nrNonEmptyFields;i++)
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
            boolean removed        = Messages.getString("System.Combo.Yes").equalsIgnoreCase( item.getText(9) );
                        
            currentMeta.getCalculation()[i] = new CalculatorMetaFunction(fieldName, calcType, fieldA, fieldB, fieldC, valueType, valueLength, valuePrecision, removed);
        }
        
        if ( ! originalMeta.equals(currentMeta) )
        {
        	currentMeta.setChanged();
        	changed = currentMeta.hasChanged();
        }
        
		dispose();
	}
}
