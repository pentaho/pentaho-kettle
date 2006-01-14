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

package be.ibridge.kettle.trans.step.uniquerows;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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


public class UniqueRowsDialog extends BaseStepDialog implements StepDialogInterface
{
    public static final String STRING_SORT_WARNING_PARAMETER = "UniqueSortWarning";
    
	private UniqueRowsMeta input;

	private Label        wlCount;
	private Button       wCount;
	private FormData     fdlCount, fdCount;

	private Label        wlCountField;
	private Text         wCountField;
	private FormData     fdlCountField, fdCountField;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	public UniqueRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(UniqueRowsMeta)in;
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
		shell.setText("Unique rows");
		
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

		wlCount=new Label(shell, SWT.RIGHT);
		wlCount.setText("Add counter to output? ");
 		props.setLook(wlCount);
		fdlCount=new FormData();
		fdlCount.left = new FormAttachment(0, 0);
		fdlCount.top  = new FormAttachment(wStepname, margin);
		fdlCount.right= new FormAttachment(middle, -margin);
		wlCount.setLayoutData(fdlCount);
		
		wCount=new Button(shell, SWT.CHECK );
 		props.setLook(wCount);
		wCount.setToolTipText("Check this to add a field (Number) containing"+Const.CR+"the number of occurences of the row.");
		fdCount=new FormData();
		fdCount.left = new FormAttachment(middle, 0);
		fdCount.top  = new FormAttachment(wStepname, margin);
		wCount.setLayoutData(fdCount);
		wCount.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
					setFlags();
				}
			}
		);

		wlCountField=new Label(shell, SWT.LEFT);
		wlCountField.setText("Counter field ");
 		props.setLook(wlCountField);
		fdlCountField=new FormData();
		fdlCountField.left = new FormAttachment(wCount, margin);
		fdlCountField.top  = new FormAttachment(wStepname, margin);
		wlCountField.setLayoutData(fdlCountField);
		wCountField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCountField);
		wCountField.addModifyListener(lsMod);
		fdCountField=new FormData();
		fdCountField.left = new FormAttachment(wlCountField, margin);
		fdCountField.top  = new FormAttachment(wStepname, margin);
		fdCountField.right= new FormAttachment(100, 0);
		wCountField.setLayoutData(fdCountField);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText("  &Get  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdOK=new FormData();
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Fields to compare on (no entries means: compare complete row)");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wCountField, margin);
		wlFields.setLayoutData(fdlFields);

		final int FieldsRows=input.getCompareFields()==null?0:input.getCompareFields().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
        {
		  new ColumnInfo("Fieldname",    ColumnInfo.COLUMN_TYPE_TEXT,   false ),
          new ColumnInfo("Ignore case",  ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] {"Y", "N"}, true )
        };
		
		wFields=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wOK, -2*margin);
		wFields.setLayoutData(fdFields);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener(SWT.Selection, lsGet );
		wOK.addListener    (SWT.Selection, lsOK );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wCountField.addSelectionListener( lsDef );
		
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
	
	public void setFlags()
	{
		wlCountField.setEnabled(wCount.getSelection());
		wCountField.setEnabled(wCount.getSelection());
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wCount.setSelection(input.isCountRows());
		if (input.getCountField()!=null) wCountField.setText(input.getCountField());
		setFlags();
		for (int i=0;i<input.getCompareFields().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			if (input.getCompareFields()[i]!=null) item.setText(1, input.getCompareFields()[i]);
            item.setText(2, input.getCaseInsensitive()[i]?"Y":"N");
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
		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getCompareFields()[i] = item.getText(1);
            input.getCaseInsensitive()[i] = "Y".equalsIgnoreCase(item.getText(2));
		}
		
		input.setCountField(wCountField.getText());
		input.setCountRows( wCount.getSelection() );
		
		stepname = wStepname.getText(); // return value
		
        if ( "Y".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") ))
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                 "Warning!", 
                 null,
                 "The 'unique' function needs the input to be sorted on the specified keys." + Const.CR + "If you don't sort the input, only consequetive identical rows will be considered!"+Const.CR,
                 MessageDialog.WARNING,
                 new String[] { "I understand" },
                 0,
                 "Please, don't show this warning anymore.",
                 "N".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") )
            );
            md.open();
            props.setCustomParameter(STRING_SORT_WARNING_PARAMETER, md.getToggleState()?"N":"Y");
            props.saveProps();
        }

        
		dispose();
	}
	
	
	private void get()
	{
		try
		{
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wFields.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(1, v.getName());
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
