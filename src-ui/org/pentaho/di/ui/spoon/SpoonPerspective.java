package org.pentaho.di.ui.spoon;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * A SpoonPerspective is able to modify the look of the application and display it's own
 * UI. Only one perspective can be active at a time though they can run concurrently.
 * SpoonPerspectives are most likely to be registered as part of a SpoonPlugin.
 * 
 * @author nbaker
 */
public interface SpoonPerspective {
  /**
   * Returns a unique identifier for this perspective
   * @return unique ID
   */
  public String getId();
  /**
   * Returns the main UI for the perspective. 
   * @return UI Composite
   */
  public Composite getUI();
  
  /**
   * Returns a localized name for the perspective
   * @param l current Locale
   * @return localized name
   */
  public String getDisplayName(Locale l);
  
  /**
   * Perspectives will be represented in spoon by an icon on the main toolbar. This method returns
   * the InputStream for that icon.
   * @return icon InputStream
   */
  public InputStream getPerspectiveIcon();
  
  /**
   * Called by Spoon whenever the active state of a perspective changes.
   * @param active
   */
  public void setActive(boolean active);
  
  /**
   * A list of Xul Overlays to be applied and removed when the perspective is loaded or unloaded
   * @return List of XulOverlays.
   */
  public List<XulOverlay> getOverlays();
  
  /**
   * Returns a list of Xul Event Handlers (controllers) to be added to Xul Containers in Spoon.
   * Perspectives may overwrite existing event handlers by registering one with the same ID. 
   * @return list of XulEventHandlers
   */
  public List<XulEventHandler> getEventHandlers();
  
  /**
   * Allows outside code to register to for activation events for this perspective.
   * 
   * @param listener
   */
  public void addPerspectiveListener(SpoonPerspectiveListener listener);
  
  /**
   * Return the active EngineMeta in the case of perspectives with save-able content.
   * 
   * @return active EngineMetaInterface
   */
  public EngineMetaInterface getActiveMeta();
}
