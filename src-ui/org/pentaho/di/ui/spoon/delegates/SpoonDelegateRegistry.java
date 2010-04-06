package org.pentaho.di.ui.spoon.delegates;

import java.lang.reflect.Constructor;

import org.pentaho.di.ui.spoon.InstanceCreationException;
import org.pentaho.di.ui.spoon.Spoon;


public class SpoonDelegateRegistry {

  public static final Class<?> DEFAULT_SPOONJOBDELEGATE_CLASS = SpoonJobDelegate.class;
  public static final Class<?> DEFAULT_SPOONTRANSDELEGATE_CLASS = SpoonTransformationDelegate.class;
  private static SpoonDelegateRegistry instance;
  private Class<?> jobDelegateClass;
  private Class<?> transDelegateClass;
  private SpoonDelegateRegistry() {

  }

  public static SpoonDelegateRegistry getInstance() {
    if (instance == null) {
      instance = new SpoonDelegateRegistry();
    }
    return instance;
  }

  public void registerSpoonJobDelegateClass(Class<?> jobDelegateClass) {
    this.jobDelegateClass = jobDelegateClass;
  }
  public void registerSpoonTransDelegateClass(Class<?> transDelegateClass) {
    this.transDelegateClass = transDelegateClass;
  }
  public Class<?> getRegisteredSpoonJobDelegateClass() {
    return this.jobDelegateClass;
  }
  public Class<?> getRegisteredSpoonTransDelegateClass() {
    return this.transDelegateClass;
  }
  public SpoonDelegate constructSpoonJobDelegate(Spoon spoon) throws InstanceCreationException {
    try {
      if(jobDelegateClass == null) {
        Constructor<?> constructor = DEFAULT_SPOONJOBDELEGATE_CLASS.getConstructor(Spoon.class);
        if (constructor != null) {
          return (SpoonDelegate) constructor.newInstance(spoon);
        } else {
          throw new InstanceCreationException("Unable to get the constructor for " + jobDelegateClass);
        }
      }
      Constructor<?> constructor = jobDelegateClass.getConstructor(Spoon.class);
      if (constructor != null) {
        return (SpoonDelegate) constructor.newInstance(spoon);
      } else {
        throw new InstanceCreationException("Unable to get the constructor for " + jobDelegateClass);
      }
    } catch (Exception e) {
      throw new InstanceCreationException("Unable to instantiate object for " + jobDelegateClass);
    }
  }
  
  public SpoonDelegate constructSpoonTransDelegate(Spoon spoon) throws InstanceCreationException {
    try {
      if(transDelegateClass == null) {
        Constructor<?> constructor = DEFAULT_SPOONTRANSDELEGATE_CLASS.getConstructor(Spoon.class);
        if (constructor != null) {
          return (SpoonDelegate) constructor.newInstance(spoon);
        } else {
          throw new InstanceCreationException("Unable to get the constructor for " + transDelegateClass);
        }
      }
      Constructor<?> constructor = transDelegateClass.getConstructor(Spoon.class);
      if (constructor != null) {
        return (SpoonDelegate) constructor.newInstance(spoon);
      } else {
        throw new InstanceCreationException("Unable to get the constructor for " + transDelegateClass);
      }
    } catch (Exception e) {
      throw new InstanceCreationException("Unable to instantiate object for " + transDelegateClass);
    }
  }
}
