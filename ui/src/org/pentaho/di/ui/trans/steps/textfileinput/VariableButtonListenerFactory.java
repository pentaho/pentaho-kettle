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

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.widget.GetCaretPositionInterface;
import org.pentaho.di.ui.core.widget.InsertTextInterface;


public class VariableButtonListenerFactory
{
	private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, VariableSpace space)
    {
        return getSelectionAdapter(composite, destination, null, null, space);
    }

    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface, final VariableSpace space)
    {
        return new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e) 
            {
            	String keys[] = space.listVariables();
                Arrays.sort(keys);
                
                int size = keys.length;
                String key[] = new String[size];
                String val[] = new String[size];
                String str[] = new String[size];
                
                for (int i=0;i<keys.length;i++)
                {
                    key[i] = keys[i];
                    val[i] = space.getVariable(key[i]);
                    str[i] = key[i]+"  ["+val[i]+"]";
                }
                
                // Before focus is lost, we get the position of where the selected variable needs to be inserted.
                int position=0;
                if (getCaretPositionInterface!=null)
                {
                    position = getCaretPositionInterface.getCaretPosition();
                }
                
                EnterSelectionDialog esd = new EnterSelectionDialog(composite.getShell(), str, BaseMessages.getString(PKG, "System.Dialog.SelectEnvironmentVar.Title"), BaseMessages.getString(PKG, "System.Dialog.SelectEnvironmentVar.Message"));
                if (esd.open()!=null)
                {
                    int nr = esd.getSelectionNr();
                    String var = "${"+key[nr]+"}";
                    
                    if (insertTextInterface==null)
                    {
                        destination.insert(var);
                        //destination.setToolTipText(StringUtil.environmentSubstitute( destination.getText() ) );
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