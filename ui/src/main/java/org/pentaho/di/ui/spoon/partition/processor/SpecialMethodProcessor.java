/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.spoon.partition.processor;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class SpecialMethodProcessor extends AbstractMethodProcessor {


  @Override
  public void schemaSelection( PartitionSettings settings, Shell shell, SpoonDelegates delegates )
    throws KettleException {
    String schema =
      super.askForSchema( settings.getSchemaNamesArray(), shell, settings.getDefaultSelectedSchemaIndex() );
    super.processForKnownSchema( schema, settings );
    if ( !StringUtil.isEmpty( schema ) ) {
      askForField( settings, delegates );
    }
  }

  private void askForField( PartitionSettings settings, SpoonDelegates delegates ) throws KettleException {
    StepDialogInterface partitionDialog =
      delegates.steps.getPartitionerDialog( settings.getStepMeta(), settings.getStepMeta().getStepPartitioningMeta(),
        settings.getTransMeta() );
    String  fieldName = partitionDialog.open();
    if ( StringUtil.isEmpty( fieldName ) ) {
      settings.rollback( settings.getBefore() );
    }
  }


}
