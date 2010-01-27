package org.pentaho.di.ui.repository.repositoryexplorer;



public interface ContextChangeListener {

  public enum TYPE {OK, CANCEL, NO_OP};
  /**
   * Fired when the context is changed
   * 
   * 
   */
  TYPE onContextChange();
}