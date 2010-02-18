package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.lang.reflect.Constructor;

import org.pentaho.di.repository.IRole;
import org.pentaho.di.ui.repository.repositoryexplorer.UIObjectCreationException;

public class UIObjectRegistery {

  private static UIObjectRegistery instance;

  private Class<?> repositoryRoleClass;

  private UIObjectRegistery() {

  }

  public static UIObjectRegistery getInstance() {
    if (instance == null) {
      instance = new UIObjectRegistery();
    }
    return instance;
  }

  public void registerUIRepositoryRoleClass(Class<?> repositoryRoleClass) {
    this.repositoryRoleClass = repositoryRoleClass;
  }

  public Class<?> getRegisteredUIRepositoryRoleClass() {
    return this.repositoryRoleClass;
  }

  public IUIRole constructUIRepositoryRole(IRole role) throws UIObjectCreationException {
    try {
      Constructor<?> constructor = repositoryRoleClass.getConstructor(IRole.class);
      if (constructor != null) {
        return (IUIRole) constructor.newInstance(role);
      } else {
        throw new UIObjectCreationException("Unable to get the constructor for " + repositoryRoleClass);
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + repositoryRoleClass);
    }
  }

}
