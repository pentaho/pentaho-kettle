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

package org.pentaho.di.starmodeler;

import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPerspectiveManager;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;

public class ModelerHelper  extends AbstractXulEventHandler implements ISpoonMenuController {
  protected static Class<?> PKG = ModelerHelper.class; // for i18n

  public static final String MODELER_NAME = "Modeler";

  private static ModelerHelper instance = null;

  private String defaultLocale = LanguageChoice.getInstance().getDefaultLocale().toString();

  private ModelerHelper() {
  }

  public static ModelerHelper getInstance() {
    if( instance == null ) {
      instance = new ModelerHelper();
      Spoon spoon = ((Spoon)SpoonFactory.getInstance());
      spoon.addSpoonMenuController(instance);
    }
    return instance;
  }


  protected String getUniqueUntitledTabName(Spoon spoon, String title) {
    int num = 1;
    String tabName = title + " " + num;
    // TODO: Add new plugin object type to spoon
    TabItem tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    while (tabItem != null) {
      tabName = title + " " + (++num);
      // TODO: Add new plugin object type to spoon
      tabItem = spoon.delegates.tabs.findTabMapEntry(tabName, TabMapEntry.ObjectType.BROWSER).getTabItem();
    }
    return tabName;
  }

  public String getName(){
    return "starModeler";
  }

  public void createEmptyModel() {
    try {
      StarDomain starDomain = new StarDomain();
      starDomain.getDomain().setName(new LocalizedString(defaultLocale, "Star model domain"));
      starDomain.getDomain().setDescription(new LocalizedString(defaultLocale, "This star model domain contains multiple star models for the same subject domain"));
      StarModelerPerspective.getInstance().createTabForDomain(starDomain);
      SpoonPerspectiveManager.getInstance().activatePerspective(StarModelerPerspective.class);
    } catch (Exception e) {
      new ErrorDialog(((Spoon) SpoonFactory.getInstance()).getShell(), "Error", "Error creating visualization", e);
    }
  }

  public void updateMenu(Document doc) {
    // Nothing so far.
  }
}
