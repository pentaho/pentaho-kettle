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


package org.pentaho.di.core.gui;

public class GUIFactory {

  private static SpoonInterface spoonInstance;
  private static ThreadDialogs threadDialogs = new RuntimeThreadDialogs(); // default to the runtime one

  public static SpoonInterface getInstance() {
    return spoonInstance;
  }

  public static void setSpoonInstance( SpoonInterface anInstance ) {
    spoonInstance = anInstance;
  }

  public static ThreadDialogs getThreadDialogs() {
    return threadDialogs;
  }

  public static void setThreadDialogs( ThreadDialogs threadDialogs ) {
    GUIFactory.threadDialogs = threadDialogs;
  }

}
