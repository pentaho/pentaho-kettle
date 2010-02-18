package org.pentaho.di.repository;

import java.lang.reflect.Constructor;

public class SecurityManagerRegistery {

  private Class<?> securityManagerClass;
  private static SecurityManagerRegistery instance;
  RepositorySecurityManager securityManager;
  private SecurityManagerRegistery() {
    
  }
  public static SecurityManagerRegistery getInstance() {
    if(instance == null) {
      instance = new SecurityManagerRegistery();
    }
    return instance;
  }
  public void registerSecurityManager(Class<?> securityManagerClass) {
    this.securityManagerClass = securityManagerClass;
  }
  public Class<?> getRegisteredSecurityManager() {
    return securityManagerClass;
  }
  
  public RepositorySecurityManager createSecurityManager(Repository repository, RepositoryMeta meta, UserInfo userInfo) throws SecurityManagerCreationException {
    try {
        if(securityManager == null) {
          Constructor<?> constructor = securityManagerClass
          .getConstructor(Repository.class, RepositoryMeta.class, UserInfo.class);
          if(constructor != null) {
            securityManager = (RepositorySecurityManager) constructor.newInstance(repository, meta, userInfo);  
          } else {
            throw new SecurityManagerCreationException("Unable to get the constructor for " + securityManagerClass);
          }
        }
    } catch (Exception e) {
      throw new SecurityManagerCreationException(e);
    }
    return securityManager;
  }
}
