/*******************************************************************************
 * Copyright (c) 2017 Hitachi America, Ltd., R&D.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hitachi America, Ltd., R&D - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


public class StyledText extends Text {

  public StyledText( Composite parent, int style ) {
    super( parent, style );
  }

  /**
   * Returns the caret position relative to the start of the text.
   *
   * @return the caret position relative to the start of the text.
   */
  public int getCaretOffset() {
    return getCaretPosition();
  }

  /**
   * Sets the caret offset.
   * @param offset caret offset, relative to the first character in the text.
   */
  public void setCaretOffset( int offset ) {
    setSelection( offset );
  }

  /**
   * Returns the selection.
   *
   * @return start and length of the selection, x is the offset of the
   *  first selected character, relative to the first character of the
   *  widget content. y is the length of the selection.
   *  The selection values returned are visual (i.e., length will always always be
   *  positive).  To determine if a selection is right-to-left (RtoL) vs. left-to-right
   *  (LtoR), compare the caretOffset to the start and end of the selection
   *  (e.g., caretOffset == start of selection implies that the selection is RtoL).
   */
  public Point getSelectionRange() {
    Point selection = getSelection();
    return new Point(selection.x, selection.y - selection.x);
  }

  /**
   * Replaces the given text range with new text.
   * @param start offset of first character to replace
   * @param length number of characters to replace. Use 0 to insert text
   * @param text new text. May be empty to delete text.
   */
  public void replaceTextRange( int start, int length, String text ) {
    setText( getText().substring( 0, start )
      + text
      + getText().substring( start + length ) );
  }

  /*
   * Empty stub
   */
  public void setStyleRange( StyleRange style ) {

  }

  /**
   * Empty stub
   * @param ranges
   * @param styles
   */
  public void setStyleRanges(int[] ranges, StyleRange[] styles) {
  }

  /*
   * Empty stub
   */
  public void setStyleRanges( StyleRange[] styles ) {

  }

  /*
   * Empty stub
   */
  public StyleRange getStyleRangeAtOffset(int offset) {
    return null;
  }

  /*
   * Empty stub
   */
  public void addLineStyleListener( LineStyleListener listener ) {

  }

  /*
   * Empty stub
   */
  public void addExtendedModifyListener( ExtendedModifyListener listener ) {

  }

  public int getLineAtOffset( int iOffset ) {
    String beforeOffset = getText().substring( 0, iOffset );
    return beforeOffset.length() - beforeOffset.replace( DELIMITER, "" ).length();
  }

  /**
   * Return getLineHeight() no matter what offset is given.
   * @param offset
   * @return
   */
  public int getLineHeight( int offset ) {
    return getLineHeight();
  }

  /**
   * Return getLocation() no matter what offset is given for the sake of single-sourcing.
   * @param offset
   * @return
   */
  public Point getLocationAtOffset( int offset ) {
    return getLocation();
  }

  /*
   * Empty stub
   */
  public int getOffsetAtLocation( Point point ) {
    return 0;
  }

  /*
   * Empty stub
   */
  public void cut() {

  }

  /*
   * Empty stub
   */
  public void copy() {

  }

  /*
   * Empty stub
   */
  public void paste() {

  }

  /*
   * Empty stub
   */
  public void setCaret( Caret caret ) {
  }
}
