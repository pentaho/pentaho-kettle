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

package org.pentaho.di.core;

import org.pentaho.di.i18n.BaseMessages;

/**
 * This class is used to store results of transformation and step verifications.
 *
 * @author Matt
 * @since 11-01-04
 *
 */
public class CheckResult implements CheckResultInterface {
  private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!

  public static final String[] typeDesc = {
    "", BaseMessages.getString( PKG, "CheckResult.OK" ), BaseMessages.getString( PKG, "CheckResult.Remark" ),
    BaseMessages.getString( PKG, "CheckResult.Warning" ), BaseMessages.getString( PKG, "CheckResult.Error" ) };

  private int type;

  private String text;

  // MB - Support both JobEntry and Step Checking
  // 6/26/07
  private CheckResultSourceInterface sourceMeta;

  private String errorCode;

  public CheckResult() {
    this( CheckResultInterface.TYPE_RESULT_NONE, "", null );
  }

  public CheckResult( int t, String s, CheckResultSourceInterface sourceMeta ) {
    type = t;
    text = s;
    this.sourceMeta = sourceMeta;
  }

  public CheckResult( int t, String errorCode, String s, CheckResultSourceInterface sourceMeta ) {
    this( t, s, sourceMeta );
    this.errorCode = errorCode;
  }

  @Override
  public int getType() {
    return type;
  }

  @Override
  public String getTypeDesc() {
    return typeDesc[type];
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public CheckResultSourceInterface getSourceInfo() {
    return sourceMeta;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append( typeDesc[type] ).append( ": " ).append( text );

    if ( sourceMeta != null ) {
      sb.append( " (" ).append( sourceMeta.getName() ).append( ")" );
    }

    return sb.toString();
  }

  /**
   * @return the errorCode
   */
  @Override
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * @param errorCode
   *          the errorCode to set
   */
  @Override
  public void setErrorCode( String errorCode ) {
    this.errorCode = errorCode;
  }

  @Override
  public void setText( String value ) {
    this.text = value;
  }

  @Override
  public void setType( int value ) {
    this.type = value;
  }

}
