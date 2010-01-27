package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.ArrayList;

import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeListener.TYPE;

public class ContextChangeListenerCollection extends ArrayList<ContextChangeListener> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Fires a context change event to all listeners.
   * 
   */
  public TYPE fireContextChange() {
    for (ContextChangeListener listener : this) {
      return listener.onContextChange();
    }
    return TYPE.NO_OP;
  }
}
