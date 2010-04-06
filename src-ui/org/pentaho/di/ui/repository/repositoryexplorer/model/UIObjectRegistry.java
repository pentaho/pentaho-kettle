package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.lang.reflect.Constructor;

import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;

public class UIObjectRegistry {

  public static final Class<?> DEFAULT_UIREPOSITORYUSER_CLASS = UIRepositoryUser.class;
  public static final Class<?> DEFAULT_UIJOB_CLASS = UIJob.class;
  public static final Class<?> DEFAULT_UITRANS_CLASS = UITransformation.class;
  public static final Class<?> DEFAULT_UIDIR_CLASS = UIRepositoryDirectory.class;
  
  private static UIObjectRegistry instance;
  
  private Class<?> repositoryUserClass;
  private Class<?> jobClass;
  private Class<?> transClass;
  private Class<?> dirClass;


  private UIObjectRegistry() {

  }

  public static UIObjectRegistry getInstance() {
    if (instance == null) {
      instance = new UIObjectRegistry();
    }
    return instance;
  }

  public void registerUIRepositoryUserClass(Class<?> repositoryUserClass) {
    this.repositoryUserClass = repositoryUserClass;
  }

  public Class<?> getRegisteredUIRepositoryUserClass() {
    return this.repositoryUserClass;
  }

  public void registerUIJobClass(Class<?> jobClass) {
    this.jobClass = jobClass;
  }

  public Class<?> getRegisteredUIJobClass() {
    return this.jobClass;
  }

  public void registerUITransformationClass(Class<?> transClass) {
    this.transClass = transClass;
  }

  public Class<?> getRegisteredUITransformationClass() {
    return this.transClass;
  }
  public void registerUIRepositoryDirectoryClass(Class<?> dirClass) {
    this.dirClass = dirClass;
  }

  public Class<?> getRegisteredUIRepositoryDirectoryClass() {
    return this.dirClass;
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
  
  public UIJob constructUIJob(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) throws UIObjectCreationException {
    try {
      if(jobClass == null) {
        Constructor<?> constructor = DEFAULT_UIJOB_CLASS.getConstructor(RepositoryContent.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UIJob) constructor.newInstance(rc, parent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + jobClass);
        }
      
      } else {
        Constructor<?> constructor = jobClass.getConstructor(RepositoryContent.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UIJob) constructor.newInstance(rc, parent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + jobClass);
        }
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + jobClass);
    }
  }
  
  public UITransformation constructUITransformation(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) throws UIObjectCreationException {
    try {
      if(transClass == null) {
        Constructor<?> constructor = DEFAULT_UITRANS_CLASS.getConstructor(RepositoryContent.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UITransformation) constructor.newInstance(rc, parent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + transClass);
        }
      
      } else {
        Constructor<?> constructor = transClass.getConstructor(RepositoryContent.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UITransformation) constructor.newInstance(rc, parent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + transClass);
        }
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + transClass);
    }
  }
  
  public UIRepositoryDirectory constructUIRepositoryDirectory(Directory rd, Repository rep) throws UIObjectCreationException {
    try {
      if(dirClass == null) {
        Constructor<?> constructor = DEFAULT_UIDIR_CLASS.getConstructor(Directory.class, Repository.class);
        if (constructor != null) {
          return (UIRepositoryDirectory) constructor.newInstance(rd, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + dirClass);
        }
      
      } else {
        Constructor<?> constructor = dirClass.getConstructor(Directory.class, Repository.class);
        if (constructor != null) {
          return (UIRepositoryDirectory) constructor.newInstance(rd, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + dirClass);
        }
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + dirClass);
    }
  }

  public UIRepositoryDirectory constructUIRepositoryDirectory(Directory rd, UIRepositoryDirectory uiParent, Repository rep) throws UIObjectCreationException {
    try {
      if(dirClass == null) {
        Constructor<?> constructor = DEFAULT_UIDIR_CLASS.getConstructor(Directory.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UIRepositoryDirectory) constructor.newInstance(rd, uiParent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + dirClass);
        }
      
      } else {
        Constructor<?> constructor = dirClass.getConstructor(Directory.class, UIRepositoryDirectory.class, Repository.class);
        if (constructor != null) {
          return (UIRepositoryDirectory) constructor.newInstance(rd, uiParent, rep);
        } else {
          throw new UIObjectCreationException("Unable to get the constructor for " + dirClass);
        }
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + dirClass);
    }
  }
}
