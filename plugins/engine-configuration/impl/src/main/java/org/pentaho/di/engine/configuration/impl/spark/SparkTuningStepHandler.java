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

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PropertiesComboDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import java.util.List;
import java.util.Map;

public class SparkTuningStepHandler extends AbstractXulEventHandler {
  public static final String SPARK_TUNING_PROPERTIES = "sparkTuningParameters";

  private static final Class<?> PKG = SparkTuningStepHandler.class;
  private static final String HANDLER_NAME = "sparkTuningStepHandler";

  @Override
  public String getName() {
    return HANDLER_NAME;
  }

  // Catching throwable in this case because the Help Browser may not be available and
  // if not catch here XUL will swallow the exception and produce no error to the user.
  @SuppressWarnings( "squid:S1181" )
  public void openSparkTuning() {
    TransGraph transGraph = Spoon.getInstance().getActiveTransGraph();
    StepMeta stepMeta = transGraph.getCurrentStep();
    String title = BaseMessages.getString( PKG, "TransGraph.Dialog.SparkTuning.Title" )
      + " - " + stepMeta.getName();

    List<String> tuningProperties = SparkTunableProperties.getProperties( stepMeta.getStepID() );

    PropertiesComboDialog dialog = new PropertiesComboDialog(
      transGraph.getParent().getShell(),
      transGraph.getTransMeta(),
      stepMeta.getAttributes( SPARK_TUNING_PROPERTIES ),
      title,
      Const.getDocUrl( BaseMessages.getString( PKG, "SparkTuning.Help.Url" ) ),
      BaseMessages.getString( PKG, "SparkTuning.Help.Title" ),
      BaseMessages.getString( PKG, "SparkTuning.Help.Header" )
    );
    dialog.setComboOptions( tuningProperties );
    try {
      Map<String, String> properties = dialog.open();

      // null means the cancel button was clicked otherwise ok was clicked
      if ( null != properties ) {
        stepMeta.setAttributes( SPARK_TUNING_PROPERTIES, properties );
        stepMeta.setChanged();
        transGraph.getSpoon().setShellText();
      }
    } catch ( Throwable e ) {
      new ErrorDialog(
        Spoon.getInstance().getShell(), BaseMessages.getString( PKG, "SparkTuning.UnexpectedError" ), BaseMessages
        .getString( PKG, "SparkTuning.UnexpectedError" ), e );
    }
  }
}
