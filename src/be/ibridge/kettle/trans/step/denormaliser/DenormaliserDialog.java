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
 *
 */

//import java.text.DateFormat;
//import java.util.Date;

package be.ibridge.kettle.trans.step.denormaliser;

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


public class DenormaliserDialog extends BaseStepDialog implements StepDialogInterface
{
    public static final String STRING_SORT_WARNING_PARAMETER = "PivotSortWarning";
    
	private Label        wlGroup;
	private TableView    wGroup;
	private FormData     fdlGroup, fdGroup;

	private Label        wlTarget;
	private TableView    wTarget;
	private FormData     fdlTarget, fdTarget;

    private Label        wlKeyField;
    private Text         wKeyField;
    private FormData     fdlKeyField, fdKeyField;

	private Button wGet, wGetAgg;
	private FormData fdGet, fdGetAgg;
	private Listener lsGet, lsGetAgg;

	private DenormaliserMeta input;

	public DenormaliserDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DenormaliserMeta)in;
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

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Denormaliser");
		
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


        // Key field...
        wlKeyField=new Label(shell, SWT.RIGHT);
        wlKeyField.setText("The key field ");
        props.setLook(wlKeyField);
        fdlKeyField=new FormData();
        fdlKeyField.left = new FormAttachment(0, 0);
        fdlKeyField.right= new FormAttachment(middle, -margin);
        fdlKeyField.top  = new FormAttachment(wStepname, margin);
        wlKeyField.setLayoutData(fdlKeyField);
        wKeyField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wKeyField);
        wKeyField.addModifyListener(lsMod);
        fdKeyField=new FormData();
        fdKeyField.left  = new FormAttachment(middle, 0);
        fdKeyField.top   = new FormAttachment(wStepname, margin);
        fdKeyField.right = new FormAttachment(100, 0);
        wKeyField.setLayoutData(fdKeyField);

		wlGroup=new Label(shell, SWT.NONE);
		wlGroup.setText("The fields that make up the grouping: ");
 		props.setLook(wlGroup);
		fdlGroup=new FormData();
		fdlGroup.left  = new FormAttachment(0, 0);
		fdlGroup.top   = new FormAttachment(wKeyField, margin);
		wlGroup.setLayoutData(fdlGroup);

		int nrKeyCols=1;
		int nrKeyRows=(input.getGroupField()!=null?input.getGroupField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo("Group field",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
		wGroup=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		fdGet = new FormData();
		fdGet.top   = new FormAttachment(wlGroup, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		fdGroup=new FormData();
		fdGroup.left  = new FormAttachment(0, 0);
		fdGroup.top   = new FormAttachment(wlGroup, margin);
		fdGroup.right = new FormAttachment(wGet, -margin);
		fdGroup.bottom= new FormAttachment(45, 0);
		wGroup.setLayoutData(fdGroup);

		// THE unpivot target field fields
		wlTarget=new Label(shell, SWT.NONE);
		wlTarget.setText("Target fields:");
 		props.setLook(wlTarget);
		fdlTarget=new FormData();
		fdlTarget.left  = new FormAttachment(0, 0);
		fdlTarget.top   = new FormAttachment(wGroup, margin);
		wlTarget.setLayoutData(fdlTarget);
		
		int UpInsRows= (input.getDenormaliserTargetField()!=null?input.getDenormaliserTargetField().length:1);
		
        String formats[] = Const.getConversionFormats();
        
		ColumnInfo[] ciTarget=new ColumnInfo[]
          {
    		 new ColumnInfo("Target fieldname",     ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo("Value fieldname",      ColumnInfo.COLUMN_TYPE_TEXT,   false),
    		 new ColumnInfo("Key value",            ColumnInfo.COLUMN_TYPE_TEXT,   false),
             new ColumnInfo("Type",                 ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getAllTypes(), false),
             new ColumnInfo("Format",               ColumnInfo.COLUMN_TYPE_CCOMBO,  formats),
             new ColumnInfo("Length",               ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Precision",            ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Currency",             ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Decimal",              ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Group",                ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Null if",              ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo("Aggregation",          ColumnInfo.COLUMN_TYPE_CCOMBO,  DenormaliserTargetField.typeAggrLongDesc, false),
          };
        
        ciTarget[ciTarget.length-1].setToolTip("This is the aggregation to will be calculated in case of a key-value collision.  None means: overwrite value");
		
		wTarget=new TableView(shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciTarget, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		wGetAgg=new Button(shell, SWT.PUSH);
		wGetAgg.setText(" &Get lookup fields ");
		fdGetAgg = new FormData();
		fdGetAgg.top   = new FormAttachment(wlTarget, margin);
		fdGetAgg.right = new FormAttachment(100, 0);
		wGetAgg.setLayoutData(fdGetAgg);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		fdTarget=new FormData();
		fdTarget.left  = new FormAttachment(0, 0);
		fdTarget.top   = new FormAttachment(wlTarget, margin);
		fdTarget.right = new FormAttachment(wGetAgg, -margin);
		fdTarget.bottom= new FormAttachment(wOK, -margin);
		wTarget.setLayoutData(fdTarget);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetAgg   = new Listener() { public void handleEvent(Event e) { getAgg(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetAgg.addListener (SWT.Selection, lsGetAgg );
		wCancel.addListener(SWT.Selection, lsCancel);
		
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
		int i;
		log.logDebug(toString(), "getting key info...");
		
        if (input.getKeyField()!= null) wKeyField.setText(input.getKeyField());

		if (input.getGroupField()!=null)
		for (i=0;i<input.getGroupField().length;i++)
		{
			TableItem item = wGroup.table.getItem(i);
			if (input.getGroupField()[i]   !=null) item.setText(1, input.getGroupField()[i]);
		}
		
		if (input.getDenormaliserTargetField()!=null)
		for (i=0;i<input.getDenormaliserTargetField().length;i++)
		{
            DenormaliserTargetField field = input.getDenormaliserTargetField()[i];
            
			TableItem item = wTarget.table.getItem(i);
            
			if (field.getTargetName() != null )           item.setText( 1, field.getTargetName());
            if (field.getFieldName() != null )            item.setText( 2, field.getFieldName());
            if (field.getKeyValue() != null )             item.setText( 3, field.getKeyValue());
            if (field.getTargetTypeDesc() != null )       item.setText( 4, field.getTargetTypeDesc());
            if (field.getTargetFormat() != null )         item.setText( 5, field.getTargetFormat());
            if (field.getTargetLength()>=0)               item.setText( 6, ""+field.getTargetLength());
            if (field.getTargetPrecision()>=0)            item.setText( 7, ""+field.getTargetPrecision());
            if (field.getTargetCurrencySymbol() != null ) item.setText( 8, field.getTargetCurrencySymbol());
            if (field.getTargetDecimalSymbol() != null )  item.setText( 9, field.getTargetDecimalSymbol());
            if (field.getTargetGroupingSymbol() != null ) item.setText(10, field.getTargetGroupingSymbol());
            if (field.getTargetNullString() != null )     item.setText(11, field.getTargetNullString());
            if (field.getTargetAggregationType()>=0 )     item.setText(12, field.getTargetAggregationTypeDescLong());
        }
		
		wStepname.selectAll();
		wGroup.setRowNums();
		wGroup.optWidth(true);
		wTarget.setRowNums();
		wTarget.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		dispose();
	}
	
	private void ok()
	{
		int sizegroup = wGroup.nrNonEmpty();
		int nrfields = wTarget.nrNonEmpty();
        
        input.setKeyField( wKeyField.getText() );

		input.allocate(sizegroup, nrfields);
				
		for (int i=0;i<sizegroup;i++)
		{
			TableItem item = wGroup.getNonEmpty(i);
			input.getGroupField()[i]    = item.getText(1);
		}
		
		for (int i=0;i<nrfields;i++)
		{
            DenormaliserTargetField field = new DenormaliserTargetField();
            
			TableItem item      = wTarget.getNonEmpty(i);
            field.setTargetName( item.getText(1) );
            field.setFieldName( item.getText(2) );
            field.setKeyValue( item.getText(3) );
            field.setTargetType( item.getText(4) );
            field.setTargetFormat( item.getText(5) );
            field.setTargetLength( Const.toInt( item.getText(6), -1) );
            field.setTargetPrecision( Const.toInt( item.getText(7), -1) );
            field.setTargetCurrencySymbol( item.getText(8) );
            field.setTargetDecimalSymbol( item.getText(9) );
            field.setTargetGroupingSymbol( item.getText(10) );
            field.setTargetNullString( item.getText(11) );
            field.setTargetAggregationType( item.getText(12) );
            
            input.getDenormaliserTargetField()[i] = field;
		}
		
		stepname = wStepname.getText();
        
        if ( "Y".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") ))
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                 "Warning!", 
                 null,
                 "The 'Unpivot' function needs the input to be sorted on the specified grouping keys." + Const.CR + "If you don't sort the input, the results may not be correct!"+Const.CR,
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
				Table table=wGroup.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(1, v.getName());
				}
				wGroup.removeEmptyRows();
				wGroup.setRowNums();
				wGroup.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}
	}

	private void getAgg()
	{
        // The grouping fields: ignore those.
        wGroup.removeEmptyRows();
        String[] groupingFields = wGroup.getItems(0);
		try
		{
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                int nr=1;
				Table table=wTarget.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
                    if (Const.indexOfString(v.getName(), groupingFields)<0) // Not a grouping field
                    {
                        if (!wKeyField.getText().equalsIgnoreCase(v.getName())) // Not the key field
                        {
        					TableItem ti = new TableItem(table, SWT.NONE);
        					ti.setText(1, "Field"+nr); // the target fieldname
        					ti.setText(2, v.getName());
                            ti.setText(4, v.getTypeDesc());
        					if (v.getLength()>=0) ti.setText(6, ""+v.getLength());
                            if (v.getPrecision()>=0) ti.setText(7, ""+v.getPrecision());
                        }
                    }
				}
				wTarget.removeEmptyRows();
				wTarget.setRowNums();
				wTarget.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get fields from previous steps because of an error", ke);
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
