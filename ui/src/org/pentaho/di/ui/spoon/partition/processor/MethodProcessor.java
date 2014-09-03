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
