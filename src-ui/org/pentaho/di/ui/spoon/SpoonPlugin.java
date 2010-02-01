package org.pentaho.di.ui.spoon;

import java.util.List;
import java.util.Map;

import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * Xul-based Spoon plugin. Implementations can modify the look of Spoon, register a 
 * SpoonLifecycleListener and add a SpoonPerspective.
 * 
 * @author nbaker
 *
 */
public interface SpoonPlugin {
  
  /**
   * Map of XulOverlays that will be applied on Spoon startup. These overlays can modify the look 
   * of the main UI as well as any Xul-based dialog registered in Spoon.
   * 
   * @return Map of XulOverlays
   */
  public Map<String, XulOverlay>  getOverlays();
  
  /**
   * Map of XulEventHandlers to be registered at Spoon startup. These Event Handlers can support UI
   * added via the Overlays or replace default registered Event Handlers
   *  
   * @return Map of XulEventHandlers
   */
  public Map<String, XulEventHandler>  getEventHandlers();
  
  /**
   * Returns an optional SpoonLifecycleListener to be notified of Spoon startup and shutdown.
   * 
   * @return optional SpoonLifecycleListener
   */
  public SpoonLifecycleListener getLifecycleListener();
  
  /**
   * Returns an optional SpoonPerspective.
   * 
   * @return optional SpoonPerspective
   */
  public SpoonPerspective getPerspective();
}
