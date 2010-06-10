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
package org.pentaho.di.ui.repository.repositoryexplorer;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.uisupport.IRepositoryExplorerUISupport;

public class UISupportRegistery {

  private static UISupportRegistery instance;
  
  private static Map<Class<? extends IRepositoryService>, Class<? extends IRepositoryExplorerUISupport>> uiSupportMap;

  private UISupportRegistery() {
    uiSupportMap = new HashMap<Class<? extends IRepositoryService>, Class<? extends IRepositoryExplorerUISupport>>();
  }

  public static UISupportRegistery getInstance() {
    if (instance == null) {
      instance = new UISupportRegistery();
    }
    return instance;
  }

  public void registerUISupport(Class<? extends IRepositoryService> service, Class<? extends IRepositoryExplorerUISupport> supportClass) {
    uiSupportMap.put(service, supportClass);
  }

  public IRepositoryExplorerUISupport createUISupport(Class<? extends IRepositoryService> service) throws UIObjectCreationException {
    Class<? extends IRepositoryExplorerUISupport> supportClass = uiSupportMap.get(service);
    if(supportClass != null) {
      return contruct(supportClass);  
    } else {
      return null;
    }
    
  }  
  
  private IRepositoryExplorerUISupport contruct(Class<? extends IRepositoryExplorerUISupport> supportClass) throws UIObjectCreationException {
    try {
      return  (IRepositoryExplorerUISupport) supportClass.newInstance();
    } catch (Throwable th) {
      throw new UIObjectCreationException(th);
      
    }
  }
}
