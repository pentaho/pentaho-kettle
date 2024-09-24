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

package org.pentaho.libformula.editor;

import org.pentaho.reporting.libraries.formula.lvalues.ParsePosition;

public class FormulaMessage {
  public static final int TYPE_ERROR = 1;
  public static final int TYPE_WARNING = 2;
  public static final int TYPE_MESSAGE = 3;
  public static final int TYPE_FUNCTION = 4;
  public static final int TYPE_FIELD = 5;
  public static final int TYPE_STATIC_NUMBER = 6;
  public static final int TYPE_STATIC_STRING = 7;
  public static final int TYPE_STATIC_DATE = 8;
  public static final int TYPE_STATIC_LOGICAL = 9;

  private ParsePosition position;
  private int type;
  private String subject;
  private String message;

  public FormulaMessage( int type, ParsePosition position, String subject, String message ) {
    this.type = type;
    this.position = position;
    this.subject = subject;
    this.message = message;
  }

  public FormulaMessage( int type, String subject, String message ) {
    this( type, null, subject, message );
  }

  @Override
  public String toString() {
    String m = "";

    switch ( type ) {
      case TYPE_ERROR:
        m += "ERROR";
        break;
      case TYPE_WARNING:
        m += "WARNING";
        break;
      case TYPE_MESSAGE:
        m += "MESSAGE";
        break;
      case TYPE_FUNCTION:
        m += "FUNCTION";
        break;
      case TYPE_FIELD:
        m += "FIELD";
        break;
      case TYPE_STATIC_STRING:
        m += "STATIC STRING";
        break;
      case TYPE_STATIC_NUMBER:
        m += "STATIC NUMBER";
        break;
      case TYPE_STATIC_DATE:
        m += "STATIC DATE/TIME";
        break;
      case TYPE_STATIC_LOGICAL:
        m += "STATIC LOGICAL";
        break;
      default:
        break;
    }

    if ( position != null ) {
      m += "@" + position.getStartLine() + "/" + position.getStartColumn() + " : ";
    } else {
      m += " : ";
    }

    m += subject + " : " + message;

    return m;
  }

  /**
   * @return the position
   */
  public ParsePosition getPosition() {
    return position;
  }

  /**
   * @param position
   *          the position to set
   */
  public void setPosition( ParsePosition position ) {
    this.position = position;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject
   *          the subject to set
   */
  public void setSubject( String subject ) {
    this.subject = subject;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage( String message ) {
    this.message = message;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType( int type ) {
    this.type = type;
  }

}
