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


package org.pentaho.di.trans.steps.mailvalidator;

public class MailValidationResult {

  private boolean isvalide;

  private String errMsg;

  public MailValidationResult() {
    this.isvalide = false;
    this.errMsg = null;
  }

  public boolean isValide() {
    return this.isvalide;
  }

  public void setValide( boolean valid ) {
    this.isvalide = valid;
  }

  public String getErrorMessage() {
    return this.errMsg;
  }

  public void setErrorMessage( String errMsg ) {
    this.errMsg = errMsg;
  }

}
