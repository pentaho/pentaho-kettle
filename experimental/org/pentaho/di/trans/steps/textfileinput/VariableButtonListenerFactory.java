package org.pentaho.di.trans.steps.textfileinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.GetCaretPositionInterface;
import be.ibridge.kettle.core.widget.InsertTextInterface;

public class VariableButtonListenerFactory
{
    private static KettleVariables kettleVariables = KettleVariables.getInstance();

    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination)
    {
        return getSelectionAdapter(composite, destination, null, null);
    }

    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface)
    {
        return new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) 
            {
                Properties sp = new Properties();
                sp.putAll( kettleVariables.getProperties() );
                sp.putAll( System.getProperties() );
                
                ArrayList keys = new ArrayList( sp.keySet() );
                Collections.sort(keys);
                
                int size = keys.size();
                String key[] = new String[size];
                String val[] = new String[size];
                String str[] = new String[size];
                
                for (int i=0;i<keys.size();i++)
                {
                    key[i] = (String)keys.get(i);
                    val[i] = sp.getProperty(key[i]);
                    str[i] = key[i]+"  ["+val[i]+"]";
                }
                
                // Before focus is lost, we get the position of where the selected variable needs to be inserted.
                int position=0;
                if (getCaretPositionInterface!=null)
                {
                    position = getCaretPositionInterface.getCaretPosition();
                }
                
                EnterSelectionDialog esd = new EnterSelectionDialog(composite.getShell(), str, Messages.getString("System.Dialog.SelectEnvironmentVar.Title"), Messages.getString("System.Dialog.SelectEnvironmentVar.Message"));
                if (esd.open()!=null)
                {
                    int nr = esd.getSelectionNr();
                    String var = "${"+key[nr]+"}";
                    
                    if (insertTextInterface==null)
                    {
                        destination.insert(var);
                        destination.setToolTipText(StringUtil.environmentSubstitute( destination.getText() ) );
                        e.doit=false;
                    }
                    else
                    {
                        insertTextInterface.insertText(var, position);
                    }
                }
            }
        };
    }
}


