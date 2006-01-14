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
 * Created on 5-aug-2004
 *
 */

//import java.text.DateFormat;
//import java.util.Date;

package be.ibridge.kettle.trans.step.databasejoin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;


public class DatabaseJoinDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlSQL;
	private Text         wSQL;
	private FormData     fdlSQL, fdSQL;

	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlOuter;
	private Button       wOuter;
	private FormData     fdlOuter, fdOuter;

	private Label        wlParam;
	private TableView    wParam;
	private FormData     fdlParam, fdParam;

	private Button wGet;
	private Listener lsGet;

	private DatabaseJoinMeta input;

	public DatabaseJoinDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DatabaseJoinMeta)in;
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
		shell.setText("Database Join");
		
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
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

		// SQL editor...
		wlSQL=new Label(shell, SWT.NONE);
		wlSQL.setText("SQL ");
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(wConnection, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wSQL=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wlSQL, margin  );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(60, 0     );
		wSQL.setLayoutData(fdSQL);
		
		// Limit the number of lines returns
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText("Number of rows to return");
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left   = new FormAttachment(0, 0);
		fdlLimit.right  = new FormAttachment(middle, -margin);
		fdlLimit.top    = new FormAttachment(wSQL, margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left   = new FormAttachment(middle, 0);
		fdLimit.right  = new FormAttachment(100, 0);
		fdLimit.top    = new FormAttachment(wSQL, margin);
		wLimit.setLayoutData(fdLimit);

		// Outer join?
		wlOuter=new Label(shell, SWT.RIGHT);
		wlOuter.setText("Outer join?");
		wlOuter.setToolTipText("Return al least the input row and add NULLs for the lookup values");
 		props.setLook(wlOuter);
		fdlOuter=new FormData();
		fdlOuter.left = new FormAttachment(0, 0);
		fdlOuter.right= new FormAttachment(middle, -margin);
		fdlOuter.top  = new FormAttachment(wLimit, margin);
		wlOuter.setLayoutData(fdlOuter);
		wOuter=new Button(shell, SWT.CHECK);
 		props.setLook(wOuter);
		wOuter.setToolTipText(wlOuter.getToolTipText());
		fdOuter=new FormData();
		fdOuter.left = new FormAttachment(middle, 0);
		fdOuter.top  = new FormAttachment(wLimit, margin);
		wOuter.setLayoutData(fdOuter);
		wOuter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(" &Get Fields ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);

		// The parameters
		wlParam=new Label(shell, SWT.NONE);
		wlParam.setText("The parameters to use: ");
 		props.setLook(wlParam);
		fdlParam=new FormData();
		fdlParam.left  = new FormAttachment(0, 0);
		fdlParam.top   = new FormAttachment(wOuter, margin);
		wlParam.setLayoutData(fdlParam);

		int nrKeyCols=2;
		int nrKeyRows=(input.getParameterField()!=null?input.getParameterField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo("Parameter fieldname",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		ciKey[1]=new ColumnInfo("Parameter Type",       ColumnInfo.COLUMN_TYPE_CCOMBO, Value.getTypes() );
		
		wParam=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdParam=new FormData();
		fdParam.left  = new FormAttachment(0, 0);
		fdParam.top   = new FormAttachment(wlParam, margin);
		fdParam.right = new FormAttachment(100, 0);
		fdParam.bottom= new FormAttachment(wOK, -2*margin);
		wParam.setLayoutData(fdParam);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		
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
		
		wSQL.setText(input.getSql());
		wLimit.setText(""+input.getRowLimit());
		wOuter.setSelection(input.isOuterJoin());
		
		if (input.getParameterField()!=null)
		for (i=0;i<input.getParameterField().length;i++)
		{
			TableItem item = wParam.table.getItem(i);
			if (input.getParameterField()[i]  !=null) item.setText(1, input.getParameterField()[i]);
			if (input.getParameterType() [i]  !=0   ) item.setText(2, Value.getTypeDesc( input.getParameterType()[i] ));
		}
		
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();
		wParam.setRowNums();
		wParam.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{
		int nrparam  = wParam.nrNonEmpty();
		
		input.allocate(nrparam);
		
		input.setRowLimit( Const.toInt( wLimit.getText(), 0) );
		input.setSql( wSQL.getText() );
		
		input.setOuterJoin( wOuter.getSelection() );
		
		log.logDebug(toString(), "Found "+nrparam+" parameters");
		for (int i=0;i<nrparam;i++)
		{
			TableItem item = wParam.getNonEmpty(i);
			input.getParameterField()[i]   = item.getText(1);
			input.getParameterType() [i]   = Value.getType( item.getText(2) );
		}

		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );

		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Please select a valid connection!");
			mb.setText("ERROR");
			mb.open();
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
				Table table=wParam.table;
				for (int i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(1, v.getName());
					ti.setText(2, v.getTypeDesc());
				}
				wParam.removeEmptyRows();
				wParam.setRowNums();
				wParam.optWidth(true);
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
