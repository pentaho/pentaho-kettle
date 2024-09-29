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

package org.pentaho.di.trans.steps.metainject;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;

/**
 * Created by bmorrise on 7/26/16.
 */
@ExtensionPoint(
  id = "OpenMapping",
  description = "Update Trans Meta based on MetaInjectMeta being the step type",
  extensionPointId = "OpenMapping" )
public class OpenMappingExtension implements ExtensionPointInterface {

  //spoon class without import of swt libraries, big-data pmr run doesn't have ui library and shouldn't
  private static Class<?> PKG = SpoonLifecycleListener.class;

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    StepMeta stepMeta = (StepMeta) ( (Object[]) object )[ 0 ];
    TransMeta transMeta = (TransMeta) ( (Object[]) object )[ 1 ];

    if ( stepMeta.getStepMetaInterface() instanceof MetaInjectMeta ) {
      // Make sure we don't accidently overwrite this transformation so we'll remove the filename and objectId
      // Modify the name so the users sees it's a result
      transMeta.setFilename( null );
      transMeta.setObjectId( null );
      String appendName = " (" + BaseMessages.getString( PKG, "TransGraph.AfterInjection" ) + ")";
      if ( !transMeta.getName().endsWith( appendName ) ) {
        transMeta.setName( transMeta.getName() + appendName );
      }
    }
  }
}
