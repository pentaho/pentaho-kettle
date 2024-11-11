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


package org.pentaho.di.core.gui;

public class RuntimeThreadDialogs implements ThreadDialogs {

  @Override
  public boolean threadMessageBox( String message, String text, boolean allowCancel, int type ) {

    // assume its ok to return to ok
    return true;
  }

}
