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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class MainSpoonPerspective implements SpoonPerspective {

  public static final String ID = "001-spoon-jobs"; //$NON-NLS-1$
  
  private Composite ui;
  private List<SpoonPerspectiveListener> listeners = new ArrayList<SpoonPerspectiveListener>();
  private TabSet tabfolder;
  private static final Class<?> PKG = Spoon.class;
  
  public MainSpoonPerspective(Composite ui, TabSet tabfolder){
    this.ui = ui;
    this.tabfolder = tabfolder;
  }
  
  // Default perspective to support Jobs and Transformations
  public String getId() {
    return ID;
  }

  public String getDisplayName(Locale l) {
    return BaseMessages.getString(PKG, "Spoon.Perspectives.DI");
  }

  public InputStream getPerspectiveIcon() {
    File f = new File("ui/images/transformation.png");
    try {
      return new FileInputStream(f);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public Composite getUI() {
    return ui;
  }

  public void setActive(boolean active) {
    for(SpoonPerspectiveListener l : listeners){
      if(active){
        l.onActivation();
        Spoon.getInstance().enableMenus();
      } else {
        l.onDeactication();
      }
    }
  }

  public List<XulEventHandler> getEventHandlers() {
    return null;
  }

  public List<XulOverlay> getOverlays() {
    return Collections.singletonList((XulOverlay) new DefaultXulOverlay("ui/main_perspective_overlay.xul"));
  }

  public void addPerspectiveListener(SpoonPerspectiveListener listener) {
    if(listeners.contains(listener) == false){
      listeners.add(listener);
    }
  }
  public EngineMetaInterface getActiveMeta() {

    if (tabfolder == null)
      return null;
    
    TabItem tabItem = tabfolder.getSelected();
    if (tabItem == null)
      return null;

    // What transformation is in the active tab?
    // TransLog, TransGraph & TransHist contain the same transformation
    //
    TabMapEntry mapEntry = ((Spoon) SpoonFactory.getInstance()).delegates.tabs.getTab(tabfolder.getSelected());
    EngineMetaInterface meta = null;
    if (mapEntry != null) {
      if (mapEntry.getObject() instanceof TransGraph)
        meta = (mapEntry.getObject()).getMeta();
      if (mapEntry.getObject() instanceof JobGraph)
        meta = (mapEntry.getObject()).getMeta();
    }

    return meta;
    
  }
  
  void setTabset(TabSet tabs){
    this.tabfolder = tabs;
  }
  
}
