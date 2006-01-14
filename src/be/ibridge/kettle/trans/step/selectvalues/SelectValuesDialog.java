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
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.trans.step.selectvalues;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
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


public class SelectValuesDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wSelectTab, wRemoveTab, wMetaTab;

	private Composite    wSelectComp, wRemoveComp, wMetaComp;
	private FormData     fdSelectComp, fdRemoveComp, fdMetaComp;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Label        wlRemove;
	private TableView    wRemove;
	private FormData     fdlRemove, fdRemove;

	private Label        wlMeta;
	private TableView    wMeta;
	private FormData     fdlMeta, fdMeta;
	
	private Button       wGetSelect, wGetRemove, wGetMeta;
	private FormData     fdGetSelect, fdGetRemove, fdGetMeta;

	private SelectValuesMeta input;
	
	public SelectValuesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SelectValuesMeta)in;
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
		changed = input.hasChanged();

		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Select / Rename values");
		
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

		// The folders!
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF SELECT TAB ///
		//////////////////////////
		
		wSelectTab=new CTabItem(wTabFolder, SWT.NONE);
		wSelectTab.setText("Select && Alter");
		
		wSelectComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wSelectComp);

		FormLayout selectLayout = new FormLayout();
		selectLayout.marginWidth  = margin;
		selectLayout.marginHeight = margin;
		wSelectComp.setLayout(selectLayout);
		
		wlFields=new Label(wSelectComp, SWT.NONE);
		wlFields.setText("Fields :");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=4;
		final int FieldsRows=input.getSelectName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Fieldname", ColumnInfo.COLUMN_TYPE_TEXT,   false );
		colinf[1]=new ColumnInfo("Rename to", ColumnInfo.COLUMN_TYPE_TEXT,   false );
		colinf[2]=new ColumnInfo("Length",    ColumnInfo.COLUMN_TYPE_TEXT,   false );
		colinf[3]=new ColumnInfo("Precision", ColumnInfo.COLUMN_TYPE_TEXT,   false );
		
		wFields=new TableView(wSelectComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
						      props
						      );

		wGetSelect = new Button(wSelectComp, SWT.PUSH);
		wGetSelect.setText("Get fields to select");
		wGetSelect.addListener(SWT.Selection, lsGet);
		fdGetSelect = new FormData();
		fdGetSelect.right = new FormAttachment(100, 0);
		fdGetSelect.top   = new FormAttachment(50, 0);
		wGetSelect.setLayoutData(fdGetSelect);
		
		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(wGetSelect, -margin);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

		fdSelectComp=new FormData();
		fdSelectComp.left  = new FormAttachment(0, 0);
		fdSelectComp.top   = new FormAttachment(0, 0);
		fdSelectComp.right = new FormAttachment(100, 0);
		fdSelectComp.bottom= new FormAttachment(100, 0);
		wSelectComp.setLayoutData(fdSelectComp);
	
		wSelectComp.layout();
		wSelectTab.setControl(wSelectComp);

		/////////////////////////////////////////////////////////////
		/// END OF SELECT TAB
		/////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////
		// START OF REMOVE TAB 
		/////////////////////////////////////////////////////////////
		wRemoveTab=new CTabItem(wTabFolder, SWT.NONE);
		wRemoveTab.setText("Remove");

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = margin;
		contentLayout.marginHeight = margin;
		
		wRemoveComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wRemoveComp);
		wRemoveComp.setLayout(contentLayout);

		wlRemove=new Label(wRemoveComp, SWT.NONE);
		wlRemove.setText("Fields to remove :");
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(0, 0);
		wlRemove.setLayoutData(fdlRemove);

		final int RemoveCols=1;
		final int RemoveRows=input.getDeleteName().length;
		
		ColumnInfo[] colrem=new ColumnInfo[RemoveCols];
		colrem[0]=new ColumnInfo("Fieldname", ColumnInfo.COLUMN_TYPE_TEXT,   false );
		
		wRemove=new TableView(wRemoveComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colrem, 
						      RemoveRows,  
						      lsMod,
						      props
						      );

		wGetRemove = new Button(wRemoveComp, SWT.PUSH);
		wGetRemove.setText("Get fields to remove");
		wGetRemove.addListener(SWT.Selection, lsGet);
		fdGetRemove = new FormData();
		fdGetRemove.right = new FormAttachment(100, 0);
		fdGetRemove.top   = new FormAttachment(50, 0);
		wGetRemove.setLayoutData(fdGetRemove);

		fdRemove=new FormData();
		fdRemove.left = new FormAttachment(0, 0);
		fdRemove.top  = new FormAttachment(wlRemove, margin);
		fdRemove.right  = new FormAttachment(wGetRemove, -margin);
		fdRemove.bottom = new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);
		
		fdRemoveComp = new FormData();
		fdRemoveComp.left  = new FormAttachment(0, 0);
		fdRemoveComp.top   = new FormAttachment(0, 0);
		fdRemoveComp.right = new FormAttachment(100, 0);
		fdRemoveComp.bottom= new FormAttachment(100, 0);
		wRemoveComp.setLayoutData(fdRemoveComp);

		wRemoveComp.layout();
		wRemoveTab.setControl(wRemoveComp);

		/////////////////////////////////////////////////////////////
		/// END OF REMOVE TAB
		/////////////////////////////////////////////////////////////

		
		//////////////////////////
		// START OF META TAB  ///
		//////////////////////////
		
		wMetaTab=new CTabItem(wTabFolder, SWT.NONE);
		wMetaTab.setText("Meta-data");
		
		wMetaComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wMetaComp);

		FormLayout metaLayout = new FormLayout();
		metaLayout.marginWidth  = margin;
		metaLayout.marginHeight = margin;
		wMetaComp.setLayout(metaLayout);
		
		wlMeta=new Label(wMetaComp, SWT.NONE);
		wlMeta.setText("Fields to alter the meta-data for :");
 		props.setLook(wlMeta);
		fdlMeta=new FormData();
		fdlMeta.left = new FormAttachment(0, 0);
		fdlMeta.top  = new FormAttachment(0, 0);
		wlMeta.setLayoutData(fdlMeta);
		
		final int MetaCols=5;
		final int MetaRows=input.getMetaName().length;
		
		ColumnInfo[] colmeta=new ColumnInfo[MetaCols];
		colmeta[0]=new ColumnInfo("Fieldname",   ColumnInfo.COLUMN_TYPE_TEXT,     false );
		colmeta[1]=new ColumnInfo("Rename to",   ColumnInfo.COLUMN_TYPE_TEXT,     false );
		colmeta[2]=new ColumnInfo("Type",        ColumnInfo.COLUMN_TYPE_CCOMBO,   Value.getAllTypes(), true);
		colmeta[3]=new ColumnInfo("Length",      ColumnInfo.COLUMN_TYPE_TEXT,     false );
		colmeta[4]=new ColumnInfo("Precision",   ColumnInfo.COLUMN_TYPE_TEXT,     false );
		
		wMeta=new TableView(wMetaComp, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colmeta, 
						      MetaRows,  
						      lsMod,
						      props
						      );

		wGetMeta = new Button(wMetaComp, SWT.PUSH);
		wGetMeta.setText("Get fields to change");
		wGetMeta.addListener(SWT.Selection, lsGet);
		fdGetMeta = new FormData();
		fdGetMeta.right = new FormAttachment(100, 0);
		fdGetMeta.top   = new FormAttachment(50, 0);
		wGetMeta.setLayoutData(fdGetMeta);

		fdMeta=new FormData();
		fdMeta.left = new FormAttachment(0, 0);
		fdMeta.top  = new FormAttachment(wlMeta, margin);
		fdMeta.right  = new FormAttachment(wGetMeta, -margin);
		fdMeta.bottom = new FormAttachment(100, 0);
		wMeta.setLayoutData(fdMeta);

		fdMetaComp=new FormData();
		fdMetaComp.left  = new FormAttachment(0, 0);
		fdMetaComp.top   = new FormAttachment(0, 0);
		fdMetaComp.right = new FormAttachment(100, 0);
		fdMetaComp.bottom= new FormAttachment(100, 0);
		wMetaComp.setLayoutData(fdMetaComp);
	
		wMetaComp.layout();
		wMetaTab.setControl(wMetaComp);

		/////////////////////////////////////////////////////////////
		/// END OF META TAB
		/////////////////////////////////////////////////////////////

		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		/////////////////////////////////////////////////////////////
		/// END OF TAB FOLDER
		/////////////////////////////////////////////////////////////


		
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
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
		wTabFolder.setSelection(0); // Default
		
		/*
		 * Select fields
		 */
		if (input.getSelectName()!=null && input.getSelectName().length>0)
		{
			for (int i=0;i<input.getSelectName().length;i++)
			{
				TableItem item = wFields.table.getItem(i);
				if (input.getSelectName()[i]!=null) 
					item.setText(1, input.getSelectName()     [i]);
				if (input.getSelectRename()[i]!=null && !input.getSelectRename()[i].equals(input.getSelectName()[i]))
					item.setText(2, input.getSelectRename()   [i]);
				item.setText(3, input.getSelectLength()   [i]==-2?"":""+input.getSelectLength()   [i]);
				item.setText(4, input.getSelectPrecision()[i]==-2?"":""+input.getSelectPrecision()[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
			wTabFolder.setSelection(0);
		}

		/*
		 * Remove certain fields...
		 */
		if (input.getDeleteName()!=null && input.getDeleteName().length>0) 
		{
			for (int i=0;i<input.getDeleteName().length;i++)
			{
				TableItem item = wRemove.table.getItem(i);
				if (input.getDeleteName()[i]!=null)  item.setText(1, input.getDeleteName()     [i]);
			}
			wRemove.setRowNums();
			wRemove.optWidth(true);
			wTabFolder.setSelection(1);
		}

		/*
		 * Change the meta-data of certain fields
		 */
		if (input.getMetaName()!=null && input.getMetaName().length>0)
		{
			for (int i=0;i<input.getMetaName().length;i++)
			{
				TableItem item = wMeta.table.getItem(i);
				if (input.getMetaName()[i]!=null) 
					item.setText(1, input.getMetaName()     [i]);
				if (input.getMetaRename()[i]!=null && !input.getMetaRename()[i].equals(input.getMetaName()[i]))
					item.setText(2, input.getMetaRename()   [i]);
				item.setText(3, Value.getTypeDesc(input.getMetaType()[i]) );
				item.setText(4, input.getMetaLength()   [i]==-2?"":""+input.getMetaLength()   [i]);
				item.setText(5, input.getMetaPrecision()[i]==-2?"":""+input.getMetaPrecision()[i]);
			}
			wMeta.setRowNums();
			wMeta.optWidth(true);
			wTabFolder.setSelection(2);
		}

		wStepname.setFocus();
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
		
		// copy info to meta class (input)

		int i;
		
		int nrfields = wFields.nrNonEmpty();
		int nrremove = wRemove.nrNonEmpty();
		int nrmeta   = wMeta.nrNonEmpty();
		
		input.allocate(nrfields, nrremove, nrmeta);
		
		for (i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getSelectName()        [i] = item.getText(1);
			input.getSelectRename()      [i] = item.getText(2);
			if (input.getSelectRename()[i]==null || input.getSelectName()[i].length()==0)
				input.getSelectRename()[i] = input.getSelectName()[i];
			input.getSelectLength()      [i] = Const.toInt(item.getText(3), -2);
			input.getSelectPrecision()   [i] = Const.toInt(item.getText(4), -2);
			
			if (input.getSelectLength()   [i]<-2) input.getSelectLength()   [i]=-2;
			if (input.getSelectPrecision()[i]<-2) input.getSelectPrecision()[i]=-2;
		}

		for (i=0;i<nrremove;i++)
		{
			TableItem item = wRemove.getNonEmpty(i);
			input.getDeleteName()        [i] = item.getText(1);
		}

		for (i=0;i<nrmeta;i++)
		{
			TableItem item = wMeta.getNonEmpty(i);
			input.getMetaName()        [i] = item.getText(1);
			input.getMetaRename()      [i] = item.getText(2);
			if (input.getMetaRename()[i]==null || input.getMetaName()[i].length()==0)
				input.getMetaRename()[i] = input.getMetaName()[i];
			input.getMetaType()        [i] = Value.getType( item.getText(3) );
			input.getMetaLength()      [i] = Const.toInt(item.getText(4), -2);
			input.getMetaPrecision()   [i] = Const.toInt(item.getText(5), -2);
			
			if (input.getMetaLength()   [i]<-2) input.getMetaLength()   [i]=-2;
			if (input.getMetaPrecision()[i]<-2) input.getMetaPrecision()[i]=-2;
		}
		dispose();
	}

	private void get()
	{
		try
		{
			int tabIndex = wTabFolder.getSelectionIndex();
			
			TableView tv;
			
			switch (tabIndex)
			{
			case 0 : tv=wFields; break;
			case 1 : tv=wRemove; break;
			case 2 : tv=wMeta; break;
			default: tv=wFields; break;
			}
	
			int i, count;
			Row r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
				Table table = tv.table;
				
				count=table.getItemCount();
				for (i=0;i<r.size();i++)
				{
					Value v = r.getValue(i);
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, ""+(count+i+1));
					ti.setText(1, v.getName());
					
					if (tabIndex==2)
					{
						ti.setText(3, Value.getTypeDesc(Value.VALUE_TYPE_NONE));
						if (v.getLength()>=0)    ti.setText(4, ""+v.getLength() );
						if (v.getPrecision()>=0) ti.setText(5, ""+v.getPrecision() );
					}
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
	}
}
