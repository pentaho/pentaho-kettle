/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.namedconfig;

import java.util.List;

import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public interface INamedConfigurationManager {

  public String getActiveShimClass();

  public void setActiveShimClass(String activeShimClass);

  public List<NamedConfiguration> getConfigurationTemplates();

  public List<NamedConfiguration> getConfigurationTemplates(String type);

  /**
   * This method will return a copy (clone) of the requested configuration
   * 
   * @param name the template to return
   * @return the NamedConfiguration that was found, otherwise null
   */
  public NamedConfiguration getConfigurationTemplate(String name);

  public void addConfigurationTemplate(NamedConfiguration configuration);

  public void create( NamedConfiguration configuration, IMetaStore metastore ) throws MetaStoreException;
  public NamedConfiguration read( String configurationName, IMetaStore metastore ) throws MetaStoreException;
  public void update( NamedConfiguration configuration, IMetaStore metastore ) throws MetaStoreException;
  public void delete( String configurationName, IMetaStore metastore ) throws MetaStoreException;
  public List<NamedConfiguration> list( IMetaStore metastore ) throws MetaStoreException;
  public List<String> listNames( IMetaStore metastore ) throws MetaStoreException;
  public boolean contains( String configurationName, IMetaStore metastore ) throws MetaStoreException;
}