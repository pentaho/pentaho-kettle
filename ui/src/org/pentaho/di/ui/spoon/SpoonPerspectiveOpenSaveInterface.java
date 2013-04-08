package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;

/**
 * The spoon perspective implementing this interface implements its own open/save dialogs and logic.
 * 
 * @author matt
 */
public interface SpoonPerspectiveOpenSaveInterface {
  /**
   * Open a file/object
   */
  public void open(boolean importFile);
  
  /**
   * Save the specified file/object
   * @param meta The object to be saved.
   * @return true if the object was saved 
   */
  public boolean save(EngineMetaInterface meta);
}
