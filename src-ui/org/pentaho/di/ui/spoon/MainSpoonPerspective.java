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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class MainSpoonPerspective implements SpoonPerspective {

  private Composite ui;
  private List<SpoonPerspectiveListener> listeners = new ArrayList<SpoonPerspectiveListener>();
  private TabSet tabfolder;
  
  public MainSpoonPerspective(Composite ui, TabSet tabfolder){
    this.ui = ui;
    this.tabfolder = tabfolder;
  }
  
  // Default perspective to support Jobs and Transformations
  public String getId() {
    return "spoon-jobs";
  }

  public String getDisplayName(Locale l) {
    return "Jobs/Transformations";
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
  
}
