package org.pentaho.di.trans.steps.textfileinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.util.StringUtil;

public class DirectoryDialogButtonListenerFactory
{
    public static final SelectionAdapter getSelectionAdapter(final Shell shell, final Text destination)
    {
        // Listen to the Browse... button
        return new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
                    if (destination.getText()!=null)
                    {
                        String fpath = StringUtil.environmentSubstitute(destination.getText());
                        dialog.setFilterPath( fpath );
                    }
                    
                    if (dialog.open()!=null)
                    {
                        String str= dialog.getFilterPath();
                        destination.setText(str);
                    }
                }
            };
    }
}
