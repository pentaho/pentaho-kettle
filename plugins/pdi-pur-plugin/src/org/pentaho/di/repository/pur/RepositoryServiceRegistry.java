/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository.pur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.repository.IRepositoryService;

public class RepositoryServiceRegistry {
  private final Map<Class<? extends IRepositoryService>, IRepositoryService> serviceMap;
  private final List<Class<? extends IRepositoryService>> serviceList;

  public RepositoryServiceRegistry() {
    serviceMap = new HashMap<Class<? extends IRepositoryService>, IRepositoryService>();
    serviceList = new ArrayList<Class<? extends IRepositoryService>>();
  }

  public void registerService( Class<? extends IRepositoryService> clazz, IRepositoryService service ) {
    if ( serviceMap.put( clazz, service ) == null ) {
      serviceList.add( clazz );
    }
  }

  public <T extends IRepositoryService> T getService( Class<T> clazz ) {
    return clazz.cast( serviceMap.get( clazz ) );
  }

  public List<Class<? extends IRepositoryService>> getRegisteredInterfaces() {
    return serviceList;
  }
}
