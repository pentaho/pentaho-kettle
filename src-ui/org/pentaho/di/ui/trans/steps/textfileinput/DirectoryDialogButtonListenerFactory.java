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
