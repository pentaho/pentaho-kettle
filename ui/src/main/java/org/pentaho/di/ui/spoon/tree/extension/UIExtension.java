package org.pentaho.di.ui.spoon.tree.extension;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface to allow the Plugins to add UI extensions
 */
public interface UIExtension {

  /**
   * Plugins can use this method to build the UI elements in the Composite parameter
   *
   * @param main Composite
   */
  void buildExtension( Composite main );
}
