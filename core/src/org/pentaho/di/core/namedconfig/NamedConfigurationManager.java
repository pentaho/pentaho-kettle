/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.util.PentahoDefaults;

public class NamedConfigurationManager implements INamedConfigurationManager {

  private static NamedConfigurationManager instance = new NamedConfigurationManager();
  
  private List<NamedConfiguration> configurationTemplates;
  private String activeShimClass;
  private Map<IMetaStore, MetaStoreFactory<NamedConfiguration>> factoryMap = new HashMap<IMetaStore, MetaStoreFactory<NamedConfiguration>>();
  
  private NamedConfigurationManager() {
    configurationTemplates = new ArrayList<NamedConfiguration>();
  }

  public static NamedConfigurationManager getInstance() {
    return instance;
  }

  private MetaStoreFactory<NamedConfiguration> getMetaStoreFactory( IMetaStore metastore ) {
    if ( factoryMap.get( metastore ) == null ) {
     factoryMap.put( metastore, new MetaStoreFactory<NamedConfiguration>( NamedConfiguration.class, metastore, PentahoDefaults.NAMESPACE ) );
    }
    return factoryMap.get( metastore );
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#getActiveShimClass()
   */
  @Override
  public String getActiveShimClass() {
    return activeShimClass;
  }

  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#setActiveShimClass(java.lang.String)
   */
  @Override
  public void setActiveShimClass(String activeShimClass) {
    this.activeShimClass = activeShimClass;
  }  
  
  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#getConfigurationTemplates()
   */
  @Override
  public List<NamedConfiguration> getConfigurationTemplates() {
    ArrayList<NamedConfiguration> configs = new ArrayList<NamedConfiguration>();
    for ( NamedConfiguration configuration : configurationTemplates ) {
      configs.add( (NamedConfiguration) configuration.clone() );
    }
    return Collections.unmodifiableList( configs );
  }

  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#getConfigurationTemplates(java.lang.String)
   */
  @Override
  public List<NamedConfiguration> getConfigurationTemplates( String type ) {
    ArrayList<NamedConfiguration> matches = new ArrayList<NamedConfiguration>();
    for ( NamedConfiguration configuration : configurationTemplates ) {
      if ( configuration.getType().equals( type ) ) {
        matches.add( (NamedConfiguration) configuration.clone() );
      }
    }
    return Collections.unmodifiableList( matches );
  }
  
  
  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#getConfigurationTemplate(java.lang.String)
   */
  @Override
  public NamedConfiguration getConfigurationTemplate( String name ) {
    for ( NamedConfiguration configuration : configurationTemplates ) {
      if ( configuration.getName().equals( name ) ) {
        return (NamedConfiguration) configuration.clone();
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.di.core.namedconfig.IConfigurationTemplateManager#addConfigurationTemplate(org.pentaho.di.core.namedconfig.model.NamedConfiguration)
   */
  @Override
  public void addConfigurationTemplate( NamedConfiguration configuration ) {
    configurationTemplates.add( configuration );
  }
 
  
  @Override
  public void create( NamedConfiguration configuration, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    factory.saveElement( configuration );
  }
  
  @Override
  public NamedConfiguration read( String configurationName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    return factory.loadElement( configurationName );
  }

  @Override
  public void update( NamedConfiguration configuration, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    factory.deleteElement( configuration.getName() );
    factory.saveElement( configuration );
  }  

  @Override
  public void delete( String configurationName, IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    factory.deleteElement( configurationName );
  }  
  
  @Override
  public List<NamedConfiguration> list( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    return factory.getElements();
  }  
  
  @Override
  public List<String> listNames( IMetaStore metastore ) throws MetaStoreException {
    MetaStoreFactory<NamedConfiguration> factory = getMetaStoreFactory( metastore );
    return factory.getElementNames();
  }   
  
  @Override
  public boolean contains( String configurationName, IMetaStore metastore ) throws MetaStoreException {
    if ( metastore == null ) {
      return false;
    }
    for ( String name : listNames( metastore ) ) {
      if ( name.equals(configurationName ) ) {
        return true;
      }
    }
    return false;
  }
  
}