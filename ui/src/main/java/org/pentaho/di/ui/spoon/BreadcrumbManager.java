/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
