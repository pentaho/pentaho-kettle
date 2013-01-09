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

package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;


/**
 * 
 * Dialog that allows you to edit the settings of a database connection.
 * 
 * @see <code>DatabaseInfo</code>
 * @author Matt
 * @since 18-05-2003
 * 
 */
public class DatabaseDialog extends XulDatabaseDialog
{
	private static Class<?> PKG = DatabaseDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	  public DatabaseDialog(Shell parent){
	    super(parent);
	  }
    public DatabaseDialog(Shell parent, DatabaseMeta databaseMeta)
    {
        super(parent);
        setDatabaseMeta(databaseMeta);
    }

    public String open()
    {
        return super.open();
    }
    
    public static final void checkPasswordVisible(Text wPassword)
    {
        String password = wPassword.getText();
        java.util.List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        // ONLY show the variable in clear text if there is ONE variable used
        // Also, it has to be the only string in the field.
        //

        if (list.size() != 1)
        {
            wPassword.setEchoChar('*');
        }
        else
        {
        	String variableName = null;
            if ((password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE)))
            {
            	//  ${VAR}
            	//  012345
            	// 
            	variableName = password.substring(StringUtil.UNIX_OPEN.length(), password.length()-StringUtil.UNIX_CLOSE.length());
            }
            if ((password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE)))
            {
            	//  %VAR%
            	//  01234
            	// 
            	variableName = password.substring(StringUtil.WINDOWS_OPEN.length(), password.length()-StringUtil.WINDOWS_CLOSE.length());
            }
            
            // If there is a variable name in there AND if it's defined in the system properties...
            // Otherwise, we'll leave it alone.
            //
            if (variableName!=null && System.getProperty(variableName)!=null)
            {
                wPassword.setEchoChar('\0'); // Show it all...
            }
            else
            {
                wPassword.setEchoChar('*');
            }
        }
    }
    
    /**
     * Test the database connection
     */
    public static final void test(Shell shell, DatabaseMeta dbinfo)
    {
        String[] remarks = dbinfo.checkParameters();
        if (remarks.length == 0)
        {
        	// Get a "test" report from this database
        	//
        	String reportMessage = dbinfo.testConnection();

        	EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "DatabaseDialog.ConnectionReport.title"), BaseMessages.getString(PKG, "DatabaseDialog.ConnectionReport.description"), reportMessage.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.setReadOnly();
            dialog.setFixed(true);
            dialog.setModal();
            dialog.open();
        }
        else
        {
            String message = ""; //$NON-NLS-1$
            for (int i = 0; i < remarks.length; i++)
                message += "    * " + remarks[i] + Const.CR; //$NON-NLS-1$

            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(BaseMessages.getString(PKG, "DatabaseDialog.ErrorParameters2.title")); //$NON-NLS-1$
            mb.setMessage(BaseMessages.getString(PKG, "DatabaseDialog.ErrorParameters2.description", message)); //$NON-NLS-1$
            mb.open();
        }
    }
}