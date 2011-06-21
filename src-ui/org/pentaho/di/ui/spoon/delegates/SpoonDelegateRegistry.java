/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
      throw new InstanceCreationException("Unable to instantiate object for " + jobDelegateClass, e);
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
        throw new InstanceCreationException("Unable to instantiate object for " + transDelegateClass, e);
    }
  }
}
