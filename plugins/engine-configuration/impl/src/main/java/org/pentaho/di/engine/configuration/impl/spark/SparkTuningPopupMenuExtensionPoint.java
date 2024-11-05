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
 *
 */
package org.pentaho.di.engine.configuration.impl.spark;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.trans.StepMenuExtension;
import org.pentaho.ui.xul.containers.XulMenupopup;

import java.util.List;

@ExtensionPoint( id = "SparkTuningStepMenuExtension", description = "Controls popup menus for Spark Tuning",
    extensionPointId = "TransStepRightClick" )
public class SparkTuningPopupMenuExtensionPoint implements ExtensionPointInterface {
  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    StepMenuExtension extension = (StepMenuExtension) object;
    StepMeta stepMeta = extension.getTransGraph().getCurrentStep();
    XulMenupopup menu = extension.getMenu();

    List<String> tunableProperties = SparkTunableProperties.getProperties( stepMeta.getStepID() );

    menu.getElementById( "trans-graph-entry-spark-tuning" ).setDisabled( tunableProperties.isEmpty() );
  }
}
