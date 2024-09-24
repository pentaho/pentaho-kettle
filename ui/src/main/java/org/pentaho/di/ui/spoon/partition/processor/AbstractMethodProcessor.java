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

package org.pentaho.di.ui.spoon.partition.processor;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public abstract class AbstractMethodProcessor implements MethodProcessor {

  public String askForSchema( String[] schemaNames, Shell shell, int defaultSelectedSchemaIndex ) {
    EnterSelectionDialog askSchema =
      new EnterSelectionDialog(
        shell, schemaNames, "Select a partition schema", "Select the partition schema to use:" );
    return askSchema.open( defaultSelectedSchemaIndex );

  }

  public void processForKnownSchema( String schemaName, PartitionSettings settings ) throws KettlePluginException {
    if ( schemaName != null ) {
      int idx = Const.indexOfString( schemaName, settings.getSchemaNames() );
      settings.updateSchema( settings.getSchemas().get( idx ) );
    } else {
      settings.rollback( settings.getBefore() );
    }
  }


}
