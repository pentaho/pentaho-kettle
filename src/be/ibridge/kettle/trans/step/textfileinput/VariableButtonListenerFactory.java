package be.ibridge.kettle.trans.step.textfileinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;

public class VariableButtonListenerFactory
{
    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Shell shell, final Text destination)
    {
        return new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) 
            {
                Properties sp = System.getProperties();
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
                
                Props props = Props.getInstance();
                EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, str, "Select an Environment Variable", "Select an Environment Variable");
                if (esd.open()!=null)
                {
                    int nr = esd.getSelectionNr();
                    destination.insert("%%"+key[nr]+"%%");
                    destination.setToolTipText(Const.replEnv( destination.getText() ) );
                }
            }
        };
    }
}


