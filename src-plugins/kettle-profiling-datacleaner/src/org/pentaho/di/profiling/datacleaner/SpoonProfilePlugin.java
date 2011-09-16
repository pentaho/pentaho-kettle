package org.pentaho.di.profiling.datacleaner;

import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

@SpoonPlugin(id = "SpoonDataCleaner", image = "")
@SpoonPluginCategories({"spoon", "trans-graph", "database_dialog"})
public class SpoonProfilePlugin implements SpoonPluginInterface {

  @Override
  public void applyToContainer(String category, XulDomContainer container) throws XulException {
    container.registerClassLoader(getClass().getClassLoader());
    
    if(category.equals("spoon")){
      container.loadOverlay("org/pentaho/di/profiling/datacleaner/spoon_overlay.xul");
      container.addEventHandler(ModelerHelper.getInstance());
    } else if(category.equals("trans-graph")){
      container.loadOverlay("org/pentaho/di/profiling/datacleaner/trans_overlay.xul");
      container.addEventHandler(ModelerHelper.getInstance());
    } else if(category.equals("database_dialog")){
      container.loadOverlay("org/pentaho/di/profiling/datacleaner/database_dialog_overlay.xul");
      container.addEventHandler(new ProfilerDatabaseExplorerController());
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }
  
}
