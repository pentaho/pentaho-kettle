/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static org.eclipse.rap.rwt.internal.textsize.TextSizeUtil.stringExtent;

import java.util.StringTokenizer;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;


/**
 * Instances of this class are used to inform or warn the user.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>ICON_ERROR, ICON_INFORMATION, ICON_QUESTION, ICON_WARNING, ICON_WORKING</dd>
 * <dd>OK, OK | CANCEL</dd>
 * <dd>YES | NO, YES | NO | CANCEL</dd>
 * <dd>RETRY | CANCEL</dd>
 * <dd>ABORT | RETRY | IGNORE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ICON_ERROR, ICON_INFORMATION, ICON_QUESTION,
 * ICON_WARNING and ICON_WORKING may be specified.
 * </p><p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 * @since 1.2
 */
public class MessageBox extends Dialog {

  private static final int SPACING = 20;
  private static final int BUTTON_WIDTH = 61;
  private static final int MAX_WIDTH = 640;

  private Image image;
  private String message;
  private boolean markupEnabled;

  /**
   * Constructs a new instance of this class given only its parent.
   *
   * @param parent a shell which will be the parent of the new instance
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public MessageBox( Shell parent ) {
    this( parent, SWT.OK | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL );
  }

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   *
   * @param parent a shell which will be the parent of the new instance
   * @param style the style of dialog to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public MessageBox( Shell parent, int style ) {
    super( parent, checkStyle ( style ) );
    checkSubclass();
    message = "";
  }

  /**
   * Returns the dialog's message, or an empty string if it does not have one.
   * The message is a description of the purpose for which the dialog was opened.
   * This message will be visible in the dialog while it is open.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the dialog's message, which is a description of
   * the purpose for which it was opened. This message will be
   * visible on the dialog while it is open.
   *
   * @param string the message
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   */
  public void setMessage( String string ) {
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    message = string;
  }

  /**
   * Controls whether the use of <em>markup</em> in message is enabled. Once the markup in message
   * is enabled it's not possible to disable it.
   *
   * @param markupEnabled true to enable the markup in message
   *
   * @since 3.2
   */
  public void setMarkupEnabled( boolean markupEnabled ) {
    if( !this.markupEnabled ) {
      this.markupEnabled = markupEnabled;
    }
  }

  /**
   * Makes the dialog visible and brings it to the front
   * of the display.
   *
   * <!-- Begin RAP specific -->
   * <p><strong>RAP Note:</strong> This method is not supported when running the application in
   * JEE_COMPATIBILITY mode. Use <code>Dialog#open(DialogCallback)</code> instead.</p>
   * <!-- End RAP specific -->
   *
   * @return the ID of the button that was selected to dismiss the
   *         message box (e.g. SWT.OK, SWT.CANCEL, etc.)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the dialog has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the dialog</li>
   * </ul>
   * @exception UnsupportedOperationException when running the application in JEE_COMPATIBILITY mode
   *
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   * @see #open(DialogCallback)
   */
  public int open() {
    checkOperationMode();
    prepareOpen();
    runEventLoop( shell );
    return returnCode;
  }

  @Override
  protected void prepareOpen() {
    determineImageFromStyle();
    shell = new Shell( parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL );
    shell.setText( title );
    createControls();
    shell.setBounds( computeShellBounds() );
    shell.pack();
  }

  private void determineImageFromStyle() {
    image = null;
    int systemImageId = -1;
    if( ( style & SWT.ICON_ERROR ) != 0 ) {
      systemImageId = SWT.ICON_ERROR;
    } else if( ( style & SWT.ICON_INFORMATION ) != 0 ) {
      systemImageId = SWT.ICON_INFORMATION;
    } else if( ( style & SWT.ICON_QUESTION ) != 0 ) {
      systemImageId = SWT.ICON_QUESTION;
    } else if( ( style & SWT.ICON_WARNING ) != 0 ) {
      systemImageId = SWT.ICON_WARNING;
    } else if( ( style & SWT.ICON_WORKING ) != 0 ) {
      systemImageId = SWT.ICON_WORKING;
    }
    if( systemImageId != -1 ) {
      image = parent.getDisplay().getSystemImage( systemImageId );
    }
  }

  private Rectangle computeShellBounds() {
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    Point preferredSize = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    Rectangle displaySize = parent.getDisplay().getBounds();
    result.x = ( displaySize.width - preferredSize.x ) / 2 + displaySize.x;
    result.y = ( displaySize.height - preferredSize.y ) / 2 + displaySize.y;
    result.width = Math.min( preferredSize.x, MAX_WIDTH );
    result.height = preferredSize.y;
    return result;
  }

  private void createControls() {
    shell.setLayout( new GridLayout( 2, false ) );
    createImage();
    createText();
    createButtons();
  }

  private void createText() {
    Label textLabel = new Label( shell, SWT.WRAP );
    if( markupEnabled ) {
      textLabel.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    }
    GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    int imageWidth = image == null ? 0 : image.getBounds().width;
    int maxTextWidth = MAX_WIDTH - imageWidth - SPACING;
    int maxLineWidth = getMaxMessageLineWidth();
    if( maxLineWidth > maxTextWidth ) {
      data.widthHint = maxTextWidth;
    }
    textLabel.setLayoutData( data );
    textLabel.setText( message );
  }

  private void createImage() {
    if( image != null ) {
      Label label = new Label( shell, SWT.CENTER );
      GridData data = new GridData( SWT.CENTER, SWT.TOP, false, false );
      data.widthHint = image.getBounds().width + SPACING;
      label.setLayoutData( data );
      label.setImage( image );
    }
  }

  private void createButtons() {
    Composite buttonArea = new Composite( shell, SWT.NONE );
    buttonArea.setLayout( new GridLayout( 0, true ) );
    GridData buttonData = new GridData( SWT.CENTER, SWT.CENTER, true, false );
    buttonData.horizontalSpan = 2;
    buttonArea.setLayoutData( buttonData );
    createButton( buttonArea, SWT.getMessage( "SWT_Yes" ), SWT.YES );
    createButton( buttonArea, SWT.getMessage( "SWT_No" ), SWT.NO );
    createButton( buttonArea, SWT.getMessage( "SWT_OK" ), SWT.OK );
    createButton( buttonArea, SWT.getMessage( "SWT_Abort" ), SWT.ABORT );
    createButton( buttonArea, SWT.getMessage( "SWT_Retry" ), SWT.RETRY );
    createButton( buttonArea, SWT.getMessage( "SWT_Cancel" ), SWT.CANCEL );
    createButton( buttonArea, SWT.getMessage( "SWT_Ignore" ), SWT.IGNORE );
    buttonArea.getChildren()[ 0 ].forceFocus();
  }

  private void createButton( Composite parent, String text, final int buttonId ) {
    if( ( style & buttonId ) == buttonId ) {
      ( ( GridLayout ) parent.getLayout() ).numColumns++;
      Button result = new Button( parent, SWT.PUSH );
      GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
      int widthHint = convertHorizontalDLUsToPixels( shell, BUTTON_WIDTH );
      Point minSize = result.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
      data.widthHint = Math.max( widthHint, minSize.x );
      result.setLayoutData( data );
      result.setText( text );
      result.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent event ) {
          MessageBox.this.returnCode = buttonId;
          shell.close();
        }
      } );
    }
  }

  private int getMaxMessageLineWidth() {
    int result = 0;
    Font font = shell.getFont();
    String lineSeparator = markupEnabled ? "<br/>" : "\n";
    StringTokenizer tokenizer = new StringTokenizer( message, lineSeparator );
    while( tokenizer.hasMoreTokens() ) {
      String line = tokenizer.nextToken();
      int lineWidth = stringExtent( font, line, markupEnabled ).x;
      result = Math.max( result, lineWidth );
    }
    return result;
  }

  private static int checkStyle( int style ) {
    int chkStyle = 0;
    int mask = SWT.YES | SWT.NO | SWT.OK | SWT.CANCEL | SWT.ABORT | SWT.RETRY | SWT.IGNORE;
    int bits = style & mask;
    if(    bits == SWT.OK
        || bits == ( SWT.OK | SWT.CANCEL ) )
    {
      chkStyle = style;
    } else if(    bits == ( SWT.YES | SWT.NO )
               || bits == ( SWT.YES | SWT.NO | SWT.CANCEL ) )
    {
      chkStyle = style;
    } else if(    bits == ( SWT.RETRY | SWT.CANCEL )
               || bits == ( SWT.ABORT | SWT.RETRY | SWT.IGNORE ) )
    {
      chkStyle = style;
    } else {
      chkStyle = ( style & ~mask ) | SWT.OK;
    }
    return chkStyle;
  }

}
