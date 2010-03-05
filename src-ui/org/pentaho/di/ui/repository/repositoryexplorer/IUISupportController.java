package org.pentaho.di.ui.repository.repositoryexplorer;

import org.pentaho.di.repository.Repository;


public interface IUISupportController {

  void init(Repository rep) throws ControllerInitializationException;
  String getName();
}
