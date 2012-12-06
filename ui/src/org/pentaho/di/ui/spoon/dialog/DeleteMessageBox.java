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

package org.pentaho.di.ui.spoon.dialog;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Displays the delete message box to confirm deletes of multiple
 * steps or job entries 
 * @author David Kincade
 */
public class DeleteMessageBox extends MessageBox {
  private static Class<?> PKG = DeleteMessageBox.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  // The title for the message box
  private static final String title = BaseMessages.getString(PKG, "DeleteMessageBox.Title"); //$NON-NLS-1$ 

  // The text to display in the dialog
  private String text = null;

  // The list of proposed steps to be deleted
  private List<String> stepList = null;

  /**
   * Creates a message box to confirm the deletion of the items 
   * @param shell the shell which will be the parent of the new instance
   * @param text the title for the dialog
   * @param stepList the text list of proposed steps to be deleted 
   */
  public DeleteMessageBox(Shell shell, String text, List<String> stepList) {
    super(shell, SWT.YES | SWT.NO | SWT.ICON_WARNING);
    this.text = text;
    this.stepList = stepList;
  }

  /**
   * Creats the dialog and then performs the display and returns the result
   * @see org.eclipse.swt.widgets.MessageBox
   */
  public int open() {
    // Set the title
    setText(title);
    
    // Set the message
    setMessage(buildMessage());
    
    // Perform the normal open operation
    return super.open();
  }
  
  /**
   * Builds a message from the text and the stepList
   * @return
   */
  protected String buildMessage() {
    StringBuffer sb = new StringBuffer();
    sb.append(text).append(Const.CR);
    if (stepList != null) {
      for (Iterator<String> it = stepList.iterator(); it.hasNext(); ) {
        sb.append("  - ").append(it.next()).append(Const.CR); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }

  /**
   * Allow this class to subclass MessageBox
   */
  protected void checkSubclass() {
  }
}
