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
