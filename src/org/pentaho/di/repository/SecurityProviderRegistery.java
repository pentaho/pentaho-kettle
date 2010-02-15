package org.pentaho.di.repository;

import java.lang.reflect.InvocationTargetException;

public class SecurityProviderRegistery {

  private Class<?> securityProviderClass;
  private static SecurityProviderRegistery instance;
  RepositorySecurityProvider securityProvider;
  private SecurityProviderRegistery() {
    
  }
  public static SecurityProviderRegistery getInstance() {
    if(instance == null) {
      instance = new SecurityProviderRegistery();
    }
    return instance;
  }
  public void registerSecurityProvider(Class<?> securityProviderClass) {
    this.securityProviderClass = securityProviderClass;
  }
  public Class<?> getRegisteredSecurityProvider() {
    return securityProviderClass;
  }
  
  public RepositorySecurityProvider createSecurityProvider(Repository repository, RepositoryMeta meta, UserInfo userInfo) {
    try {
      try {
        if(securityProvider == null) {
          securityProvider = (RepositorySecurityProvider) securityProviderClass
          .getConstructor(Repository.class, RepositoryMeta.class, UserInfo.class)
            .newInstance(repository, meta, userInfo);
        }
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (SecurityException e) {
      e.printStackTrace();
      return null;
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    return securityProvider;
  }
}
