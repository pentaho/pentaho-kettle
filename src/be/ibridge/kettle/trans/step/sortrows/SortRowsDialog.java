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

package be.ibridge.kettle.trans.step.sortrows;

import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class SortRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlSortDir;
	private Button       wbSortDir;
	private Button       wbcSortDir;
	private Text         wSortDir;
	private FormData     fdlSortDir, fdbSortDir, fdbcSortDir, fdSortDir;

	private Label        wlPrefix;
	private Text         wPrefix;
	private FormData     fdlPrefix, fdPrefix;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private SortRowsMeta input;
	
	public SortRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SortRowsMeta)in;
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
				input. setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Sort rows");
		
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

		// Connection line
		wlSortDir=new Label(shell, SWT.RIGHT);
		wlSortDir.setText("Sort directory ");
 		props.setLook(wlSortDir);
		fdlSortDir=new FormData();
		fdlSortDir.left = new FormAttachment(0, 0);
		fdlSortDir.right= new FormAttachment(middle, -margin);
		fdlSortDir.top  = new FormAttachment(wStepname, margin);
		wlSortDir.setLayoutData(fdlSortDir);

		wbSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSortDir);
		wbSortDir.setText("&Browse...");
		fdbSortDir=new FormData();
		fdbSortDir.right= new FormAttachment(100, 0);
		fdbSortDir.top  = new FormAttachment(wStepname, margin);
		wbSortDir.setLayoutData(fdbSortDir);

		wbcSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbcSortDir);
		wbcSortDir.setText("&Variable...");
		fdbcSortDir=new FormData();
		fdbcSortDir.right= new FormAttachment(wbSortDir, -margin);
		fdbcSortDir.top  = new FormAttachment(wStepname, margin);
		wbcSortDir.setLayoutData(fdbcSortDir);

		wSortDir=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSortDir.setText("temp");
 		props.setLook(wSortDir);
		wSortDir.addModifyListener(lsMod);
		fdSortDir=new FormData();
		fdSortDir.left = new FormAttachment(middle, 0);
		fdSortDir.top  = new FormAttachment(wStepname, margin);
		fdSortDir.right= new FormAttachment(wbcSortDir, -margin);
		wSortDir.setLayoutData(fdSortDir);
		
		wbSortDir.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
				dd.setFilterPath(wSortDir.getText());
				String dir = dd.open();
				if (dir!=null)
				{
					wSortDir.setText(dir);
				}
			}
		});

		// Whenever something changes, set the tooltip to the expanded version:
		wSortDir.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wSortDir.setToolTipText(Const.replEnv( wSortDir.getText() ) );
				}
			}
		);

		// Listen to the Variable... button
		wbcSortDir.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					Properties sp = System.getProperties();
					Enumeration keys = sp.keys();
					int size = sp.values().size();
					String key[] = new String[size];
					String val[] = new String[size];
					String str[] = new String[size];
					int i=0;
					while (keys.hasMoreElements())
					{
						key[i] = (String)keys.nextElement();
						val[i] = sp.getProperty(key[i]);
						str[i] = key[i]+"  ["+val[i]+"]";
						i++;
					}
					
					EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, str, "Select an Environment Variable", "Select an Environment Variable");
					if (esd.open()!=null)
					{
						int nr = esd.getSelectionNr();
						wSortDir.insert("%%"+key[nr]+"%%");
					}
				}
				
			}
		);

		// Table line...
		wlPrefix=new Label(shell, SWT.RIGHT);
		wlPrefix.setText("TMP-file prefix ");
 		props.setLook(wlPrefix);
		fdlPrefix=new FormData();
		fdlPrefix.left = new FormAttachment(0, 0);
		fdlPrefix.right= new FormAttachment(middle, -margin);
		fdlPrefix.top  = new FormAttachment(wbSortDir, margin*2);
		wlPrefix.setLayoutData(fdlPrefix);
		wPrefix=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPrefix);
		wPrefix.addModifyListener(lsMod);
		fdPrefix=new FormData();
		fdPrefix.left  = new FormAttachment(middle, 0);
		fdPrefix.top   = new FormAttachment(wbSortDir, margin*2);
		fdPrefix.right = new FormAttachment(100, 0);
		wPrefix.setLayoutData(fdPrefix);
		wPrefix.setText("srt");

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Fields :");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wPrefix, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=2;
		final int FieldsRows=input.getFieldName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Fieldname",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[1]=new ColumnInfo("Ascending",  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "Y", "N" } );
		
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
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
		wFields.setLayoutData(fdFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wSortDir.addSelectionListener( lsDef );
		wPrefix.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				Point size = shell.getSize();
				wFields.setSize(size.x-10, size.y-50);
				wFields.table.setSize(size.x-10, size.y-50);
				wFields.redraw();
			}
		};
		shell.addListener(SWT.Resize, lsResize);

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
		if (input.getPrefix() != null) wPrefix.setText(input.getPrefix());
		if (input.getDirectory() != null) wSortDir.setText(input.getDirectory());
		
		int i;
		Table table = wFields.table;
		table.removeAll();
		for (i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1, input.getFieldName()[i]);
			ti.setText(2, input.getAscending()[i]?"Y":"N");
		}
		if (table.getItemCount()==0) // at least 1!
		{
			new TableItem(table, SWT.NONE);
		}

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
		int i;
		stepname = wStepname.getText(); // return value

		// copy info to SortRowsMeta class (input)
		input.setPrefix( wPrefix.getText() );
		input.setDirectory( wSortDir.getText() );

		//Table table = wFields.table;
		int nrfields = wFields.nrNonEmpty();

		input.allocate(nrfields);
		
		for (i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
			input.getAscending()[i] = "Y".equalsIgnoreCase(ti.getText(2));
		}
		
		dispose();
	}
	
	private void get()
	{
		try
		{
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table=wFields.table;
				count=table.getItemCount();
				for (i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, ""+(count+i+1));
					ti.setText(1, v.getName());
					ti.setText(2, "Y");
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, props, "Get fields failed", "Unable to get the fields from previous steps because of an error", ke);
		}

	}
}
