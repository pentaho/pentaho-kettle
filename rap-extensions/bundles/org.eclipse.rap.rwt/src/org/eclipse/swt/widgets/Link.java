/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
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

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.ILinkAdapter;
import org.eclipse.swt.internal.widgets.linkkit.LinkLCA;


/**
 * Instances of this class represent a selectable
 * user interface object that displays a text with
 * links.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @since 1.0
 */
public class Link extends Control {

  private String text;
  private String displayText;
  private Point[] offsets;
  private String[] ids;
  private int[] mnemonics;
  private transient ILinkAdapter linkAdapter;

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
   *
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Link( Composite parent, int style ) {
    super( parent, style );
    text = "";
    displayText = "";
  }

  @Override
  void initState() {
    addState( THEME_BACKGROUND );
  }

  /**
   * Sets the receiver's text.
   * <p>
   * The string can contain both regular text and hyperlinks.  A hyperlink
   * is delimited by an anchor tag, &lt;A&gt; and &lt;/A&gt;.  Within an
   * anchor, a single HREF attribute is supported.  When a hyperlink is
   * selected, the text field of the selection event contains either the
   * text of the hyperlink or the value of its HREF, if one was specified.
   * In the rare case of identical hyperlinks within the same string, the
   * HREF tag can be used to distinguish between them.  The string may
   * include the mnemonic character and line delimiters.
   * </p>
   *
   * @param string the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setText( String string ) {
    checkWidget();
    if( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( !string.equals( text ) ) {
      displayText = parse( string );
      text = string;
    }
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

  ///////////////////////////////////////
  // Listener registration/deregistration

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is called when the control is selected.
   * <code>widgetDefaultSelected</code> is not called.
   * </p>
   *
   * @param listener the listener which should be notified
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
   * @see #removeSelectionListener
   * @see SelectionEvent
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
   * be notified when the control is selected.
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

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int width = 0;
    int height = 0;
    if( ( displayText.length() > 0 ) ) {
      // Replace '&' with '&&' to ensure proper size calculation with one '&',
      // because the other will be escaped in
      // TextSizeUtil#createMeasureString()
      String string = escapeAmpersand( displayText );
      Point extent = TextSizeUtil.textExtent( getFont(), string, wHint );
      width = extent.x;
      height = extent.y;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    BoxDimensions border = getBorder();
    BoxDimensions padding = getPadding();
    width += border.left + border.right + padding.left + padding.right;
    height += border.top + border.bottom + padding.top + padding.bottom;
    return new Point( width, height );
  }

  private static String escapeAmpersand( String string ) {
    StringBuilder result = new StringBuilder();
    for( int i = 0; i < string.length(); i++ ) {
      if( string.charAt( i ) == '&' ) {
        result.append( "&&" );
      } else {
        result.append( string.charAt( i ) );
      }
    }
    return result.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == ILinkAdapter.class ) {
      if( linkAdapter == null ) {
        linkAdapter = new ILinkAdapter() {
          @Override
          public String getDisplayText() {
            return displayText;
          }
          @Override
          public Point[] getOffsets() {
            return offsets;
          }
          @Override
          public String[] getIds() {
            return ids;
          }
        };
      }
      return ( T )linkAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )LinkLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  @Override
  String getNameText() {
    return getText();
  }

  /* verbatim copy from SWT */
  @SuppressWarnings("all")
  String parse (String string) {
    int length = string.length ();
    offsets = new Point [length / 4];
    ids = new String [length / 4];
    mnemonics = new int [length / 4 + 1];
    StringBuffer result = new StringBuffer ();
    char [] buffer = new char [length];
    string.getChars (0, string.length (), buffer, 0);
    int index = 0, state = 0, linkIndex = 0;
    int start = 0, tagStart = 0, linkStart = 0, endtagStart = 0, refStart = 0;
    while (index < length) {
      char c = Character.toLowerCase (buffer [index]);
      switch (state) {
        case 0:
          if (c == '<') {
            tagStart = index;
            state++;
          }
          break;
        case 1:
          if (c == 'a') {
            state++;
          }
          break;
        case 2:
          switch (c) {
            case 'h':
              state = 7;
              break;
            case '>':
              linkStart = index  + 1;
              state++;
              break;
            default:
              if (Character.isWhitespace(c)) {
                break;
              } else {
                state = 13;
              }
          }
          break;
        case 3:
          if (c == '<') {
            endtagStart = index;
            state++;
          }
          break;
        case 4:
          state = c == '/' ? state + 1 : 3;
          break;
        case 5:
          state = c == 'a' ? state + 1 : 3;
          break;
        case 6:
          if (c == '>') {
            mnemonics [linkIndex] = parseMnemonics (buffer, start, tagStart, result);
            int offset = result.length ();
            parseMnemonics (buffer, linkStart, endtagStart, result);
            offsets [linkIndex] = new Point (offset, result.length () - 1);
            if (ids [linkIndex] == null) {
              ids [linkIndex] = new String (buffer, linkStart, endtagStart - linkStart);
            }
            linkIndex++;
            start = tagStart = linkStart = endtagStart = refStart = index + 1;
            state = 0;
          } else {
            state = 3;
          }
          break;
        case 7:
          state = c == 'r' ? state + 1 : 0;
          break;
        case 8:
          state = c == 'e' ? state + 1 : 0;
          break;
        case 9:
          state = c == 'f' ? state + 1 : 0;
          break;
        case 10:
          state = c == '=' ? state + 1 : 0;
          break;
        case 11:
          if (c == '"') {
            state++;
            refStart = index + 1;
          } else {
            state = 0;
          }
          break;
        case 12:
          if (c == '"') {
            ids[linkIndex] = new String (buffer, refStart, index - refStart);
            state = 2;
          }
          break;
        case 13:
          if (Character.isWhitespace (c)) {
            state = 0;
          } else if (c == '='){
            state++;
          }
          break;
        case 14:
          state = c == '"' ? state + 1 : 0;
          break;
        case 15:
          if (c == '"') {
            state = 2;
          }
          break;
        default:
          state = 0;
          break;
      }
      index++;
    }
    if (start < length) {
      int tmp = parseMnemonics (buffer, start, tagStart, result);
      int mnemonic = parseMnemonics (buffer, Math.max (tagStart, linkStart), length, result);
      if (mnemonic == -1) {
        mnemonic = tmp;
      }
      mnemonics [linkIndex] = mnemonic;
    } else {
      mnemonics [linkIndex] = -1;
    }
    if (offsets.length != linkIndex) {
      Point [] newOffsets = new Point [linkIndex];
      System.arraycopy (offsets, 0, newOffsets, 0, linkIndex);
      offsets = newOffsets;
      String [] newIDs = new String [linkIndex];
      System.arraycopy (ids, 0, newIDs, 0, linkIndex);
      ids = newIDs;
      int [] newMnemonics = new int [linkIndex + 1];
      System.arraycopy (mnemonics, 0, newMnemonics, 0, linkIndex + 1);
      mnemonics = newMnemonics;
    }
    return result.toString ();
  }

  /* verbatim copy from SWT */
  int parseMnemonics (char[] buffer, int start, int end, StringBuffer result) {
    int mnemonic = -1, index = start;
    while (index < end) {
      if (buffer [index] == '&') {
        if (index + 1 < end && buffer [index + 1] == '&') {
          result.append (buffer [index]);
          index++;
        } else {
          mnemonic = result.length();
        }
      } else {
        result.append (buffer [index]);
      }
      index++;
    }
    return mnemonic;
  }

}
