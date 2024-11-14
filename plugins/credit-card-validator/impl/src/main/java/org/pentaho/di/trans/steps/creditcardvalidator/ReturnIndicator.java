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


package org.pentaho.di.trans.steps.creditcardvalidator;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ReturnIndicator {
  public String CardType;
  public boolean CardValid;
  public String UnValidMsg;

  public ReturnIndicator() {
    super();
    CardValid = false;
    CardType = null;
    UnValidMsg = null;
  }

}
