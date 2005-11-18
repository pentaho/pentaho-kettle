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

package be.ibridge.kettle.schema.dialog;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.schema.RelationshipMeta;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.schema.TableField;
import be.ibridge.kettle.schema.TableMeta;


public class RelationshipDialog extends Dialog
{
	private Label        wlFrom;
	private CCombo       wFrom;
    private FormData     fdlFrom, fdFrom;
	
	private Label        wlTo;
	private CCombo       wTo;
	private FormData     fdlTo,fdTo;

	private CCombo       wFromField;
	private FormData     fdFromField;

	private CCombo       wToField;
	private FormData     fdToField;
	
	private Button   wGuess;
	private FormData fdGuess;
	private Listener lsGuess;

	private Label        wlRelation;
	private CCombo       wRelation;
	private FormData     fdlRelation, fdRelation;

	private Button   wGuessRel;
	private FormData fdGuessRel;
	private Listener lsGuessRel;

	private Label        wlComplex;
	private Button       wComplex;
	private FormData     fdlComplex, fdComplex;

	private Label        wlComplexJoin;
	private Text         wComplexJoin;
	private FormData     fdlComplexJoin, fdComplexJoin;

	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private RelationshipMeta input;
	private Shell  shell;
	private SchemaMeta schema;
	
	private TableMeta fromtable, totable;
		
	private ModifyListener lsMod;
	
	private boolean changed, backupComplex;
	
	public RelationshipDialog(Shell parent, int style, LogWriter l, RelationshipMeta in, SchemaMeta sch)
	{
		super(parent, style);
		input=in;
		schema=sch;
		
		fromtable = input.getTableFrom();
		totable   = input.getTableTo();
	}

	public Object open()
	{
        Props props = Props.getInstance();
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setBackground(GUIResource.getInstance().getColorBackground());
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		backupComplex = input.isComplex();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Hop: From --> To");
		
		int middle = schema.props.getMiddlePct();
		int length = Const.LENGTH;
		int margin = Const.MARGIN;
		int width  = Const.RIGHT;

		// From step line
		wlFrom=new Label(shell, SWT.RIGHT);
		wlFrom.setText("From table / field: ");
        props.setLook(wlFrom);
		fdlFrom=new FormData();
		fdlFrom.left = new FormAttachment(0, 0);
		fdlFrom.right= new FormAttachment(middle, -margin);
		fdlFrom.top  = new FormAttachment(0, margin);
		wlFrom.setLayoutData(fdlFrom);
		wFrom=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFrom.setText("Select the source table");
        props.setLook(wFrom);

		for (int i=0;i<schema.nrTables();i++)
		{
			TableMeta ti = schema.getTable(i);
			wFrom.add(ti.getName());
		}
		wFrom.addModifyListener(lsMod);
		wFrom.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					// grab the new fromtable:
					fromtable=schema.findTable(wFrom.getText());
					refreshFromFields();
				}
			}
		);

		fdFrom=new FormData();
		fdFrom.left = new FormAttachment(middle, 0);
		fdFrom.top  = new FormAttachment(0, margin);
		fdFrom.right= new FormAttachment(60, 0);
		wFrom.setLayoutData(fdFrom);

		wFromField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFromField.setText("");
        props.setLook(wFromField);
        refreshFromFields();		
		wFromField.addModifyListener(lsMod);

		fdFromField=new FormData();
		fdFromField.left = new FormAttachment(wFrom, margin*2);
		fdFromField.top  = new FormAttachment(0, margin);
		fdFromField.right= new FormAttachment(100, 0);
		wFromField.setLayoutData(fdFromField);

		// To line
		wlTo=new Label(shell, SWT.RIGHT);
		wlTo.setText("To table / field: ");
        props.setLook(wlTo);
		fdlTo=new FormData();
		fdlTo.left = new FormAttachment(0, 0);
		fdlTo.right= new FormAttachment(middle, -margin);
		fdlTo.top  = new FormAttachment(wFrom, margin);
		wlTo.setLayoutData(fdlTo);
		wTo=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wTo.setText("Select the destination table");
        props.setLook(wTo);

		for (int i=0;i<schema.nrTables();i++)
		{
			TableMeta ti = schema.getTable(i);
			wTo.add(ti.getName());
		} 
		wTo.addModifyListener(lsMod);
		wTo.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					// grab the new fromtable:
					totable=schema.findTable(wTo.getText());
					refreshToFields();
				}
			}
		);
		
		fdTo=new FormData();
		fdTo.left = new FormAttachment(middle, 0);
		fdTo.top  = new FormAttachment(wFrom, margin);
		fdTo.right= new FormAttachment(60, 0);
		wTo.setLayoutData(fdTo);


		// ToField step line
		wToField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wToField.setText("Select the field");
        props.setLook(wToField);
		refreshToFields();
		wToField.addModifyListener(lsMod);

		fdToField=new FormData();
		fdToField.left = new FormAttachment(wTo, margin*2);
		fdToField.top  = new FormAttachment(wFromField, margin);
		fdToField.right= new FormAttachment(100, 0);
		wToField.setLayoutData(fdToField);

		wGuess=new Button(shell, SWT.PUSH);
		wGuess.setText("  &Guess matching fields  ");
		lsGuess = new Listener() { public void handleEvent(Event e) { guess(); } };
		wGuess.addListener(SWT.Selection, lsGuess );
		fdGuess=new FormData();
		fdGuess.left       = new FormAttachment(wTo, margin*2);
		fdGuess.top        = new FormAttachment(wToField, margin);
		wGuess.setLayoutData(fdGuess);

		// Relation line
		wlRelation=new Label(shell, SWT.RIGHT);
		wlRelation.setText("Relationship : ");
        props.setLook(wlRelation);
		fdlRelation=new FormData();
		fdlRelation.left = new FormAttachment(0, 0);
		fdlRelation.right= new FormAttachment(middle, -margin);
		fdlRelation.top  = new FormAttachment(wGuess, margin*2);
		wlRelation.setLayoutData(fdlRelation);
		wRelation=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wRelation);

		for (int i=0;i<RelationshipMeta.typeRelationshipDesc.length;i++)
		{
			wRelation.add(RelationshipMeta.typeRelationshipDesc[i]);
		}
		wRelation.addModifyListener(lsMod);

		fdRelation=new FormData();
		fdRelation.left = new FormAttachment(middle, 0);
		fdRelation.top  = new FormAttachment(wGuess, margin*2);
		fdRelation.right= new FormAttachment(60, 0);
		wRelation.setLayoutData(fdRelation);

		wGuessRel=new Button(shell, SWT.PUSH);
		wGuessRel.setText("  &Guess relationship  ");
		lsGuessRel = new Listener() { public void handleEvent(Event e) { guessRelationship(); } };
		wGuessRel.addListener(SWT.Selection, lsGuessRel );
		fdGuessRel=new FormData();
		fdGuessRel.left       = new FormAttachment(wRelation, margin*2);
		fdGuessRel.top        = new FormAttachment(wGuess, margin*2);
		wGuessRel.setLayoutData(fdGuessRel);

		// Complex checkbox
		wlComplex=new Label(shell, SWT.RIGHT);
		wlComplex.setText("Complex join? ");
        props.setLook(wlComplex);
		fdlComplex=new FormData();
		fdlComplex.left = new FormAttachment(0, 0);
		fdlComplex.right= new FormAttachment(middle, -margin);
		fdlComplex.top  = new FormAttachment(wGuessRel, margin);
		wlComplex.setLayoutData(fdlComplex);
		wComplex=new Button(shell, SWT.CHECK);
        props.setLook(wComplex);
		fdComplex=new FormData();
		fdComplex.left = new FormAttachment(middle, 0);
		fdComplex.right= new FormAttachment(0, middle+length);
		fdComplex.top  = new FormAttachment(wGuessRel, margin);
		wComplex.setLayoutData(fdComplex);
		wComplex.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.flipComplex();
					input.setChanged();
					setComplex();
				}
			}
		);

		// ComplexJoin line
		wlComplexJoin=new Label(shell, SWT.RIGHT);
		wlComplexJoin.setText("Complex join expression: ");
        props.setLook(wlComplexJoin);
		fdlComplexJoin=new FormData();
		fdlComplexJoin.left = new FormAttachment(0, 0);
		fdlComplexJoin.right= new FormAttachment(middle, -margin);
		fdlComplexJoin.top  = new FormAttachment(wComplex, margin);
		wlComplexJoin.setLayoutData(fdlComplexJoin);
		wComplexJoin=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		wComplexJoin.setText("");
        props.setLook(wComplexJoin);
		wComplexJoin.addModifyListener(lsMod);
		fdComplexJoin=new FormData();
		fdComplexJoin.left   = new FormAttachment(0, 0);
		fdComplexJoin.right  = new FormAttachment(100, 0);
		fdComplexJoin.top    = new FormAttachment(wlComplexJoin, margin);
		fdComplexJoin.bottom = new FormAttachment(100, -50);
		wComplexJoin.setLayoutData(fdComplexJoin);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.bottom = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		//shell.pack();
		shell.setSize(middle+width+50, 350);
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return input;
	}
	
	public void setComplex()
	{
		wFromField.setEnabled(input.isRegular());
		wToField.setEnabled(input.isRegular());
		wComplexJoin.setEnabled(input.isComplex());
		wlComplexJoin.setEnabled(input.isComplex());

        /*
		if (input.isRegular())
		{
			wFromField.setBackground(bg);
			wToField.setBackground(bg);
			wComplexJoin.setBackground(gray);
		}
		else
		{
			wFromField.setBackground(gray);
			wToField.setBackground(gray);
			wComplexJoin.setBackground(bg);
		}
		*/
	}

	public void refreshFromFields()
	{
		wFromField.removeAll();
		if (fromtable!=null)
		{
			for (int i=0;i<fromtable.nrFields();i++)
			{
				TableField f = fromtable.getField(i);
				wFromField.add(f.getName());
			}
		}
	}
	
	public void refreshToFields()
	{
		wToField.removeAll();
		if (totable!=null)
		{
			for (int i=0;i<totable.nrFields();i++)
			{
				TableField f = totable.getField(i);
				wToField.add(f.getName());
			}
		}
	}

	public void dispose()
	{
			shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getTableFrom() != null) wFrom.setText(input.getTableFrom().getName());
		if (input.getTableTo()   != null) wTo.setText(input.getTableTo().getName());
		
		if (input.getFieldnrFrom()>=0) wFromField.select(input.getFieldnrFrom());
		if (input.getFieldnrTo()>=0)   wToField.select(input.getFieldnrTo());
		
		wRelation.select(input.getType());
		wComplex.setSelection(input.isComplex());
		if (input.getComplexJoin()!=null) wComplexJoin.setText(input.getComplexJoin());
		setComplex();
	}
	
	private void cancel()
	{
		input.setChanged(changed);
		input.setComplex(backupComplex);
		input=null;
		dispose();
	}
	
	private void ok()
	{
		input.setTableFrom( schema.findTable(wFrom.getText()) );
		input.setTableTo  ( schema.findTable(wTo  .getText()) );
		
		input.setFieldnrFrom( wFromField.getSelectionIndex());
		input.setFieldnrTo  ( wToField.getSelectionIndex());
		
		input.setType       ( wRelation.getSelectionIndex());
		
		input.setComplexJoin( wComplexJoin.getText());
		
		if (input.getTableFrom()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
			mb.setMessage("Table ["+wFrom.getText()+"] doesn't exist!");
			mb.setText("Warning!");
			mb.open();
		}
		else
		{
			if (input.getTableTo()==null)
			{
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
				mb.setMessage("Table ["+wTo.getText()+"] doesn't exist!");
				mb.setText("Warning!");
				mb.open();
			}
			else
			{
				if (input.getTableFrom().getName().equalsIgnoreCase(input.getTableTo().getName()))
				{
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
					mb.setMessage("A relationship can't be made to the same table!");
					mb.setText("Warning!");
					mb.open();
				}
				else
				{
					dispose();
				}
			}
		}
	}
	
	// Try to find fields with the same name in both tables...
	public void guess()
	{
		for (int i=0;i<wFromField.getItemCount();i++)
		{
			for (int j=0;j<wToField.getItemCount();j++)
			{
				String one = wFromField.getItem(i);
				String two = wToField.getItem(j);
				
				if (one.equalsIgnoreCase(two))
				{
					wFromField.select(i);
					wToField.select(j);
					return;
				}
			}
		}
	}

	// Try to find fields with the same name in both tables...
	public void guessRelationship()
	{
		if (fromtable!=null && totable!=null)
		{
			if (fromtable.isFact() && totable.isDimension()) wRelation.select(RelationshipMeta.TYPE_RELATIONSHIP_N_1);
			if (fromtable.isDimension() && totable.isFact()) wRelation.select(RelationshipMeta.TYPE_RELATIONSHIP_1_N);
			if (fromtable.isFact() && totable.isFact())      wRelation.select(RelationshipMeta.TYPE_RELATIONSHIP_N_N);
		}
	}
}
