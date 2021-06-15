/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH.
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
import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.FontUtil;
import org.eclipse.swt.internal.widgets.ITextAdapter;
import org.eclipse.swt.internal.widgets.textkit.TextLCA;
import org.eclipse.swt.internal.widgets.textkit.TextThemeAdapter;


/**
 * Instances of this class are selectable user interface
 * objects that allow the user to enter and modify text.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CENTER, LEFT, MULTI, PASSWORD, SEARCH, SINGLE, RIGHT, READ_ONLY, WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Verify</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles MULTI and SINGLE may be specified.
 * </p>
 * <p>
 * Note: The styles ICON_CANCEL and ICON_SEARCH are hints used in combination with SEARCH.
 * When the platform supports the hint, the text control shows these icons.  When an icon
 * is selected, a default selection event is sent with the detail field set to one of
 * ICON_CANCEL or ICON_SEARCH.  Normally, application code does not need to check the
 * detail.  In the case of ICON_CANCEL, the text is cleared before the default selection
 * event is sent causing the application to search for an empty string.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * <p>Due to limitations of the JavaScript library, the current WRAP behavior
 * of a MULI line text is always as if WRAP was set.</p>
 * @since 1.0
 */
public class Text extends Scrollable {

  private static final Size ZERO = new Size( 0, 0 );

  static final double LINE_HEIGHT_FACTOR = 1.4;

  /**
   * The maximum number of characters that can be entered
   * into a text widget.
   * <p>
   * Note that this value is platform dependent, based upon
   * the native widget implementation.
   * </p>
   */
  public static final int LIMIT = Integer.MAX_VALUE;

  /**
   * The delimiter used by multi-line text widgets.  When text
   * is queried from the widget, it will be delimited using
   * this delimiter.
   */
  public static final String DELIMITER = "\n";

  private ITextAdapter textAdapter;
  private String text;
  private String message;
  private int textLimit;
  private final Point selection;
  private char echoChar;

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
   * @see SWT#SINGLE
   * @see SWT#MULTI
   * @see SWT#READ_ONLY
   * @see SWT#WRAP
   * @see SWT#PASSWORD
   * @see SWT#SEARCH
   * @see SWT#ICON_SEARCH
   * @see SWT#ICON_CANCEL
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Text( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    textLimit = LIMIT;
    selection = new Point( 0, 0 );
    text = "";
    message = "";
    echoChar = ( char )0;
    if( ( style & SWT.PASSWORD ) != 0 ) {
      echoChar = '?';
    }
    if( ( style & SWT.SEARCH ) != 0 ) {
      /*
       * Ensure that SWT.ICON_CANCEL and ICON_SEARCH are set. NOTE: ICON_CANCEL
       * has the same value as H_SCROLL and ICON_SEARCH has the same value as
       * V_SCROLL so it is necessary to first clear these bits to avoid a scroll
       * bar and then reset the bit using the original style supplied by the
       * programmer.
       */
      if( ( style & SWT.ICON_CANCEL ) != 0 ) {
        this.style |= SWT.ICON_CANCEL;
      }
      if( ( style & SWT.ICON_SEARCH ) != 0 ) {
        this.style |= SWT.ICON_SEARCH;
      }
    }
  }

  @Override
  void initState() {
    if( ( style & SWT.READ_ONLY ) != 0 ) {
      if( ( style & ( SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL ) ) == 0 ) {
        addState( THEME_BACKGROUND );
      }
    }
  }

  /**
   * Sets the contents of the receiver to the given string. If the receiver has style
   * SINGLE and the argument contains multiple lines of text, the result of this
   * operation is undefined and may vary from platform to platform.
   *
   * @param text the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setText( String text ) {
    checkWidget();
    if( text == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( internalSetText( text ) ) {
      resetSelection();
      notifyListeners( SWT.Modify, new Event() );
    }
  }

  /**
   * Returns the widget text.
   * <p>
   * The text for a text widget is the characters in the widget, or
   * an empty string if this has never been set.
   * </p>
   *
   * @return the widget text
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
   * Sets the contents of the receiver to the characters in the array. If the receiver has style
   * SINGLE and the argument contains multiple lines of text, the result of this
   * operation is undefined and may vary from platform to platform.
   *
   * @param text a character array that contains the new text
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the array is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @see #getTextChars()
   * @since 1.4
   */
  public void setTextChars( char[] text ) {
    checkWidget();
    if( text == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    setText( new String( text ) );
  }

  /**
   * Returns the widget's text as a character array.
   * <p>
   * The text for a text widget is the characters in the widget, or
   * a zero length array if this has never been set.
   * </p>
   *
   * @return a character array that contains the widget's text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @see #setTextChars(char[])
   * @since 1.4
   */
  public char[] getTextChars() {
    checkWidget();
    int length = text.length();
    char[] chars = new char[ length ];
    if( length > 0 ) {
      text.getChars( 0, length, chars, 0 );
    }
    return chars;
  }

  /**
   * Returns a range of text. Returns an empty string if the start of the range
   * is greater than the end.
   * <p>
   * Indexing is zero based. The range of a selection is from 0..N-1 where N is
   * the number of characters in the widget.
   * </p>
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return the range of text
   * @exception SWTException <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   * @since 1.3
   */
  public String getText( int start, int end ) {
    checkWidget();
    String result;
    if( !( start <= end && 0 <= end ) ) {
      result = "";
    } else {
      int safeEnd = Math.min( end, text.length() - 1 );
      if( start > safeEnd ) {
        result = "";
      } else {
        int safeStart = Math.max( 0, start );
        /*
         * NOTE: The current implementation uses substring () which can
         * reference a potentially large character array.
         */
        result = text.substring( safeStart, safeEnd + 1 );
      }
    }
    return result;
  }

  /**
   * Appends a string.
   * <p>
   * The new text is appended to the text at
   * the end of the widget.
   * </p>
   *
   * @param string the string to be appended
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  // TODO [rh] fire VerifyEvent missing
  public void append( String string ) {
    checkWidget();
    if( string == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    setText( text + string );
  }

  /**
   * Returns the line delimiter.
   *
   * @return a string that is the line delimiter
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #DELIMITER
   */
  public String getLineDelimiter() {
    checkWidget();
    return DELIMITER;
  }

  /**
   * Returns the height of a line.
   *
   * @return the height of a row of text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getLineHeight() {
    checkWidget();
    int fontSize = FontUtil.getData( getFont() ).getHeight();
    return ( int )Math.floor( fontSize * LINE_HEIGHT_FACTOR );
  }

  /**
   * Sets the widget message. The message text is displayed
   * as a hint for the user, indicating the purpose of the field.
   * <p>
   * Typically this is used in conjunction with <code>SWT.SEARCH</code>.
   * </p>
   *
   * @param message the new message
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the message is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public void setMessage( String message ) {
    checkWidget();
    if( message == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    this.message = message;
  }

  /**
   * Returns the widget message.  The message text is displayed
   * as a hint for the user, indicating the purpose of the field.
   * <p>
   * Typically this is used in conjunction with <code>SWT.SEARCH</code>.
   * </p>
   *
   * @return the widget message
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  public String getMessage() {
    checkWidget();
    return message;
  }

  /**
   * Sets the echo character.
   * <p>
   * The echo character is the character that is
   * displayed when the user enters text or the
   * text is changed by the programmer. Setting
   * the echo character to '\0' clears the echo
   * character and redraws the original text.
   * If for any reason the echo character is invalid,
   * or if the platform does not allow modification
   * of the echo character, the default echo character
   * for the platform is used.
   * </p>
   *
   * @param echo the new echo character
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.3
   */
  public void setEchoChar( char echo ) {
    checkWidget();
    if( ( style & SWT.MULTI ) == 0 ) {
      echoChar = echo;
    }
  }

  /**
   * Returns the echo character.
   * <p>
   * The echo character is the character that is
   * displayed when the user enters text or the
   * text is changed by the programmer.
   * </p>
   *
   * @return the echo character
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setEchoChar
   * @since 1.3
   */
  public char getEchoChar() {
    checkWidget ();
    return echoChar;
  }

  /**
   * Sets the maximum number of characters that the receiver
   * is capable of holding to be the argument.
   * <p>
   * Instead of trying to set the text limit to zero, consider
   * creating a read-only text widget.
   * </p><p>
   * To reset this value to the default, use <code>setTextLimit(Text.LIMIT)</code>.
   * Specifying a limit value larger than <code>Text.LIMIT</code> sets the
   * receiver's limit to <code>Text.LIMIT</code>.
   * </p>
   *
   * @param textLimit new text limit
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   */
  public void setTextLimit( int textLimit ) {
    checkWidget();
    if( textLimit == 0 ) {
      error( SWT.ERROR_CANNOT_BE_ZERO );
    }
    // Note that we mimic here the behavior of SWT Text with style MULTI on
    // Windows. In SWT, other operating systems and/or style flags behave
    // different.
    this.textLimit = textLimit;
  }

  /**
   * Returns the maximum number of characters that the receiver is capable of holding.
   * <p>
   * If this has not been changed by <code>setTextLimit()</code>,
   * it will be the constant <code>Text.LIMIT</code>.
   * </p>
   *
   * @return the text limit
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #LIMIT
   */
  public int getTextLimit() {
    checkWidget ();
    return textLimit;
  }

  /**
   * Returns the number of characters.
   *
   * @return number of characters in the widget
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public int getCharCount() {
    checkWidget();
    return text.length();
  }

  /**
   * Sets the selection.
   * <p>
   * Indexing is zero based.  The range of
   * a selection is from 0..N where N is
   * the number of characters in the widget.
   * </p><p>
   * Text selections are specified in terms of
   * caret positions.  In a text widget that
   * contains N characters, there are N+1 caret
   * positions, ranging from 0..N.  This differs
   * from other functions that address character
   * position such as getText () that use the
   * regular array indexing rules.
   * </p>
   *
   * @param start new caret position
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int start ) {
    checkWidget();
    setSelection( start, start );
  }

  /**
   * Sets the selection to the range specified
   * by the given start and end indices.
   * <p>
   * Indexing is zero based.  The range of
   * a selection is from 0..N where N is
   * the number of characters in the widget.
   * </p><p>
   * Text selections are specified in terms of
   * caret positions.  In a text widget that
   * contains N characters, there are N+1 caret
   * positions, ranging from 0..N.  This differs
   * from other functions that address character
   * position such as getText () that use the
   * usual array indexing rules.
   * </p>
   *
   * @param start the start of the range
   * @param end the end of the range
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( int start, int end ) {
    checkWidget();
    int validatedStart = selection.x;
    int validatedEnd = selection.y;
    if( start >= 0 && end >= start ) {
      validatedStart = Math.min( start, text.length() );
      validatedEnd = Math.min( end, text.length() );
    } else if ( end >= 0 && start > end ) {
      validatedStart = Math.min( end, text.length() );
      validatedEnd = Math.min( start, text.length() );
    }
    selection.x = validatedStart;
    selection.y = validatedEnd;
  }

  /**
   * Sets the selection to the range specified
   * by the given point, where the x coordinate
   * represents the start index and the y coordinate
   * represents the end index.
   * <p>
   * Indexing is zero based.  The range of
   * a selection is from 0..N where N is
   * the number of characters in the widget.
   * </p><p>
   * Text selections are specified in terms of
   * caret positions.  In a text widget that
   * contains N characters, there are N+1 caret
   * positions, ranging from 0..N.  This differs
   * from other functions that address character
   * position such as getText () that use the
   * usual array indexing rules.
   * </p>
   *
   * @param selection the point
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setSelection( Point selection ) {
    checkWidget();
    if( selection == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    setSelection( selection.x, selection.y );
  }

  /**
   * Returns a <code>Point</code> whose x coordinate is the
   * character position representing the start of the selected
   * text, and whose y coordinate is the character position
   * representing the end of the selection. An "empty" selection
   * is indicated by the x and y coordinates having the same value.
   * <p>
   * Indexing is zero based.  The range of a selection is from
   * 0..N where N is the number of characters in the widget.
   * </p>
   *
   * @return a point representing the selection start and end
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Point getSelection() {
    checkWidget();
    return new Point( selection.x, selection.y );
  }

  /**
   * Returns the number of selected characters.
   *
   * @return the number of selected characters.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int getSelectionCount() {
    checkWidget();
    return selection.y - selection.x;
  }

  /**
   * Gets the selected text, or an empty string if there is no current selection.
   *
   * @return the selected text
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public String getSelectionText() {
    checkWidget();
    return text.substring( selection.x, selection.y );
  }

  /**
   * Clears the selection.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void clearSelection() {
    checkWidget();
    selection.x = selection.y;
  }

  /**
   * Selects all the text in the receiver.
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void selectAll() {
    checkWidget();
    selection.x = 0;
    selection.y = text.length();
  }

  /**
   * Returns the character position of the caret.
   * <p>
   * Indexing is zero based.
   * </p>
   *
   * @return the position of the caret
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   * @since 1.3
   */
  public int getCaretPosition() {
    checkWidget();
    return selection.x;
  }

  /**
   * Sets the editable state.
   *
   * @param editable the new editable state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setEditable( boolean editable ) {
    checkWidget();
    style &= ~SWT.READ_ONLY;
    if( !editable ) {
      style |= SWT.READ_ONLY;
    }
  }

  /**
   * Returns the editable state.
   *
   * @return whether or not the receiver is editable
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public boolean getEditable() {
    checkWidget();
    return ( style & SWT.READ_ONLY ) == 0;
  }

  /**
   * Inserts a string.
   * <p>
   * The old selection is replaced with the new text.
   * </p>
   *
   * @param string the string
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the string is <code>null</code></li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void insert( String string ) {
    checkWidget();
    if( string == null ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    String oldText = getText();
    Point sel = getSelection();
    String replace = oldText.substring( 0, sel.x );
    replace += string;
    replace += oldText.substring( sel.y );
    setText( replace );
    setSelection( sel.x + string.length() );
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    int height = 0, width = 0;
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
      Font font = getFont();
      boolean wrap = ( style & SWT.WRAP ) != 0 ;
      int wrapWidth = 0;
      if( wrap && wHint != SWT.DEFAULT ) {
        wrapWidth = wHint;
      }
      Point extent;
      Point messageExtent = TextSizeUtil.stringExtent( font, message );
      // Single-line text field should have same size as Combo, Spinner, etc.
      if( ( getStyle() & SWT.SINGLE ) != 0 ) {
        extent = TextSizeUtil.stringExtent( font, text );
      } else {
        extent = TextSizeUtil.textExtent( font, text, wrapWidth );
      }
      extent.x = Math.max( extent.x, messageExtent.x );
      extent.y = Math.max( extent.y, messageExtent.y );
      if( extent.x != 0 ) {
        width = extent.x;
      }
      if( extent.y != 0 ) {
        height = extent.y;
      }
      Size searchIconSize = getSearchIconOuterSize();
      Size cancelIconSize = getCancelIconOuterSize();
      width += searchIconSize.width + cancelIconSize.width;
      height = Math.max( height, searchIconSize.height );
      height = Math.max( height, cancelIconSize.height );
    }
    if( width == 0 ) {
      width = DEFAULT_WIDTH;
    }
    if( height == 0 ) {
      height = DEFAULT_HEIGHT;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    // [rh] Fix for bug 306354: take into account that there is now 1px
    // right padding on the client side (see Text.js#_applyElement)
    width += 1;
    Rectangle trim = computeTrim( 0, 0, width, height );
    return new Point( trim.width, trim.height );
  }

  @Override
  public Rectangle computeTrim( int x, int y, int width, int height ) {
    Rectangle result = super.computeTrim( x, y, width, height );
    if( ( style & SWT.MULTI ) != 0 && ( style & SWT.H_SCROLL ) != 0 ) {
      result.width++;
    }
    return result;
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the control is selected by the user, by sending
   * it one of the messages defined in the <code>SelectionListener</code>
   * interface.
   * <p>
   * <code>widgetSelected</code> is not called for texts.
   * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed in a single-line text,
   * or when ENTER is pressed in a search text. If the receiver has the <code>SWT.SEARCH | SWT.ICON_CANCEL</code> style
   * and the user cancels the search, the event object detail field contains the value <code>SWT.ICON_CANCEL</code>.
   * Likewise, if the receiver has the <code>SWT.ICON_SEARCH</code> style and the icon search is selected, the
   * event object detail field contains the value <code>SWT.ICON_SEARCH</code>.
   * </p>
   *
   * @param listener the listener which should be notified when the control is selected by the user
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

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's text is modified, by sending
   * it one of the messages defined in the <code>ModifyListener</code>
   * interface.
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
   * @see ModifyListener
   * @see #removeModifyListener
   */
  public void addModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Modify, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver's text is modified.
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
   * @see ModifyListener
   * @see #addModifyListener
   */
  public void removeModifyListener( ModifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Modify, listener );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver's text is verified, by sending
   * it one of the messages defined in the <code>VerifyListener</code>
   * interface.
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
   * @see VerifyListener
   * @see #removeVerifyListener
   */
  public void addVerifyListener( VerifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Verify, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the control is verified.
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
   * @see VerifyListener
   * @see #addVerifyListener
   */
  public void removeVerifyListener( VerifyListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Verify, listener );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == ITextAdapter.class ) {
      if( textAdapter == null ) {
        textAdapter = new ITextAdapter() {
          @Override
          public void setText( String text ) {
            if( internalSetText( text ) ) {
              adjustSelection();
              notifyListeners( SWT.Modify, new Event() );
            }
          }
        };
      }
      return ( T )textAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )TextLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  @Override
  public ScrollBar getHorizontalBar() {
    checkWidget();
    // [if] Client-side multi Text widget is based on HTML textarea and doesn't have separate,
    // server managed scrollbars
    return null;
  }

  @Override
  public ScrollBar getVerticalBar() {
    checkWidget();
    // [if] Client-side multi Text widget is based on HTML textarea and doesn't have separate,
    // server managed scrollbars
    return null;
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  private boolean internalSetText( String text ) {
    String verifiedText = verifyText( text, 0, this.text.length() );
    if( verifiedText != null ) {
      if( verifiedText.length() > textLimit ) {
        this.text = verifiedText.substring( 0, textLimit );
      } else {
        this.text = verifiedText;
      }
    }
    return verifiedText != null;
  }

  private String verifyText( String text, int start, int end ) {
    Event event = new Event();
    event.text = text;
    event.start = start;
    event.end = end;
    notifyListeners( SWT.Verify, event );
    /*
     * It is possible (but unlikely), that application code could have disposed
     * the widget in the verify event. If this happens, answer null to cancel
     * the operation.
     */
    String result;
    if( event.doit && !isDisposed() ) {
      result = event.text;
    } else {
      return null;
    }
    return result;
  }

  private void resetSelection() {
    selection.x = 0;
    selection.y = 0;
  }

  private void adjustSelection() {
    selection.x = Math.min( selection.x, text.length() );
    selection.y = Math.min( selection.y, text.length() );
  }

  private static int checkStyle( int style ) {
    int result = style;
    if( ( result & SWT.SEARCH ) != 0 ) {
      result |= SWT.SINGLE | SWT.BORDER;
      result &= ~SWT.PASSWORD;
    }
    if( ( result & SWT.SINGLE ) != 0 && ( result & SWT.MULTI ) != 0 ) {
      result &= ~SWT.MULTI;
    }
    result = checkBits( result, SWT.LEFT, SWT.CENTER, SWT.RIGHT, 0, 0, 0 );
    if( ( result & SWT.SINGLE ) != 0 ) {
      result &= ~( SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP );
    }
    if( ( result & SWT.WRAP ) != 0 ) {
      result |= SWT.MULTI;
      result &= ~SWT.H_SCROLL;
    }
    if( ( result & SWT.MULTI ) != 0 ) {
      result &= ~SWT.PASSWORD;
    }
    if( ( result & ( SWT.SINGLE | SWT.MULTI ) ) != 0 ) {
      return result;
    }
    if( ( style & ( SWT.H_SCROLL | SWT.V_SCROLL ) ) != 0 ) {
      return result | SWT.MULTI;
    }
    return result | SWT.SINGLE;
  }

  private Size getSearchIconOuterSize() {
    if( ( style & SWT.SEARCH ) != 0 && ( style & SWT.ICON_SEARCH ) != 0 ) {
      Size imageSize = getThemeAdapter().getSearchIconImageSize( this );
      int spacing = getThemeAdapter().getSearchIconSpacing( this );
      return new Size( imageSize.width + spacing, imageSize.height );
    }
    return ZERO;
  }

  private Size getCancelIconOuterSize() {
    if( ( style & SWT.SEARCH ) != 0 && ( style & SWT.ICON_CANCEL ) != 0 ) {
      Size imageSize = getThemeAdapter().getCancelIconImageSize( this );
      int spacing = getThemeAdapter().getCancelIconSpacing( this );
      return new Size( imageSize.width + spacing, imageSize.height );
    }
    return ZERO;
  }

  private TextThemeAdapter getThemeAdapter() {
    return ( TextThemeAdapter )getAdapter( ThemeAdapter.class );
  }

  public void showSelection() {
    return;
  }
}