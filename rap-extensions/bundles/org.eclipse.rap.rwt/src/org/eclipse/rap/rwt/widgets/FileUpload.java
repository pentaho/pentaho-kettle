/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.internal.widgets.IFileUploadAdapter;
import org.eclipse.rap.rwt.internal.widgets.fileuploadkit.FileUploadLCA;
import org.eclipse.rap.rwt.internal.widgets.fileuploadkit.FileUploadThemeAdapter;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TypedListener;


// Known limitations:
// 1. The border can not be clicked.
// 2. Pressing the button by keyboard (Space/Enter) works, but will have
//    no visual feedback (pressed state).
// 3. The mouse-cursor can not be changed in Firefox, it will always be
//    the default, not the "pointer" hand.
// 4. In Chrome the keyboard control will only work if the button has been
//    focused with TAB, therefore the focus-frame will not be displayed if
//    focused with mouse in Chrome. Its still focused.

/**
 * A button-like widget allowing to select a file from the client's file system and to upload this
 * file to a given URL using HTTP POST. Multiple files may be selected if MULTI style is given,
 * but not all clients may support it.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>MULTI</dd>
 * </dl>
 * </p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileUpload extends Canvas {

  private final IFileUploadAdapter fileUploadAdapter;
  private String text;
  private Image image;
  private String[] fileNames = new String[ 0 ];
  private String[] filterExtensions = new String[ 0 ];
  private String url;

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
   * </p>
   *
   * @param parent a composite control which will be the parent of the new instance (cannot be null)
   * @param style the style of control to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
   * </ul>
   */
  public FileUpload( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    fileUploadAdapter = new FileUploadAdapter();
    text = "";
  }

  /**
   * Sets the receiver's text.
   *
   * @param text the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setText( String text ) {
    if( text == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    checkWidget();
    this.text = text;
  }

  /**
   * Returns the receiver's text, which will be an empty
   * string if it has never been set.
   *
   * @return the receiver's text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getText() {
    checkWidget();
    return text;
  }

  /**
   * Sets the receiver's image to the argument, which may be
   * <code>null</code> indicating that no image should be displayed.
   *
   * @param image the image to display on the receiver (may be <code>null</code>)
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setImage( Image image ) {
    checkWidget();
    if( image != null && image.isDisposed() ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    this.image = image;
  }

  /**
   * Returns the receiver's image if it has one, or null
   * if it does not.
   *
   * @return the receiver's image
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Image getImage() {
    checkWidget();
    return image;
  }

  /**
   * Returns the selected file name, without the path. If no file name has been selected,
   * <code>null</code> is returned. If multiple files have been selected, the first is returned.
   *
   * @return the selected file name
   *
   * @exception SWTException <ul>
   *   <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *   <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getFileName() {
    checkWidget();
    return fileNames.length > 0 ? fileNames[ 0 ] : null;
  }

  /**
   * Returns a (possibly empty) array with the names of all files that were selected.
   *
   * @return the names of the selected files
   * @since 2.2
   *
   * @exception SWTException <ul>
   *   <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *   <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String[] getFileNames() {
    checkWidget();
    return fileNames.clone();
  }

  /**
   * Set the file extensions which the widget will use to filter the files it shows to the argument,
   * which may not be null.
   * <p>
   * An extension filter string must be of the form ".extension".
   * </p>
   *
   * @param extensions the file extension filter
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the extensions is null</li>
   * </ul>
   * @exception SWTException <ul>
   *   <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *   <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 3.2
   */
  public void setFilterExtensions( String[] extensions ) {
    checkWidget();
    if( extensions == null ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    filterExtensions = extensions;
  }

  /**
   * Returns the file extensions which the widget will use to filter the files it shows.
   *
   * @return the file extensions filter
   *
   * @since 3.2
   */
  public String[] getFilterExtensions() {
    checkWidget();
    return filterExtensions;
  }

  /**
   * Starts to upload the selected file to the given URL using HTTP POST. If no file has been
   * selected, nothing happens.
   * <p>
   * <strong>Note:</strong> Calling this method while a file is still uploading might interrupt the
   * ongoing upload. Due to restrictions of the client, there is no feedback for success or failure
   * of the upload. This information can only be obtained from the server that accepts the upload.
   * </p>
   *
   * @param url the URL to upload to, must not be <code>null</code>
   *
   * @exception IllegalArgumentException <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the url is null</li>
   *              </ul>
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the
   *              receiver</li>
   *              </ul>
   */
  public void submit( String url ) {
    if( url == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    checkWidget();
    if( fileNames.length > 0 ) {
      this.url  = url;
    }
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the user changes the receiver's selection, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the selected file changes.
   * <code>widgetDefaultSelected</code> is not called.
   * </p>
   *
   * @param listener the listener which should be notified when the user changes the receiver's selection
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SelectionListener
   * @see SelectionEvent
   * @see #removeSelectionListener
   */
  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Selection, typedListener );
    addListener( SWT.DefaultSelection, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the user changes the receiver's selection.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SelectionListener
   * @see SelectionEvent
   * @see #addSelectionListener
   */
  public void removeSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Selection, listener );
    removeListener( SWT.DefaultSelection, listener );
  }

  /////////////////////
  // Overridden methods

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    boolean hasImage = image != null;
    boolean hasText = text.length() > 0;
    if( hasImage ) {
      Rectangle imageBounds = image.getBounds ();
      width = imageBounds.width;
      height = imageBounds.height;
    }
    if( hasText ) {
      Point extent = TextSizeUtil.stringExtent( getFont(), text );
      width += extent.x;
      height = Math.max( height, extent.y );
    }
    if( height == 0 ) {
      height = 10;
    }
    FileUploadThemeAdapter themeAdapter = getThemeAdapter();
    if( hasText && hasImage ) {
      int spacing = themeAdapter.getSpacing( this );
      width += spacing;
    }
    BoxDimensions padding = themeAdapter.getPadding( this );
    width += padding.left + padding.right;
    height += padding.top + padding.bottom;
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    BoxDimensions border = themeAdapter.getBorder( this );
    width += border.left + border.right;
    height += border.top + border.bottom;
    return new Point( width, height );
  }

  @Override
  public void setLayout( Layout layout ) {
    checkWidget();
    // prevent setting a layout
  }

  @Override
  public Control[] getChildren() {
    checkWidget();
    return new Control[ 0 ];
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == IFileUploadAdapter.class ) {
      return ( T )fileUploadAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )FileUploadLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  private static int checkStyle( int style ) {
    int mask = SWT.BORDER | SWT.MULTI;
    return style & mask;
  }

  private FileUploadThemeAdapter getThemeAdapter() {
    return ( FileUploadThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  ////////////////
  // Inner classes

  private final class FileUploadAdapter implements IFileUploadAdapter {

    @Override
    public void setFileNames( String[] value ) {
      fileNames = value;
    }

    @Override
    public String getAndResetUrl() {
      String result = url;
      url = null;
      return result;
    }

  }

}
