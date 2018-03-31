/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
package org.pentaho.di.ui.core.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;

import java.lang.reflect.Field;

/**
 * A simple message dialog containing a title, icon, message and a single button (OK by default) that closes the dialog.
 * The dialog width can be specified and the height is auto-adjusted based on the width.
 */
public class SimpleMessageDialog extends MessageDialog {

  protected static Class<?> PKG = SimpleMessageDialog.class;

  public static final int BUTTON_WIDTH = 65;
  public static final int DEFULT_WIDTH = 450;

  private int width;
  private int buttonWidth;

  /**
   * Creates a new dialog with the button label set to "Ok", dialog width set to {@link #DEFULT_WIDTH} and button width
   * set to {@link #BUTTON_WIDTH}
   *
   * @param parentShell the parent {@link Shell}
   * @param title       the dialog title
   * @param message     the dialog message
   * @param dialogType  the dialog type ({@link MessageDialog#INFORMATION}, {@link MessageDialog#WARNING}, {@link
   *                    MessageDialog#ERROR} etc...)
   */
  public SimpleMessageDialog( final Shell parentShell, final String title, final String message,
                              final int dialogType ) {
    this( parentShell, title, message, dialogType, BaseMessages.getString( PKG, "System.Button.OK" ),
      DEFULT_WIDTH, BUTTON_WIDTH );
  }

  /**
   * Creates a new dialog with the button label set to {@code closeButtonLabel}, dialog width set to {@link
   * #DEFULT_WIDTH} and button width set to {@link #BUTTON_WIDTH}
   *
   * @param parentShell the parent {@link Shell}
   * @param title       the dialog title
   * @param message     the dialog message
   * @param dialogType  the dialog type ({@link MessageDialog#INFORMATION}, {@link MessageDialog#WARNING}, {@link
   *                    MessageDialog#ERROR} etc...)
   * @param buttonLabel the label for the close dialog
   */
  public SimpleMessageDialog( final Shell parentShell, final String title, final String message,
                              final int dialogType, final String buttonLabel ) {
    this( parentShell, title, message, dialogType, buttonLabel, DEFULT_WIDTH, BUTTON_WIDTH );
  }

  /**
   * Creates a new dialog with the specified title, message, dialogType and width.
   *
   * @param parentShell the parent {@link Shell}
   * @param title       the dialog title
   * @param message     the dialog message
   * @param dialogType  the dialog type ({@link MessageDialog#INFORMATION}, {@link MessageDialog#WARNING}, {@link
   *                    MessageDialog#ERROR} etc...)
   * @param buttonLabel the button label
   * @param width       dialog width
   * @param buttonWidth button width
   */
  public SimpleMessageDialog( final Shell parentShell, final String title, final String message, final int dialogType,
                              final String buttonLabel, final int width, final int buttonWidth ) {
    super( parentShell, title, null, message, dialogType, new String[] { buttonLabel }, 0 );
    this.width = width;
    this.buttonWidth = buttonWidth;
  }

  /**
   * Overridden to auto-size the shell according to the selected width.
   */
  @Override
  protected void constrainShellSize() {
    super.constrainShellSize();
    try {
      // the shell property within the Windows class is private - need to access it via reflection
      final Field shellField = Window.class.getDeclaredField( "shell" );
      shellField.setAccessible( true );
      final Shell thisShell = (Shell) shellField.get( this );
      thisShell.pack();
      final int height = thisShell.computeSize( width, SWT.DEFAULT ).y;
      thisShell.setBounds( thisShell.getBounds().x, thisShell.getBounds().y, width + 4, height + 2 );
    } catch ( final Exception e ) {
      // nothing to do
    }
  }

  /**
   * Overridden to make the shell background white.
   *
   * @param shell
   */
  @Override
  protected void configureShell( Shell shell ) {
    super.configureShell( shell );
    shell.setBackground( shell.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
    shell.setBackgroundMode( SWT.INHERIT_FORCE );
  }

  /**
   * Overridden to give the button the desired width.
   */
  @Override
  public void create() {
    super.create();
    final Button button = getButton( 0 );
    final int newX = button.getBounds().x + button.getBounds().width - buttonWidth;
    button.setBounds( newX, button.getBounds().y, buttonWidth, button.getBounds().height );
  }
}
