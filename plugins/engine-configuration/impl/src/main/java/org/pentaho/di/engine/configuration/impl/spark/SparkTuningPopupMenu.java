/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
 */
package org.pentaho.di.engine.configuration.impl.spark;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.util.Enumeration;
import java.util.ResourceBundle;

@SpoonPlugin( id = "SparkStepPropertiesPlugin", image = "" )
@SpoonPluginCategories( {"trans-graph"} )
public class SparkTuningPopupMenu implements SpoonPluginInterface {
  private static Class<?> PKG = SparkTuningPopupMenu.class;

  private final ResourceBundle resourceBundle;
  private SparkTuningStepHandler handler;

  public SparkTuningPopupMenu() {
    handler = new SparkTuningStepHandler();
    resourceBundle = new ResourceBundle() {
      @Override
      public Enumeration<String> getKeys() {
        return null;
      }

      @Override
      protected Object handleGetObject( String key ) {
        return BaseMessages.getString( PKG, key );
      }
    };
  }

  @Override public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    if ( category.equals( "trans-graph" ) ) {
      container.registerClassLoader( getClass().getClassLoader() );
      container.loadOverlay( "org/pentaho/di/engine/configuration/impl/spark/spark-tuning-menu.xul", resourceBundle );
      container.addEventHandler( this.handler );
    }
  }

  @Override public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  @Override public SpoonPerspective getPerspective() {
    return null;
  }
}
