package org.pentaho.di.core.extension;

import org.pentaho.di.core.exception.KettleException;

public interface ExtensionPointInterface {
  
  /**
   * This method is called by the Kettle code
   * @param object The subject object that is passed to the plugin code
   * @throws KettleException In case the plugin decides that an error has occurred and the parent process should stop.
   */
  public void callExtensionPoint(Object object) throws KettleException;
}
