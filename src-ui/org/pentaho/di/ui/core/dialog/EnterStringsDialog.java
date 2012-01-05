/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Shows a dialog that allows you to enter values for a number of strings.
 *  
 * @author Matt
 *
 */
public class EnterStringsDialog extends Dialog
{
	private static Class<?> PKG = EnterStringsDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
		
	private Button   wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell          shell;
	private RowMetaAndData strings;
	private PropsUI 		   props;
    
    private boolean       readOnly;
    private String message;
    private String title;
    private Image  shellImage;

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
		props=PropsUI.getInstance();
        readOnly=false;
        
        title = BaseMessages.getString(PKG, "EnterStringsDialog.Title");
        message = BaseMessages.getString(PKG, "EnterStringsDialog.Message");
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
                new ColumnInfo(BaseMessages.getString(PKG, "EnterStringsDialog.StringName.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(BaseMessages.getString(PKG, "EnterStringsDialog.StringValue.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly)                
            };
        
        wFields=new TableView(Variables.getADefaultVariableSpace(),
        		              shell, 
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
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

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

		if (shellImage!=null) shell.setImage(shellImage);
		
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
        	int nrNonEmptyFields = wFields.nrNonEmpty(); 
            for (int i=0;i<nrNonEmptyFields;i++)
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
        	int nrNonEmptyFields = wFields.nrNonEmpty(); 
        	for (int i=0;i<nrNonEmptyFields;i++)
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

	/**
	 * @param shellImage the shellImage to set
	 */
	public void setShellImage(Image shellImage) {
		this.shellImage = shellImage;
	}
}
