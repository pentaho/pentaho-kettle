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
