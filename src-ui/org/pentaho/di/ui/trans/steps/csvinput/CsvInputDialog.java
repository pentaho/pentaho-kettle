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

package org.pentaho.di.ui.trans.steps.csvinput;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.csvinput.Messages;

public class CsvInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CsvInputMeta inputMeta;
	
	private TextVar      wFilename;
	private Button       wbbFilename; // Browse for a file
	private TextVar      wDelimiter;
	private TextVar      wEnclosure;
	private TextVar      wBufferSize;
	private Button       wLazyConversion;
	private Button       wHeaderPresent;
	private TableView    wFields;
	
	public CsvInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		inputMeta=(CsvInputMeta)in;
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
		shell.setText(Messages.getString("CsvInputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("CsvInputDialog.Stepname.Label")); //$NON-NLS-1$
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
		// The filename browse button
		//
        wbbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(Messages.getString("System.Button.Browse"));
        wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbFilename = new FormData();
        fdbFilename.top  = new FormAttachment(lastControl, margin);
        fdbFilename.right= new FormAttachment(100, 0);
        wbbFilename.setLayoutData(fdbFilename);

        // The field itself...
        //
		Label wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("CsvInputDialog.Filename.Label")); //$NON-NLS-1$
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
		fdFilename.right= new FormAttachment(wbbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		lastControl = wFilename;
		
		// delimiter
		Label wlDelimiter = new Label(shell, SWT.RIGHT);
		wlDelimiter.setText(Messages.getString("CsvInputDialog.Delimiter.Label")); //$NON-NLS-1$
 		props.setLook(wlDelimiter);
		FormData fdlDelimiter = new FormData();
		fdlDelimiter.top  = new FormAttachment(lastControl, margin);
		fdlDelimiter.left = new FormAttachment(0, 0);
		fdlDelimiter.right= new FormAttachment(middle, -margin);
		wlDelimiter.setLayoutData(fdlDelimiter);
		wDelimiter=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDelimiter);
		wDelimiter.addModifyListener(lsMod);
		FormData fdDelimiter = new FormData();
		fdDelimiter.top  = new FormAttachment(lastControl, margin);
		fdDelimiter.left = new FormAttachment(middle, 0);
		fdDelimiter.right= new FormAttachment(100, 0);
		wDelimiter.setLayoutData(fdDelimiter);
		lastControl = wDelimiter;

		// delimiter
		Label wlEnclosure = new Label(shell, SWT.RIGHT);
		wlEnclosure.setText(Messages.getString("CsvInputDialog.Enclosure.Label")); //$NON-NLS-1$
 		props.setLook(wlEnclosure);
		FormData fdlEnclosure = new FormData();
		fdlEnclosure.top  = new FormAttachment(lastControl, margin);
		fdlEnclosure.left = new FormAttachment(0, 0);
		fdlEnclosure.right= new FormAttachment(middle, -margin);
		wlEnclosure.setLayoutData(fdlEnclosure);
		wEnclosure=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wEnclosure);
		wEnclosure.addModifyListener(lsMod);
		FormData fdEnclosure = new FormData();
		fdEnclosure.top  = new FormAttachment(lastControl, margin);
		fdEnclosure.left = new FormAttachment(middle, 0);
		fdEnclosure.right= new FormAttachment(100, 0);
		wEnclosure.setLayoutData(fdEnclosure);
		lastControl = wEnclosure;

		// bufferSize
		//
		Label wlBufferSize = new Label(shell, SWT.RIGHT);
		wlBufferSize.setText(Messages.getString("CsvInputDialog.BufferSize.Label")); //$NON-NLS-1$
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
		wlLazyConversion.setText(Messages.getString("CsvInputDialog.LazyConversion.Label")); //$NON-NLS-1$
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
		wlHeaderPresent.setText(Messages.getString("CsvInputDialog.HeaderPresent.Label")); //$NON-NLS-1$
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
             new ColumnInfo(Messages.getString("CsvInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("CsvInputDialog.TypeColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  ValueMeta.getTypes(), true ),
             new ColumnInfo(Messages.getString("CsvInputDialog.FormatColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  Const.getConversionFormats()),
             new ColumnInfo(Messages.getString("CsvInputDialog.LengthColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("CsvInputDialog.PrecisionColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("CsvInputDialog.CurrencyColumn.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("CsvInputDialog.DecimalColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
             new ColumnInfo(Messages.getString("CsvInputDialog.GroupColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
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
		wFilename.addSelectionListener( lsDef );
		wDelimiter.addSelectionListener( lsDef );
		wEnclosure.addSelectionListener( lsDef );
		wBufferSize.addSelectionListener( lsDef );
		
		// Listen to the browse button next to the file name
		wbbFilename.addSelectionListener(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e) 
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*.txt;*.csv", "*.csv", "*.txt", "*"});
						if (wFilename.getText()!=null)
						{
							String fname = transMeta.environmentSubstitute(wFilename.getText());
							dialog.setFileName( fname );
						}
						
						dialog.setFilterNames(new String[] {Messages.getString("System.FileType.CSVFiles")+", "+Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.CSVFiles"), Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.AllFiles")});
						
						if (dialog.open()!=null)
						{
							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
							wFilename.setText(str);
						}
					}
				}
			);

		
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
		wDelimiter.setText(Const.NVL(inputMeta.getDelimiter(), ""));
		wEnclosure.setText(Const.NVL(inputMeta.getEnclosure(), ""));
		wBufferSize.setText(Const.NVL(inputMeta.getBufferSize(), ""));
		wLazyConversion.setSelection(inputMeta.isLazyConversionActive());
		wHeaderPresent.setSelection(inputMeta.isHeaderPresent());

		for (int i=0;i<inputMeta.getFieldNames().length;i++) {
			TableItem item = new TableItem(wFields.table, SWT.NONE);
			int colnr=1;
			item.setText(colnr++, Const.NVL(inputMeta.getFieldNames()[i], ""));
			item.setText(colnr++, ValueMeta.getTypeDesc(inputMeta.getFieldTypes()[i]));
			item.setText(colnr++, Const.NVL(inputMeta.getFieldFormat()[i], ""));
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
		inputMeta.setDelimiter(wDelimiter.getText());
		inputMeta.setEnclosure(wEnclosure.getText());
		inputMeta.setBufferSize(wBufferSize.getText());
		inputMeta.setLazyConversionActive(wLazyConversion.getSelection());
		inputMeta.setHeaderPresent(wHeaderPresent.getSelection());

    	int nrNonEmptyFields = wFields.nrNonEmpty(); 
    	inputMeta.allocate(nrNonEmptyFields);

		for (int i=0;i<nrNonEmptyFields;i++) {
			TableItem item = wFields.getNonEmpty(i);
			int colnr=1;
			inputMeta.getFieldNames()[i] = item.getText(colnr++);
			inputMeta.getFieldTypes()[i] = ValueMeta.getType( item.getText(colnr++) );
			inputMeta.getFieldFormat()[i] = item.getText(colnr++);
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
