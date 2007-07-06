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

package org.pentaho.di.trans.steps.fixedinput;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.widget.ColumnInfo;
import org.pentaho.di.core.widget.TableView;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;


public class FixedInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private FixedInputMeta inputMeta;
	
	private TextVar      wFilename;
	private TextVar      wLineWidth;
	private Button       wLineFeedPresent;
	private TextVar      wBufferSize;
	private Button       wLazyConversion;
	private Button       wHeaderPresent;
	private TableView    wFields;
	
	public FixedInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		inputMeta=(FixedInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, inputMeta);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				inputMeta.setChanged();
			}
		};
		changed = inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("FixedInputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("FixedInputDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname;
		
		// Filename...
		//
		Label wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("FixedInputDialog.Filename.Label")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		FormData fdlFilename = new FormData();
		fdlFilename.top  = new FormAttachment(lastControl, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top  = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(100, 0);
		wFilename.setLayoutData(fdFilename);
		lastControl = wFilename;
		
		// delimiter
		Label wlLineWidth = new Label(shell, SWT.RIGHT);
		wlLineWidth.setText(Messages.getString("FixedInputDialog.LineWidth.Label")); //$NON-NLS-1$
 		props.setLook(wlLineWidth);
		FormData fdlLineWidth = new FormData();
		fdlLineWidth.top  = new FormAttachment(lastControl, margin);
		fdlLineWidth.left = new FormAttachment(0, 0);
		fdlLineWidth.right= new FormAttachment(middle, -margin);
		wlLineWidth.setLayoutData(fdlLineWidth);
		wLineWidth=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLineWidth);
		wLineWidth.addModifyListener(lsMod);
		FormData fdLineWidth = new FormData();
		fdLineWidth.top  = new FormAttachment(lastControl, margin);
		fdLineWidth.left = new FormAttachment(middle, 0);
		fdLineWidth.right= new FormAttachment(100, 0);
		wLineWidth.setLayoutData(fdLineWidth);
		lastControl = wLineWidth;

		// delimiter
		Label wlLineFeedPresent = new Label(shell, SWT.RIGHT);
		wlLineFeedPresent.setText(Messages.getString("FixedInputDialog.LineFeedPresent.Label")); //$NON-NLS-1$
 		props.setLook(wlLineFeedPresent);
		FormData fdlLineFeedPresent = new FormData();
		fdlLineFeedPresent.top  = new FormAttachment(lastControl, margin);
		fdlLineFeedPresent.left = new FormAttachment(0, 0);
		fdlLineFeedPresent.right= new FormAttachment(middle, -margin);
		wlLineFeedPresent.setLayoutData(fdlLineFeedPresent);
		wLineFeedPresent=new Button(shell, SWT.CHECK);
 		props.setLook(wLineFeedPresent);
		FormData fdLineFeedPresent = new FormData();
		fdLineFeedPresent.top  = new FormAttachment(lastControl, margin);
		fdLineFeedPresent.left = new FormAttachment(middle, 0);
		fdLineFeedPresent.right= new FormAttachment(100, 0);
		wLineFeedPresent.setLayoutData(fdLineFeedPresent);
		lastControl = wLineFeedPresent;

		// bufferSize
		//
		Label wlBufferSize = new Label(shell, SWT.RIGHT);
		wlBufferSize.setText(Messages.getString("FixedInputDialog.BufferSize.Label")); //$NON-NLS-1$
 		props.setLook(wlBufferSize);
		FormData fdlBufferSize = new FormData();
		fdlBufferSize.top  = new FormAttachment(lastControl, margin);
		fdlBufferSize.left = new FormAttachment(0, 0);
		fdlBufferSize.right= new FormAttachment(middle, -margin);
		wlBufferSize.setLayoutData(fdlBufferSize);
		wBufferSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBufferSize);
		wBufferSize.addModifyListener(lsMod);
		FormData fdBufferSize = new FormData();
		fdBufferSize.top  = new FormAttachment(lastControl, margin);
		fdBufferSize.left = new FormAttachment(middle, 0);
		fdBufferSize.right= new FormAttachment(100, 0);
		wBufferSize.setLayoutData(fdBufferSize);
		lastControl = wBufferSize;
		
		// performingLazyConversion?
		//
		Label wlLazyConversion = new Label(shell, SWT.RIGHT);
		wlLazyConversion.setText(Messages.getString("FixedInputDialog.LazyConversion.Label")); //$NON-NLS-1$
 		props.setLook(wlLazyConversion);
		FormData fdlLazyConversion = new FormData();
		fdlLazyConversion.top  = new FormAttachment(lastControl, margin);
		fdlLazyConversion.left = new FormAttachment(0, 0);
		fdlLazyConversion.right= new FormAttachment(middle, -margin);
		wlLazyConversion.setLayoutData(fdlLazyConversion);
		wLazyConversion = new Button(shell, SWT.CHECK);
 		props.setLook(wLazyConversion);
		FormData fdLazyConversion = new FormData();
		fdLazyConversion.top  = new FormAttachment(lastControl, margin);
		fdLazyConversion.left = new FormAttachment(middle, 0);
		fdLazyConversion.right= new FormAttachment(100, 0);
		wLazyConversion.setLayoutData(fdLazyConversion);
		lastControl = wLazyConversion;

		// header row?
		//
		Label wlHeaderPresent = new Label(shell, SWT.RIGHT);
		wlHeaderPresent.setText(Messages.getString("FixedInputDialog.HeaderPresent.Label")); //$NON-NLS-1$
 		props.setLook(wlHeaderPresent);
		FormData fdlHeaderPresent = new FormData();
		fdlHeaderPresent.top  = new FormAttachment(lastControl, margin);
		fdlHeaderPresent.left = new FormAttachment(0, 0);
		fdlHeaderPresent.right= new FormAttachment(middle, -margin);
		wlHeaderPresent.setLayoutData(fdlHeaderPresent);
		wHeaderPresent = new Button(shell, SWT.CHECK);
 		props.setLook(wHeaderPresent);
		FormData fdHeaderPresent = new FormData();
		fdHeaderPresent.top  = new FormAttachment(lastControl, margin);
		fdHeaderPresent.left = new FormAttachment(middle, 0);
		fdHeaderPresent.right= new FormAttachment(100, 0);
		wHeaderPresent.setLayoutData(fdHeaderPresent);
		lastControl = wHeaderPresent;

		// Some buttons first, so that the dialog scales nicely...
		//
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);


		// Fields
        ColumnInfo[] colinf=new ColumnInfo[]
            {
             new ColumnInfo(Messages.getString("FixedInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.TypeColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.getTypes(), true ),
             new ColumnInfo(Messages.getString("FixedInputDialog.FormatColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  Const.getConversionFormats()),
             new ColumnInfo(Messages.getString("FixedInputDialog.WidthColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.LengthColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.PrecisionColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.CurrencyColumn.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.DecimalColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("FixedInputDialog.GroupColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
            };
        
        wFields=new TableView(transMeta, shell, 
                              SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              1,  
                              lsMod,
                              props
                              );

        FormData fdFields = new FormData();
        fdFields.top   = new FormAttachment(lastControl, margin*2);
        fdFields.bottom= new FormAttachment(wOK, -margin*2);
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);
        
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
		inputMeta.setChanged(changed);
	
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
		wStepname.setText(stepname);
		wFilename.setText(Const.NVL(inputMeta.getFilename(), ""));
		wLineWidth.setText(Const.NVL(inputMeta.getLineWidth(), ""));
		wLineFeedPresent.setSelection(inputMeta.isLineFeedPresent());
		wBufferSize.setText(Const.NVL(inputMeta.getBufferSize(), ""));
		wLazyConversion.setSelection(inputMeta.isLazyConversionActive());
		wHeaderPresent.setSelection(inputMeta.isHeaderPresent());

		for (int i=0;i<inputMeta.getFieldNames().length;i++) {
			TableItem item = new TableItem(wFields.table, SWT.NONE);
			int colnr=1;
			item.setText(colnr++, Const.NVL(inputMeta.getFieldNames()[i], ""));
			item.setText(colnr++, ValueMeta.getTypeDesc(inputMeta.getFieldTypes()[i]));
			item.setText(colnr++, Const.NVL(inputMeta.getFieldFormat()[i], ""));
			item.setText(colnr++, inputMeta.getFieldWidth()[i]>=0?Integer.toString(inputMeta.getFieldWidth()[i]):"") ;
			item.setText(colnr++, inputMeta.getFieldLength()[i]>=0?Integer.toString(inputMeta.getFieldLength()[i]):"") ;
			item.setText(colnr++, inputMeta.getFieldPrecision()[i]>=0?Integer.toString(inputMeta.getFieldPrecision()[i]):"") ;
			item.setText(colnr++, Const.NVL(inputMeta.getFieldCurrency()[i], ""));
			item.setText(colnr++, Const.NVL(inputMeta.getFieldDecimal()[i], ""));
			item.setText(colnr++, Const.NVL(inputMeta.getFieldGrouping()[i], ""));
		}
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		inputMeta.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		stepname = wStepname.getText(); // return value
		
		inputMeta.setFilename(wFilename.getText());
		inputMeta.setLineWidth(wLineWidth.getText());
		inputMeta.setBufferSize(wBufferSize.getText());
		inputMeta.setLazyConversionActive(wLazyConversion.getSelection());
		inputMeta.setHeaderPresent(wHeaderPresent.getSelection());
		inputMeta.setLineFeedPresent(wLineFeedPresent.getSelection());

		inputMeta.allocate(wFields.nrNonEmpty());

		for (int i=0;i<wFields.nrNonEmpty();i++) {
			TableItem item = wFields.getNonEmpty(i);
			int colnr=1;
			inputMeta.getFieldNames()[i] = item.getText(colnr++);
			inputMeta.getFieldTypes()[i] = ValueMeta.getType( item.getText(colnr++) );
			inputMeta.getFieldFormat()[i] = item.getText(colnr++);
			inputMeta.getFieldWidth()[i] = Const.toInt(item.getText(colnr++), -1);
			inputMeta.getFieldLength()[i] = Const.toInt(item.getText(colnr++), -1);
			inputMeta.getFieldPrecision()[i] = Const.toInt(item.getText(colnr++), -1);
			inputMeta.getFieldCurrency()[i] = item.getText(colnr++);
			inputMeta.getFieldDecimal()[i] = item.getText(colnr++);
			inputMeta.getFieldGrouping()[i] = item.getText(colnr++);
		}
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
		
		inputMeta.setChanged();
		dispose();
	}
}
