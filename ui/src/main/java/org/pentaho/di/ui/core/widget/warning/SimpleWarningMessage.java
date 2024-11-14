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


package org.pentaho.di.ui.core.widget.warning;

public class SimpleWarningMessage implements WarningMessageInterface {

  private String warningMessage;
  private boolean warning;

  /**
   * @param warning
   * @param warningMessage
   */
  public SimpleWarningMessage( boolean warning, String warningMessage ) {
    this.warning = warning;
    this.warningMessage = warningMessage;
  }

  /**
   * @return the warningMessage
   */
  public String getWarningMessage() {
    return warningMessage;
  }

  /**
   * @param warningMessage
   *          the warningMessage to set
   */
  public void setWarningMessage( String warningMessage ) {
    this.warningMessage = warningMessage;
  }

  /**
   * @return the warning
   */
  public boolean isWarning() {
    return warning;
  }

  /**
   * @param warning
   *          the warning to set
   */
  public void setWarning( boolean warning ) {
    this.warning = warning;
  }
}
