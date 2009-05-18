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
package org.pentaho.di.ui.core.widget;

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;


public class VariableButtonListenerFactory
{
	private static Class<?> PKG = VariableButtonListenerFactory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    // Listen to the Variable... button
    public static final SelectionAdapter getSelectionAdapter(final Composite composite, final Text destination, final VariableSpace space)
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
                // Before focus is lost, we get the position of where the selected variable needs to be inserted.
                int position=0;
                if (getCaretPositionInterface!=null)
                {
                    position = getCaretPositionInterface.getCaretPosition();
                }
                
                String variableName = getVariableName(composite.getShell(), space);
                if (variableName!=null)
                {
                    String var = "${"+variableName+"}";
                    if (insertTextInterface==null)
                    {
                        destination.insert(var);
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
    
    // Listen to the Variable... button
    public static final String getVariableName(Shell shell, VariableSpace space)
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
                
        EnterSelectionDialog esd = new EnterSelectionDialog(shell, str, BaseMessages.getString(PKG, "System.Dialog.SelectEnvironmentVar.Title"), BaseMessages.getString(PKG, "System.Dialog.SelectEnvironmentVar.Message"));
        esd.clearModal();
        if (esd.open()!=null)
        {
            int nr = esd.getSelectionNr();
            String var = key[nr];
         
            return var;
        }
        else
        {
        	return null;
        }
    }
}