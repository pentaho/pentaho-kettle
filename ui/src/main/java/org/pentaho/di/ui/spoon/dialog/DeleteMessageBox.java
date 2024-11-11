/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
 * Displays the delete message box to confirm deletes of multiple steps or job entries
 *
 * @author David Kincade
 */
public class DeleteMessageBox extends MessageBox {
  private static Class<?> PKG = DeleteMessageBox.class; // for i18n purposes, needed by Translator2!!

  // The title for the message box
  private static final String title = BaseMessages.getString( PKG, "DeleteMessageBox.Title" );

  // The text to display in the dialog
  private String text = null;

  // The list of proposed steps to be deleted
  private List<String> stepList = null;

  /**
   * Creates a message box to confirm the deletion of the items
   *
   * @param shell
   *          the shell which will be the parent of the new instance
   * @param text
   *          the title for the dialog
   * @param stepList
   *          the text list of proposed steps to be deleted
   */
  public DeleteMessageBox( Shell shell, String text, List<String> stepList ) {
    super( shell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
    this.text = text;
    this.stepList = stepList;
  }

  /**
   * Creats the dialog and then performs the display and returns the result
   *
   * @see org.eclipse.swt.widgets.MessageBox
   */
  public int open() {
    // Set the title
    setText( title );

    // Set the message
    setMessage( buildMessage() );

    // Perform the normal open operation
    return super.open();
  }

  /**
   * Builds a message from the text and the stepList
   *
   * @return
   */
  protected String buildMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append( text ).append( Const.CR );
    if ( stepList != null ) {
      for ( Iterator<String> it = stepList.iterator(); it.hasNext(); ) {
        sb.append( "  - " ).append( it.next() ).append( Const.CR );
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
