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


package org.pentaho.di.ui.spoon;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage breadcrumbing so that we know which document/tab/perspective opened a given document.
 *
 * @author jamesdixon
 *
 */
public class BreadcrumbManager {

  private Map<String, String> contentCallStack = new HashMap<String, String>();

  private static BreadcrumbManager INSTANCE = new BreadcrumbManager();

  public static BreadcrumbManager getInstance() {
    return INSTANCE;
  }

  /**
   * Adds a link between a caller id a callee id.
   *
   * @param caller
   * @param callee
   */
  public void addCaller( String caller, String callee ) {
    contentCallStack.put( callee, caller );
  }

  /**
   * Returns the id of the caller for a given callee id. Will return null if there is no caller registered for the
   * callee id.
   *
   * @param callee
   * @return
   */
  public String getCaller( String callee ) {
    return contentCallStack.get( callee );
  }

  /**
   * Removes the caller id of the callee from the breadcrumb manager
   *
   * @param callee
   */
  public void removeCaller( String callee ) {
    contentCallStack.remove( callee );
  }
}
