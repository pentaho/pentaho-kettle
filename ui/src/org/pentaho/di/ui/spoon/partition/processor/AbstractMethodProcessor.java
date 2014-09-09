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
      settings.updateSchema( settings.getTransMeta().getPartitionSchemas().get( idx ) );
    } else {
      settings.rollback( settings.getBefore() );
    }
  }


}
