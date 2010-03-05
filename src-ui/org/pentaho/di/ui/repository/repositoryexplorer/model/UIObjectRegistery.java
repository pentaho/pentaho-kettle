package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.lang.reflect.Constructor;

import org.pentaho.di.repository.IUser;

public class UIObjectRegistery {

  public static final Class<?> DEFAULT_UIREPOSITORYUSER_CLASS = UIRepositoryUser.class;
  private static UIObjectRegistery instance;
  
  private Class<?> repositoryUserClass;

  private UIObjectRegistery() {

  }

  public static UIObjectRegistery getInstance() {
    if (instance == null) {
      instance = new UIObjectRegistery();
    }
    return instance;
  }

  public void registerUIRepositoryUserClass(Class<?> repositoryUserClass) {
    this.repositoryUserClass = repositoryUserClass;
  }

  public Class<?> getRegisteredUIRepositoryUserClass() {
    return this.repositoryUserClass;
  }

  public IUIUser constructUIRepositoryUser(IUser user) throws UIObjectCreationException {
    try {
      if(repositoryUserClass == null) {
        repositoryUserClass = DEFAULT_UIREPOSITORYUSER_CLASS;
      }
      Constructor<?> constructor = repositoryUserClass.getConstructor(IUser.class);
      if (constructor != null) {
        return (IUIUser) constructor.newInstance(user);
      } else {
        throw new UIObjectCreationException("Unable to get the constructor for " + repositoryUserClass);
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + repositoryUserClass);
    }
  }
}
