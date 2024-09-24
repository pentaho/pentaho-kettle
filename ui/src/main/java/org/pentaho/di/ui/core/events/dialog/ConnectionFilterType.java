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
package org.pentaho.di.ui.core.events.dialog;

public enum ConnectionFilterType {

  S3, HCP, GS, SNW;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

}
