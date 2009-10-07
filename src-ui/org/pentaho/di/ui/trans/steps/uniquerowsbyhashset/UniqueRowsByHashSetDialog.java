package org.pentaho.di.ui.trans.steps.uniquerowsbyhashset;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.uniquerowsbyhashset.UniqueRowsByHashSetMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class UniqueRowsByHashSetDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = UniqueRowsByHashSetMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private UniqueRowsByHashSetMeta input;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Label        wlStoreValues;
	private Button       wStoreValues;
	private FormData     fdlStoreValues, fdStoreValues;
	
    private Map<String, Integer> inputFields;

    private ColumnInfo[] colinf;
    
	public UniqueRowsByHashSetDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(UniqueRowsByHashSetMeta)in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
        setShellImage(shell, input);

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
		shell.setText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.Stepname.Label")); //$NON-NLS-1$
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
		
        wlStoreValues=new Label(shell, SWT.RIGHT);
        wlStoreValues.setText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.StoreValues.Label")); //$NON-NLS-1$
        props.setLook(wlStoreValues);
        fdlStoreValues=new FormData();
        fdlStoreValues.left = new FormAttachment(0, 0);
        fdlStoreValues.top  = new FormAttachment(wStepname, margin);
        fdlStoreValues.right= new FormAttachment(middle, -margin);
        wlStoreValues.setLayoutData(fdlStoreValues);
        
        wStoreValues=new Button(shell, SWT.CHECK );
        props.setLook(wStoreValues);
        wStoreValues.setToolTipText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.StoreValues.ToolTip",Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$
        fdStoreValues=new FormData();
        fdStoreValues.left = new FormAttachment(middle, 0);
        fdStoreValues.top  = new FormAttachment(wStepname, margin);
        wStoreValues.setLayoutData(fdStoreValues);
        wStoreValues.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                }
            }
        );

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.Get.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		fdOK=new FormData();
		
		setButtonPositions(new Button[] { wOK, wCancel , wGet} , margin, null);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStoreValues, margin);
		wlFields.setLayoutData(fdlFields);

		final int FieldsRows=input.getCompareFields()==null?0:input.getCompareFields().length;
		
		colinf=new ColumnInfo[]
        {
		  new ColumnInfo(BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.ColumnInfo.Fieldname"),ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false), //$NON-NLS-1$
        };
		
		wFields=new TableView(transMeta, shell, 
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
		
		
		  // 
      // Search the fields in the background
		
      final Runnable runnable = new Runnable()
      {
          public void run()
          {
              StepMeta stepMeta = transMeta.findStep(stepname);
              if (stepMeta!=null)
              {
                  try
                  {
                  	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                     
                      // Remember these fields...
                      for (int i=0;i<row.size();i++)
                      {
                          inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                      }
                      setComboBoxes();
                  }
                  catch(KettleException e)
                  {
                  	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                  }
              }
          }
      };
      new Thread(runnable).start();


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wGet.addListener(SWT.Selection, lsGet );
		wOK.addListener    (SWT.Selection, lsOK );
		
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
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
    }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
	    wStoreValues.setSelection(input.getStoreValues());

	    for (int i=0;i<input.getCompareFields().length;i++)
	    {
	        TableItem item = wFields.table.getItem(i);
	        if (input.getCompareFields()[i]!=null) item.setText(1, input.getCompareFields()[i]);
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
		if (Const.isEmpty(wStepname.getText())) return;

		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getCompareFields()[i] = item.getText(1);
		}
		
		stepname = wStepname.getText(); // return value
        input.setStoreValues( wStoreValues.getSelection() );

		dispose();
	}
	
	
	private void get()
	{
		try
		{
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "UniqueRowsByHashSetDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
