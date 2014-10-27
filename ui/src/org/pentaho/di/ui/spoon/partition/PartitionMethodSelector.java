package org.pentaho.di.ui.spoon.partition;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionMethodSelector {

  public String askForPartitionMethod( Shell shell, PartitionSettings settings ) {
    EnterSelectionDialog dialog =
      new EnterSelectionDialog( shell, settings.getOptions(), "Partioning method", "Select the partitioning method" );
    return dialog.open( settings.getDefaultSelectedMethodIndex() );
  }

}
