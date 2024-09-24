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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public interface MethodProcessor {
  void schemaSelection( PartitionSettings settings, Shell shell, SpoonDelegates delegates ) throws KettleException;
}
