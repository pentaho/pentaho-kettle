package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.ResourceBundle;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.ui.xul.binding.BindingFactory;



public interface ISecurityController {

  public void setBindingFactory(BindingFactory bf);
  public BindingFactory getBindingFactory();
  public void setRepository(Repository repository);
  public Repository getRepository();
  public void setRepositorySecurityManager(RepositorySecurityManager rsm);
  public RepositorySecurityManager getRepositorySecurityManager();
  public void setMessages(ResourceBundle resourceBundle);
  public ResourceBundle getMessages();

}
