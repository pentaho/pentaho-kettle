/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.lang.reflect.Constructor;

import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;

public class UIObjectRegistry {

  public static final Class<?> DEFAULT_UIREPOSITORYUSER_CLASS = UIRepositoryUser.class;
  public static final Class<?> DEFAULT_UIJOB_CLASS = UIJob.class;
  public static final Class<?> DEFAULT_UITRANS_CLASS = UITransformation.class;
  public static final Class<?> DEFAULT_UIDIR_CLASS = UIRepositoryDirectory.class;
  
  private static UIObjectRegistry instance;
  
  private Class<?> repositoryUserClass = DEFAULT_UIREPOSITORYUSER_CLASS;
  private Class<?> jobClass = DEFAULT_UIJOB_CLASS;
  private Class<?> transClass = DEFAULT_UITRANS_CLASS;
  private Class<?> dirClass = DEFAULT_UIDIR_CLASS;


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
  
  public UIJob constructUIJob(RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep) throws UIObjectCreationException {
    try {
      Constructor<?> constructor = jobClass.getConstructor(RepositoryElementMetaInterface.class, UIRepositoryDirectory.class, Repository.class);
      if (constructor != null) {
        return (UIJob) constructor.newInstance(rc, parent, rep);
      } else {
        throw new UIObjectCreationException("Unable to get the constructor for " + jobClass);
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + jobClass);
    }
  }
  
  public UITransformation constructUITransformation(RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep) throws UIObjectCreationException {
    try {
      Constructor<?> constructor = transClass.getConstructor(RepositoryElementMetaInterface.class, UIRepositoryDirectory.class, Repository.class);
      if (constructor != null) {
        return (UITransformation) constructor.newInstance(rc, parent, rep);
      } else {
        throw new UIObjectCreationException("Unable to get the constructor for " + transClass);
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + transClass);
    }
  }

  public UIRepositoryDirectory constructUIRepositoryDirectory(RepositoryDirectoryInterface rd, UIRepositoryDirectory uiParent, Repository rep) throws UIObjectCreationException {
    try {
      Constructor<?> constructor = dirClass.getConstructor(RepositoryDirectoryInterface.class, UIRepositoryDirectory.class, Repository.class);
      if (constructor != null) {
        return (UIRepositoryDirectory) constructor.newInstance(rd, uiParent, rep);
      } else {
        throw new UIObjectCreationException("Unable to get the constructor for " + dirClass);
      }
    } catch (Exception e) {
      throw new UIObjectCreationException("Unable to instantiate object for " + dirClass);
    }
  }
}
