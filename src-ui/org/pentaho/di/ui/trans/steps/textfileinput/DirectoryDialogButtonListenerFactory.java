/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.trans.steps.textfileinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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
                    	String fpath = destination.getText();
                        //String fpath = StringUtil.environmentSubstitute(destination.getText());
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
