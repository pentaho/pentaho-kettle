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

package be.ibridge.kettle.trans.step.mappingoutput;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;

public class MappingOutputDialog extends BaseStepDialog implements StepDialogInterface
{
    private Label             wlFields;

    private TableView         wFields;

    private FormData          fdlFields, fdFields;

    private MappingOutputMeta input;

    public MappingOutputDialog(Shell parent, Object in, TransMeta tr, String sname)
    {
        super(parent, (BaseStepMeta) in, tr, sname);
        input = (MappingOutputMeta) in;
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

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("Mapping Output Specification");

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText("Step name ");
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        
        wlFields = new Label(shell, SWT.NONE);
        wlFields.setText("The fields that are added or removed by this mapping:");
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);

        final int FieldsRows = input.getFieldName().length;

        ColumnInfo[] colinf = new ColumnInfo[] 
        { 
            new ColumnInfo("Name", ColumnInfo.COLUMN_TYPE_TEXT, false),
            new ColumnInfo("Type", ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes()),
            new ColumnInfo("Length", ColumnInfo.COLUMN_TYPE_TEXT, false), 
            new ColumnInfo("Precision", ColumnInfo.COLUMN_TYPE_TEXT, false),
            new ColumnInfo("Added (N=Removed)", ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y", "N"}, true) 
        };

        wFields = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);

        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText("  &OK  ");
        wGet = new Button(shell, SWT.PUSH);
        wGet.setText("  &Get fields  ");
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText("  &Cancel  ");

        setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, wFields);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };
        lsGet = new Listener()
        {
            public void handleEvent(Event e)
            {
                get();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wGet.addListener(SWT.Selection, lsGet);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

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
                item.setText(5, input.getFieldAdded()[i]?"Y":"N");
            }
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
        
        wStepname.selectAll();
    }

    private void cancel()
    {
        stepname = null;
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
            
            input.getFieldAdded()[i] = "Y".equalsIgnoreCase(item.getText(5));
        }

        dispose();
    }
    
    private void get()
    {
        try
        {
            TableView tv=wFields; 
 
            Row r = transMeta.getPrevStepFields(stepname);
            if (r!=null)
            {
                Table table = tv.table;
                
                for (int i=0;i<r.size();i++)
                {
                    Value v = r.getValue(i);
                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(1, v.getName());
                    ti.setText(2, v.getTypeDesc());
                    if (v.getLength()>=0)    ti.setText(3, ""+v.getLength() );
                    if (v.getPrecision()>=0) ti.setText(4, ""+v.getPrecision() );
                    ti.setText(5, "Y");
                }
                tv.removeEmptyRows();
                tv.setRowNums();
                tv.optWidth(true);
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
        }
    }}
