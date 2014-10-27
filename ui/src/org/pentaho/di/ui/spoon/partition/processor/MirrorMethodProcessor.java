package org.pentaho.di.ui.spoon.partition.processor;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.partition.PartitionSettings;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class MirrorMethodProcessor extends AbstractMethodProcessor {

  @Override
  public void schemaSelection( PartitionSettings settings, Shell shell, SpoonDelegates delegates )
    throws KettlePluginException {
    String schema = super.askForSchema( settings.getSchemaNames(), shell, settings.getDefaultSelectedSchemaIndex() );
    super.processForKnownSchema( schema, settings );
  }
}
