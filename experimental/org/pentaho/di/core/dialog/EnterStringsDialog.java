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

package org.pentaho.di.core.dialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import be.ibridge.kettle.core.ColumnInfo;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.widget.TableView;
import org.pentaho.di.trans.step.BaseStepDialog;


/**
 * Shows a dialog that allows you to enter values for a number of strings.
 *  
 * @author Matt
 *
 */
public class EnterStringsDialog extends Dialog
{
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
		
	private Button   wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell          shell;
	private RowMetaAndData strings;
	private Props 		   props;
    
    private boolean       readOnly;
    private String message;
    private String title;

    /**
     * Constructs a new dialog
     * @param parent The parent shell to link to
     * @param style The style in which we want to draw this shell.
     * @param strings The list of rows to change.
     */
	public EnterStringsDialog(Shell parent, int style, RowMetaAndData strings)
	{
		super(parent, style);
		this.strings=strings;
		props=Props.getInstance();
        readOnly=false;
        
        title = Messages.getString("EnterStringsDialog.Title");
        message = Messages.getString("EnterStringsDialog.Message");
	}

	public RowMetaAndData open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);
		
		int margin = Const.MARGIN;
        
        

		// Message line
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(message);
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);

        int FieldsRows=strings.getRowMeta().size();
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
                new ColumnInfo(Messages.getString("EnterStringsDialog.StringName.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(Messages.getString("EnterStringsDialog.StringValue.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly)                
            };
        
        wFields=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              null,
                              props
                              );
        wFields.setReadonly(readOnly);
        
        fdFields=new FormData();
        fdFields.left   = new FormAttachment(0, 0);
        fdFields.top    = new FormAttachment(wlFields, 30);
        fdFields.right  = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };

		wOK.addListener (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return strings;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        if (strings!=null)
        {
            for (int i=0;i<strings.getRowMeta().size();i++)
            {
                ValueMetaInterface valueMeta = strings.getRowMeta().getValueMeta(i);
                Object valueData = strings.getData()[i];
                String string;
                try
                {
                    string = valueMeta.getString(valueData);
                }
                catch (KettleValueException e)
                {
                    string = "";
                    // TODO: can this ever be a meaningful exception?  We're editing strings almost by definition
                }
                TableItem item = wFields.table.getItem(i);
                item.setText(1, valueMeta.getName() );
                if (!Const.isEmpty(string)) item.setText(2, string);
            }
        }
        wFields.sortTable(1);
        wFields.setRowNums();
        wFields.optWidth(true);
	}

	private void cancel()
	{
        strings=null;
		dispose();
	}
	
	private void ok()
	{
        if (readOnly)
        {
            // Loop over the input rows and find the new values...
            for (int i=0;i<wFields.nrNonEmpty();i++)
            {
                TableItem item = wFields.getNonEmpty(i);
                String name = item.getText(1);
                for (int j=0;j<strings.getRowMeta().size();j++)
                {
                    ValueMetaInterface valueMeta = strings.getRowMeta().getValueMeta(j);
                    
                    if (valueMeta.getName().equalsIgnoreCase(name))
                    {
                        String stringValue = item.getText(2);
                        strings.getData()[j] = stringValue;
                    }
                }
            }
        }
        else // Variable: re-construct the list of strings again...
        {
            strings.clear();
            for (int i=0;i<wFields.nrNonEmpty();i++)
            {
                TableItem item = wFields.getNonEmpty(i);
                String name = item.getText(1);
                String value = item.getText(2);
                strings.addValue( new ValueMeta(name, ValueMetaInterface.TYPE_STRING), value);
            }
        }
        dispose();
	}

    /**
     * @return Returns the readOnly.
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
}
