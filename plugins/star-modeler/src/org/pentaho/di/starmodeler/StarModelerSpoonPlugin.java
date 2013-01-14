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
