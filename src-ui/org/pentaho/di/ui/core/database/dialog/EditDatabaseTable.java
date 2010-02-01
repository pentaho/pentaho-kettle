 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.core.database.dialog;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;


/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EditDatabaseTable extends Dialog
{
	private TableView    wFields;
	private FormData     fdFields;

	private Button wClose, wFirst, wPrev, wNext, wLast;
	private FormData fdClose, fdFirst, fdPrev, fdNext, fdLast;
	private Listener lsClose, lsFirst, lsPrev, lsNext, lsLast;

	private Shell         shell;
	private PropsUI         props;
	private DatabaseMeta  dbinfo;
	private String        tablename;
	private int           scroll_size;
	
	private Database      db;
	private ArrayList<Object[]>     buffer;
	private int           position;
	private int           max_position;
	private ResultSet     rs;
    private RowMetaInterface rowMeta;
	
    public EditDatabaseTable(Shell parent, int style, DatabaseMeta dbinfo, String tablename, int scroll_size)
    {
        super(parent, style);
        
        this.dbinfo = dbinfo;
        this.props = PropsUI.getInstance();
        this.tablename = tablename;
        this.scroll_size = scroll_size;
        
        max_position=0;
    }
    
	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Edit table ["+tablename+"] on database ["+dbinfo.getName()+"]");
		
		int margin = Const.MARGIN;
		
		// Open connection & get first batch
		try
		{
			openQuery();
			getFirst();
		}
		catch(KettleException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("An error occured : "+e.getMessage());
			mb.setText("ERROR");
			mb.open();
			return;
		}

		if (buffer.size()==0)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("No rows found in this table!");
			mb.setText("ERROR");
			mb.open();
			return;
		}
		
		int FieldsRows=buffer.size();
		
		ColumnInfo[] colinf=new ColumnInfo[rowMeta.size()];
		for (int i=0;i<rowMeta.size();i++)
		{
			ValueMetaInterface v=rowMeta.getValueMeta(i);
			colinf[i]=new ColumnInfo(v.getName(),  ColumnInfo.COLUMN_TYPE_TEXT,   false);
			colinf[i].setToolTip(v.toStringMeta());
		}
		
		wFields=new TableView(dbinfo, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      null,
							  props
						      );

		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(0, 0);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(" &Close ");
		fdClose=new FormData();
		fdClose.left=new FormAttachment(25, 0);
		fdClose.bottom =new FormAttachment(100, -margin);
		wClose.setLayoutData(fdClose);

		wFirst=new Button(shell, SWT.PUSH);
		wFirst.setText(" &First ");
		fdFirst=new FormData();
		fdFirst.left=new FormAttachment(wClose, 10);
		fdFirst.bottom =new FormAttachment(100, -margin);
		wFirst.setLayoutData(fdFirst);

		wPrev=new Button(shell, SWT.PUSH);
		wPrev.setText(" &Prev ");
		fdPrev=new FormData();
		fdPrev.left=new FormAttachment(wFirst, 10);
		fdPrev.bottom =new FormAttachment(100, -margin);
		wPrev.setLayoutData(fdPrev);

		wNext=new Button(shell, SWT.PUSH);
		wNext.setText(" &Next ");
		fdNext=new FormData();
		fdNext.left=new FormAttachment(wPrev, 10);
		fdNext.bottom =new FormAttachment(100, -margin);
		wNext.setLayoutData(fdNext);

		wLast=new Button(shell, SWT.PUSH);
		wLast.setText(" &Last ");
		fdLast=new FormData();
		fdLast.left=new FormAttachment(wNext, 10);
		fdLast.bottom =new FormAttachment(100, -margin);
		wLast.setLayoutData(fdLast);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		lsFirst = new Listener() { public void handleEvent(Event e) { first(); } };
		lsPrev  = new Listener() { public void handleEvent(Event e) { prev(); } };
		lsNext  = new Listener() { public void handleEvent(Event e) { next(); } };
		lsLast  = new Listener() { public void handleEvent(Event e) { last(); } };
		wClose.addListener(SWT.Selection, lsClose    );
		wFirst.addListener(SWT.Selection, lsFirst    );
		wPrev .addListener(SWT.Selection, lsPrev     );
		wNext .addListener(SWT.Selection, lsNext     );
		wLast .addListener(SWT.Selection, lsLast     );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );


		BaseStepDialog.setSize(shell);

		getData();
		wFields.optWidth(true);
		shell.open();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	private void openQuery()
		throws KettleException
	{
		db = new Database(dbinfo);
		db.connect();
		
        // TODO add schema-name as well
		rs = db.openQuery("SELECT * FROM "+dbinfo.quoteField(tablename), null, null, ResultSet.TYPE_SCROLL_SENSITIVE);
		rowMeta = db.getReturnRowMeta();
        
		position=1;
	}

	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	private void getFirst()
		throws KettleException
	{
		db.first(rs);
		getNext();
	}
	
	public void getNext() throws KettleException
	{
		buffer = new ArrayList<Object[]>();

		// OK, now get a maximum of <scroll_size> rows
		Object[] row = db.getRow(rs);
		for (int i=0;i<scroll_size && row!=null;i++)
		{
			position++;
			if (position>max_position) max_position=position;
			buffer.add(row);

			row = db.getRow(rs);
		}
	}
	
	public void getPrev() throws KettleException
	{
		position-=scroll_size;
		if (position<1) position=1;
		db.absolute(rs, position);
		
		getNext();
	}
	
	public void getLast() throws KettleException
	{
		position=max_position-scroll_size;
		if (position<1) position=1;
		
		db.absolute(rs, position);
		
		getNext();
	}
	
	public void getData()
	{
		for (int i=0;i<buffer.size();i++)
		{
			Object[] row = buffer.get(i);

			wFields.table.getItem(i).setText(0, ""+(position-scroll_size+i));
			
			for (int c=0;c<rowMeta.size();c++)
			{
				ValueMetaInterface v=rowMeta.getValueMeta(c);
				String show;
                try
                {
    				if (v.isNumeric()) show = v.getString(row[c]); // TODO add support for padding
    				else               show = v.getString(row[c]);
    				wFields.table.getItem(i).setText(c+1, show);
                }
                catch(KettleValueException e)
                {
                    LogWriter.getInstance(LogWriter.LOG_LEVEL_DEBUG).logBasic(toString(), Const.getStackTracker(e), null);
                }
			}
		}
		
	}
	
	private void close()
	{
		dispose();
	}
	
	public boolean isDisposed()
	{
		return shell.isDisposed();
	}	
	
	public void first()
	{
		try
		{
			getFirst();
			getData();
		}
		catch(KettleException e)
		{
			
		}
	}

	public void prev()
	{
		try
		{
			getPrev();
			getData();
		}
		catch(KettleException e)
		{
			
		}
	}
	
	public void next()
	{
		try
		{
			getNext();
			getData();
		}
		catch(KettleException e)
		{
			
		}
	}

	public void last()
	{
		try
		{
			getLast();
			getData();
		}
		catch(KettleException e)
		{
			
		}
	}

    /**
     * @return the rowMeta
     */
    public RowMetaInterface getRowMeta()
    {
        return rowMeta;
    }
}
