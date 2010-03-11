package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;

/**
 * Common functionality expected of all browse controller implementations.
 * 
 * @author mlowery
 */
public interface IBrowseController {

  void addContextChangeVetoer(ContextChangeVetoer listener);

  void removeContextChangeVetoer(ContextChangeVetoer listener);

}
