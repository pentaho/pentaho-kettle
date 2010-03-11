package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;

public class ContextChangeVetoerCollection extends ArrayList<ContextChangeVetoer> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Fires a context change event to all listeners.
   * 
   */
  public List<TYPE> fireContextChange() {
    List<TYPE> returnValue = new ArrayList<TYPE>();
    for (ContextChangeVetoer listener : this) {
      returnValue.add(listener.onContextChange());
    }
    return returnValue;
  }
}
