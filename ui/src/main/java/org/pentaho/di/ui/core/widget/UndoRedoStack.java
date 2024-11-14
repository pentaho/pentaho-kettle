/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.core.widget;

public class UndoRedoStack {

  public static final int DELETE = 0;
  public static final int INSERT = 1;

  private String strNewText;
  private String strReplacedText;
  private int iCursorPosition;
  private int iEventLength;
  private int iType;

  public UndoRedoStack( int iCursorPosition, String strNewText, String strReplacedText, int iEventLength, int iType ) {
    this.iCursorPosition = iCursorPosition;
    this.strNewText = strNewText;
    this.strReplacedText = strReplacedText;
    this.iEventLength = iEventLength;
    this.iType = iType;
  }

  public String getReplacedText() {
    return this.strReplacedText;
  }

  public String getNewText() {
    return this.strNewText;
  }

  public int getCursorPosition() {
    return this.iCursorPosition;
  }

  public int getEventLength() {
    return iEventLength;
  }

  public int getType() {
    return iType;
  }

}
