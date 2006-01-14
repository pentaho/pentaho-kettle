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

package be.ibridge.kettle.trans.step.mappinginput;

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
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class MappingInputDialog extends BaseStepDialog implements StepDialogInterface
{
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;

	private MappingInputMeta input;

	public MappingInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MappingInputMeta )in;
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
		shell.setText("Mapping Input Specification");
		
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
        wlFields.setText("The required input fields for this mapping (sub-transformation) :");
        props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=input.getFieldName().length;
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
                new ColumnInfo("Name",       ColumnInfo.COLUMN_TYPE_TEXT,   false),
                new ColumnInfo("Type",       ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() ),
                new ColumnInfo("Length",     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                new ColumnInfo("Precision",  ColumnInfo.COLUMN_TYPE_TEXT,   false)
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
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wFields);

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
        for (int i=0;i<input.getFieldName().length;i++)
        {
            if (input.getFieldName()[i]!=null)
            {
                TableItem item = wFields.table.getItem(i);
                item.setText(1, input.getFieldName()[i]);
                String type   = Value.getTypeDesc(input.getFieldType()[i]);
                int length    = input.getFieldLength()[i];
                int prec      = input.getFieldPrecision()[i];
                if (type  !=null) item.setText(2, type  );
                if (length>=0   ) item.setText(3, ""+length);
                if (prec>=0     ) item.setText(4, ""+prec  );
            }
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
        
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
		
        int nrfields = wFields.nrNonEmpty();

        input.allocate(nrfields);

        for (int i=0;i<nrfields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            input.getFieldName()[i]   = item.getText(1);
            input.getFieldType()[i]   = Value.getType(item.getText(2));
            String slength = item.getText(3);
            String sprec   = item.getText(4);
            
            input.getFieldLength()[i]    = Const.toInt(slength, -1); 
            input.getFieldPrecision()[i] = Const.toInt(sprec  , -1); 
        }

		dispose();
	}
}
