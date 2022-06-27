/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.metastore.locator.api.impl;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.trans.Trans;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by tkafalas on 7/10/2017.
 */

@ExtensionPoint( id = "MetastoreLocatorMetaLoadExtensionPoint", extensionPointId = "TransformationMetaLoaded",
  description = "" )
public class MetastoreLocatorExtensionPoint implements ExtensionPointInterface {
  MetastoreLocatorOsgi metastoreLocatorOsgi;

  public MetastoreLocatorExtensionPoint() {
    try {
      Collection<MetastoreLocator> metastoreLocators = PluginServiceLoader.loadServices( MetastoreLocator.class );
      Optional<MetastoreLocator> metastoreLocatorOptional = metastoreLocators.stream().findFirst();
      if ( !metastoreLocatorOptional.isPresent() ) {
        throw new KettlePluginException( "No metastore locator found" );
      }
      metastoreLocatorOsgi = (MetastoreLocatorOsgi) metastoreLocatorOptional.get();
    } catch ( KettlePluginException e ) {
      LogChannel.GENERAL.logError( "Error getting metastore locator", e );
    }
  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    AbstractMeta meta;
    if ( object instanceof Trans ) {
      meta = ( (Trans) object ).getTransMeta();
    } else if ( object instanceof JobExecutionExtension ) {
      meta = ( (JobExecutionExtension) object ).job.getJobMeta();
    } else {
      meta = (AbstractMeta) object;
    }
    if ( meta.getMetastoreLocatorOsgi() == null ) {
      meta.setMetastoreLocatorOsgi( metastoreLocatorOsgi );
    }
  }
}
