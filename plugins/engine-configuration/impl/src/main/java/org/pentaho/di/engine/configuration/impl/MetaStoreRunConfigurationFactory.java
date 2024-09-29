/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationFactory;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.persist.MetaStoreFactory;

import java.util.Collections;
import java.util.List;

import static org.pentaho.metastore.util.PentahoDefaults.NAMESPACE;

/**
 * Created by bmorrise on 3/17/17.
 */
public abstract class MetaStoreRunConfigurationFactory implements RunConfigurationFactory {

  protected CheckedMetaStoreSupplier metastoreSupplier;

  public MetaStoreRunConfigurationFactory( CheckedMetaStoreSupplier metastoreSupplier ) {
    this.metastoreSupplier = metastoreSupplier;
  }

  private <T extends RunConfiguration> MetaStoreFactory<T> getMetastoreFactory( Class<T> clazz,
                                                                                  IMetaStore metaStore ) {
    return new MetaStoreFactory<>( clazz, metaStore, NAMESPACE );
  }

  protected <T extends RunConfiguration> MetaStoreFactory<T> getMetastoreFactory( Class<T> clazz )
    throws MetaStoreException {
    return getMetastoreFactory( clazz, metastoreSupplier.get() );
  }

  protected abstract <T extends RunConfiguration> MetaStoreFactory<T> getMetaStoreFactory() throws MetaStoreException;

  @Override
  public boolean delete( String name ) {
    try {
      getMetaStoreFactory().deleteElement( name );
    } catch ( MetaStoreException me ) {
      return false;
    }

    return true;
  }

  @Override
  public void deleteAll() {
    try {
      List<String> elementNames = getMetaStoreFactory().getElementNames();
      for ( String name : elementNames ) {
        getMetaStoreFactory().deleteElement( name );
      }
    } catch ( MetaStoreException me ) {
      // Ignore
    }

  }

  @Override
  public List<RunConfiguration> load() {
    try {
      return getMetaStoreFactory().getElements();
    } catch ( MetaStoreException me ) {
      return Collections.emptyList();
    }
  }


  @Override
  public RunConfiguration load( String name ) {
    try {
      return getMetaStoreFactory().loadElement( name );
    } catch ( MetaStoreException me ) {
      return null;
    }
  }

  @Override
  public boolean save( RunConfiguration runConfiguration ) {
    try {
      getMetaStoreFactory().saveElement( runConfiguration );
    } catch ( MetaStoreException me ) {
      return false;
    }

    return true;
  }

  @Override
  public List<String> getNames() {
    try {
      return getMetaStoreFactory().getElementNames();
    } catch ( MetaStoreException me ) {
      return Collections.emptyList();
    }
  }

  public void setMetastoreSupplier( CheckedMetaStoreSupplier metastoreSupplier ) {
    this.metastoreSupplier = metastoreSupplier;
  }
}
