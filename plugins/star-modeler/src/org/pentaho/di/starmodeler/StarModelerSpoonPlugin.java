/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.starmodeler;

import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@SpoonPlugin(id = "StarModeler", image = "")
@SpoonPluginCategories({"spoon"})
public class StarModelerSpoonPlugin implements SpoonPluginInterface {

  public StarModelerSpoonPlugin() {
  }

  public void applyToContainer(String category, XulDomContainer container) throws XulException {
    container.registerClassLoader(getClass().getClassLoader());
    if(category.equals("spoon")){
      container.loadOverlay("org/pentaho/di/starmodeler/xul/spoon_overlays.xul");
      container.addEventHandler(ModelerHelper.getInstance());
    }
  }

  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  public SpoonPerspective getPerspective() {
    return StarModelerPerspective.getInstance();
  }


}
