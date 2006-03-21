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
 
package be.ibridge.kettle.spoon;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;



/**
 * SpoonHistory handles the display of the historical information regarding earlier runs of this transformation.
 * The idea is that this Composite is only populated when after loading of a transformation, we find a connection and logging table.
 * We then read from this table and populate the grid and log.
 *  
 * @see be.ibridge.kettle.spoon.Spoon
 * @author Matt
 * @since  16-mar-2006
 */
public class SpoonHistory extends Composite
{
	private ColumnInfo[] colinf;	
	
	private Text   wText;
	private Button wRefresh;
    private TableView wFields;
    
	private FormData fdText, fdSash, fdRefresh; 
	
	private Spoon spoon;

    private ArrayList rowList;
	
	public SpoonHistory(Composite parent, int style, Spoon sp, LogWriter l, String fname)
	{
		super(parent, style);
		spoon = sp;
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		setLayout(formLayout);
		
		setVisible(true);
        spoon.props.setLook(this);
		
		SashForm sash = new SashForm(this, SWT.VERTICAL);
		spoon.props.setLook(sash);
		
		sash.setLayout(new FillLayout());

		final int FieldsRows=1;
		
		colinf=new ColumnInfo[] {
            new ColumnInfo("Batch ID",        ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Status",          ColumnInfo.COLUMN_TYPE_TEXT, false, true),
            new ColumnInfo("Read",            ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Written",         ColumnInfo.COLUMN_TYPE_TEXT, false, true),
            new ColumnInfo("Updated",         ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Input",           ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Output",          ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Errors",          ColumnInfo.COLUMN_TYPE_TEXT, false, true),
            new ColumnInfo("Start date",      ColumnInfo.COLUMN_TYPE_TEXT, false, true),
            new ColumnInfo("End date",        ColumnInfo.COLUMN_TYPE_TEXT, false, true),
    		new ColumnInfo("Log date",        ColumnInfo.COLUMN_TYPE_TEXT, false, true),
            new ColumnInfo("Dependency date", ColumnInfo.COLUMN_TYPE_TEXT, false, true)
        };
		
        for (int i=0;i<colinf.length;i++) colinf[i].setAllignement(SWT.RIGHT);
        
        wFields=new TableView(sash, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  true, // readonly!
							  null,
							  spoon.props
							  );
		
		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		spoon.props.setLook(wText);
		wText.setVisible(true);
		
		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText(" Refresh ");

		fdRefresh    = new FormData(); 
		fdRefresh.left   = new FormAttachment(15, 0);  
		fdRefresh.bottom = new FormAttachment(100, 0);
		wRefresh.setLayoutData(fdRefresh);

		// Put text in the middle
		fdText=new FormData();
		fdText.left   = new FormAttachment(0, 0);
		fdText.top    = new FormAttachment(0, 0);
		fdText.right  = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100, 0);
		wText.setLayoutData(fdText);

		
		fdSash     = new FormData(); 
		fdSash.left   = new FormAttachment(0, 0);  // First one in the left top corner
		fdSash.top    = new FormAttachment(0, 0);
		fdSash.right  = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(wRefresh, -5);
		sash.setLayoutData(fdSash);
		
		// sash.setWeights(new int[] { 60, 40} );

		pack();
        
        SelectionAdapter lsRefresh = new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    refreshHistory();
                }
            };
		
		wRefresh.addSelectionListener(lsRefresh);
        
        wFields.table.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    showLogEntry();
                }
            }
        );
	}
	
	public void refreshHistory()
	{
        // See if there is a transformation loaded that has a connection table specified.
        TransMeta transMeta = spoon.getTransMeta();
        if (transMeta!=null && transMeta.getName()!=null && transMeta.getName().length()>0)
        {
            if (transMeta.getLogConnection()!=null)
            {
                if (transMeta.getLogTable()!=null && transMeta.getLogTable().length()>0)
                {
                    Database database = null;
                    try
                    {
                        // open a connection
                        database = new Database(transMeta.getLogConnection());
                        database.connect();
                        
                        Row params = new Row();
                        params.addValue(new Value("transname", transMeta.getName()));
                        ResultSet resultSet = database.openQuery("SELECT * FROM "+transMeta.getLogTable()+" WHERE TRANSNAME = ?", params);
                        
                        rowList = new ArrayList();
                        Row row = database.getRow(resultSet);
                        while (row!=null)
                        {
                            rowList.add(row);
                            row = database.getRow(resultSet);
                        }
                        database.closeQuery(resultSet);

                        if (rowList.size()>0)
                        {
                            wFields.table.clearAll();
                            
                            // OK, now that we have a series of rows, we can add them to the table view...
                            for (int i=0;i<rowList.size();i++)
                            {
                                row = (Row) rowList.get(i);
                                TableItem item = new TableItem(wFields.table, SWT.NONE);
                                item.setText( 1, row.getString("ID_BATCH", ""));          
                                item.setText( 2, row.getString("STATUS", ""));          
                                item.setText( 3, row.getString("LINES_READ", ""));          
                                item.setText( 4, row.getString("LINES_WRITTEN", ""));          
                                item.setText( 5, row.getString("LINES_UPDATED", ""));          
                                item.setText( 6, row.getString("LINES_INPUT", ""));          
                                item.setText( 7, row.getString("LINES_OUTPUT", ""));          
                                item.setText( 8, row.getString("ERRORS", ""));          
                                item.setText( 9, row.getString("STARTDATE", ""));          
                                item.setText(10, row.getString("ENDDATE", ""));          
                                item.setText(11, row.getString("LOGDATE", ""));          
                                item.setText(12, row.getString("DEPDATE", ""));                                
                            }
                            
                            wFields.removeEmptyRows();
                            wFields.setRowNums();
                            wFields.optWidth(true);
                            wFields.table.setSelection(0);
                            
                            showLogEntry();
                        }
                    }
                    catch(KettleException e)
                    {
                        new ErrorDialog(this.getShell(), Props.getInstance(), "Error getting logging information", "Error getting information from the logging table", e);
                    }
                    finally
                    {
                        if (database!=null) database.disconnect();
                    }
                    
                }
            }
        }
	}
    	
	public void showLogEntry()
    {
        // grab the selected line in the table:
        int nr = wFields.table.getSelectionIndex();
        if (nr>=0 && nr<rowList.size())
        {
            // OK, grab this one from the buffer...
            Row row = (Row) rowList.get(nr);
            String logging = row.getString("LOG_FIELD", "");
            if (logging!=null) 
            {
                wText.setText(logging);
            }
        }
    }

    public String toString()
	{
		return Spoon.APP_NAME;
	}

}
