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
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class NoneMethodProcessor extends AbstractMethodProcessor {
  @Override
  public void schemaSelection( PartitionSettings settings, Shell shell, SpoonDelegates delegates ) {
  }
}
