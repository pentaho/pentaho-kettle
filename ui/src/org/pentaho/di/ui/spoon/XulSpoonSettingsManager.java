/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import java.io.IOException;

import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.ui.xul.XulSettingsManager;

public class XulSpoonSettingsManager implements XulSettingsManager {
  private static XulSpoonSettingsManager instance = new XulSpoonSettingsManager();
  
  public String getSetting(String prop) {
    return PropsUI.getInstance().getCustomParameter(prop, null);
  }

  public void save() throws IOException {
    PropsUI.getInstance().saveProps();

  }

  public void storeSetting(String prop, String val) {
    PropsUI.getInstance().setCustomParameter(prop, val);

  }
  
  public static XulSpoonSettingsManager getInstance(){
    return instance;
  }
}