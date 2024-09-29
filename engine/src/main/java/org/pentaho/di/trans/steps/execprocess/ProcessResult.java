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


package org.pentaho.di.trans.steps.execprocess;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ProcessResult {
  private String outputStream;
  private String errorStream;
  private long exitValue;

  public ProcessResult() {
    super();
    this.outputStream = null;
    this.errorStream = null;
    this.exitValue = 1;
  }

  public String getOutputStream() {
    return this.outputStream;
  }

  public void setOutputStream( String string ) {
    this.outputStream = string;
  }

  public String getErrorStream() {
    return this.errorStream;
  }

  public void setErrorStream( String string ) {
    this.errorStream = string;
  }

  public long getExistStatus() {
    return this.exitValue;
  }

  public void setExistStatus( long value ) {
    this.exitValue = value;
  }
}
