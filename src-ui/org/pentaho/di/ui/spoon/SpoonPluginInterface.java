package org.pentaho.di.ui.spoon;

import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

public interface SpoonPluginInterface {
  
  
  public void applyToContainer(String category, XulDomContainer container) throws XulException ;
  
  /**
   * Provides an optional SpoonLifecycleListener to be notified of Spoon startup and shutdown.
   * 
   * @return optional SpoonLifecycleListener
   */
  public SpoonLifecycleListener getLifecycleListener();
  
  /**
   * Provides an optional SpoonPerspective.
   * 
   * @return optional SpoonPerspective
   */
  public SpoonPerspective getPerspective();
}
