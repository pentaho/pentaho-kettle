/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
